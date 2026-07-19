package br.com.cuidaplus.api.care_task.dto;

import br.com.cuidaplus.api.care_task.*;
import br.com.cuidaplus.api.profile.DiaSemana;
import java.time.*;
import java.util.*;

public record CareTaskSummaryResponse(
  UUID id, String title, String description, TaskCategory category, String customCategory,
  TaskPriority priority, TaskRecurrenceType recurrenceType, LocalDate startDate, LocalDate endDate,
  LocalTime scheduledTime, Integer intervalDays, Set<DiaSemana> weekdays, String timezone,
  boolean reminderEnabled, Integer reminderMinutesBefore, String notes, TaskSeriesStatus status,
  UUID assistedPersonId, String assistedPersonName, UUID contractId, UUID caregiverId, String caregiverName,
  LocalDate nextOccurrenceDate, LocalTime nextOccurrenceTime, long version, Instant createdAt, Instant updatedAt
) {}
