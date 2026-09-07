-- Completa a identificação relacional das tabelas associativas que ainda não
-- possuíam chave primária. Todas as colunas envolvidas já são NOT NULL.
-- As verificações impedem que a migration escolha silenciosamente quais dados
-- preservar caso um banco evoluído contenha duplicidades fora das regras da aplicação.

DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM cuidador_disponibilidade_dia
    GROUP BY perfil_cuidador_id, dia_semana
    HAVING COUNT(*) > 1
  ) THEN
    RAISE EXCEPTION 'Não é possível criar a PK de cuidador_disponibilidade_dia: existem relações duplicadas.';
  END IF;

  IF EXISTS (
    SELECT 1
    FROM cuidador_disponibilidade_periodo
    GROUP BY perfil_cuidador_id, periodo
    HAVING COUNT(*) > 1
  ) THEN
    RAISE EXCEPTION 'Não é possível criar a PK de cuidador_disponibilidade_periodo: existem relações duplicadas.';
  END IF;

  IF EXISTS (
    SELECT 1
    FROM cuidador_formacao
    GROUP BY perfil_cuidador_id, formacao
    HAVING COUNT(*) > 1
  ) THEN
    RAISE EXCEPTION 'Não é possível criar a PK de cuidador_formacao: existem relações duplicadas.';
  END IF;

  IF EXISTS (
    SELECT 1
    FROM cuidador_modalidade
    GROUP BY perfil_cuidador_id, modalidade
    HAVING COUNT(*) > 1
  ) THEN
    RAISE EXCEPTION 'Não é possível criar a PK de cuidador_modalidade: existem relações duplicadas.';
  END IF;

  IF EXISTS (
    SELECT 1
    FROM cuidador_servico
    GROUP BY perfil_cuidador_id, servico
    HAVING COUNT(*) > 1
  ) THEN
    RAISE EXCEPTION 'Não é possível criar a PK de cuidador_servico: existem relações duplicadas.';
  END IF;

  IF EXISTS (
    SELECT 1
    FROM pessoa_assistida_alergia
    GROUP BY pessoa_assistida_id, alergia
    HAVING COUNT(*) > 1
  ) THEN
    RAISE EXCEPTION 'Não é possível criar a PK de pessoa_assistida_alergia: existem relações duplicadas.';
  END IF;

  IF EXISTS (
    SELECT 1
    FROM pessoa_assistida_restricao_alimentar
    GROUP BY pessoa_assistida_id, restricao
    HAVING COUNT(*) > 1
  ) THEN
    RAISE EXCEPTION 'Não é possível criar a PK de pessoa_assistida_restricao_alimentar: existem relações duplicadas.';
  END IF;
END
$$;

ALTER TABLE cuidador_disponibilidade_dia
  ADD CONSTRAINT pk_cuidador_disponibilidade_dia
  PRIMARY KEY (perfil_cuidador_id, dia_semana);

ALTER TABLE cuidador_disponibilidade_periodo
  ADD CONSTRAINT pk_cuidador_disponibilidade_periodo
  PRIMARY KEY (perfil_cuidador_id, periodo);

ALTER TABLE cuidador_formacao
  ADD CONSTRAINT pk_cuidador_formacao
  PRIMARY KEY (perfil_cuidador_id, formacao);

ALTER TABLE cuidador_modalidade
  ADD CONSTRAINT pk_cuidador_modalidade
  PRIMARY KEY (perfil_cuidador_id, modalidade);

ALTER TABLE cuidador_servico
  ADD CONSTRAINT pk_cuidador_servico
  PRIMARY KEY (perfil_cuidador_id, servico);

ALTER TABLE pessoa_assistida_alergia
  ADD CONSTRAINT pk_pessoa_assistida_alergia
  PRIMARY KEY (pessoa_assistida_id, alergia);

ALTER TABLE pessoa_assistida_restricao_alimentar
  ADD CONSTRAINT pk_pessoa_assistida_restricao_alimentar
  PRIMARY KEY (pessoa_assistida_id, restricao);

-- A PK acima mantém exatamente a mesma regra de unicidade desta constraint,
-- evitando dois índices equivalentes sobre cuidador_formacao.
ALTER TABLE cuidador_formacao
  DROP CONSTRAINT IF EXISTS uk_cuidador_formacao_perfil_cuidador_id_formacao;
