-- Remove o subgrafo legado de grupos/itens de cuidado.
-- A migração aborta em ambientes que ainda tenham dados históricos para evitar perda silenciosa.
SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '2min';

DO $$
DECLARE
  tabela text;
  total bigint;
  coluna text;
BEGIN
  FOREACH tabela IN ARRAY ARRAY[
    'grupos_cuidado',
    'grupos_cuidado_itens',
    'solicitacoes_servico_grupos_cuidado_itens_copias',
    'contratacoes_itens_cuidado',
    'contratacoes_itens_cuidado_historico'
  ]
  LOOP
    IF to_regclass(format('%I.%I', current_schema(), tabela)) IS NOT NULL THEN
      EXECUTE format('SELECT count(*) FROM %I.%I', current_schema(), tabela) INTO total;
      IF total > 0 THEN
        RAISE EXCEPTION
          'Remoção legada bloqueada: %.% contém % registro(s). Migre ou preserve os dados manualmente.',
          current_schema(), tabela, total;
      END IF;
    END IF;
  END LOOP;

  FOREACH coluna IN ARRAY ARRAY['care_group_id', 'care_group_name_snapshot']
  LOOP
    IF EXISTS (
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = current_schema()
        AND table_name = 'solicitacoes_servico'
        AND column_name = coluna
    ) THEN
      EXECUTE format(
        'SELECT count(*) FROM %I.solicitacoes_servico WHERE %I IS NOT NULL',
        current_schema(), coluna
      ) INTO total;
      IF total > 0 THEN
        RAISE EXCEPTION
          'Remoção legada bloqueada: %.solicitacoes_servico.% contém % valor(es). Migre ou preserve os dados manualmente.',
          current_schema(), coluna, total;
      END IF;
    END IF;
  END LOOP;
END $$;

-- Ordem inversa das dependências; sem CASCADE para revelar qualquer dependência inesperada.
DROP TABLE IF EXISTS contratacoes_itens_cuidado_historico;
DROP TABLE IF EXISTS contratacoes_itens_cuidado;
DROP TABLE IF EXISTS solicitacoes_servico_grupos_cuidado_itens_copias;
DROP TABLE IF EXISTS grupos_cuidado_itens;

DO $$
DECLARE
  restricao record;
BEGIN
  FOR restricao IN
    SELECT c.conname
    FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY (c.conkey)
    WHERE n.nspname = current_schema()
      AND t.relname = 'solicitacoes_servico'
      AND c.contype = 'f'
      AND a.attname = 'care_group_id'
  LOOP
    EXECUTE format(
      'ALTER TABLE %I.solicitacoes_servico DROP CONSTRAINT %I',
      current_schema(), restricao.conname
    );
  END LOOP;
END $$;

ALTER TABLE IF EXISTS solicitacoes_servico
  DROP COLUMN IF EXISTS care_group_id,
  DROP COLUMN IF EXISTS care_group_name_snapshot;

DROP TABLE IF EXISTS grupos_cuidado;
