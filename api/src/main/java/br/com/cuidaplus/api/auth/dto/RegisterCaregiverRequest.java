package br.com.cuidaplus.api.auth.dto;

import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.profile.FormacaoCuidador;
import br.com.cuidaplus.api.profile.ModalidadeAtendimento;
import br.com.cuidaplus.api.profile.PeriodoDisponibilidade;
import br.com.cuidaplus.api.profile.ServicoOferecido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.Set;

public record RegisterCaregiverRequest(
  @Valid
  @NotNull(message = "Informe os dados do cuidador.")
  RegisterUserDataRequest user,

  @Valid
  @NotNull(message = "Informe o endereço do cuidador.")
  AddressRequest address,

  @Valid
  @NotNull(message = "Informe o perfil profissional do cuidador.")
  CaregiverProfileRequest caregiverProfile
) {
  public record CaregiverProfileRequest(
    @NotBlank(message = "Informe sua experiência.")
    @Size(max = 500, message = "A experiência deve ter no máximo 500 caracteres.")
    String experiencia,

    FormacaoCuidador formacao,

    @Size(max = 180, message = "A formação personalizada deve ter no máximo 180 caracteres.")
    String formacaoOutro,

    @Size(max = 500, message = "A biografia deve ter no máximo 500 caracteres.")
    String biografia,

    @NotEmpty(message = "Informe ao menos uma modalidade de atendimento.")
    Set<ModalidadeAtendimento> modalidades,

    @Size(max = 180, message = "A modalidade personalizada deve ter no máximo 180 caracteres.")
    String modalidadeOutro,

    @NotEmpty(message = "Informe ao menos um serviço oferecido.")
    Set<ServicoOferecido> servicosOferecidos,

    @Size(max = 180, message = "O serviço personalizado deve ter no máximo 180 caracteres.")
    String servicoOutro,

    @Valid
    @NotNull(message = "Informe a disponibilidade.")
    AvailabilityRequest disponibilidade
  ) {
    @AssertTrue(message = "Informe a formação personalizada.")
    public boolean isFormacaoOutroValida() {
      return formacao != FormacaoCuidador.OUTRO || (formacaoOutro != null && !formacaoOutro.isBlank());
    }

    @AssertTrue(message = "Informe a modalidade personalizada.")
    public boolean isModalidadeOutroValida() {
      return modalidades == null || !modalidades.contains(ModalidadeAtendimento.OUTRO) || (modalidadeOutro != null && !modalidadeOutro.isBlank());
    }

    @AssertTrue(message = "Informe o serviço personalizado.")
    public boolean isServicoOutroValido() {
      return servicosOferecidos == null || !servicosOferecidos.contains(ServicoOferecido.OUTRO) || (servicoOutro != null && !servicoOutro.isBlank());
    }
  }

  public record AvailabilityRequest(
    @NotEmpty(message = "Informe ao menos um dia da semana.")
    Set<DiaSemana> diasSemana,

    @NotEmpty(message = "Informe ao menos um periodo.")
    Set<PeriodoDisponibilidade> periodos,

    LocalTime horarioInicio,

    LocalTime horarioFim,

    @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres.")
    String observacao
  ) {
    @AssertTrue(message = "Informe horario inicial e final.")
    public boolean isHorarioPersonalizadoValido() {
      return periodos == null || !periodos.contains(PeriodoDisponibilidade.HORARIO_PERSONALIZADO) || (horarioInicio != null && horarioFim != null);
    }
  }
}
