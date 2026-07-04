package br.com.cuidaplus.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterUserDataRequest(
  @NotBlank(message = "Informe seu nome completo.")
  @Size(max = 140, message = "O nome deve ter no máximo 140 caracteres.")
  String nome,

  @NotBlank(message = "Informe seu CPF.")
  @Pattern(regexp = "\\d{11}|\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}", message = "Informe um CPF com 11 dígitos.")
  String cpf,

  @NotBlank(message = "Informe seu e-mail.")
  @Email(message = "Informe um e-mail válido.")
  @Size(max = 180, message = "O e-mail deve ter no máximo 180 caracteres.")
  String email,

  @NotBlank(message = "Informe uma senha.")
  @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
  String senha,

  @NotBlank(message = "Informe seu telefone.")
  @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
  String telefone,

  @NotNull(message = "Informe a data de nascimento.")
  @PastOrPresent(message = "A data de nascimento não pode ser futura.")
  LocalDate dataNascimento
) {}
