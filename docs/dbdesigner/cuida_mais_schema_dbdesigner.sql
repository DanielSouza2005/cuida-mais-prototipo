-- Script de modelagem do banco Cuidar+
-- Gerado por introspecção direta do banco PostgreSQL.
-- Destinado à importação no DBDesigner.
-- Não contém dados reais.
-- Não contém usuários, senhas, hashes, tokens ou informações sensíveis.
-- A tabela flyway_schema_history foi omitida por ser infraestrutura do Flyway.
-- Observação: uuid, timestamptz, json/jsonb e numeric foram simplificados para melhorar a compatibilidade com o DBDesigner.

CREATE TABLE "auditoria_acao_critica" (
  "id" varchar(36) NOT NULL,
  "tipo_acao" varchar(80) NOT NULL,
  "categoria" varchar(50) NOT NULL,
  "resultado" varchar(40) NOT NULL,
  "usuario_responsavel_id" varchar(36),
  "tipo_usuario_responsavel" varchar(40),
  "entidade_afetada_tipo" varchar(80),
  "entidade_afetada_id" varchar(36),
  "entidade_relacionada_tipo" varchar(80),
  "entidade_relacionada_id" varchar(36),
  "valor_anterior_resumido" text,
  "valor_novo_resumido" text,
  "motivo" varchar(500),
  "mensagem_resumida" varchar(500),
  "ip" varchar(80),
  "user_agent_resumido" varchar(255),
  "criado_em" timestamp DEFAULT now() NOT NULL,
  CONSTRAINT "auditoria_acao_critica_pkey" PRIMARY KEY (id),
  CONSTRAINT "chk_auditoria_acao_categoria" CHECK (categoria IN ('SEGURANCA', 'ADMINISTRATIVO', 'PRIVACIDADE', 'CONTRATACAO', 'ASSISTENCIAL', 'ATENDIMENTO', 'RELATORIO')),
  CONSTRAINT "chk_auditoria_acao_resultado" CHECK (resultado IN ('SUCESSO', 'FALHA', 'BLOQUEADO_POR_REGRA'))
);

CREATE TABLE "contratacao" (
  "id" varchar(36) NOT NULL,
  "solicitacao_servico_id" varchar(36) NOT NULL,
  "usuario_responsavel_id" varchar(36) NOT NULL,
  "usuario_cuidador_id" varchar(36) NOT NULL,
  "pessoa_assistida_id" varchar(36) NOT NULL,
  "status" varchar(30) NOT NULL,
  "data_inicio" date NOT NULL,
  "data_fim" date,
  "criado_em" timestamp NOT NULL,
  "atualizado_em" timestamp NOT NULL,
  "motivo_cancelamento" varchar(1000),
  "motivo_encerramento" varchar(1000),
  "tipo_encerramento" varchar(50),
  "motivo_solicitacao_encerramento" varchar(1000),
  "observacoes_encerramento" varchar(1000),
  "usuario_solicitante_encerramento_id" varchar(36),
  "encerramento_solicitado_em" timestamp,
  "data_fim_efetiva" date,
  "cancelado_em" timestamp,
  "usuario_solicitante_cancelamento_id" varchar(36),
  "cancelamento_solicitado_em" timestamp,
  CONSTRAINT "pk_contratacao" PRIMARY KEY (id),
  CONSTRAINT "uk_contratacao_solicitacao_servico_id" UNIQUE (solicitacao_servico_id)
);

CREATE TABLE "cuidador" (
  "id" varchar(36) NOT NULL,
  "usuario_id" varchar(36) NOT NULL,
  "formacao_outro" varchar(180),
  "experiencia" varchar(500),
  "biografia" varchar(500),
  "cep" varchar(9),
  "rua" varchar(180),
  "numero" varchar(30),
  "complemento" varchar(120),
  "bairro" varchar(120),
  "cidade" varchar(120),
  "estado" varchar(2),
  "ponto_referencia" varchar(180),
  "horario_inicio" time,
  "horario_fim" time,
  "observacao" varchar(500),
  "modalidade_outro" varchar(180),
  "servico_outro" varchar(180),
  "criado_em" timestamp NOT NULL,
  "atualizado_em" timestamp NOT NULL,
  "tempo_experiencia" varchar(30),
  "latitude" decimal(10,7),
  "longitude" decimal(10,7),
  "situacao_aprovacao" varchar(30) DEFAULT 'PENDENTE' NOT NULL,
  "analisado_em" timestamp,
  "analisado_por_usuario_id" varchar(36),
  "motivo_reprovacao" varchar(1000),
  "motivo_bloqueio_profissional" varchar(1000),
  CONSTRAINT "pk_cuidador" PRIMARY KEY (id),
  CONSTRAINT "uk_cuidador_usuario_id" UNIQUE (usuario_id),
  CONSTRAINT "chk_cuidador_situacao_aprovacao" CHECK (situacao_aprovacao IN ('PENDENTE', 'APROVADO', 'REPROVADO', 'BLOQUEADO'))
);

CREATE TABLE "cuidador_disponibilidade_dia" (
  "perfil_cuidador_id" varchar(36) NOT NULL,
  "dia_semana" varchar(20) NOT NULL,
  CONSTRAINT "pk_cuidador_disponibilidade_dia" PRIMARY KEY (perfil_cuidador_id, dia_semana)
);

CREATE TABLE "cuidador_disponibilidade_periodo" (
  "perfil_cuidador_id" varchar(36) NOT NULL,
  "periodo" varchar(30) NOT NULL,
  CONSTRAINT "pk_cuidador_disponibilidade_periodo" PRIMARY KEY (perfil_cuidador_id, periodo)
);

CREATE TABLE "cuidador_formacao" (
  "perfil_cuidador_id" varchar(36) NOT NULL,
  "formacao" varchar(40) NOT NULL,
  CONSTRAINT "pk_cuidador_formacao" PRIMARY KEY (perfil_cuidador_id, formacao)
);

CREATE TABLE "cuidador_historico_situacao" (
  "id" varchar(36) NOT NULL,
  "cuidador_id" varchar(36) NOT NULL,
  "situacao_anterior" varchar(30),
  "situacao_nova" varchar(30) NOT NULL,
  "motivo" varchar(1000),
  "usuario_administrador_id" varchar(36) NOT NULL,
  "criado_em" timestamp NOT NULL,
  CONSTRAINT "cuidador_historico_situacao_pkey" PRIMARY KEY (id),
  CONSTRAINT "chk_cuidador_historico_situacao_nova" CHECK (situacao_nova IN ('PENDENTE', 'APROVADO', 'REPROVADO', 'BLOQUEADO'))
);

CREATE TABLE "cuidador_modalidade" (
  "perfil_cuidador_id" varchar(36) NOT NULL,
  "modalidade" varchar(40) NOT NULL,
  CONSTRAINT "pk_cuidador_modalidade" PRIMARY KEY (perfil_cuidador_id, modalidade)
);

CREATE TABLE "cuidador_servico" (
  "perfil_cuidador_id" varchar(36) NOT NULL,
  "servico" varchar(50) NOT NULL,
  CONSTRAINT "pk_cuidador_servico" PRIMARY KEY (perfil_cuidador_id, servico)
);

CREATE TABLE "notificacao" (
  "id" varchar(36) NOT NULL,
  "usuario_destinatario_id" varchar(36) NOT NULL,
  "tipo" varchar(50) NOT NULL,
  "titulo" varchar(180) NOT NULL,
  "mensagem" varchar(500) NOT NULL,
  "tipo_entidade_relacionada" varchar(40) NOT NULL,
  "entidade_relacionada_id" varchar(36) NOT NULL,
  "lida_em" timestamp,
  "removida_em" timestamp,
  "criado_em" timestamp NOT NULL,
  "chave_deduplicacao" varchar(220),
  CONSTRAINT "pk_notificacao" PRIMARY KEY (id)
);

CREATE TABLE "notificacao_preferencia" (
  "id" varchar(36) NOT NULL,
  "usuario_id" varchar(36) NOT NULL,
  "tipo_notificacao" varchar(64) NOT NULL,
  "habilitado" boolean NOT NULL,
  "criado_em" timestamp NOT NULL,
  "atualizado_em" timestamp NOT NULL,
  CONSTRAINT "pk_notificacao_preferencia" PRIMARY KEY (id),
  CONSTRAINT "uk_notificacao_preferencia_usuario_id_tipo_notificacao" UNIQUE (usuario_id, tipo_notificacao)
);

CREATE TABLE "ocorrencia_cuidado" (
  "id" varchar(36) NOT NULL,
  "tarefa_id" varchar(36) NOT NULL,
  "contratacao_id" varchar(36) NOT NULL,
  "pessoa_assistida_id" varchar(36) NOT NULL,
  "usuario_cuidador_id" varchar(36) NOT NULL,
  "data_prevista" date NOT NULL,
  "horario_previsto" time NOT NULL,
  "instante_previsto_utc" timestamp NOT NULL,
  "fuso_horario" varchar(80) NOT NULL,
  "status" varchar(25) NOT NULL,
  "concluido_em" timestamp,
  "usuario_executor_id" varchar(36),
  "motivo_nao_realizacao" varchar(1000),
  "anotacao_execucao" varchar(1000),
  "cancelado_em" timestamp,
  "excecao" boolean DEFAULT false NOT NULL,
  "criado_em" timestamp NOT NULL,
  "atualizado_em" timestamp NOT NULL,
  "versao" bigint DEFAULT 0 NOT NULL,
  "marcada_nao_realizada_automaticamente" boolean DEFAULT false NOT NULL,
  "status_atualizado_em" timestamp,
  CONSTRAINT "pk_ocorrencia_cuidado" PRIMARY KEY (id),
  CONSTRAINT "uk_ocorrencia_cuidado_tarefa_id_data_prevista_horario_previsto" UNIQUE (tarefa_id, data_prevista, horario_previsto)
);

CREATE TABLE "ocorrencia_cuidado_foto" (
  "id" varchar(36) NOT NULL,
  "ocorrencia_id" varchar(36),
  "usuario_envio_id" varchar(36) NOT NULL,
  "nome_arquivo" varchar(80) NOT NULL,
  "nome_arquivo_original" varchar(255),
  "tipo_conteudo" varchar(30) NOT NULL,
  "tamanho_arquivo" bigint NOT NULL,
  "criado_em" timestamp NOT NULL,
  "registro_atividade_id" varchar(36),
  CONSTRAINT "pk_ocorrencia_cuidado_foto" PRIMARY KEY (id),
  CONSTRAINT "uk_ocorrencia_cuidado_foto_nome_arquivo" UNIQUE (nome_arquivo),
  CONSTRAINT "ck_ocorrencia_cuidado_foto_vinculo_unico" CHECK (ocorrencia_id IS NOT NULL AND registro_atividade_id IS NULL OR ocorrencia_id IS NULL AND registro_atividade_id IS NOT NULL)
);

CREATE TABLE "ocorrencia_cuidado_lembrete" (
  "id" varchar(36) NOT NULL,
  "ocorrencia_id" varchar(36) NOT NULL,
  "usuario_destinatario_id" varchar(36) NOT NULL,
  "tipo_lembrete" varchar(40) NOT NULL,
  "previsto_em" timestamp NOT NULL,
  "enviado_em" timestamp,
  "cancelado_em" timestamp,
  "status" varchar(20) NOT NULL,
  "chave_deduplicacao" varchar(220) NOT NULL,
  "criado_em" timestamp NOT NULL,
  "atualizado_em" timestamp NOT NULL,
  CONSTRAINT "pk_ocorrencia_cuidado_lembrete" PRIMARY KEY (id),
  CONSTRAINT "uk_ocorrencia_cuidado_lembrete_chave_deduplicacao" UNIQUE (chave_deduplicacao)
);

CREATE TABLE "pessoa_assistida" (
  "id" varchar(36) NOT NULL,
  "usuario_responsavel_id" varchar(36) NOT NULL,
  "nome" varchar(140) NOT NULL,
  "data_nascimento" date NOT NULL,
  "grau_dependencia" varchar(30) NOT NULL,
  "mobilidade" varchar(30) NOT NULL,
  "mobilidade_outro" varchar(120),
  "alergias_outro" varchar(180),
  "alergias_detalhes" varchar(500),
  "restricoes_alimentares_outro" varchar(180),
  "restricoes_alimentares_detalhes" varchar(500),
  "medicamentos" varchar(500),
  "observacoes" varchar(500),
  "cep" varchar(9),
  "rua" varchar(180),
  "numero" varchar(30),
  "complemento" varchar(120),
  "bairro" varchar(120),
  "cidade" varchar(120),
  "estado" varchar(2),
  "ponto_referencia" varchar(180),
  "criado_em" timestamp NOT NULL,
  "atualizado_em" timestamp NOT NULL,
  "latitude" decimal(10,7),
  "longitude" decimal(10,7),
  CONSTRAINT "pk_pessoa_assistida" PRIMARY KEY (id)
);

CREATE TABLE "pessoa_assistida_alergia" (
  "pessoa_assistida_id" varchar(36) NOT NULL,
  "alergia" varchar(40) NOT NULL,
  CONSTRAINT "pk_pessoa_assistida_alergia" PRIMARY KEY (pessoa_assistida_id, alergia)
);

CREATE TABLE "pessoa_assistida_contato_emergencia" (
  "id" varchar(36) NOT NULL,
  "pessoa_assistida_id" varchar(36) NOT NULL,
  "nome" varchar(140),
  "telefone" varchar(20),
  "vinculo" varchar(120) NOT NULL,
  "contato_responsavel" boolean DEFAULT false NOT NULL,
  "criado_em" timestamp NOT NULL,
  "atualizado_em" timestamp NOT NULL,
  CONSTRAINT "pk_pessoa_assistida_contato_emergencia" PRIMARY KEY (id),
  CONSTRAINT "uk_pessoa_assistida_contato_emergencia_pessoa_assistida_id" UNIQUE (pessoa_assistida_id)
);

CREATE TABLE "pessoa_assistida_restricao_alimentar" (
  "pessoa_assistida_id" varchar(36) NOT NULL,
  "restricao" varchar(40) NOT NULL,
  CONSTRAINT "pk_pessoa_assistida_restricao_alimentar" PRIMARY KEY (pessoa_assistida_id, restricao)
);

CREATE TABLE "registro_atendimento" (
  "id" varchar(36) NOT NULL,
  "contratacao_id" varchar(36) NOT NULL,
  "cuidador_id" varchar(36) NOT NULL,
  "responsavel_id" varchar(36) NOT NULL,
  "pessoa_assistida_id" varchar(36) NOT NULL,
  "data_atendimento" date NOT NULL,
  "tipo_registro" varchar(10) NOT NULL,
  "registrado_em" timestamp NOT NULL,
  "latitude" double precision NOT NULL,
  "longitude" double precision NOT NULL,
  "precisao" double precision NOT NULL,
  "localizacao_capturada_em" timestamp NOT NULL,
  "endereco_registrado" varchar(500),
  "fuso_dispositivo" varchar(80) NOT NULL,
  "horario_inicio_previsto" time NOT NULL,
  "horario_fim_previsto" time NOT NULL,
  "janela_permitida_inicio" timestamp NOT NULL,
  "janela_permitida_fim" timestamp NOT NULL,
  "dentro_janela_permitida" boolean NOT NULL,
  "criado_em" timestamp NOT NULL,
  "atualizado_em" timestamp NOT NULL,
  CONSTRAINT "pk_registro_atendimento" PRIMARY KEY (id),
  CONSTRAINT "uk_registro_atendimento_contratacao_id_data_atendiment_fc5cf1c5" UNIQUE (contratacao_id, data_atendimento, tipo_registro),
  CONSTRAINT "ck_registro_atendimento_latitude" CHECK (latitude >= '-90' AND latitude <= 90),
  CONSTRAINT "ck_registro_atendimento_longitude" CHECK (longitude >= '-180' AND longitude <= 180),
  CONSTRAINT "ck_registro_atendimento_precisao" CHECK (precisao >= 0 AND precisao <= 1000),
  CONSTRAINT "ck_registro_atendimento_tipo" CHECK (tipo_registro IN ('START', 'END'))
);

CREATE TABLE "registro_diario_cuidado" (
  "id" varchar(36) NOT NULL,
  "ocorrencia_id" varchar(36),
  "contratacao_id" varchar(36) NOT NULL,
  "pessoa_assistida_id" varchar(36) NOT NULL,
  "usuario_responsavel_id" varchar(36) NOT NULL,
  "usuario_cuidador_id" varchar(36) NOT NULL,
  "tipo_atividade" varchar(40) NOT NULL,
  "titulo" varchar(180) NOT NULL,
  "anotacoes" varchar(1000),
  "ocorrido_em" timestamp NOT NULL,
  "criado_em" timestamp NOT NULL,
  "tipo_origem" varchar(20) DEFAULT 'PLANNED' NOT NULL,
  "data_registro" date NOT NULL,
  "fuso_horario" varchar(80) NOT NULL,
  "tipo_cuidado" varchar(40) NOT NULL,
  "descricao" varchar(2000),
  "importante" boolean DEFAULT false NOT NULL,
  "usuario_criacao_id" varchar(36) NOT NULL,
  CONSTRAINT "pk_registro_diario_cuidado" PRIMARY KEY (id),
  CONSTRAINT "uk_registro_diario_cuidado_ocorrencia_id" UNIQUE (ocorrencia_id)
);

CREATE TABLE "relatorio_atendimento" (
  "id" varchar(36) NOT NULL,
  "contratacao_id" varchar(36) NOT NULL,
  "data_atendimento" date NOT NULL,
  "registro_inicio_atendimento_id" varchar(36) NOT NULL,
  "registro_fim_atendimento_id" varchar(36) NOT NULL,
  "cuidador_id" varchar(36) NOT NULL,
  "responsavel_id" varchar(36) NOT NULL,
  "pessoa_assistida_id" varchar(36) NOT NULL,
  "texto_gerado" text NOT NULL,
  "texto_editado" text,
  "texto_final" text,
  "observacoes_adicionais" varchar(4000),
  "anotacoes_enfermagem" text NOT NULL,
  "status" varchar(20) NOT NULL,
  "status_email" varchar(20) DEFAULT 'NOT_SENT' NOT NULL,
  "email_enviado_em" timestamp,
  "mensagem_erro_email" varchar(500),
  "gerado_em" timestamp NOT NULL,
  "editado_em" timestamp,
  "finalizado_em" timestamp,
  "criado_em" timestamp NOT NULL,
  "atualizado_em" timestamp NOT NULL,
  "email_solicitado_em" timestamp,
  "tentativas_email" integer DEFAULT 0 NOT NULL,
  "proxima_tentativa_email_em" timestamp,
  CONSTRAINT "pk_relatorio_atendimento" PRIMARY KEY (id),
  CONSTRAINT "uk_relatorio_atendimento_contratacao_id_data_atendimento" UNIQUE (contratacao_id, data_atendimento),
  CONSTRAINT "ck_relatorio_atendimento_status" CHECK (status IN ('DRAFT', 'FINALIZED')),
  CONSTRAINT "ck_relatorio_atendimento_status_email" CHECK (status_email IN ('NOT_SENT', 'PENDING', 'SENT', 'FAILED'))
);

CREATE TABLE "responsavel" (
  "id" varchar(36) NOT NULL,
  "usuario_id" varchar(36) NOT NULL,
  "parentesco" varchar(40) NOT NULL,
  "parentesco_outro" varchar(120),
  "preferencia_contato" varchar(30),
  "criado_em" timestamp NOT NULL,
  "atualizado_em" timestamp NOT NULL,
  "situacao_aprovacao" varchar(30) DEFAULT 'PENDENTE' NOT NULL,
  "analisado_em" timestamp,
  "analisado_por_usuario_id" varchar(36),
  "motivo_reprovacao" varchar(1000),
  "motivo_bloqueio" varchar(1000),
  CONSTRAINT "pk_responsavel" PRIMARY KEY (id),
  CONSTRAINT "uk_responsavel_usuario_id" UNIQUE (usuario_id),
  CONSTRAINT "chk_responsavel_situacao_aprovacao" CHECK (situacao_aprovacao IN ('PENDENTE', 'APROVADO', 'REPROVADO', 'BLOQUEADO'))
);

CREATE TABLE "responsavel_historico_situacao" (
  "id" varchar(36) NOT NULL,
  "responsavel_id" varchar(36) NOT NULL,
  "situacao_anterior" varchar(30),
  "situacao_nova" varchar(30) NOT NULL,
  "motivo" varchar(1000),
  "usuario_administrador_id" varchar(36) NOT NULL,
  "criado_em" timestamp NOT NULL,
  CONSTRAINT "responsavel_historico_situacao_pkey" PRIMARY KEY (id),
  CONSTRAINT "chk_responsavel_historico_situacao_nova" CHECK (situacao_nova IN ('PENDENTE', 'APROVADO', 'REPROVADO', 'BLOQUEADO'))
);

CREATE TABLE "rotina_cuidado" (
  "id" varchar(36) NOT NULL,
  "usuario_responsavel_id" varchar(36) NOT NULL,
  "pessoa_assistida_id" varchar(36),
  "nome" varchar(140) NOT NULL,
  "descricao" varchar(1000),
  "ativo" boolean DEFAULT true NOT NULL,
  "criado_em" timestamp NOT NULL,
  "atualizado_em" timestamp NOT NULL,
  CONSTRAINT "pk_rotina_cuidado" PRIMARY KEY (id)
);

CREATE TABLE "rotina_cuidado_item" (
  "id" varchar(36) NOT NULL,
  "rotina_cuidado_id" varchar(36) NOT NULL,
  "titulo" varchar(140) NOT NULL,
  "descricao" varchar(1000),
  "ordem_exibicao" integer NOT NULL,
  "ativo" boolean DEFAULT true NOT NULL,
  "criado_em" timestamp NOT NULL,
  "atualizado_em" timestamp NOT NULL,
  "categoria" varchar(40),
  "categoria_personalizada" varchar(120),
  "prioridade" varchar(20),
  "tipo_recorrencia" varchar(40),
  "horario_previsto" time,
  "intervalo_dias" integer,
  "lembrete_habilitado" boolean,
  "minutos_antecedencia_lembrete" integer,
  "anotacoes" varchar(2000),
  "nome_medicamento" varchar(180),
  "dosagem_medicamento" varchar(80),
  "unidade_medicamento" varchar(30),
  "unidade_personalizada_medicamento" varchar(80),
  "via_administracao_medicamento" varchar(30),
  "via_personalizada_medicamento" varchar(120),
  "instrucoes_medicamento" varchar(1000),
  "lembrar_no_horario_previsto" boolean DEFAULT true NOT NULL,
  "lembrete_atraso_habilitado" boolean DEFAULT false NOT NULL,
  "minutos_para_atraso" integer,
  "repetir_enquanto_pendente" boolean DEFAULT false NOT NULL,
  "intervalo_repeticao_minutos" integer,
  "importante" boolean DEFAULT false NOT NULL,
  "notificar_responsavel_se_importante" boolean DEFAULT false NOT NULL,
  "exige_foto_conclusao" boolean DEFAULT false NOT NULL,
  CONSTRAINT "pk_rotina_cuidado_item" PRIMARY KEY (id)
);

CREATE TABLE "rotina_cuidado_item_dia_semana" (
  "item_rotina_cuidado_id" varchar(36) NOT NULL,
  "dia_semana" varchar(20) NOT NULL,
  CONSTRAINT "pk_rotina_cuidado_item_dia_semana" PRIMARY KEY (item_rotina_cuidado_id, dia_semana)
);

CREATE TABLE "solicitacao_servico" (
  "id" varchar(36) NOT NULL,
  "usuario_responsavel_id" varchar(36) NOT NULL,
  "usuario_cuidador_id" varchar(36),
  "pessoa_assistida_id" varchar(36) NOT NULL,
  "tipo_contratacao" varchar(40) NOT NULL,
  "status" varchar(30) NOT NULL,
  "data_inicio" date,
  "data_fim" date,
  "descricao_necessidades" varchar(2000) NOT NULL,
  "outra_atividade" varchar(500),
  "observacoes_adicionais" varchar(2000),
  "observacoes_negociacao" varchar(1000),
  "criado_em" timestamp NOT NULL,
  "atualizado_em" timestamp NOT NULL,
  "expira_em" timestamp NOT NULL,
  "cancelado_em" timestamp,
  "motivo_rejeicao" varchar(1000),
  "motivo_cancelamento" varchar(1000),
  "rotina_cuidado_id" varchar(36),
  "nome_rotina_copia" varchar(140),
  "iniciado_por" varchar(20) DEFAULT 'RESPONSIBLE' NOT NULL,
  "usuario_solicitante_id" varchar(36) NOT NULL,
  "oportunidade_origem_id" varchar(36),
  CONSTRAINT "pk_solicitacao_servico" PRIMARY KEY (id)
);

CREATE TABLE "solicitacao_servico_agenda_dia" (
  "solicitacao_servico_id" varchar(36) NOT NULL,
  "dia_semana" varchar(20) NOT NULL,
  "horario_inicio" time NOT NULL,
  "horario_fim" time NOT NULL,
  CONSTRAINT "pk_solicitacao_servico_agenda_dia" PRIMARY KEY (solicitacao_servico_id, dia_semana)
);

CREATE TABLE "solicitacao_servico_atividade" (
  "solicitacao_servico_id" varchar(36) NOT NULL,
  "atividade" varchar(50) NOT NULL,
  CONSTRAINT "pk_solicitacao_servico_atividade" PRIMARY KEY (solicitacao_servico_id, atividade)
);

CREATE TABLE "solicitacao_servico_contratacao_historico_status" (
  "id" varchar(36) NOT NULL,
  "tipo_entidade" varchar(40) NOT NULL,
  "entidade_id" varchar(36) NOT NULL,
  "status_anterior" varchar(30),
  "novo_status" varchar(30) NOT NULL,
  "usuario_alteracao_id" varchar(36),
  "motivo" varchar(1000),
  "criado_em" timestamp NOT NULL,
  CONSTRAINT "pk_solicitacao_servico_contratacao_historico_status" PRIMARY KEY (id)
);

CREATE TABLE "solicitacao_servico_data" (
  "solicitacao_servico_id" varchar(36) NOT NULL,
  "data_servico" date NOT NULL,
  CONSTRAINT "pk_solicitacao_servico_data" PRIMARY KEY (solicitacao_servico_id, data_servico)
);

CREATE TABLE "solicitacao_servico_item_cuidado_copia" (
  "id" varchar(36) NOT NULL,
  "solicitacao_servico_id" varchar(36) NOT NULL,
  "rotina_cuidado_original_id" varchar(36) NOT NULL,
  "item_rotina_cuidado_original_id" varchar(36),
  "titulo" varchar(140) NOT NULL,
  "descricao" varchar(1000),
  "ordem_exibicao" integer NOT NULL,
  "criado_em" timestamp NOT NULL,
  "categoria" varchar(40),
  "categoria_personalizada" varchar(120),
  "prioridade" varchar(20),
  "tipo_recorrencia" varchar(40),
  "horario_previsto" time,
  "intervalo_dias" integer,
  "lembrete_habilitado" boolean,
  "minutos_antecedencia_lembrete" integer,
  "anotacoes" varchar(2000),
  "nome_medicamento" varchar(180),
  "dosagem_medicamento" varchar(80),
  "unidade_medicamento" varchar(30),
  "unidade_personalizada_medicamento" varchar(80),
  "via_administracao_medicamento" varchar(30),
  "via_personalizada_medicamento" varchar(120),
  "instrucoes_medicamento" varchar(1000),
  "lembrar_no_horario_previsto" boolean DEFAULT true NOT NULL,
  "lembrete_atraso_habilitado" boolean DEFAULT false NOT NULL,
  "minutos_para_atraso" integer,
  "repetir_enquanto_pendente" boolean DEFAULT false NOT NULL,
  "intervalo_repeticao_minutos" integer,
  "importante" boolean DEFAULT false NOT NULL,
  "notificar_responsavel_se_importante" boolean DEFAULT false NOT NULL,
  "exige_foto_conclusao" boolean DEFAULT false NOT NULL,
  CONSTRAINT "pk_solicitacao_servico_item_cuidado_copia" PRIMARY KEY (id)
);

CREATE TABLE "solicitacao_servico_item_cuidado_copia_dia_semana" (
  "item_copia_id" varchar(36) NOT NULL,
  "dia_semana" varchar(20) NOT NULL,
  CONSTRAINT "pk_solicitacao_servico_item_cuidado_copia_dia_semana" PRIMARY KEY (item_copia_id, dia_semana)
);

CREATE TABLE "tarefa_cuidado" (
  "id" varchar(36) NOT NULL,
  "titulo" varchar(140) NOT NULL,
  "descricao" varchar(2000),
  "categoria" varchar(40) NOT NULL,
  "categoria_personalizada" varchar(120),
  "prioridade" varchar(20) NOT NULL,
  "tipo_recorrencia" varchar(40) NOT NULL,
  "data_inicio" date NOT NULL,
  "data_fim" date,
  "horario_previsto" time NOT NULL,
  "intervalo_dias" integer,
  "fuso_horario" varchar(80) NOT NULL,
  "lembrete_habilitado" boolean DEFAULT false NOT NULL,
  "minutos_antecedencia_lembrete" integer,
  "anotacoes" varchar(2000),
  "status" varchar(20) NOT NULL,
  "pessoa_assistida_id" varchar(36) NOT NULL,
  "contratacao_id" varchar(36) NOT NULL,
  "responsavel_criador_id" varchar(36) NOT NULL,
  "cuidador_executor_id" varchar(36) NOT NULL,
  "serie_anterior_id" varchar(36),
  "nome_medicamento" varchar(180),
  "dosagem_medicamento" varchar(80),
  "unidade_medicamento" varchar(30),
  "unidade_personalizada_medicamento" varchar(80),
  "via_administracao_medicamento" varchar(30),
  "via_personalizada_medicamento" varchar(120),
  "instrucoes_medicamento" varchar(1000),
  "criado_em" timestamp NOT NULL,
  "atualizado_em" timestamp NOT NULL,
  "usuario_criacao_id" varchar(36) NOT NULL,
  "usuario_atualizacao_id" varchar(36) NOT NULL,
  "versao" bigint DEFAULT 0 NOT NULL,
  "item_copia_origem_id" varchar(36),
  "lembrar_no_horario_previsto" boolean DEFAULT true NOT NULL,
  "lembrete_atraso_habilitado" boolean DEFAULT false NOT NULL,
  "minutos_para_atraso" integer,
  "repetir_enquanto_pendente" boolean DEFAULT false NOT NULL,
  "intervalo_repeticao_minutos" integer,
  "importante" boolean DEFAULT false NOT NULL,
  "notificar_responsavel_se_importante" boolean DEFAULT false NOT NULL,
  "exige_foto_conclusao" boolean DEFAULT false NOT NULL,
  "tarefa_duplicada_de_id" varchar(36),
  CONSTRAINT "pk_tarefa_cuidado" PRIMARY KEY (id),
  CONSTRAINT "ck_tarefa_cuidado_datas" CHECK (data_fim IS NULL OR data_fim >= data_inicio),
  CONSTRAINT "ck_tarefa_cuidado_intervalo" CHECK (intervalo_dias IS NULL OR intervalo_dias > 0),
  CONSTRAINT "ck_tarefa_cuidado_lembrete" CHECK (NOT lembrete_habilitado OR minutos_antecedencia_lembrete IS NOT NULL AND minutos_antecedencia_lembrete >= 0)
);

CREATE TABLE "tarefa_cuidado_auditoria" (
  "id" varchar(36) NOT NULL,
  "tarefa_id" varchar(36) NOT NULL,
  "ocorrencia_id" varchar(36),
  "usuario_ator_id" varchar(36),
  "acao" varchar(40) NOT NULL,
  "detalhes" varchar(500),
  "criado_em" timestamp NOT NULL,
  CONSTRAINT "pk_tarefa_cuidado_auditoria" PRIMARY KEY (id)
);

CREATE TABLE "tarefa_cuidado_dia_semana" (
  "tarefa_id" varchar(36) NOT NULL,
  "dia_semana" varchar(20) NOT NULL,
  CONSTRAINT "pk_tarefa_cuidado_dia_semana" PRIMARY KEY (tarefa_id, dia_semana)
);

CREATE TABLE "usuario" (
  "id" varchar(36) NOT NULL,
  "criado_em" timestamp NOT NULL,
  "email" varchar(180) NOT NULL,
  "nome_completo" varchar(140) NOT NULL,
  "senha_hash" varchar(255) NOT NULL,
  "atualizado_em" timestamp NOT NULL,
  "tipo_usuario" varchar(20) NOT NULL,
  "telefone" varchar(20),
  "situacao_conta" varchar(30) DEFAULT 'ACTIVE' NOT NULL,
  "url_foto_perfil" varchar(500),
  "motivo_bloqueio" varchar(1000),
  "bloqueado_em" timestamp,
  "bloqueado_por_usuario_id" varchar(36),
  "desbloqueado_em" timestamp,
  "desbloqueado_por_usuario_id" varchar(36),
  "ultimo_login_em" timestamp,
  "exclusao_solicitada_em" timestamp,
  "excluido_em" timestamp,
  "dados_anonimizados_em" timestamp,
  "maior_de_idade_confirmado" boolean NOT NULL,
  CONSTRAINT "pk_usuario" PRIMARY KEY (id),
  CONSTRAINT "uk_usuario_email" UNIQUE (email),
  CONSTRAINT "chk_usuario_situacao_conta" CHECK (situacao_conta IN ('ATIVO', 'BLOQUEADO', 'INATIVO', 'EXCLUIDO')),
  CONSTRAINT "ck_usuario_tipo_usuario" CHECK (tipo_usuario IN ('RESPONSAVEL', 'CUIDADOR', 'ADMIN', 'FAMILY', 'CAREGIVER'))
);

CREATE TABLE "usuario_confirmacao_exclusao" (
  "id" varchar(36) NOT NULL,
  "usuario_id" varchar(36) NOT NULL,
  "hash_token" varchar(64) NOT NULL,
  "expira_em" timestamp NOT NULL,
  "usado_em" timestamp,
  "criado_em" timestamp NOT NULL,
  CONSTRAINT "usuario_confirmacao_exclusao_pkey" PRIMARY KEY (id),
  CONSTRAINT "uk_usuario_confirmacao_exclusao_hash" UNIQUE (hash_token)
);

CREATE TABLE "usuario_exclusao_auditoria" (
  "id" varchar(36) NOT NULL,
  "usuario_referencia" varchar(36) NOT NULL,
  "perfil" varchar(30) NOT NULL,
  "solicitado_em" timestamp NOT NULL,
  "concluido_em" timestamp NOT NULL,
  "resultado" varchar(30) NOT NULL,
  "criado_em" timestamp NOT NULL,
  CONSTRAINT "usuario_exclusao_auditoria_pkey" PRIMARY KEY (id),
  CONSTRAINT "chk_usuario_exclusao_auditoria_resultado" CHECK (resultado = 'SUCESSO')
);

CREATE TABLE "usuario_token_redefinicao_senha" (
  "id" varchar(36) NOT NULL,
  "criado_em" timestamp NOT NULL,
  "expira_em" timestamp NOT NULL,
  "hash_token" varchar(64) NOT NULL,
  "usado_em" timestamp,
  "usuario_id" varchar(36) NOT NULL,
  CONSTRAINT "pk_usuario_token_redefinicao_senha" PRIMARY KEY (id),
  CONSTRAINT "uk_usuario_token_redefinicao_senha_hash_token" UNIQUE (hash_token)
);

-- Relacionamentos

ALTER TABLE "auditoria_acao_critica"
  ADD CONSTRAINT "fk_auditoria_acao_usuario" FOREIGN KEY (usuario_responsavel_id) REFERENCES usuario(id) ON DELETE SET NULL;

ALTER TABLE "contratacao"
  ADD CONSTRAINT "fk_contratacao_pessoa_assistida_id" FOREIGN KEY (pessoa_assistida_id) REFERENCES pessoa_assistida(id);

ALTER TABLE "contratacao"
  ADD CONSTRAINT "fk_contratacao_solicitacao_servico_id" FOREIGN KEY (solicitacao_servico_id) REFERENCES solicitacao_servico(id);

ALTER TABLE "contratacao"
  ADD CONSTRAINT "fk_contratacao_usuario_cuidador_id" FOREIGN KEY (usuario_cuidador_id) REFERENCES usuario(id);

ALTER TABLE "contratacao"
  ADD CONSTRAINT "fk_contratacao_usuario_responsavel_id" FOREIGN KEY (usuario_responsavel_id) REFERENCES usuario(id);

ALTER TABLE "contratacao"
  ADD CONSTRAINT "fk_contratacao_usuario_solicitante_cancelamento_id" FOREIGN KEY (usuario_solicitante_cancelamento_id) REFERENCES usuario(id);

ALTER TABLE "contratacao"
  ADD CONSTRAINT "fk_contratacao_usuario_solicitante_encerramento_id" FOREIGN KEY (usuario_solicitante_encerramento_id) REFERENCES usuario(id);

ALTER TABLE "cuidador"
  ADD CONSTRAINT "fk_cuidador_analisado_por" FOREIGN KEY (analisado_por_usuario_id) REFERENCES usuario(id);

ALTER TABLE "cuidador"
  ADD CONSTRAINT "fk_cuidador_usuario_id" FOREIGN KEY (usuario_id) REFERENCES usuario(id);

ALTER TABLE "cuidador_disponibilidade_dia"
  ADD CONSTRAINT "fk_cuidador_disponibilidade_dia_perfil_cuidador_id" FOREIGN KEY (perfil_cuidador_id) REFERENCES cuidador(id);

ALTER TABLE "cuidador_disponibilidade_periodo"
  ADD CONSTRAINT "fk_cuidador_disponibilidade_periodo_perfil_cuidador_id" FOREIGN KEY (perfil_cuidador_id) REFERENCES cuidador(id);

ALTER TABLE "cuidador_formacao"
  ADD CONSTRAINT "fk_cuidador_formacao_perfil_cuidador_id" FOREIGN KEY (perfil_cuidador_id) REFERENCES cuidador(id);

ALTER TABLE "cuidador_historico_situacao"
  ADD CONSTRAINT "fk_cuidador_historico_administrador" FOREIGN KEY (usuario_administrador_id) REFERENCES usuario(id);

ALTER TABLE "cuidador_historico_situacao"
  ADD CONSTRAINT "fk_cuidador_historico_cuidador" FOREIGN KEY (cuidador_id) REFERENCES cuidador(id);

ALTER TABLE "cuidador_modalidade"
  ADD CONSTRAINT "fk_cuidador_modalidade_perfil_cuidador_id" FOREIGN KEY (perfil_cuidador_id) REFERENCES cuidador(id);

ALTER TABLE "cuidador_servico"
  ADD CONSTRAINT "fk_cuidador_servico_perfil_cuidador_id" FOREIGN KEY (perfil_cuidador_id) REFERENCES cuidador(id);

ALTER TABLE "notificacao"
  ADD CONSTRAINT "fk_notificacao_usuario_destinatario_id" FOREIGN KEY (usuario_destinatario_id) REFERENCES usuario(id);

ALTER TABLE "notificacao_preferencia"
  ADD CONSTRAINT "fk_notificacao_preferencia_usuario_id" FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE;

ALTER TABLE "ocorrencia_cuidado"
  ADD CONSTRAINT "fk_ocorrencia_cuidado_contratacao_id" FOREIGN KEY (contratacao_id) REFERENCES contratacao(id);

ALTER TABLE "ocorrencia_cuidado"
  ADD CONSTRAINT "fk_ocorrencia_cuidado_pessoa_assistida_id" FOREIGN KEY (pessoa_assistida_id) REFERENCES pessoa_assistida(id);

ALTER TABLE "ocorrencia_cuidado"
  ADD CONSTRAINT "fk_ocorrencia_cuidado_tarefa_id" FOREIGN KEY (tarefa_id) REFERENCES tarefa_cuidado(id);

ALTER TABLE "ocorrencia_cuidado"
  ADD CONSTRAINT "fk_ocorrencia_cuidado_usuario_cuidador_id" FOREIGN KEY (usuario_cuidador_id) REFERENCES usuario(id);

ALTER TABLE "ocorrencia_cuidado"
  ADD CONSTRAINT "fk_ocorrencia_cuidado_usuario_executor_id" FOREIGN KEY (usuario_executor_id) REFERENCES usuario(id);

ALTER TABLE "ocorrencia_cuidado_foto"
  ADD CONSTRAINT "fk_ocorrencia_cuidado_foto_ocorrencia_id" FOREIGN KEY (ocorrencia_id) REFERENCES ocorrencia_cuidado(id) ON DELETE CASCADE;

ALTER TABLE "ocorrencia_cuidado_foto"
  ADD CONSTRAINT "fk_ocorrencia_cuidado_foto_registro_atividade_id" FOREIGN KEY (registro_atividade_id) REFERENCES registro_diario_cuidado(id) ON DELETE CASCADE;

ALTER TABLE "ocorrencia_cuidado_foto"
  ADD CONSTRAINT "fk_ocorrencia_cuidado_foto_usuario_envio_id" FOREIGN KEY (usuario_envio_id) REFERENCES usuario(id);

ALTER TABLE "ocorrencia_cuidado_lembrete"
  ADD CONSTRAINT "fk_ocorrencia_cuidado_lembrete_ocorrencia_id" FOREIGN KEY (ocorrencia_id) REFERENCES ocorrencia_cuidado(id);

ALTER TABLE "ocorrencia_cuidado_lembrete"
  ADD CONSTRAINT "fk_ocorrencia_cuidado_lembrete_usuario_destinatario_id" FOREIGN KEY (usuario_destinatario_id) REFERENCES usuario(id);

ALTER TABLE "pessoa_assistida"
  ADD CONSTRAINT "fk_pessoa_assistida_usuario_responsavel_id" FOREIGN KEY (usuario_responsavel_id) REFERENCES usuario(id);

ALTER TABLE "pessoa_assistida_alergia"
  ADD CONSTRAINT "fk_pessoa_assistida_alergia_pessoa_assistida_id" FOREIGN KEY (pessoa_assistida_id) REFERENCES pessoa_assistida(id);

ALTER TABLE "pessoa_assistida_contato_emergencia"
  ADD CONSTRAINT "fk_pessoa_assistida_contato_emergencia_pessoa_assistida_id" FOREIGN KEY (pessoa_assistida_id) REFERENCES pessoa_assistida(id);

ALTER TABLE "pessoa_assistida_restricao_alimentar"
  ADD CONSTRAINT "fk_pessoa_assistida_restricao_alimentar_pessoa_assistida_id" FOREIGN KEY (pessoa_assistida_id) REFERENCES pessoa_assistida(id);

ALTER TABLE "registro_atendimento"
  ADD CONSTRAINT "fk_registro_atendimento_contratacao_id" FOREIGN KEY (contratacao_id) REFERENCES contratacao(id) ON DELETE CASCADE;

ALTER TABLE "registro_atendimento"
  ADD CONSTRAINT "fk_registro_atendimento_cuidador_id" FOREIGN KEY (cuidador_id) REFERENCES usuario(id);

ALTER TABLE "registro_atendimento"
  ADD CONSTRAINT "fk_registro_atendimento_pessoa_assistida_id" FOREIGN KEY (pessoa_assistida_id) REFERENCES pessoa_assistida(id);

ALTER TABLE "registro_atendimento"
  ADD CONSTRAINT "fk_registro_atendimento_responsavel_id" FOREIGN KEY (responsavel_id) REFERENCES usuario(id);

ALTER TABLE "registro_diario_cuidado"
  ADD CONSTRAINT "fk_registro_diario_cuidado_contratacao_id" FOREIGN KEY (contratacao_id) REFERENCES contratacao(id);

ALTER TABLE "registro_diario_cuidado"
  ADD CONSTRAINT "fk_registro_diario_cuidado_ocorrencia_id" FOREIGN KEY (ocorrencia_id) REFERENCES ocorrencia_cuidado(id);

ALTER TABLE "registro_diario_cuidado"
  ADD CONSTRAINT "fk_registro_diario_cuidado_pessoa_assistida_id" FOREIGN KEY (pessoa_assistida_id) REFERENCES pessoa_assistida(id);

ALTER TABLE "registro_diario_cuidado"
  ADD CONSTRAINT "fk_registro_diario_cuidado_usuario_criacao_id" FOREIGN KEY (usuario_criacao_id) REFERENCES usuario(id);

ALTER TABLE "registro_diario_cuidado"
  ADD CONSTRAINT "fk_registro_diario_cuidado_usuario_cuidador_id" FOREIGN KEY (usuario_cuidador_id) REFERENCES usuario(id);

ALTER TABLE "registro_diario_cuidado"
  ADD CONSTRAINT "fk_registro_diario_cuidado_usuario_responsavel_id" FOREIGN KEY (usuario_responsavel_id) REFERENCES usuario(id);

ALTER TABLE "relatorio_atendimento"
  ADD CONSTRAINT "fk_relatorio_atendimento_contratacao_id" FOREIGN KEY (contratacao_id) REFERENCES contratacao(id) ON DELETE CASCADE;

ALTER TABLE "relatorio_atendimento"
  ADD CONSTRAINT "fk_relatorio_atendimento_cuidador_id" FOREIGN KEY (cuidador_id) REFERENCES usuario(id);

ALTER TABLE "relatorio_atendimento"
  ADD CONSTRAINT "fk_relatorio_atendimento_pessoa_assistida_id" FOREIGN KEY (pessoa_assistida_id) REFERENCES pessoa_assistida(id);

ALTER TABLE "relatorio_atendimento"
  ADD CONSTRAINT "fk_relatorio_atendimento_registro_fim_atendimento_id" FOREIGN KEY (registro_fim_atendimento_id) REFERENCES registro_atendimento(id);

ALTER TABLE "relatorio_atendimento"
  ADD CONSTRAINT "fk_relatorio_atendimento_registro_inicio_atendimento_id" FOREIGN KEY (registro_inicio_atendimento_id) REFERENCES registro_atendimento(id);

ALTER TABLE "relatorio_atendimento"
  ADD CONSTRAINT "fk_relatorio_atendimento_responsavel_id" FOREIGN KEY (responsavel_id) REFERENCES usuario(id);

ALTER TABLE "responsavel"
  ADD CONSTRAINT "fk_responsavel_analisado_por" FOREIGN KEY (analisado_por_usuario_id) REFERENCES usuario(id);

ALTER TABLE "responsavel"
  ADD CONSTRAINT "fk_responsavel_usuario_id" FOREIGN KEY (usuario_id) REFERENCES usuario(id);

ALTER TABLE "responsavel_historico_situacao"
  ADD CONSTRAINT "fk_responsavel_historico_administrador" FOREIGN KEY (usuario_administrador_id) REFERENCES usuario(id);

ALTER TABLE "responsavel_historico_situacao"
  ADD CONSTRAINT "fk_responsavel_historico_responsavel" FOREIGN KEY (responsavel_id) REFERENCES responsavel(id);

ALTER TABLE "rotina_cuidado"
  ADD CONSTRAINT "fk_rotina_cuidado_pessoa_assistida_id" FOREIGN KEY (pessoa_assistida_id) REFERENCES pessoa_assistida(id);

ALTER TABLE "rotina_cuidado"
  ADD CONSTRAINT "fk_rotina_cuidado_usuario_responsavel_id" FOREIGN KEY (usuario_responsavel_id) REFERENCES usuario(id);

ALTER TABLE "rotina_cuidado_item"
  ADD CONSTRAINT "fk_rotina_cuidado_item_rotina_cuidado_id" FOREIGN KEY (rotina_cuidado_id) REFERENCES rotina_cuidado(id) ON DELETE CASCADE;

ALTER TABLE "rotina_cuidado_item_dia_semana"
  ADD CONSTRAINT "fk_rotina_cuidado_item_dia_semana_item_rotina_cuidado_id" FOREIGN KEY (item_rotina_cuidado_id) REFERENCES rotina_cuidado_item(id) ON DELETE CASCADE;

ALTER TABLE "solicitacao_servico"
  ADD CONSTRAINT "fk_solicitacao_servico_oportunidade_origem_id" FOREIGN KEY (oportunidade_origem_id) REFERENCES solicitacao_servico(id);

ALTER TABLE "solicitacao_servico"
  ADD CONSTRAINT "fk_solicitacao_servico_pessoa_assistida_id" FOREIGN KEY (pessoa_assistida_id) REFERENCES pessoa_assistida(id);

ALTER TABLE "solicitacao_servico"
  ADD CONSTRAINT "fk_solicitacao_servico_rotina_cuidado_id" FOREIGN KEY (rotina_cuidado_id) REFERENCES rotina_cuidado(id);

ALTER TABLE "solicitacao_servico"
  ADD CONSTRAINT "fk_solicitacao_servico_usuario_cuidador_id" FOREIGN KEY (usuario_cuidador_id) REFERENCES usuario(id);

ALTER TABLE "solicitacao_servico"
  ADD CONSTRAINT "fk_solicitacao_servico_usuario_responsavel_id" FOREIGN KEY (usuario_responsavel_id) REFERENCES usuario(id);

ALTER TABLE "solicitacao_servico"
  ADD CONSTRAINT "fk_solicitacao_servico_usuario_solicitante_id" FOREIGN KEY (usuario_solicitante_id) REFERENCES usuario(id);

ALTER TABLE "solicitacao_servico_agenda_dia"
  ADD CONSTRAINT "fk_solicitacao_servico_agenda_dia_solicitacao_servico_id" FOREIGN KEY (solicitacao_servico_id) REFERENCES solicitacao_servico(id) ON DELETE CASCADE;

ALTER TABLE "solicitacao_servico_atividade"
  ADD CONSTRAINT "fk_solicitacao_servico_atividade_solicitacao_servico_id" FOREIGN KEY (solicitacao_servico_id) REFERENCES solicitacao_servico(id) ON DELETE CASCADE;

ALTER TABLE "solicitacao_servico_contratacao_historico_status"
  ADD CONSTRAINT "fk_solicitacao_servico_contratacao_historico_status_us_1818e2f4" FOREIGN KEY (usuario_alteracao_id) REFERENCES usuario(id);

ALTER TABLE "solicitacao_servico_data"
  ADD CONSTRAINT "fk_solicitacao_servico_data_solicitacao_servico_id" FOREIGN KEY (solicitacao_servico_id) REFERENCES solicitacao_servico(id) ON DELETE CASCADE;

ALTER TABLE "solicitacao_servico_item_cuidado_copia"
  ADD CONSTRAINT "fk_solicitacao_servico_item_cuidado_copia_item_rotina__c9c91f7b" FOREIGN KEY (item_rotina_cuidado_original_id) REFERENCES rotina_cuidado_item(id) ON DELETE SET NULL;

ALTER TABLE "solicitacao_servico_item_cuidado_copia"
  ADD CONSTRAINT "fk_solicitacao_servico_item_cuidado_copia_rotina_cuida_870340c6" FOREIGN KEY (rotina_cuidado_original_id) REFERENCES rotina_cuidado(id);

ALTER TABLE "solicitacao_servico_item_cuidado_copia"
  ADD CONSTRAINT "fk_solicitacao_servico_item_cuidado_copia_solicitacao__2fe54571" FOREIGN KEY (solicitacao_servico_id) REFERENCES solicitacao_servico(id) ON DELETE CASCADE;

ALTER TABLE "solicitacao_servico_item_cuidado_copia_dia_semana"
  ADD CONSTRAINT "fk_solicitacao_servico_item_cuidado_copia_dia_semana_i_78242eb8" FOREIGN KEY (item_copia_id) REFERENCES solicitacao_servico_item_cuidado_copia(id) ON DELETE CASCADE;

ALTER TABLE "tarefa_cuidado"
  ADD CONSTRAINT "fk_tarefa_cuidado_contratacao_id" FOREIGN KEY (contratacao_id) REFERENCES contratacao(id);

ALTER TABLE "tarefa_cuidado"
  ADD CONSTRAINT "fk_tarefa_cuidado_cuidador_executor_id" FOREIGN KEY (cuidador_executor_id) REFERENCES usuario(id);

ALTER TABLE "tarefa_cuidado"
  ADD CONSTRAINT "fk_tarefa_cuidado_item_copia_origem_id" FOREIGN KEY (item_copia_origem_id) REFERENCES solicitacao_servico_item_cuidado_copia(id);

ALTER TABLE "tarefa_cuidado"
  ADD CONSTRAINT "fk_tarefa_cuidado_pessoa_assistida_id" FOREIGN KEY (pessoa_assistida_id) REFERENCES pessoa_assistida(id);

ALTER TABLE "tarefa_cuidado"
  ADD CONSTRAINT "fk_tarefa_cuidado_responsavel_criador_id" FOREIGN KEY (responsavel_criador_id) REFERENCES usuario(id);

ALTER TABLE "tarefa_cuidado"
  ADD CONSTRAINT "fk_tarefa_cuidado_serie_anterior_id" FOREIGN KEY (serie_anterior_id) REFERENCES tarefa_cuidado(id);

ALTER TABLE "tarefa_cuidado"
  ADD CONSTRAINT "fk_tarefa_cuidado_tarefa_duplicada_de_id" FOREIGN KEY (tarefa_duplicada_de_id) REFERENCES tarefa_cuidado(id);

ALTER TABLE "tarefa_cuidado"
  ADD CONSTRAINT "fk_tarefa_cuidado_usuario_atualizacao_id" FOREIGN KEY (usuario_atualizacao_id) REFERENCES usuario(id);

ALTER TABLE "tarefa_cuidado"
  ADD CONSTRAINT "fk_tarefa_cuidado_usuario_criacao_id" FOREIGN KEY (usuario_criacao_id) REFERENCES usuario(id);

ALTER TABLE "tarefa_cuidado_auditoria"
  ADD CONSTRAINT "fk_tarefa_cuidado_auditoria_ocorrencia_id" FOREIGN KEY (ocorrencia_id) REFERENCES ocorrencia_cuidado(id);

ALTER TABLE "tarefa_cuidado_auditoria"
  ADD CONSTRAINT "fk_tarefa_cuidado_auditoria_tarefa_id" FOREIGN KEY (tarefa_id) REFERENCES tarefa_cuidado(id);

ALTER TABLE "tarefa_cuidado_auditoria"
  ADD CONSTRAINT "fk_tarefa_cuidado_auditoria_usuario_ator_id" FOREIGN KEY (usuario_ator_id) REFERENCES usuario(id);

ALTER TABLE "tarefa_cuidado_dia_semana"
  ADD CONSTRAINT "fk_tarefa_cuidado_dia_semana_tarefa_id" FOREIGN KEY (tarefa_id) REFERENCES tarefa_cuidado(id);

ALTER TABLE "usuario"
  ADD CONSTRAINT "fk_usuario_bloqueado_por" FOREIGN KEY (bloqueado_por_usuario_id) REFERENCES usuario(id);

ALTER TABLE "usuario"
  ADD CONSTRAINT "fk_usuario_desbloqueado_por" FOREIGN KEY (desbloqueado_por_usuario_id) REFERENCES usuario(id);

ALTER TABLE "usuario_confirmacao_exclusao"
  ADD CONSTRAINT "fk_usuario_confirmacao_exclusao_usuario" FOREIGN KEY (usuario_id) REFERENCES usuario(id);

ALTER TABLE "usuario_token_redefinicao_senha"
  ADD CONSTRAINT "fk_usuario_token_redefinicao_senha_usuario_id" FOREIGN KEY (usuario_id) REFERENCES usuario(id);

-- Índices secundários (índices de PK/UNIQUE são criados pelas próprias constraints)

CREATE INDEX idx_auditoria_acao_criado_em ON auditoria_acao_critica (criado_em DESC);
CREATE INDEX idx_auditoria_acao_entidade ON auditoria_acao_critica (entidade_afetada_tipo, entidade_afetada_id, criado_em DESC);
CREATE INDEX idx_auditoria_acao_tipo ON auditoria_acao_critica (tipo_acao, criado_em DESC);
CREATE INDEX idx_auditoria_acao_usuario ON auditoria_acao_critica (usuario_responsavel_id, criado_em DESC);
CREATE INDEX idx_contratacao_status_data_fim_efetiva ON contratacao (status, data_fim_efetiva) WHERE ((status) = 'ENCERRAMENTO_AGENDADO');
CREATE INDEX idx_contratacao_usuario_cuidador_id_status ON contratacao (usuario_cuidador_id, status);
CREATE INDEX idx_contratacao_usuario_responsavel_id_atualizado_em ON contratacao (usuario_responsavel_id, atualizado_em DESC);
CREATE INDEX idx_cuidador_situacao_aprovacao ON cuidador (situacao_aprovacao);
CREATE INDEX idx_cuidador_historico_cuidador_criado ON cuidador_historico_situacao (cuidador_id, criado_em DESC);
CREATE INDEX idx_notificacao_usuario_destinatario_id_removida_em_criado_em ON notificacao (usuario_destinatario_id, removida_em, criado_em DESC);
CREATE UNIQUE INDEX ux_notificacao_chave_deduplicacao ON notificacao (chave_deduplicacao) WHERE (chave_deduplicacao IS NOT NULL);
CREATE INDEX idx_notificacao_preferencia_usuario_id ON notificacao_preferencia (usuario_id);
CREATE INDEX idx_ocorrencia_cuidado_contratacao_id_data_prevista ON ocorrencia_cuidado (contratacao_id, data_prevista);
CREATE INDEX idx_ocorrencia_cuidado_pessoa_assistida_id_data_prevista ON ocorrencia_cuidado (pessoa_assistida_id, data_prevista);
CREATE INDEX idx_ocorrencia_cuidado_status_instante_previsto_utc ON ocorrencia_cuidado (status, instante_previsto_utc);
CREATE INDEX idx_ocorrencia_cuidado_tarefa_id_data_prevista ON ocorrencia_cuidado (tarefa_id, data_prevista);
CREATE INDEX idx_ocorrencia_cuidado_usuario_cuidador_id_data_prevista ON ocorrencia_cuidado (usuario_cuidador_id, data_prevista);
CREATE UNIQUE INDEX ux_ocorrencia_cuidado_contratacao_id_tarefa_id_data_pr_a365437c ON ocorrencia_cuidado (contratacao_id, tarefa_id, data_prevista, horario_previsto);
CREATE INDEX idx_ocorrencia_cuidado_foto_ocorrencia_id_criado_em ON ocorrencia_cuidado_foto (ocorrencia_id, criado_em);
CREATE INDEX idx_ocorrencia_cuidado_foto_registro_atividade_id_criado_em ON ocorrencia_cuidado_foto (registro_atividade_id, criado_em);
CREATE INDEX idx_ocorrencia_cuidado_lembrete_ocorrencia_id_status ON ocorrencia_cuidado_lembrete (ocorrencia_id, status);
CREATE INDEX idx_ocorrencia_cuidado_lembrete_status_previsto_em ON ocorrencia_cuidado_lembrete (status, previsto_em);
CREATE INDEX idx_registro_atendimento_contratacao_id_data_atendimen_4c3ec664 ON registro_atendimento (contratacao_id, data_atendimento, registrado_em);
CREATE INDEX idx_registro_atendimento_cuidador_id_data_atendimento ON registro_atendimento (cuidador_id, data_atendimento);
CREATE INDEX idx_registro_diario_cuidado_contratacao_id_data_regist_b64ac750 ON registro_diario_cuidado (contratacao_id, data_registro, ocorrido_em);
CREATE INDEX idx_registro_diario_cuidado_usuario_cuidador_id_data_r_e12a85a0 ON registro_diario_cuidado (usuario_cuidador_id, data_registro, ocorrido_em);
CREATE INDEX idx_registro_diario_cuidado_usuario_responsavel_id_dat_18645e03 ON registro_diario_cuidado (usuario_responsavel_id, data_registro, ocorrido_em);
CREATE INDEX idx_relatorio_atendimento_responsavel_id_cuidador_id_d_b3f71a9d ON relatorio_atendimento (responsavel_id, cuidador_id, data_atendimento);
CREATE INDEX idx_relatorio_atendimento_status_email_proxima_tentati_88c55cf7 ON relatorio_atendimento (status_email, proxima_tentativa_email_em, email_solicitado_em);
CREATE INDEX idx_responsavel_situacao_aprovacao ON responsavel (situacao_aprovacao);
CREATE INDEX idx_responsavel_historico_responsavel_criado ON responsavel_historico_situacao (responsavel_id, criado_em DESC);
CREATE INDEX idx_rotina_cuidado_pessoa_assistida_id_ativo ON rotina_cuidado (pessoa_assistida_id, ativo);
CREATE INDEX idx_rotina_cuidado_usuario_responsavel_id_atualizado_em ON rotina_cuidado (usuario_responsavel_id, atualizado_em DESC);
CREATE INDEX idx_rotina_cuidado_item_rotina_cuidado_id_ordem_exibicao ON rotina_cuidado_item (rotina_cuidado_id, ordem_exibicao);
CREATE INDEX idx_solicitacao_servico_oportunidade_origem_id_usuario_093350b0 ON solicitacao_servico (oportunidade_origem_id, usuario_cuidador_id);
CREATE INDEX idx_solicitacao_servico_status_iniciado_por_criado_em ON solicitacao_servico (status, iniciado_por, criado_em DESC);
CREATE INDEX idx_solicitacao_servico_usuario_responsavel_id_atualizado_em ON solicitacao_servico (usuario_responsavel_id, atualizado_em DESC);
CREATE INDEX idx_solicitacao_servico_usuario_responsavel_id_status ON solicitacao_servico (usuario_responsavel_id, status);
CREATE INDEX idx_solicitacao_servico_usuario_responsavel_id_usuario_5464ea4f ON solicitacao_servico (usuario_responsavel_id, usuario_cuidador_id, pessoa_assistida_id, status);
CREATE UNIQUE INDEX ux_solicitacao_servico_oportunidade_origem_id_usuario__1abc0dd7 ON solicitacao_servico (oportunidade_origem_id, usuario_cuidador_id) WHERE (oportunidade_origem_id IS NOT NULL);
CREATE INDEX idx_solicitacao_servico_contratacao_historico_status_t_f7eac776 ON solicitacao_servico_contratacao_historico_status (tipo_entidade, entidade_id, criado_em);
CREATE INDEX idx_solicitacao_servico_item_cuidado_copia_solicitacao_4519f23e ON solicitacao_servico_item_cuidado_copia (solicitacao_servico_id, ordem_exibicao);
CREATE INDEX idx_tarefa_cuidado_contratacao_id ON tarefa_cuidado (contratacao_id);
CREATE INDEX idx_tarefa_cuidado_cuidador_executor_id_status_atualizado_em ON tarefa_cuidado (cuidador_executor_id, status, atualizado_em DESC);
CREATE INDEX idx_tarefa_cuidado_pessoa_assistida_id ON tarefa_cuidado (pessoa_assistida_id);
CREATE INDEX idx_tarefa_cuidado_responsavel_criador_id_status_atualizado_em ON tarefa_cuidado (responsavel_criador_id, status, atualizado_em DESC);
CREATE INDEX idx_tarefa_cuidado_tarefa_duplicada_de_id ON tarefa_cuidado (tarefa_duplicada_de_id);
CREATE UNIQUE INDEX ux_tarefa_cuidado_item_copia_origem_id ON tarefa_cuidado (item_copia_origem_id) WHERE (item_copia_origem_id IS NOT NULL);
CREATE INDEX idx_tarefa_cuidado_auditoria_tarefa_id_criado_em ON tarefa_cuidado_auditoria (tarefa_id, criado_em DESC);
CREATE INDEX idx_usuario_situacao_conta ON usuario (situacao_conta);
CREATE INDEX idx_usuario_confirmacao_exclusao_usuario ON usuario_confirmacao_exclusao (usuario_id, criado_em DESC);
CREATE INDEX idx_usuario_exclusao_auditoria_referencia ON usuario_exclusao_auditoria (usuario_referencia, criado_em DESC);
