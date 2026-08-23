package br.com.cuidaplus.api.service_request.dto;
import br.com.cuidaplus.api.profile.*;
import br.com.cuidaplus.api.service_request.*;
import br.com.cuidaplus.api.care_routine.dto.StructuredCareItemResponse;
import java.time.*;
import java.util.*;
public record ServiceRequestResponse(
  UUID id, ServiceRequestStatus status, ServiceRequestInitiator initiatedBy, HiringType hiringType,
  UUID caregiverId, String caregiverName, String caregiverProfilePhotoUrl,
  UUID assistedPersonId, String assistedPersonName, String careAddress,
  CareRoutine careRoutine,
  LocalDate startDate, LocalDate endDate, Set<LocalDate> specificDates,
  List<Schedule> scheduleDays, String needsDescription, Set<ServicoOferecido> activities,
  String activityOther, String additionalNotes, String negotiationNotes,
  Instant createdAt, Instant expiresAt, Instant canceledAt
) {
  public record Schedule(DiaSemana weekday, LocalTime startTime, LocalTime endTime) {}
  public record CareRoutine(UUID id, String name, List<StructuredCareItemResponse> items) {}
}
