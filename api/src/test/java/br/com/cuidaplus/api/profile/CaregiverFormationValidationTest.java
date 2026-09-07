package br.com.cuidaplus.api.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.cuidaplus.api.auth.dto.RegisterCaregiverRequest;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.profile.dto.CaregiverExperienceUpdateRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CaregiverFormationValidationTest {
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test void registrationRequestRejectsEmptyFormation() {
    var request = registrationProfile(Set.of(), null);

    assertThat(validator.validate(request)).extracting(violation -> violation.getMessage())
      .contains("Informe ao menos uma formação.");
  }

  @Test void profileUpdateRequestRejectsEmptyFormation() {
    var request = new CaregiverExperienceUpdateRequest(TempoExperiencia.DE_1_A_2_ANOS, Set.of(), null, null);

    assertThat(validator.validate(request)).extracting(violation -> violation.getMessage())
      .contains("Informe ao menos uma formação.");
  }

  @Test void acceptsAFormationAndRequiresDescriptionForOther() {
    assertThat(validator.validate(registrationProfile(Set.of(FormacaoCuidador.TECNICO_ENFERMAGEM), null))).isEmpty();
    assertThat(validator.validate(registrationProfile(Set.of(FormacaoCuidador.OUTRO), "")))
      .extracting(violation -> violation.getMessage()).contains("Informe a formação personalizada.");
  }

  @Test void domainPolicyProtectsServicesCalledOutsideTheHttpValidationLayer() {
    assertThatThrownBy(() -> CaregiverFormationPolicy.requireValid(Set.of(), null))
      .isInstanceOfSatisfying(BusinessException.class, error -> {
        assertThat(error.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(error.getMessage()).isEqualTo("Informe ao menos uma formação.");
      });
    assertThat(CaregiverFormationPolicy.requireValid(Set.of(FormacaoCuidador.PRIMEIROS_SOCORROS), null))
      .containsExactly(FormacaoCuidador.PRIMEIROS_SOCORROS);
  }

  private RegisterCaregiverRequest.CaregiverProfileRequest registrationProfile(
    Set<FormacaoCuidador> formations, String customFormation) {
    return new RegisterCaregiverRequest.CaregiverProfileRequest(
      TempoExperiencia.DE_1_A_2_ANOS,
      formations,
      customFormation,
      null,
      Set.of(ModalidadeAtendimento.DIURNO),
      null,
      Set.of(ServicoOferecido.COMPANHIA),
      null,
      new RegisterCaregiverRequest.AvailabilityRequest(
        Set.of(DiaSemana.SEGUNDA), Set.of(PeriodoDisponibilidade.MANHA), null, null, null));
  }
}
