package br.com.cuidaplus.api.agenda.dto;

import br.com.cuidaplus.api.agenda.AgendaSourceType;
import br.com.cuidaplus.api.care_contract.CareContractStatus;
import br.com.cuidaplus.api.service_request.HiringType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AgendaEventResponse(
  String id,
  UUID contractId,
  String title,
  String description,
  LocalDateTime startDateTime,
  LocalDateTime endDateTime,
  LocalDate eventDate,
  CareContractStatus status,
  HiringType hiringType,
  String participantName,
  String participantPhotoUrl,
  String assistedPersonName,
  String careAddressSummary,
  AgendaSourceType sourceType,
  boolean hasScheduledTermination,
  LocalDate effectiveEndDate
) {}
