package br.com.cuidaplus.api.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DisablePushTokenRequest(
  @NotBlank(message = "Informe o token de notificação.")
  @Size(max = 255, message = "Token de notificação inválido.")
  String token
) {}
