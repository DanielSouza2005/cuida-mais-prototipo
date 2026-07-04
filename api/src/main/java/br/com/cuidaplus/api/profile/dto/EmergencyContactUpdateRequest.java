package br.com.cuidaplus.api.profile.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public record EmergencyContactUpdateRequest(
  @Size(max = 140, message = "O nome deve ter no máximo 140 caracteres.")
  String nome,

  @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
  String telefone,

  @Size(max = 120, message = "O vínculo deve ter no máximo 120 caracteres.")
  String vinculo,

  boolean isResponsibleContact
) {
  @AssertTrue(message = "Informe nome, telefone e vínculo do contato de emergência.")
  public boolean isContatoManualValido() {
    return isResponsibleContact
      || (nome != null && !nome.isBlank()
        && telefone != null && !telefone.isBlank()
        && vinculo != null && !vinculo.isBlank());
  }
}
