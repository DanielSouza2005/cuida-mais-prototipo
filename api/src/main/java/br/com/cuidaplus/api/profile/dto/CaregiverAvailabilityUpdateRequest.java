package br.com.cuidaplus.api.profile.dto;

import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.profile.PeriodoDisponibilidade;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.Set;

public record CaregiverAvailabilityUpdateRequest(
  @NotEmpty(message = "Informe ao menos um dia da semana.")
  Set<DiaSemana> diasSemana,

  @NotEmpty(message = "Informe ao menos um periodo.")
  Set<PeriodoDisponibilidade> periodos,

  LocalTime horarioInicio,

  LocalTime horarioFim,

  @Size(max = 500, message = "A observacao deve ter no maximo 500 caracteres.")
  String observacao
) {
  @AssertTrue(message = "Informe horario inicial e final.")
  public boolean isHorarioPersonalizadoValido() {
    return periodos == null || !periodos.contains(PeriodoDisponibilidade.HORARIO_PERSONALIZADO) || (horarioInicio != null && horarioFim != null);
  }
}
