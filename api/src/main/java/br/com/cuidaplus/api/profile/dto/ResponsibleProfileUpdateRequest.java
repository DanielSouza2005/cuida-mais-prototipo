package br.com.cuidaplus.api.profile.dto;

import br.com.cuidaplus.api.profile.Parentesco;
import br.com.cuidaplus.api.profile.PreferenciaContato;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResponsibleProfileUpdateRequest(
  @NotNull(message = "Informe o parentesco.")
  Parentesco parentesco,

  @Size(max = 120, message = "O parentesco personalizado deve ter no máximo 120 caracteres.")
  String parentescoOutro,

  @NotNull(message = "Informe a preferência de contato.")
  PreferenciaContato preferenciaContato
) {
  @AssertTrue(message = "Informe o parentesco personalizado.")
  public boolean isParentescoOutroValido() {
    return parentesco != Parentesco.OUTRO || (parentescoOutro != null && !parentescoOutro.isBlank());
  }
}
