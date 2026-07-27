package br.com.cuidaplus.api.care_routine.dto;

import java.util.*;

public record CareRoutineFormDataResponse(List<AssistedPerson> assistedPersons) {
  public record AssistedPerson(UUID id, String name) {}
}
