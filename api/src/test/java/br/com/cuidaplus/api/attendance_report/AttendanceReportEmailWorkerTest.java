package br.com.cuidaplus.api.attendance_report;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.cuidaplus.api.email.AttendanceReportEmailMessage;
import br.com.cuidaplus.api.email.EmailService;
import br.com.cuidaplus.api.profile.AssistedPerson;
import br.com.cuidaplus.api.service_attendance.ServiceAttendanceRecord;
import br.com.cuidaplus.api.user.User;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceReportEmailWorkerTest {
  @Mock AttendanceReportRepository reports;
  @Mock EmailService email;
  @Mock User responsible;
  @Mock User caregiver;
  @Mock AssistedPerson assistedPerson;

  private UUID reportId;
  private AttendanceReport report;
  private AttendanceReportEmailWorker worker;

  @BeforeEach
  void setUp() {
    reportId = UUID.randomUUID();
    report = new AttendanceReport();
    report.setStatus(AttendanceReportStatus.FINALIZED);
    report.setEmailStatus(AttendanceReportEmailStatus.PENDING);
    report.setEmailRequestedAt(Instant.now());
    report.setResponsible(responsible);
    report.setCaregiver(caregiver);
    report.setAssistedPerson(assistedPerson);
    report.setAttendanceDate(LocalDate.of(2026, 8, 24));
    report.setFinalText("Texto final.");
    report.setNursingNotes("Sem intercorrências registradas.");
    ServiceAttendanceRecord start = attendanceRecord(Instant.parse("2026-08-24T11:00:00Z"));
    ServiceAttendanceRecord end = attendanceRecord(Instant.parse("2026-08-24T20:00:00Z"));
    report.setStartRecord(start);
    report.setEndRecord(end);
    lenient().when(responsible.getEmail()).thenReturn("responsavel@example.test");
    lenient().when(responsible.getFullName()).thenReturn("João Responsável");
    lenient().when(caregiver.getFullName()).thenReturn("Ana Cuidadora");
    lenient().when(assistedPerson.getNome()).thenReturn("Maria");
    when(reports.findForEmailDeliveryById(reportId)).thenReturn(Optional.of(report));
    worker = new AttendanceReportEmailWorker(reports, email);
  }

  @Test
  void sendsPendingReportAndMarksItAsSent() {
    when(email.sendAttendanceReportEmail(eq("responsavel@example.test"), any(AttendanceReportEmailMessage.class))).thenReturn(true);

    worker.deliver(reportId);

    assertEquals(AttendanceReportEmailStatus.SENT, report.getEmailStatus());
    assertEquals(1, report.getEmailAttempts());
    assertNotNull(report.getEmailSentAt());
    assertNull(report.getEmailNextRetryAt());
  }

  @Test
  void failedDeliveryKeepsFinalizedReportAndSchedulesRetry() {
    when(email.sendAttendanceReportEmail(anyString(), any())).thenReturn(false);

    worker.deliver(reportId);

    assertEquals(AttendanceReportStatus.FINALIZED, report.getStatus());
    assertEquals(AttendanceReportEmailStatus.FAILED, report.getEmailStatus());
    assertEquals(1, report.getEmailAttempts());
    assertNotNull(report.getEmailNextRetryAt());
  }

  @Test
  void sentReportIsNeverDeliveredAgain() {
    report.setEmailStatus(AttendanceReportEmailStatus.SENT);

    worker.deliver(reportId);

    verifyNoInteractions(email);
    assertEquals(0, report.getEmailAttempts());
  }

  private ServiceAttendanceRecord attendanceRecord(Instant recordedAt) {
    ServiceAttendanceRecord record = new ServiceAttendanceRecord();
    record.setRecordedAt(recordedAt);
    record.setDeviceTimezone("America/Sao_Paulo");
    return record;
  }
}
