package br.com.cuidaplus.api.account.dto;

import jakarta.validation.constraints.NotBlank;

public record DeleteAccountRequest(
  @NotBlank(message = "Confirme novamente sua identidade para excluir a conta.") String confirmationToken
) {
  @Override public String toString() { return "DeleteAccountRequest[confirmationToken=[PROTEGIDO]]"; }
}
