package br.com.cuidaplus.api.service_attendance.dto;

import br.com.cuidaplus.api.service_attendance.AttendanceStatus;
import java.time.*;
import java.util.UUID;

public record AttendanceSummaryResponse(
  UUID contractId, LocalDate attendanceDate, String assistedPersonName,
  LocalTime scheduledStartTime, LocalTime scheduledEndTime,
  Instant startWindowStart, Instant startWindowEnd, Instant endWindowStart, Instant endWindowEnd,
  AttendanceStatus status, String statusLabel, boolean canStart, boolean canEnd, String actionMessage,
  AttendanceRecordResponse startRecord, AttendanceRecordResponse endRecord
) {}
