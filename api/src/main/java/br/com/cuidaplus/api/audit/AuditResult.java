package br.com.cuidaplus.api.audit;

public enum AuditResult {
  SUCESSO("Sucesso"),
  FALHA("Falha"),
  BLOQUEADO_POR_REGRA("Bloqueado por regra");

  private final String label;
  AuditResult(String label) { this.label = label; }
  public String getLabel() { return label; }
}

