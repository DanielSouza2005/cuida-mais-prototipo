package br.com.cuidaplus.api.service_request.dto;
import br.com.cuidaplus.api.profile.*;
import br.com.cuidaplus.api.service_request.*;
import java.time.*;
import java.util.*;
public record ServiceRequestResponse(
  UUID id, ServiceRequestStatus status, HiringType hiringType,
  UUID caregiverId, String caregiverName, String caregiverProfilePhotoUrl,
  UUID assistedPersonId, String assistedPersonName, String careAddress,
  LocalDate startDate, LocalDate endDate, Set<LocalDate> specificDates,
  List<Schedule> scheduleDays, String needsDescription, Set<ServicoOferecido> activities,
  String activityOther, String additionalNotes, String negotiationNotes,
  Instant createdAt, Instant expiresAt, Instant canceledAt
) {
  public record Schedule(DiaSemana weekday, LocalTime startTime, LocalTime endTime) {}
}
