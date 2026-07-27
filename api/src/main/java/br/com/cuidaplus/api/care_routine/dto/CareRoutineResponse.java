package br.com.cuidaplus.api.care_routine.dto;

import java.time.Instant;
import java.util.*;

public record CareRoutineResponse(
  UUID id, String name, String description, boolean active,
  AssistedPerson assistedPerson, List<StructuredCareItemResponse> items, Instant createdAt, Instant updatedAt
) {
  public record AssistedPerson(UUID id, String name) {}
}
