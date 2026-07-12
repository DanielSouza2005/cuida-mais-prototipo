package br.com.cuidaplus.api.caregiver.dto;

import br.com.cuidaplus.api.profile.FormacaoCuidador;
import br.com.cuidaplus.api.profile.ModalidadeAtendimento;
import br.com.cuidaplus.api.profile.ServicoOferecido;
import br.com.cuidaplus.api.profile.TempoExperiencia;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CaregiverDetailsResponse(
  UUID id,
  String nome,
  String cidade,
  String bairro,
  String estado,
  Double distanciaKm,
  TempoExperiencia experienciaRange,
  Set<FormacaoCuidador> formacoes,
  String formacaoOutro,
  String biografia,
  Set<ModalidadeAtendimento> modalidadesAtendimento,
  String modalidadeOutro,
  Set<ServicoOferecido> servicosOferecidos,
  String servicoOutro,
  CaregiverAvailabilityResponse disponibilidade,
  Instant dataCadastro,
  String status
) {}
