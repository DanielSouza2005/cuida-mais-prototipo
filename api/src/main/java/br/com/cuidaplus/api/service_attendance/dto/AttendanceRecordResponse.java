package br.com.cuidaplus.api.service_attendance.dto;

import br.com.cuidaplus.api.service_attendance.AttendanceRecordType;
import java.time.*;
import java.util.UUID;

public record AttendanceRecordResponse(
  UUID id, AttendanceRecordType type, String label, Instant recordedAt, LocalDate attendanceDate,
  double latitude, double longitude, double accuracy, String addressSnapshot, String deviceTimezone,
  LocalTime scheduledStartTime, LocalTime scheduledEndTime, Instant allowedWindowStart,
  Instant allowedWindowEnd, boolean withinAllowedWindow
) {}
