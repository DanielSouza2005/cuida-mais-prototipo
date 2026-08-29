-- Traduz as estruturas legadas de grupos/itens de cuidado e remove "snapshot"
-- do nome da tabela ativa. Somente metadados são alterados; dados são preservados.
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

CREATE OR REPLACE FUNCTION cuidaplus_renomear_coluna(tabela text, nome_antigo text, nome_novo text)
RETURNS void LANGUAGE plpgsql AS $$
DECLARE antiga_existe boolean; nova_existe boolean;
BEGIN
  IF to_regclass(format('%I.%I', current_schema(), tabela)) IS NULL THEN
    RETURN;
  END IF;
  SELECT EXISTS (SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema() AND table_name = tabela AND column_name = nome_antigo)
    INTO antiga_existe;
  SELECT EXISTS (SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema() AND table_name = tabela AND column_name = nome_novo)
    INTO nova_existe;
  IF antiga_existe AND NOT nova_existe THEN
    EXECUTE format('ALTER TABLE %I RENAME COLUMN %I TO %I', tabela, nome_antigo, nome_novo);
  ELSIF NOT antiga_existe AND nova_existe THEN
    RETURN;
  ELSE
    RAISE EXCEPTION 'Estado inesperado ao renomear %.% para %', tabela, nome_antigo, nome_novo;
  END IF;
END $$;

SELECT cuidaplus_renomear_tabela(antigo, novo) FROM (VALUES
  ('care_groups', 'grupos_cuidado'),
  ('care_group_items', 'itens_grupos_cuidado'),
  ('service_request_care_item_snapshots', 'copias_itens_cuidado_solicitacoes'),
  ('contract_care_items', 'itens_cuidado_contratacoes'),
  ('contract_care_item_history', 'historico_itens_cuidado_contratacoes'),
  ('snapshot_itens_cuidado_solicitacoes', 'copias_itens_rotina_solicitacoes')
) AS nomes(antigo, novo);

SELECT cuidaplus_renomear_coluna(tabela, antigo, novo) FROM (VALUES
  ('grupos_cuidado', 'responsible_user_id', 'usuario_responsavel_id'),
  ('grupos_cuidado', 'assisted_person_id', 'pessoa_assistida_id'),
  ('grupos_cuidado', 'name', 'nome'),
  ('grupos_cuidado', 'description', 'descricao'),
  ('grupos_cuidado', 'active', 'ativo'),
  ('grupos_cuidado', 'created_at', 'criado_em'),
  ('grupos_cuidado', 'updated_at', 'atualizado_em'),
  ('itens_grupos_cuidado', 'care_group_id', 'grupo_cuidado_id'),
  ('itens_grupos_cuidado', 'title', 'titulo'),
  ('itens_grupos_cuidado', 'description', 'descricao'),
  ('itens_grupos_cuidado', 'required', 'obrigatorio'),
  ('itens_grupos_cuidado', 'sort_order', 'ordem_exibicao'),
  ('itens_grupos_cuidado', 'active', 'ativo'),
  ('itens_grupos_cuidado', 'created_at', 'criado_em'),
  ('itens_grupos_cuidado', 'updated_at', 'atualizado_em'),
  ('copias_itens_cuidado_solicitacoes', 'service_request_id', 'solicitacao_servico_id'),
  ('copias_itens_cuidado_solicitacoes', 'original_care_group_id', 'grupo_cuidado_original_id'),
  ('copias_itens_cuidado_solicitacoes', 'original_care_group_item_id', 'item_grupo_cuidado_original_id'),
  ('copias_itens_cuidado_solicitacoes', 'title', 'titulo'),
  ('copias_itens_cuidado_solicitacoes', 'description', 'descricao'),
  ('copias_itens_cuidado_solicitacoes', 'required', 'obrigatorio'),
  ('copias_itens_cuidado_solicitacoes', 'sort_order', 'ordem_exibicao'),
  ('copias_itens_cuidado_solicitacoes', 'created_at', 'criado_em'),
  ('itens_cuidado_contratacoes', 'care_contract_id', 'contratacao_id'),
  ('itens_cuidado_contratacoes', 'service_request_snapshot_id', 'copia_item_solicitacao_id'),
  ('itens_cuidado_contratacoes', 'original_care_group_id', 'grupo_cuidado_original_id'),
  ('itens_cuidado_contratacoes', 'original_care_group_item_id', 'item_grupo_cuidado_original_id'),
  ('itens_cuidado_contratacoes', 'title', 'titulo'),
  ('itens_cuidado_contratacoes', 'description', 'descricao'),
  ('itens_cuidado_contratacoes', 'sort_order', 'ordem_exibicao'),
  ('itens_cuidado_contratacoes', 'status_updated_at', 'status_atualizado_em'),
  ('itens_cuidado_contratacoes', 'status_updated_by_user_id', 'usuario_atualizacao_status_id'),
  ('itens_cuidado_contratacoes', 'execution_notes', 'anotacoes_execucao'),
  ('itens_cuidado_contratacoes', 'created_at', 'criado_em'),
  ('itens_cuidado_contratacoes', 'updated_at', 'atualizado_em'),
  ('historico_itens_cuidado_contratacoes', 'contract_care_item_id', 'item_cuidado_contratacao_id'),
  ('historico_itens_cuidado_contratacoes', 'contract_id', 'contratacao_id'),
  ('historico_itens_cuidado_contratacoes', 'previous_status', 'status_anterior'),
  ('historico_itens_cuidado_contratacoes', 'new_status', 'status_novo'),
  ('historico_itens_cuidado_contratacoes', 'previous_title', 'titulo_anterior'),
  ('historico_itens_cuidado_contratacoes', 'new_title', 'titulo_novo'),
  ('historico_itens_cuidado_contratacoes', 'previous_description', 'descricao_anterior'),
  ('historico_itens_cuidado_contratacoes', 'new_description', 'descricao_nova'),
  ('historico_itens_cuidado_contratacoes', 'notes', 'anotacoes'),
  ('historico_itens_cuidado_contratacoes', 'changed_by_user_id', 'usuario_alteracao_id'),
  ('historico_itens_cuidado_contratacoes', 'group_update_action', 'acao_atualizacao_grupo'),
  ('historico_itens_cuidado_contratacoes', 'created_at', 'criado_em')
) AS nomes(tabela, antigo, novo);

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
      AND t.relname = ANY (ARRAY['grupos_cuidado', 'itens_grupos_cuidado',
        'copias_itens_cuidado_solicitacoes', 'itens_cuidado_contratacoes',
        'historico_itens_cuidado_contratacoes', 'copias_itens_rotina_solicitacoes']::name[])
    GROUP BY c.oid, c.conname, t.relname, c.contype
  LOOP
    base := CASE item.contype WHEN 'p' THEN 'pk_' || item.tabela
      WHEN 'f' THEN 'fk_' || item.tabela || '_' || item.colunas
      ELSE 'uk_' || item.tabela || '_' || item.colunas END;
    novo := CASE WHEN length(base) <= 63 THEN base ELSE left(base, 54) || '_' || left(md5(base), 8) END;
    IF item.antigo <> novo THEN
      EXECUTE format('ALTER TABLE %I RENAME CONSTRAINT %I TO %I', item.tabela, item.antigo, novo);
    END IF;
  END LOOP;
END $$;

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
      AND t.relname = ANY (ARRAY['grupos_cuidado', 'itens_grupos_cuidado',
        'copias_itens_cuidado_solicitacoes', 'itens_cuidado_contratacoes',
        'historico_itens_cuidado_contratacoes', 'copias_itens_rotina_solicitacoes']::name[])
    GROUP BY i.oid, i.relname, t.relname, x.indisunique
  LOOP
    base := CASE WHEN item.indisunique THEN 'ux_' ELSE 'idx_' END || item.tabela || '_' || item.colunas;
    novo := CASE WHEN length(base) <= 63 THEN base ELSE left(base, 54) || '_' || left(md5(base), 8) END;
    IF item.antigo <> novo THEN EXECUTE format('ALTER INDEX %I RENAME TO %I', item.antigo, novo); END IF;
  END LOOP;
END $$;

DROP FUNCTION cuidaplus_renomear_coluna(text, text, text);
DROP FUNCTION cuidaplus_renomear_tabela(text, text);
