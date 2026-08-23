package br.com.cuidaplus.api.service_request.dto;

import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.profile.GrauDependencia;
import br.com.cuidaplus.api.profile.Mobilidade;
import br.com.cuidaplus.api.profile.ServicoOferecido;
import br.com.cuidaplus.api.service_request.HiringType;
import br.com.cuidaplus.api.service_request.ServiceRequestStatus;
import br.com.cuidaplus.api.service_request.ServiceRequestInitiator;
import br.com.cuidaplus.api.care_routine.dto.StructuredCareItemResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ResponsibleServiceRequestResponse(
  UUID id,
  ServiceRequestStatus status,
  ServiceRequestInitiator initiatedBy,
  Caregiver caregiver,
  Assisted assistedPerson,
  CareAddress careAddress,
  CareRoutine careRoutine,
  HiringType hiringType,
  LocalDate startDate,
  LocalDate endDate,
  Set<LocalDate> specificDates,
  List<Schedule> scheduleDays,
  Set<ServicoOferecido> activities,
  String needsDescription,
  String additionalNotes,
  String negotiationNotes,
  String rejectionReason,
  Instant createdAt,
  Instant expiresAt,
  Instant answeredAt
) {
  public record Caregiver(UUID id, String name, String profilePhotoUrl, String city, String state) {}
  public record Assisted(UUID id, String name, GrauDependencia dependencyLevel, Mobilidade mobility) {}
  public record CareAddress(String street, String number, String complement, String neighborhood, String city, String state, String cep, String referencePoint) {}
  public record Schedule(DiaSemana weekday, LocalTime startTime, LocalTime endTime) {}
  public record CareRoutine(UUID id, String name, List<StructuredCareItemResponse> items) {}
}
