package br.com.cuidaplus.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyPasswordResetCodeRequest(
  @NotBlank(message = "Informe o e-mail.") @Email(message = "Informe um e-mail válido.") String email,
  @NotBlank(message = "Informe o código de verificação.")
  @Pattern(regexp = "\\d{6}", message = "O código deve possuir seis dígitos.") String code
) {
  @Override public String toString() {
    return "VerifyPasswordResetCodeRequest[email=[PROTEGIDO], code=[PROTEGIDO]]";
  }
}
