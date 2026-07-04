package br.com.cuidaplus.api.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PersonalInfoUpdateRequest(
  @NotBlank(message = "Informe seu nome completo.")
  @Size(max = 140, message = "O nome deve ter no máximo 140 caracteres.")
  String nome,

  @NotBlank(message = "Informe seu telefone.")
  @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
  String telefone
) {}
