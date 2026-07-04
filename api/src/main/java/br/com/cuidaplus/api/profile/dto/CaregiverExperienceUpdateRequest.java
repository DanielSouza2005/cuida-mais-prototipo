package br.com.cuidaplus.api.profile.dto;

import br.com.cuidaplus.api.profile.FormacaoCuidador;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CaregiverExperienceUpdateRequest(
  @NotBlank(message = "Informe sua experiência.")
  @Size(max = 500, message = "A experiência deve ter no máximo 500 caracteres.")
  String experiencia,

  FormacaoCuidador formacao,

  @Size(max = 180, message = "A formação personalizada deve ter no máximo 180 caracteres.")
  String formacaoOutro,

  @Size(max = 500, message = "A biografia deve ter no máximo 500 caracteres.")
  String biografia
) {
  @AssertTrue(message = "Informe a formação personalizada.")
  public boolean isFormacaoOutroValida() {
    return formacao != FormacaoCuidador.OUTRO || (formacaoOutro != null && !formacaoOutro.isBlank());
  }
}
