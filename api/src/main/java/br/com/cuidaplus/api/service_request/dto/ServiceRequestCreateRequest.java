package br.com.cuidaplus.api.service_request.dto;

import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.service_request.HiringType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;

public record ServiceRequestCreateRequest(
  UUID caregiverId,
  @NotNull UUID assistedPersonId,
  @NotNull UUID careAddressId,
  @NotNull(message = "Selecione uma rotina de cuidados.") UUID careRoutineId,
  @NotNull HiringType hiringType,
  LocalDate startDate,
  LocalDate endDate,
  Set<LocalDate> specificDates,
  @Valid List<ScheduleDayRequest> scheduleDays,
  @NotBlank @Size(max = 2000) String needsDescription,
  @Size(max = 2000) String additionalNotes,
  @Size(max = 1000) String negotiationNotes
) {
  public record ScheduleDayRequest(@NotNull DiaSemana weekday, @NotNull LocalTime startTime, @NotNull LocalTime endTime) {}
}
