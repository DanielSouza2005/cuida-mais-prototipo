package br.com.cuidaplus.api.contract_termination.dto;

import br.com.cuidaplus.api.care_contract.ContractTerminationType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record TerminateContractRequest(
  @NotNull(message = "Selecione o tipo de encerramento.") ContractTerminationType terminationType,
  @NotNull(message = "Informe a data efetiva de término.") LocalDate effectiveEndDate,
  @NotBlank(message = "Informe o motivo.") @Size(max = 1000, message = "O motivo deve ter no máximo 1000 caracteres.") String reason,
  @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres.") String notes
) {}
