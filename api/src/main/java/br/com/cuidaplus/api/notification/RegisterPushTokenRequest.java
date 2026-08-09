package br.com.cuidaplus.api.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterPushTokenRequest(
  @NotBlank(message = "Informe o token de notificação.")
  @Size(max = 255, message = "Token de notificação inválido.")
  @Pattern(regexp = "^(Exponent|Expo)PushToken\\[[^\\]]+\\]$", message = "Token de notificação inválido.")
  String token,
  @NotBlank(message = "Informe a plataforma do dispositivo.")
  @Size(max = 20, message = "Plataforma inválida.")
  String platform,
  @Size(max = 180, message = "Identificador do dispositivo inválido.") String deviceId,
  @Size(max = 40, message = "Versão do aplicativo inválida.") String appVersion
) {}
