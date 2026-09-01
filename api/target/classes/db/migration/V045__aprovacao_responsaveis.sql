SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '2min';

ALTER TABLE responsavel
  ADD COLUMN situacao_aprovacao varchar(30) NOT NULL DEFAULT 'APROVADO',
  ADD COLUMN analisado_em timestamptz,
  ADD COLUMN analisado_por_usuario_id uuid,
  ADD COLUMN motivo_reprovacao varchar(1000),
  ADD COLUMN motivo_bloqueio varchar(1000),
  ADD CONSTRAINT chk_responsavel_situacao_aprovacao CHECK (situacao_aprovacao IN ('PENDENTE', 'APROVADO', 'REPROVADO', 'BLOQUEADO')),
  ADD CONSTRAINT fk_responsavel_analisado_por FOREIGN KEY (analisado_por_usuario_id) REFERENCES usuario(id);

ALTER TABLE responsavel ALTER COLUMN situacao_aprovacao SET DEFAULT 'PENDENTE';

CREATE TABLE responsavel_historico_situacao (
  id uuid PRIMARY KEY,
  responsavel_id uuid NOT NULL,
  situacao_anterior varchar(30),
  situacao_nova varchar(30) NOT NULL,
  motivo varchar(1000),
  usuario_administrador_id uuid NOT NULL,
  criado_em timestamptz NOT NULL,
  CONSTRAINT fk_responsavel_historico_responsavel FOREIGN KEY (responsavel_id) REFERENCES responsavel(id),
  CONSTRAINT fk_responsavel_historico_administrador FOREIGN KEY (usuario_administrador_id) REFERENCES usuario(id),
  CONSTRAINT chk_responsavel_historico_situacao_nova CHECK (situacao_nova IN ('PENDENTE', 'APROVADO', 'REPROVADO', 'BLOQUEADO'))
);

CREATE INDEX idx_responsavel_situacao_aprovacao ON responsavel(situacao_aprovacao);
CREATE INDEX idx_responsavel_historico_responsavel_criado ON responsavel_historico_situacao(responsavel_id, criado_em DESC);
