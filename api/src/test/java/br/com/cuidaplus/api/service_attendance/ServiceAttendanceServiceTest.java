package br.com.cuidaplus.api.service_attendance;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.contract_termination.ContractStatusProcessorService;
import br.com.cuidaplus.api.notification.NotificationService;
import br.com.cuidaplus.api.notification.NotificationType;
import br.com.cuidaplus.api.profile.*;
import br.com.cuidaplus.api.service_attendance.dto.AttendanceActionRequest;
import br.com.cuidaplus.api.service_request.*;
import br.com.cuidaplus.api.user.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceAttendanceServiceTest {
  @Mock CareContractRepository contracts;
  @Mock ServiceAttendanceRepository records;
  @Mock UserService users;
  @Mock ContractStatusProcessorService statusProcessor;
  @Mock NotificationService notifications;
  @Mock CareContract contract;
  @Mock ServiceRequest request;
  @Mock User caregiver;
  @Mock User responsible;
  @Mock AssistedPerson assisted;
  final UUID caregiverId = UUID.randomUUID();
  final UUID contractId = UUID.randomUUID();
  final LocalDate date = LocalDate.of(2026, 8, 24);
  final List<ServiceAttendanceRecord> stored = new ArrayList<>();

  @BeforeEach
  void setUp() {
    when(users.findById(caregiverId)).thenReturn(caregiver);
    when(caregiver.getId()).thenReturn(caregiverId);
    when(caregiver.getUserType()).thenReturn(UserType.CUIDADOR);
    when(contract.getId()).thenReturn(contractId);
    when(contract.getCaregiverUser()).thenReturn(caregiver);
    when(contract.getResponsibleUser()).thenReturn(responsible);
    when(contract.getAssistedPerson()).thenReturn(assisted);
    when(contract.getServiceRequest()).thenReturn(request);
    when(contract.getStatus()).thenReturn(CareContractStatus.ATIVA);
    when(contract.getStartDate()).thenReturn(date.minusMonths(1));
    when(contract.getEndDate()).thenReturn(date.plusMonths(1));
    when(assisted.getNome()).thenReturn("Maria");
    when(request.getHiringType()).thenReturn(HiringType.PERIODO_DETERMINADO);
    when(request.getSpecificDates()).thenReturn(Set.of());
    when(request.getScheduleDays()).thenReturn(Set.of(schedule()));
    when(contracts.findForUpdateById(contractId)).thenReturn(Optional.of(contract));
    when(statusProcessor.processContractIfDue(contract)).thenReturn(contract);
    when(records.findByContractAndAttendanceDateOrderByRecordedAtAsc(contract, date)).thenAnswer(invocation -> List.copyOf(stored));
    when(records.existsByContractAndAttendanceDateAndRecordType(eq(contract), eq(date), any())).thenAnswer(invocation -> {
      AttendanceRecordType type = invocation.getArgument(2);
      return stored.stream().anyMatch(item -> item.getRecordType() == type);
    });
    when(records.save(any())).thenAnswer(invocation -> { ServiceAttendanceRecord value = invocation.getArgument(0); stored.add(value); return value; });
  }

  @Test
  void startsAtTheExactBeginningOfTheThirtyMinuteWindowAndStoresRealTime() {
    Instant now = Instant.parse("2026-08-24T16:30:00Z");
    ServiceAttendanceService service = serviceAt(now);

    var response = service.start(caregiverId, contractId, request(now, false));

    assertEquals(AttendanceStatus.IN_PROGRESS, response.status());
    assertEquals(now, response.startRecord().recordedAt());
    assertEquals(AttendanceRecordType.START, stored.getFirst().getRecordType());
    assertEquals(now, stored.getFirst().getRecordedAt());
    verify(notifications).create(eq(responsible), eq(NotificationType.SERVICE_ATTENDANCE_STARTED), eq("Atendimento iniciado"), anyString(), any(), eq(contractId), any());
  }

  @Test
  void blocksStartingOneMinuteBeforeTheAllowedWindow() {
    Instant now = Instant.parse("2026-08-24T16:29:00Z");
    BusinessException error = assertThrows(BusinessException.class, () -> serviceAt(now).start(caregiverId, contractId, request(now, false)));
    assertEquals("Ainda não é possível iniciar este atendimento. Você poderá iniciar a partir de 13:30.", error.getMessage());
    verify(records, never()).save(any());
  }

  @Test
  void rejectsMockedLocation() {
    Instant now = Instant.parse("2026-08-24T16:30:00Z");
    BusinessException error = assertThrows(BusinessException.class, () -> serviceAt(now).start(caregiverId, contractId, request(now, true)));
    assertEquals("Não é possível registrar o atendimento com uma localização simulada.", error.getMessage());
  }

  @Test
  void endsAtTheExactBeginningOfTheThirtyMinuteWindow() {
    stored.add(record(AttendanceRecordType.START));
    Instant now = Instant.parse("2026-08-25T00:30:00Z");

    var response = serviceAt(now).end(caregiverId, contractId, request(now, false));

    assertEquals(AttendanceStatus.ENDED, response.status());
    assertEquals(now, response.endRecord().recordedAt());
    verify(notifications).create(eq(responsible), eq(NotificationType.SERVICE_ATTENDANCE_ENDED), eq("Atendimento encerrado"), anyString(), any(), eq(contractId), any());
  }

  @Test
  void blocksEndingOneMinuteAfterTheAllowedWindow() {
    stored.add(record(AttendanceRecordType.START));
    Instant now = Instant.parse("2026-08-25T01:31:00Z");
    BusinessException error = assertThrows(BusinessException.class, () -> serviceAt(now).end(caregiverId, contractId, request(now, false)));
    assertEquals("O prazo para encerrar este atendimento foi encerrado.", error.getMessage());
  }

  @Test
  void requiresStartAndBlocksCareAfterEnd() {
    ServiceAttendanceService service = serviceAt(Instant.parse("2026-08-24T18:00:00Z"));
    assertEquals("Você precisa iniciar o atendimento antes de registrar cuidados.", assertThrows(BusinessException.class, () -> service.requireActiveAttendance(contract, date)).getMessage());
    stored.add(record(AttendanceRecordType.START));
    assertDoesNotThrow(() -> service.requireActiveAttendance(contract, date));
    stored.add(record(AttendanceRecordType.END));
    assertEquals("Este atendimento já foi encerrado. Não é possível registrar novos cuidados.", assertThrows(BusinessException.class, () -> service.requireActiveAttendance(contract, date)).getMessage());
  }

  @Test
  void returnsControlledEmptyContentWhenThereIsNoAttendanceToday() {
    when(contracts.findByCaregiverUserOrderByUpdatedAtDesc(caregiver)).thenReturn(List.of());

    var response = serviceAt(Instant.parse("2026-08-24T15:00:00Z")).today(caregiverId);

    assertNotNull(response.content());
    assertTrue(response.content().isEmpty());
  }

  @Test
  void returnsScheduledActiveContractInTodayAttendance() {
    when(contracts.findByCaregiverUserOrderByUpdatedAtDesc(caregiver)).thenReturn(List.of(contract));

    var response = serviceAt(Instant.parse("2026-08-24T15:00:00Z")).today(caregiverId);

    assertEquals(1, response.content().size());
    assertEquals(contractId, response.content().getFirst().contractId());
    assertEquals(date, response.content().getFirst().attendanceDate());
    assertEquals("Maria", response.content().getFirst().assistedPersonName());
  }

  @Test
  void keepsEndedAttendanceAvailableAsDayContext() {
    when(contracts.findByCaregiverUserOrderByUpdatedAtDesc(caregiver)).thenReturn(List.of(contract));
    stored.add(record(AttendanceRecordType.START));
    stored.add(record(AttendanceRecordType.END));

    var response = serviceAt(Instant.parse("2026-08-25T00:45:00Z")).today(caregiverId);

    assertEquals(1, response.content().size());
    assertEquals(AttendanceStatus.ENDED, response.content().getFirst().status());
  }

  private ServiceAttendanceService serviceAt(Instant now) {
    return new ServiceAttendanceService(contracts, records, users, statusProcessor, new AttendanceScheduleService(), notifications,
      Clock.fixed(now, AttendanceTimeConfig.SERVICE_ZONE));
  }

  private AttendanceActionRequest request(Instant capturedAt, boolean mocked) {
    return new AttendanceActionRequest(date, -23.55052, -46.633308, 18.5, capturedAt, mocked, "America/Sao_Paulo");
  }

  private ServiceRequestScheduleDay schedule() {
    ServiceRequestScheduleDay value = new ServiceRequestScheduleDay();
    value.setWeekday(DiaSemana.SEGUNDA); value.setStartTime(LocalTime.of(14, 0)); value.setEndTime(LocalTime.of(22, 0));
    return value;
  }

  private ServiceAttendanceRecord record(AttendanceRecordType type) {
    ServiceAttendanceRecord value = new ServiceAttendanceRecord();
    value.setContract(contract); value.setAttendanceDate(date); value.setRecordType(type); value.setRecordedAt(Instant.now());
    return value;
  }
}
