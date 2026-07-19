package br.com.cuidaplus.api.care_task.dto;

import br.com.cuidaplus.api.care_task.*;

public record MedicationResponse(
  String name, String dosage, MedicationUnit unit, String customUnit,
  MedicationAdministrationRoute administrationRoute, String customAdministrationRoute,
  String additionalInstructions
) {}
