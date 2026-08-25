package br.com.cuidaplus.api.attendance_report;

public enum AttendanceReportStatus {
  DRAFT("Rascunho"), FINALIZED("Finalizado");

  private final String label;
  AttendanceReportStatus(String label) { this.label = label; }
  public String getLabel() { return label; }
}
