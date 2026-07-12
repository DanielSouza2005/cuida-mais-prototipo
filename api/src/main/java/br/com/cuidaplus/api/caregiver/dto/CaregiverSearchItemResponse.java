package br.com.cuidaplus.api.caregiver.dto;

import br.com.cuidaplus.api.profile.FormacaoCuidador;
import br.com.cuidaplus.api.profile.ModalidadeAtendimento;
import br.com.cuidaplus.api.profile.ServicoOferecido;
import br.com.cuidaplus.api.profile.TempoExperiencia;
import java.util.Set;
import java.util.UUID;

public record CaregiverSearchItemResponse(
  UUID id,
  String nome,
  String profilePhotoUrl,
  String cidade,
  String bairro,
  String estado,
  Double distanciaKm,
  TempoExperiencia experienciaRange,
  Set<FormacaoCuidador> formacoes,
  String formacaoOutro,
  Set<ServicoOferecido> servicosOferecidos,
  Set<ModalidadeAtendimento> modalidadesAtendimento,
  CaregiverAvailabilityResponse disponibilidadeResumo,
  String biografiaResumo
) {}
