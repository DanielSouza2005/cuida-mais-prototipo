package br.com.cuidaplus.api.agenda.dto;

import br.com.cuidaplus.api.contract_history.dto.ContractHistoryDetailsResponse;
import br.com.cuidaplus.api.service_attendance.dto.AttendanceSummaryResponse;

public record AgendaEventDetailsResponse(
  AgendaEventResponse event,
  ContractHistoryDetailsResponse contract,
  AttendanceSummaryResponse attendance
) {}
