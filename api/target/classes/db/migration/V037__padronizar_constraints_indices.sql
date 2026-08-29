-- Lote 7/7: nomes dos objetos auxiliares. As tabelas e os dados já foram
-- concluídos nos lotes anteriores.
SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '2min';

DO $$
DECLARE
  item RECORD;
  nome_base text;
  nome_novo text;
BEGIN
  FOR item IN
    SELECT
      constraint_row.oid,
      constraint_row.conname AS nome_antigo,
      table_row.relname AS tabela,
      constraint_row.contype,
      string_agg(column_row.attname, '_' ORDER BY key_row.ordinality) AS colunas
    FROM pg_constraint constraint_row
    JOIN pg_class table_row ON table_row.oid = constraint_row.conrelid
    JOIN pg_namespace namespace_row ON namespace_row.oid = table_row.relnamespace
    LEFT JOIN LATERAL unnest(constraint_row.conkey) WITH ORDINALITY key_row(attnum, ordinality) ON TRUE
    LEFT JOIN pg_attribute column_row
      ON column_row.attrelid = table_row.oid AND column_row.attnum = key_row.attnum
    WHERE namespace_row.nspname = current_schema()
      AND constraint_row.contype IN ('p', 'f', 'u')
      AND table_row.relname = ANY (ARRAY[
        'usuarios', 'tokens_redefinicao_senha', 'perfis_responsaveis',
        'perfis_cuidadores', 'pessoas_assistidas', 'contatos_emergencia',
        'alergias_pessoas_assistidas', 'restricoes_alimentares_pessoas_assistidas',
        'modalidades_cuidadores', 'servicos_cuidadores',
        'dias_disponibilidade_cuidadores', 'periodos_disponibilidade_cuidadores',
        'formacoes_cuidadores', 'solicitacoes_servico',
        'datas_solicitacoes_servico', 'dias_agenda_solicitacoes_servico',
        'atividades_solicitacoes_servico', 'contratacoes', 'historico_status',
        'rotinas_cuidado', 'itens_rotinas_cuidado', 'dias_semana_itens_rotina',
        'snapshot_itens_cuidado_solicitacoes', 'dias_semana_snapshot_solicitacoes',
        'tarefas_cuidado', 'dias_semana_tarefas_cuidado', 'ocorrencias_tarefas',
        'registros_atividades_cuidado', 'auditoria_tarefas_cuidado',
        'lembretes_tarefas_cuidado', 'fotos_ocorrencias_cuidado', 'notificacoes',
        'preferencias_notificacoes_usuarios', 'registros_atendimento',
        'relatorios_atendimento'
      ]::name[])
    GROUP BY constraint_row.oid, constraint_row.conname, table_row.relname, constraint_row.contype
  LOOP
    nome_base := CASE item.contype
      WHEN 'p' THEN 'pk_' || item.tabela
      WHEN 'f' THEN 'fk_' || item.tabela || '_' || item.colunas
      ELSE 'uk_' || item.tabela || '_' || item.colunas
    END;
    nome_novo := CASE
      WHEN length(nome_base) <= 63 THEN nome_base
      ELSE left(nome_base, 54) || '_' || left(md5(nome_base), 8)
    END;
    IF item.nome_antigo <> nome_novo THEN
      EXECUTE format('ALTER TABLE %I RENAME CONSTRAINT %I TO %I', item.tabela, item.nome_antigo, nome_novo);
    END IF;
  END LOOP;
END $$;

DO $$
DECLARE
  item RECORD;
BEGIN
  FOR item IN
    SELECT * FROM (VALUES
      ('usuarios', 'ck_users_user_type', 'ck_usuarios_tipo_usuario'),
      ('tarefas_cuidado', 'ck_care_task_dates', 'ck_tarefas_cuidado_datas'),
      ('tarefas_cuidado', 'ck_care_task_interval', 'ck_tarefas_cuidado_intervalo'),
      ('tarefas_cuidado', 'ck_care_task_reminder', 'ck_tarefas_cuidado_lembrete'),
      ('fotos_ocorrencias_cuidado', 'ck_care_photo_single_parent', 'ck_fotos_ocorrencias_vinculo_unico'),
      ('registros_atendimento', 'ck_service_attendance_type', 'ck_registros_atendimento_tipo'),
      ('registros_atendimento', 'ck_service_attendance_latitude', 'ck_registros_atendimento_latitude'),
      ('registros_atendimento', 'ck_service_attendance_longitude', 'ck_registros_atendimento_longitude'),
      ('registros_atendimento', 'ck_service_attendance_accuracy', 'ck_registros_atendimento_precisao'),
      ('relatorios_atendimento', 'ck_attendance_report_status', 'ck_relatorios_atendimento_status'),
      ('relatorios_atendimento', 'ck_attendance_report_email_status', 'ck_relatorios_atendimento_status_email')
    ) AS nomes(tabela, nome_antigo, nome_novo)
  LOOP
    IF EXISTS (
      SELECT 1 FROM pg_constraint constraint_row
      JOIN pg_class table_row ON table_row.oid = constraint_row.conrelid
      JOIN pg_namespace namespace_row ON namespace_row.oid = table_row.relnamespace
      WHERE namespace_row.nspname = current_schema()
        AND table_row.relname = item.tabela
        AND constraint_row.conname = item.nome_antigo
    ) THEN
      EXECUTE format('ALTER TABLE %I RENAME CONSTRAINT %I TO %I', item.tabela, item.nome_antigo, item.nome_novo);
    END IF;
  END LOOP;
END $$;

DO $$
DECLARE
  item RECORD;
  nome_base text;
  nome_novo text;
BEGIN
  FOR item IN
    SELECT
      index_row.oid AS indice_oid,
      index_row.relname AS nome_antigo,
      table_row.relname AS tabela,
      index_data.indisunique,
      string_agg(column_row.attname, '_' ORDER BY key_row.ordinality) AS colunas
    FROM pg_index index_data
    JOIN pg_class index_row ON index_row.oid = index_data.indexrelid
    JOIN pg_class table_row ON table_row.oid = index_data.indrelid
    JOIN pg_namespace namespace_row ON namespace_row.oid = table_row.relnamespace
    JOIN LATERAL unnest(index_data.indkey) WITH ORDINALITY key_row(attnum, ordinality)
      ON key_row.attnum > 0
    JOIN pg_attribute column_row
      ON column_row.attrelid = table_row.oid AND column_row.attnum = key_row.attnum
    LEFT JOIN pg_constraint constraint_row ON constraint_row.conindid = index_row.oid
    WHERE namespace_row.nspname = current_schema()
      AND constraint_row.oid IS NULL
      AND table_row.relname = ANY (ARRAY[
        'usuarios', 'tokens_redefinicao_senha', 'perfis_responsaveis',
        'perfis_cuidadores', 'pessoas_assistidas', 'contatos_emergencia',
        'alergias_pessoas_assistidas', 'restricoes_alimentares_pessoas_assistidas',
        'modalidades_cuidadores', 'servicos_cuidadores',
        'dias_disponibilidade_cuidadores', 'periodos_disponibilidade_cuidadores',
        'formacoes_cuidadores', 'solicitacoes_servico',
        'datas_solicitacoes_servico', 'dias_agenda_solicitacoes_servico',
        'atividades_solicitacoes_servico', 'contratacoes', 'historico_status',
        'rotinas_cuidado', 'itens_rotinas_cuidado', 'dias_semana_itens_rotina',
        'snapshot_itens_cuidado_solicitacoes', 'dias_semana_snapshot_solicitacoes',
        'tarefas_cuidado', 'dias_semana_tarefas_cuidado', 'ocorrencias_tarefas',
        'registros_atividades_cuidado', 'auditoria_tarefas_cuidado',
        'lembretes_tarefas_cuidado', 'fotos_ocorrencias_cuidado', 'notificacoes',
        'preferencias_notificacoes_usuarios', 'registros_atendimento',
        'relatorios_atendimento'
      ]::name[])
    GROUP BY index_row.oid, index_row.relname, table_row.relname, index_data.indisunique
  LOOP
    nome_base := CASE WHEN item.indisunique THEN 'ux_' ELSE 'idx_' END
      || item.tabela || '_' || item.colunas;
    nome_novo := CASE
      WHEN length(nome_base) <= 63 THEN nome_base
      ELSE left(nome_base, 54) || '_' || left(md5(nome_base), 8)
    END;
    IF item.nome_antigo <> nome_novo THEN
      EXECUTE format('ALTER INDEX %I RENAME TO %I', item.nome_antigo, nome_novo);
    END IF;
  END LOOP;
END $$;

DROP FUNCTION cuidaplus_renomear_coluna(text, text, text);
DROP FUNCTION cuidaplus_renomear_tabela(text, text);
