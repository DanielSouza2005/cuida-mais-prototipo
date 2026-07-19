package br.com.cuidaplus.api.care_task.dto;

import br.com.cuidaplus.api.care_task.*;
import jakarta.validation.constraints.Size;

public record MedicationRequest(
  @Size(max = 180) String name,
  @Size(max = 80) String dosage,
  MedicationUnit unit,
  @Size(max = 80) String customUnit,
  MedicationAdministrationRoute administrationRoute,
  @Size(max = 120) String customAdministrationRoute,
  @Size(max = 1000) String additionalInstructions
) {}
