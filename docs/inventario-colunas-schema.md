# Inventário técnico de colunas do schema `public`

Catálogo vigente após a V043. As 416 colunas estão relacionadas abaixo. As descrições funcionais por coluna estão no [dicionário principal](modelagem-banco-dados.md); este apêndice concentra metadados físicos e o mapeamento responsável.

| Tabela | Coluna | Tipo | Obrigatória | PK | FK para | Unique | Check(s) | Default | Índice(s) | JPA/infraestrutura |
|---|---|---|---:|---:|---|---:|---|---|---|---|
| `contratacao` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_contratacao | `CareContract` |
| `contratacao` | `solicitacao_servico_id` | uuid | Sim | Não | solicitacao_servico | Sim | — | — | uk_contratacao_solicitacao_servico_id | `CareContract` |
| `contratacao` | `usuario_responsavel_id` | uuid | Sim | Não | usuario | Não | — | — | idx_contratacao_usuario_responsavel_id_atualizado_em | `CareContract` |
| `contratacao` | `usuario_cuidador_id` | uuid | Sim | Não | usuario | Não | — | — | idx_contratacao_usuario_cuidador_id_status | `CareContract` |
| `contratacao` | `pessoa_assistida_id` | uuid | Sim | Não | pessoa_assistida | Não | — | — | — | `CareContract` |
| `contratacao` | `status` | character varying | Sim | Não | — | Não | — | — | idx_contratacao_status_data_fim_efetiva, idx_contratacao_usuario_cuidador_id_status | `CareContract` |
| `contratacao` | `data_inicio` | date | Sim | Não | — | Não | — | — | — | `CareContract` |
| `contratacao` | `data_fim` | date | Não | Não | — | Não | — | — | — | `CareContract` |
| `contratacao` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `CareContract` |
| `contratacao` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_contratacao_usuario_responsavel_id_atualizado_em | `CareContract` |
| `contratacao` | `motivo_cancelamento` | character varying | Não | Não | — | Não | — | — | — | `CareContract` |
| `contratacao` | `motivo_encerramento` | character varying | Não | Não | — | Não | — | — | — | `CareContract` |
| `contratacao` | `tipo_encerramento` | character varying | Não | Não | — | Não | — | — | — | `CareContract` |
| `contratacao` | `motivo_solicitacao_encerramento` | character varying | Não | Não | — | Não | — | — | — | `CareContract` |
| `contratacao` | `observacoes_encerramento` | character varying | Não | Não | — | Não | — | — | — | `CareContract` |
| `contratacao` | `usuario_solicitante_encerramento_id` | uuid | Não | Não | usuario | Não | — | — | — | `CareContract` |
| `contratacao` | `encerramento_solicitado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `CareContract` |
| `contratacao` | `data_fim_efetiva` | date | Não | Não | — | Não | — | — | idx_contratacao_status_data_fim_efetiva | `CareContract` |
| `contratacao` | `cancelado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `CareContract` |
| `contratacao` | `usuario_solicitante_cancelamento_id` | uuid | Não | Não | usuario | Não | — | — | — | `CareContract` |
| `contratacao` | `cancelamento_solicitado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `CareContract` |
| `cuidador` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_cuidador | `CaregiverProfile` |
| `cuidador` | `usuario_id` | uuid | Sim | Não | usuario | Sim | — | — | uk_cuidador_usuario_id | `CaregiverProfile` |
| `cuidador` | `formacao_outro` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `experiencia` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `biografia` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `cep` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `rua` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `numero` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `complemento` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `bairro` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `cidade` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `estado` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `ponto_referencia` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `horario_inicio` | time without time zone | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `horario_fim` | time without time zone | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `observacao` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `modalidade_outro` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `servico_outro` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `tempo_experiencia` | character varying | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `latitude` | numeric | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador` | `longitude` | numeric | Não | Não | — | Não | — | — | — | `CaregiverProfile` |
| `cuidador_disponibilidade_dia` | `perfil_cuidador_id` | uuid | Sim | Não | cuidador | Não | — | — | — | `CaregiverAvailability` |
| `cuidador_disponibilidade_dia` | `dia_semana` | character varying | Sim | Não | — | Não | — | — | — | `CaregiverAvailability` |
| `cuidador_disponibilidade_periodo` | `perfil_cuidador_id` | uuid | Sim | Não | cuidador | Não | — | — | — | `CaregiverAvailability` |
| `cuidador_disponibilidade_periodo` | `periodo` | character varying | Sim | Não | — | Não | — | — | — | `CaregiverAvailability` |
| `cuidador_formacao` | `perfil_cuidador_id` | uuid | Sim | Não | cuidador | Sim | — | — | uk_cuidador_formacao_perfil_cuidador_id_formacao | coleção de `CaregiverProfile` |
| `cuidador_formacao` | `formacao` | character varying | Sim | Não | — | Sim | — | — | uk_cuidador_formacao_perfil_cuidador_id_formacao | coleção de `CaregiverProfile` |
| `cuidador_modalidade` | `perfil_cuidador_id` | uuid | Sim | Não | cuidador | Não | — | — | — | coleção de `CaregiverProfile` |
| `cuidador_modalidade` | `modalidade` | character varying | Sim | Não | — | Não | — | — | — | coleção de `CaregiverProfile` |
| `cuidador_servico` | `perfil_cuidador_id` | uuid | Sim | Não | cuidador | Não | — | — | — | coleção de `CaregiverProfile` |
| `cuidador_servico` | `servico` | character varying | Sim | Não | — | Não | — | — | — | coleção de `CaregiverProfile` |
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
| `notificacao` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_notificacao | `Notification` |
| `notificacao` | `usuario_destinatario_id` | uuid | Sim | Não | usuario | Não | — | — | idx_notificacao_usuario_destinatario_id_removida_em_criado_em | `Notification` |
| `notificacao` | `tipo` | character varying | Sim | Não | — | Não | — | — | — | `Notification` |
| `notificacao` | `titulo` | character varying | Sim | Não | — | Não | — | — | — | `Notification` |
| `notificacao` | `mensagem` | character varying | Sim | Não | — | Não | — | — | — | `Notification` |
| `notificacao` | `tipo_entidade_relacionada` | character varying | Sim | Não | — | Não | — | — | — | `Notification` |
| `notificacao` | `entidade_relacionada_id` | uuid | Sim | Não | — | Não | — | — | — | `Notification` |
| `notificacao` | `lida_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `Notification` |
| `notificacao` | `removida_em` | timestamp with time zone | Não | Não | — | Não | — | — | idx_notificacao_usuario_destinatario_id_removida_em_criado_em | `Notification` |
| `notificacao` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_notificacao_usuario_destinatario_id_removida_em_criado_em | `Notification` |
| `notificacao` | `chave_deduplicacao` | character varying | Não | Não | — | Sim | — | — | ux_notificacao_chave_deduplicacao | `Notification` |
| `notificacao_preferencia` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_notificacao_preferencia | `UserNotificationPreference` |
| `notificacao_preferencia` | `usuario_id` | uuid | Sim | Não | usuario | Sim | — | — | idx_notificacao_preferencia_usuario_id, uk_notificacao_preferencia_usuario_id_tipo_notificacao | `UserNotificationPreference` |
| `notificacao_preferencia` | `tipo_notificacao` | character varying | Sim | Não | — | Sim | — | — | uk_notificacao_preferencia_usuario_id_tipo_notificacao | `UserNotificationPreference` |
| `notificacao_preferencia` | `habilitado` | boolean | Sim | Não | — | Não | — | — | — | `UserNotificationPreference` |
| `notificacao_preferencia` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `UserNotificationPreference` |
| `notificacao_preferencia` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `UserNotificationPreference` |
| `ocorrencia_cuidado` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_ocorrencia_cuidado | `TaskOccurrence` |
| `ocorrencia_cuidado` | `tarefa_id` | uuid | Sim | Não | tarefa_cuidado | Sim | — | — | idx_ocorrencia_cuidado_tarefa_id_data_prevista, uk_ocorrencia_cuidado_tarefa_id_data_prevista_horario_previsto, ux_ocorrencia_cuidado_contratacao_id_tarefa_id_data_pr_a365437c | `TaskOccurrence` |
| `ocorrencia_cuidado` | `contratacao_id` | uuid | Sim | Não | contratacao | Sim | — | — | idx_ocorrencia_cuidado_contratacao_id_data_prevista, ux_ocorrencia_cuidado_contratacao_id_tarefa_id_data_pr_a365437c | `TaskOccurrence` |
| `ocorrencia_cuidado` | `pessoa_assistida_id` | uuid | Sim | Não | pessoa_assistida | Não | — | — | idx_ocorrencia_cuidado_pessoa_assistida_id_data_prevista | `TaskOccurrence` |
| `ocorrencia_cuidado` | `usuario_cuidador_id` | uuid | Sim | Não | usuario | Não | — | — | idx_ocorrencia_cuidado_usuario_cuidador_id_data_prevista | `TaskOccurrence` |
| `ocorrencia_cuidado` | `data_prevista` | date | Sim | Não | — | Sim | — | — | idx_ocorrencia_cuidado_contratacao_id_data_prevista, idx_ocorrencia_cuidado_pessoa_assistida_id_data_prevista, idx_ocorrencia_cuidado_tarefa_id_data_prevista, idx_ocorrencia_cuidado_usuario_cuidador_id_data_prevista, uk_ocorrencia_cuidado_tarefa_id_data_prevista_horario_previsto, ux_ocorrencia_cuidado_contratacao_id_tarefa_id_data_pr_a365437c | `TaskOccurrence` |
| `ocorrencia_cuidado` | `horario_previsto` | time without time zone | Sim | Não | — | Sim | — | — | uk_ocorrencia_cuidado_tarefa_id_data_prevista_horario_previsto, ux_ocorrencia_cuidado_contratacao_id_tarefa_id_data_pr_a365437c | `TaskOccurrence` |
| `ocorrencia_cuidado` | `instante_previsto_utc` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_ocorrencia_cuidado_status_instante_previsto_utc | `TaskOccurrence` |
| `ocorrencia_cuidado` | `fuso_horario` | character varying | Sim | Não | — | Não | — | — | — | `TaskOccurrence` |
| `ocorrencia_cuidado` | `status` | character varying | Sim | Não | — | Não | — | — | idx_ocorrencia_cuidado_status_instante_previsto_utc | `TaskOccurrence` |
| `ocorrencia_cuidado` | `concluido_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `TaskOccurrence` |
| `ocorrencia_cuidado` | `usuario_executor_id` | uuid | Não | Não | usuario | Não | — | — | — | `TaskOccurrence` |
| `ocorrencia_cuidado` | `motivo_nao_realizacao` | character varying | Não | Não | — | Não | — | — | — | `TaskOccurrence` |
| `ocorrencia_cuidado` | `anotacao_execucao` | character varying | Não | Não | — | Não | — | — | — | `TaskOccurrence` |
| `ocorrencia_cuidado` | `cancelado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `TaskOccurrence` |
| `ocorrencia_cuidado` | `excecao` | boolean | Sim | Não | — | Não | — | false | — | `TaskOccurrence` |
| `ocorrencia_cuidado` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `TaskOccurrence` |
| `ocorrencia_cuidado` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `TaskOccurrence` |
| `ocorrencia_cuidado` | `versao` | bigint | Sim | Não | — | Não | — | 0 | — | `TaskOccurrence` |
| `ocorrencia_cuidado` | `marcada_nao_realizada_automaticamente` | boolean | Sim | Não | — | Não | — | false | — | `TaskOccurrence` |
| `ocorrencia_cuidado` | `status_atualizado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `TaskOccurrence` |
| `ocorrencia_cuidado_foto` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_ocorrencia_cuidado_foto | `CareOccurrencePhoto` |
| `ocorrencia_cuidado_foto` | `ocorrencia_id` | uuid | Não | Não | ocorrencia_cuidado | Não | ck_ocorrencia_cuidado_foto_vinculo_unico | — | idx_ocorrencia_cuidado_foto_ocorrencia_id_criado_em | `CareOccurrencePhoto` |
| `ocorrencia_cuidado_foto` | `usuario_envio_id` | uuid | Sim | Não | usuario | Não | — | — | — | `CareOccurrencePhoto` |
| `ocorrencia_cuidado_foto` | `nome_arquivo` | character varying | Sim | Não | — | Sim | — | — | uk_ocorrencia_cuidado_foto_nome_arquivo | `CareOccurrencePhoto` |
| `ocorrencia_cuidado_foto` | `nome_arquivo_original` | character varying | Não | Não | — | Não | — | — | — | `CareOccurrencePhoto` |
| `ocorrencia_cuidado_foto` | `tipo_conteudo` | character varying | Sim | Não | — | Não | — | — | — | `CareOccurrencePhoto` |
| `ocorrencia_cuidado_foto` | `tamanho_arquivo` | bigint | Sim | Não | — | Não | — | — | — | `CareOccurrencePhoto` |
| `ocorrencia_cuidado_foto` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_ocorrencia_cuidado_foto_ocorrencia_id_criado_em, idx_ocorrencia_cuidado_foto_registro_atividade_id_criado_em | `CareOccurrencePhoto` |
| `ocorrencia_cuidado_foto` | `registro_atividade_id` | uuid | Não | Não | registro_diario_cuidado | Não | ck_ocorrencia_cuidado_foto_vinculo_unico | — | idx_ocorrencia_cuidado_foto_registro_atividade_id_criado_em | `CareOccurrencePhoto` |
| `ocorrencia_cuidado_lembrete` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_ocorrencia_cuidado_lembrete | `TaskReminder` |
| `ocorrencia_cuidado_lembrete` | `ocorrencia_id` | uuid | Sim | Não | ocorrencia_cuidado | Não | — | — | idx_ocorrencia_cuidado_lembrete_ocorrencia_id_status | `TaskReminder` |
| `ocorrencia_cuidado_lembrete` | `usuario_destinatario_id` | uuid | Sim | Não | usuario | Não | — | — | — | `TaskReminder` |
| `ocorrencia_cuidado_lembrete` | `tipo_lembrete` | character varying | Sim | Não | — | Não | — | — | — | `TaskReminder` |
| `ocorrencia_cuidado_lembrete` | `previsto_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_ocorrencia_cuidado_lembrete_status_previsto_em | `TaskReminder` |
| `ocorrencia_cuidado_lembrete` | `enviado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `TaskReminder` |
| `ocorrencia_cuidado_lembrete` | `cancelado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `TaskReminder` |
| `ocorrencia_cuidado_lembrete` | `status` | character varying | Sim | Não | — | Não | — | — | idx_ocorrencia_cuidado_lembrete_ocorrencia_id_status, idx_ocorrencia_cuidado_lembrete_status_previsto_em | `TaskReminder` |
| `ocorrencia_cuidado_lembrete` | `chave_deduplicacao` | character varying | Sim | Não | — | Sim | — | — | uk_ocorrencia_cuidado_lembrete_chave_deduplicacao | `TaskReminder` |
| `ocorrencia_cuidado_lembrete` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `TaskReminder` |
| `ocorrencia_cuidado_lembrete` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `TaskReminder` |
| `pessoa_assistida` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_pessoa_assistida | `AssistedPerson` |
| `pessoa_assistida` | `usuario_responsavel_id` | uuid | Sim | Não | usuario | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `nome` | character varying | Sim | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `cpf` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `data_nascimento` | date | Sim | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `grau_dependencia` | character varying | Sim | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `mobilidade` | character varying | Sim | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `mobilidade_outro` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `alergias_outro` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `alergias_detalhes` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `restricoes_alimentares_outro` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `restricoes_alimentares_detalhes` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `medicamentos` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `observacoes` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `cep` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `rua` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `numero` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `complemento` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `bairro` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `cidade` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `estado` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `ponto_referencia` | character varying | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `latitude` | numeric | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida` | `longitude` | numeric | Não | Não | — | Não | — | — | — | `AssistedPerson` |
| `pessoa_assistida_alergia` | `pessoa_assistida_id` | uuid | Sim | Não | pessoa_assistida | Não | — | — | — | coleção de `AssistedPerson` |
| `pessoa_assistida_alergia` | `alergia` | character varying | Sim | Não | — | Não | — | — | — | coleção de `AssistedPerson` |
| `pessoa_assistida_contato_emergencia` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_pessoa_assistida_contato_emergencia | `EmergencyContact` |
| `pessoa_assistida_contato_emergencia` | `pessoa_assistida_id` | uuid | Sim | Não | pessoa_assistida | Sim | — | — | uk_pessoa_assistida_contato_emergencia_pessoa_assistida_id | `EmergencyContact` |
| `pessoa_assistida_contato_emergencia` | `nome` | character varying | Sim | Não | — | Não | — | — | — | `EmergencyContact` |
| `pessoa_assistida_contato_emergencia` | `telefone` | character varying | Sim | Não | — | Não | — | — | — | `EmergencyContact` |
| `pessoa_assistida_contato_emergencia` | `vinculo` | character varying | Sim | Não | — | Não | — | — | — | `EmergencyContact` |
| `pessoa_assistida_contato_emergencia` | `contato_responsavel` | boolean | Sim | Não | — | Não | — | false | — | `EmergencyContact` |
| `pessoa_assistida_contato_emergencia` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `EmergencyContact` |
| `pessoa_assistida_contato_emergencia` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `EmergencyContact` |
| `pessoa_assistida_restricao_alimentar` | `pessoa_assistida_id` | uuid | Sim | Não | pessoa_assistida | Não | — | — | — | coleção de `AssistedPerson` |
| `pessoa_assistida_restricao_alimentar` | `restricao` | character varying | Sim | Não | — | Não | — | — | — | coleção de `AssistedPerson` |
| `registro_atendimento` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_registro_atendimento | `ServiceAttendanceRecord` |
| `registro_atendimento` | `contratacao_id` | uuid | Sim | Não | contratacao | Sim | — | — | idx_registro_atendimento_contratacao_id_data_atendimen_4c3ec664, uk_registro_atendimento_contratacao_id_data_atendiment_fc5cf1c5 | `ServiceAttendanceRecord` |
| `registro_atendimento` | `cuidador_id` | uuid | Sim | Não | usuario | Não | — | — | idx_registro_atendimento_cuidador_id_data_atendimento | `ServiceAttendanceRecord` |
| `registro_atendimento` | `responsavel_id` | uuid | Sim | Não | usuario | Não | — | — | — | `ServiceAttendanceRecord` |
| `registro_atendimento` | `pessoa_assistida_id` | uuid | Sim | Não | pessoa_assistida | Não | — | — | — | `ServiceAttendanceRecord` |
| `registro_atendimento` | `data_atendimento` | date | Sim | Não | — | Sim | — | — | idx_registro_atendimento_contratacao_id_data_atendimen_4c3ec664, idx_registro_atendimento_cuidador_id_data_atendimento, uk_registro_atendimento_contratacao_id_data_atendiment_fc5cf1c5 | `ServiceAttendanceRecord` |
| `registro_atendimento` | `tipo_registro` | character varying | Sim | Não | — | Sim | ck_registro_atendimento_tipo | — | uk_registro_atendimento_contratacao_id_data_atendiment_fc5cf1c5 | `ServiceAttendanceRecord` |
| `registro_atendimento` | `registrado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_registro_atendimento_contratacao_id_data_atendimen_4c3ec664 | `ServiceAttendanceRecord` |
| `registro_atendimento` | `latitude` | double precision | Sim | Não | — | Não | ck_registro_atendimento_latitude | — | — | `ServiceAttendanceRecord` |
| `registro_atendimento` | `longitude` | double precision | Sim | Não | — | Não | ck_registro_atendimento_longitude | — | — | `ServiceAttendanceRecord` |
| `registro_atendimento` | `precisao` | double precision | Sim | Não | — | Não | ck_registro_atendimento_precisao | — | — | `ServiceAttendanceRecord` |
| `registro_atendimento` | `localizacao_capturada_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registro_atendimento` | `endereco_registrado` | character varying | Não | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registro_atendimento` | `fuso_dispositivo` | character varying | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registro_atendimento` | `horario_inicio_previsto` | time without time zone | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registro_atendimento` | `horario_fim_previsto` | time without time zone | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registro_atendimento` | `janela_permitida_inicio` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registro_atendimento` | `janela_permitida_fim` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registro_atendimento` | `dentro_janela_permitida` | boolean | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registro_atendimento` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registro_atendimento` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ServiceAttendanceRecord` |
| `registro_diario_cuidado` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_registro_diario_cuidado | `CareActivityRecord` |
| `registro_diario_cuidado` | `ocorrencia_id` | uuid | Não | Não | ocorrencia_cuidado | Sim | — | — | uk_registro_diario_cuidado_ocorrencia_id | `CareActivityRecord` |
| `registro_diario_cuidado` | `contratacao_id` | uuid | Sim | Não | contratacao | Não | — | — | idx_registro_diario_cuidado_contratacao_id_data_regist_b64ac750 | `CareActivityRecord` |
| `registro_diario_cuidado` | `pessoa_assistida_id` | uuid | Sim | Não | pessoa_assistida | Não | — | — | — | `CareActivityRecord` |
| `registro_diario_cuidado` | `usuario_responsavel_id` | uuid | Sim | Não | usuario | Não | — | — | idx_registro_diario_cuidado_usuario_responsavel_id_dat_18645e03 | `CareActivityRecord` |
| `registro_diario_cuidado` | `usuario_cuidador_id` | uuid | Sim | Não | usuario | Não | — | — | idx_registro_diario_cuidado_usuario_cuidador_id_data_r_e12a85a0 | `CareActivityRecord` |
| `registro_diario_cuidado` | `tipo_atividade` | character varying | Sim | Não | — | Não | — | — | — | `CareActivityRecord` |
| `registro_diario_cuidado` | `titulo` | character varying | Sim | Não | — | Não | — | — | — | `CareActivityRecord` |
| `registro_diario_cuidado` | `anotacoes` | character varying | Não | Não | — | Não | — | — | — | `CareActivityRecord` |
| `registro_diario_cuidado` | `ocorrido_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_registro_diario_cuidado_contratacao_id_data_regist_b64ac750, idx_registro_diario_cuidado_usuario_cuidador_id_data_r_e12a85a0, idx_registro_diario_cuidado_usuario_responsavel_id_dat_18645e03 | `CareActivityRecord` |
| `registro_diario_cuidado` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `CareActivityRecord` |
| `registro_diario_cuidado` | `tipo_origem` | character varying | Sim | Não | — | Não | — | 'PLANNED'::character varying | — | `CareActivityRecord` |
| `registro_diario_cuidado` | `data_registro` | date | Sim | Não | — | Não | — | — | idx_registro_diario_cuidado_contratacao_id_data_regist_b64ac750, idx_registro_diario_cuidado_usuario_cuidador_id_data_r_e12a85a0, idx_registro_diario_cuidado_usuario_responsavel_id_dat_18645e03 | `CareActivityRecord` |
| `registro_diario_cuidado` | `fuso_horario` | character varying | Sim | Não | — | Não | — | — | — | `CareActivityRecord` |
| `registro_diario_cuidado` | `tipo_cuidado` | character varying | Sim | Não | — | Não | — | — | — | `CareActivityRecord` |
| `registro_diario_cuidado` | `descricao` | character varying | Não | Não | — | Não | — | — | — | `CareActivityRecord` |
| `registro_diario_cuidado` | `importante` | boolean | Sim | Não | — | Não | — | false | — | `CareActivityRecord` |
| `registro_diario_cuidado` | `usuario_criacao_id` | uuid | Sim | Não | usuario | Não | — | — | — | `CareActivityRecord` |
| `relatorio_atendimento` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_relatorio_atendimento | `AttendanceReport` |
| `relatorio_atendimento` | `contratacao_id` | uuid | Sim | Não | contratacao | Sim | — | — | uk_relatorio_atendimento_contratacao_id_data_atendimento | `AttendanceReport` |
| `relatorio_atendimento` | `data_atendimento` | date | Sim | Não | — | Sim | — | — | idx_relatorio_atendimento_responsavel_id_cuidador_id_d_b3f71a9d, uk_relatorio_atendimento_contratacao_id_data_atendimento | `AttendanceReport` |
| `relatorio_atendimento` | `registro_inicio_atendimento_id` | uuid | Sim | Não | registro_atendimento | Não | — | — | — | `AttendanceReport` |
| `relatorio_atendimento` | `registro_fim_atendimento_id` | uuid | Sim | Não | registro_atendimento | Não | — | — | — | `AttendanceReport` |
| `relatorio_atendimento` | `cuidador_id` | uuid | Sim | Não | usuario | Não | — | — | idx_relatorio_atendimento_responsavel_id_cuidador_id_d_b3f71a9d | `AttendanceReport` |
| `relatorio_atendimento` | `responsavel_id` | uuid | Sim | Não | usuario | Não | — | — | idx_relatorio_atendimento_responsavel_id_cuidador_id_d_b3f71a9d | `AttendanceReport` |
| `relatorio_atendimento` | `pessoa_assistida_id` | uuid | Sim | Não | pessoa_assistida | Não | — | — | — | `AttendanceReport` |
| `relatorio_atendimento` | `texto_gerado` | text | Sim | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorio_atendimento` | `texto_editado` | text | Não | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorio_atendimento` | `texto_final` | text | Não | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorio_atendimento` | `observacoes_adicionais` | character varying | Não | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorio_atendimento` | `anotacoes_enfermagem` | text | Sim | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorio_atendimento` | `status` | character varying | Sim | Não | — | Não | ck_relatorio_atendimento_status | — | — | `AttendanceReport` |
| `relatorio_atendimento` | `status_email` | character varying | Sim | Não | — | Não | ck_relatorio_atendimento_status_email | 'NOT_SENT'::character varying | idx_relatorio_atendimento_status_email_proxima_tentati_88c55cf7 | `AttendanceReport` |
| `relatorio_atendimento` | `email_enviado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorio_atendimento` | `mensagem_erro_email` | character varying | Não | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorio_atendimento` | `gerado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorio_atendimento` | `editado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorio_atendimento` | `finalizado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorio_atendimento` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorio_atendimento` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `AttendanceReport` |
| `relatorio_atendimento` | `email_solicitado_em` | timestamp with time zone | Não | Não | — | Não | — | — | idx_relatorio_atendimento_status_email_proxima_tentati_88c55cf7 | `AttendanceReport` |
| `relatorio_atendimento` | `tentativas_email` | integer | Sim | Não | — | Não | — | 0 | — | `AttendanceReport` |
| `relatorio_atendimento` | `proxima_tentativa_email_em` | timestamp with time zone | Não | Não | — | Não | — | — | idx_relatorio_atendimento_status_email_proxima_tentati_88c55cf7 | `AttendanceReport` |
| `responsavel` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_responsavel | `ResponsibleProfile` |
| `responsavel` | `usuario_id` | uuid | Sim | Não | usuario | Sim | — | — | uk_responsavel_usuario_id | `ResponsibleProfile` |
| `responsavel` | `parentesco` | character varying | Sim | Não | — | Não | — | — | — | `ResponsibleProfile` |
| `responsavel` | `parentesco_outro` | character varying | Não | Não | — | Não | — | — | — | `ResponsibleProfile` |
| `responsavel` | `preferencia_contato` | character varying | Sim | Não | — | Não | — | — | — | `ResponsibleProfile` |
| `responsavel` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ResponsibleProfile` |
| `responsavel` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ResponsibleProfile` |
| `rotina_cuidado` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_rotina_cuidado | `CareRoutine` |
| `rotina_cuidado` | `usuario_responsavel_id` | uuid | Sim | Não | usuario | Não | — | — | idx_rotina_cuidado_usuario_responsavel_id_atualizado_em | `CareRoutine` |
| `rotina_cuidado` | `pessoa_assistida_id` | uuid | Não | Não | pessoa_assistida | Não | — | — | idx_rotina_cuidado_pessoa_assistida_id_ativo | `CareRoutine` |
| `rotina_cuidado` | `nome` | character varying | Sim | Não | — | Não | — | — | — | `CareRoutine` |
| `rotina_cuidado` | `descricao` | character varying | Não | Não | — | Não | — | — | — | `CareRoutine` |
| `rotina_cuidado` | `ativo` | boolean | Sim | Não | — | Não | — | true | idx_rotina_cuidado_pessoa_assistida_id_ativo | `CareRoutine` |
| `rotina_cuidado` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `CareRoutine` |
| `rotina_cuidado` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_rotina_cuidado_usuario_responsavel_id_atualizado_em | `CareRoutine` |
| `rotina_cuidado_item` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_rotina_cuidado_item | `CareRoutineItem` |
| `rotina_cuidado_item` | `rotina_cuidado_id` | uuid | Sim | Não | rotina_cuidado | Não | — | — | idx_rotina_cuidado_item_rotina_cuidado_id_ordem_exibicao | `CareRoutineItem` |
| `rotina_cuidado_item` | `titulo` | character varying | Sim | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `descricao` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `ordem_exibicao` | integer | Sim | Não | — | Não | — | — | idx_rotina_cuidado_item_rotina_cuidado_id_ordem_exibicao | `CareRoutineItem` |
| `rotina_cuidado_item` | `ativo` | boolean | Sim | Não | — | Não | — | true | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `categoria` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `categoria_personalizada` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `prioridade` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `tipo_recorrencia` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `horario_previsto` | time without time zone | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `intervalo_dias` | integer | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `lembrete_habilitado` | boolean | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `minutos_antecedencia_lembrete` | integer | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `anotacoes` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `nome_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `dosagem_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `unidade_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `unidade_personalizada_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `via_administracao_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `via_personalizada_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `instrucoes_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `lembrar_no_horario_previsto` | boolean | Sim | Não | — | Não | — | true | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `lembrete_atraso_habilitado` | boolean | Sim | Não | — | Não | — | false | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `minutos_para_atraso` | integer | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `repetir_enquanto_pendente` | boolean | Sim | Não | — | Não | — | false | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `intervalo_repeticao_minutos` | integer | Não | Não | — | Não | — | — | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `importante` | boolean | Sim | Não | — | Não | — | false | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `notificar_responsavel_se_importante` | boolean | Sim | Não | — | Não | — | false | — | `CareRoutineItem` |
| `rotina_cuidado_item` | `exige_foto_conclusao` | boolean | Sim | Não | — | Não | — | false | — | `CareRoutineItem` |
| `rotina_cuidado_item_dia_semana` | `item_rotina_cuidado_id` | uuid | Sim | Sim | rotina_cuidado_item | Não | — | — | pk_rotina_cuidado_item_dia_semana | coleção de `CareRoutineItem` |
| `rotina_cuidado_item_dia_semana` | `dia_semana` | character varying | Sim | Sim | — | Não | — | — | pk_rotina_cuidado_item_dia_semana | coleção de `CareRoutineItem` |
| `solicitacao_servico` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_solicitacao_servico | `ServiceRequest` |
| `solicitacao_servico` | `usuario_responsavel_id` | uuid | Sim | Não | usuario | Não | — | — | idx_solicitacao_servico_usuario_responsavel_id_atualizado_em, idx_solicitacao_servico_usuario_responsavel_id_status, idx_solicitacao_servico_usuario_responsavel_id_usuario_5464ea4f | `ServiceRequest` |
| `solicitacao_servico` | `usuario_cuidador_id` | uuid | Não | Não | usuario | Sim | — | — | idx_solicitacao_servico_oportunidade_origem_id_usuario_093350b0, idx_solicitacao_servico_usuario_responsavel_id_usuario_5464ea4f, ux_solicitacao_servico_oportunidade_origem_id_usuario__1abc0dd7 | `ServiceRequest` |
| `solicitacao_servico` | `pessoa_assistida_id` | uuid | Sim | Não | pessoa_assistida | Não | — | — | idx_solicitacao_servico_usuario_responsavel_id_usuario_5464ea4f | `ServiceRequest` |
| `solicitacao_servico` | `tipo_contratacao` | character varying | Sim | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacao_servico` | `status` | character varying | Sim | Não | — | Não | — | — | idx_solicitacao_servico_status_iniciado_por_criado_em, idx_solicitacao_servico_usuario_responsavel_id_status, idx_solicitacao_servico_usuario_responsavel_id_usuario_5464ea4f | `ServiceRequest` |
| `solicitacao_servico` | `data_inicio` | date | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacao_servico` | `data_fim` | date | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacao_servico` | `descricao_necessidades` | character varying | Sim | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacao_servico` | `outra_atividade` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacao_servico` | `observacoes_adicionais` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacao_servico` | `observacoes_negociacao` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacao_servico` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_solicitacao_servico_status_iniciado_por_criado_em | `ServiceRequest` |
| `solicitacao_servico` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_solicitacao_servico_usuario_responsavel_id_atualizado_em | `ServiceRequest` |
| `solicitacao_servico` | `expira_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacao_servico` | `cancelado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacao_servico` | `motivo_rejeicao` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacao_servico` | `motivo_cancelamento` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacao_servico` | `rotina_cuidado_id` | uuid | Não | Não | rotina_cuidado | Não | — | — | — | `ServiceRequest` |
| `solicitacao_servico` | `nome_rotina_copia` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequest` |
| `solicitacao_servico` | `iniciado_por` | character varying | Sim | Não | — | Não | — | 'RESPONSIBLE'::character varying | idx_solicitacao_servico_status_iniciado_por_criado_em | `ServiceRequest` |
| `solicitacao_servico` | `usuario_solicitante_id` | uuid | Sim | Não | usuario | Não | — | — | — | `ServiceRequest` |
| `solicitacao_servico` | `oportunidade_origem_id` | uuid | Não | Não | solicitacao_servico | Sim | — | — | idx_solicitacao_servico_oportunidade_origem_id_usuario_093350b0, ux_solicitacao_servico_oportunidade_origem_id_usuario__1abc0dd7 | `ServiceRequest` |
| `solicitacao_servico_agenda_dia` | `solicitacao_servico_id` | uuid | Sim | Sim | solicitacao_servico | Não | — | — | pk_solicitacao_servico_agenda_dia | coleção de `ServiceRequest` |
| `solicitacao_servico_agenda_dia` | `dia_semana` | character varying | Sim | Sim | — | Não | — | — | pk_solicitacao_servico_agenda_dia | coleção de `ServiceRequest` |
| `solicitacao_servico_agenda_dia` | `horario_inicio` | time without time zone | Sim | Não | — | Não | — | — | — | coleção de `ServiceRequest` |
| `solicitacao_servico_agenda_dia` | `horario_fim` | time without time zone | Sim | Não | — | Não | — | — | — | coleção de `ServiceRequest` |
| `solicitacao_servico_atividade` | `solicitacao_servico_id` | uuid | Sim | Sim | solicitacao_servico | Não | — | — | pk_solicitacao_servico_atividade | coleção de `ServiceRequest` |
| `solicitacao_servico_atividade` | `atividade` | character varying | Sim | Sim | — | Não | — | — | pk_solicitacao_servico_atividade | coleção de `ServiceRequest` |
| `solicitacao_servico_contratacao_historico_status` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_solicitacao_servico_contratacao_historico_status | `StatusHistory` |
| `solicitacao_servico_contratacao_historico_status` | `tipo_entidade` | character varying | Sim | Não | — | Não | — | — | idx_solicitacao_servico_contratacao_historico_status_t_f7eac776 | `StatusHistory` |
| `solicitacao_servico_contratacao_historico_status` | `entidade_id` | uuid | Sim | Não | — | Não | — | — | idx_solicitacao_servico_contratacao_historico_status_t_f7eac776 | `StatusHistory` |
| `solicitacao_servico_contratacao_historico_status` | `status_anterior` | character varying | Não | Não | — | Não | — | — | — | `StatusHistory` |
| `solicitacao_servico_contratacao_historico_status` | `novo_status` | character varying | Sim | Não | — | Não | — | — | — | `StatusHistory` |
| `solicitacao_servico_contratacao_historico_status` | `usuario_alteracao_id` | uuid | Não | Não | usuario | Não | — | — | — | `StatusHistory` |
| `solicitacao_servico_contratacao_historico_status` | `motivo` | character varying | Não | Não | — | Não | — | — | — | `StatusHistory` |
| `solicitacao_servico_contratacao_historico_status` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_solicitacao_servico_contratacao_historico_status_t_f7eac776 | `StatusHistory` |
| `solicitacao_servico_data` | `solicitacao_servico_id` | uuid | Sim | Sim | solicitacao_servico | Não | — | — | pk_solicitacao_servico_data | coleção de `ServiceRequest` |
| `solicitacao_servico_data` | `data_servico` | date | Sim | Sim | — | Não | — | — | pk_solicitacao_servico_data | coleção de `ServiceRequest` |
| `solicitacao_servico_item_cuidado_copia` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_solicitacao_servico_item_cuidado_copia | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `solicitacao_servico_id` | uuid | Sim | Não | solicitacao_servico | Não | — | — | idx_solicitacao_servico_item_cuidado_copia_solicitacao_4519f23e | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `rotina_cuidado_original_id` | uuid | Sim | Não | rotina_cuidado | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `item_rotina_cuidado_original_id` | uuid | Não | Não | rotina_cuidado_item | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `titulo` | character varying | Sim | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `descricao` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `ordem_exibicao` | integer | Sim | Não | — | Não | — | — | idx_solicitacao_servico_item_cuidado_copia_solicitacao_4519f23e | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `categoria` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `categoria_personalizada` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `prioridade` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `tipo_recorrencia` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `horario_previsto` | time without time zone | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `intervalo_dias` | integer | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `lembrete_habilitado` | boolean | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `minutos_antecedencia_lembrete` | integer | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `anotacoes` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `nome_medicamento` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `dosagem_medicamento` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `unidade_medicamento` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `unidade_personalizada_medicamento` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `via_administracao_medicamento` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `via_personalizada_medicamento` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `instrucoes_medicamento` | character varying | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `lembrar_no_horario_previsto` | boolean | Sim | Não | — | Não | — | true | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `lembrete_atraso_habilitado` | boolean | Sim | Não | — | Não | — | false | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `minutos_para_atraso` | integer | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `repetir_enquanto_pendente` | boolean | Sim | Não | — | Não | — | false | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `intervalo_repeticao_minutos` | integer | Não | Não | — | Não | — | — | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `importante` | boolean | Sim | Não | — | Não | — | false | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `notificar_responsavel_se_importante` | boolean | Sim | Não | — | Não | — | false | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia` | `exige_foto_conclusao` | boolean | Sim | Não | — | Não | — | false | — | `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia_dia_semana` | `item_copia_id` | uuid | Sim | Sim | solicitacao_servico_item_cuidado_copia | Não | — | — | pk_solicitacao_servico_item_cuidado_copia_dia_semana | coleção de `ServiceRequestCareItemSnapshot` |
| `solicitacao_servico_item_cuidado_copia_dia_semana` | `dia_semana` | character varying | Sim | Sim | — | Não | — | — | pk_solicitacao_servico_item_cuidado_copia_dia_semana | coleção de `ServiceRequestCareItemSnapshot` |
| `tarefa_cuidado` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_tarefa_cuidado | `CareTask` |
| `tarefa_cuidado` | `titulo` | character varying | Sim | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `descricao` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `categoria` | character varying | Sim | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `categoria_personalizada` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `prioridade` | character varying | Sim | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `tipo_recorrencia` | character varying | Sim | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `data_inicio` | date | Sim | Não | — | Não | ck_tarefa_cuidado_datas | — | — | `CareTask` |
| `tarefa_cuidado` | `data_fim` | date | Não | Não | — | Não | ck_tarefa_cuidado_datas | — | — | `CareTask` |
| `tarefa_cuidado` | `horario_previsto` | time without time zone | Sim | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `intervalo_dias` | integer | Não | Não | — | Não | ck_tarefa_cuidado_intervalo | — | — | `CareTask` |
| `tarefa_cuidado` | `fuso_horario` | character varying | Sim | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `lembrete_habilitado` | boolean | Sim | Não | — | Não | ck_tarefa_cuidado_lembrete | false | — | `CareTask` |
| `tarefa_cuidado` | `minutos_antecedencia_lembrete` | integer | Não | Não | — | Não | ck_tarefa_cuidado_lembrete | — | — | `CareTask` |
| `tarefa_cuidado` | `anotacoes` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `status` | character varying | Sim | Não | — | Não | — | — | idx_tarefa_cuidado_cuidador_executor_id_status_atualizado_em, idx_tarefa_cuidado_responsavel_criador_id_status_atualizado_em | `CareTask` |
| `tarefa_cuidado` | `pessoa_assistida_id` | uuid | Sim | Não | pessoa_assistida | Não | — | — | idx_tarefa_cuidado_pessoa_assistida_id | `CareTask` |
| `tarefa_cuidado` | `contratacao_id` | uuid | Sim | Não | contratacao | Não | — | — | idx_tarefa_cuidado_contratacao_id | `CareTask` |
| `tarefa_cuidado` | `responsavel_criador_id` | uuid | Sim | Não | usuario | Não | — | — | idx_tarefa_cuidado_responsavel_criador_id_status_atualizado_em | `CareTask` |
| `tarefa_cuidado` | `cuidador_executor_id` | uuid | Sim | Não | usuario | Não | — | — | idx_tarefa_cuidado_cuidador_executor_id_status_atualizado_em | `CareTask` |
| `tarefa_cuidado` | `serie_anterior_id` | uuid | Não | Não | tarefa_cuidado | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `nome_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `dosagem_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `unidade_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `unidade_personalizada_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `via_administracao_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `via_personalizada_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `instrucoes_medicamento` | character varying | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_tarefa_cuidado_cuidador_executor_id_status_atualizado_em, idx_tarefa_cuidado_responsavel_criador_id_status_atualizado_em | `CareTask` |
| `tarefa_cuidado` | `usuario_criacao_id` | uuid | Sim | Não | usuario | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `usuario_atualizacao_id` | uuid | Sim | Não | usuario | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `versao` | bigint | Sim | Não | — | Não | — | 0 | — | `CareTask` |
| `tarefa_cuidado` | `item_copia_origem_id` | uuid | Não | Não | solicitacao_servico_item_cuidado_copia | Sim | — | — | ux_tarefa_cuidado_item_copia_origem_id | `CareTask` |
| `tarefa_cuidado` | `lembrar_no_horario_previsto` | boolean | Sim | Não | — | Não | — | true | — | `CareTask` |
| `tarefa_cuidado` | `lembrete_atraso_habilitado` | boolean | Sim | Não | — | Não | — | false | — | `CareTask` |
| `tarefa_cuidado` | `minutos_para_atraso` | integer | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `repetir_enquanto_pendente` | boolean | Sim | Não | — | Não | — | false | — | `CareTask` |
| `tarefa_cuidado` | `intervalo_repeticao_minutos` | integer | Não | Não | — | Não | — | — | — | `CareTask` |
| `tarefa_cuidado` | `importante` | boolean | Sim | Não | — | Não | — | false | — | `CareTask` |
| `tarefa_cuidado` | `notificar_responsavel_se_importante` | boolean | Sim | Não | — | Não | — | false | — | `CareTask` |
| `tarefa_cuidado` | `exige_foto_conclusao` | boolean | Sim | Não | — | Não | — | false | — | `CareTask` |
| `tarefa_cuidado` | `tarefa_duplicada_de_id` | uuid | Não | Não | tarefa_cuidado | Não | — | — | idx_tarefa_cuidado_tarefa_duplicada_de_id | `CareTask` |
| `tarefa_cuidado_auditoria` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_tarefa_cuidado_auditoria | `TaskAuditEntry` |
| `tarefa_cuidado_auditoria` | `tarefa_id` | uuid | Sim | Não | tarefa_cuidado | Não | — | — | idx_tarefa_cuidado_auditoria_tarefa_id_criado_em | `TaskAuditEntry` |
| `tarefa_cuidado_auditoria` | `ocorrencia_id` | uuid | Não | Não | ocorrencia_cuidado | Não | — | — | — | `TaskAuditEntry` |
| `tarefa_cuidado_auditoria` | `usuario_ator_id` | uuid | Não | Não | usuario | Não | — | — | — | `TaskAuditEntry` |
| `tarefa_cuidado_auditoria` | `acao` | character varying | Sim | Não | — | Não | — | — | — | `TaskAuditEntry` |
| `tarefa_cuidado_auditoria` | `detalhes` | character varying | Não | Não | — | Não | — | — | — | `TaskAuditEntry` |
| `tarefa_cuidado_auditoria` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | idx_tarefa_cuidado_auditoria_tarefa_id_criado_em | `TaskAuditEntry` |
| `tarefa_cuidado_dia_semana` | `tarefa_id` | uuid | Sim | Sim | tarefa_cuidado | Não | — | — | pk_tarefa_cuidado_dia_semana | coleção de `CareTask` |
| `tarefa_cuidado_dia_semana` | `dia_semana` | character varying | Sim | Sim | — | Não | — | — | pk_tarefa_cuidado_dia_semana | coleção de `CareTask` |
| `usuario` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_usuario | `User` |
| `usuario` | `data_nascimento` | date | Sim | Não | — | Não | — | — | — | `User` |
| `usuario` | `cpf` | character varying | Sim | Não | — | Sim | — | — | uk_usuario_cpf | `User` |
| `usuario` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `User` |
| `usuario` | `email` | character varying | Sim | Não | — | Sim | — | — | uk_usuario_email | `User` |
| `usuario` | `nome_completo` | character varying | Sim | Não | — | Não | — | — | — | `User` |
| `usuario` | `senha_hash` | character varying | Sim | Não | — | Não | — | — | — | `User` |
| `usuario` | `atualizado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `User` |
| `usuario` | `tipo_usuario` | character varying | Sim | Não | — | Não | ck_usuario_tipo_usuario | — | — | `User` |
| `usuario` | `telefone` | character varying | Não | Não | — | Não | — | — | — | `User` |
| `usuario` | `status` | character varying | Sim | Não | — | Não | — | 'ACTIVE'::character varying | — | `User` |
| `usuario` | `url_foto_perfil` | character varying | Não | Não | — | Não | — | — | — | `User` |
| `usuario_token_redefinicao_senha` | `id` | uuid | Sim | Sim | — | Não | — | — | pk_usuario_token_redefinicao_senha | `PasswordResetToken` |
| `usuario_token_redefinicao_senha` | `criado_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `PasswordResetToken` |
| `usuario_token_redefinicao_senha` | `expira_em` | timestamp with time zone | Sim | Não | — | Não | — | — | — | `PasswordResetToken` |
| `usuario_token_redefinicao_senha` | `hash_token` | character varying | Sim | Não | — | Sim | — | — | uk_usuario_token_redefinicao_senha_hash_token | `PasswordResetToken` |
| `usuario_token_redefinicao_senha` | `usado_em` | timestamp with time zone | Não | Não | — | Não | — | — | — | `PasswordResetToken` |
| `usuario_token_redefinicao_senha` | `usuario_id` | uuid | Sim | Não | usuario | Não | — | — | — | `PasswordResetToken` |
