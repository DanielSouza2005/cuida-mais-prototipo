ALTER TABLE usuario_token_redefinicao_senha
  ADD COLUMN tentativas_codigo INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN verificado_em TIMESTAMP WITH TIME ZONE,
  ADD COLUMN hash_autorizacao VARCHAR(64),
  ADD COLUMN autorizacao_expira_em TIMESTAMP WITH TIME ZONE,
  ADD COLUMN autorizacao_usada_em TIMESTAMP WITH TIME ZONE,
  ADD COLUMN invalidado_em TIMESTAMP WITH TIME ZONE;

UPDATE usuario_token_redefinicao_senha
SET invalidado_em = COALESCE(usado_em, CURRENT_TIMESTAMP)
WHERE invalidado_em IS NULL;

ALTER TABLE usuario_token_redefinicao_senha
  ADD CONSTRAINT ck_usuario_token_redefinicao_senha_tentativas
    CHECK (tentativas_codigo BETWEEN 0 AND 5),
  ADD CONSTRAINT uk_usuario_token_redefinicao_senha_hash_autorizacao
    UNIQUE (hash_autorizacao);

CREATE INDEX idx_usuario_token_redefinicao_senha_usuario_criado
  ON usuario_token_redefinicao_senha(usuario_id, criado_em DESC);

ALTER TABLE registro_atendimento
  ALTER COLUMN latitude DROP NOT NULL,
  ALTER COLUMN longitude DROP NOT NULL,
  ALTER COLUMN precisao DROP NOT NULL,
  ALTER COLUMN localizacao_capturada_em DROP NOT NULL,
  ADD COLUMN status_localizacao VARCHAR(30) NOT NULL DEFAULT 'OBTIDA';

ALTER TABLE registro_atendimento
  ADD CONSTRAINT ck_registro_atendimento_status_localizacao
    CHECK (status_localizacao IN ('OBTIDA', 'PERMISSAO_NEGADA', 'SERVICO_DESATIVADO', 'TIMEOUT', 'INDISPONIVEL')),
  ADD CONSTRAINT ck_registro_atendimento_localizacao_coerente
    CHECK (
      (status_localizacao = 'OBTIDA' AND latitude IS NOT NULL AND longitude IS NOT NULL AND localizacao_capturada_em IS NOT NULL)
      OR
      (status_localizacao <> 'OBTIDA' AND latitude IS NULL AND longitude IS NULL AND precisao IS NULL AND localizacao_capturada_em IS NULL)
    );
