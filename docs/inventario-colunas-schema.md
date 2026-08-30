# Inventário técnico de colunas do schema `public`

Catálogo extraído após a V041. Cada uma das 417 colunas foi analisada. As descrições funcionais por coluna estão no [dicionário principal](modelagem-banco-dados.md); este apêndice concentra metadados físicos e o mapeamento responsável.

| Tabela | Coluna | Tipo | Obrigatória | PK | FK para | Unique | Check(s) | Default | Índice(s) | JPA/infraestrutura |
|---|---|---|---:|---:|---|---:|---|---|---|---|
| `contratacoes` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_contratacoes | `CareContract` |
| `contratacoes` | `solicitacao_servico_id` | uuid | Sim | Não | solicitacoes_servico | Sim | — | — | uk_contratacoes_solicitacao_servico_id | `CareContract` |
| `contratacoes` | `usuario_responsavel_id` | uuid | Sim | Não | usuarios | Não | — | — | idx_contratacoes_usuario_responsavel_id_atualizado_em | `CareContract` |
| `contratacoes` | `usuario_cuidador_id` | uuid | Sim | Não | usuarios | Não | — | — | idx_contratacoes_usuario_cuidador_id_status | `CareContract` |
| `contratacoes` | `pessoa_assistida_id` | uuid | Sim | Não | pessoas_assistidas | Não | — | — | — | `CareContract` |
| `contratacoes` | `status` | character varying | Sim | Não | — | Não | — | — | idx_contratacoes_status_data_fim_efetiva, idx_contratacoes_usuario_cuidador_id_status | `CareContract` |
| `contratacoes` | `data_inicio` | date | Sim | Não | — | Não | — | — | — | `CareContract` |
| `contratacoes` | `data_fim` | date | Não | Não | — | Não | — | — | — | `CareContract` |
| `contratacoes` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `CareContract` |
| `contratacoes` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_contratacoes_usuario_responsavel_id_atualizado_em | `CareContract` |
| `contratacoes` | `motivo_cancelamento` | character varying | Não | Não | — | Não | — | — | — | `CareContract` |
| `contratacoes` | `motivo_encerramento` | character varying | Não | Não | — | Não | — | — | — | `CareContract` |
| `contratacoes` | `tipo_encerramento` | character varying | Não | Não | — | Não | — | — | — | `CareContract` |
| `contratacoes` | `motivo_solicitacao_encerramento` | character varying | Não | Não | — | Não | — | — | — | `CareContract` |
| `contratacoes` | `observacoes_encerramento` | character varying | Não | Não | — | Não | — | — | — | `CareContract` |
| `contratacoes` | `usuario_solicitante_encerramento_id` | uuid | Não | Não | usuarios | Não | — | — | — | `CareContract` |
| `contratacoes` | `encerramento_solicitado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `CareContract` |
| `contratacoes` | `data_fim_efetiva` | date | Não | Não | — | Não | — | — | idx_contratacoes_status_data_fim_efetiva | `CareContract` |
| `contratacoes` | `cancelado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `CareContract` |
| `contratacoes` | `usuario_solicitante_cancelamento_id` | uuid | Não | Não | usuarios | Não | — | — | — | `CareContract` |
| `contratacoes` | `cancelamento_solicitado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `CareContract` |
| `cuidadores` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_cuidadores | `CaregiverProfile` |
| `cuidadores` | `usuario_id` | uuid | Sim | Não | usuarios | Sim | — | — | uk_cuidadores_usuario_id | `CaregiverProfile` |
| `cuidadores` | `formacao` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `formacao_outro` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `experiencia` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `biografia` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `cep` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `rua` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `numero` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `complemento` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `bairro` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `cidade` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `estado` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `ponto_referencia` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `horario_inicio` | time without time zone | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `horario_fim` | time without time zone | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `observacao` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `modalidade_outro` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `servico_outro` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `tempo_experiencia` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `latitude` | numeric | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores` | `longitude` | numeric | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidadores_disponibilidade_dias` | `perfil_cuidador_id` | uuid | Sim | Não | cuidadores | Não | — | — | — | `CaregiverAvailability` |
| `cuidadores_disponibilidade_dias` | `dia_semana` | character varying | Sim | Não | — | Não | — | — | — | `CaregiverAvailability` |
| `cuidadores_disponibilidade_periodos` | `perfil_cuidador_id` | uuid | Sim | Não | cuidadores | Não | — | — | — | `CaregiverAvailability` |
| `cuidadores_disponibilidade_periodos` | `periodo` | character varying | Sim | Não | — | Não | — | — | — | `CaregiverAvailability` |
| `cuidadores_formacoes` | `perfil_cuidador_id` | uuid | Sim | Não | cuidadores | Sim | — | — | uk_cuidadores_formacoes_perfil_cuidador_id_formacao | coleção de `CaregiverProfile` |
| `cuidadores_formacoes` | `formacao` | character varying | Sim | Não | — | Sim | — | — | uk_cuidadores_formacoes_perfil_cuidador_id_formacao | coleção de `CaregiverProfile` |
| `cuidadores_modalidades` | `perfil_cuidador_id` | uuid | Sim | Não | cuidadores | Não | — | — | — | coleção de `CaregiverProfile` |
| `cuidadores_modalidades` | `modalidade` | character varying | Sim | Não | — | Não | — | — | — | coleção de `CaregiverProfile` |
| `cuidadores_servicos` | `perfil_cuidador_id` | uuid | Sim | Não | cuidadores | Não | — | — | — | coleção de `CaregiverProfile` |
| `cuidadores_servicos` | `servico` | character varying | Sim | Não | — | Não | — | — | — | coleção de `CaregiverProfile` |
| `flyway_schema_history` | `installed_rank` | integer | Sim | Sim | — | Não | — | — | flyway_schema_history_pk | Flyway |
| `flyway_schema_history` | `version` | character varying | Não | Não | — | Não | — | — | — | Flyway |
| `flyway_schema_history` | `description` | character varying | Sim | Não | — | Não | — | — | — | Flyway |
| `flyway_schema_history` | `type` | character varying | Sim | Não | — | Não | — | — | — | Flyway |
| `flyway_schema_history` | `script` | character varying | Sim | Não | — | Não | — | — | — | Flyway |
| `flyway_schema_history` | `checksum` | integer | Não | Não | — | Não | — | — | — | Flyway |
| `flyway_schema_history` | `installed_by` | character varying | Sim | Não | — | Não | — | — | — | Flyway |
| `flyway_schema_history` | `installed_on` | timestamp without time zone | Sim | Não | — | Não | — | now() | — | Flyway |
| `flyway_schema_history` | `execution_time` | integer | Sim | Não | — | Não | — | — | — | Flyway |
| `flyway_schema_history` | `success` | boolean | Sim | Não | — | Não | — | — | flyway_schema_history_s_idx | Flyway |
| `notificacoes` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_notificacoes | `Notification` |
| `notificacoes` | `usuario_destinatario_id` | uuid | Sim | Não | usuarios | Não | — | — | idx_notificacoes_usuario_destinatario_id_removida_em_criado_em | `Notification` |
| `notificacoes` | `tipo` | character varying | Sim | Não | — | Não | — | — | — | `Notification` |
| `notificacoes` | `titulo` | character varying | Sim | Não | — | Não | — | — | — | `Notification` |
| `notificacoes` | `mensagem` | character varying | Sim | Não | — | Não | — | — | — | `Notification` |
| `notificacoes` | `tipo_entidade_relacionada` | character varying | Sim | Não | — | Não | — | — | — | `Notification` |
| `notificacoes` | `entidade_relacionada_id` | uuid | Sim | Não | — | Não | — | — | — | `Notification` |
| `notificacoes` | `lida_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `Notification` |
| `notificacoes` | `removida_em` | timestamp with time zone | Não | Não | — | Não | — | — | idx_notificacoes_usuario_destinatario_id_removida_em_criado_em | `Notification` |
| `notificacoes` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_notificacoes_usuario_destinatario_id_removida_em_criado_em | `Notification` |
| `notificacoes` | `chave_deduplicacao` | character varying | Não | Não | — | Sim | — | — | ux_notificacoes_chave_deduplicacao | `Notification` |
| `notificacoes_preferencias` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_notificacoes_preferencias | `UserNotificationPreference` |
| `notificacoes_preferencias` | `usuario_id` | uuid | Sim | Não | usuarios | Sim | — | — | idx_notificacoes_preferencias_usuario_id, uk_notificacoes_preferencias_usuario_id_tipo_notificacao | `UserNotificationPreference` |
| `notificacoes_preferencias` | `tipo_notificacao` | character varying | Sim | Não | — | Sim | — | — | uk_notificacoes_preferencias_usuario_id_tipo_notificacao | `UserNotificationPreference` |
| `notificacoes_preferencias` | `habilitado` | boolean | Sim | Não | — | Não | — | — | — | `UserNotificationPreference` |
| `notificacoes_preferencias` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `UserNotificationPreference` |
| `notificacoes_preferencias` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `UserNotificationPreference` |
| `ocorrencias_cuidado` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_ocorrencias_cuidado | `TaskOccurrence` |
| `ocorrencias_cuidado` | `tarefa_id` | uuid | Sim | Não | tarefas_cuidado | Sim | — | — | idx_ocorrencias_cuidado_tarefa_id_data_prevista, uk_ocorrencias_cuidado_tarefa_id_data_prevista_horario_previsto, ux_ocorrencias_cuidado_contratacao_id_tarefa_id_data_p_d0d3090c | `TaskOccurrence` |
| `ocorrencias_cuidado` | `contratacao_id` | uuid | Sim | Não | contratacoes | Sim | — | — | idx_ocorrencias_cuidado_contratacao_id_data_prevista, ux_ocorrencias_cuidado_contratacao_id_tarefa_id_data_p_d0d3090c | `TaskOccurrence` |
| `ocorrencias_cuidado` | `pessoa_assistida_id` | uuid | Sim | Não | pessoas_assistidas | Não | — | — | idx_ocorrencias_cuidado_pessoa_assistida_id_data_prevista | `TaskOccurrence` |
| `ocorrencias_cuidado` | `usuario_cuidador_id` | uuid | Sim | Não | usuarios | Não | — | — | idx_ocorrencias_cuidado_usuario_cuidador_id_data_prevista | `TaskOccurrence` |
| `ocorrencias_cuidado` | `data_prevista` | date | Sim | Não | — | Sim | — | — | idx_ocorrencias_cuidado_contratacao_id_data_prevista, idx_ocorrencias_cuidado_pessoa_assistida_id_data_prevista, idx_ocorrencias_cuidado_tarefa_id_data_prevista, idx_ocorrencias_cuidado_usuario_cuidador_id_data_prevista, uk_ocorrencias_cuidado_tarefa_id_data_prevista_horario_previsto, ux_ocorrencias_cuidado_contratacao_id_tarefa_id_data_p_d0d3090c | `TaskOccurrence` |
| `ocorrencias_cuidado` | `horario_previsto` | time without time zone | Sim | Não | — | Sim | — | — | uk_ocorrencias_cuidado_tarefa_id_data_prevista_horario_previsto, ux_ocorrencias_cuidado_contratacao_id_tarefa_id_data_p_d0d3090c | `TaskOccurrence` |
| `ocorrencias_cuidado` | `instante_previsto_utc` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_ocorrencias_cuidado_status_instante_previsto_utc | `TaskOccurrence` |
| `ocorrencias_cuidado` | `fuso_horario` | character varying | Sim | Não | — | Não | — | — | — | `TaskOccurrence` |
| `ocorrencias_cuidado` | `status` | character varying | Sim | Não | — | Não | — | — | idx_ocorrencias_cuidado_status_instante_previsto_utc | `TaskOccurrence` |
| `ocorrencias_cuidado` | `concluido_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `TaskOccurrence` |
| `ocorrencias_cuidado` | `usuario_executor_id` | uuid | Não | Não | usuarios | Não | — | — | — | `TaskOccurrence` |
| `ocorrencias_cuidado` | `motivo_nao_realizacao` | character varying | Não | Não | — | Não | — | — | — | `TaskOccurrence` |
| `ocorrencias_cuidado` | `anotacao_execucao` | character varying | Não | Não | — | Não | — | — | — | `TaskOccurrence` |
| `ocorrencias_cuidado` | `cancelado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `TaskOccurrence` |
| `ocorrencias_cuidado` | `excecao` | boolean | Sim | Não | — | Não | — | false | — | `TaskOccurrence` |
| `ocorrencias_cuidado` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `TaskOccurrence` |
| `ocorrencias_cuidado` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `TaskOccurrence` |
| `ocorrencias_cuidado` | `versao` | bigint | Sim | Não | — | Não | — | 0 | — | `TaskOccurrence` |
| `ocorrencias_cuidado` | `marcada_nao_realizada_automaticamente` | boolean | Sim | Não | — | Não | — | false | — | `TaskOccurrence` |
| `ocorrencias_cuidado` | `status_atualizado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `TaskOccurrence` |
| `ocorrencias_cuidado_fotos` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_ocorrencias_cuidado_fotos | `CareOccurrencePhoto` |
| `ocorrencias_cuidado_fotos` | `ocorrencia_id` | uuid | Não | Não | ocorrencias_cuidado | Não | ck_ocorrencias_cuidado_fotos_vinculo_unico | — | idx_ocorrencias_cuidado_fotos_ocorrencia_id_criado_em | `CareOccurrencePhoto` |
| `ocorrencias_cuidado_fotos` | `usuario_envio_id` | uuid | Sim | Não | usuarios | Não | — | — | — | `CareOccurrencePhoto` |
| `ocorrencias_cuidado_fotos` | `nome_arquivo` | character varying | Sim | Não | — | Sim | — | — | uk_ocorrencias_cuidado_fotos_nome_arquivo | `CareOccurrencePhoto` |
| `ocorrencias_cuidado_fotos` | `nome_arquivo_original` | character varying | Não | Não | — | Não | — | — | — | `CareOccurrencePhoto` |
| `ocorrencias_cuidado_fotos` | `tipo_conteudo` | character varying | Sim | Não | — | Não | — | — | — | `CareOccurrencePhoto` |
| `ocorrencias_cuidado_fotos` | `tamanho_arquivo` | bigint | Sim | Não | — | Não | — | — | — | `CareOccurrencePhoto` |
| `ocorrencias_cuidado_fotos` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_ocorrencias_cuidado_fotos_ocorrencia_id_criado_em, idx_ocorrencias_cuidado_fotos_registro_atividade_id_criado_em | `CareOccurrencePhoto` |
| `ocorrencias_cuidado_fotos` | `registro_atividade_id` | uuid | Não | Não | registros_diario_cuidado | Não | ck_ocorrencias_cuidado_fotos_vinculo_unico | — | idx_ocorrencias_cuidado_fotos_registro_atividade_id_criado_em | `CareOccurrencePhoto` |
| `ocorrencias_cuidado_lembretes` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_ocorrencias_cuidado_lembretes | `TaskReminder` |
| `ocorrencias_cuidado_lembretes` | `ocorrencia_id` | uuid | Sim | Não | ocorrencias_cuidado | Não | — | — | idx_ocorrencias_cuidado_lembretes_ocorrencia_id_status | `TaskReminder` |
| `ocorrencias_cuidado_lembretes` | `usuario_destinatario_id` | uuid | Sim | Não | usuarios | Não | — | — | — | `TaskReminder` |
| `ocorrencias_cuidado_lembretes` | `tipo_lembrete` | character varying | Sim | Não | — | Não | — | — | — | `TaskReminder` |
| `ocorrencias_cuidado_lembretes` | `previsto_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_ocorrencias_cuidado_lembretes_status_previsto_em | `TaskReminder` |
| `ocorrencias_cuidado_lembretes` | `enviado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `TaskReminder` |
| `ocorrencias_cuidado_lembretes` | `cancelado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `TaskReminder` |
| `ocorrencias_cuidado_lembretes` | `status` | character varying | Sim | Não | — | Não | — | — | idx_ocorrencias_cuidado_lembretes_ocorrencia_id_status, idx_ocorrencias_cuidado_lembretes_status_previsto_em | `TaskReminder` |
| `ocorrencias_cuidado_lembretes` | `chave_deduplicacao` | character varying | Sim | Não | — | Sim | — | — | uk_ocorrencias_cuidado_lembretes_chave_deduplicacao | `TaskReminder` |
| `ocorrencias_cuidado_lembretes` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `TaskReminder` |
| `ocorrencias_cuidado_lembretes` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `TaskReminder` |
| `pessoas_assistidas` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_pessoas_assistidas | `AssistedPerson` |
| `pessoas_assistidas` | `usuario_responsavel_id` | uuid | Sim | Não | usuarios | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `nome` | character varying | Sim | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `cpf` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `data_nascimento` | date | Sim | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `grau_dependencia` | character varying | Sim | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `mobilidade` | character varying | Sim | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `mobilidade_outro` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `alergias_outro` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `alergias_detalhes` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `restricoes_alimentares_outro` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `restricoes_alimentares_detalhes` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `medicamentos` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `observacoes` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `cep` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `rua` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `numero` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `complemento` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `bairro` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `cidade` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `estado` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `ponto_referencia` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `latitude` | numeric | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas` | `longitude` | numeric | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoas_assistidas_alergias` | `pessoa_assistida_id` | uuid | Sim | Não | pessoas_assistidas | Não | — | — | — | coleção de `AssistedPerson` |
| `pessoas_assistidas_alergias` | `alergia` | character varying | Sim | Não | — | Não | — | — | — | coleção de `AssistedPerson` |
| `pessoas_assistidas_contatos_emergencia` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_pessoas_assistidas_contatos_emergencia | `EmergencyContact` |
| `pessoas_assistidas_contatos_emergencia` | `pessoa_assistida_id` | uuid | Sim | Não | pessoas_assistidas | Sim | — | — | uk_pessoas_assistidas_contatos_emergencia_pessoa_assistida_id | `EmergencyContact` |
| `pessoas_assistidas_contatos_emergencia` | `nome` | character varying | Sim | Não | — | Não | — | — | — | `EmergencyContact` |
| `pessoas_assistidas_contatos_emergencia` | `telefone` | character varying | Sim | Não | — | Não | — | — | — | `EmergencyContact` |
| `pessoas_assistidas_contatos_emergencia` | `vinculo` | character varying | Sim | Não | — | Não | — | — | — | `EmergencyContact` |
| `pessoas_assistidas_contatos_emergencia` | `contato_responsavel` | boolean | Sim | Não | — | Não | — | false | — | `EmergencyContact` |
| `pessoas_assistidas_contatos_emergencia` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `EmergencyContact` |
| `pessoas_assistidas_contatos_emergencia` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `EmergencyContact` |
| `pessoas_assistidas_restricoes_alimentares` | `pessoa_assistida_id` | uuid | Sim | Não | pessoas_assistidas | Não | — | — | — | coleção de `AssistedPerson` |
| `pessoas_assistidas_restricoes_alimentares` | `restricao` | character varying | Sim | Não | — | Não | — | — | — | coleção de `AssistedPerson` |
| `registros_atendimento` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_registros_atendimento | `ServiceAttendanceRecord` |
| `registros_atendimento` | `contratacao_id` | uuid | Sim | Não | contratacoes | Sim | — | — | idx_registros_atendimento_contratacao_id_data_atendime_44da5d09, uk_registros_atendimento_contratacao_id_data_atendimen_cc40da51 | `ServiceAttendanceRecord` |
| `registros_atendimento` | `cuidador_id` | uuid | Sim | Não | usuarios | Não | — | — | idx_registros_atendimento_cuidador_id_data_atendimento | `ServiceAttendanceRecord` |
| `registros_atendimento` | `responsavel_id` | uuid | Sim | Não | usuarios | Não | — | — | — | `ServiceAttendanceRecord` |
| `registros_atendimento` | `pessoa_assistida_id` | uuid | Sim | Não | pessoas_assistidas | Não | — | — | — | `ServiceAttendanceRecord` |
| `registros_atendimento` | `data_atendimento` | date | Sim | Não | — | Sim | — | — | idx_registros_atendimento_contratacao_id_data_atendime_44da5d09, idx_registros_atendimento_cuidador_id_data_atendimento, uk_registros_atendimento_contratacao_id_data_atendimen_cc40da51 | `ServiceAttendanceRecord` |
| `registros_atendimento` | `tipo_registro` | character varying | Sim | Não | — | Sim | ck_registros_atendimento_tipo | — | uk_registros_atendimento_contratacao_id_data_atendimen_cc40da51 | `ServiceAttendanceRecord` |
| `registros_atendimento` | `registrado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_registros_atendimento_contratacao_id_data_atendime_44da5d09 | `ServiceAttendanceRecord` |
| `registros_atendimento` | `latitude` | double precision | Sim | Não | — | Não | ck_registros_atendimento_latitude | — | — | `ServiceAttendanceRecord` |
| `registros_atendimento` | `longitude` | double precision | Sim | Não | — | Não | ck_registros_atendimento_longitude | — | — | `ServiceAttendanceRecord` |
| `registros_atendimento` | `precisao` | double precision | Sim | Não | — | Não | ck_registros_atendimento_precisao | — | — | `ServiceAttendanceRecord` |
| `registros_atendimento` | `localizacao_capturada_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registros_atendimento` | `endereco_registrado` | character varying | Não | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registros_atendimento` | `fuso_dispositivo` | character varying | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registros_atendimento` | `horario_inicio_previsto` | time without time zone | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registros_atendimento` | `horario_fim_previsto` | time without time zone | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registros_atendimento` | `janela_permitida_inicio` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registros_atendimento` | `janela_permitida_fim` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registros_atendimento` | `dentro_janela_permitida` | boolean | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registros_atendimento` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registros_atendimento` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registros_diario_cuidado` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_registros_diario_cuidado | `CareActivityRecord` |
| `registros_diario_cuidado` | `ocorrencia_id` | uuid | Não | Não | ocorrencias_cuidado | Sim | — | — | uk_registros_diario_cuidado_ocorrencia_id | `CareActivityRecord` |
| `registros_diario_cuidado` | `contratacao_id` | uuid | Sim | Não | contratacoes | Não | — | — | idx_registros_diario_cuidado_contratacao_id_data_regis_8ac737e5 | `CareActivityRecord` |
| `registros_diario_cuidado` | `pessoa_assistida_id` | uuid | Sim | Não | pessoas_assistidas | Não | — | — | — | `CareActivityRecord` |
| `registros_diario_cuidado` | `usuario_responsavel_id` | uuid | Sim | Não | usuarios | Não | — | — | idx_registros_diario_cuidado_usuario_responsavel_id_da_0db0a88a | `CareActivityRecord` |
| `registros_diario_cuidado` | `usuario_cuidador_id` | uuid | Sim | Não | usuarios | Não | — | — | idx_registros_diario_cuidado_usuario_cuidador_id_data__74961eac | `CareActivityRecord` |
| `registros_diario_cuidado` | `tipo_atividade` | character varying | Sim | Não | — | Não | — | — | — | `CareActivityRecord` |
| `registros_diario_cuidado` | `titulo` | character varying | Sim | Não | — | Não | — | — | — | `CareActivityRecord` |
| `registros_diario_cuidado` | `anotacoes` | character varying | Não | Não | — | Não | — | — | — | `CareActivityRecord` |
| `registros_diario_cuidado` | `ocorrido_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_registros_diario_cuidado_contratacao_id_data_regis_8ac737e5, idx_registros_diario_cuidado_usuario_cuidador_id_data__74961eac, idx_registros_diario_cuidado_usuario_responsavel_id_da_0db0a88a | `CareActivityRecord` |
| `registros_diario_cuidado` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `CareActivityRecord` |
| `registros_diario_cuidado` | `tipo_origem` | character varying | Sim | Não | — | Não | — | 'PLANNED'::character varying | — | `CareActivityRecord` |
| `registros_diario_cuidado` | `data_registro` | date | Sim | Não | — | Não | — | — | idx_registros_diario_cuidado_contratacao_id_data_regis_8ac737e5, idx_registros_diario_cuidado_usuario_cuidador_id_data__74961eac, idx_registros_diario_cuidado_usuario_responsavel_id_da_0db0a88a | `CareActivityRecord` |
| `registros_diario_cuidado` | `fuso_horario` | character varying | Sim | Não | — | Não | — | — | — | `CareActivityRecord` |
| `registros_diario_cuidado` | `tipo_cuidado` | character varying | Sim | Não | — | Não | — | — | — | `CareActivityRecord` |
| `registros_diario_cuidado` | `descricao` | character varying | Não | Não | — | Não | — | — | — | `CareActivityRecord` |
| `registros_diario_cuidado` | `importante` | boolean | Sim | Não | — | Não | — | false | — | `CareActivityRecord` |
| `registros_diario_cuidado` | `usuario_criacao_id` | uuid | Sim | Não | usuarios | Não | — | — | — | `CareActivityRecord` |
| `relatorios_atendimento` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_relatorios_atendimento | `AttendanceReport` |
| `relatorios_atendimento` | `contratacao_id` | uuid | Sim | Não | contratacoes | Sim | — | — | uk_relatorios_atendimento_contratacao_id_data_atendimento | `AttendanceReport` |
| `relatorios_atendimento` | `data_atendimento` | date | Sim | Não | — | Sim | — | — | idx_relatorios_atendimento_responsavel_id_cuidador_id__90df1883, uk_relatorios_atendimento_contratacao_id_data_atendimento | `AttendanceReport` |
| `relatorios_atendimento` | `registro_inicio_atendimento_id` | uuid | Sim | Não | registros_atendimento | Não | — | — | — | `AttendanceReport` |
| `relatorios_atendimento` | `registro_fim_atendimento_id` | uuid | Sim | Não | registros_atendimento | Não | — | — | — | `AttendanceReport` |
| `relatorios_atendimento` | `cuidador_id` | uuid | Sim | Não | usuarios | Não | — | — | idx_relatorios_atendimento_responsavel_id_cuidador_id__90df1883 | `AttendanceReport` |
| `relatorios_atendimento` | `responsavel_id` | uuid | Sim | Não | usuarios | Não | — | — | idx_relatorios_atendimento_responsavel_id_cuidador_id__90df1883 | `AttendanceReport` |
| `relatorios_atendimento` | `pessoa_assistida_id` | uuid | Sim | Não | pessoas_assistidas | Não | — | — | — | `AttendanceReport` |
| `relatorios_atendimento` | `texto_gerado` | text | Sim | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorios_atendimento` | `texto_editado` | text | Não | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorios_atendimento` | `texto_final` | text | Não | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorios_atendimento` | `observacoes_adicionais` | character varying | Não | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorios_atendimento` | `anotacoes_enfermagem` | text | Sim | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorios_atendimento` | `status` | character varying | Sim | Não | — | Não | ck_relatorios_atendimento_status | — | — | `AttendanceReport` |
| `relatorios_atendimento` | `status_email` | character varying | Sim | Não | — | Não | ck_relatorios_atendimento_status_email | 'NOT_SENT'::character varying | idx_relatorios_atendimento_status_email_proxima_tentat_0fcf1039 | `AttendanceReport` |
| `relatorios_atendimento` | `email_enviado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorios_atendimento` | `mensagem_erro_email` | character varying | Não | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorios_atendimento` | `gerado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorios_atendimento` | `editado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorios_atendimento` | `finalizado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorios_atendimento` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorios_atendimento` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorios_atendimento` | `email_solicitado_em` | timestamp with time zone | Não | Não | — | Não | — | — | idx_relatorios_atendimento_status_email_proxima_tentat_0fcf1039 | `AttendanceReport` |
| `relatorios_atendimento` | `tentativas_email` | integer | Sim | Não | — | Não | — | 0 | — | `AttendanceReport` |
| `relatorios_atendimento` | `proxima_tentativa_email_em` | timestamp with time zone | Não | Não | — | Não | — | — | idx_relatorios_atendimento_status_email_proxima_tentat_0fcf1039 | `AttendanceReport` |
| `responsaveis` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_responsaveis | `ResponsibleProfile` |
| `responsaveis` | `usuario_id` | uuid | Sim | Não | usuarios | Sim | — | — | uk_responsaveis_usuario_id | `ResponsibleProfile` |
| `responsaveis` | `parentesco` | character varying | Sim | Não | — | Não | — | — | — | `ResponsibleProfile` |
| `responsaveis` | `parentesco_outro` | character varying | Não | Não | — | Não | — | — | — | `ResponsibleProfile` |
| `responsaveis` | `preferencia_contato` | character varying | Sim | Não | — | Não | — | — | — | `ResponsibleProfile` |
| `responsaveis` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ResponsibleProfile` |
| `responsaveis` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ResponsibleProfile` |
| `rotinas_cuidado` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_rotinas_cuidado | `CareRoutine` |
| `rotinas_cuidado` | `usuario_responsavel_id` | uuid | Sim | Não | usuarios | Não | — | — | idx_rotinas_cuidado_usuario_responsavel_id_atualizado_em | `CareRoutine` |
| `rotinas_cuidado` | `pessoa_assistida_id` | uuid | Não | Não | pessoas_assistidas | Não | — | — | idx_rotinas_cuidado_pessoa_assistida_id_ativo | `CareRoutine` |
| `rotinas_cuidado` | `nome` | character varying | Sim | Não | — | Não | — | — | — | `CareRoutine` |
| `rotinas_cuidado` | `descricao` | character varying | Não | Não | — | Não | — | — | — | `CareRoutine` |
| `rotinas_cuidado` | `ativo` | boolean | Sim | Não | — | Não | — | true | idx_rotinas_cuidado_pessoa_assistida_id_ativo | `CareRoutine` |
| `rotinas_cuidado` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `CareRoutine` |
| `rotinas_cuidado` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_rotinas_cuidado_usuario_responsavel_id_atualizado_em | `CareRoutine` |
| `rotinas_cuidado_itens` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_rotinas_cuidado_itens | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `rotina_cuidado_id` | uuid | Sim | Não | rotinas_cuidado | Não | — | — | idx_rotinas_cuidado_itens_rotina_cuidado_id_ordem_exibicao | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `titulo` | character varying | Sim | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `descricao` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `ordem_exibicao` | integer | Sim | Não | — | Não | — | — | idx_rotinas_cuidado_itens_rotina_cuidado_id_ordem_exibicao | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `ativo` | boolean | Sim | Não | — | Não | — | true | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `categoria` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `categoria_personalizada` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `prioridade` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `tipo_recorrencia` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `horario_previsto` | time without time zone | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `intervalo_dias` | integer | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `lembrete_habilitado` | boolean | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `minutos_antecedencia_lembrete` | integer | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `anotacoes` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `nome_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `dosagem_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `unidade_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `unidade_personalizada_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `via_administracao_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `via_personalizada_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `instrucoes_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `lembrar_no_horario_previsto` | boolean | Sim | Não | — | Não | — | true | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `lembrete_atraso_habilitado` | boolean | Sim | Não | — | Não | — | false | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `minutos_para_atraso` | integer | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `repetir_enquanto_pendente` | boolean | Sim | Não | — | Não | — | false | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `intervalo_repeticao_minutos` | integer | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `importante` | boolean | Sim | Não | — | Não | — | false | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `notificar_responsavel_se_importante` | boolean | Sim | Não | — | Não | — | false | — | `CareRoutineItem` |
| `rotinas_cuidado_itens` | `exige_foto_conclusao` | boolean | Sim | Não | — | Não | — | false | — | `CareRoutineItem` |
| `rotinas_cuidado_itens_dias_semana` | `item_rotina_cuidado_id` | uuid | Sim | Sim | rotinas_cuidado_itens | Não | — | — | pk_rotinas_cuidado_itens_dias_semana | coleção de `CareRoutineItem` |
| `rotinas_cuidado_itens_dias_semana` | `dia_semana` | character varying | Sim | Sim | — | Não | — | — | pk_rotinas_cuidado_itens_dias_semana | coleção de `CareRoutineItem` |
| `solicitacoes_servico` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_solicitacoes_servico | `ServiceRequest` |
| `solicitacoes_servico` | `usuario_responsavel_id` | uuid | Sim | Não | usuarios | Não | — | — | idx_solicitacoes_servico_usuario_responsavel_id_atualizado_em, idx_solicitacoes_servico_usuario_responsavel_id_status, idx_solicitacoes_servico_usuario_responsavel_id_usuari_6b7dd64f | `ServiceRequest` |
| `solicitacoes_servico` | `usuario_cuidador_id` | uuid | Não | Não | usuarios | Sim | — | — | idx_solicitacoes_servico_oportunidade_origem_id_usuari_7f881080, idx_solicitacoes_servico_usuario_responsavel_id_usuari_6b7dd64f, ux_solicitacoes_servico_oportunidade_origem_id_usuario_82cfc2c0 | `ServiceRequest` |
| `solicitacoes_servico` | `pessoa_assistida_id` | uuid | Sim | Não | pessoas_assistidas | Não | — | — | idx_solicitacoes_servico_usuario_responsavel_id_usuari_6b7dd64f | `ServiceRequest` |
| `solicitacoes_servico` | `tipo_contratacao` | character varying | Sim | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacoes_servico` | `status` | character varying | Sim | Não | — | Não | — | — | idx_solicitacoes_servico_status_iniciado_por_criado_em, idx_solicitacoes_servico_usuario_responsavel_id_status, idx_solicitacoes_servico_usuario_responsavel_id_usuari_6b7dd64f | `ServiceRequest` |
| `solicitacoes_servico` | `data_inicio` | date | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacoes_servico` | `data_fim` | date | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacoes_servico` | `descricao_necessidades` | character varying | Sim | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacoes_servico` | `outra_atividade` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacoes_servico` | `observacoes_adicionais` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacoes_servico` | `observacoes_negociacao` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacoes_servico` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_solicitacoes_servico_status_iniciado_por_criado_em | `ServiceRequest` |
| `solicitacoes_servico` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_solicitacoes_servico_usuario_responsavel_id_atualizado_em | `ServiceRequest` |
| `solicitacoes_servico` | `expira_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacoes_servico` | `cancelado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacoes_servico` | `motivo_rejeicao` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacoes_servico` | `motivo_cancelamento` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacoes_servico` | `rotina_cuidado_id` | uuid | Não | Não | rotinas_cuidado | Não | — | — | — | `ServiceRequest` |
| `solicitacoes_servico` | `nome_rotina_copia` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacoes_servico` | `iniciado_por` | character varying | Sim | Não | — | Não | — | 'RESPONSIBLE'::character varying | idx_solicitacoes_servico_status_iniciado_por_criado_em | `ServiceRequest` |
| `solicitacoes_servico` | `usuario_solicitante_id` | uuid | Sim | Não | usuarios | Não | — | — | — | `ServiceRequest` |
| `solicitacoes_servico` | `oportunidade_origem_id` | uuid | Não | Não | solicitacoes_servico | Sim | — | — | idx_solicitacoes_servico_oportunidade_origem_id_usuari_7f881080, ux_solicitacoes_servico_oportunidade_origem_id_usuario_82cfc2c0 | `ServiceRequest` |
| `solicitacoes_servico_agenda_dias` | `solicitacao_servico_id` | uuid | Sim | Sim | solicitacoes_servico | Não | — | — | pk_solicitacoes_servico_agenda_dias | coleção de `ServiceRequest` |
| `solicitacoes_servico_agenda_dias` | `dia_semana` | character varying | Sim | Sim | — | Não | — | — | pk_solicitacoes_servico_agenda_dias | coleção de `ServiceRequest` |
| `solicitacoes_servico_agenda_dias` | `horario_inicio` | time without time zone | Sim | Não | — | Não | — | — | — | coleção de `ServiceRequest` |
| `solicitacoes_servico_agenda_dias` | `horario_fim` | time without time zone | Sim | Não | — | Não | — | — | — | coleção de `ServiceRequest` |
| `solicitacoes_servico_atividades` | `solicitacao_servico_id` | uuid | Sim | Sim | solicitacoes_servico | Não | — | — | pk_solicitacoes_servico_atividades | coleção de `ServiceRequest` |
| `solicitacoes_servico_atividades` | `atividade` | character varying | Sim | Sim | — | Não | — | — | pk_solicitacoes_servico_atividades | coleção de `ServiceRequest` |
| `solicitacoes_servico_contratacoes_historico_status` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_solicitacoes_servico_contratacoes_historico_status | `StatusHistory` |
| `solicitacoes_servico_contratacoes_historico_status` | `tipo_entidade` | character varying | Sim | Não | — | Não | — | — | idx_solicitacoes_servico_contratacoes_historico_status_157c06d0 | `StatusHistory` |
| `solicitacoes_servico_contratacoes_historico_status` | `entidade_id` | uuid | Sim | Não | — | Não | — | — | idx_solicitacoes_servico_contratacoes_historico_status_157c06d0 | `StatusHistory` |
| `solicitacoes_servico_contratacoes_historico_status` | `status_anterior` | character varying | Não | Não | — | Não | — | — | — | `StatusHistory` |
| `solicitacoes_servico_contratacoes_historico_status` | `novo_status` | character varying | Sim | Não | — | Não | — | — | — | `StatusHistory` |
| `solicitacoes_servico_contratacoes_historico_status` | `usuario_alteracao_id` | uuid | Não | Não | usuarios | Não | — | — | — | `StatusHistory` |
| `solicitacoes_servico_contratacoes_historico_status` | `motivo` | character varying | Não | Não | — | Não | — | — | — | `StatusHistory` |
| `solicitacoes_servico_contratacoes_historico_status` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_solicitacoes_servico_contratacoes_historico_status_157c06d0 | `StatusHistory` |
| `solicitacoes_servico_datas` | `solicitacao_servico_id` | uuid | Sim | Sim | solicitacoes_servico | Não | — | — | pk_solicitacoes_servico_datas | coleção de `ServiceRequest` |
| `solicitacoes_servico_datas` | `data_servico` | date | Sim | Sim | — | Não | — | — | pk_solicitacoes_servico_datas | coleção de `ServiceRequest` |
| `solicitacoes_servico_itens_cuidado_copias` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_solicitacoes_servico_itens_cuidado_copias | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `solicitacao_servico_id` | uuid | Sim | Não | solicitacoes_servico | Não | — | — | idx_solicitacoes_servico_itens_cuidado_copias_solicita_d0389010 | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `rotina_cuidado_original_id` | uuid | Sim | Não | rotinas_cuidado | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `item_rotina_cuidado_original_id` | uuid | Não | Não | rotinas_cuidado_itens | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `titulo` | character varying | Sim | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `descricao` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `ordem_exibicao` | integer | Sim | Não | — | Não | — | — | idx_solicitacoes_servico_itens_cuidado_copias_solicita_d0389010 | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `categoria` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `categoria_personalizada` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `prioridade` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `tipo_recorrencia` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `horario_previsto` | time without time zone | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `intervalo_dias` | integer | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `lembrete_habilitado` | boolean | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `minutos_antecedencia_lembrete` | integer | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `anotacoes` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `nome_medicamento` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `dosagem_medicamento` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `unidade_medicamento` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `unidade_personalizada_medicamento` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `via_administracao_medicamento` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `via_personalizada_medicamento` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `instrucoes_medicamento` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `lembrar_no_horario_previsto` | boolean | Sim | Não | — | Não | — | true | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `lembrete_atraso_habilitado` | boolean | Sim | Não | — | Não | — | false | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `minutos_para_atraso` | integer | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `repetir_enquanto_pendente` | boolean | Sim | Não | — | Não | — | false | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `intervalo_repeticao_minutos` | integer | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `importante` | boolean | Sim | Não | — | Não | — | false | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `notificar_responsavel_se_importante` | boolean | Sim | Não | — | Não | — | false | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias` | `exige_foto_conclusao` | boolean | Sim | Não | — | Não | — | false | — | `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias_dias_semana` | `item_copia_id` | uuid | Sim | Sim | solicitacoes_servico_itens_cuidado_copias | Não | — | — | pk_solicitacoes_servico_itens_cuidado_copias_dias_semana | coleção de `ServiceRequestCareItemSnapshot` |
| `solicitacoes_servico_itens_cuidado_copias_dias_semana` | `dia_semana` | character varying | Sim | Sim | — | Não | — | — | pk_solicitacoes_servico_itens_cuidado_copias_dias_semana | coleção de `ServiceRequestCareItemSnapshot` |
| `tarefas_cuidado` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_tarefas_cuidado | `CareTask` |
| `tarefas_cuidado` | `titulo` | character varying | Sim | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `descricao` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `categoria` | character varying | Sim | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `categoria_personalizada` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `prioridade` | character varying | Sim | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `tipo_recorrencia` | character varying | Sim | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `data_inicio` | date | Sim | Não | — | Não | ck_tarefas_cuidado_datas | — | — | `CareTask` |
| `tarefas_cuidado` | `data_fim` | date | Não | Não | — | Não | ck_tarefas_cuidado_datas | — | — | `CareTask` |
| `tarefas_cuidado` | `horario_previsto` | time without time zone | Sim | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `intervalo_dias` | integer | Não | Não | — | Não | ck_tarefas_cuidado_intervalo | — | — | `CareTask` |
| `tarefas_cuidado` | `fuso_horario` | character varying | Sim | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `lembrete_habilitado` | boolean | Sim | Não | — | Não | ck_tarefas_cuidado_lembrete | false | — | `CareTask` |
| `tarefas_cuidado` | `minutos_antecedencia_lembrete` | integer | Não | Não | — | Não | ck_tarefas_cuidado_lembrete | — | — | `CareTask` |
| `tarefas_cuidado` | `anotacoes` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `status` | character varying | Sim | Não | — | Não | — | — | idx_tarefas_cuidado_cuidador_executor_id_status_atualizado_em, idx_tarefas_cuidado_responsavel_criador_id_status_atualizado_em | `CareTask` |
| `tarefas_cuidado` | `pessoa_assistida_id` | uuid | Sim | Não | pessoas_assistidas | Não | — | — | idx_tarefas_cuidado_pessoa_assistida_id | `CareTask` |
| `tarefas_cuidado` | `contratacao_id` | uuid | Sim | Não | contratacoes | Não | — | — | idx_tarefas_cuidado_contratacao_id | `CareTask` |
| `tarefas_cuidado` | `responsavel_criador_id` | uuid | Sim | Não | usuarios | Não | — | — | idx_tarefas_cuidado_responsavel_criador_id_status_atualizado_em | `CareTask` |
| `tarefas_cuidado` | `cuidador_executor_id` | uuid | Sim | Não | usuarios | Não | — | — | idx_tarefas_cuidado_cuidador_executor_id_status_atualizado_em | `CareTask` |
| `tarefas_cuidado` | `serie_anterior_id` | uuid | Não | Não | tarefas_cuidado | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `nome_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `dosagem_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `unidade_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `unidade_personalizada_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `via_administracao_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `via_personalizada_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `instrucoes_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_tarefas_cuidado_cuidador_executor_id_status_atualizado_em, idx_tarefas_cuidado_responsavel_criador_id_status_atualizado_em | `CareTask` |
| `tarefas_cuidado` | `usuario_criacao_id` | uuid | Sim | Não | usuarios | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `usuario_atualizacao_id` | uuid | Sim | Não | usuarios | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `versao` | bigint | Sim | Não | — | Não | — | 0 | — | `CareTask` |
| `tarefas_cuidado` | `item_copia_origem_id` | uuid | Não | Não | solicitacoes_servico_itens_cuidado_copias | Sim | — | — | ux_tarefas_cuidado_item_copia_origem_id | `CareTask` |
| `tarefas_cuidado` | `lembrar_no_horario_previsto` | boolean | Sim | Não | — | Não | — | true | — | `CareTask` |
| `tarefas_cuidado` | `lembrete_atraso_habilitado` | boolean | Sim | Não | — | Não | — | false | — | `CareTask` |
| `tarefas_cuidado` | `minutos_para_atraso` | integer | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `repetir_enquanto_pendente` | boolean | Sim | Não | — | Não | — | false | — | `CareTask` |
| `tarefas_cuidado` | `intervalo_repeticao_minutos` | integer | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefas_cuidado` | `importante` | boolean | Sim | Não | — | Não | — | false | — | `CareTask` |
| `tarefas_cuidado` | `notificar_responsavel_se_importante` | boolean | Sim | Não | — | Não | — | false | — | `CareTask` |
| `tarefas_cuidado` | `exige_foto_conclusao` | boolean | Sim | Não | — | Não | — | false | — | `CareTask` |
| `tarefas_cuidado` | `tarefa_duplicada_de_id` | uuid | Não | Não | tarefas_cuidado | Não | — | — | idx_tarefas_cuidado_tarefa_duplicada_de_id | `CareTask` |
| `tarefas_cuidado_auditoria` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_tarefas_cuidado_auditoria | `TaskAuditEntry` |
| `tarefas_cuidado_auditoria` | `tarefa_id` | uuid | Sim | Não | tarefas_cuidado | Não | — | — | idx_tarefas_cuidado_auditoria_tarefa_id_criado_em | `TaskAuditEntry` |
| `tarefas_cuidado_auditoria` | `ocorrencia_id` | uuid | Não | Não | ocorrencias_cuidado | Não | — | — | — | `TaskAuditEntry` |
| `tarefas_cuidado_auditoria` | `usuario_ator_id` | uuid | Não | Não | usuarios | Não | — | — | — | `TaskAuditEntry` |
| `tarefas_cuidado_auditoria` | `acao` | character varying | Sim | Não | — | Não | — | — | — | `TaskAuditEntry` |
| `tarefas_cuidado_auditoria` | `detalhes` | character varying | Não | Não | — | Não | — | — | — | `TaskAuditEntry` |
| `tarefas_cuidado_auditoria` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_tarefas_cuidado_auditoria_tarefa_id_criado_em | `TaskAuditEntry` |
| `tarefas_cuidado_dias_semana` | `tarefa_id` | uuid | Sim | Sim | tarefas_cuidado | Não | — | — | pk_tarefas_cuidado_dias_semana | coleção de `CareTask` |
| `tarefas_cuidado_dias_semana` | `dia_semana` | character varying | Sim | Sim | — | Não | — | — | pk_tarefas_cuidado_dias_semana | coleção de `CareTask` |
| `usuarios` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_usuarios | `User` |
| `usuarios` | `data_nascimento` | date | Sim | Não | — | Não | — | — | — | `User` |
| `usuarios` | `cpf` | character varying | Sim | Não | — | Sim | — | — | uk_usuarios_cpf | `User` |
| `usuarios` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `User` |
| `usuarios` | `email` | character varying | Sim | Não | — | Sim | — | — | uk_usuarios_email | `User` |
| `usuarios` | `nome_completo` | character varying | Sim | Não | — | Não | — | — | — | `User` |
| `usuarios` | `senha_hash` | character varying | Sim | Não | — | Não | — | — | — | `User` |
| `usuarios` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `User` |
| `usuarios` | `tipo_usuario` | character varying | Sim | Não | — | Não | ck_usuarios_tipo_usuario | — | — | `User` |
| `usuarios` | `telefone` | character varying | Não | Não | — | Não | — | — | — | `User` |
| `usuarios` | `status` | character varying | Sim | Não | — | Não | — | 'ACTIVE'::character varying | — | `User` |
| `usuarios` | `url_foto_perfil` | character varying | Não | Não | — | Não | — | — | — | `User` |
| `usuarios_tokens_redefinicao_senha` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_usuarios_tokens_redefinicao_senha | `PasswordResetToken` |
| `usuarios_tokens_redefinicao_senha` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `PasswordResetToken` |
| `usuarios_tokens_redefinicao_senha` | `expira_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `PasswordResetToken` |
| `usuarios_tokens_redefinicao_senha` | `hash_token` | character varying | Sim | Não | — | Sim | — | — | uk_usuarios_tokens_redefinicao_senha_hash_token | `PasswordResetToken` |
| `usuarios_tokens_redefinicao_senha` | `usado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `PasswordResetToken` |
| `usuarios_tokens_redefinicao_senha` | `usuario_id` | uuid | Sim | Não | usuarios | Não | — | — | — | `PasswordResetToken` |
