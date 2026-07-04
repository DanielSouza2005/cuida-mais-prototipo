package br.com.cuidaplus.api.user.dto;

import br.com.cuidaplus.api.user.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
  @NotBlank(message = "Informe seu nome completo.")
  @Size(max = 140, message = "O nome deve ter no máximo 140 caracteres.")
  String fullName,

  @NotBlank(message = "Informe seu CPF.")
  @Pattern(regexp = "\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}", message = "Informe um CPF com 11 dígitos.")
  String cpf,

  @NotBlank(message = "Informe seu e-mail.")
  @Email(message = "Informe um e-mail válido.")
  @Size(max = 180, message = "O e-mail deve ter no máximo 180 caracteres.")
  String email,

  @NotBlank(message = "Informe seu telefone.")
  @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
  String phone,

  @NotBlank(message = "Informe a data no formato dd/mm/aaaa.")
  @Pattern(regexp = "\\d{2}/\\d{2}/\\d{4}", message = "Informe a data no formato dd/mm/aaaa.")
  String birthDate,

  @NotNull(message = "Informe o tipo de conta.")
  UserType userType
) {}
