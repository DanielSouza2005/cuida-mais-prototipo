-- Lote 3/7: solicitações, contratações e histórico.
SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '2min';

SELECT cuidaplus_renomear_tabela(nome_antigo, nome_novo)
FROM (VALUES
  ('service_requests', 'solicitacoes_servico'),
  ('service_request_dates', 'datas_solicitacoes_servico'),
  ('service_request_schedule_days', 'dias_agenda_solicitacoes_servico'),
  ('service_request_activities', 'atividades_solicitacoes_servico'),
  ('care_contracts', 'contratacoes'),
  ('status_history', 'historico_status')
) AS tabelas(nome_antigo, nome_novo);

SELECT cuidaplus_renomear_coluna(tabela, nome_antigo, nome_novo)
FROM (VALUES
  ('solicitacoes_servico', 'responsible_user_id', 'usuario_responsavel_id'),
  ('solicitacoes_servico', 'caregiver_user_id', 'usuario_cuidador_id'),
  ('solicitacoes_servico', 'assisted_person_id', 'pessoa_assistida_id'),
  ('solicitacoes_servico', 'hiring_type', 'tipo_contratacao'),
  ('solicitacoes_servico', 'start_date', 'data_inicio'),
  ('solicitacoes_servico', 'end_date', 'data_fim'),
  ('solicitacoes_servico', 'needs_description', 'descricao_necessidades'),
  ('solicitacoes_servico', 'activity_other', 'outra_atividade'),
  ('solicitacoes_servico', 'additional_notes', 'observacoes_adicionais'),
  ('solicitacoes_servico', 'negotiation_notes', 'observacoes_negociacao'),
  ('solicitacoes_servico', 'created_at', 'criado_em'),
  ('solicitacoes_servico', 'updated_at', 'atualizado_em'),
  ('solicitacoes_servico', 'expires_at', 'expira_em'),
  ('solicitacoes_servico', 'canceled_at', 'cancelado_em'),
  ('solicitacoes_servico', 'rejection_reason', 'motivo_rejeicao'),
  ('solicitacoes_servico', 'cancellation_reason', 'motivo_cancelamento'),
  ('solicitacoes_servico', 'care_routine_id', 'rotina_cuidado_id'),
  ('solicitacoes_servico', 'care_routine_name_snapshot', 'nome_rotina_snapshot'),
  ('solicitacoes_servico', 'initiated_by', 'iniciado_por'),
  ('solicitacoes_servico', 'requester_user_id', 'usuario_solicitante_id'),
  ('solicitacoes_servico', 'source_opportunity_id', 'oportunidade_origem_id'),
  ('datas_solicitacoes_servico', 'service_request_id', 'solicitacao_servico_id'),
  ('datas_solicitacoes_servico', 'service_date', 'data_servico'),
  ('dias_agenda_solicitacoes_servico', 'service_request_id', 'solicitacao_servico_id'),
  ('dias_agenda_solicitacoes_servico', 'weekday', 'dia_semana'),
  ('dias_agenda_solicitacoes_servico', 'start_time', 'horario_inicio'),
  ('dias_agenda_solicitacoes_servico', 'end_time', 'horario_fim'),
  ('atividades_solicitacoes_servico', 'service_request_id', 'solicitacao_servico_id'),
  ('atividades_solicitacoes_servico', 'activity', 'atividade'),
  ('contratacoes', 'service_request_id', 'solicitacao_servico_id'),
  ('contratacoes', 'responsible_user_id', 'usuario_responsavel_id'),
  ('contratacoes', 'caregiver_user_id', 'usuario_cuidador_id'),
  ('contratacoes', 'assisted_person_id', 'pessoa_assistida_id'),
  ('contratacoes', 'start_date', 'data_inicio'),
  ('contratacoes', 'end_date', 'data_fim'),
  ('contratacoes', 'cancellation_reason', 'motivo_cancelamento'),
  ('contratacoes', 'closure_reason', 'motivo_encerramento'),
  ('contratacoes', 'termination_type', 'tipo_encerramento'),
  ('contratacoes', 'termination_reason', 'motivo_solicitacao_encerramento'),
  ('contratacoes', 'termination_notes', 'observacoes_encerramento'),
  ('contratacoes', 'termination_requested_by_user_id', 'usuario_solicitante_encerramento_id'),
  ('contratacoes', 'termination_requested_at', 'encerramento_solicitado_em'),
  ('contratacoes', 'effective_end_date', 'data_fim_efetiva'),
  ('contratacoes', 'canceled_at', 'cancelado_em'),
  ('contratacoes', 'cancellation_requested_by_user_id', 'usuario_solicitante_cancelamento_id'),
  ('contratacoes', 'cancellation_requested_at', 'cancelamento_solicitado_em'),
  ('contratacoes', 'created_at', 'criado_em'),
  ('contratacoes', 'updated_at', 'atualizado_em'),
  ('historico_status', 'entity_type', 'tipo_entidade'),
  ('historico_status', 'entity_id', 'entidade_id'),
  ('historico_status', 'previous_status', 'status_anterior'),
  ('historico_status', 'new_status', 'novo_status'),
  ('historico_status', 'changed_by_user_id', 'usuario_alteracao_id'),
  ('historico_status', 'reason', 'motivo'),
  ('historico_status', 'created_at', 'criado_em')
) AS colunas(tabela, nome_antigo, nome_novo);

