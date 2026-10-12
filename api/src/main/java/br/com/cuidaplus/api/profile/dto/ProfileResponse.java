package br.com.cuidaplus.api.profile.dto;

import br.com.cuidaplus.api.profile.Alergia;
import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.profile.FormacaoCuidador;
import br.com.cuidaplus.api.profile.GrauDependencia;
import br.com.cuidaplus.api.profile.Mobilidade;
import br.com.cuidaplus.api.profile.ModalidadeAtendimento;
import br.com.cuidaplus.api.profile.Parentesco;
import br.com.cuidaplus.api.profile.PeriodoDisponibilidade;
import br.com.cuidaplus.api.profile.PreferenciaContato;
import br.com.cuidaplus.api.profile.RestricaoAlimentar;
import br.com.cuidaplus.api.profile.ServicoOferecido;
import br.com.cuidaplus.api.profile.TempoExperiencia;
import br.com.cuidaplus.api.user.dto.UserResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProfileResponse(
  UserResponse user,
  ResponsibleProfileResponse responsibleProfile,
  List<AssistedPersonResponse> assistedPersons,
  CaregiverProfileResponse caregiverProfile
) {
  public record ResponsibleProfileResponse(
    Parentesco parentesco,
    String parentescoOutro,
    PreferenciaContato preferenciaContato
  ) {}

  public record CaregiverProfileResponse(
    Set<FormacaoCuidador> formacoes,
    String formacaoOutro,
    TempoExperiencia tempoExperiencia,
    String experiencia,
    String biografia,
    AddressResponse enderecoAtendimento,
    Set<ModalidadeAtendimento> modalidades,
    String modalidadeOutro,
    Set<ServicoOferecido> servicosOferecidos,
    String servicoOutro,
    AvailabilityResponse disponibilidade
  ) {}

  public record AvailabilityResponse(
    Set<DiaSemana> diasSemana,
    Set<PeriodoDisponibilidade> periodos,
    LocalTime horarioInicio,
    LocalTime horarioFim,
    String observacao
  ) {}

  public record AssistedPersonResponse(
    UUID id,
    String nome,
    LocalDate dataNascimento,
    GrauDependencia grauDependencia,
    Mobilidade mobilidade,
    String mobilidadeOutro,
    Set<Alergia> alergias,
    String alergiasOutro,
    String alergiasDetalhes,
    Set<RestricaoAlimentar> restricoesAlimentares,
    String restricoesAlimentaresOutro,
    String restricoesAlimentaresDetalhes,
    String medicamentos,
    String observacoes,
    AddressResponse enderecoCuidado,
    @JsonInclude(JsonInclude.Include.NON_NULL) EmergencyContactResponse contatoEmergencia
  ) {}

  public record EmergencyContactResponse(
    String nome,
    String telefone,
    String vinculo,
    boolean isResponsibleContact
  ) {}

  public record AddressResponse(
    String cep,
    String rua,
    String numero,
    String complemento,
    String bairro,
    String cidade,
    String estado,
    String pontoReferencia,
    BigDecimal latitude,
    BigDecimal longitude
  ) {}
}
