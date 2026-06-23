package br.com.cuidaplus.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
  @NotBlank(message = "Informe seu e-mail.")
  @Email(message = "Informe um e-mail válido.")
  String email
) {}
