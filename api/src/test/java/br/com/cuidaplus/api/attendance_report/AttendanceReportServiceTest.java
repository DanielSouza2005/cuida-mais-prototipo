package br.com.cuidaplus.api.attendance_report;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.cuidaplus.api.attendance_report.dto.UpdateAttendanceReportRequest;
import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.care_task.*;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.email.*;
import br.com.cuidaplus.api.notification.*;
import br.com.cuidaplus.api.profile.AssistedPerson;
import br.com.cuidaplus.api.service_attendance.*;
import br.com.cuidaplus.api.user.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AttendanceReportServiceTest {
  @Mock AttendanceReportRepository reports;
  @Mock CareContractRepository contracts;
  @Mock ServiceAttendanceRepository attendance;
  @Mock TaskOccurrenceRepository occurrences;
  @Mock CareActivityRecordRepository activities;
  @Mock CareOccurrencePhotoRepository photos;
  @Mock UserService users;
  @Mock NotificationService notifications;
  @Mock EmailService email;
  @Mock ApplicationEventPublisher events;
  @Mock CareContract contract;
  @Mock User caregiver;
  @Mock User responsible;
  @Mock AssistedPerson assisted;

  private final UUID caregiverId = UUID.randomUUID();
  private final UUID responsibleId = UUID.randomUUID();
  private final UUID contractId = UUID.randomUUID();
  private final LocalDate date = LocalDate.of(2026, 8, 24);
  private ServiceAttendanceRecord start;
  private ServiceAttendanceRecord end;
  private AttendanceReportService service;

  @BeforeEach
  void setUp() {
    when(caregiver.getId()).thenReturn(caregiverId);
    when(caregiver.getUserType()).thenReturn(UserType.CUIDADOR);
    when(caregiver.getFullName()).thenReturn("Ana Cuidadora");
    when(responsible.getId()).thenReturn(responsibleId);
    when(responsible.getFullName()).thenReturn("João Responsável");
    when(responsible.getEmail()).thenReturn("joao@example.test");
    when(contract.getId()).thenReturn(contractId);
    when(contract.getCaregiverUser()).thenReturn(caregiver);
    when(contract.getResponsibleUser()).thenReturn(responsible);
    when(contract.getAssistedPerson()).thenReturn(assisted);
    when(assisted.getNome()).thenReturn("Maria");
    when(users.findById(caregiverId)).thenReturn(caregiver);
    when(contracts.findForUpdateById(contractId)).thenReturn(Optional.of(contract));
    when(contracts.findById(contractId)).thenReturn(Optional.of(contract));
    when(occurrences.findByContractAndScheduledDateOrderByScheduledInstantUtcAsc(contract, date)).thenReturn(List.of());
    when(activities.findByContractAndEntryDateAndSourceTypeOrderByOccurredAtAsc(contract, date, CareRecordSourceType.MANUAL)).thenReturn(List.of());
    when(reports.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(reports.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    start = record(AttendanceRecordType.START, Instant.parse("2026-08-24T11:00:00Z"));
    end = record(AttendanceRecordType.END, Instant.parse("2026-08-24T20:00:00Z"));
    when(attendance.findByContractAndAttendanceDateAndRecordType(contract, date, AttendanceRecordType.START)).thenReturn(Optional.of(start));
    when(attendance.findByContractAndAttendanceDateAndRecordType(contract, date, AttendanceRecordType.END)).thenReturn(Optional.of(end));
    service = new AttendanceReportService(reports, contracts, attendance, occurrences, activities, photos, users, notifications, events);
  }

  @Test
  void generatesDeterministicDraftOnlyAfterAttendanceEnded() {
    var response = service.generate(caregiverId, contractId, date);

    assertEquals(AttendanceReportStatus.DRAFT, response.status());
    assertTrue(response.generatedText().contains("Relatório de atendimento"));
    assertTrue(response.generatedText().contains("08:00 — Atendimento iniciado. Localização registrada."));
    assertTrue(response.generatedText().contains("17:00 — Atendimento encerrado. Localização registrada."));
    assertTrue(response.generatedText().contains("Anotações de enfermagem:"));
    assertFalse(response.generatedText().contains("START"));
  }

  @Test
  void refusesGenerationWithoutEndRecord() {
    when(attendance.findByContractAndAttendanceDateAndRecordType(contract, date, AttendanceRecordType.END)).thenReturn(Optional.empty());

    BusinessException error = assertThrows(BusinessException.class, () -> service.generate(caregiverId, contractId, date));

    assertEquals(HttpStatus.CONFLICT, error.getStatus());
    assertEquals("O relatório só pode ser gerado após o encerramento do atendimento.", error.getMessage());
  }

  @Test
  void reportIncludesOnlyRoutineCareInsideTheOneOffAttendanceWindow() {
    start.setScheduledStartTime(LocalTime.of(20, 0));
    start.setScheduledEndTime(LocalTime.of(23, 0));
    end.setScheduledStartTime(LocalTime.of(20, 0));
    end.setScheduledEndTime(LocalTime.of(23, 0));
    TaskOccurrence morning = occurrence("Cuidado das 08h", LocalTime.of(8, 0), Instant.parse("2026-08-24T11:00:00Z"));
    TaskOccurrence noon = occurrence("Cuidado das 11h30", LocalTime.of(11, 30), Instant.parse("2026-08-24T14:30:00Z"));
    TaskOccurrence night = occurrence("Cuidado das 22h40", LocalTime.of(22, 40), Instant.parse("2026-08-25T01:40:00Z"));
    when(occurrences.findByContractAndScheduledDateOrderByScheduledInstantUtcAsc(contract, date))
      .thenReturn(List.of(morning, noon, night));

    String text = service.generate(caregiverId, contractId, date).generatedText();

    assertFalse(text.contains("Cuidado das 08h"));
    assertFalse(text.contains("Cuidado das 11h30"));
    assertTrue(text.contains("Cuidado das 22h40"));
  }

  @Test
  void finalizesWithoutWaitingForEmailAndNotifiesResponsibleOnce() {
    AttendanceReport draft = generatedDraft();
    when(reports.findByContractAndAttendanceDate(contract, date)).thenReturn(Optional.of(draft));

    var response = service.finalizeReport(caregiverId, contractId, date, new UpdateAttendanceReportRequest("Texto revisado.", "Complemento."));

    assertEquals(AttendanceReportStatus.FINALIZED, response.status());
    assertEquals(AttendanceReportEmailStatus.PENDING, response.emailStatus());
    assertEquals("Texto revisado.", response.finalText());
    verify(notifications).create(eq(responsible), eq(NotificationType.ATTENDANCE_REPORT_AVAILABLE), anyString(), anyString(), eq(RelatedEntityType.ATTENDANCE_REPORT), nullable(UUID.class));
    verify(events).publishEvent(any(AttendanceReportEmailRequested.class));
    verifyNoInteractions(email);
    assertThrows(BusinessException.class, () -> service.finalizeReport(caregiverId, contractId, date, new UpdateAttendanceReportRequest("Outra versão.", null)));
    verify(events, times(1)).publishEvent(any(AttendanceReportEmailRequested.class));
  }

  private AttendanceReport generatedDraft() {
    service.generate(caregiverId, contractId, date);
    ArgumentCaptor<AttendanceReport> captor = ArgumentCaptor.forClass(AttendanceReport.class);
    verify(reports).saveAndFlush(captor.capture());
    return captor.getValue();
  }

  private ServiceAttendanceRecord record(AttendanceRecordType type, Instant instant) {
    ServiceAttendanceRecord value = new ServiceAttendanceRecord();
    value.setContract(contract); value.setAttendanceDate(date); value.setRecordType(type); value.setRecordedAt(instant);
    value.setDeviceTimezone("America/Sao_Paulo"); value.setScheduledStartTime(LocalTime.of(8, 0)); value.setScheduledEndTime(LocalTime.of(17, 0));
    return value;
  }

  private TaskOccurrence occurrence(String title, LocalTime time, Instant instant) {
    CareTask task = new CareTask();
    task.setTitle(title);
    task.setCategory(TaskCategory.PERSONALIZADA);
    TaskOccurrence value = new TaskOccurrence();
    value.setContract(contract);
    value.setTask(task);
    value.setScheduledDate(date);
    value.setScheduledTime(time);
    value.setScheduledInstantUtc(instant);
    value.setStatus(TaskOccurrenceStatus.PENDENTE);
    return value;
  }
}
