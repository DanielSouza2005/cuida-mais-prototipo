package br.com.cuidaplus.api.contract_history.dto;

import java.util.List;

public record ContractHistoryPageResponse(
  List<ContractHistoryItemResponse> content, int page, int size,
  long totalElements, int totalPages, boolean last
) {}
