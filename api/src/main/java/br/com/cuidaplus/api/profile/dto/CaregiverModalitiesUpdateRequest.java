package br.com.cuidaplus.api.profile.dto;

import br.com.cuidaplus.api.profile.ModalidadeAtendimento;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record CaregiverModalitiesUpdateRequest(
  @NotEmpty(message = "Informe ao menos uma modalidade de atendimento.")
  Set<ModalidadeAtendimento> modalidades,

  @Size(max = 180, message = "A modalidade personalizada deve ter no máximo 180 caracteres.")
  String modalidadeOutro
) {
  @AssertTrue(message = "Informe a modalidade personalizada.")
  public boolean isModalidadeOutroValida() {
    return modalidades == null || !modalidades.contains(ModalidadeAtendimento.OUTRO) || (modalidadeOutro != null && !modalidadeOutro.isBlank());
  }
}
