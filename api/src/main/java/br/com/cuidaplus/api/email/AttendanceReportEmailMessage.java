package br.com.cuidaplus.api.email;

public record AttendanceReportEmailMessage(
  String responsibleName, String assistedPersonName, String caregiverName, String attendanceDate,
  String startedAt, String endedAt, String finalText, String nursingNotes
) {}
