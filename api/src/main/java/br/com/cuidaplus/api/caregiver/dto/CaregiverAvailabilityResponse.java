package br.com.cuidaplus.api.caregiver.dto;

import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.profile.PeriodoDisponibilidade;
import java.time.LocalTime;
import java.util.Set;

public record CaregiverAvailabilityResponse(
  Set<DiaSemana> diasSemana,
  Set<PeriodoDisponibilidade> periodos,
  LocalTime horarioInicio,
  LocalTime horarioFim,
  String observacao
) {}
