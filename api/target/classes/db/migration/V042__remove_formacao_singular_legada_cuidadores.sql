-- Consolida a formação profissional na coleção normalizada antes de remover o campo singular legado.
SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '2min';

INSERT INTO cuidadores_formacoes (perfil_cuidador_id, formacao)
SELECT id, formacao
FROM cuidadores
WHERE formacao IS NOT NULL
  AND btrim(formacao) <> ''
ON CONFLICT (perfil_cuidador_id, formacao) DO NOTHING;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM cuidadores c
    WHERE c.formacao IS NOT NULL
      AND btrim(c.formacao) <> ''
      AND NOT EXISTS (
        SELECT 1
        FROM cuidadores_formacoes cf
        WHERE cf.perfil_cuidador_id = c.id
          AND cf.formacao = c.formacao
      )
  ) THEN
    RAISE EXCEPTION 'Existem formações singulares sem equivalente em cuidadores_formacoes';
  END IF;
END $$;

ALTER TABLE cuidadores
  DROP COLUMN IF EXISTS formacao;
