package br.com.cuidaplus.api.care_routine.dto;

import br.com.cuidaplus.api.care_task.*;
import br.com.cuidaplus.api.care_task.dto.MedicationRequest;
import br.com.cuidaplus.api.profile.DiaSemana;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import java.time.LocalTime;

public record CareRoutineRequest(
  @NotBlank(message = "Informe o nome da rotina.") @Size(max = 140) String name,
  @Size(max = 1000) String description,
  UUID assistedPersonId,
  @NotEmpty(message = "Adicione pelo menos um cuidado.") @Size(max = 50) @Valid List<Item> items
) {
  public record Item(
    @NotBlank(message = "Informe o título do cuidado.") @Size(max = 140) String title,
    @Size(max = 1000) String description,
    @Min(1) Integer sortOrder,
    @NotNull(message = "Selecione o tipo do cuidado.") TaskCategory category,
    @Size(max = 120) String customCategory,
    @NotNull TaskPriority priority,
    @NotNull TaskRecurrenceType recurrenceType,
    @NotNull LocalTime scheduledTime,
    @Positive Integer intervalDays,
    Set<DiaSemana> weekdays,
    boolean reminderEnabled,
    @PositiveOrZero Integer reminderMinutesBefore,
    @Size(max = 2000) String notes,
    @Valid MedicationRequest medication
  ) {}
}
