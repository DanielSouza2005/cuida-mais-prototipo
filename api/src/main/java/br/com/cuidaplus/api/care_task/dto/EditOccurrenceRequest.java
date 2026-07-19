package br.com.cuidaplus.api.care_task.dto;

import jakarta.validation.constraints.*;
import java.time.*;

public record EditOccurrenceRequest(
  @NotNull LocalDate scheduledDate,
  @NotNull LocalTime scheduledTime,
  @NotBlank @Size(max = 80) String timezone,
  @PositiveOrZero long version
) {}
