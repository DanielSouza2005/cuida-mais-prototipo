package br.com.cuidaplus.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserDataRequest(
  @NotBlank(message = "Informe seu nome completo.")
  @Size(max = 140, message = "O nome deve ter no máximo 140 caracteres.")
  String nome,

  @NotBlank(message = "Informe seu e-mail.")
  @Email(message = "Informe um e-mail válido.")
  @Size(max = 180, message = "O e-mail deve ter no máximo 180 caracteres.")
  String email,

  @NotBlank(message = "Informe uma senha.")
  @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
  String senha,

  @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
  @Pattern(regexp = "^\\s*$|^\\d{10,11}$|^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$", message = "Informe um telefone válido com DDD.")
  String telefone,

  @AssertTrue(message = "Para criar sua conta, confirme que você tem 18 anos ou mais.")
  boolean maiorDeIdadeConfirmado
) {
  @Override
  public String toString() {
    return "RegisterUserDataRequest[dados=[PROTEGIDOS]]";
  }
}
