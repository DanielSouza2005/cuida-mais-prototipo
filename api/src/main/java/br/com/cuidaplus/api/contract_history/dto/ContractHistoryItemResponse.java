package br.com.cuidaplus.api.contract_history.dto;

import br.com.cuidaplus.api.contract_history.*;
import br.com.cuidaplus.api.service_request.HiringType;
import java.time.*;
import java.util.UUID;

public record ContractHistoryItemResponse(
  UUID id, ContractHistoryItemType itemType, UUID serviceRequestId, UUID contractId,
  String participantName, String participantPhotoUrl, String assistedPersonName,
  String status, ContractHistoryStatusGroup statusGroup, HiringType hiringType,
  LocalDate startDate, LocalDate endDate, String scheduleSummary, Instant updatedAt,
  String rejectionReason, String cancellationReason, String closureReason
) {}
