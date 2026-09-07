package br.com.cuidaplus.api.audit;

public enum AuditCategory {
  SEGURANCA("Segurança"),
  ADMINISTRATIVO("Administrativo"),
  PRIVACIDADE("Privacidade"),
  CONTRATACAO("Contratação"),
  ASSISTENCIAL("Assistencial"),
  ATENDIMENTO("Atendimento"),
  RELATORIO("Relatório");

  private final String label;
  AuditCategory(String label) { this.label = label; }
  public String getLabel() { return label; }
}

