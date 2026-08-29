-- Lote 1/7: usuários e perfis principais.
-- Cada lote é uma transação Flyway independente. Uma espera por lock falha em
-- até 10 segundos, permitindo identificar a sessão bloqueadora e tentar de novo.
SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '2min';

CREATE OR REPLACE FUNCTION cuidaplus_renomear_tabela(nome_antigo text, nome_novo text)
RETURNS void
LANGUAGE plpgsql
AS $$
BEGIN
  IF to_regclass(format('%I.%I', current_schema(), nome_antigo)) IS NOT NULL
     AND to_regclass(format('%I.%I', current_schema(), nome_novo)) IS NULL THEN
    EXECUTE format('ALTER TABLE %I RENAME TO %I', nome_antigo, nome_novo);
  ELSIF to_regclass(format('%I.%I', current_schema(), nome_antigo)) IS NULL
        AND to_regclass(format('%I.%I', current_schema(), nome_novo)) IS NOT NULL THEN
    RETURN;
  ELSE
    RAISE EXCEPTION 'Estado inesperado ao renomear tabela % para %', nome_antigo, nome_novo;
  END IF;
END;
$$;

CREATE OR REPLACE FUNCTION cuidaplus_renomear_coluna(
  nome_tabela text,
  nome_antigo text,
  nome_novo text
)
RETURNS void
LANGUAGE plpgsql
AS $$
DECLARE
  coluna_antiga_existe boolean;
  coluna_nova_existe boolean;
BEGIN
  SELECT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = nome_tabela
      AND column_name = nome_antigo
  ) INTO coluna_antiga_existe;

  SELECT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = nome_tabela
      AND column_name = nome_novo
  ) INTO coluna_nova_existe;

  IF coluna_antiga_existe AND NOT coluna_nova_existe THEN
    EXECUTE format('ALTER TABLE %I RENAME COLUMN %I TO %I', nome_tabela, nome_antigo, nome_novo);
  ELSIF NOT coluna_antiga_existe AND coluna_nova_existe THEN
    RETURN;
  ELSE
    RAISE EXCEPTION 'Estado inesperado ao renomear %.% para %', nome_tabela, nome_antigo, nome_novo;
  END IF;
END;
$$;

SELECT cuidaplus_renomear_tabela(nome_antigo, nome_novo)
FROM (VALUES
  ('users', 'usuarios'),
  ('password_reset_tokens', 'tokens_redefinicao_senha'),
  ('responsible_profiles', 'perfis_responsaveis'),
  ('caregiver_profiles', 'perfis_cuidadores'),
  ('assisted_persons', 'pessoas_assistidas')
) AS tabelas(nome_antigo, nome_novo);

SELECT cuidaplus_renomear_coluna(tabela, nome_antigo, nome_novo)
FROM (VALUES
  ('usuarios', 'full_name', 'nome_completo'),
  ('usuarios', 'password_hash', 'senha_hash'),
  ('usuarios', 'birth_date', 'data_nascimento'),
  ('usuarios', 'user_type', 'tipo_usuario'),
  ('usuarios', 'phone', 'telefone'),
  ('usuarios', 'profile_photo_url', 'url_foto_perfil'),
  ('usuarios', 'created_at', 'criado_em'),
  ('usuarios', 'updated_at', 'atualizado_em'),
  ('tokens_redefinicao_senha', 'user_id', 'usuario_id'),
  ('tokens_redefinicao_senha', 'token_hash', 'hash_token'),
  ('tokens_redefinicao_senha', 'expires_at', 'expira_em'),
  ('tokens_redefinicao_senha', 'used_at', 'usado_em'),
  ('tokens_redefinicao_senha', 'created_at', 'criado_em'),
  ('perfis_responsaveis', 'user_id', 'usuario_id'),
  ('perfis_responsaveis', 'created_at', 'criado_em'),
  ('perfis_responsaveis', 'updated_at', 'atualizado_em'),
  ('perfis_cuidadores', 'user_id', 'usuario_id'),
  ('perfis_cuidadores', 'created_at', 'criado_em'),
  ('perfis_cuidadores', 'updated_at', 'atualizado_em'),
  ('pessoas_assistidas', 'responsible_user_id', 'usuario_responsavel_id'),
  ('pessoas_assistidas', 'created_at', 'criado_em'),
  ('pessoas_assistidas', 'updated_at', 'atualizado_em')
) AS colunas(tabela, nome_antigo, nome_novo);

