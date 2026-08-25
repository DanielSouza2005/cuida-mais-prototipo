package br.com.cuidaplus.api.attendance_report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAttendanceReportRequest(
  @NotBlank(message = "O texto do relatório não pode ficar vazio.") @Size(max = 30000, message = "O relatório deve ter no máximo 30.000 caracteres.") String editedText,
  @Size(max = 4000, message = "As observações adicionais devem ter no máximo 4.000 caracteres.") String additionalNotes
) {}
