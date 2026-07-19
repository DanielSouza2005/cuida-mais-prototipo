package br.com.cuidaplus.api.agenda.dto;

import br.com.cuidaplus.api.contract_history.dto.ContractHistoryDetailsResponse;

public record AgendaEventDetailsResponse(
  AgendaEventResponse event,
  ContractHistoryDetailsResponse contract
) {}
