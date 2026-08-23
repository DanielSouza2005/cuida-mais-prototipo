package br.com.cuidaplus.api.service_request.dto;

import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.service_request.HiringType;
import br.com.cuidaplus.api.service_request.ServiceRequestStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ResponsibleServicePublicationResponse(
  UUID id, ServiceRequestStatus status, HiringType hiringType,
  UUID assistedPersonId, String assistedPersonName,
  LocalDate startDate, LocalDate endDate, Set<LocalDate> specificDates, List<Schedule> scheduleDays,
  String city, String neighborhood, String state, String needsDescription,
  long applicantCount, long pendingApplicantCount, long acceptedApplicantCount,
  List<Application> applications, Instant createdAt, Instant expiresAt
) {
  public record Schedule(DiaSemana weekday, LocalTime startTime, LocalTime endTime) {}
  public record Application(UUID id, ServiceRequestStatus status, UUID caregiverId, String caregiverName, String caregiverProfilePhotoUrl, Instant createdAt) {}
}
