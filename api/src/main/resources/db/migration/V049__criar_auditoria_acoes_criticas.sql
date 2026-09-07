SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '2min';

CREATE TABLE auditoria_acao_critica (
  id uuid PRIMARY KEY,
  tipo_acao varchar(80) NOT NULL,
  categoria varchar(50) NOT NULL,
  resultado varchar(40) NOT NULL,
  usuario_responsavel_id uuid,
  tipo_usuario_responsavel varchar(40),
  entidade_afetada_tipo varchar(80),
  entidade_afetada_id uuid,
  entidade_relacionada_tipo varchar(80),
  entidade_relacionada_id uuid,
  valor_anterior_resumido jsonb,
  valor_novo_resumido jsonb,
  motivo varchar(500),
  mensagem_resumida varchar(500),
  ip varchar(80),
  user_agent_resumido varchar(255),
  criado_em timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT fk_auditoria_acao_usuario
    FOREIGN KEY (usuario_responsavel_id) REFERENCES usuario(id) ON DELETE SET NULL,
  CONSTRAINT chk_auditoria_acao_categoria
    CHECK (categoria IN ('SEGURANCA', 'ADMINISTRATIVO', 'PRIVACIDADE', 'CONTRATACAO', 'ASSISTENCIAL', 'ATENDIMENTO', 'RELATORIO')),
  CONSTRAINT chk_auditoria_acao_resultado
    CHECK (resultado IN ('SUCESSO', 'FALHA', 'BLOQUEADO_POR_REGRA'))
);

CREATE INDEX idx_auditoria_acao_criado_em
  ON auditoria_acao_critica (criado_em DESC);
CREATE INDEX idx_auditoria_acao_usuario
  ON auditoria_acao_critica (usuario_responsavel_id, criado_em DESC);
CREATE INDEX idx_auditoria_acao_tipo
  ON auditoria_acao_critica (tipo_acao, criado_em DESC);
CREATE INDEX idx_auditoria_acao_entidade
  ON auditoria_acao_critica (entidade_afetada_tipo, entidade_afetada_id, criado_em DESC);

