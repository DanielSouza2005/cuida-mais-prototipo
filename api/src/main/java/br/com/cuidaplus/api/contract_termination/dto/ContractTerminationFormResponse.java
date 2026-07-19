package br.com.cuidaplus.api.contract_termination.dto;

import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.contract_termination.*;
import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.service_request.HiringType;
import java.time.*;
import java.util.*;

public record ContractTerminationFormResponse(
  UUID contractId,
  CareContractStatus status,
  ContractTerminationActionType actionType,
  HiringType hiringType,
  LocalDate startDate,
  LocalDate endDate,
  ContractParticipantRole participantRole,
  String assistedPersonName,
  String otherPartyName,
  List<Schedule> scheduleDays,
  List<ContractTerminationType> allowedTerminationTypes
) {
  public record Schedule(DiaSemana weekday, LocalTime startTime, LocalTime endTime) {}
}
