package br.com.cuidaplus.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.cuidaplus.api.auth.dto.RegisterUserDataRequest;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class UserDataMinimizationValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void acceptsRegistrationWithoutPhoneWhenAdultAgeRangeIsConfirmed() {
    var request = request(null, true);

    assertThat(validator.validate(request)).isEmpty();
  }

  @Test
  void rejectsRegistrationWithoutAdultAgeRangeConfirmation() {
    var request = request(null, false);

    assertThat(validator.validate(request))
      .extracting(violation -> violation.getMessage())
      .contains("Para criar sua conta, confirme que você tem 18 anos ou mais.");
  }

  @Test
  void validatesPhoneOnlyWhenItIsProvided() {
    assertThat(validator.validate(request("11999999999", true))).isEmpty();
    assertThat(validator.validate(request("123", true)))
      .extracting(violation -> violation.getMessage())
      .contains("Informe um telefone válido com DDD.");
  }

  @Test
  void publicUserResponseDoesNotExposeRemovedIdentityFields() throws Exception {
    User user = new User();
    user.setFullName("Pessoa Teste");
    user.setEmail("pessoa@example.com");
    user.setMaiorDeIdadeConfirmado(true);

    String json = new ObjectMapper().writeValueAsString(new UserMapper().toResponse(user));

    assertThat(json).doesNotContain("cpf", "birthDate", "dataNascimento", "maiorDeIdadeConfirmado");
  }

  private RegisterUserDataRequest request(String phone, boolean adultConfirmed) {
    return new RegisterUserDataRequest(
      "Pessoa Teste",
      "pessoa@example.com",
      "secret123",
      phone,
      adultConfirmed
    );
  }
}
