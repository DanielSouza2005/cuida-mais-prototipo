package br.com.cuidaplus.api.attendance_report;

import java.time.Duration;
import java.time.Instant;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AttendanceReportEmailRetryScheduler {
  private final AttendanceReportRepository reports;
  private final AttendanceReportEmailWorker worker;

  public AttendanceReportEmailRetryScheduler(AttendanceReportRepository reports, AttendanceReportEmailWorker worker) {
    this.reports = reports;
    this.worker = worker;
  }

  @Scheduled(initialDelayString = "${app.attendance-report.email-initial-delay-ms:120000}",
    fixedDelayString = "${app.attendance-report.email-retry-delay-ms:60000}")
  public void retryPendingDeliveries() {
    Instant now = Instant.now();
    reports.findEmailDeliveryCandidates(AttendanceReportStatus.FINALIZED, AttendanceReportEmailStatus.PENDING,
      AttendanceReportEmailStatus.FAILED, now.minus(Duration.ofMinutes(2)), now, PageRequest.of(0, 20))
      .forEach(worker::deliver);
  }
}
