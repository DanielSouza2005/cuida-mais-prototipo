# Dicionário de nomenclatura relacional das tabelas

Este mapa registra o catálogo histórico do PostgreSQL na versão 038, antes da migration de padronização relacional. Os nomes plurais abaixo pertencem a essa fotografia histórica. `flyway_schema_history` é infraestrutura externa e permanece inalterada. As cinco tabelas marcadas como legadas foram removidas pela V040; a V041 concluiu a tradução das colunas; a V042 consolidou a formação; e a V043 renomeou todas as tabelas de domínio para o singular. A correspondência final está no [dicionário da V043](dicionario-renomeacao-tabelas-singular.md).

## Inventário técnico anterior à V039

| Tabela na V038 | Linhas | PK | Tabelas referenciadas por FK |
|---|---:|---|---|
| `alergias_pessoas_assistidas` | 2 | — | `pessoas_assistidas` |
| `atividades_solicitacoes_servico` | 31 | `solicitacao_servico_id, atividade` | `solicitacoes_servico` |
| `auditoria_tarefas_cuidado` | 68 | `id` | `ocorrencias_tarefas`, `tarefas_cuidado`, `usuarios` |
| `contatos_emergencia` | 1 | `id` | `pessoas_assistidas` |
| `contratacoes` | 19 | `id` | `pessoas_assistidas`, `solicitacoes_servico`, `usuarios` |
| `copias_itens_cuidado_solicitacoes` | 0 | `id` | `solicitacoes_servico` |
| `copias_itens_rotina_solicitacoes` | 47 | `id` | `itens_rotinas_cuidado`, `rotinas_cuidado`, `solicitacoes_servico` |
| `datas_solicitacoes_servico` | 20 | `solicitacao_servico_id, data_servico` | `solicitacoes_servico` |
| `dias_agenda_solicitacoes_servico` | 29 | `solicitacao_servico_id, dia_semana` | `solicitacoes_servico` |
| `dias_disponibilidade_cuidadores` | 9 | — | `perfis_cuidadores` |
| `dias_semana_itens_rotina` | 0 | `item_rotina_cuidado_id, dia_semana` | `itens_rotinas_cuidado` |
| `dias_semana_snapshot_solicitacoes` | 0 | `item_snapshot_id, dia_semana` | `copias_itens_rotina_solicitacoes` |
| `dias_semana_tarefas_cuidado` | 0 | `tarefa_id, dia_semana` | `tarefas_cuidado` |
| `flyway_schema_history` | 38 | `installed_rank` | — |
| `formacoes_cuidadores` | 7 | — | `perfis_cuidadores` |
| `fotos_ocorrencias_cuidado` | 4 | `id` | `ocorrencias_tarefas`, `registros_atividades_cuidado`, `usuarios` |
| `grupos_cuidado` | 0 | `id` | `pessoas_assistidas`, `usuarios` |
| `historico_itens_cuidado_contratacoes` | 0 | `id` | `contratacoes`, `itens_cuidado_contratacoes`, `usuarios` |
| `historico_status` | 79 | `id` | `usuarios` |
| `itens_cuidado_contratacoes` | 0 | `id` | `contratacoes`, `copias_itens_cuidado_solicitacoes`, `usuarios` |
| `itens_grupos_cuidado` | 0 | `id` | `grupos_cuidado` |
| `itens_rotinas_cuidado` | 3 | `id` | `rotinas_cuidado` |
| `lembretes_tarefas_cuidado` | 128 | `id` | `ocorrencias_tarefas`, `usuarios` |
| `modalidades_cuidadores` | 9 | — | `perfis_cuidadores` |
| `notificacoes` | 80 | `id` | `usuarios` |
| `ocorrencias_tarefas` | 70 | `id` | `contratacoes`, `pessoas_assistidas`, `tarefas_cuidado`, `usuarios` |
| `perfis_cuidadores` | 3 | `id` | `usuarios` |
| `perfis_responsaveis` | 1 | `id` | `usuarios` |
| `periodos_disponibilidade_cuidadores` | 7 | — | `perfis_cuidadores` |
| `pessoas_assistidas` | 1 | `id` | `usuarios` |
| `preferencias_notificacoes_usuarios` | 0 | `id` | `usuarios` |
| `registros_atendimento` | 12 | `id` | `contratacoes`, `pessoas_assistidas`, `usuarios` |
| `registros_atividades_cuidado` | 8 | `id` | `contratacoes`, `ocorrencias_tarefas`, `pessoas_assistidas`, `usuarios` |
| `relatorios_atendimento` | 4 | `id` | `contratacoes`, `pessoas_assistidas`, `registros_atendimento`, `usuarios` |
| `restricoes_alimentares_pessoas_assistidas` | 1 | — | `pessoas_assistidas` |
| `rotinas_cuidado` | 1 | `id` | `pessoas_assistidas`, `usuarios` |
| `servicos_cuidadores` | 11 | — | `perfis_cuidadores` |
| `solicitacoes_servico` | 23 | `id` | `grupos_cuidado`, `pessoas_assistidas`, `rotinas_cuidado`, `solicitacoes_servico`, `usuarios` |
| `tarefas_cuidado` | 41 | `id` | `contratacoes`, `copias_itens_rotina_solicitacoes`, `pessoas_assistidas`, `tarefas_cuidado`, `usuarios` |
| `tokens_redefinicao_senha` | 12 | `id` | `usuarios` |
| `usuarios` | 4 | `id` | — |

## Mapa completo: nome atual → nome relacional

| Tabela atual | Novo nome | Tipo | Entidade principal | Problema/justificativa |
|---|---|---|---|---|
| `alergias_pessoas_assistidas` | `pessoas_assistidas_alergias` | Associativa/coleção | `pessoas_assistidas` | Coloca a entidade proprietária antes da coleção. |
| `atividades_solicitacoes_servico` | `solicitacoes_servico_atividades` | Filha/coleção | `solicitacoes_servico` | Agrupa as atividades com a solicitação. |
| `auditoria_tarefas_cuidado` | `tarefas_cuidado_auditoria` | Auditoria | `tarefas_cuidado` | A auditoria cobre tarefas e suas ocorrências. |
| `contatos_emergencia` | `pessoas_assistidas_contatos_emergencia` | Filha | `pessoas_assistidas` | O contato não existe fora da pessoa assistida. |
| `contratacoes` | `contratacoes` | Principal | — | Nome central, direto e já padronizado. |
| `copias_itens_cuidado_solicitacoes` | `solicitacoes_servico_grupos_cuidado_itens_copias` | Filha/legada | `solicitacoes_servico` | Cópia legada de item de grupo pertencente à solicitação. |
| `copias_itens_rotina_solicitacoes` | `solicitacoes_servico_itens_cuidado_copias` | Filha | `solicitacoes_servico` | Snapshot imutável dos itens solicitado; remove termo inglês e inicia pelo pai. |
| `datas_solicitacoes_servico` | `solicitacoes_servico_datas` | Filha/coleção | `solicitacoes_servico` | Coloca a solicitação antes de suas datas. |
| `dias_agenda_solicitacoes_servico` | `solicitacoes_servico_agenda_dias` | Filha/coleção | `solicitacoes_servico` | Explicita a grade semanal da solicitação. |
| `dias_disponibilidade_cuidadores` | `cuidadores_disponibilidade_dias` | Filha/coleção | `cuidadores` | Agrupa a disponibilidade junto do cuidador. |
| `dias_semana_itens_rotina` | `rotinas_cuidado_itens_dias_semana` | Filha/coleção | `rotinas_cuidado_itens` | Hierarquia completa rotina → item → dias. |
| `dias_semana_snapshot_solicitacoes` | `solicitacoes_servico_itens_cuidado_copias_dias_semana` | Filha/coleção | `solicitacoes_servico_itens_cuidado_copias` | Hierarquia completa da cópia do item. |
| `dias_semana_tarefas_cuidado` | `tarefas_cuidado_dias_semana` | Filha/coleção | `tarefas_cuidado` | Coloca a tarefa antes da recorrência semanal. |
| `flyway_schema_history` | `flyway_schema_history` | Infraestrutura | Flyway | Nome controlado pelo Flyway; não pertence ao modelo de domínio. |
| `formacoes_cuidadores` | `cuidadores_formacoes` | Associativa/coleção | `cuidadores` | Segue o padrão entidade → qualificação. |
| `fotos_ocorrencias_cuidado` | `ocorrencias_cuidado_fotos` | Anexo | `ocorrencias_cuidado` | A ocorrência passa a preceder seus anexos. |
| `grupos_cuidado` | `grupos_cuidado` | Principal legada | — | Nome claro; preservado enquanto houver FKs históricas. |
| `historico_itens_cuidado_contratacoes` | `contratacoes_itens_cuidado_historico` | Histórico | `contratacoes_itens_cuidado` | Hierarquia contratação → item → histórico. |
| `historico_status` | `solicitacoes_servico_contratacoes_historico_status` | Histórico polimórfico | Solicitações e contratações | A tabela registra transições dos dois domínios. |
| `itens_cuidado_contratacoes` | `contratacoes_itens_cuidado` | Filha/legada | `contratacoes` | Agrupa itens junto da contratação. |
| `itens_grupos_cuidado` | `grupos_cuidado_itens` | Filha/legada | `grupos_cuidado` | Coloca o grupo antes de seus itens. |
| `itens_rotinas_cuidado` | `rotinas_cuidado_itens` | Filha | `rotinas_cuidado` | Coloca a rotina antes de seus itens. |
| `lembretes_tarefas_cuidado` | `ocorrencias_cuidado_lembretes` | Filha/processamento | `ocorrencias_cuidado` | A FK obrigatória é da ocorrência, não da série de tarefa. |
| `modalidades_cuidadores` | `cuidadores_modalidades` | Associativa/coleção | `cuidadores` | Segue o padrão entidade → modalidade. |
| `notificacoes` | `notificacoes` | Principal | — | Nome central e já padronizado. |
| `ocorrencias_tarefas` | `ocorrencias_cuidado` | Principal operacional | — | Representa o cuidado previsto/realizado em uma data. |
| `perfis_cuidadores` | `cuidadores` | Principal | `usuarios` | O registro representa o cuidador e possui FK única para usuário. |
| `perfis_responsaveis` | `responsaveis` | Principal | `usuarios` | O registro representa o responsável e possui FK única para usuário. |
| `periodos_disponibilidade_cuidadores` | `cuidadores_disponibilidade_periodos` | Filha/coleção | `cuidadores` | Agrupa os períodos junto da disponibilidade do cuidador. |
| `pessoas_assistidas` | `pessoas_assistidas` | Principal | — | Nome central, direto e já padronizado. |
| `preferencias_notificacoes_usuarios` | `notificacoes_preferencias` | Preferência | `notificacoes` | Agrupa preferências no domínio de notificações; `usuario_id` mantém o proprietário. |
| `registros_atendimento` | `registros_atendimento` | Principal operacional | `contratacoes` | Registra início/fim e localização por data; nome já coerente com RF19. |
| `registros_atividades_cuidado` | `registros_diario_cuidado` | Principal operacional | `contratacoes` | Diferencia cuidado avulso/diário de tarefa planejada e ocorrência. |
| `relatorios_atendimento` | `relatorios_atendimento` | Principal | `registros_atendimento` | Relatório consolidado do atendimento; nome já padronizado. |
| `restricoes_alimentares_pessoas_assistidas` | `pessoas_assistidas_restricoes_alimentares` | Associativa/coleção | `pessoas_assistidas` | Coloca a pessoa antes da restrição. |
| `rotinas_cuidado` | `rotinas_cuidado` | Principal | — | Modelo reutilizável; nome já padronizado. |
| `servicos_cuidadores` | `cuidadores_servicos` | Associativa/coleção | `cuidadores` | Explicita cuidador → serviços oferecidos. |
| `solicitacoes_servico` | `solicitacoes_servico` | Principal | — | Unifica solicitações diretas e publicações pelo discriminador existente. |
| `tarefas_cuidado` | `tarefas_cuidado` | Principal de planejamento | `contratacoes` | Série/planejamento que gera ocorrências; nome já claro. |
| `tokens_redefinicao_senha` | `usuarios_tokens_redefinicao_senha` | Filha/processamento | `usuarios` | Agrupa tokens com a entidade autenticada. |
| `usuarios` | `usuarios` | Principal | — | Raiz de autenticação e identidade; nome já padronizado. |

## Integridade e uso no backend

- Todas as tabelas de domínio usam UUID; não existem sequences de domínio no schema `public`.
- PKs, FKs, uniques e índices são preservados por renomeação de catálogo e recebem nomes coerentes na migration seguinte.
- As coleções JPA (`@CollectionTable`) cobrem alergias, restrições, disponibilidade, formação, modalidade, serviço, datas, agenda, atividades e dias de recorrência.
- As entidades JPA cobrem usuários, perfis, pessoas assistidas, solicitações, contratações, histórico, rotinas, tarefas, ocorrências, diário, fotos, lembretes, notificações, preferências, registros e relatórios.
- Existe uma query nativa em `TaskOccurrenceRepository`; ela deve acompanhar o novo nome de `ocorrencias_cuidado`.
- As cinco tabelas de grupos/itens legadas estavam vazias na V038; foram preservadas pela V039 e removidas com suas FKs pela V040.
- `solicitacoes_servico` representa tanto pedidos diretos quanto oportunidades/publicações; não há uma segunda tabela física `publicacoes_servico`.

## Entidades, repositories e SQL ativo

| Domínio físico | Entidade/coleção JPA | Repository direto |
|---|---|---|
| `usuarios`, `usuarios_tokens_redefinicao_senha` | `User`, `PasswordResetToken` | `UserRepository`, `PasswordResetTokenRepository` |
| `responsaveis`, `cuidadores` e coleções de cuidadores | `ResponsibleProfile`, `CaregiverProfile` | `ResponsibleProfileRepository`, `CaregiverProfileRepository` |
| `pessoas_assistidas` e suas coleções | `AssistedPerson`, `EmergencyContact` | `AssistedPersonRepository`, `EmergencyContactRepository` |
| `solicitacoes_servico` e tabelas filhas | `ServiceRequest`, `ServiceRequestCareItemSnapshot` | `ServiceRequestRepository` |
| `contratacoes` | `CareContract` | `CareContractRepository` |
| `solicitacoes_servico_contratacoes_historico_status` | `StatusHistory` | `StatusHistoryRepository` |
| `rotinas_cuidado` e tabelas filhas | `CareRoutine`, `CareRoutineItem` | `CareRoutineRepository` |
| `tarefas_cuidado` e tabelas filhas | `CareTask`, `TaskAuditEntry` | `CareTaskRepository`, `TaskAuditEntryRepository` |
| `ocorrencias_cuidado` e tabelas filhas | `TaskOccurrence`, `TaskReminder`, `CareOccurrencePhoto` | `TaskOccurrenceRepository`, `TaskReminderRepository`, `CareOccurrencePhotoRepository` |
| `registros_diario_cuidado` | `CareActivityRecord` | `CareActivityRecordRepository` |
| `notificacoes`, `notificacoes_preferencias` | `Notification`, `UserNotificationPreference` | `NotificationRepository`, `UserNotificationPreferenceRepository` |
| `registros_atendimento` | `ServiceAttendanceRecord` | `ServiceAttendanceRepository` |
| `relatorios_atendimento` | `AttendanceReport` | `AttendanceReportRepository` |

A única query SQL nativa ativa está em `TaskOccurrenceRepository` e foi atualizada para `ocorrencias_cuidado`. Não existem seeds SQL ativos fora das migrations. As migrations V001–V039 permanecem como etapas históricas; nomes anteriores dentro delas são intencionais, a V040 remove as estruturas legadas e a V041 conclui a tradução das colunas físicas de domínio.
