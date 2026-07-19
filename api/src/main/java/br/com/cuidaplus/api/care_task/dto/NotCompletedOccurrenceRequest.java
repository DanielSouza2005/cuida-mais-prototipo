package br.com.cuidaplus.api.care_task.dto;

import jakarta.validation.constraints.*;

public record NotCompletedOccurrenceRequest(
  @NotBlank @Size(max = 1000) String reason,
  @Size(max = 1000) String executionNote,
  @PositiveOrZero long version
) {}
