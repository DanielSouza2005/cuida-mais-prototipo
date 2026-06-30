package br.com.cuidaplus.api.auth.dto;

import br.com.cuidaplus.api.profile.Alergia;
import br.com.cuidaplus.api.profile.GrauDependencia;
import br.com.cuidaplus.api.profile.Mobilidade;
import br.com.cuidaplus.api.profile.Parentesco;
import br.com.cuidaplus.api.profile.PreferenciaContato;
import br.com.cuidaplus.api.profile.RestricaoAlimentar;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

public record RegisterResponsibleRequest(
  @Valid
  @NotNull(message = "Informe os dados do responsavel.")
  RegisterUserDataRequest user,

  @Valid
  @NotNull(message = "Informe o perfil do responsavel.")
  ResponsibleProfileRequest responsibleProfile,

  @Valid
  @NotNull(message = "Informe a pessoa assistida.")
  AssistedPersonRequest assistedPerson
) {
  public record ResponsibleProfileRequest(
    @NotNull(message = "Informe o parentesco.")
    Parentesco parentesco,

    @Size(max = 120, message = "O parentesco personalizado deve ter no maximo 120 caracteres.")
    String parentescoOutro,

    @NotNull(message = "Informe a preferencia de contato.")
    PreferenciaContato preferenciaContato
  ) {
    @AssertTrue(message = "Informe o parentesco personalizado.")
    public boolean isParentescoOutroValido() {
      return parentesco != Parentesco.OUTRO || (parentescoOutro != null && !parentescoOutro.isBlank());
    }
  }

  public record AssistedPersonRequest(
    @NotBlank(message = "Informe o nome da pessoa assistida.")
    @Size(max = 140, message = "O nome deve ter no maximo 140 caracteres.")
    String nome,

    @Pattern(regexp = "|\\d{11}|\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}", message = "Informe um CPF com 11 digitos.")
    String cpf,

    @NotNull(message = "Informe a data de nascimento da pessoa assistida.")
    @PastOrPresent(message = "A data de nascimento nao pode ser futura.")
    LocalDate dataNascimento,

    @NotNull(message = "Informe o grau de dependencia.")
    GrauDependencia grauDependencia,

    @NotNull(message = "Informe a mobilidade.")
    Mobilidade mobilidade,

    @Size(max = 120, message = "A mobilidade personalizada deve ter no maximo 120 caracteres.")
    String mobilidadeOutro,

    @NotEmpty(message = "Informe se a pessoa assistida possui alergias.")
    Set<Alergia> alergias,

    @Size(max = 180, message = "A alergia personalizada deve ter no maximo 180 caracteres.")
    String alergiasOutro,

    @Size(max = 500, message = "Os detalhes da alergia devem ter no maximo 500 caracteres.")
    String alergiasDetalhes,

    @NotEmpty(message = "Informe se ha restricoes alimentares.")
    Set<RestricaoAlimentar> restricoesAlimentares,

    @Size(max = 180, message = "A restricao personalizada deve ter no maximo 180 caracteres.")
    String restricoesAlimentaresOutro,

    @Size(max = 500, message = "Os detalhes da restricao devem ter no maximo 500 caracteres.")
    String restricoesAlimentaresDetalhes,

    @Size(max = 500, message = "Os medicamentos devem ter no maximo 500 caracteres.")
    String medicamentos,

    @Size(max = 500, message = "As observacoes devem ter no maximo 500 caracteres.")
    String observacoes,

    @Valid
    @NotNull(message = "Informe o endereco do cuidado.")
    AddressRequest enderecoCuidado,

    @Valid
    @NotNull(message = "Informe o contato de emergencia.")
    EmergencyContactRequest contatoEmergencia
  ) {
    @AssertTrue(message = "Informe a mobilidade personalizada.")
    public boolean isMobilidadeOutroValida() {
      return mobilidade != Mobilidade.OUTRO || (mobilidadeOutro != null && !mobilidadeOutro.isBlank());
    }
  }

  public record EmergencyContactRequest(
    @NotBlank(message = "Informe o nome do contato de emergencia.")
    @Size(max = 140, message = "O nome deve ter no maximo 140 caracteres.")
    String nome,

    @NotBlank(message = "Informe o telefone do contato de emergencia.")
    @Size(max = 20, message = "O telefone deve ter no maximo 20 caracteres.")
    String telefone,

    @NotBlank(message = "Informe o vinculo do contato de emergencia.")
    @Size(max = 120, message = "O vinculo deve ter no maximo 120 caracteres.")
    String vinculo,

    boolean isResponsibleContact
  ) {}
}
