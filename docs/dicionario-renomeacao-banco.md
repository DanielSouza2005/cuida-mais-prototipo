# Dicionário de renomeação do banco

Este documento registra a primeira etapa de tradução, entre as migrations V031–V038. A organização relacional da V039, inclusive para tabelas que mantiveram o nome, está em [dicionario-nomenclatura-relacional.md](dicionario-nomenclatura-relacional.md). Os cinco artefatos de grupos/itens identificados como legados nesta visão histórica foram removidos pela V040; a V041 traduz as três colunas físicas que ainda continham `snapshot`; e a V042 consolida a formação na coleção normalizada e remove `cuidadores.formacao`. O schema vigente está em [modelagem-banco-dados.md](modelagem-banco-dados.md). Os identificadores físicos usam `snake_case`, sem acentos. Nomes Java, propriedades JSON e endpoints REST foram preservados nessa etapa histórica.

## Tabelas

| Nome antigo | Nome físico novo | Nome lógico no TCC | Finalidade |
|---|---|---|---|
| `users` | `usuarios` | Usuários | Autenticação e dados comuns dos usuários. |
| `password_reset_tokens` | `tokens_redefinicao_senha` | Tokens de redefinição de senha | Tokens temporários de recuperação de acesso. |
| `responsible_profiles` | `perfis_responsaveis` | Perfis de responsáveis | Dados específicos do responsável. |
| `caregiver_profiles` | `perfis_cuidadores` | Perfis de cuidadores | Dados profissionais e endereço do cuidador. |
| `assisted_persons` | `pessoas_assistidas` | Pessoas assistidas | Dados pessoais e de cuidado da pessoa atendida. |
| `emergency_contacts` | `contatos_emergencia` | Contatos de emergência | Contato de emergência da pessoa assistida. |
| `assisted_person_allergies` | `alergias_pessoas_assistidas` | Alergias das pessoas assistidas | Valores multivalorados de alergias. |
| `assisted_person_food_restrictions` | `restricoes_alimentares_pessoas_assistidas` | Restrições alimentares | Valores multivalorados de restrições alimentares. |
| `caregiver_modalities` | `modalidades_cuidadores` | Modalidades dos cuidadores | Modalidades de atendimento oferecidas. |
| `caregiver_services` | `servicos_cuidadores` | Serviços dos cuidadores | Serviços oferecidos pelo cuidador. |
| `caregiver_availability_days` | `dias_disponibilidade_cuidadores` | Dias de disponibilidade | Dias disponíveis do cuidador. |
| `caregiver_availability_periods` | `periodos_disponibilidade_cuidadores` | Períodos de disponibilidade | Períodos disponíveis do cuidador. |
| `caregiver_formations` | `formacoes_cuidadores` | Formações dos cuidadores | Formações profissionais do cuidador. |
| `service_requests` | `solicitacoes_servico` | Solicitações de serviço | Solicitações diretas e oportunidades publicadas. |
| `service_request_dates` | `datas_solicitacoes_servico` | Datas das solicitações | Datas específicas de uma solicitação. |
| `service_request_schedule_days` | `dias_agenda_solicitacoes_servico` | Dias da agenda da solicitação | Grade semanal e horários solicitados. |
| `service_request_activities` | `atividades_solicitacoes_servico` | Atividades das solicitações | Atividades necessárias no serviço. |
| `notifications` | `notificacoes` | Notificações | Eventos internos destinados aos usuários. |
| `care_contracts` | `contratacoes` | Contratações | Vínculos formalizados entre responsável e cuidador. |
| `status_history` | `historico_status` | Histórico de status | Auditoria das transições de solicitação e contratação. |
| `care_tasks` | `tarefas_cuidado` | Tarefas de cuidado | Séries de cuidados planejados. |
| `care_task_weekdays` | `dias_semana_tarefas_cuidado` | Dias das tarefas | Dias semanais de recorrência das tarefas. |
| `task_occurrences` | `ocorrencias_tarefas` | Ocorrências de tarefas | Execuções previstas de cada tarefa. |
| `care_activity_records` | `registros_atividades_cuidado` | Registros de atividades de cuidado | Diário consolidado de cuidados planejados e avulsos. |
| `care_task_audit` | `auditoria_tarefas_cuidado` | Auditoria das tarefas | Ações realizadas sobre tarefas e ocorrências. |
| `care_routines` | `rotinas_cuidado` | Rotinas de cuidado | Modelos reutilizáveis de rotina. |
| `care_routine_items` | `itens_rotinas_cuidado` | Itens das rotinas de cuidado | Itens estruturados de uma rotina. |
| `service_request_care_items_snapshot` | `copias_itens_rotina_solicitacoes` | Cópias dos itens solicitados | Cópia imutável dos cuidados na contratação. |
| `care_groups` | `grupos_cuidado` | Grupos de cuidado legados | Estrutura legada de agrupamento de cuidados. |
| `care_group_items` | `itens_grupos_cuidado` | Itens dos grupos de cuidado | Itens da estrutura legada de agrupamento. |
| `service_request_care_item_snapshots` | `copias_itens_cuidado_solicitacoes` | Cópias legadas dos itens solicitados | Cópias da estrutura legada de grupos de cuidado. |
| `contract_care_items` | `itens_cuidado_contratacoes` | Itens de cuidado das contratações | Itens vinculados à estrutura legada de contratação. |
| `contract_care_item_history` | `historico_itens_cuidado_contratacoes` | Histórico dos itens contratados | Histórico da estrutura legada de itens contratados. |
| `snapshot_itens_cuidado_solicitacoes` | `copias_itens_rotina_solicitacoes` | Cópias dos itens de rotina | Nome parcialmente traduzido substituído pela V038. |
| `care_routine_item_weekdays` | `dias_semana_itens_rotina` | Dias dos itens de rotina | Recorrência semanal dos itens da rotina. |
| `service_request_care_snapshot_weekdays` | `dias_semana_snapshot_solicitacoes` | Dias das cópias de itens | Recorrência semanal preservada no snapshot. |
| `care_task_reminders` | `lembretes_tarefas_cuidado` | Lembretes das tarefas | Agendamento e entrega de lembretes. |
| `care_occurrence_photos` | `fotos_ocorrencias_cuidado` | Fotos das ocorrências de cuidado | Evidências fotográficas dos cuidados. |
| `user_notification_preferences` | `preferencias_notificacoes_usuarios` | Preferências de notificações | Habilitação de eventos por usuário. |
| `service_attendance_records` | `registros_atendimento` | Registros de atendimento | Início/fim do atendimento com localização. |
| `attendance_reports` | `relatorios_atendimento` | Relatórios de atendimento | Relatório final e entrega por e-mail. |

## Colunas renomeadas

Cada equivalência aplica-se a todas as tabelas em que o nome antigo ocorria. Colunas já em português, identificadores técnicos universais e coordenadas são relacionadas na seção seguinte.

| Nome antigo | Nome novo | Descrição/impacto |
|---|---|---|
| `full_name` | `nome_completo` | Nome civil do usuário. |
| `password_hash` | `senha_hash` | Hash de autenticação; conteúdo não foi alterado. |
| `birth_date` | `data_nascimento` | Data de nascimento. |
| `user_type` | `tipo_usuario` | Enum do perfil de acesso. |
| `phone` | `telefone` | Telefone do usuário. |
| `profile_photo_url` | `url_foto_perfil` | URL da foto de perfil. |
| `created_at` / `updated_at` | `criado_em` / `atualizado_em` | Auditoria temporal. |
| `user_id` | `usuario_id` | FK para `usuarios.id`. |
| `token_hash` | `hash_token` | Hash do token de redefinição. |
| `expires_at` / `used_at` | `expira_em` / `usado_em` | Validade e uso do token. |
| `active` / `enabled` | `ativo` / `habilitado` | Indicadores booleanos. |
| `responsible_user_id` | `usuario_responsavel_id` | FK para o usuário responsável. |
| `caregiver_user_id` | `usuario_cuidador_id` | FK para o usuário cuidador. |
| `assisted_person_id` | `pessoa_assistida_id` | FK para a pessoa assistida. |
| `responsible_contact` | `contato_responsavel` | Indica que o contato é o responsável. |
| `caregiver_profile_id` | `perfil_cuidador_id` | FK para o perfil do cuidador. |
| `hiring_type` | `tipo_contratacao` | Modalidade da contratação. |
| `start_date` / `end_date` | `data_inicio` / `data_fim` | Intervalo de datas. |
| `needs_description` | `descricao_necessidades` | Necessidades informadas na solicitação. |
| `activity_other` | `outra_atividade` | Atividade livre. |
| `additional_notes` | `observacoes_adicionais` | Observações complementares. |
| `negotiation_notes` | `observacoes_negociacao` | Observações da negociação. |
| `canceled_at` | `cancelado_em` | Instante de cancelamento. |
| `rejection_reason` | `motivo_rejeicao` | Motivo da rejeição. |
| `cancellation_reason` | `motivo_cancelamento` | Motivo do cancelamento. |
| `service_request_id` | `solicitacao_servico_id` | FK para a solicitação. |
| `service_date` | `data_servico` | Data específica do serviço. |
| `weekday` | `dia_semana` | Enum do dia da semana. |
| `start_time` / `end_time` | `horario_inicio` / `horario_fim` | Horários da grade. |
| `activity` | `atividade` | Atividade solicitada. |
| `recipient_user_id` | `usuario_destinatario_id` | Destinatário da notificação/lembrete. |
| `type` | `tipo` | Tipo genérico do registro. |
| `title` / `message` | `titulo` / `mensagem` | Conteúdo textual. |
| `related_entity_type` | `tipo_entidade_relacionada` | Tipo da entidade relacionada. |
| `related_entity_id` | `entidade_relacionada_id` | ID da entidade relacionada. |
| `read_at` / `cleared_at` | `lida_em` / `removida_em` | Leitura e remoção lógica da notificação. |
| `deduplication_key` | `chave_deduplicacao` | Chave única contra eventos duplicados. |
| `contract_id` | `contratacao_id` | FK para `contratacoes.id`. |
| `caregiver_id` / `responsible_id` | `cuidador_id` / `responsavel_id` | FKs de participantes. |
| `closure_reason` | `motivo_encerramento` | Motivo de encerramento da contratação. |
| `termination_type` | `tipo_encerramento` | Enum do encerramento. |
| `termination_reason` | `motivo_solicitacao_encerramento` | Motivo informado na solicitação. |
| `termination_notes` | `observacoes_encerramento` | Observações do encerramento. |
| `termination_requested_by_user_id` | `usuario_solicitante_encerramento_id` | Solicitante do encerramento. |
| `termination_requested_at` | `encerramento_solicitado_em` | Instante da solicitação. |
| `effective_end_date` | `data_fim_efetiva` | Data em que o encerramento produz efeito. |
| `cancellation_requested_by_user_id` | `usuario_solicitante_cancelamento_id` | Solicitante do cancelamento. |
| `cancellation_requested_at` | `cancelamento_solicitado_em` | Instante da solicitação de cancelamento. |
| `entity_type` / `entity_id` | `tipo_entidade` / `entidade_id` | Referência polimórfica do histórico. |
| `previous_status` / `new_status` | `status_anterior` / `novo_status` | Transição registrada. |
| `changed_by_user_id` | `usuario_alteracao_id` | Autor da transição. |
| `reason` | `motivo` | Justificativa da transição. |
| `name` / `description` | `nome` / `descricao` | Nome e descrição. |
| `care_routine_id` | `rotina_cuidado_id` | FK para a rotina. |
| `sort_order` | `ordem_exibicao` | Ordem do item. |
| `category` / `custom_category` | `categoria` / `categoria_personalizada` | Categoria normalizada ou livre. |
| `priority` | `prioridade` | Prioridade da tarefa. |
| `recurrence_type` | `tipo_recorrencia` | Regra de recorrência. |
| `scheduled_time` | `horario_previsto` | Horário planejado. |
| `interval_days` | `intervalo_dias` | Intervalo de recorrência. |
| `reminder_enabled` | `lembrete_habilitado` | Habilitação do lembrete. |
| `reminder_minutes_before` | `minutos_antecedencia_lembrete` | Antecedência do lembrete. |
| `reminder_at_scheduled_time` | `lembrar_no_horario_previsto` | Lembrete no horário exato. |
| `overdue_reminder_enabled` | `lembrete_atraso_habilitado` | Habilitação de aviso de atraso. |
| `overdue_after_minutes` | `minutos_para_atraso` | Tolerância antes do atraso. |
| `repeat_while_pending` | `repetir_enquanto_pendente` | Repetição enquanto não concluída. |
| `repeat_interval_minutes` | `intervalo_repeticao_minutos` | Intervalo de repetição. |
| `important` | `importante` | Marca de relevância. |
| `notify_responsible_if_important` | `notificar_responsavel_se_importante` | Regra de alerta ao responsável. |
| `requires_completion_photo` | `exige_foto_conclusao` | Exige evidência fotográfica. |
| `notes` | `anotacoes` | Anotações livres. |
| `care_routine_item_id` | `item_rotina_cuidado_id` | FK para o item da rotina. |
| `care_routine_name_snapshot` | `nome_rotina_snapshot` | Nome preservado da rotina. |
| `original_care_routine_id` | `rotina_cuidado_original_id` | Rotina de origem do snapshot. |
| `original_care_routine_item_id` | `item_rotina_cuidado_original_id` | Item de origem do snapshot. |
| `snapshot_item_id` | `item_snapshot_id` | FK para o item copiado. |
| `task_id` | `tarefa_id` | FK para a tarefa. |
| `responsible_creator_id` | `responsavel_criador_id` | Responsável que criou a tarefa. |
| `caregiver_executor_id` | `cuidador_executor_id` | Cuidador executor. |
| `previous_series_id` | `serie_anterior_id` | Série anterior após edição. |
| `source_snapshot_item_id` | `item_snapshot_origem_id` | Item de snapshot que originou a tarefa. |
| `duplicate_of_task_id` | `tarefa_duplicada_de_id` | Série canônica da duplicata. |
| `medication_name` | `nome_medicamento` | Nome do medicamento. |
| `medication_dosage` | `dosagem_medicamento` | Dosagem. |
| `medication_unit` | `unidade_medicamento` | Unidade enumerada. |
| `medication_custom_unit` | `unidade_personalizada_medicamento` | Unidade livre. |
| `medication_administration_route` | `via_administracao_medicamento` | Via enumerada. |
| `medication_custom_route` | `via_personalizada_medicamento` | Via livre. |
| `medication_instructions` | `instrucoes_medicamento` | Instruções. |
| `created_by_user_id` / `updated_by_user_id` | `usuario_criacao_id` / `usuario_atualizacao_id` | Autoria da criação/alteração. |
| `version` | `versao` | Controle otimista. |
| `scheduled_date` | `data_prevista` | Data da ocorrência. |
| `scheduled_instant_utc` | `instante_previsto_utc` | Instante normalizado em UTC. |
| `timezone` | `fuso_horario` | Fuso IANA. |
| `completed_at` | `concluido_em` | Instante de conclusão. |
| `executed_by_user_id` | `usuario_executor_id` | Usuário que executou. |
| `non_completion_reason` | `motivo_nao_realizacao` | Justificativa de não realização. |
| `execution_note` | `anotacao_execucao` | Nota de execução. |
| `exception` | `excecao` | Ocorrência excepcional. |
| `auto_marked_not_done` | `marcada_nao_realizada_automaticamente` | Expiração automática. |
| `status_updated_at` | `status_atualizado_em` | Última transição da ocorrência. |
| `occurrence_id` | `ocorrencia_id` | FK para a ocorrência. |
| `activity_type` | `tipo_atividade` | Tipo da atividade do diário. |
| `source_type` | `tipo_origem` | Origem planejada ou manual. |
| `entry_date` | `data_registro` | Data lógica do diário. |
| `care_type` | `tipo_cuidado` | Tipo do cuidado. |
| `occurred_at` | `ocorrido_em` | Instante efetivo. |
| `activity_record_id` | `registro_atividade_id` | FK para registro manual. |
| `actor_user_id` | `usuario_ator_id` | Autor da ação auditada. |
| `action` / `details` | `acao` / `detalhes` | Ação e detalhes da auditoria. |
| `uploaded_by_user_id` | `usuario_envio_id` | Autor do upload. |
| `file_name` / `original_file_name` | `nome_arquivo` / `nome_arquivo_original` | Nomes interno e original. |
| `content_type` / `file_size` | `tipo_conteudo` / `tamanho_arquivo` | Metadados do arquivo. |
| `reminder_type` | `tipo_lembrete` | Categoria do lembrete. |
| `scheduled_at` / `sent_at` | `previsto_em` / `enviado_em` | Agendamento e envio. |
| `notification_type` | `tipo_notificacao` | Evento configurável. |
| `requester_user_id` | `usuario_solicitante_id` | Autor da solicitação/oportunidade. |
| `initiated_by` | `iniciado_por` | Papel que iniciou o fluxo. |
| `source_opportunity_id` | `oportunidade_origem_id` | Publicação que originou a candidatura. |
| `attendance_date` | `data_atendimento` | Data do atendimento. |
| `record_type` | `tipo_registro` | Início ou fim. |
| `recorded_at` | `registrado_em` | Instante registrado. |
| `accuracy` | `precisao` | Precisão da geolocalização. |
| `location_captured_at` | `localizacao_capturada_em` | Instante da captura. |
| `address_snapshot` | `endereco_registrado` | Endereço textual preservado. |
| `device_timezone` | `fuso_dispositivo` | Fuso informado pelo dispositivo. |
| `scheduled_start_time` / `scheduled_end_time` | `horario_inicio_previsto` / `horario_fim_previsto` | Horário contratado. |
| `allowed_window_start` / `allowed_window_end` | `janela_permitida_inicio` / `janela_permitida_fim` | Limites temporais permitidos. |
| `within_allowed_window` | `dentro_janela_permitida` | Resultado da validação temporal. |
| `attendance_start_record_id` / `attendance_end_record_id` | `registro_inicio_atendimento_id` / `registro_fim_atendimento_id` | Registros usados no relatório. |
| `generated_text` / `edited_text` / `final_text` | `texto_gerado` / `texto_editado` / `texto_final` | Versões do relatório. |
| `nursing_notes` | `anotacoes_enfermagem` | Conteúdo clínico consolidado. |
| `email_status` | `status_email` | Estado da entrega. |
| `email_requested_at` / `email_sent_at` | `email_solicitado_em` / `email_enviado_em` | Instantes da entrega. |
| `email_attempts` | `tentativas_email` | Quantidade de tentativas. |
| `email_next_retry_at` | `proxima_tentativa_email_em` | Próximo retry. |
| `email_error_message` | `mensagem_erro_email` | Último erro. |
| `generated_at` / `edited_at` / `finalized_at` | `gerado_em` / `editado_em` / `finalizado_em` | Ciclo de vida do relatório. |

## Colunas preservadas

As colunas abaixo já estavam em português ou são identificadores técnicos universais e, por isso, mantiveram o nome: `id`, `cpf`, `email`, `status`, `cep`, `rua`, `numero`, `complemento`, `bairro`, `cidade`, `estado`, `latitude`, `longitude`, `ponto_referencia`, `parentesco`, `parentesco_outro`, `preferencia_contato`, `formacao`, `formacao_outro`, `experiencia`, `tempo_experiencia`, `biografia`, `horario_inicio`, `horario_fim`, `observacao`, `modalidade`, `modalidade_outro`, `servico`, `servico_outro`, `nome`, `data_nascimento`, `grau_dependencia`, `mobilidade`, `mobilidade_outro`, `alergia`, `alergias_outro`, `alergias_detalhes`, `restricao`, `restricoes_alimentares_outro`, `restricoes_alimentares_detalhes`, `medicamentos`, `observacoes`, `telefone`, `vinculo`, `dia_semana` e `periodo`.

## Constraints, índices e sequences

- A V037 renomeia todas as PKs para `pk_<tabela>`.
- FKs passam a `fk_<tabela>_<coluna_fk>`.
- Uniques passam a `uk_<tabela>_<colunas>`; índices únicos parciais usam `ux_`.
- Índices comuns passam a `idx_<tabela>_<colunas>`.
- Checks recebem prefixo `ck_` e nomes em português.
- Nomes acima de 63 caracteres são abreviados de modo determinístico e recebem um sufixo hash para evitar colisões no PostgreSQL.
- Não foram encontradas sequences de domínio: todas as PKs são UUID.

## Impacto no código

| Área | Tratamento |
|---|---|
| Entidades JPA | A estratégia física traduz os nomes inferidos e os nomes declarados em annotations. |
| Repositories JPQL | Sem mudança, pois JPQL usa entidades e propriedades Java. |
| Query nativa de ocorrências | Atualizada para `ocorrencias_tarefas` e colunas em português. |
| DTOs e JSON | Preservados para não quebrar o aplicativo. |
| Frontend Expo | Sem alteração; ele não conhece nomes físicos do banco. |
| Migrations V001–V030 | Preservadas como histórico imutável. |
| Migrations V031–V038 | Executam os renames em transações Flyway pequenas e retomáveis. |
