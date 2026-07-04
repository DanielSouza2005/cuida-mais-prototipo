package br.com.cuidaplus.api.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PersonalInfoUpdateRequest(
  @NotBlank(message = "Informe seu nome completo.")
  @Size(max = 140, message = "O nome deve ter no maximo 140 caracteres.")
  String nome,

  @NotBlank(message = "Informe seu e-mail.")
  @Email(message = "Informe um e-mail valido.")
  @Size(max = 180, message = "O e-mail deve ter no maximo 180 caracteres.")
  String email,

  @NotBlank(message = "Informe seu telefone.")
  @Size(max = 20, message = "O telefone deve ter no maximo 20 caracteres.")
  String telefone,

  @NotNull(message = "Informe a data de nascimento.")
  @PastOrPresent(message = "A data de nascimento nao pode ser futura.")
  LocalDate dataNascimento
) {}
