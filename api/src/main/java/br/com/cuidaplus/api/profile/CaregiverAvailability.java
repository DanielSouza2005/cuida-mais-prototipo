package br.com.cuidaplus.api.profile;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Embeddable
public class CaregiverAvailability {

  @ElementCollection
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "caregiver_availability_days", joinColumns = @JoinColumn(name = "caregiver_profile_id"))
  @Column(name = "dia_semana", length = 20)
  private Set<DiaSemana> diasSemana = new LinkedHashSet<>();

  @ElementCollection
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "caregiver_availability_periods", joinColumns = @JoinColumn(name = "caregiver_profile_id"))
  @Column(name = "periodo", length = 30)
  private Set<PeriodoDisponibilidade> periodos = new LinkedHashSet<>();

  private LocalTime horarioInicio;

  private LocalTime horarioFim;

  @Column(length = 500)
  private String observacao;

  public Set<DiaSemana> getDiasSemana() {
    return diasSemana;
  }

  public void setDiasSemana(Set<DiaSemana> diasSemana) {
    this.diasSemana = diasSemana;
  }

  public Set<PeriodoDisponibilidade> getPeriodos() {
    return periodos;
  }

  public void setPeriodos(Set<PeriodoDisponibilidade> periodos) {
    this.periodos = periodos;
  }

  public LocalTime getHorarioInicio() {
    return horarioInicio;
  }

  public void setHorarioInicio(LocalTime horarioInicio) {
    this.horarioInicio = horarioInicio;
  }

  public LocalTime getHorarioFim() {
    return horarioFim;
  }

  public void setHorarioFim(LocalTime horarioFim) {
    this.horarioFim = horarioFim;
  }

  public String getObservacao() {
    return observacao;
  }

  public void setObservacao(String observacao) {
    this.observacao = observacao;
  }
}
