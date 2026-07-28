package br.com.cuidaplus.api.care_task;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.care_task.dto.*;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.contract_termination.ContractStatusProcessorService;
import br.com.cuidaplus.api.notification.*;
import br.com.cuidaplus.api.user.User;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class TaskOccurrenceServiceTest {
  @Mock CareTaskRepository tasks; @Mock TaskOccurrenceRepository occurrences; @Mock TaskAuthorizationService authorization;
  @Mock TaskRecurrenceService recurrence; @Mock TaskResponseMapper mapper; @Mock TaskAuditService audit;
  @Mock CareActivityIntegrationService activities; @Mock ContractStatusProcessorService contractProcessor;
  @Mock NotificationService notifications; @Mock TaskDateTimeService dateTimes;
  @Mock TaskReminderService reminders;
  @Mock ContractCareTaskProvisioningService provisioning;
  @Mock TaskOccurrenceExpirationService expiration;
  @Mock CareOccurrencePhotoService photoService;
  @Mock User caregiver; @Mock User responsible; @Mock CareContract contract;
  TaskOccurrenceService service;
  CareTask task; TaskOccurrence occurrence; UUID caregiverId; UUID occurrenceId;

  @BeforeEach
  void setUp() {
    service = new TaskOccurrenceService(tasks, occurrences, authorization, recurrence, mapper, audit, activities, contractProcessor, notifications, dateTimes, reminders, provisioning, expiration, photoService);
    caregiverId = UUID.randomUUID(); occurrenceId = UUID.randomUUID();
    task = new CareTask(); task.setStatus(TaskSeriesStatus.ATIVA); task.setResponsibleCreator(responsible); task.setTitle("Administrar medicamento");
    occurrence = mock(TaskOccurrence.class);
    lenient().when(authorization.requireCaregiver(caregiverId)).thenReturn(caregiver);
    lenient().when(occurrences.findByIdAndCaregiver(occurrenceId, caregiver)).thenReturn(Optional.of(occurrence));
    lenient().when(occurrence.getTask()).thenReturn(task); lenient().when(occurrence.getContract()).thenReturn(contract);
    lenient().when(occurrence.getCaregiver()).thenReturn(caregiver); lenient().when(occurrence.getStatus()).thenReturn(TaskOccurrenceStatus.PENDENTE);
    lenient().when(occurrence.getVersion()).thenReturn(2L); lenient().when(contractProcessor.processContractIfDue(contract)).thenReturn(contract);
    LocalDate today = LocalDate.of(2026, 7, 27); lenient().when(occurrence.getScheduledDate()).thenReturn(today); lenient().when(dateTimes.today(anyString())).thenReturn(today); lenient().when(occurrence.getTimezone()).thenReturn("America/Sao_Paulo");
  }

  @Test
  void authorizedCaregiverCompletesAndCreatesActivityInSameServiceOperation() {
    Instant executedAt = Instant.now().minusSeconds(60);
    service.complete(caregiverId, occurrenceId, new CompleteOccurrenceRequest(executedAt, "Executada sem intercorrências.", 2));
    verify(occurrence).setStatus(TaskOccurrenceStatus.CONCLUIDA); verify(occurrence).setCompletedAt(executedAt); verify(occurrence).setExecutedBy(caregiver);
    verify(reminders).cancelFuture(occurrence);
    verify(activities).createForCompletedOccurrence(occurrence, executedAt, "Executada sem intercorrências.");
    verify(audit).record(task, occurrence, caregiver, TaskAuditAction.OCORRENCIA_CONCLUIDA, "Cuidado concluído pelo cuidador.");
  }

  @Test
  void activityFailurePropagatesAndPreventsSuccessfulCompletionResponse() {
    doThrow(new BusinessException("Não foi possível criar o registro de atividade.", HttpStatus.CONFLICT)).when(activities).createForCompletedOccurrence(eq(occurrence), any(), any());
    assertThrows(BusinessException.class, () -> service.complete(caregiverId, occurrenceId, new CompleteOccurrenceRequest(Instant.now(), null, 2)));
    verify(mapper, never()).occurrence(any());
    verify(notifications, never()).create(any(), any(), anyString(), anyString(), any(), any());
  }

  @Test
  void completedOccurrenceCannotBeCompletedAgain() {
    when(occurrence.getStatus()).thenReturn(TaskOccurrenceStatus.CONCLUIDA);
    BusinessException error = assertThrows(BusinessException.class, () -> service.complete(caregiverId, occurrenceId, new CompleteOccurrenceRequest(Instant.now(), null, 2)));
    assertEquals(HttpStatus.CONFLICT, error.getStatus()); verifyNoInteractions(activities);
  }

  @Test
  void nonCompletionRequiresReasonAndPersistsAuthorizedReason() {
    assertThrows(BusinessException.class, () -> service.notCompleted(caregiverId, occurrenceId, new NotCompletedOccurrenceRequest(" ", null, 2)));
    service.notCompleted(caregiverId, occurrenceId, new NotCompletedOccurrenceRequest("Pessoa assistida recusou.", "Responsável avisado.", 2));
    verify(occurrence).setStatus(TaskOccurrenceStatus.NAO_REALIZADA); verify(occurrence).setNonCompletionReason("Pessoa assistida recusou.");
    verify(reminders).cancelFuture(occurrence); verify(reminders).notifyResponsibleNotDone(occurrence);
  }

  @Test
  void occurrenceFromAnotherCaregiverIsNotExposed() {
    when(occurrences.findByIdAndCaregiver(occurrenceId, caregiver)).thenReturn(Optional.empty());
    BusinessException error = assertThrows(BusinessException.class, () -> service.details(caregiverId, occurrenceId));
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
  }

  @Test
  void pausedTaskAndInactiveContractBlockExecution() {
    task.setStatus(TaskSeriesStatus.PAUSADA);
    assertThrows(BusinessException.class, () -> service.complete(caregiverId, occurrenceId, new CompleteOccurrenceRequest(Instant.now(), null, 2)));
    task.setStatus(TaskSeriesStatus.ATIVA); doThrow(new BusinessException("A contratação precisa estar ativa.", HttpStatus.CONFLICT)).when(authorization).requireContractActive(contract);
    assertThrows(BusinessException.class, () -> service.complete(caregiverId, occurrenceId, new CompleteOccurrenceRequest(Instant.now(), null, 2)));
  }

  @Test
  void occurrenceCanOnlyBeUpdatedOnItsScheduledDate() {
    when(occurrence.getScheduledDate()).thenReturn(LocalDate.of(2026, 7, 26));
    BusinessException error = assertThrows(BusinessException.class,
      () -> service.complete(caregiverId, occurrenceId, new CompleteOccurrenceRequest(Instant.now(), null, 2)));
    assertEquals("Este cuidado só pode ser atualizado no dia previsto.", error.getMessage());
    verifyNoInteractions(activities, photoService);
  }

  @Test
  void requiredCompletionPhotoIsAlsoEnforcedByBackend() {
    task.setRequiresCompletionPhoto(true);
    BusinessException error = assertThrows(BusinessException.class,
      () -> service.complete(caregiverId, occurrenceId, new CompleteOccurrenceRequest(Instant.now(), null, 2)));
    assertEquals("Adicione pelo menos uma foto para concluir este cuidado.", error.getMessage());
    verifyNoInteractions(activities, photoService);
  }
}
