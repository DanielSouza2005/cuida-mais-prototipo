package br.com.cuidaplus.api.care_task.dto;

import br.com.cuidaplus.api.care_task.*;
import java.time.*;
import java.util.*;

public record TaskOccurrenceResponse(
  UUID id, UUID taskId, String title, String description, TaskCategory category, String customCategory,
  TaskPriority priority, LocalDate scheduledDate, LocalTime scheduledTime, Instant scheduledInstantUtc,
  String timezone, TaskOccurrenceStatus status, UUID assistedPersonId, String assistedPersonName,
  UUID contractId, UUID caregiverId, String caregiverName, UUID responsibleId, String responsibleName,
  MedicationResponse medication, String taskNotes, boolean important, boolean reminderEnabled,
  Integer reminderMinutesBefore, boolean reminderAtScheduledTime, boolean overdueReminderEnabled,
  Integer overdueAfterMinutes, boolean repeatWhilePending, Integer repeatIntervalMinutes,
  String hiringTypeLabel, LocalDate contractStartDate, LocalDate contractEndDate, String careAddress,
  String dependencyLabel, String mobilityLabel, Instant completedAt, String executedByName,
  String nonCompletionReason, String executionNote, UUID activityRecordId, boolean exception,
  boolean requiresCompletionPhoto, boolean autoMarkedNotDone, List<CareOccurrencePhotoResponse> photos,
  long taskVersion, long version
) {}
