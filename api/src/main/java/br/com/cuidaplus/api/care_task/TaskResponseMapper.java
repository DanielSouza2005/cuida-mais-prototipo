package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_task.dto.*;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class TaskResponseMapper {
  private final TaskOccurrenceRepository occurrences;
  private final CareActivityRecordRepository activities;
  private final TaskAuditEntryRepository audit;
  private final TaskRecurrenceService recurrence;

  public TaskResponseMapper(TaskOccurrenceRepository occurrences, CareActivityRecordRepository activities, TaskAuditEntryRepository audit, TaskRecurrenceService recurrence) {
    this.occurrences = occurrences; this.activities = activities; this.audit = audit; this.recurrence = recurrence;
  }

  public CareTaskSummaryResponse summary(CareTask task) {
    TaskOccurrence next = occurrences.findByTaskOrderByScheduledInstantUtcDesc(task).stream()
      .filter(item -> item.getStatus() == TaskOccurrenceStatus.PENDENTE && !item.getScheduledInstantUtc().isBefore(Instant.now()))
      .min(Comparator.comparing(TaskOccurrence::getScheduledInstantUtc)).orElse(null);
    return new CareTaskSummaryResponse(
      task.getId(), task.getTitle(), task.getDescription(), task.getCategory(), task.getCustomCategory(), task.getPriority(), task.getRecurrenceType(),
      task.getStartDate(), task.getEndDate(), task.getScheduledTime(), task.getIntervalDays(), new LinkedHashSet<>(task.getWeekdays()), task.getTimezone(),
      task.isReminderEnabled(), task.getReminderMinutesBefore(), task.getNotes(), task.getStatus(), task.getAssistedPerson().getId(), task.getAssistedPerson().getNome(),
      task.getContract().getId(), task.getCaregiverExecutor().getId(), task.getCaregiverExecutor().getFullName(),
      next == null ? null : next.getScheduledDate(), next == null ? null : next.getScheduledTime(), task.getVersion(), task.getCreatedAt(), task.getUpdatedAt()
    );
  }

  public CareTaskDetailsResponse details(CareTask task) {
    List<TaskAuditResponse> auditResponses = audit.findByTaskOrderByCreatedAtDesc(task).stream().limit(30)
      .map(entry -> new TaskAuditResponse(entry.getId(), entry.getAction(), entry.getActor().getFullName(), entry.getDetails(), entry.getCreatedAt())).toList();
    return new CareTaskDetailsResponse(summary(task), medication(task), auditResponses);
  }

  public TaskOccurrenceResponse occurrence(TaskOccurrence occurrence) {
    CareTask task = occurrence.getTask();
    UUID activityId = activities.findByOccurrence(occurrence).map(CareActivityRecord::getId).orElse(null);
    return new TaskOccurrenceResponse(
      occurrence.getId(), task.getId(), task.getTitle(), task.getDescription(), task.getCategory(), task.getCustomCategory(), task.getPriority(),
      occurrence.getScheduledDate(), occurrence.getScheduledTime(), occurrence.getScheduledInstantUtc(), occurrence.getTimezone(), recurrence.effectiveStatus(occurrence),
      occurrence.getAssistedPerson().getId(), occurrence.getAssistedPerson().getNome(), occurrence.getContract().getId(), occurrence.getCaregiver().getId(),
      occurrence.getCaregiver().getFullName(), task.getResponsibleCreator().getId(), task.getResponsibleCreator().getFullName(), medication(task), task.getNotes(),
      occurrence.getCompletedAt(), occurrence.getExecutedBy() == null ? null : occurrence.getExecutedBy().getFullName(), occurrence.getNonCompletionReason(),
      occurrence.getExecutionNote(), activityId, occurrence.isException(), task.getVersion(), occurrence.getVersion()
    );
  }

  public MedicationResponse medication(CareTask task) {
    MedicationDetails value = task.getMedication();
    if (value == null || value.getName() == null) return null;
    return new MedicationResponse(value.getName(), value.getDosage(), value.getUnit(), value.getCustomUnit(), value.getAdministrationRoute(), value.getCustomAdministrationRoute(), value.getAdditionalInstructions());
  }
}
