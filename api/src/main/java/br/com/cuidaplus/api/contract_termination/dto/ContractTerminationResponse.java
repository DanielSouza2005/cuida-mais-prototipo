package br.com.cuidaplus.api.contract_termination.dto;

import br.com.cuidaplus.api.care_contract.*;
import java.time.*;
import java.util.UUID;

public record ContractTerminationResponse(
  UUID contractId,
  CareContractStatus status,
  LocalDate effectiveEndDate,
  Instant terminationRequestedAt,
  Instant canceledAt,
  String requestedByName,
  ContractTerminationType terminationType,
  String reason,
  String notes,
  Instant updatedAt
) {}
