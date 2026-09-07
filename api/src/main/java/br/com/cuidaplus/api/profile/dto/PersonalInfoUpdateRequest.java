package br.com.cuidaplus.api.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PersonalInfoUpdateRequest(
  @NotBlank(message = "Informe seu nome completo.")
  @Size(max = 140, message = "O nome deve ter no máximo 140 caracteres.")
  String nome,

  @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
  @Pattern(regexp = "^\\s*$|^\\d{10,11}$|^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$", message = "Informe um telefone válido com DDD.")
  String telefone
) {}
