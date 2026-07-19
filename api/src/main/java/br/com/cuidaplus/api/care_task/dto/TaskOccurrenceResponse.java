package br.com.cuidaplus.api.care_task.dto;

import br.com.cuidaplus.api.care_task.*;
import java.time.*;
import java.util.UUID;

public record TaskOccurrenceResponse(
  UUID id, UUID taskId, String title, String description, TaskCategory category, String customCategory,
  TaskPriority priority, LocalDate scheduledDate, LocalTime scheduledTime, Instant scheduledInstantUtc,
  String timezone, TaskOccurrenceStatus status, UUID assistedPersonId, String assistedPersonName,
  UUID contractId, UUID caregiverId, String caregiverName, UUID responsibleId, String responsibleName,
  MedicationResponse medication, String taskNotes, Instant completedAt, String executedByName,
  String nonCompletionReason, String executionNote, UUID activityRecordId, boolean exception, long taskVersion, long version
) {}
