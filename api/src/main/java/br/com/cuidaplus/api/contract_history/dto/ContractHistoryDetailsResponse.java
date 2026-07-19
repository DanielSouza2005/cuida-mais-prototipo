package br.com.cuidaplus.api.contract_history.dto;

import br.com.cuidaplus.api.contract_history.ContractHistoryItemType;
import br.com.cuidaplus.api.care_contract.ContractTerminationType;
import br.com.cuidaplus.api.profile.*;
import br.com.cuidaplus.api.service_request.*;
import br.com.cuidaplus.api.status_history.StatusHistoryEntityType;
import java.time.*;
import java.util.*;

public record ContractHistoryDetailsResponse(
  UUID id, ContractHistoryItemType itemType, UUID serviceRequestId, UUID contractId,
  String status, String statusLabel, Caregiver caregiver, Responsible responsible, Assisted assistedPerson,
  CareAddress careAddress, HiringType hiringType, LocalDate startDate, LocalDate endDate,
  Set<LocalDate> specificDates, List<Schedule> scheduleDays, Set<String> activities,
  String needsDescription, String additionalNotes, String negotiationNotes,
  String rejectionReason, String cancellationReason, String closureReason,
  ContractTerminationType terminationType, String terminationReason, String terminationNotes,
  String terminationRequestedByName, Instant terminationRequestedAt, LocalDate effectiveEndDate,
  String cancellationRequestedByName, Instant cancellationRequestedAt, Instant canceledAt,
  Instant createdAt, Instant updatedAt, List<StatusHistoryEntry> statusHistory
) {
  public record Caregiver(UUID id, String name, String profilePhotoUrl, String locationSummary) {}
  public record Responsible(UUID id, String name) {}
  public record Assisted(UUID id, String name, String dependencyLevel, String mobility, List<String> allergies, List<String> foodRestrictions, String notes) {}
  public record CareAddress(String street, String number, String complement, String neighborhood, String city, String state, String cep, String referencePoint) {}
  public record Schedule(DiaSemana weekday, LocalTime startTime, LocalTime endTime) {}
  public record StatusHistoryEntry(UUID id, StatusHistoryEntityType entityType, String previousStatus, String newStatus, String label, String reason, String changedByName, Instant createdAt) {}
}
