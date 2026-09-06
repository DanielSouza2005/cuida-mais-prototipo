SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '2min';

ALTER TABLE usuario DROP CONSTRAINT chk_usuario_situacao_conta;
ALTER TABLE usuario
  ADD COLUMN exclusao_solicitada_em timestamptz,
  ADD COLUMN excluido_em timestamptz,
  ADD COLUMN dados_anonimizados_em timestamptz,
  ADD CONSTRAINT chk_usuario_situacao_conta
    CHECK (situacao_conta IN ('ATIVO', 'BLOQUEADO', 'INATIVO', 'EXCLUIDO'));

ALTER TABLE usuario ALTER COLUMN cpf DROP NOT NULL;
ALTER TABLE usuario ALTER COLUMN data_nascimento DROP NOT NULL;
ALTER TABLE pessoa_assistida_contato_emergencia ALTER COLUMN nome DROP NOT NULL;
ALTER TABLE pessoa_assistida_contato_emergencia ALTER COLUMN telefone DROP NOT NULL;

CREATE TABLE usuario_confirmacao_exclusao (
  id uuid PRIMARY KEY,
  usuario_id uuid NOT NULL,
  hash_token varchar(64) NOT NULL,
  expira_em timestamptz NOT NULL,
  usado_em timestamptz,
  criado_em timestamptz NOT NULL,
  CONSTRAINT uk_usuario_confirmacao_exclusao_hash UNIQUE (hash_token),
  CONSTRAINT fk_usuario_confirmacao_exclusao_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE TABLE usuario_exclusao_auditoria (
  id uuid PRIMARY KEY,
  usuario_referencia uuid NOT NULL,
  perfil varchar(30) NOT NULL,
  solicitado_em timestamptz NOT NULL,
  concluido_em timestamptz NOT NULL,
  resultado varchar(30) NOT NULL,
  criado_em timestamptz NOT NULL,
  CONSTRAINT chk_usuario_exclusao_auditoria_resultado
    CHECK (resultado IN ('SUCESSO'))
);

CREATE INDEX idx_usuario_confirmacao_exclusao_usuario
  ON usuario_confirmacao_exclusao(usuario_id, criado_em DESC);
CREATE INDEX idx_usuario_exclusao_auditoria_referencia
  ON usuario_exclusao_auditoria(usuario_referencia, criado_em DESC);
