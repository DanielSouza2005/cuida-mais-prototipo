package br.com.cuidaplus.api.service_attendance;

import java.time.*;

public record AttendanceSchedule(
  LocalDate date,
  LocalTime startTime,
  LocalTime endTime,
  Instant scheduledStart,
  Instant scheduledEnd,
  Instant startWindowStart,
  Instant startWindowEnd,
  Instant endWindowStart,
  Instant endWindowEnd
) {}
