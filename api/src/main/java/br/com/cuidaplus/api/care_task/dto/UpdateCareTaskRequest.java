package br.com.cuidaplus.api.care_task.dto;

import br.com.cuidaplus.api.care_task.*;
import br.com.cuidaplus.api.profile.DiaSemana;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;

public record UpdateCareTaskRequest(
  @NotBlank @Size(max = 140) String title,
  @Size(max = 2000) String description,
  @NotNull TaskCategory category,
  @Size(max = 120) String customCategory,
  @NotNull TaskPriority priority,
  @NotNull TaskRecurrenceType recurrenceType,
  @NotNull LocalDate startDate,
  LocalDate endDate,
  @NotNull LocalTime scheduledTime,
  @Positive Integer intervalDays,
  Set<DiaSemana> weekdays,
  @NotBlank @Size(max = 80) String timezone,
  boolean reminderEnabled,
  @PositiveOrZero Integer reminderMinutesBefore,
  @Size(max = 2000) String notes,
  @NotNull TaskEditScope scope,
  UUID occurrenceId,
  @PositiveOrZero Long occurrenceVersion,
  @PositiveOrZero long version,
  @Valid MedicationRequest medication
) {}
