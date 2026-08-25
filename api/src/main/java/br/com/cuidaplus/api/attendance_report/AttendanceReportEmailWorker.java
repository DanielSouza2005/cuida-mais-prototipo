package br.com.cuidaplus.api.attendance_report;

import br.com.cuidaplus.api.email.AttendanceReportEmailMessage;
import br.com.cuidaplus.api.email.EmailService;
import br.com.cuidaplus.api.service_attendance.AttendanceTimeConfig;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceReportEmailWorker {
  private static final int MAX_ATTEMPTS = 3;
  private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

  private final AttendanceReportRepository reports;
  private final EmailService email;

  public AttendanceReportEmailWorker(AttendanceReportRepository reports, EmailService email) {
    this.reports = reports;
    this.email = email;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void deliver(UUID reportId) {
    AttendanceReport report = reports.findForEmailDeliveryById(reportId).orElse(null);
    if (report == null || report.getStatus() != AttendanceReportStatus.FINALIZED || !canAttempt(report)) return;

    Instant now = Instant.now();
    report.setEmailAttempts(report.getEmailAttempts() + 1);
    report.setEmailNextRetryAt(null);
    String address = report.getResponsible().getEmail();
    if (address == null || address.isBlank() || !address.contains("@")) {
      fail(report, "O responsável não possui um e-mail válido cadastrado.", false, now);
      return;
    }

    ZoneId zone = safeZone(report.getStartRecord().getDeviceTimezone());
    AttendanceReportEmailMessage message = new AttendanceReportEmailMessage(
      report.getResponsible().getFullName(), report.getAssistedPerson().getNome(), report.getCaregiver().getFullName(),
      report.getAttendanceDate().format(DATE), format(report.getStartRecord().getRecordedAt(), zone),
      format(report.getEndRecord().getRecordedAt(), zone), report.getFinalText(), report.getNursingNotes());
    if (email.sendAttendanceReportEmail(address, message)) {
      report.setEmailStatus(AttendanceReportEmailStatus.SENT);
      report.setEmailSentAt(now);
      report.setEmailErrorMessage(null);
      return;
    }
    fail(report, "Não foi possível enviar o e-mail. O relatório permanece disponível no aplicativo.", true, now);
  }

  private boolean canAttempt(AttendanceReport report) {
    if (report.getEmailStatus() == AttendanceReportEmailStatus.PENDING) return true;
    return report.getEmailStatus() == AttendanceReportEmailStatus.FAILED
      && report.getEmailAttempts() < MAX_ATTEMPTS
      && report.getEmailNextRetryAt() != null
      && !report.getEmailNextRetryAt().isAfter(Instant.now());
  }

  private void fail(AttendanceReport report, String message, boolean retryable, Instant now) {
    report.setEmailStatus(AttendanceReportEmailStatus.FAILED);
    report.setEmailErrorMessage(message);
    if (retryable && report.getEmailAttempts() < MAX_ATTEMPTS) {
      report.setEmailNextRetryAt(now.plus(Duration.ofMinutes(15L * report.getEmailAttempts())));
    }
  }

  private ZoneId safeZone(String value) {
    try { return ZoneId.of(value); }
    catch (Exception ignored) { return AttendanceTimeConfig.SERVICE_ZONE; }
  }

  private String format(Instant value, ZoneId zone) {
    return value.atZone(zone).toLocalTime().format(TIME);
  }
}
