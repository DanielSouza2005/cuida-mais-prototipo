package br.com.cuidaplus.api.care_task.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record TaskActionRequest(@PositiveOrZero long version, @Size(max = 500) String reason) {}
