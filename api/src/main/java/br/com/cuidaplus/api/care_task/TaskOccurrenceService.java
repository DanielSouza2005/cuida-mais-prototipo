package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_task.dto.*;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.contract_termination.ContractStatusProcessorService;
import br.com.cuidaplus.api.notification.*;
import br.com.cuidaplus.api.user.User;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskOccurrenceService {
  private final CareTaskRepository tasks;
  private final TaskOccurrenceRepository occurrences;
  private final TaskAuthorizationService authorization;
  private final TaskRecurrenceService recurrence;
  private final TaskResponseMapper mapper;
  private final TaskAuditService audit;
  private final CareActivityIntegrationService activities;
  private final ContractStatusProcessorService contractStatusProcessor;
  private final NotificationService notifications;
  private final TaskDateTimeService dateTimes;

  public TaskOccurrenceService(CareTaskRepository tasks, TaskOccurrenceRepository occurrences, TaskAuthorizationService authorization,
    TaskRecurrenceService recurrence, TaskResponseMapper mapper, TaskAuditService audit, CareActivityIntegrationService activities,
    ContractStatusProcessorService contractStatusProcessor, NotificationService notifications, TaskDateTimeService dateTimes) {
    this.tasks=tasks; this.occurrences=occurrences; this.authorization=authorization; this.recurrence=recurrence; this.mapper=mapper;
    this.audit=audit; this.activities=activities; this.contractStatusProcessor=contractStatusProcessor; this.notifications=notifications; this.dateTimes=dateTimes;
  }

  @Transactional
  public TaskOccurrencePageResponse list(UUID userId, LocalDate start, LocalDate end, TaskCategory category, TaskOccurrenceStatus status,
    UUID assistedPersonId, int page, int size) {
    User caregiver = authorization.requireCaregiver(userId);
    recurrence.validateRange(start, end);
    tasks.findByCaregiverExecutorOrderByUpdatedAtDesc(caregiver).forEach(task -> {
      contractStatusProcessor.processContractIfDue(task.getContract());
      recurrence.generate(task, start, end);
    });
    List<TaskOccurrenceResponse> filtered = occurrences.findByCaregiverAndScheduledDateBetweenOrderByScheduledInstantUtcAsc(caregiver, start, end).stream()
      .filter(item -> item.getTask().getStatus() == TaskSeriesStatus.ATIVA || item.getStatus() != TaskOccurrenceStatus.PENDENTE)
      .filter(item -> activeContract(item) || item.getStatus() != TaskOccurrenceStatus.PENDENTE)
      .filter(item -> category == null || item.getTask().getCategory() == category)
      .filter(item -> status == null || recurrence.effectiveStatus(item) == status)
      .filter(item -> assistedPersonId == null || item.getAssistedPerson().getId().equals(assistedPersonId))
      .map(mapper::occurrence).toList();
    return page(filtered, page, size);
  }

  @Transactional
  public TaskOccurrencePageResponse day(UUID userId, LocalDate date, String timezone, TaskCategory category, TaskOccurrenceStatus status, UUID assistedPersonId) {
    dateTimes.requireZone(timezone);
    return list(userId, date, date, category, status, assistedPersonId, 0, 50);
  }

  @Transactional
  public TaskOccurrenceResponse details(UUID userId, UUID occurrenceId) {
    User caregiver = authorization.requireCaregiver(userId);
    return mapper.occurrence(owned(occurrenceId, caregiver));
  }

  @Transactional
  public TaskOccurrenceResponse complete(UUID userId, UUID occurrenceId, CompleteOccurrenceRequest request) {
    User caregiver = authorization.requireCaregiver(userId);
    TaskOccurrence occurrence = owned(occurrenceId, caregiver);
    validateExecution(occurrence, request.version());
    Instant executedAt = request.executedAt() == null ? Instant.now() : request.executedAt();
    if (executedAt.isAfter(Instant.now().plusSeconds(300))) throw new BusinessException("A data da execução não pode estar no futuro.");
    occurrence.setStatus(TaskOccurrenceStatus.CONCLUIDA); occurrence.setCompletedAt(executedAt); occurrence.setExecutedBy(caregiver); occurrence.setExecutionNote(trim(request.executionNote()));
    occurrences.saveAndFlush(occurrence);
    activities.createForCompletedOccurrence(occurrence, executedAt, trim(request.executionNote()));
    audit.record(occurrence.getTask(), occurrence, caregiver, TaskAuditAction.OCORRENCIA_CONCLUIDA, "Ocorrência concluída pelo cuidador.");
    notifications.create(occurrence.getTask().getResponsibleCreator(), NotificationType.TASK_OCCURRENCE_COMPLETED, "Tarefa concluída", occurrence.getTask().getTitle() + " foi marcada como concluída.", RelatedEntityType.TASK_OCCURRENCE, occurrence.getId());
    return mapper.occurrence(occurrence);
  }

  @Transactional
  public TaskOccurrenceResponse notCompleted(UUID userId, UUID occurrenceId, NotCompletedOccurrenceRequest request) {
    User caregiver = authorization.requireCaregiver(userId);
    TaskOccurrence occurrence = owned(occurrenceId, caregiver);
    validateExecution(occurrence, request.version());
    if (request.reason() == null || request.reason().isBlank()) throw new BusinessException("Informe a justificativa da não realização.");
    occurrence.setStatus(TaskOccurrenceStatus.NAO_REALIZADA); occurrence.setExecutedBy(caregiver); occurrence.setNonCompletionReason(request.reason().trim()); occurrence.setExecutionNote(trim(request.executionNote()));
    audit.record(occurrence.getTask(), occurrence, caregiver, TaskAuditAction.OCORRENCIA_NAO_REALIZADA, "Ocorrência marcada como não realizada.");
    notifications.create(occurrence.getTask().getResponsibleCreator(), NotificationType.TASK_OCCURRENCE_NOT_COMPLETED, "Tarefa não realizada", occurrence.getTask().getTitle() + " foi marcada como não realizada.", RelatedEntityType.TASK_OCCURRENCE, occurrence.getId());
    return mapper.occurrence(occurrence);
  }

  private void validateExecution(TaskOccurrence occurrence, long version) {
    if (occurrence.getVersion() != version) throw new BusinessException("Esta ocorrência foi atualizada em outro dispositivo. Recarregue os dados.", HttpStatus.CONFLICT);
    if (occurrence.getStatus() == TaskOccurrenceStatus.CONCLUIDA) throw new BusinessException("Esta ocorrência já foi concluída.", HttpStatus.CONFLICT);
    if (occurrence.getStatus() == TaskOccurrenceStatus.CANCELADA) throw new BusinessException("Uma ocorrência cancelada não pode ser executada.", HttpStatus.CONFLICT);
    if (occurrence.getStatus() == TaskOccurrenceStatus.NAO_REALIZADA) throw new BusinessException("Esta ocorrência já foi marcada como não realizada.", HttpStatus.CONFLICT);
    if (occurrence.getTask().getStatus() == TaskSeriesStatus.PAUSADA) throw new BusinessException("Esta tarefa está pausada.", HttpStatus.CONFLICT);
    if (occurrence.getTask().getStatus() != TaskSeriesStatus.ATIVA) throw new BusinessException("Esta tarefa não está ativa.", HttpStatus.CONFLICT);
    authorization.requireContractActive(contractStatusProcessor.processContractIfDue(occurrence.getContract()));
  }

  private TaskOccurrence owned(UUID id, User caregiver) {
    return occurrences.findByIdAndCaregiver(id, caregiver).orElseThrow(() -> new BusinessException("Ocorrência não encontrada.", HttpStatus.NOT_FOUND));
  }
  private boolean activeContract(TaskOccurrence occurrence) {
    var status = occurrence.getContract().getStatus();
    return status == br.com.cuidaplus.api.care_contract.CareContractStatus.ATIVA || status == br.com.cuidaplus.api.care_contract.CareContractStatus.ENCERRAMENTO_AGENDADO;
  }
  private String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }
  private TaskOccurrencePageResponse page(List<TaskOccurrenceResponse> items, int page, int size) { int safePage=Math.max(0,page),safeSize=Math.min(Math.max(1,size),50),from=Math.min(safePage*safeSize,items.size()),to=Math.min(from+safeSize,items.size()),pages=items.isEmpty()?0:(int)Math.ceil((double)items.size()/safeSize); return new TaskOccurrencePageResponse(items.subList(from,to),safePage,safeSize,items.size(),pages,safePage+1>=pages); }
}
