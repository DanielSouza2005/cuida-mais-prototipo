-- Padroniza tabelas filhas como <entidade_principal>_<filho>.
-- ALTER TABLE RENAME preserva dados, FKs, PKs, índices e constraints.
SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '2min';

CREATE OR REPLACE FUNCTION cuidaplus_renomear_tabela(nome_antigo text, nome_novo text)
RETURNS void LANGUAGE plpgsql AS $$
BEGIN
  IF to_regclass(format('%I.%I', current_schema(), nome_antigo)) IS NOT NULL
     AND to_regclass(format('%I.%I', current_schema(), nome_novo)) IS NULL THEN
    EXECUTE format('ALTER TABLE %I RENAME TO %I', nome_antigo, nome_novo);
  ELSIF to_regclass(format('%I.%I', current_schema(), nome_antigo)) IS NULL
        AND to_regclass(format('%I.%I', current_schema(), nome_novo)) IS NOT NULL THEN
    RETURN;
  ELSIF to_regclass(format('%I.%I', current_schema(), nome_antigo)) IS NOT NULL
        OR to_regclass(format('%I.%I', current_schema(), nome_novo)) IS NOT NULL THEN
    RAISE EXCEPTION 'Estado inesperado ao renomear tabela % para %', nome_antigo, nome_novo;
  END IF;
END $$;

SELECT cuidaplus_renomear_tabela(antigo, novo) FROM (VALUES
  ('tokens_redefinicao_senha', 'usuarios_tokens_redefinicao_senha'),
  ('perfis_responsaveis', 'responsaveis'),
  ('perfis_cuidadores', 'cuidadores'),
  ('contatos_emergencia', 'pessoas_assistidas_contatos_emergencia'),
  ('alergias_pessoas_assistidas', 'pessoas_assistidas_alergias'),
  ('restricoes_alimentares_pessoas_assistidas', 'pessoas_assistidas_restricoes_alimentares'),
  ('modalidades_cuidadores', 'cuidadores_modalidades'),
  ('servicos_cuidadores', 'cuidadores_servicos'),
  ('dias_disponibilidade_cuidadores', 'cuidadores_disponibilidade_dias'),
  ('periodos_disponibilidade_cuidadores', 'cuidadores_disponibilidade_periodos'),
  ('formacoes_cuidadores', 'cuidadores_formacoes'),
  ('datas_solicitacoes_servico', 'solicitacoes_servico_datas'),
  ('dias_agenda_solicitacoes_servico', 'solicitacoes_servico_agenda_dias'),
  ('atividades_solicitacoes_servico', 'solicitacoes_servico_atividades'),
  ('copias_itens_cuidado_solicitacoes', 'solicitacoes_servico_grupos_cuidado_itens_copias'),
  ('copias_itens_rotina_solicitacoes', 'solicitacoes_servico_itens_cuidado_copias'),
  ('dias_semana_snapshot_solicitacoes', 'solicitacoes_servico_itens_cuidado_copias_dias_semana'),
  ('historico_status', 'solicitacoes_servico_contratacoes_historico_status'),
  ('itens_cuidado_contratacoes', 'contratacoes_itens_cuidado'),
  ('historico_itens_cuidado_contratacoes', 'contratacoes_itens_cuidado_historico'),
  ('itens_grupos_cuidado', 'grupos_cuidado_itens'),
  ('itens_rotinas_cuidado', 'rotinas_cuidado_itens'),
  ('dias_semana_itens_rotina', 'rotinas_cuidado_itens_dias_semana'),
  ('dias_semana_tarefas_cuidado', 'tarefas_cuidado_dias_semana'),
  ('ocorrencias_tarefas', 'ocorrencias_cuidado'),
  ('lembretes_tarefas_cuidado', 'ocorrencias_cuidado_lembretes'),
  ('fotos_ocorrencias_cuidado', 'ocorrencias_cuidado_fotos'),
  ('auditoria_tarefas_cuidado', 'tarefas_cuidado_auditoria'),
  ('registros_atividades_cuidado', 'registros_diario_cuidado'),
  ('preferencias_notificacoes_usuarios', 'notificacoes_preferencias')
) AS nomes(antigo, novo);

DO $$
DECLARE item record; base text; novo text;
BEGIN
  FOR item IN
    SELECT c.conname antigo, t.relname tabela, c.contype,
      string_agg(a.attname, '_' ORDER BY k.ordinality) colunas
    FROM pg_constraint c
    JOIN pg_class t ON t.oid=c.conrelid
    JOIN pg_namespace n ON n.oid=t.relnamespace
    LEFT JOIN LATERAL unnest(c.conkey) WITH ORDINALITY k(attnum,ordinality) ON true
    LEFT JOIN pg_attribute a ON a.attrelid=t.oid AND a.attnum=k.attnum
    WHERE n.nspname=current_schema() AND c.contype IN ('p','f','u')
      AND t.relname <> 'flyway_schema_history'
    GROUP BY c.oid,c.conname,t.relname,c.contype
  LOOP
    base := CASE item.contype WHEN 'p' THEN 'pk_'||item.tabela
      WHEN 'f' THEN 'fk_'||item.tabela||'_'||item.colunas
      ELSE 'uk_'||item.tabela||'_'||item.colunas END;
    novo := CASE WHEN length(base)<=63 THEN base ELSE left(base,54)||'_'||left(md5(base),8) END;
    IF item.antigo<>novo THEN
      EXECUTE format('ALTER TABLE %I RENAME CONSTRAINT %I TO %I',item.tabela,item.antigo,novo);
    END IF;
  END LOOP;
END $$;

DO $$
DECLARE item record; base text; novo text;
BEGIN
  FOR item IN
    SELECT i.relname antigo,t.relname tabela,x.indisunique,
      string_agg(a.attname,'_' ORDER BY k.ordinality) colunas
    FROM pg_index x
    JOIN pg_class i ON i.oid=x.indexrelid
    JOIN pg_class t ON t.oid=x.indrelid
    JOIN pg_namespace n ON n.oid=t.relnamespace
    JOIN LATERAL unnest(x.indkey) WITH ORDINALITY k(attnum,ordinality) ON k.attnum>0
    JOIN pg_attribute a ON a.attrelid=t.oid AND a.attnum=k.attnum
    LEFT JOIN pg_constraint c ON c.conindid=i.oid
    WHERE n.nspname=current_schema() AND c.oid IS NULL AND t.relname<>'flyway_schema_history'
    GROUP BY i.oid,i.relname,t.relname,x.indisunique
  LOOP
    base := CASE WHEN item.indisunique THEN 'ux_' ELSE 'idx_' END||item.tabela||'_'||item.colunas;
    novo := CASE WHEN length(base)<=63 THEN base ELSE left(base,54)||'_'||left(md5(base),8) END;
    IF item.antigo<>novo THEN EXECUTE format('ALTER INDEX %I RENAME TO %I',item.antigo,novo); END IF;
  END LOOP;
END $$;

ALTER TABLE ocorrencias_cuidado_fotos
  RENAME CONSTRAINT ck_fotos_ocorrencias_vinculo_unico
  TO ck_ocorrencias_cuidado_fotos_vinculo_unico;

DROP FUNCTION cuidaplus_renomear_tabela(text,text);
