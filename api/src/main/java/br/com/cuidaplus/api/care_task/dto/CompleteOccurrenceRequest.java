package br.com.cuidaplus.api.care_task.dto;

import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CompleteOccurrenceRequest(Instant executedAt, @Size(max = 1000) String executionNote, long version) {}
