package br.com.cuidaplus.api.service_attendance.dto;

import br.com.cuidaplus.api.service_attendance.AttendanceRecordType;
import java.time.*;
import java.util.UUID;
import br.com.cuidaplus.api.service_attendance.AttendanceLocationStatus;

public record AttendanceRecordResponse(
  UUID id, AttendanceRecordType type, String label, Instant recordedAt, LocalDate attendanceDate,
  Double latitude, Double longitude, Double accuracy, AttendanceLocationStatus locationStatus, String addressSnapshot, String deviceTimezone,
  LocalTime scheduledStartTime, LocalTime scheduledEndTime, Instant allowedWindowStart,
  Instant allowedWindowEnd, boolean withinAllowedWindow
) {}
