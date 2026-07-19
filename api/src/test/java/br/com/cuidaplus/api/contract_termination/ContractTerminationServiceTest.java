package br.com.cuidaplus.api.contract_termination;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.contract_termination.dto.*;
import br.com.cuidaplus.api.notification.*;
import br.com.cuidaplus.api.service_request.*;
import br.com.cuidaplus.api.status_history.*;
import br.com.cuidaplus.api.user.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ContractTerminationServiceTest {
  @Mock CareContractRepository contracts;
  @Mock UserService users;
  @Mock StatusHistoryService history;
  @Mock NotificationService notifications;
  @Mock ContractStatusProcessorService processor;
  @Mock CareContract contract;
  @Mock ServiceRequest serviceRequest;
  @Mock User responsible;
  @Mock User caregiver;
  ContractTerminationService service;
  UUID contractId;
  UUID responsibleId;
  UUID caregiverId;
  AtomicReference<CareContractStatus> status;

  @BeforeEach
  void setUp() {
    service = new ContractTerminationService(contracts, users, history, notifications, processor);
    contractId = UUID.randomUUID(); responsibleId = UUID.randomUUID(); caregiverId = UUID.randomUUID();
    status = new AtomicReference<>(CareContractStatus.ATIVA);
    lenient().when(contract.getId()).thenReturn(contractId);
    lenient().when(contract.getStatus()).thenAnswer(ignored -> status.get());
    lenient().doAnswer(invocation -> { status.set(invocation.getArgument(0)); return null; }).when(contract).setStatus(any());
    lenient().when(contract.getResponsibleUser()).thenReturn(responsible);
    lenient().when(contract.getCaregiverUser()).thenReturn(caregiver);
    lenient().when(responsible.getId()).thenReturn(responsibleId);
    lenient().when(caregiver.getId()).thenReturn(caregiverId);
    lenient().when(responsible.getFullName()).thenReturn("Daniel Oliveira");
    lenient().when(caregiver.getFullName()).thenReturn("Mariana Costa");
    lenient().when(contract.getServiceRequest()).thenReturn(serviceRequest);
    lenient().when(serviceRequest.getHiringType()).thenReturn(HiringType.PERIODO_INDETERMINADO);
    lenient().when(contract.getStartDate()).thenReturn(LocalDate.now().minusDays(10));
    lenient().when(processor.processContractIfDue(contract)).thenReturn(contract);
  }

  @Test
  void responsibleCanTerminateImmediatelyAndCaregiverIsNotified() {
    when(users.findById(responsibleId)).thenReturn(responsible);
    when(contracts.findForUpdateById(contractId)).thenReturn(Optional.of(contract));
    var request = new TerminateContractRequest(ContractTerminationType.ANTECIPADO_RESPONSAVEL, LocalDate.now(), "Mudança na rotina.", null);

    var response = service.terminate(responsibleId, contractId, request);

    assertEquals(CareContractStatus.ENCERRADA, response.status());
    verify(history).record(StatusHistoryEntityType.CARE_CONTRACT, contractId, "ATIVA", "ENCERRADA", responsible, "Mudança na rotina.");
    verify(notifications).create(caregiver, NotificationType.CONTRACT_TERMINATED, "Serviço encerrado", "A contratação foi encerrada.", RelatedEntityType.CARE_CONTRACT, contractId);
  }

  @Test
  void futureTerminationBecomesScheduled() {
    when(users.findById(caregiverId)).thenReturn(caregiver);
    when(contracts.findForUpdateById(contractId)).thenReturn(Optional.of(contract));
    var request = new TerminateContractRequest(ContractTerminationType.ANTECIPADO_CUIDADOR, LocalDate.now().plusDays(5), "Nova escala de trabalho.", "Aviso antecipado.");

    var response = service.terminate(caregiverId, contractId, request);

    assertEquals(CareContractStatus.ENCERRAMENTO_AGENDADO, response.status());
    verify(notifications).create(eq(responsible), eq(NotificationType.CONTRACT_TERMINATION_SCHEDULED), eq("Encerramento agendado"), contains("agendado para"), eq(RelatedEntityType.CARE_CONTRACT), eq(contractId));
  }

  @Test
  void scheduledContractCanBeCanceledBeforeStart() {
    status.set(CareContractStatus.AGENDADA);
    when(contract.getStartDate()).thenReturn(LocalDate.now().plusDays(3));
    when(users.findById(responsibleId)).thenReturn(responsible);
    when(contracts.findForUpdateById(contractId)).thenReturn(Optional.of(contract));

    var response = service.cancelBeforeStart(responsibleId, contractId, new CancelContractBeforeStartRequest("Cuidados reorganizados.", null));

    assertEquals(CareContractStatus.CANCELADA, response.status());
    verify(history).record(StatusHistoryEntityType.CARE_CONTRACT, contractId, "AGENDADA", "CANCELADA", responsible, "Cuidados reorganizados.");
    verify(notifications).create(caregiver, NotificationType.CONTRACT_CANCELED_BEFORE_START, "Contratação cancelada", "A contratação foi cancelada antes do início.", RelatedEntityType.CARE_CONTRACT, contractId);
  }

  @Test
  void caregiverCanCancelBeforeStartAndResponsibleIsNotified() {
    status.set(CareContractStatus.AGENDADA);
    when(contract.getStartDate()).thenReturn(LocalDate.now().plusDays(3));
    when(users.findById(caregiverId)).thenReturn(caregiver);
    when(contracts.findForUpdateById(contractId)).thenReturn(Optional.of(contract));

    var response = service.cancelBeforeStart(caregiverId, contractId, new CancelContractBeforeStartRequest("Indisponibilidade na agenda.", null));

    assertEquals(CareContractStatus.CANCELADA, response.status());
    verify(history).record(StatusHistoryEntityType.CARE_CONTRACT, contractId, "AGENDADA", "CANCELADA", caregiver, "Indisponibilidade na agenda.");
    verify(notifications).create(responsible, NotificationType.CONTRACT_CANCELED_BEFORE_START, "Contratação cancelada", "A contratação foi cancelada antes do início.", RelatedEntityType.CARE_CONTRACT, contractId);
  }

  @Test
  void responsibleCannotUseCaregiverTerminationType() {
    when(users.findById(responsibleId)).thenReturn(responsible);
    when(contracts.findForUpdateById(contractId)).thenReturn(Optional.of(contract));

    BusinessException error = assertThrows(BusinessException.class, () -> service.terminate(responsibleId, contractId,
      new TerminateContractRequest(ContractTerminationType.ANTECIPADO_CUIDADOR, LocalDate.now(), "Motivo", null)));

    assertEquals("Tipo de encerramento inválido para esta contratação.", error.getMessage());
    verifyNoInteractions(history, notifications);
  }

  @Test
  void scheduledTerminationCannotBeChangedAgain() {
    status.set(CareContractStatus.ENCERRAMENTO_AGENDADO);
    when(users.findById(caregiverId)).thenReturn(caregiver);
    when(contracts.findForUpdateById(contractId)).thenReturn(Optional.of(contract));

    BusinessException error = assertThrows(BusinessException.class, () -> service.terminate(caregiverId, contractId,
      new TerminateContractRequest(ContractTerminationType.ANTECIPADO_CUIDADOR, LocalDate.now().plusDays(2), "Outro motivo", null)));

    assertEquals(HttpStatus.CONFLICT, error.getStatus());
    verifyNoInteractions(history, notifications);
  }

  @Test
  void activeContractCannotUseCancellationEndpoint() {
    when(users.findById(responsibleId)).thenReturn(responsible);
    when(contracts.findForUpdateById(contractId)).thenReturn(Optional.of(contract));

    BusinessException error = assertThrows(BusinessException.class, () -> service.cancelBeforeStart(responsibleId, contractId, new CancelContractBeforeStartRequest("Motivo", null)));

    assertEquals("Serviços ativos devem ser encerrados, não cancelados.", error.getMessage());
    verifyNoInteractions(history, notifications);
  }

  @Test
  void nonParticipantCannotChangeContract() {
    UUID strangerId = UUID.randomUUID(); User stranger = mock(User.class);
    when(stranger.getId()).thenReturn(strangerId); when(users.findById(strangerId)).thenReturn(stranger);
    when(contracts.findForUpdateById(contractId)).thenReturn(Optional.of(contract));

    BusinessException error = assertThrows(BusinessException.class, () -> service.terminate(strangerId, contractId, new TerminateContractRequest(ContractTerminationType.ACORDO_ENTRE_PARTES, LocalDate.now(), "Motivo", null)));

    assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    verifyNoInteractions(history, notifications);
  }
}
