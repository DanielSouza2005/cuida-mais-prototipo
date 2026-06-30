package br.com.cuidaplus.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
  @NotBlank(message = "Informe o CEP.")
  @Size(max = 9, message = "O CEP deve ter no maximo 9 caracteres.")
  String cep,

  @NotBlank(message = "Informe a rua.")
  @Size(max = 180, message = "A rua deve ter no maximo 180 caracteres.")
  String rua,

  @NotBlank(message = "Informe o numero.")
  @Size(max = 30, message = "O numero deve ter no maximo 30 caracteres.")
  String numero,

  @Size(max = 120, message = "O complemento deve ter no maximo 120 caracteres.")
  String complemento,

  @NotBlank(message = "Informe o bairro.")
  @Size(max = 120, message = "O bairro deve ter no maximo 120 caracteres.")
  String bairro,

  @NotBlank(message = "Informe a cidade.")
  @Size(max = 120, message = "A cidade deve ter no maximo 120 caracteres.")
  String cidade,

  @NotBlank(message = "Informe o estado.")
  @Size(max = 2, message = "Informe a UF com 2 caracteres.")
  String estado,

  @Size(max = 180, message = "O ponto de referencia deve ter no maximo 180 caracteres.")
  String pontoReferencia
) {}
