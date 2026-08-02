package br.com.cuidaplus.api.care_task.dto;

import java.util.UUID;

public record CareDiaryContractOptionResponse(
  UUID contractId,
  String contractLabel,
  UUID assistedPersonId,
  String assistedPersonName
) {}
