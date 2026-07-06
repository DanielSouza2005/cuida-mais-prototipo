package br.com.cuidaplus.api.profile.dto;

import br.com.cuidaplus.api.profile.FormacaoCuidador;
import br.com.cuidaplus.api.profile.TempoExperiencia;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record CaregiverExperienceUpdateRequest(
  @NotNull(message = "Informe seu tempo de experiência.")
  TempoExperiencia tempoExperiencia,

  FormacaoCuidador formacao,

  Set<FormacaoCuidador> formacoes,

  @Size(max = 180, message = "A formação personalizada deve ter no máximo 180 caracteres.")
  String formacaoOutro,

  @Size(max = 500, message = "A biografia deve ter no máximo 500 caracteres.")
  String biografia
) {
  @AssertTrue(message = "Informe a formação personalizada.")
  public boolean isFormacaoOutroValida() {
    return !containsOutro(formacoes, formacao) || (formacaoOutro != null && !formacaoOutro.isBlank());
  }

  private boolean containsOutro(Set<FormacaoCuidador> formacoes, FormacaoCuidador formacao) {
    return formacao == FormacaoCuidador.OUTRO || (formacoes != null && formacoes.contains(FormacaoCuidador.OUTRO));
  }
}
