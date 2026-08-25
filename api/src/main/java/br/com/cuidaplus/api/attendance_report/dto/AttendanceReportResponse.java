package br.com.cuidaplus.api.attendance_report.dto;

import br.com.cuidaplus.api.attendance_report.*;
import java.time.*;
import java.util.UUID;

public record AttendanceReportResponse(
  UUID id, UUID contractId, LocalDate attendanceDate, String assistedPersonName, String caregiverName,
  LocalTime scheduledStartTime, LocalTime scheduledEndTime, Instant startedAt, Instant endedAt,
  String generatedText, String editableText, String finalText, String additionalNotes, String nursingNotes,
  AttendanceReportStatus status, String statusLabel, AttendanceReportEmailStatus emailStatus,
  Instant generatedAt, Instant editedAt, Instant finalizedAt
) {}
