package br.com.cuidaplus.api.user.dto;

import br.com.cuidaplus.api.user.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
  @NotBlank(message = "Informe seu nome completo.")
  @Size(max = 140, message = "O nome deve ter no máximo 140 caracteres.")
  String fullName,

  @NotBlank(message = "Informe seu e-mail.")
  @Email(message = "Informe um e-mail válido.")
  @Size(max = 180, message = "O e-mail deve ter no máximo 180 caracteres.")
  String email,

  @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
  @jakarta.validation.constraints.Pattern(regexp = "^\\s*$|^\\d{10,11}$|^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$", message = "Informe um telefone válido com DDD.")
  String phone,

  @NotNull(message = "Informe o tipo de conta.")
  UserType userType
) {}
