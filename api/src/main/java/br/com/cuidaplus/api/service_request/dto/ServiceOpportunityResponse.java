package br.com.cuidaplus.api.service_request.dto;

import br.com.cuidaplus.api.care_task.TaskCategory;
import br.com.cuidaplus.api.profile.*;
import br.com.cuidaplus.api.service_request.*;
import java.time.*;
import java.util.*;

public record ServiceOpportunityResponse(
  UUID id,
  UUID applicationId,
  ServiceRequestStatus status,
  ServiceRequestStatus applicationStatus,
  HiringType hiringType,
  LocalDate startDate,
  LocalDate endDate,
  Set<LocalDate> specificDates,
  List<Schedule> scheduleDays,
  String city,
  String neighborhood,
  String state,
  String assistedPersonAlias,
  GrauDependencia dependencyLevel,
  Mobilidade mobility,
  String needsDescription,
  CareRoutine careRoutine,
  Double distanceKm,
  Instant createdAt,
  Instant expiresAt
) {
  public record Schedule(DiaSemana weekday, LocalTime startTime, LocalTime endTime) {}
  public record CareRoutine(UUID id, String name, List<CareItem> items) {}
  public record CareItem(String title, String description, TaskCategory category, String customCategory, LocalTime scheduledTime) {}
}
