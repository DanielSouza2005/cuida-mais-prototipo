package br.com.cuidaplus.api.account.dto;

import jakarta.validation.constraints.NotBlank;

public record ReauthenticateRequest(@NotBlank(message = "Informe sua senha atual.") String senha) {
  @Override public String toString() { return "ReauthenticateRequest[senha=[PROTEGIDA]]"; }
}
