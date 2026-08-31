# Mapeamento de tabelas para o singular

Levantamento realizado em 30/08/2026 no schema `public` na V042, antes da renomeação. O catálogo possui 35 tabelas de domínio e uma tabela de infraestrutura, 416 colunas e dados em 31 tabelas. Não existem views, sequences ou rotinas armazenadas no schema. Todas as FKs, constraints e índices foram inventariados; o PostgreSQL preserva seus vínculos por OID durante `ALTER TABLE ... RENAME TO`.

| Tabela atual | Linhas | Tipo | Domínio | Entidade/mapeamento | Nome singular definido | Requisitos | Justificativa |
|---|---:|---|---|---|---|---|---|
| `usuarios` | 4 | Principal | Identidade | `User` | `usuario` | RF01–RF07, RF10, RF17, RF18 | Cada linha representa uma conta. |
| `usuarios_tokens_redefinicao_senha` | 12 | Filha | Autenticação | `PasswordResetToken` | `usuario_token_redefinicao_senha` | RF03 | Cada linha representa um token de um usuário. |
| `responsaveis` | 1 | Principal | Perfis | `ResponsibleProfile` | `responsavel` | RF01, RF04 | Cada linha representa um perfil responsável. |
| `cuidadores` | 3 | Principal | Perfis | `CaregiverProfile` | `cuidador` | RF01, RF04–RF07 | Cada linha representa um perfil cuidador. |
| `cuidadores_disponibilidade_dias` | 9 | Filha/coleção | Perfis | `CaregiverAvailability.diasSemana` | `cuidador_disponibilidade_dia` | RF05–RF07 | Um dia disponível por linha. |
| `cuidadores_disponibilidade_periodos` | 7 | Filha/coleção | Perfis | `CaregiverAvailability.periodos` | `cuidador_disponibilidade_periodo` | RF05–RF07 | Um período disponível por linha. |
| `cuidadores_formacoes` | 7 | Filha/coleção | Perfis | `CaregiverProfile.formacoes` | `cuidador_formacao` | RF05–RF07 | Uma formação por linha. |
| `cuidadores_modalidades` | 9 | Filha/coleção | Perfis | `CaregiverProfile.modalidades` | `cuidador_modalidade` | RF05–RF07 | Uma modalidade por linha. |
| `cuidadores_servicos` | 11 | Filha/coleção | Perfis | `CaregiverProfile.servicosOferecidos` | `cuidador_servico` | RF05–RF07 | Um serviço oferecido por linha. |
| `pessoas_assistidas` | 1 | Principal | Assistência | `AssistedPerson` | `pessoa_assistida` | RF01, RF04, RF08, RF10, RF17, RF18 | Cada linha representa uma pessoa. |
| `pessoas_assistidas_alergias` | 2 | Filha/coleção | Assistência | `AssistedPerson.alergias` | `pessoa_assistida_alergia` | RF01, RF04 | Uma alergia por linha. |
| `pessoas_assistidas_contatos_emergencia` | 1 | Filha | Assistência | `EmergencyContact` | `pessoa_assistida_contato_emergencia` | RF01, RF04 | Um contato vinculado por linha. |
| `pessoas_assistidas_restricoes_alimentares` | 1 | Filha/coleção | Assistência | `AssistedPerson.restricoesAlimentares` | `pessoa_assistida_restricao_alimentar` | RF01, RF04 | Uma restrição por linha. |
| `solicitacoes_servico` | 23 | Principal | Solicitação | `ServiceRequest` | `solicitacao_servico` | RF08–RF10, RF17 | Cada linha representa uma solicitação ou oportunidade. |
| `solicitacoes_servico_agenda_dias` | 29 | Filha/coleção | Solicitação | `ServiceRequest.scheduleDays` | `solicitacao_servico_agenda_dia` | RF08, RF12 | Um dia da agenda por linha. |
| `solicitacoes_servico_atividades` | 31 | Filha/coleção | Solicitação | `ServiceRequest.activities` | `solicitacao_servico_atividade` | RF08 | Uma atividade por linha. |
| `solicitacoes_servico_datas` | 20 | Filha/coleção | Solicitação | `ServiceRequest.specificDates` | `solicitacao_servico_data` | RF08, RF12 | Uma data específica por linha. |
| `solicitacoes_servico_itens_cuidado_copias` | 47 | Filha/cópia | Solicitação | `ServiceRequestCareItemSnapshot` | `solicitacao_servico_item_cuidado_copia` | RF08, RF13 | Uma cópia imutável de item por linha. |
| `solicitacoes_servico_itens_cuidado_copias_dias_semana` | 0 | Filha/coleção | Solicitação | `ServiceRequestCareItemSnapshot.weekdays` | `solicitacao_servico_item_cuidado_copia_dia_semana` | RF08, RF13 | Um dia da cópia por linha. |
| `solicitacoes_servico_contratacoes_historico_status` | 79 | Histórico polimórfico | Solicitação/contratação | `StatusHistory` | `solicitacao_servico_contratacao_historico_status` | RF09–RF11 | Preserva no nome os dois tipos de entidade registrados. |
| `contratacoes` | 19 | Principal | Contratação | `CareContract` | `contratacao` | RF09–RF12, RF15–RF18 | Cada linha representa um vínculo contratado. |
| `rotinas_cuidado` | 1 | Principal | Rotina | `CareRoutine` | `rotina_cuidado` | RF08, RF13 | Cada linha representa uma rotina. |
| `rotinas_cuidado_itens` | 3 | Filha | Rotina | `CareRoutineItem` | `rotina_cuidado_item` | RF13 | Cada linha representa um item da rotina. |
| `rotinas_cuidado_itens_dias_semana` | 0 | Filha/coleção | Rotina | `CareRoutineItem.weekdays` | `rotina_cuidado_item_dia_semana` | RF13 | Um dia do item por linha. |
| `tarefas_cuidado` | 41 | Principal | Tarefa | `CareTask` | `tarefa_cuidado` | RF13–RF15 | Cada linha representa uma série de tarefa. |
| `tarefas_cuidado_dias_semana` | 0 | Filha/coleção | Tarefa | `CareTask.weekdays` | `tarefa_cuidado_dia_semana` | RF13 | Um dia recorrente por linha. |
| `tarefas_cuidado_auditoria` | 68 | Auditoria | Tarefa | `TaskAuditEntry` | `tarefa_cuidado_auditoria` | RF13, RF15 | Auditoria já é conceito singular. |
| `ocorrencias_cuidado` | 70 | Principal | Execução | `TaskOccurrence` | `ocorrencia_cuidado` | RF12–RF16, RF19 | Cada linha representa uma ocorrência. |
| `ocorrencias_cuidado_fotos` | 4 | Anexo | Execução | `CareOccurrencePhoto` | `ocorrencia_cuidado_foto` | RF15, RF16, RF19 | Uma foto por linha. |
| `ocorrencias_cuidado_lembretes` | 128 | Filha | Execução | `TaskReminder` | `ocorrencia_cuidado_lembrete` | RF14 | Um lembrete por linha. |
| `registros_diario_cuidado` | 8 | Principal/linha do tempo | Diário | `CareActivityRecord` | `registro_diario_cuidado` | RF15, RF16, RF19 | Cada linha representa um registro. |
| `registros_atendimento` | 12 | Principal | Presença | `ServiceAttendanceRecord` | `registro_atendimento` | RF12, RF16, RF18, RF19 | Cada linha representa check-in ou check-out. |
| `relatorios_atendimento` | 4 | Principal | Relatório | `AttendanceReport` | `relatorio_atendimento` | RF19 | Cada linha representa um relatório. |
| `notificacoes` | 80 | Principal | Notificação | `Notification` | `notificacao` | RF09, RF11, RF14, RF17–RF19 | Cada linha representa uma notificação. |
| `notificacoes_preferencias` | 0 | Filha/preferência | Notificação | `UserNotificationPreference` | `notificacao_preferencia` | RF09, RF11, RF14, RF17–RF19 | Cada linha representa uma preferência. |
| `flyway_schema_history` | 42 | Infraestrutura | Flyway | Flyway | `flyway_schema_history` | — | Nome controlado pela ferramenta; deve permanecer. |

## Dependências e decisões técnicas

- A única query nativa ativa está em `TaskOccurrenceRepository` e referencia `ocorrencias_cuidado`; ela deve passar a usar `ocorrencia_cuidado`.
- Os mappings físicos são centralizados por `PortuguesePhysicalNamingStrategy`; as classes, propriedades e contratos JSON permanecem inalterados.
- Não existem tabelas legadas no catálogo atual: as cinco estruturas antigas foram removidas pela V040. Portanto, não há legado a renomear ou decisão pendente.
- Não existem sequences no schema porque as entidades usam UUID; não há views ou rotinas armazenadas dependentes.
- Todas as 35 tabelas de domínio serão renomeadas. A tabela do Flyway será a única mantida.

## Artefatos correlatos inventariados

- **Repositories principais:** `UserRepository`, `PasswordResetTokenRepository`, `ResponsibleProfileRepository`, `CaregiverProfileRepository`, `AssistedPersonRepository`, `EmergencyContactRepository`, `ServiceRequestRepository`, `StatusHistoryRepository`, `CareContractRepository`, `CareRoutineRepository`, `CareTaskRepository`, `TaskOccurrenceRepository`, `TaskReminderRepository`, `TaskAuditEntryRepository`, `CareActivityRecordRepository`, `CareOccurrencePhotoRepository`, `ServiceAttendanceRepository`, `AttendanceReportRepository`, `NotificationRepository` e `UserNotificationPreferenceRepository`.
- **Coleções sem repository próprio:** disponibilidade, formação, modalidade, serviço, alergia, restrição alimentar, agenda, atividade, data e dias da semana são persistidos pelas entidades proprietárias com `@ElementCollection`, `@CollectionTable` ou relacionamento JPA.
- **Migrations:** V001–V030 criam e evoluem o modelo lógico original; V031–V038 traduzem tabelas e colunas; V039 organiza tabelas filhas e padroniza objetos; V040 remove os cinco legados; V041 conclui a tradução de colunas; V042 consolida formação; V043 executa exclusivamente a renomeação para o singular.
- **Catálogo físico:** FKs, uniques, checks e índices por coluna estão no [inventário do schema](inventario-colunas-schema.md). A finalidade e os relacionamentos por tabela estão na [modelagem principal](modelagem-banco-dados.md).
- **Documentação:** o [dicionário antigo → novo](dicionario-renomeacao-tabelas-singular.md) é a referência de transição; as matrizes RF → tabelas e tabela → RF ficam na modelagem principal.
