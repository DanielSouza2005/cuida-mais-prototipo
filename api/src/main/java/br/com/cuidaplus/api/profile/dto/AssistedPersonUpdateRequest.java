package br.com.cuidaplus.api.profile.dto;

import br.com.cuidaplus.api.profile.Alergia;
import br.com.cuidaplus.api.profile.GrauDependencia;
import br.com.cuidaplus.api.profile.Mobilidade;
import br.com.cuidaplus.api.profile.RestricaoAlimentar;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

public record AssistedPersonUpdateRequest(
  @NotBlank(message = "Informe o nome da pessoa assistida.")
  @Size(max = 140, message = "O nome deve ter no máximo 140 caracteres.")
  String nome,

  @Pattern(regexp = "|\\d{11}|\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}", message = "Informe um CPF com 11 dígitos.")
  String cpf,

  @NotNull(message = "Informe a data de nascimento da pessoa assistida.")
  @PastOrPresent(message = "A data de nascimento não pode ser futura.")
  LocalDate dataNascimento,

  @NotNull(message = "Informe o grau de dependencia.")
  GrauDependencia grauDependencia,

  @NotNull(message = "Informe a mobilidade.")
  Mobilidade mobilidade,

  @Size(max = 120, message = "A mobilidade personalizada deve ter no máximo 120 caracteres.")
  String mobilidadeOutro,

  @NotEmpty(message = "Informe se a pessoa assistida possui alergias.")
  Set<Alergia> alergias,

  @Size(max = 180, message = "A alergia personalizada deve ter no máximo 180 caracteres.")
  String alergiasOutro,

  @Size(max = 500, message = "Os detalhes da alergia devem ter no máximo 500 caracteres.")
  String alergiasDetalhes,

  @NotEmpty(message = "Informe se há restrições alimentares.")
  Set<RestricaoAlimentar> restricoesAlimentares,

  @Size(max = 180, message = "A restrição personalizada deve ter no máximo 180 caracteres.")
  String restricoesAlimentaresOutro,

  @Size(max = 500, message = "Os detalhes da restrição devem ter no máximo 500 caracteres.")
  String restricoesAlimentaresDetalhes,

  @Size(max = 500, message = "Os medicamentos devem ter no máximo 500 caracteres.")
  String medicamentos,

  @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres.")
  String observacoes
) {
  @AssertTrue(message = "Informe a mobilidade personalizada.")
  public boolean isMobilidadeOutroValida() {
    return mobilidade != Mobilidade.OUTRO || (mobilidadeOutro != null && !mobilidadeOutro.isBlank());
  }

  @AssertTrue(message = "Informe a alergia personalizada.")
  public boolean isAlergiasOutroValida() {
    return alergias == null || !alergias.contains(Alergia.OUTRO) || (alergiasOutro != null && !alergiasOutro.isBlank());
  }

  @AssertTrue(message = "Informe a restrição alimentar personalizada.")
  public boolean isRestricoesAlimentaresOutroValida() {
    return restricoesAlimentares == null
      || !restricoesAlimentares.contains(RestricaoAlimentar.OUTRO)
      || (restricoesAlimentaresOutro != null && !restricoesAlimentaresOutro.isBlank());
  }
}
