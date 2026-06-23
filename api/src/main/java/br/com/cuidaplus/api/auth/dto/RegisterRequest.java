package br.com.cuidaplus.api.auth.dto;

import br.com.cuidaplus.api.user.UserType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
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

  @NotBlank(message = "Informe uma senha.")
  @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
  String password,

  @NotBlank(message = "Informe a data no formato dd/mm/aaaa.")
  @Pattern(regexp = "\\d{2}/\\d{2}/\\d{4}", message = "Informe a data no formato dd/mm/aaaa.")
  String birthDate,

  @NotNull(message = "Informe o tipo de conta.")
  UserType userType,

  @AssertTrue(message = "Aceite os Termos e a Política de Privacidade.")
  boolean acceptedTerms
) {}
