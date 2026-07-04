package br.com.cuidaplus.api.profile.dto;

import br.com.cuidaplus.api.profile.ServicoOferecido;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record CaregiverServicesUpdateRequest(
  @NotEmpty(message = "Informe ao menos um servico oferecido.")
  Set<ServicoOferecido> servicosOferecidos,

  @Size(max = 180, message = "O servico personalizado deve ter no maximo 180 caracteres.")
  String servicoOutro
) {
  @AssertTrue(message = "Informe o servico personalizado.")
  public boolean isServicoOutroValido() {
    return servicosOferecidos == null || !servicosOferecidos.contains(ServicoOferecido.OUTRO) || (servicoOutro != null && !servicoOutro.isBlank());
  }
}
