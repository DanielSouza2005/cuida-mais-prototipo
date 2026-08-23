package br.com.cuidaplus.api.notification;

public enum NotificationCategory {
  SOLICITACOES("Solicitações"),
  SERVICOS_PUBLICADOS("Serviços publicados"),
  INTERESSES("Interesses enviados"),
  CONTRATACOES("Contratações"),
  CUIDADOS("Cuidados"),
  DIARIO("Diário"),
  SISTEMA("Sistema");

  private final String label;

  NotificationCategory(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
