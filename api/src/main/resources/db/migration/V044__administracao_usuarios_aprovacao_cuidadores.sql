SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '2min';

ALTER TABLE usuario RENAME COLUMN status TO situacao_conta;
UPDATE usuario SET situacao_conta = CASE UPPER(situacao_conta)
  WHEN 'ACTIVE' THEN 'ATIVO'
  WHEN 'BLOCKED' THEN 'BLOQUEADO'
  WHEN 'INACTIVE' THEN 'INATIVO'
  ELSE situacao_conta END;

ALTER TABLE usuario
  ADD COLUMN motivo_bloqueio varchar(1000),
  ADD COLUMN bloqueado_em timestamptz,
  ADD COLUMN bloqueado_por_usuario_id uuid,
  ADD COLUMN desbloqueado_em timestamptz,
  ADD COLUMN desbloqueado_por_usuario_id uuid,
  ADD COLUMN ultimo_login_em timestamptz,
  ADD CONSTRAINT chk_usuario_situacao_conta CHECK (situacao_conta IN ('ATIVO', 'BLOQUEADO', 'INATIVO')),
  ADD CONSTRAINT fk_usuario_bloqueado_por FOREIGN KEY (bloqueado_por_usuario_id) REFERENCES usuario(id),
  ADD CONSTRAINT fk_usuario_desbloqueado_por FOREIGN KEY (desbloqueado_por_usuario_id) REFERENCES usuario(id);

ALTER TABLE cuidador
  ADD COLUMN situacao_aprovacao varchar(30) NOT NULL DEFAULT 'APROVADO',
  ADD COLUMN analisado_em timestamptz,
  ADD COLUMN analisado_por_usuario_id uuid,
  ADD COLUMN motivo_reprovacao varchar(1000),
  ADD COLUMN motivo_bloqueio_profissional varchar(1000),
  ADD CONSTRAINT chk_cuidador_situacao_aprovacao CHECK (situacao_aprovacao IN ('PENDENTE', 'APROVADO', 'REPROVADO', 'BLOQUEADO')),
  ADD CONSTRAINT fk_cuidador_analisado_por FOREIGN KEY (analisado_por_usuario_id) REFERENCES usuario(id);

ALTER TABLE cuidador ALTER COLUMN situacao_aprovacao SET DEFAULT 'PENDENTE';

CREATE TABLE cuidador_historico_situacao (
  id uuid PRIMARY KEY,
  cuidador_id uuid NOT NULL,
  situacao_anterior varchar(30),
  situacao_nova varchar(30) NOT NULL,
  motivo varchar(1000),
  usuario_administrador_id uuid NOT NULL,
  criado_em timestamptz NOT NULL,
  CONSTRAINT fk_cuidador_historico_cuidador FOREIGN KEY (cuidador_id) REFERENCES cuidador(id),
  CONSTRAINT fk_cuidador_historico_administrador FOREIGN KEY (usuario_administrador_id) REFERENCES usuario(id),
  CONSTRAINT chk_cuidador_historico_situacao_nova CHECK (situacao_nova IN ('PENDENTE', 'APROVADO', 'REPROVADO', 'BLOQUEADO'))
);

CREATE INDEX idx_usuario_situacao_conta ON usuario(situacao_conta);
CREATE INDEX idx_cuidador_situacao_aprovacao ON cuidador(situacao_aprovacao);
CREATE INDEX idx_cuidador_historico_cuidador_criado ON cuidador_historico_situacao(cuidador_id, criado_em DESC);
