package br.com.cuidaplus.api.profile.dto;

import br.com.cuidaplus.api.profile.ServicoOferecido;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record CaregiverServicesUpdateRequest(
  @NotEmpty(message = "Informe ao menos um serviço oferecido.")
  Set<ServicoOferecido> servicosOferecidos,

  @Size(max = 180, message = "O serviço personalizado deve ter no máximo 180 caracteres.")
  String servicoOutro
) {
  @AssertTrue(message = "Informe o serviço personalizado.")
  public boolean isServicoOutroValido() {
    return servicosOferecidos == null || !servicosOferecidos.contains(ServicoOferecido.OUTRO) || (servicoOutro != null && !servicoOutro.isBlank());
  }
}
