package br.com.cuidaplus.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
  @NotBlank(message = "Informe o token de recuperacao.")
  String token,

  @NotBlank(message = "Informe uma nova senha.")
  @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
  String password
) {}
