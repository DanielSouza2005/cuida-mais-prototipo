-- Padroniza todas as tabelas de dominio no singular.
-- ALTER TABLE RENAME preserva dados, FKs, PKs, indices e constraints.
SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '2min';

CREATE OR REPLACE FUNCTION cuidaplus_renomear_tabela_v043(nome_antigo text, nome_novo text)
RETURNS void LANGUAGE plpgsql AS $$
DECLARE
  antigo_existe boolean := to_regclass(format('%I.%I', current_schema(), nome_antigo)) IS NOT NULL;
  novo_existe boolean := to_regclass(format('%I.%I', current_schema(), nome_novo)) IS NOT NULL;
BEGIN
  IF antigo_existe AND NOT novo_existe THEN
    EXECUTE format('ALTER TABLE %I RENAME TO %I', nome_antigo, nome_novo);
  ELSIF NOT antigo_existe AND novo_existe THEN
    RETURN;
  ELSE
    RAISE EXCEPTION 'Estado inesperado ao renomear tabela % para % (antiga=%, nova=%)',
      nome_antigo, nome_novo, antigo_existe, novo_existe;
  END IF;
END $$;

SELECT cuidaplus_renomear_tabela_v043(antigo, novo) FROM (VALUES
  ('usuarios', 'usuario'),
  ('usuarios_tokens_redefinicao_senha', 'usuario_token_redefinicao_senha'),
  ('responsaveis', 'responsavel'),
  ('cuidadores', 'cuidador'),
  ('cuidadores_disponibilidade_dias', 'cuidador_disponibilidade_dia'),
  ('cuidadores_disponibilidade_periodos', 'cuidador_disponibilidade_periodo'),
  ('cuidadores_formacoes', 'cuidador_formacao'),
  ('cuidadores_modalidades', 'cuidador_modalidade'),
  ('cuidadores_servicos', 'cuidador_servico'),
  ('pessoas_assistidas', 'pessoa_assistida'),
  ('pessoas_assistidas_alergias', 'pessoa_assistida_alergia'),
  ('pessoas_assistidas_contatos_emergencia', 'pessoa_assistida_contato_emergencia'),
  ('pessoas_assistidas_restricoes_alimentares', 'pessoa_assistida_restricao_alimentar'),
  ('solicitacoes_servico', 'solicitacao_servico'),
  ('solicitacoes_servico_agenda_dias', 'solicitacao_servico_agenda_dia'),
  ('solicitacoes_servico_atividades', 'solicitacao_servico_atividade'),
  ('solicitacoes_servico_datas', 'solicitacao_servico_data'),
  ('solicitacoes_servico_itens_cuidado_copias', 'solicitacao_servico_item_cuidado_copia'),
  ('solicitacoes_servico_itens_cuidado_copias_dias_semana', 'solicitacao_servico_item_cuidado_copia_dia_semana'),
  ('solicitacoes_servico_contratacoes_historico_status', 'solicitacao_servico_contratacao_historico_status'),
  ('contratacoes', 'contratacao'),
  ('rotinas_cuidado', 'rotina_cuidado'),
  ('rotinas_cuidado_itens', 'rotina_cuidado_item'),
  ('rotinas_cuidado_itens_dias_semana', 'rotina_cuidado_item_dia_semana'),
  ('tarefas_cuidado', 'tarefa_cuidado'),
  ('tarefas_cuidado_dias_semana', 'tarefa_cuidado_dia_semana'),
  ('tarefas_cuidado_auditoria', 'tarefa_cuidado_auditoria'),
  ('ocorrencias_cuidado', 'ocorrencia_cuidado'),
  ('ocorrencias_cuidado_fotos', 'ocorrencia_cuidado_foto'),
  ('ocorrencias_cuidado_lembretes', 'ocorrencia_cuidado_lembrete'),
  ('registros_diario_cuidado', 'registro_diario_cuidado'),
  ('registros_atendimento', 'registro_atendimento'),
  ('relatorios_atendimento', 'relatorio_atendimento'),
  ('notificacoes', 'notificacao'),
  ('notificacoes_preferencias', 'notificacao_preferencia')
) AS nomes(antigo, novo);

-- Recalcula nomes de PKs, FKs e restricoes UNIQUE com base na tabela singular.
DO $$
DECLARE item record; base text; novo text;
BEGIN
  FOR item IN
    SELECT c.conname antigo, t.relname tabela, c.contype,
      string_agg(a.attname, '_' ORDER BY k.ordinality) colunas
    FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    LEFT JOIN LATERAL unnest(c.conkey) WITH ORDINALITY k(attnum, ordinality) ON true
    LEFT JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = k.attnum
    WHERE n.nspname = current_schema() AND c.contype IN ('p', 'f', 'u')
      AND t.relname <> 'flyway_schema_history'
    GROUP BY c.oid, c.conname, t.relname, c.contype
  LOOP
    base := CASE item.contype
      WHEN 'p' THEN 'pk_' || item.tabela
      WHEN 'f' THEN 'fk_' || item.tabela || '_' || item.colunas
      ELSE 'uk_' || item.tabela || '_' || item.colunas
    END;
    novo := CASE WHEN length(base) <= 63 THEN base
      ELSE left(base, 54) || '_' || left(md5(base), 8) END;
    IF item.antigo <> novo THEN
      EXECUTE format('ALTER TABLE %I RENAME CONSTRAINT %I TO %I', item.tabela, item.antigo, novo);
    END IF;
  END LOOP;
END $$;

-- Recalcula nomes dos indices que nao pertencem a constraints.
DO $$
DECLARE item record; base text; novo text;
BEGIN
  FOR item IN
    SELECT i.relname antigo, t.relname tabela, x.indisunique,
      string_agg(a.attname, '_' ORDER BY k.ordinality) colunas
    FROM pg_index x
    JOIN pg_class i ON i.oid = x.indexrelid
    JOIN pg_class t ON t.oid = x.indrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    JOIN LATERAL unnest(x.indkey) WITH ORDINALITY k(attnum, ordinality) ON k.attnum > 0
    JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = k.attnum
    LEFT JOIN pg_constraint c ON c.conindid = i.oid
    WHERE n.nspname = current_schema() AND c.oid IS NULL
      AND t.relname <> 'flyway_schema_history'
    GROUP BY i.oid, i.relname, t.relname, x.indisunique
  LOOP
    base := CASE WHEN item.indisunique THEN 'ux_' ELSE 'idx_' END
      || item.tabela || '_' || item.colunas;
    novo := CASE WHEN length(base) <= 63 THEN base
      ELSE left(base, 54) || '_' || left(md5(base), 8) END;
    IF item.antigo <> novo THEN
      EXECUTE format('ALTER INDEX %I RENAME TO %I', item.antigo, novo);
    END IF;
  END LOOP;
END $$;

-- CHECKs nao possuem colunas catalogadas de forma equivalente; a lista e explicita.
DO $$
DECLARE item record; antigo_existe boolean; novo_existe boolean;
BEGIN
  FOR item IN SELECT * FROM (VALUES
    ('ocorrencia_cuidado_foto', 'ck_ocorrencias_cuidado_fotos_vinculo_unico', 'ck_ocorrencia_cuidado_foto_vinculo_unico'),
    ('registro_atendimento', 'ck_registros_atendimento_tipo', 'ck_registro_atendimento_tipo'),
    ('registro_atendimento', 'ck_registros_atendimento_latitude', 'ck_registro_atendimento_latitude'),
    ('registro_atendimento', 'ck_registros_atendimento_longitude', 'ck_registro_atendimento_longitude'),
    ('registro_atendimento', 'ck_registros_atendimento_precisao', 'ck_registro_atendimento_precisao'),
    ('relatorio_atendimento', 'ck_relatorios_atendimento_status', 'ck_relatorio_atendimento_status'),
    ('relatorio_atendimento', 'ck_relatorios_atendimento_status_email', 'ck_relatorio_atendimento_status_email'),
    ('tarefa_cuidado', 'ck_tarefas_cuidado_datas', 'ck_tarefa_cuidado_datas'),
    ('tarefa_cuidado', 'ck_tarefas_cuidado_intervalo', 'ck_tarefa_cuidado_intervalo'),
    ('tarefa_cuidado', 'ck_tarefas_cuidado_lembrete', 'ck_tarefa_cuidado_lembrete'),
    ('usuario', 'ck_usuarios_tipo_usuario', 'ck_usuario_tipo_usuario')
  ) AS checks(tabela, antigo, novo)
  LOOP
    SELECT EXISTS (
      SELECT 1 FROM pg_constraint c
      JOIN pg_class t ON t.oid = c.conrelid
      JOIN pg_namespace n ON n.oid = t.relnamespace
      WHERE n.nspname = current_schema() AND t.relname = item.tabela AND c.conname = item.antigo
    ) INTO antigo_existe;
    SELECT EXISTS (
      SELECT 1 FROM pg_constraint c
      JOIN pg_class t ON t.oid = c.conrelid
      JOIN pg_namespace n ON n.oid = t.relnamespace
      WHERE n.nspname = current_schema() AND t.relname = item.tabela AND c.conname = item.novo
    ) INTO novo_existe;

    IF antigo_existe AND NOT novo_existe THEN
      EXECUTE format('ALTER TABLE %I RENAME CONSTRAINT %I TO %I', item.tabela, item.antigo, item.novo);
    ELSIF NOT antigo_existe AND novo_existe THEN
      CONTINUE;
    ELSE
      RAISE EXCEPTION 'Estado inesperado do CHECK %.% -> % (antigo=%, novo=%)',
        item.tabela, item.antigo, item.novo, antigo_existe, novo_existe;
    END IF;
  END LOOP;
END $$;

DROP FUNCTION cuidaplus_renomear_tabela_v043(text, text);
