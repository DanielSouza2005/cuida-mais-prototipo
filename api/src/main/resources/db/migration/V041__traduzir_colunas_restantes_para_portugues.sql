-- Substitui o termo físico inglês "snapshot" por "copia" nas colunas de domínio.
-- ALTER COLUMN RENAME preserva dados, tipos, FKs, índices, uniques e checks.
SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '2min';

CREATE OR REPLACE FUNCTION cuidaplus_renomear_coluna_v041(
  tabela text,
  nome_antigo text,
  nome_novo text
) RETURNS void LANGUAGE plpgsql AS $$
DECLARE
  antiga_existe boolean;
  nova_existe boolean;
BEGIN
  IF to_regclass(format('%I.%I', current_schema(), tabela)) IS NULL THEN
    RAISE EXCEPTION 'Tabela esperada %.% não existe', current_schema(), tabela;
  END IF;

  SELECT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = tabela
      AND column_name = nome_antigo
  ) INTO antiga_existe;

  SELECT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = tabela
      AND column_name = nome_novo
  ) INTO nova_existe;

  IF antiga_existe AND NOT nova_existe THEN
    EXECUTE format(
      'ALTER TABLE %I.%I RENAME COLUMN %I TO %I',
      current_schema(), tabela, nome_antigo, nome_novo
    );
  ELSIF NOT antiga_existe AND nova_existe THEN
    RETURN;
  ELSE
    RAISE EXCEPTION 'Estado ambíguo ao renomear %.% para % (antiga=%, nova=%)',
      tabela, nome_antigo, nome_novo, antiga_existe, nova_existe;
  END IF;
END $$;

SELECT cuidaplus_renomear_coluna_v041(tabela, antigo, novo)
FROM (VALUES
  ('solicitacoes_servico', 'nome_rotina_snapshot', 'nome_rotina_copia'),
  ('solicitacoes_servico_itens_cuidado_copias_dias_semana', 'item_snapshot_id', 'item_copia_id'),
  ('tarefas_cuidado', 'item_snapshot_origem_id', 'item_copia_origem_id')
) AS colunas(tabela, antigo, novo);

-- Recalcula nomes de PK/FK/unique apenas nas tabelas afetadas, seguindo a regra da V039.
DO $$
DECLARE
  item record;
  base text;
  novo text;
BEGIN
  FOR item IN
    SELECT c.conname antigo, t.relname tabela, c.contype,
      string_agg(a.attname, '_' ORDER BY k.ordinality) colunas
    FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    LEFT JOIN LATERAL unnest(c.conkey) WITH ORDINALITY k(attnum, ordinality) ON true
    LEFT JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = k.attnum
    WHERE n.nspname = current_schema()
      AND c.contype IN ('p', 'f', 'u')
      AND t.relname = ANY (ARRAY[
        'solicitacoes_servico_itens_cuidado_copias_dias_semana',
        'tarefas_cuidado'
      ]::name[])
    GROUP BY c.oid, c.conname, t.relname, c.contype
  LOOP
    base := CASE item.contype
      WHEN 'p' THEN 'pk_' || item.tabela
      WHEN 'f' THEN 'fk_' || item.tabela || '_' || item.colunas
      ELSE 'uk_' || item.tabela || '_' || item.colunas
    END;
    novo := CASE
      WHEN length(base) <= 63 THEN base
      ELSE left(base, 54) || '_' || left(md5(base), 8)
    END;
    IF item.antigo <> novo THEN
      EXECUTE format(
        'ALTER TABLE %I.%I RENAME CONSTRAINT %I TO %I',
        current_schema(), item.tabela, item.antigo, novo
      );
    END IF;
  END LOOP;
END $$;

-- Recalcula nomes de índices não vinculados a constraints, inclusive o unique parcial da tarefa.
DO $$
DECLARE
  item record;
  base text;
  novo text;
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
    WHERE n.nspname = current_schema()
      AND c.oid IS NULL
      AND t.relname = ANY (ARRAY[
        'solicitacoes_servico_itens_cuidado_copias_dias_semana',
        'tarefas_cuidado'
      ]::name[])
    GROUP BY i.oid, i.relname, t.relname, x.indisunique
  LOOP
    base := CASE WHEN item.indisunique THEN 'ux_' ELSE 'idx_' END
      || item.tabela || '_' || item.colunas;
    novo := CASE
      WHEN length(base) <= 63 THEN base
      ELSE left(base, 54) || '_' || left(md5(base), 8)
    END;
    IF item.antigo <> novo THEN
      EXECUTE format(
        'ALTER INDEX %I.%I RENAME TO %I',
        current_schema(), item.antigo, novo
      );
    END IF;
  END LOOP;
END $$;

DROP FUNCTION cuidaplus_renomear_coluna_v041(text, text, text);
