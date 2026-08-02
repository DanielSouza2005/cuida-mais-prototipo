package br.com.cuidaplus.api.care_task;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.cuidaplus.api.care_contract.CareContract;
import br.com.cuidaplus.api.care_contract.CareContractRepository;
import br.com.cuidaplus.api.care_contract.CareContractStatus;
import br.com.cuidaplus.api.care_task.dto.CreateManualCareRequest;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.contract_termination.ContractStatusProcessorService;
import br.com.cuidaplus.api.profile.AssistedPerson;
import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.service_request.HiringType;
import br.com.cuidaplus.api.service_request.ServiceRequest;
import br.com.cuidaplus.api.service_request.ServiceRequestScheduleDay;
import br.com.cuidaplus.api.user.User;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class CareDiaryServiceTest {
  @Mock CareActivityRecordRepository records;
  @Mock CareContractRepository contracts;
  @Mock CareOccurrencePhotoRepository photos;
  @Mock CareOccurrencePhotoService photoService;
  @Mock TaskAuthorizationService authorization;
  @Mock TaskOccurrenceService occurrences;
  @Mock TaskDateTimeService dateTimes;
  @Mock ContractStatusProcessorService statusProcessor;
  @Mock User caregiver;
  @Mock User responsible;
  @Mock AssistedPerson assisted;
  @Mock CareContract contract;
  @Mock ServiceRequest request;

  CareDiaryService service;
  UUID userId;
  UUID caregiverId;
  UUID assistedId;
  UUID contractId;
  LocalDate date;

  @BeforeEach
  void setUp() {
    service = new CareDiaryService(records, contracts, photos, photoService, authorization, occurrences, dateTimes, statusProcessor);
    userId = UUID.randomUUID(); caregiverId = UUID.randomUUID(); assistedId = UUID.randomUUID(); contractId = UUID.randomUUID();
    date = LocalDate.of(2026, 7, 29);
    lenient().when(authorization.requireCaregiver(userId)).thenReturn(caregiver);
    lenient().when(caregiver.getId()).thenReturn(caregiverId);
    lenient().when(contract.getId()).thenReturn(contractId);
    lenient().when(contract.getCaregiverUser()).thenReturn(caregiver);
    lenient().when(contract.getResponsibleUser()).thenReturn(responsible);
    lenient().when(contract.getAssistedPerson()).thenReturn(assisted);
    lenient().when(contract.getServiceRequest()).thenReturn(request);
    lenient().when(contract.getStatus()).thenReturn(CareContractStatus.ATIVA);
    lenient().when(contract.getStartDate()).thenReturn(LocalDate.of(2026, 7, 1));
    lenient().when(contract.getEndDate()).thenReturn(LocalDate.of(2026, 8, 31));
    lenient().when(assisted.getId()).thenReturn(assistedId);
    lenient().when(assisted.getNome()).thenReturn("Maria Aparecida");
    lenient().when(caregiver.getFullName()).thenReturn("Ana Paula");
    lenient().when(request.getHiringType()).thenReturn(HiringType.PERIODO_DETERMINADO);
    lenient().when(request.getScheduleDays()).thenReturn(Set.of(schedule(DiaSemana.QUARTA)));
    lenient().when(contracts.findById(contractId)).thenReturn(Optional.of(contract));
    lenient().when(statusProcessor.processContractIfDue(contract)).thenReturn(contract);
    lenient().when(dateTimes.requireZone("America/Sao_Paulo")).thenReturn(ZoneId.of("America/Sao_Paulo"));
    lenient().when(dateTimes.toInstant(date, LocalTime.of(14, 30), "America/Sao_Paulo")).thenReturn(Instant.parse("2026-07-29T17:30:00Z"));
    lenient().when(records.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    lenient().when(photos.findByActivityRecordOrderByCreatedAtAsc(any())).thenReturn(List.of());
  }

  @Test
  void caregiverCreatesManualCareWithoutGeneratingPlannedOccurrences() {
    service.createManual(userId, request(), List.of());

    ArgumentCaptor<CareActivityRecord> captor = ArgumentCaptor.forClass(CareActivityRecord.class);
    verify(records).saveAndFlush(captor.capture());
    CareActivityRecord saved = captor.getValue();
    assertEquals(CareRecordSourceType.MANUAL, saved.getSourceType());
    assertEquals("OBSERVACAO", saved.getCareType());
    assertEquals(date, saved.getEntryDate());
    assertSame(contract, saved.getContract());
    assertSame(assisted, saved.getAssistedPerson());
    assertSame(caregiver, saved.getCaregiver());
    assertNull(saved.getOccurrence());
    verify(photoService).attach(saved, caregiver, List.of());
    verifyNoInteractions(occurrences);
  }

  @Test
  void caregiverCannotCreateManualCareForAnotherCaregiversContract() {
    User other = mock(User.class);
    when(contract.getCaregiverUser()).thenReturn(other);
    when(other.getId()).thenReturn(UUID.randomUUID());

    BusinessException error = assertThrows(BusinessException.class, () -> service.createManual(userId, request(), List.of()));

    assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    verify(records, never()).saveAndFlush(any());
  }

  @Test
  void caregiverCannotUseAssistedPersonOutsideContract() {
    CreateManualCareRequest request = request();
    request.setAssistedPersonId(UUID.randomUUID());

    BusinessException error = assertThrows(BusinessException.class, () -> service.createManual(userId, request, List.of()));

    assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    verify(records, never()).saveAndFlush(any());
  }

  @Test
  void manualCareIsBlockedOutsideContractSchedule() {
    when(request.getScheduleDays()).thenReturn(Set.of(schedule(DiaSemana.SEGUNDA)));

    BusinessException error = assertThrows(BusinessException.class, () -> service.createManual(userId, request(), List.of()));

    assertEquals(HttpStatus.CONFLICT, error.getStatus());
    verify(records, never()).saveAndFlush(any());
  }

  private CreateManualCareRequest request() {
    CreateManualCareRequest value = new CreateManualCareRequest();
    value.setContractId(contractId); value.setAssistedPersonId(assistedId); value.setEntryDate(date); value.setOccurredTime(LocalTime.of(14, 30));
    value.setCareType(ManualCareType.OBSERVACAO); value.setTitle("Hidratação extra"); value.setDescription("Foi oferecida água após caminhada leve.");
    value.setNotes("Aceitou bem."); value.setTimezone("America/Sao_Paulo");
    return value;
  }

  private ServiceRequestScheduleDay schedule(DiaSemana weekday) {
    ServiceRequestScheduleDay value = new ServiceRequestScheduleDay(); value.setWeekday(weekday); value.setStartTime(LocalTime.of(8, 0)); value.setEndTime(LocalTime.of(18, 0)); return value;
  }
}
