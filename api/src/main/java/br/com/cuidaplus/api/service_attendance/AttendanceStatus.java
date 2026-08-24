package br.com.cuidaplus.api.service_attendance;

public enum AttendanceStatus {
  NOT_STARTED("Não iniciado"),
  CAN_START("Pode iniciar"),
  IN_PROGRESS("Em andamento"),
  CAN_END("Pode encerrar"),
  ENDED("Encerrado"),
  OUTSIDE_WINDOW("Fora do horário permitido"),
  MISSED("Registro não realizado");

  private final String label;
  AttendanceStatus(String label) { this.label = label; }
  public String getLabel() { return label; }
}
