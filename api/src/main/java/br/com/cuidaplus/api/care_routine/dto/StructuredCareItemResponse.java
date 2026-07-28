package br.com.cuidaplus.api.care_routine.dto;

import br.com.cuidaplus.api.care_task.*;
import br.com.cuidaplus.api.profile.DiaSemana;
import java.time.LocalTime;
import java.util.*;

public record StructuredCareItemResponse(
  UUID id, String title, String description, int sortOrder,
  TaskCategory category, String categoryLabel, String customCategory, TaskPriority priority,
  TaskRecurrenceType recurrenceType, LocalTime scheduledTime, Integer intervalDays, Set<DiaSemana> weekdays,
  Boolean reminderEnabled, Integer reminderMinutesBefore, boolean reminderAtScheduledTime,
  boolean overdueReminderEnabled, Integer overdueAfterMinutes, boolean repeatWhilePending,
  Integer repeatIntervalMinutes, boolean important, boolean notifyResponsibleIfImportant, boolean requiresCompletionPhoto,
  String notes, Medication medication
) {
  public record Medication(String name, String dosage, MedicationUnit unit, String customUnit,
    MedicationAdministrationRoute administrationRoute, String customAdministrationRoute, String additionalInstructions) {}
}
