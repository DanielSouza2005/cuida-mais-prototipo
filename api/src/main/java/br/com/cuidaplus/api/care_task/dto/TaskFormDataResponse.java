package br.com.cuidaplus.api.care_task.dto;

import br.com.cuidaplus.api.care_contract.CareContractStatus;
import java.time.LocalDate;
import java.util.*;

public record TaskFormDataResponse(List<ContractOption> contracts) {
  public record ContractOption(
    UUID contractId, CareContractStatus status, LocalDate startDate, LocalDate endDate, LocalDate effectiveEndDate,
    UUID assistedPersonId, String assistedPersonName, UUID caregiverId, String caregiverName
  ) {}
}
