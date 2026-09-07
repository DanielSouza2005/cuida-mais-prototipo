-- Script de modelagem do banco Cuidar+
-- Gerado por introspecção direta do banco PostgreSQL.
-- Destinado à importação no DBDesigner.
-- Não contém dados reais.
-- Não contém usuários, senhas, hashes, tokens ou informações sensíveis.
-- A tabela flyway_schema_history foi omitida por ser infraestrutura do Flyway.
-- Esta versão preserva os tipos, defaults e constraints reais do PostgreSQL.

CREATE TABLE "auditoria_acao_critica" (
  "id" uuid NOT NULL,
  "tipo_acao" character varying(80) NOT NULL,
  "categoria" character varying(50) NOT NULL,
  "resultado" character varying(40) NOT NULL,
  "usuario_responsavel_id" uuid,
  "tipo_usuario_responsavel" character varying(40),
  "entidade_afetada_tipo" character varying(80),
  "entidade_afetada_id" uuid,
  "entidade_relacionada_tipo" character varying(80),
  "entidade_relacionada_id" uuid,
  "valor_anterior_resumido" jsonb,
  "valor_novo_resumido" jsonb,
  "motivo" character varying(500),
  "mensagem_resumida" character varying(500),
  "ip" character varying(80),
  "user_agent_resumido" character varying(255),
  "criado_em" timestamp with time zone DEFAULT now() NOT NULL,
  CONSTRAINT "auditoria_acao_critica_pkey" PRIMARY KEY (id),
  CONSTRAINT "chk_auditoria_acao_categoria" CHECK (categoria::text = ANY (ARRAY['SEGURANCA'::character varying, 'ADMINISTRATIVO'::character varying, 'PRIVACIDADE'::character varying, 'CONTRATACAO'::character varying, 'ASSISTENCIAL'::character varying, 'ATENDIMENTO'::character varying, 'RELATORIO'::character varying]::text[])),
  CONSTRAINT "chk_auditoria_acao_resultado" CHECK (resultado::text = ANY (ARRAY['SUCESSO'::character varying, 'FALHA'::character varying, 'BLOQUEADO_POR_REGRA'::character varying]::text[]))
);

CREATE TABLE "contratacao" (
  "id" uuid NOT NULL,
  "solicitacao_servico_id" uuid NOT NULL,
  "usuario_responsavel_id" uuid NOT NULL,
  "usuario_cuidador_id" uuid NOT NULL,
  "pessoa_assistida_id" uuid NOT NULL,
  "status" character varying(30) NOT NULL,
  "data_inicio" date NOT NULL,
  "data_fim" date,
  "criado_em" timestamp with time zone NOT NULL,
  "atualizado_em" timestamp with time zone NOT NULL,
  "motivo_cancelamento" character varying(1000),
  "motivo_encerramento" character varying(1000),
  "tipo_encerramento" character varying(50),
  "motivo_solicitacao_encerramento" character varying(1000),
  "observacoes_encerramento" character varying(1000),
  "usuario_solicitante_encerramento_id" uuid,
  "encerramento_solicitado_em" timestamp with time zone,
  "data_fim_efetiva" date,
  "cancelado_em" timestamp with time zone,
  "usuario_solicitante_cancelamento_id" uuid,
  "cancelamento_solicitado_em" timestamp with time zone,
  CONSTRAINT "pk_contratacao" PRIMARY KEY (id),
  CONSTRAINT "uk_contratacao_solicitacao_servico_id" UNIQUE (solicitacao_servico_id)
);

CREATE TABLE "cuidador" (
  "id" uuid NOT NULL,
  "usuario_id" uuid NOT NULL,
  "formacao_outro" character varying(180),
  "experiencia" character varying(500),
  "biografia" character varying(500),
  "cep" character varying(9),
  "rua" character varying(180),
  "numero" character varying(30),
  "complemento" character varying(120),
  "bairro" character varying(120),
  "cidade" character varying(120),
  "estado" character varying(2),
  "ponto_referencia" character varying(180),
  "horario_inicio" time without time zone,
  "horario_fim" time without time zone,
  "observacao" character varying(500),
  "modalidade_outro" character varying(180),
  "servico_outro" character varying(180),
  "criado_em" timestamp with time zone NOT NULL,
  "atualizado_em" timestamp with time zone NOT NULL,
  "tempo_experiencia" character varying(30),
  "latitude" numeric(10,7),
  "longitude" numeric(10,7),
  "situacao_aprovacao" character varying(30) DEFAULT 'PENDENTE'::character varying NOT NULL,
  "analisado_em" timestamp with time zone,
  "analisado_por_usuario_id" uuid,
  "motivo_reprovacao" character varying(1000),
  "motivo_bloqueio_profissional" character varying(1000),
  CONSTRAINT "pk_cuidador" PRIMARY KEY (id),
  CONSTRAINT "uk_cuidador_usuario_id" UNIQUE (usuario_id),
  CONSTRAINT "chk_cuidador_situacao_aprovacao" CHECK (situacao_aprovacao::text = ANY (ARRAY['PENDENTE'::character varying, 'APROVADO'::character varying, 'REPROVADO'::character varying, 'BLOQUEADO'::character varying]::text[]))
);

CREATE TABLE "cuidador_disponibilidade_dia" (
  "perfil_cuidador_id" uuid NOT NULL,
  "dia_semana" character varying(20) NOT NULL,
  CONSTRAINT "pk_cuidador_disponibilidade_dia" PRIMARY KEY (perfil_cuidador_id, dia_semana)
);

CREATE TABLE "cuidador_disponibilidade_periodo" (
  "perfil_cuidador_id" uuid NOT NULL,
  "periodo" character varying(30) NOT NULL,
  CONSTRAINT "pk_cuidador_disponibilidade_periodo" PRIMARY KEY (perfil_cuidador_id, periodo)
);

CREATE TABLE "cuidador_formacao" (
  "perfil_cuidador_id" uuid NOT NULL,
  "formacao" character varying(40) NOT NULL,
  CONSTRAINT "pk_cuidador_formacao" PRIMARY KEY (perfil_cuidador_id, formacao)
);

CREATE TABLE "cuidador_historico_situacao" (
  "id" uuid NOT NULL,
  "cuidador_id" uuid NOT NULL,
  "situacao_anterior" character varying(30),
  "situacao_nova" character varying(30) NOT NULL,
  "motivo" character varying(1000),
  "usuario_administrador_id" uuid NOT NULL,
  "criado_em" timestamp with time zone NOT NULL,
  CONSTRAINT "cuidador_historico_situacao_pkey" PRIMARY KEY (id),
  CONSTRAINT "chk_cuidador_historico_situacao_nova" CHECK (situacao_nova::text = ANY (ARRAY['PENDENTE'::character varying, 'APROVADO'::character varying, 'REPROVADO'::character varying, 'BLOQUEADO'::character varying]::text[]))
);

CREATE TABLE "cuidador_modalidade" (
  "perfil_cuidador_id" uuid NOT NULL,
  "modalidade" character varying(40) NOT NULL,
  CONSTRAINT "pk_cuidador_modalidade" PRIMARY KEY (perfil_cuidador_id, modalidade)
);

CREATE TABLE "cuidador_servico" (
  "perfil_cuidador_id" uuid NOT NULL,
  "servico" character varying(50) NOT NULL,
  CONSTRAINT "pk_cuidador_servico" PRIMARY KEY (perfil_cuidador_id, servico)
);

CREATE TABLE "notificacao" (
  "id" uuid NOT NULL,
  "usuario_destinatario_id" uuid NOT NULL,
  "tipo" character varying(50) NOT NULL,
  "titulo" character varying(180) NOT NULL,
  "mensagem" character varying(500) NOT NULL,
  "tipo_entidade_relacionada" character varying(40) NOT NULL,
  "entidade_relacionada_id" uuid NOT NULL,
  "lida_em" timestamp with time zone,
  "removida_em" timestamp with time zone,
  "criado_em" timestamp with time zone NOT NULL,
  "chave_deduplicacao" character varying(220),
  CONSTRAINT "pk_notificacao" PRIMARY KEY (id)
);

CREATE TABLE "notificacao_preferencia" (
  "id" uuid NOT NULL,
  "usuario_id" uuid NOT NULL,
  "tipo_notificacao" character varying(64) NOT NULL,
  "habilitado" boolean NOT NULL,
  "criado_em" timestamp with time zone NOT NULL,
  "atualizado_em" timestamp with time zone NOT NULL,
  CONSTRAINT "pk_notificacao_preferencia" PRIMARY KEY (id),
  CONSTRAINT "uk_notificacao_preferencia_usuario_id_tipo_notificacao" UNIQUE (usuario_id, tipo_notificacao)
);

CREATE TABLE "ocorrencia_cuidado" (
  "id" uuid NOT NULL,
  "tarefa_id" uuid NOT NULL,
  "contratacao_id" uuid NOT NULL,
  "pessoa_assistida_id" uuid NOT NULL,
  "usuario_cuidador_id" uuid NOT NULL,
  "data_prevista" date NOT NULL,
  "horario_previsto" time without time zone NOT NULL,
  "instante_previsto_utc" timestamp with time zone NOT NULL,
  "fuso_horario" character varying(80) NOT NULL,
  "status" character varying(25) NOT NULL,
  "concluido_em" timestamp with time zone,
  "usuario_executor_id" uuid,
  "motivo_nao_realizacao" character varying(1000),
  "anotacao_execucao" character varying(1000),
  "cancelado_em" timestamp with time zone,
  "excecao" boolean DEFAULT false NOT NULL,
  "criado_em" timestamp with time zone NOT NULL,
  "atualizado_em" timestamp with time zone NOT NULL,
  "versao" bigint DEFAULT 0 NOT NULL,
  "marcada_nao_realizada_automaticamente" boolean DEFAULT false NOT NULL,
  "status_atualizado_em" timestamp with time zone,
  CONSTRAINT "pk_ocorrencia_cuidado" PRIMARY KEY (id),
  CONSTRAINT "uk_ocorrencia_cuidado_tarefa_id_data_prevista_horario_previsto" UNIQUE (tarefa_id, data_prevista, horario_previsto)
);

CREATE TABLE "ocorrencia_cuidado_foto" (
  "id" uuid NOT NULL,
  "ocorrencia_id" uuid,
  "usuario_envio_id" uuid NOT NULL,
  "nome_arquivo" character varying(80) NOT NULL,
  "nome_arquivo_original" character varying(255),
  "tipo_conteudo" character varying(30) NOT NULL,
  "tamanho_arquivo" bigint NOT NULL,
  "criado_em" timestamp with time zone NOT NULL,
  "registro_atividade_id" uuid,
  CONSTRAINT "pk_ocorrencia_cuidado_foto" PRIMARY KEY (id),
  CONSTRAINT "uk_ocorrencia_cuidado_foto_nome_arquivo" UNIQUE (nome_arquivo),
  CONSTRAINT "ck_ocorrencia_cuidado_foto_vinculo_unico" CHECK (ocorrencia_id IS NOT NULL AND registro_atividade_id IS NULL OR ocorrencia_id IS NULL AND registro_atividade_id IS NOT NULL)
);

CREATE TABLE "ocorrencia_cuidado_lembrete" (
  "id" uuid NOT NULL,
  "ocorrencia_id" uuid NOT NULL,
  "usuario_destinatario_id" uuid NOT NULL,
  "tipo_lembrete" character varying(40) NOT NULL,
  "previsto_em" timestamp with time zone NOT NULL,
  "enviado_em" timestamp with time zone,
  "cancelado_em" timestamp with time zone,
  "status" character varying(20) NOT NULL,
  "chave_deduplicacao" character varying(220) NOT NULL,
  "criado_em" timestamp with time zone NOT NULL,
  "atualizado_em" timestamp with time zone NOT NULL,
  CONSTRAINT "pk_ocorrencia_cuidado_lembrete" PRIMARY KEY (id),
  CONSTRAINT "uk_ocorrencia_cuidado_lembrete_chave_deduplicacao" UNIQUE (chave_deduplicacao)
);

CREATE TABLE "pessoa_assistida" (
  "id" uuid NOT NULL,
  "usuario_responsavel_id" uuid NOT NULL,
  "nome" character varying(140) NOT NULL,
  "data_nascimento" date NOT NULL,
  "grau_dependencia" character varying(30) NOT NULL,
  "mobilidade" character varying(30) NOT NULL,
  "mobilidade_outro" character varying(120),
  "alergias_outro" character varying(180),
  "alergias_detalhes" character varying(500),
  "restricoes_alimentares_outro" character varying(180),
  "restricoes_alimentares_detalhes" character varying(500),
  "medicamentos" character varying(500),
  "observacoes" character varying(500),
  "cep" character varying(9),
  "rua" character varying(180),
  "numero" character varying(30),
  "complemento" character varying(120),
  "bairro" character varying(120),
  "cidade" character varying(120),
  "estado" character varying(2),
  "ponto_referencia" character varying(180),
  "criado_em" timestamp with time zone NOT NULL,
  "atualizado_em" timestamp with time zone NOT NULL,
  "latitude" numeric(10,7),
  "longitude" numeric(10,7),
  CONSTRAINT "pk_pessoa_assistida" PRIMARY KEY (id)
);

CREATE TABLE "pessoa_assistida_alergia" (
  "pessoa_assistida_id" uuid NOT NULL,
  "alergia" character varying(40) NOT NULL,
  CONSTRAINT "pk_pessoa_assistida_alergia" PRIMARY KEY (pessoa_assistida_id, alergia)
);

CREATE TABLE "pessoa_assistida_contato_emergencia" (
  "id" uuid NOT NULL,
  "pessoa_assistida_id" uuid NOT NULL,
  "nome" character varying(140),
  "telefone" character varying(20),
  "vinculo" character varying(120) NOT NULL,
  "contato_responsavel" boolean DEFAULT false NOT NULL,
  "criado_em" timestamp with time zone NOT NULL,
  "atualizado_em" timestamp with time zone NOT NULL,
  CONSTRAINT "pk_pessoa_assistida_contato_emergencia" PRIMARY KEY (id),
  CONSTRAINT "uk_pessoa_assistida_contato_emergencia_pessoa_assistida_id" UNIQUE (pessoa_assistida_id)
);

CREATE TABLE "pessoa_assistida_restricao_alimentar" (
  "pessoa_assistida_id" uuid NOT NULL,
  "restricao" character varying(40) NOT NULL,
  CONSTRAINT "pk_pessoa_assistida_restricao_alimentar" PRIMARY KEY (pessoa_assistida_id, restricao)
);

CREATE TABLE "registro_atendimento" (
  "id" uuid NOT NULL,
  "contratacao_id" uuid NOT NULL,
  "cuidador_id" uuid NOT NULL,
  "responsavel_id" uuid NOT NULL,
  "pessoa_assistida_id" uuid NOT NULL,
  "data_atendimento" date NOT NULL,
  "tipo_registro" character varying(10) NOT NULL,
  "registrado_em" timestamp with time zone NOT NULL,
  "latitude" double precision NOT NULL,
  "longitude" double precision NOT NULL,
  "precisao" double precision NOT NULL,
  "localizacao_capturada_em" timestamp with time zone NOT NULL,
  "endereco_registrado" character varying(500),
  "fuso_dispositivo" character varying(80) NOT NULL,
  "horario_inicio_previsto" time without time zone NOT NULL,
  "horario_fim_previsto" time without time zone NOT NULL,
  "janela_permitida_inicio" timestamp with time zone NOT NULL,
  "janela_permitida_fim" timestamp with time zone NOT NULL,
  "dentro_janela_permitida" boolean NOT NULL,
  "criado_em" timestamp with time zone NOT NULL,
  "atualizado_em" timestamp with time zone NOT NULL,
  CONSTRAINT "pk_registro_atendimento" PRIMARY KEY (id),
  CONSTRAINT "uk_registro_atendimento_contratacao_id_data_atendiment_fc5cf1c5" UNIQUE (contratacao_id, data_atendimento, tipo_registro),
  CONSTRAINT "ck_registro_atendimento_latitude" CHECK (latitude >= '-90'::integer::double precision AND latitude <= 90::double precision),
  CONSTRAINT "ck_registro_atendimento_longitude" CHECK (longitude >= '-180'::integer::double precision AND longitude <= 180::double precision),
  CONSTRAINT "ck_registro_atendimento_precisao" CHECK (precisao >= 0::double precision AND precisao <= 1000::double precision),
  CONSTRAINT "ck_registro_atendimento_tipo" CHECK (tipo_registro::text = ANY (ARRAY['START'::character varying, 'END'::character varying]::text[]))
);

CREATE TABLE "registro_diario_cuidado" (
  "id" uuid NOT NULL,
  "ocorrencia_id" uuid,
  "contratacao_id" uuid NOT NULL,
  "pessoa_assistida_id" uuid NOT NULL,
  "usuario_responsavel_id" uuid NOT NULL,
  "usuario_cuidador_id" uuid NOT NULL,
  "tipo_atividade" character varying(40) NOT NULL,
  "titulo" character varying(180) NOT NULL,
  "anotacoes" character varying(1000),
  "ocorrido_em" timestamp with time zone NOT NULL,
  "criado_em" timestamp with time zone NOT NULL,
  "tipo_origem" character varying(20) DEFAULT 'PLANNED'::character varying NOT NULL,
  "data_registro" date NOT NULL,
  "fuso_horario" character varying(80) NOT NULL,
  "tipo_cuidado" character varying(40) NOT NULL,
  "descricao" character varying(2000),
  "importante" boolean DEFAULT false NOT NULL,
  "usuario_criacao_id" uuid NOT NULL,
  CONSTRAINT "pk_registro_diario_cuidado" PRIMARY KEY (id),
  CONSTRAINT "uk_registro_diario_cuidado_ocorrencia_id" UNIQUE (ocorrencia_id)
);

CREATE TABLE "relatorio_atendimento" (
  "id" uuid NOT NULL,
  "contratacao_id" uuid NOT NULL,
  "data_atendimento" date NOT NULL,
  "registro_inicio_atendimento_id" uuid NOT NULL,
  "registro_fim_atendimento_id" uuid NOT NULL,
  "cuidador_id" uuid NOT NULL,
  "responsavel_id" uuid NOT NULL,
  "pessoa_assistida_id" uuid NOT NULL,
  "texto_gerado" text NOT NULL,
  "texto_editado" text,
  "texto_final" text,
  "observacoes_adicionais" character varying(4000),
  "anotacoes_enfermagem" text NOT NULL,
  "status" character varying(20) NOT NULL,
  "status_email" character varying(20) DEFAULT 'NOT_SENT'::character varying NOT NULL,
  "email_enviado_em" timestamp with time zone,
  "mensagem_erro_email" character varying(500),
  "gerado_em" timestamp with time zone NOT NULL,
  "editado_em" timestamp with time zone,
  "finalizado_em" timestamp with time zone,
  "criado_em" timestamp with time zone NOT NULL,
  "atualizado_em" timestamp with time zone NOT NULL,
  "email_solicitado_em" timestamp with time zone,
  "tentativas_email" integer DEFAULT 0 NOT NULL,
  "proxima_tentativa_email_em" timestamp with time zone,
  CONSTRAINT "pk_relatorio_atendimento" PRIMARY KEY (id),
  CONSTRAINT "uk_relatorio_atendimento_contratacao_id_data_atendimento" UNIQUE (contratacao_id, data_atendimento),
  CONSTRAINT "ck_relatorio_atendimento_status" CHECK (status::text = ANY (ARRAY['DRAFT'::character varying, 'FINALIZED'::character varying]::text[])),
  CONSTRAINT "ck_relatorio_atendimento_status_email" CHECK (status_email::text = ANY (ARRAY['NOT_SENT'::character varying, 'PENDING'::character varying, 'SENT'::character varying, 'FAILED'::character varying]::text[]))
);

CREATE TABLE "responsavel" (
  "id" uuid NOT NULL,
  "usuario_id" uuid NOT NULL,
  "parentesco" character varying(40) NOT NULL,
  "parentesco_outro" character varying(120),
  "preferencia_contato" character varying(30),
  "criado_em" timestamp with time zone NOT NULL,
  "atualizado_em" timestamp with time zone NOT NULL,
  "situacao_aprovacao" character varying(30) DEFAULT 'PENDENTE'::character varying NOT NULL,
  "analisado_em" timestamp with time zone,
  "analisado_por_usuario_id" uuid,
  "motivo_reprovacao" character varying(1000),
  "motivo_bloqueio" character varying(1000),
  CONSTRAINT "pk_responsavel" PRIMARY KEY (id),
  CONSTRAINT "uk_responsavel_usuario_id" UNIQUE (usuario_id),
  CONSTRAINT "chk_responsavel_situacao_aprovacao" CHECK (situacao_aprovacao::text = ANY (ARRAY['PENDENTE'::character varying, 'APROVADO'::character varying, 'REPROVADO'::character varying, 'BLOQUEADO'::character varying]::text[]))
);

CREATE TABLE "responsavel_historico_situacao" (
  "id" uuid NOT NULL,
  "responsavel_id" uuid NOT NULL,
  "situacao_anterior" character varying(30),
  "situacao_nova" character varying(30) NOT NULL,
  "motivo" character varying(1000),
  "usuario_administrador_id" uuid NOT NULL,
  "criado_em" timestamp with time zone NOT NULL,
  CONSTRAINT "responsavel_historico_situacao_pkey" PRIMARY KEY (id),
  CONSTRAINT "chk_responsavel_historico_situacao_nova" CHECK (situacao_nova::text = ANY (ARRAY['PENDENTE'::character varying, 'APROVADO'::character varying, 'REPROVADO'::character varying, 'BLOQUEADO'::character varying]::text[]))
);

CREATE TABLE "rotina_cuidado" (
  "id" uuid NOT NULL,
  "usuario_responsavel_id" uuid NOT NULL,
  "pessoa_assistida_id" uuid,
  "nome" character varying(140) NOT NULL,
  "descricao" character varying(1000),
  "ativo" boolean DEFAULT true NOT NULL,
  "criado_em" timestamp with time zone NOT NULL,
  "atualizado_em" timestamp with time zone NOT NULL,
  CONSTRAINT "pk_rotina_cuidado" PRIMARY KEY (id)
);

CREATE TABLE "rotina_cuidado_item" (
  "id" uuid NOT NULL,
  "rotina_cuidado_id" uuid NOT NULL,
  "titulo" character varying(140) NOT NULL,
  "descricao" character varying(1000),
  "ordem_exibicao" integer NOT NULL,
  "ativo" boolean DEFAULT true NOT NULL,
  "criado_em" timestamp with time zone NOT NULL,
  "atualizado_em" timestamp with time zone NOT NULL,
  "categoria" character varying(40),
  "categoria_personalizada" character varying(120),
  "prioridade" character varying(20),
  "tipo_recorrencia" character varying(40),
  "horario_previsto" time without time zone,
  "intervalo_dias" integer,
  "lembrete_habilitado" boolean,
  "minutos_antecedencia_lembrete" integer,
  "anotacoes" character varying(2000),
  "nome_medicamento" character varying(180),
  "dosagem_medicamento" character varying(80),
  "unidade_medicamento" character varying(30),
  "unidade_personalizada_medicamento" character varying(80),
  "via_administracao_medicamento" character varying(30),
  "via_personalizada_medicamento" character varying(120),
  "instrucoes_medicamento" character varying(1000),
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
  "item_rotina_cuidado_id" uuid NOT NULL,
  "dia_semana" character varying(20) NOT NULL,
  CONSTRAINT "pk_rotina_cuidado_item_dia_semana" PRIMARY KEY (item_rotina_cuidado_id, dia_semana)
);

CREATE TABLE "solicitacao_servico" (
  "id" uuid NOT NULL,
  "usuario_responsavel_id" uuid NOT NULL,
  "usuario_cuidador_id" uuid,
  "pessoa_assistida_id" uuid NOT NULL,
  "tipo_contratacao" character varying(40) NOT NULL,
  "status" character varying(30) NOT NULL,
  "data_inicio" date,
  "data_fim" date,
  "descricao_necessidades" character varying(2000) NOT NULL,
  "outra_atividade" character varying(500),
  "observacoes_adicionais" character varying(2000),
  "observacoes_negociacao" character varying(1000),
  "criado_em" timestamp with time zone NOT NULL,
  "atualizado_em" timestamp with time zone NOT NULL,
  "expira_em" timestamp with time zone NOT NULL,
  "cancelado_em" timestamp with time zone,
  "motivo_rejeicao" character varying(1000),
  "motivo_cancelamento" character varying(1000),
  "rotina_cuidado_id" uuid,
  "nome_rotina_copia" character varying(140),
  "iniciado_por" character varying(20) DEFAULT 'RESPONSIBLE'::character varying NOT NULL,
  "usuario_solicitante_id" uuid NOT NULL,
  "oportunidade_origem_id" uuid,
  CONSTRAINT "pk_solicitacao_servico" PRIMARY KEY (id)
);

CREATE TABLE "solicitacao_servico_agenda_dia" (
  "solicitacao_servico_id" uuid NOT NULL,
  "dia_semana" character varying(20) NOT NULL,
  "horario_inicio" time without time zone NOT NULL,
  "horario_fim" time without time zone NOT NULL,
  CONSTRAINT "pk_solicitacao_servico_agenda_dia" PRIMARY KEY (solicitacao_servico_id, dia_semana)
);

CREATE TABLE "solicitacao_servico_atividade" (
  "solicitacao_servico_id" uuid NOT NULL,
  "atividade" character varying(50) NOT NULL,
  CONSTRAINT "pk_solicitacao_servico_atividade" PRIMARY KEY (solicitacao_servico_id, atividade)
);

CREATE TABLE "solicitacao_servico_contratacao_historico_status" (
  "id" uuid NOT NULL,
  "tipo_entidade" character varying(40) NOT NULL,
  "entidade_id" uuid NOT NULL,
  "status_anterior" character varying(30),
  "novo_status" character varying(30) NOT NULL,
  "usuario_alteracao_id" uuid,
  "motivo" character varying(1000),
  "criado_em" timestamp with time zone NOT NULL,
  CONSTRAINT "pk_solicitacao_servico_contratacao_historico_status" PRIMARY KEY (id)
);

CREATE TABLE "solicitacao_servico_data" (
  "solicitacao_servico_id" uuid NOT NULL,
  "data_servico" date NOT NULL,
  CONSTRAINT "pk_solicitacao_servico_data" PRIMARY KEY (solicitacao_servico_id, data_servico)
);

CREATE TABLE "solicitacao_servico_item_cuidado_copia" (
  "id" uuid NOT NULL,
  "solicitacao_servico_id" uuid NOT NULL,
  "rotina_cuidado_original_id" uuid NOT NULL,
  "item_rotina_cuidado_original_id" uuid,
  "titulo" character varying(140) NOT NULL,
  "descricao" character varying(1000),
  "ordem_exibicao" integer NOT NULL,
  "criado_em" timestamp with time zone NOT NULL,
  "categoria" character varying(40),
  "categoria_personalizada" character varying(120),
  "prioridade" character varying(20),
  "tipo_recorrencia" character varying(40),
  "horario_previsto" time without time zone,
  "intervalo_dias" integer,
  "lembrete_habilitado" boolean,
  "minutos_antecedencia_lembrete" integer,
  "anotacoes" character varying(2000),
  "nome_medicamento" character varying(180),
  "dosagem_medicamento" character varying(80),
  "unidade_medicamento" character varying(30),
  "unidade_personalizada_medicamento" character varying(80),
  "via_administracao_medicamento" character varying(30),
  "via_personalizada_medicamento" character varying(120),
  "instrucoes_medicamento" character varying(1000),
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
  "item_copia_id" uuid NOT NULL,
  "dia_semana" character varying(20) NOT NULL,
  CONSTRAINT "pk_solicitacao_servico_item_cuidado_copia_dia_semana" PRIMARY KEY (item_copia_id, dia_semana)
);

CREATE TABLE "tarefa_cuidado" (
  "id" uuid NOT NULL,
  "titulo" character varying(140) NOT NULL,
  "descricao" character varying(2000),
  "categoria" character varying(40) NOT NULL,
  "categoria_personalizada" character varying(120),
  "prioridade" character varying(20) NOT NULL,
  "tipo_recorrencia" character varying(40) NOT NULL,
  "data_inicio" date NOT NULL,
  "data_fim" date,
  "horario_previsto" time without time zone NOT NULL,
  "intervalo_dias" integer,
  "fuso_horario" character varying(80) NOT NULL,
  "lembrete_habilitado" boolean DEFAULT false NOT NULL,
  "minutos_antecedencia_lembrete" integer,
  "anotacoes" character varying(2000),
  "status" character varying(20) NOT NULL,
  "pessoa_assistida_id" uuid NOT NULL,
  "contratacao_id" uuid NOT NULL,
  "responsavel_criador_id" uuid NOT NULL,
  "cuidador_executor_id" uuid NOT NULL,
  "serie_anterior_id" uuid,
  "nome_medicamento" character varying(180),
  "dosagem_medicamento" character varying(80),
  "unidade_medicamento" character varying(30),
  "unidade_personalizada_medicamento" character varying(80),
  "via_administracao_medicamento" character varying(30),
  "via_personalizada_medicamento" character varying(120),
  "instrucoes_medicamento" character varying(1000),
  "criado_em" timestamp with time zone NOT NULL,
  "atualizado_em" timestamp with time zone NOT NULL,
  "usuario_criacao_id" uuid NOT NULL,
  "usuario_atualizacao_id" uuid NOT NULL,
  "versao" bigint DEFAULT 0 NOT NULL,
  "item_copia_origem_id" uuid,
  "lembrar_no_horario_previsto" boolean DEFAULT true NOT NULL,
  "lembrete_atraso_habilitado" boolean DEFAULT false NOT NULL,
  "minutos_para_atraso" integer,
  "repetir_enquanto_pendente" boolean DEFAULT false NOT NULL,
  "intervalo_repeticao_minutos" integer,
  "importante" boolean DEFAULT false NOT NULL,
  "notificar_responsavel_se_importante" boolean DEFAULT false NOT NULL,
  "exige_foto_conclusao" boolean DEFAULT false NOT NULL,
  "tarefa_duplicada_de_id" uuid,
  CONSTRAINT "pk_tarefa_cuidado" PRIMARY KEY (id),
  CONSTRAINT "ck_tarefa_cuidado_datas" CHECK (data_fim IS NULL OR data_fim >= data_inicio),
  CONSTRAINT "ck_tarefa_cuidado_intervalo" CHECK (intervalo_dias IS NULL OR intervalo_dias > 0),
  CONSTRAINT "ck_tarefa_cuidado_lembrete" CHECK (NOT lembrete_habilitado OR minutos_antecedencia_lembrete IS NOT NULL AND minutos_antecedencia_lembrete >= 0)
);

CREATE TABLE "tarefa_cuidado_auditoria" (
  "id" uuid NOT NULL,
  "tarefa_id" uuid NOT NULL,
  "ocorrencia_id" uuid,
  "usuario_ator_id" uuid,
  "acao" character varying(40) NOT NULL,
  "detalhes" character varying(500),
  "criado_em" timestamp with time zone NOT NULL,
  CONSTRAINT "pk_tarefa_cuidado_auditoria" PRIMARY KEY (id)
);

CREATE TABLE "tarefa_cuidado_dia_semana" (
  "tarefa_id" uuid NOT NULL,
  "dia_semana" character varying(20) NOT NULL,
  CONSTRAINT "pk_tarefa_cuidado_dia_semana" PRIMARY KEY (tarefa_id, dia_semana)
);

CREATE TABLE "usuario" (
  "id" uuid NOT NULL,
  "criado_em" timestamp(6) with time zone NOT NULL,
  "email" character varying(180) NOT NULL,
  "nome_completo" character varying(140) NOT NULL,
  "senha_hash" character varying(255) NOT NULL,
  "atualizado_em" timestamp(6) with time zone NOT NULL,
  "tipo_usuario" character varying(20) NOT NULL,
  "telefone" character varying(20),
  "situacao_conta" character varying(30) DEFAULT 'ACTIVE'::character varying NOT NULL,
  "url_foto_perfil" character varying(500),
  "motivo_bloqueio" character varying(1000),
  "bloqueado_em" timestamp with time zone,
  "bloqueado_por_usuario_id" uuid,
  "desbloqueado_em" timestamp with time zone,
  "desbloqueado_por_usuario_id" uuid,
  "ultimo_login_em" timestamp with time zone,
  "exclusao_solicitada_em" timestamp with time zone,
  "excluido_em" timestamp with time zone,
  "dados_anonimizados_em" timestamp with time zone,
  "maior_de_idade_confirmado" boolean NOT NULL,
  CONSTRAINT "pk_usuario" PRIMARY KEY (id),
  CONSTRAINT "uk_usuario_email" UNIQUE (email),
  CONSTRAINT "chk_usuario_situacao_conta" CHECK (situacao_conta::text = ANY (ARRAY['ATIVO'::character varying, 'BLOQUEADO'::character varying, 'INATIVO'::character varying, 'EXCLUIDO'::character varying]::text[])),
  CONSTRAINT "ck_usuario_tipo_usuario" CHECK (tipo_usuario::text = ANY (ARRAY['RESPONSAVEL'::character varying, 'CUIDADOR'::character varying, 'ADMIN'::character varying, 'FAMILY'::character varying, 'CAREGIVER'::character varying]::text[]))
);

CREATE TABLE "usuario_confirmacao_exclusao" (
  "id" uuid NOT NULL,
  "usuario_id" uuid NOT NULL,
  "hash_token" character varying(64) NOT NULL,
  "expira_em" timestamp with time zone NOT NULL,
  "usado_em" timestamp with time zone,
  "criado_em" timestamp with time zone NOT NULL,
  CONSTRAINT "usuario_confirmacao_exclusao_pkey" PRIMARY KEY (id),
  CONSTRAINT "uk_usuario_confirmacao_exclusao_hash" UNIQUE (hash_token)
);

CREATE TABLE "usuario_exclusao_auditoria" (
  "id" uuid NOT NULL,
  "usuario_referencia" uuid NOT NULL,
  "perfil" character varying(30) NOT NULL,
  "solicitado_em" timestamp with time zone NOT NULL,
  "concluido_em" timestamp with time zone NOT NULL,
  "resultado" character varying(30) NOT NULL,
  "criado_em" timestamp with time zone NOT NULL,
  CONSTRAINT "usuario_exclusao_auditoria_pkey" PRIMARY KEY (id),
  CONSTRAINT "chk_usuario_exclusao_auditoria_resultado" CHECK (resultado::text = 'SUCESSO'::text)
);

CREATE TABLE "usuario_token_redefinicao_senha" (
  "id" uuid NOT NULL,
  "criado_em" timestamp(6) with time zone NOT NULL,
  "expira_em" timestamp(6) with time zone NOT NULL,
  "hash_token" character varying(64) NOT NULL,
  "usado_em" timestamp(6) with time zone,
  "usuario_id" uuid NOT NULL,
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

CREATE INDEX idx_auditoria_acao_criado_em ON public.auditoria_acao_critica USING btree (criado_em DESC);
CREATE INDEX idx_auditoria_acao_entidade ON public.auditoria_acao_critica USING btree (entidade_afetada_tipo, entidade_afetada_id, criado_em DESC);
CREATE INDEX idx_auditoria_acao_tipo ON public.auditoria_acao_critica USING btree (tipo_acao, criado_em DESC);
CREATE INDEX idx_auditoria_acao_usuario ON public.auditoria_acao_critica USING btree (usuario_responsavel_id, criado_em DESC);
CREATE INDEX idx_contratacao_status_data_fim_efetiva ON public.contratacao USING btree (status, data_fim_efetiva) WHERE ((status)::text = 'ENCERRAMENTO_AGENDADO'::text);
CREATE INDEX idx_contratacao_usuario_cuidador_id_status ON public.contratacao USING btree (usuario_cuidador_id, status);
CREATE INDEX idx_contratacao_usuario_responsavel_id_atualizado_em ON public.contratacao USING btree (usuario_responsavel_id, atualizado_em DESC);
CREATE INDEX idx_cuidador_situacao_aprovacao ON public.cuidador USING btree (situacao_aprovacao);
CREATE INDEX idx_cuidador_historico_cuidador_criado ON public.cuidador_historico_situacao USING btree (cuidador_id, criado_em DESC);
CREATE INDEX idx_notificacao_usuario_destinatario_id_removida_em_criado_em ON public.notificacao USING btree (usuario_destinatario_id, removida_em, criado_em DESC);
CREATE UNIQUE INDEX ux_notificacao_chave_deduplicacao ON public.notificacao USING btree (chave_deduplicacao) WHERE (chave_deduplicacao IS NOT NULL);
CREATE INDEX idx_notificacao_preferencia_usuario_id ON public.notificacao_preferencia USING btree (usuario_id);
CREATE INDEX idx_ocorrencia_cuidado_contratacao_id_data_prevista ON public.ocorrencia_cuidado USING btree (contratacao_id, data_prevista);
CREATE INDEX idx_ocorrencia_cuidado_pessoa_assistida_id_data_prevista ON public.ocorrencia_cuidado USING btree (pessoa_assistida_id, data_prevista);
CREATE INDEX idx_ocorrencia_cuidado_status_instante_previsto_utc ON public.ocorrencia_cuidado USING btree (status, instante_previsto_utc);
CREATE INDEX idx_ocorrencia_cuidado_tarefa_id_data_prevista ON public.ocorrencia_cuidado USING btree (tarefa_id, data_prevista);
CREATE INDEX idx_ocorrencia_cuidado_usuario_cuidador_id_data_prevista ON public.ocorrencia_cuidado USING btree (usuario_cuidador_id, data_prevista);
CREATE UNIQUE INDEX ux_ocorrencia_cuidado_contratacao_id_tarefa_id_data_pr_a365437c ON public.ocorrencia_cuidado USING btree (contratacao_id, tarefa_id, data_prevista, horario_previsto);
CREATE INDEX idx_ocorrencia_cuidado_foto_ocorrencia_id_criado_em ON public.ocorrencia_cuidado_foto USING btree (ocorrencia_id, criado_em);
CREATE INDEX idx_ocorrencia_cuidado_foto_registro_atividade_id_criado_em ON public.ocorrencia_cuidado_foto USING btree (registro_atividade_id, criado_em);
CREATE INDEX idx_ocorrencia_cuidado_lembrete_ocorrencia_id_status ON public.ocorrencia_cuidado_lembrete USING btree (ocorrencia_id, status);
CREATE INDEX idx_ocorrencia_cuidado_lembrete_status_previsto_em ON public.ocorrencia_cuidado_lembrete USING btree (status, previsto_em);
CREATE INDEX idx_registro_atendimento_contratacao_id_data_atendimen_4c3ec664 ON public.registro_atendimento USING btree (contratacao_id, data_atendimento, registrado_em);
CREATE INDEX idx_registro_atendimento_cuidador_id_data_atendimento ON public.registro_atendimento USING btree (cuidador_id, data_atendimento);
CREATE INDEX idx_registro_diario_cuidado_contratacao_id_data_regist_b64ac750 ON public.registro_diario_cuidado USING btree (contratacao_id, data_registro, ocorrido_em);
CREATE INDEX idx_registro_diario_cuidado_usuario_cuidador_id_data_r_e12a85a0 ON public.registro_diario_cuidado USING btree (usuario_cuidador_id, data_registro, ocorrido_em);
CREATE INDEX idx_registro_diario_cuidado_usuario_responsavel_id_dat_18645e03 ON public.registro_diario_cuidado USING btree (usuario_responsavel_id, data_registro, ocorrido_em);
CREATE INDEX idx_relatorio_atendimento_responsavel_id_cuidador_id_d_b3f71a9d ON public.relatorio_atendimento USING btree (responsavel_id, cuidador_id, data_atendimento);
CREATE INDEX idx_relatorio_atendimento_status_email_proxima_tentati_88c55cf7 ON public.relatorio_atendimento USING btree (status_email, proxima_tentativa_email_em, email_solicitado_em);
CREATE INDEX idx_responsavel_situacao_aprovacao ON public.responsavel USING btree (situacao_aprovacao);
CREATE INDEX idx_responsavel_historico_responsavel_criado ON public.responsavel_historico_situacao USING btree (responsavel_id, criado_em DESC);
CREATE INDEX idx_rotina_cuidado_pessoa_assistida_id_ativo ON public.rotina_cuidado USING btree (pessoa_assistida_id, ativo);
CREATE INDEX idx_rotina_cuidado_usuario_responsavel_id_atualizado_em ON public.rotina_cuidado USING btree (usuario_responsavel_id, atualizado_em DESC);
CREATE INDEX idx_rotina_cuidado_item_rotina_cuidado_id_ordem_exibicao ON public.rotina_cuidado_item USING btree (rotina_cuidado_id, ordem_exibicao);
CREATE INDEX idx_solicitacao_servico_oportunidade_origem_id_usuario_093350b0 ON public.solicitacao_servico USING btree (oportunidade_origem_id, usuario_cuidador_id);
CREATE INDEX idx_solicitacao_servico_status_iniciado_por_criado_em ON public.solicitacao_servico USING btree (status, iniciado_por, criado_em DESC);
CREATE INDEX idx_solicitacao_servico_usuario_responsavel_id_atualizado_em ON public.solicitacao_servico USING btree (usuario_responsavel_id, atualizado_em DESC);
CREATE INDEX idx_solicitacao_servico_usuario_responsavel_id_status ON public.solicitacao_servico USING btree (usuario_responsavel_id, status);
CREATE INDEX idx_solicitacao_servico_usuario_responsavel_id_usuario_5464ea4f ON public.solicitacao_servico USING btree (usuario_responsavel_id, usuario_cuidador_id, pessoa_assistida_id, status);
CREATE UNIQUE INDEX ux_solicitacao_servico_oportunidade_origem_id_usuario__1abc0dd7 ON public.solicitacao_servico USING btree (oportunidade_origem_id, usuario_cuidador_id) WHERE (oportunidade_origem_id IS NOT NULL);
CREATE INDEX idx_solicitacao_servico_contratacao_historico_status_t_f7eac776 ON public.solicitacao_servico_contratacao_historico_status USING btree (tipo_entidade, entidade_id, criado_em);
CREATE INDEX idx_solicitacao_servico_item_cuidado_copia_solicitacao_4519f23e ON public.solicitacao_servico_item_cuidado_copia USING btree (solicitacao_servico_id, ordem_exibicao);
CREATE INDEX idx_tarefa_cuidado_contratacao_id ON public.tarefa_cuidado USING btree (contratacao_id);
CREATE INDEX idx_tarefa_cuidado_cuidador_executor_id_status_atualizado_em ON public.tarefa_cuidado USING btree (cuidador_executor_id, status, atualizado_em DESC);
CREATE INDEX idx_tarefa_cuidado_pessoa_assistida_id ON public.tarefa_cuidado USING btree (pessoa_assistida_id);
CREATE INDEX idx_tarefa_cuidado_responsavel_criador_id_status_atualizado_em ON public.tarefa_cuidado USING btree (responsavel_criador_id, status, atualizado_em DESC);
CREATE INDEX idx_tarefa_cuidado_tarefa_duplicada_de_id ON public.tarefa_cuidado USING btree (tarefa_duplicada_de_id);
CREATE UNIQUE INDEX ux_tarefa_cuidado_item_copia_origem_id ON public.tarefa_cuidado USING btree (item_copia_origem_id) WHERE (item_copia_origem_id IS NOT NULL);
CREATE INDEX idx_tarefa_cuidado_auditoria_tarefa_id_criado_em ON public.tarefa_cuidado_auditoria USING btree (tarefa_id, criado_em DESC);
CREATE INDEX idx_usuario_situacao_conta ON public.usuario USING btree (situacao_conta);
CREATE INDEX idx_usuario_confirmacao_exclusao_usuario ON public.usuario_confirmacao_exclusao USING btree (usuario_id, criado_em DESC);
CREATE INDEX idx_usuario_exclusao_auditoria_referencia ON public.usuario_exclusao_auditoria USING btree (usuario_referencia, criado_em DESC);
