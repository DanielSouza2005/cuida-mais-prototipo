-- Lote 6/7: notificações, preferências, atendimento e relatório.
SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '2min';

SELECT cuidaplus_renomear_tabela(nome_antigo, nome_novo)
FROM (VALUES
  ('notifications', 'notificacoes'),
  ('user_notification_preferences', 'preferencias_notificacoes_usuarios'),
  ('service_attendance_records', 'registros_atendimento'),
  ('attendance_reports', 'relatorios_atendimento')
) AS tabelas(nome_antigo, nome_novo);

SELECT cuidaplus_renomear_coluna(tabela, nome_antigo, nome_novo)
FROM (VALUES
  ('notificacoes', 'recipient_user_id', 'usuario_destinatario_id'),
  ('notificacoes', 'type', 'tipo'),
  ('notificacoes', 'title', 'titulo'),
  ('notificacoes', 'message', 'mensagem'),
  ('notificacoes', 'related_entity_type', 'tipo_entidade_relacionada'),
  ('notificacoes', 'related_entity_id', 'entidade_relacionada_id'),
  ('notificacoes', 'read_at', 'lida_em'),
  ('notificacoes', 'cleared_at', 'removida_em'),
  ('notificacoes', 'deduplication_key', 'chave_deduplicacao'),
  ('notificacoes', 'created_at', 'criado_em'),
  ('preferencias_notificacoes_usuarios', 'user_id', 'usuario_id'),
  ('preferencias_notificacoes_usuarios', 'notification_type', 'tipo_notificacao'),
  ('preferencias_notificacoes_usuarios', 'enabled', 'habilitado'),
  ('preferencias_notificacoes_usuarios', 'created_at', 'criado_em'),
  ('preferencias_notificacoes_usuarios', 'updated_at', 'atualizado_em'),
  ('registros_atendimento', 'contract_id', 'contratacao_id'),
  ('registros_atendimento', 'caregiver_id', 'cuidador_id'),
  ('registros_atendimento', 'responsible_id', 'responsavel_id'),
  ('registros_atendimento', 'assisted_person_id', 'pessoa_assistida_id'),
  ('registros_atendimento', 'attendance_date', 'data_atendimento'),
  ('registros_atendimento', 'record_type', 'tipo_registro'),
  ('registros_atendimento', 'recorded_at', 'registrado_em'),
  ('registros_atendimento', 'accuracy', 'precisao'),
  ('registros_atendimento', 'location_captured_at', 'localizacao_capturada_em'),
  ('registros_atendimento', 'address_snapshot', 'endereco_registrado'),
  ('registros_atendimento', 'device_timezone', 'fuso_dispositivo'),
  ('registros_atendimento', 'scheduled_start_time', 'horario_inicio_previsto'),
  ('registros_atendimento', 'scheduled_end_time', 'horario_fim_previsto'),
  ('registros_atendimento', 'allowed_window_start', 'janela_permitida_inicio'),
  ('registros_atendimento', 'allowed_window_end', 'janela_permitida_fim'),
  ('registros_atendimento', 'within_allowed_window', 'dentro_janela_permitida'),
  ('registros_atendimento', 'created_at', 'criado_em'),
  ('registros_atendimento', 'updated_at', 'atualizado_em'),
  ('relatorios_atendimento', 'contract_id', 'contratacao_id'),
  ('relatorios_atendimento', 'attendance_date', 'data_atendimento'),
  ('relatorios_atendimento', 'attendance_start_record_id', 'registro_inicio_atendimento_id'),
  ('relatorios_atendimento', 'attendance_end_record_id', 'registro_fim_atendimento_id'),
  ('relatorios_atendimento', 'caregiver_id', 'cuidador_id'),
  ('relatorios_atendimento', 'responsible_id', 'responsavel_id'),
  ('relatorios_atendimento', 'assisted_person_id', 'pessoa_assistida_id'),
  ('relatorios_atendimento', 'generated_text', 'texto_gerado'),
  ('relatorios_atendimento', 'edited_text', 'texto_editado'),
  ('relatorios_atendimento', 'final_text', 'texto_final'),
  ('relatorios_atendimento', 'additional_notes', 'observacoes_adicionais'),
  ('relatorios_atendimento', 'nursing_notes', 'anotacoes_enfermagem'),
  ('relatorios_atendimento', 'email_status', 'status_email'),
  ('relatorios_atendimento', 'email_sent_at', 'email_enviado_em'),
  ('relatorios_atendimento', 'email_error_message', 'mensagem_erro_email'),
  ('relatorios_atendimento', 'generated_at', 'gerado_em'),
  ('relatorios_atendimento', 'edited_at', 'editado_em'),
  ('relatorios_atendimento', 'finalized_at', 'finalizado_em'),
  ('relatorios_atendimento', 'created_at', 'criado_em'),
  ('relatorios_atendimento', 'updated_at', 'atualizado_em'),
  ('relatorios_atendimento', 'email_requested_at', 'email_solicitado_em'),
  ('relatorios_atendimento', 'email_attempts', 'tentativas_email'),
  ('relatorios_atendimento', 'email_next_retry_at', 'proxima_tentativa_email_em')
) AS colunas(tabela, nome_antigo, nome_novo);

