package br.com.cuidaplus.api.agenda;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.contract_history.ResponsibleContractHistoryService;
import br.com.cuidaplus.api.service_attendance.ServiceAttendanceService;
import br.com.cuidaplus.api.service_attendance.AttendanceStatus;
import br.com.cuidaplus.api.service_attendance.dto.AttendanceSummaryResponse;
import br.com.cuidaplus.api.contract_termination.ContractStatusProcessorService;
import br.com.cuidaplus.api.profile.*;
import br.com.cuidaplus.api.service_request.*;
import br.com.cuidaplus.api.user.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {
  @Mock CareContractRepository contracts;
  @Mock UserService users;
  @Mock ContractStatusProcessorService statusProcessor;
  @Mock ResponsibleContractHistoryService contractHistory;
  @Mock ServiceAttendanceService attendance;
  @Mock AttendanceSummaryResponse attendanceSummary;
  @Mock CareContract contract;
  @Mock ServiceRequest request;
  @Mock User responsible;
  @Mock User caregiver;
  @Mock AssistedPerson assisted;
  AgendaService service;
  UUID responsibleId;

  @BeforeEach
  void setUp() {
    service = new AgendaService(contracts, users, statusProcessor, contractHistory, attendance);
    responsibleId = UUID.randomUUID();
    lenient().when(users.findById(responsibleId)).thenReturn(responsible);
    lenient().when(responsible.getUserType()).thenReturn(UserType.RESPONSAVEL);
    lenient().when(contracts.findByResponsibleUserOrderByUpdatedAtDesc(responsible)).thenReturn(List.of(contract));
    lenient().when(statusProcessor.processContractIfDue(contract)).thenReturn(contract);
    lenient().when(contract.getId()).thenReturn(UUID.randomUUID());
    lenient().when(contract.getStatus()).thenReturn(CareContractStatus.ATIVA);
    lenient().when(contract.getStartDate()).thenReturn(LocalDate.of(2026, 7, 1));
    lenient().when(contract.getEndDate()).thenReturn(LocalDate.of(2026, 7, 31));
    lenient().when(contract.getServiceRequest()).thenReturn(request);
    lenient().when(contract.getResponsibleUser()).thenReturn(responsible);
    lenient().when(contract.getCaregiverUser()).thenReturn(caregiver);
    lenient().when(responsible.getId()).thenReturn(UUID.randomUUID());
    lenient().when(caregiver.getId()).thenReturn(UUID.randomUUID());
    lenient().when(contract.getAssistedPerson()).thenReturn(assisted);
    lenient().when(caregiver.getFullName()).thenReturn("Ana Paula");
    lenient().when(caregiver.getProfilePhotoUrl()).thenReturn("/uploads/ana.jpg");
    lenient().when(responsible.getFullName()).thenReturn("Daniel Oliveira");
    lenient().when(assisted.getNome()).thenReturn("Maria Aparecida");
    lenient().when(assisted.getEnderecoCuidado()).thenReturn(address());
    lenient().when(request.getHiringType()).thenReturn(HiringType.PERIODO_DETERMINADO);
    lenient().when(attendance.details(any(), any(), any())).thenReturn(attendanceSummary);
    lenient().when(attendanceSummary.status()).thenReturn(AttendanceStatus.NOT_STARTED);
    lenient().when(attendanceSummary.statusLabel()).thenReturn("Não iniciado");
    lenient().when(request.getSpecificDates()).thenReturn(new LinkedHashSet<>());
    lenient().when(request.getScheduleDays()).thenReturn(Set.of(
      schedule(DiaSemana.SEGUNDA, 8, 12),
      schedule(DiaSemana.QUARTA, 9, 13)
    ));
  }

  @Test
  void determinedContractGeneratesOnlyContractedWeekdaysInRange() {
    var response = service.events(responsibleId, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 7), AgendaViewMode.WEEK);

    assertEquals(List.of(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 6)), response.content().stream().map(event -> event.eventDate()).toList());
    assertEquals("Ana Paula", response.content().get(0).participantName());
    assertEquals("Cuidado de Maria Aparecida", response.content().get(0).title());
    assertEquals(LocalTime.of(9, 0), response.content().get(0).startDateTime().toLocalTime());
  }

  @Test
  void pointContractUsesOnlySpecificDatesAndFallsBackToSavedSchedule() {
    when(request.getHiringType()).thenReturn(HiringType.PONTUAL);
    when(request.getSpecificDates()).thenReturn(new LinkedHashSet<>(List.of(
      LocalDate.of(2026, 7, 20),
      LocalDate.of(2026, 7, 22),
      LocalDate.of(2026, 8, 3)
    )));
    when(request.getScheduleDays()).thenReturn(Set.of(schedule(DiaSemana.SEGUNDA, 8, 12)));

    var response = service.events(responsibleId, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), AgendaViewMode.MONTH);

    assertEquals(List.of(LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 22)), response.content().stream().map(event -> event.eventDate()).toList());
    assertTrue(response.content().stream().allMatch(event -> event.startDateTime().toLocalTime().equals(LocalTime.of(8, 0))));
  }

  @Test
  void scheduledTerminationCapsFutureOccurrences() {
    when(contract.getStatus()).thenReturn(CareContractStatus.ENCERRAMENTO_AGENDADO);
    when(contract.getEffectiveEndDate()).thenReturn(LocalDate.of(2026, 7, 8));

    var response = service.events(responsibleId, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), AgendaViewMode.MONTH);

    assertFalse(response.content().isEmpty());
    assertTrue(response.content().stream().allMatch(event -> !event.eventDate().isAfter(LocalDate.of(2026, 7, 8))));
    assertTrue(response.content().stream().allMatch(event -> event.hasScheduledTermination()));
  }

  @Test
  void caregiverReceivesResponsibleAsParticipantAndUsesOwnContracts() {
    UUID caregiverId = UUID.randomUUID();
    when(users.findById(caregiverId)).thenReturn(caregiver);
    when(caregiver.getUserType()).thenReturn(UserType.CUIDADOR);
    when(contracts.findByCaregiverUserOrderByUpdatedAtDesc(caregiver)).thenReturn(List.of(contract));

    var response = service.events(caregiverId, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1), AgendaViewMode.DAY);

    assertEquals("Daniel Oliveira", response.content().get(0).participantName());
    verify(contracts).findByCaregiverUserOrderByUpdatedAtDesc(caregiver);
    verify(contracts, never()).findByResponsibleUserOrderByUpdatedAtDesc(caregiver);
  }

  @Test
  void canceledContractsAndOversizedRangesAreRejectedFromResults() {
    when(contract.getStatus()).thenReturn(CareContractStatus.CANCELADA);
    assertTrue(service.events(responsibleId, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), AgendaViewMode.MONTH).content().isEmpty());

    assertThrows(BusinessException.class, () -> service.events(
      responsibleId,
      LocalDate.of(2026, 1, 1),
      LocalDate.of(2026, 1, 1).plusDays(90),
      AgendaViewMode.MONTH
    ));
  }

  private ServiceRequestScheduleDay schedule(DiaSemana weekday, int startHour, int endHour) {
    ServiceRequestScheduleDay schedule = new ServiceRequestScheduleDay();
    schedule.setWeekday(weekday);
    schedule.setStartTime(LocalTime.of(startHour, 0));
    schedule.setEndTime(LocalTime.of(endHour, 0));
    return schedule;
  }

  private AddressFields address() {
    AddressFields address = new AddressFields();
    address.setBairro("Jardim São Francisco");
    address.setCidade("Santa Bárbara d'Oeste");
    address.setEstado("SP");
    return address;
  }
}
