package br.com.cuidaplus.api.auth.dto;

import br.com.cuidaplus.api.user.UserType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
  @NotBlank(message = "Informe seu nome completo.")
  @Size(max = 140, message = "O nome deve ter no máximo 140 caracteres.")
  String fullName,

  @NotBlank(message = "Informe seu e-mail.")
  @Email(message = "Informe um e-mail válido.")
  @Size(max = 180, message = "O e-mail deve ter no máximo 180 caracteres.")
  String email,

  @NotBlank(message = "Informe uma senha.")
  @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
  String password,

  @AssertTrue(message = "Para criar sua conta, confirme que você tem 18 anos ou mais.")
  boolean maiorDeIdadeConfirmado,

  @NotNull(message = "Informe o tipo de conta.")
  UserType userType,

  @AssertTrue(message = "Aceite os Termos e a Política de Privacidade.")
  boolean acceptedTerms
) {}
