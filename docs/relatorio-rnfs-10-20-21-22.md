# Relatório de Validação — RNF10, RNF20 e RNF21

Data da análise: 12/10/2026  
Escopo: `api/`, `front/`, `docs/`, migrations Flyway, documentação de modelagem e testes automatizados.  
Natureza: análise técnica estática e verificação automatizada.

## Resumo executivo

| RNF | Status | Justificativa curta | Prioridade de correção |
|---|---|---|---|
| RNF10 | **PARCIALMENTE ATENDIDO** | Os fluxos assistenciais principais autenticam e validam propriedade/participação no backend, mas oportunidades abertas revelam grau de dependência, mobilidade, necessidades e resumo da rotina a cuidadores ainda não vinculados. Faltam testes sistemáticos de acesso cruzado em todos os recursos sensíveis. | **Alta** |
| RNF20 | **PARCIALMENTE ATENDIDO** | Os fluxos de banco mais críticos têm transações, bloqueios e constraints, porém uploads de fotos escrevem no sistema de arquivos antes da confirmação da transação e não possuem compensação de rollback. O envio de recuperação de senha também ocorre dentro da transação. | **Alta** |
| RNF21 | **PARCIALMENTE ATENDIDO** | Há separação clara por camadas e módulos, DTOs, serviços de infraestrutura e cliente HTTP centralizado. Persistem classes/telas grandes, duplicação de autorização e arquivos excessivamente compactados, com baixa cobertura automatizada no frontend. | **Média** |

### Conclusão geral

Permanecem duas lacunas de maior impacto fora do escopo atual: exposição pré-vínculo de dados assistenciais nas oportunidades e falta de atomicidade entre banco e armazenamento de fotos.

### Problemas críticos e de alta prioridade

1. **Privacidade/autorização:** `ServiceOpportunityService.response(...)` entrega `grauDependencia`, `mobilidade`, `needsDescription` e categorias/horários da rotina a qualquer cuidador autenticado e aprovado que consulte uma oportunidade, antes de existir solicitação aceita, contratação ou atendimento.
2. **Consistência banco/arquivo:** `CareOccurrencePhotoStorageService.store(...)` e `ProfilePhotoStorageService.store(...)` persistem o arquivo imediatamente; se a transação JPA falhar depois, o arquivo não é removido. Em atualização/cadastro de foto de perfil, também não há limpeza compensatória do arquivo novo nem remoção explícita da foto anterior.

---

## RNF10 — Restrição de acesso a dados assistenciais

**Status: PARCIALMENTE ATENDIDO**

### Evidências

| Área | Evidência | Arquivo/método |
|---|---|---|
| Autenticação global | A configuração exige autenticação em qualquer rota não explicitamente pública; `/api/admin/**` exige `ROLE_ADMIN`. O filtro valida token, existência do usuário, situação da conta e aprovação do perfil. | `security/SecurityConfig.java`, `securityFilterChain(...)`, linhas 31–58; `security/JwtAuthenticationFilter.java`, `doFilterInternal(...)` e `validateAccess(...)`, linhas 43–102 |
| Perfil da pessoa assistida | A consulta de perfil parte exclusivamente do usuário autenticado. Alterações de pessoa assistida, endereço e contato usam `findByIdAndResponsibleUser`, impedindo edição por outro responsável. | `profile/ProfileService.java`, `findMyProfile(...)`, linhas 91–130; `findResponsibleAssistedPerson(...)`, linhas 296–304 |
| Rotinas de cuidado | Listagem e detalhe usam o responsável autenticado; pessoa assistida informada na rotina também é buscada pelo par responsável + id. | `care_routine/CareRoutineService.java`, `list(...)`, `details(...)`, `owned(...)` e `apply(...)` |
| Solicitações dirigidas | O responsável somente cria solicitação com pessoa assistida própria. O cuidador só consulta solicitação em que é o destinatário, usando consultas filtradas por `caregiverUser`. | `service_request/ServiceRequestService.java`, `create(...)` e `find(...)`; `CaregiverServiceRequestService.java`, `owned(...)` e `ownedForUpdate(...)`, linhas 62–64 |
| Contratações e histórico | Detalhes completos — inclusive alergias, restrições, observações e endereço — só são montados depois de validar que o usuário é responsável ou cuidador da contratação. | `contract_history/ResponsibleContractHistoryService.java`, `details(...)` e `contractDetails(...)`, linhas 76–95; `toDetails(...)`, linhas 115–136 |
| Agenda | A agenda é construída apenas a partir das contratações do responsável ou cuidador autenticado. O detalhe reutiliza as verificações de contratação e atendimento. | `agenda/AgendaService.java`, `events(...)`, linhas 50–74; `details(...)`, linhas 77–84 |
| Atendimento | Início/fim exigem cuidador e vínculo com a contratação; consulta exige que o usuário seja um dos dois participantes. | `service_attendance/ServiceAttendanceService.java`, `start(...)`, `end(...)`, `details(...)`, `requireCaregiverContract(...)` e `requireParticipant(...)`, linhas 48–160 |
| Tarefas e ocorrências | Consultas do cuidador usam `findByIdAndCaregiver`; consultas do responsável comparam `responsibleCreator`; execução também exige atendimento ativo e contrato válido. | `care_task/TaskOccurrenceService.java`, `details(...)`, `responsibleDetails(...)`, `owned(...)` e `validateExecution(...)`, linhas 144–240 |
| Diário assistencial | Criação valida cuidador da contratação, correspondência da pessoa assistida, data elegível e atendimento ativo. Listas e detalhes são consultados por cuidador/responsável. | `care_task/CareDiaryService.java`, `createManual(...)`, linhas 80–119; `caregiverDiary(...)`, `responsibleDiary(...)`, `caregiverDetails(...)` e `responsibleDetails(...)`, linhas 121–160 |
| Fotos assistenciais | O arquivo só é devolvido se o usuário for o cuidador ou responsável associado à ocorrência/registro, e o id da foto é novamente vinculado ao pai consultado. As rotas não estão na lista pública da segurança. | `care_task/CareOccurrencePhotoService.java`, `get(...)` e `getManual(...)`, linhas 44–60; `SecurityConfig.java`, linhas 40–55 |
| Relatórios | Geração/edição/finalização são restritas ao cuidador da contratação. Leitura é limitada ao cuidador e responsável do relatório, e o responsável só vê versão finalizada. | `attendance_report/AttendanceReportService.java`, `generate(...)`, `get(...)`, `getById(...)`, `requireEditable(...)` e `requireCaregiverOwner(...)`, linhas 51–165 e 254–259 |
| Administração | As rotas administrativas são protegidas por role e auditadas. Os DTOs administrativos de responsável não incluem pessoa assistida, dados clínicos ou endereço assistencial. | `security/SecurityConfig.java`, linha 54; `admin/AdminService.java`, `user(...)`, `caregiver(...)`, `responsible(...)`; `admin/AdminDtos.java`, linhas 24–58 |
| Erros de acesso | Há respostas 401/403/404 controladas; regras de domínio usam `BusinessException` e o tratamento central produz código, mensagem e campos. | `common/GlobalExceptionHandler.java`, linhas 21–90; `common/ApiError.java`, linhas 6–12 |
| Frontend | Chamadas passam pelo cliente central que anexa Bearer token; fotos assistenciais usam endpoint autenticado. O frontend não é a barreira de autorização. | `front/src/services/api.ts`, `apiRequest(...)`, linhas 155–212, e `apiImageDataUrl(...)`, linhas 214–231; services em `front/src/services/` |
| Testes existentes | Há testes de acesso administrativo, ocorrência de outro cuidador, diário em contratação alheia e regras de participantes. | `AdminAuthorizationTest`, `TaskOccurrenceServiceTest.occurrenceFromAnotherCaregiverIsNotExposed`, `CareDiaryServiceTest.caregiverCannotCreateManualCareForAnotherCaregiversContract` e `caregiverCannotUseAssistedPersonOutsideContract` |

### Lacunas

| Lacuna | Risco | Prioridade |
|---|---|---|
| Oportunidades abertas retornam grau de dependência, mobilidade, descrição de necessidades e resumo da rotina para cuidadores ainda não vinculados. | Divulgação de dados assistenciais além do vínculo exigido pelo RNF10; enumeração em massa por paginação. A pseudonimização do nome e a redução do endereço diminuem, mas não eliminam, o impacto. | **Alta** |
| `locations(...)` deriva cidade e bairro de todas as oportunidades descobríveis para montar sugestões. | Exposição agregada de localização do cuidado antes do vínculo; pode facilitar inferência quando há poucos registros em uma região. | **Média** |
| A autorização é repetida em vários serviços e, em alguns casos, ocorre após `findById` não escopado. | Maior chance de divergência futura e enumeração de existência pelo contraste 403/404, embora o DTO sensível só seja montado após a verificação nos fluxos revisados. | **Média** |
| Não há testes de integração de autorização cruzada cobrindo sistematicamente fotos, relatório por id/contrato, agenda, perfil da pessoa assistida, rotina e detalhes completos da contratação. | Regressões de IDOR/BOLA podem não ser detectadas pela suíte atual. | **Alta** |
| Não foi encontrada uma política explícita de minimização/consentimento definindo quais campos assistenciais podem aparecer antes do aceite. | O código não permite distinguir uma divulgação de negócio deliberada de uma exposição excessiva. | **Alta** |

### Recomendações

- Definir formalmente uma projeção “pré-vínculo” mínima para oportunidades. Remover grau de dependência, mobilidade, texto livre de necessidades e detalhes de rotina, ou justificar cada campo com finalidade e consentimento explícitos.
- Liberar o detalhamento assistencial progressivamente: oportunidade anonimizada → solicitação/interesse aceito → contratação válida → atendimento em curso, conforme a necessidade de cada campo.
- Centralizar políticas de acesso assistencial em um serviço reutilizável com métodos por recurso/ação e, quando possível, usar consultas de repositório já escopadas ao participante.
- Padronizar resposta 404 para recursos de terceiros quando não houver necessidade de revelar sua existência.
- Adicionar testes de integração com dois responsáveis, dois cuidadores e um terceiro usuário, cobrindo GET e mutações de pessoa assistida, rotina, tarefa, ocorrência, foto, diário, agenda, contratação e relatório.

### Arquivos que precisariam ser ajustados

- `api/src/main/java/br/com/cuidaplus/api/service_request/ServiceOpportunityService.java`
- `api/src/main/java/br/com/cuidaplus/api/service_request/dto/ServiceOpportunityResponse.java`
- `api/src/main/java/br/com/cuidaplus/api/service_request/ServiceRequestRepository.java`
- `api/src/main/java/br/com/cuidaplus/api/care_task/TaskAuthorizationService.java` ou um novo componente comum de autorização
- Serviços assistenciais que hoje repetem verificações de participante
- `front/src/types/serviceOpportunity.ts` e telas de oportunidades, se a projeção for reduzida
- Testes de integração de segurança nos módulos citados

---

## RNF20 — Integridade transacional

**Status: PARCIALMENTE ATENDIDO**

### Fluxos avaliados

| Fluxo | Usa transação? | Risco de inconsistência | Evidência |
|---|---:|---|---|
| Cadastro de responsável | Sim | Baixo no banco: usuário, responsável, pessoa assistida e contato participam da mesma transação. | `AuthService.registerResponsible(...)`, linhas 127–171 |
| Cadastro de cuidador | Sim | **Médio/alto com foto:** banco é transacional, mas o arquivo já pode ter sido escrito quando ocorrer rollback. | `AuthService.registerCaregiver(...)`, linhas 174–213; `ProfilePhotoStorageService.store(...)`, linhas 35–60 |
| Criação/cancelamento de solicitação | Sim | Baixo no banco: entidade, snapshots, histórico e notificação compartilham a transação. | `ServiceRequestService.create(...)` e `cancel(...)` |
| Interesse em oportunidade | Sim + lock | Baixo/médio: usa lock pessimista, checagem de existência e índice único por oportunidade/cuidador. | `ServiceOpportunityService.apply(...)`, linhas 98–121; migration `V025__prevent_duplicate_service_opportunity_applications.sql` |
| Aceite/rejeição e criação de contratação | Sim + lock na entrada | Baixo: status, contratação, provisionamento, históricos e notificação ficam no mesmo fluxo transacional; há unicidade de contratação por solicitação. | `ServiceOpportunityService.accept(...)`; `CaregiverServiceRequestService.accept(...)`; `ServiceRequestDecisionService.accept(...)`, linhas 28–51; migration `V008__rf10_notifications_and_contracts.sql` |
| Encerramento/cancelamento | Sim | Baixo: alteração da contratação, histórico, auditoria e notificações são coordenados. | `ContractTerminationService.terminate(...)`, `cancelBeforeStart(...)` e `ContractStatusProcessorService` |
| Criação/edição de rotina com itens | Sim | Baixo no banco: entidade agregada e itens são persistidos sob transação. | `CareRoutineService.create(...)`, `update(...)`, `setActive(...)` |
| Tarefas recorrentes e ocorrências | Sim | Baixo/médio: transações e índices de idempotência reduzem duplicidade; há testes de recorrência/provisionamento. | `CareTaskService`; `TaskOccurrenceService`; `ContractCareTaskProvisioningService`; migrations `V017`, `V019` |
| Conclusão de ocorrência | Sim no banco | **Alto com foto:** status, atividade, auditoria e notificação revertem juntos, mas arquivos gravados antes de uma falha posterior ficam órfãos. | `TaskOccurrenceService.complete(...)`, linhas 163–197; `CareOccurrencePhotoService.attach(...)`, linhas 24–42 |
| Cuidado avulso/diário | Sim no banco | **Alto com foto:** registro é salvo, fotos são gravadas no disco e depois há auditoria; rollback do banco não remove arquivos. | `CareDiaryService.createManual(...)`, linhas 80–118 |
| Início/fim de atendimento | Sim + lock | Baixo: lock de contratação, verificações de duplicidade e constraint única por contrato/data/tipo. | `ServiceAttendanceService.start(...)` e `end(...)`, linhas 48–90; migration `V028__rf19_service_attendance.sql` |
| Geração/finalização de relatório | Sim + lock/unique | Baixo: relatório único por contrato/data; finalização, auditoria e notificação são atômicas. O e-mail é solicitado somente após commit e possui retry. | `AttendanceReportService.generate(...)` e `finalizeReport(...)`; `AttendanceReportEmailEventListener`, linhas 16–20; `AttendanceReportEmailWorker`; migrations `V029` e `V030` |
| Administração | Sim + lock | Baixo: estado, histórico e auditoria são transacionais; e-mail é agendado após commit. | `AdminService.blockUser(...)`, `unblockUser(...)`, `review(...)`, `reviewResponsible(...)`, linhas 96–205; `sendAfterCommit(...)` |
| Exclusão de conta | Sim + locks | Baixo no banco: validações, anonimização, limpeza e auditoria ocorrem juntas; remoção da foto é pós-commit. | `AccountDeletionService.deleteAccount(...)`, linhas 123–185; `deletePhotoAfterCommit(...)`, linhas 272–280 |
| Recuperação de senha | Sim no banco | Médio: token e auditoria são persistidos sob transação, mas o e-mail é chamado antes do commit. Um erro de commit posterior pode produzir link sem token confirmado. | `AuthService.requestPasswordReset(...)`, linhas 242–265 |

### Controles positivos de banco

- Chaves estrangeiras ligam contratações, tarefas, ocorrências, fotos, atendimentos e relatórios às entidades principais.
- Há unicidade para contratação por solicitação, ocorrência por tarefa/data/horário, manifestação por oportunidade/cuidador, atendimento por contrato/data/tipo, relatório por contrato/data, notificações deduplicadas e preferências por usuário/tipo.
- Há checks para status, coordenadas, precisão, datas e outros invariantes.
- Locks pessimistas são usados em decisões concorrentes, início/fim de atendimento, geração de relatório e operações administrativas/privacidade relevantes.
- O relatório de atendimento separa a confirmação de negócio do envio externo por `@TransactionalEventListener(AFTER_COMMIT)` e worker com tentativas.

### Lacunas

| Lacuna | Exemplo de estado inválido possível | Prioridade |
|---|---|---|
| Upload assistencial não tem compensação de rollback. | Arquivo presente em `uploads/care-occurrences` sem linha em `ocorrencia_cuidado_foto`, caso falhe o `save`, auditoria, atividade ou notificação posterior. | **Alta** |
| Upload de perfil não tem staging/compensação e não remove claramente a foto substituída. | Arquivo novo sem referência após rollback; arquivos antigos permanecem após várias trocas; cadastro com foto pode deixar arquivo após falha do banco. | **Alta** |
| E-mail de recuperação de senha é enviado dentro da transação. | Usuário recebe link cujo token não foi confirmado se o commit falhar; lentidão SMTP prolonga a transação. | **Média** |
| Ausência de testes de falha injetada após gravação de arquivo. | A suíte passa sem demonstrar limpeza de órfãos ou comportamento diante de falha parcial de storage/DB. | **Alta** |
| Alguns serviços auxiliares que gravam (`StatusHistoryService`, `TaskAuditService`, `CareActivityIntegrationService`) dependem da transação do chamador e não declaram a expectativa. | Uso futuro fora de um fluxo transacional pode persistir apenas parte da operação. Atualmente os usos críticos revisados partem de métodos transacionais. | **Baixa** |

### Recomendações

- Implementar staging de arquivo e promoção somente após commit, ou registrar compensação `afterCompletion` para excluir o arquivo quando a transação não confirmar.
- Na substituição de foto de perfil, preservar a referência anterior até o commit; remover o arquivo antigo após commit e remover o novo após rollback.
- Mover o e-mail de recuperação de senha para evento/outbox pós-commit, mantendo token, estado de entrega e retentativa.
- Documentar nos serviços auxiliares a propagação transacional esperada ou declarar `@Transactional(propagation = MANDATORY)` quando só puderem ser chamados dentro de transação.
- Criar testes de integração de rollback para cadastro, aceite, atendimento, conclusão com foto, diário com foto, relatório e exclusão de conta.

### Arquivos que precisariam ser ajustados

- `api/src/main/java/br/com/cuidaplus/api/care_task/CareOccurrencePhotoStorageService.java`
- `api/src/main/java/br/com/cuidaplus/api/care_task/CareOccurrencePhotoService.java`
- `api/src/main/java/br/com/cuidaplus/api/care_task/TaskOccurrenceService.java`
- `api/src/main/java/br/com/cuidaplus/api/care_task/CareDiaryService.java`
- `api/src/main/java/br/com/cuidaplus/api/storage/ProfilePhotoStorageService.java`
- `api/src/main/java/br/com/cuidaplus/api/profile/ProfileService.java`
- `api/src/main/java/br/com/cuidaplus/api/auth/AuthService.java`
- Testes de integração correspondentes

---

## RNF21 — Arquitetura modular e manutenibilidade

**Status: PARCIALMENTE ATENDIDO**

### Pontos positivos

| Área | Evidência |
|---|---|
| Backend por domínio | Pacotes separados para `auth`, `profile`, `service_request`, `care_task`, `agenda`, `service_attendance`, `attendance_report`, `admin`, `audit`, `notification`, `email`, `storage` e outros. |
| Camadas backend | Controllers delegam predominantemente a services; repositories isolam JPA; entidades e DTOs estão separados na maioria dos módulos. |
| Serviços externos | E-mail possui interface e implementações (`EmailService`, `SmtpEmailService`, `NoopEmailService`); fotos possuem storage próprio; auditoria e notificação são serviços independentes. |
| Segurança e erros | Autenticação JWT, obtenção do usuário autenticado e tratamento global de exceções estão centralizados. |
| DTOs | Endpoints assistenciais principais retornam records/classes de resposta, sem expor diretamente entidades JPA. |
| Frontend | Há separação entre rotas/telas (`src/app`), componentes, serviços HTTP, tipos, hooks, context, tema e utilitários. |
| Cliente HTTP | Autenticação, serialização, erros, redaction de diagnóstico e falha de rede são centralizados em `front/src/services/api.ts`. |
| Tipagem | O frontend passou em `tsc --noEmit`; services usam tipos próprios por domínio. |
| Testabilidade backend | Existem 27 classes de teste e 136 testes; a execução desta análise terminou com 136 aprovados, 0 falhas, 0 erros e 0 ignorados. |

### Problemas encontrados

| Problema | Impacto | Arquivo/área | Prioridade |
|---|---|---|---|
| Classes backend acumulam muitas responsabilidades. | Mudanças têm raio amplo e testes exigem muitos mocks; aumenta acoplamento. | `AuthService` (~395 linhas), `ProfileService` (~405 linhas), `AdminService` (~300 linhas), `AttendanceReportService` (~306 linhas), `CaregiverSearchService` (~301 linhas) | **Média** |
| Telas frontend muito grandes. | Estado, regras de formulário, integração, validação e apresentação ficam misturados; evolução e testes ficam caros. | `front/src/app/caregiver-search.tsx` (~909 linhas), `signup.tsx` (~837), `caregiver-profile/[id].tsx` (~420), `login.tsx` (~398) | **Alta** |
| Arquivos com classes inteiras em uma linha e formatação inconsistente. | Revisão, merge, depuração e análise estática ficam mais difíceis. | `NotificationService.java`, `NotificationController.java`, `CaregiverServiceRequestController.java` e trechos compactados em services/controllers | **Média** |
| Regras de papel/participação duplicadas. | Uma correção pode ser aplicada em um módulo e esquecida em outro, criando divergência de autorização. | Métodos `requireCaregiver`, `requireResponsible`, `requireParticipant`, `owned` em vários services | **Alta** |
| Respostas e paginação não têm abstração comum. | Consumidores precisam conhecer vários formatos; dificulta documentação e componentes reutilizáveis. | DTOs `*PageResponse`, paginação manual de notificações/admin e listas não paginadas | **Média** |
| Hardcode visual permanece relevante. | Tema e consistência visual ficam mais difíceis de manter; foram encontradas 93 ocorrências de cores hexadecimais em telas/componentes. | `front/src/app`, `front/src/components`, apesar de `front/src/theme/tokens.ts` | **Baixa** |
| Não foram encontrados testes automatizados do frontend. | Fluxos, serialização, tratamento de erro e regras de apresentação podem regredir sem detecção automática. | `front/` | **Média** |
| README da API não acompanha a arquitetura real. | A descoberta e manutenção dos módulos dependem de leitura do código. | `api/README.md` | **Média** |

### Recomendações

- Criar DTO explícito e versionável para `/api/profile/me`, com tipos aninhados para responsável, cuidador e pessoas assistidas.
- Extrair políticas de autorização e participação para componentes de domínio reutilizáveis e testados.
- Dividir services grandes por caso de uso, mantendo uma fachada apenas quando necessária.
- Decompor as maiores telas em hooks de carregamento/mutação, schemas/validações e componentes de seções, mantendo services como única porta HTTP.
- Aplicar formatter Java/TypeScript e impedir arquivos compactados em uma linha por verificação automatizada.
- Consolidar tokens visuais e substituir cores hardcoded gradualmente.
- Introduzir testes de services, hooks e fluxos críticos do frontend.

### Arquivos que precisariam ser ajustados

- `api/src/main/java/br/com/cuidaplus/api/profile/ProfileService.java` e novos DTOs de perfil
- Services com verificações duplicadas de papel/vínculo
- `api/src/main/java/br/com/cuidaplus/api/notification/NotificationService.java`
- Controllers excessivamente compactados
- `front/src/app/caregiver-search.tsx`
- `front/src/app/signup.tsx`
- `front/src/app/caregiver-profile/[id].tsx`
- `front/src/app/login.tsx`
- `front/src/theme/tokens.ts` e telas/componentes com cores hardcoded
- Estrutura de testes do frontend

---

## Ações recomendadas

### Alta prioridade

- Reduzir e formalizar a projeção pré-vínculo das oportunidades, com testes que comprovem que dados assistenciais completos só aparecem após o vínculo permitido.
- Garantir compensação/staging transacional para fotos assistenciais e de perfil.
- Criar matriz de testes de autorização cruzada para todos os recursos assistenciais.
- Centralizar políticas de autorização assistencial para evitar divergência entre módulos.

### Média prioridade

- Mover recuperação de senha para entrega pós-commit com estado/retry.
- Decompor services e telas grandes e iniciar testes automatizados no frontend.

### Baixa prioridade

- Declarar/documentar a propagação exigida pelos serviços auxiliares de histórico/auditoria/atividade.
- Aplicar formatação consistente aos arquivos Java excessivamente compactados.
- Reduzir hardcodes de cor e consolidar o tema do frontend.
- Atualizar o mapa arquitetural e a documentação de manutenção.

## Verificações automatizadas executadas

| Verificação | Resultado |
|---|---|
| Backend Maven (`mvn test`) | **Aprovado:** 27 suítes, 138 testes, 0 falhas, 0 erros, 0 ignorados |
| Frontend TypeScript (`tsc --noEmit`) | **Aprovado** |
| Frontend lint (`expo lint --max-warnings=0`) | **Aprovado** |

Os testes comprovam os cenários codificados, mas não substituem os testes faltantes de autorização cruzada e de rollback de arquivos apontados neste relatório.

## Arquivos analisados

### Backend

- Configuração e build: `api/pom.xml`, `api/src/main/resources/application.properties`, `api/README.md`
- Segurança/erros: `security/*`, `common/*`
- Domínios: `account/*`, `admin/*`, `agenda/*`, `attendance_report/*`, `audit/*`, `auth/*`, `care_contract/*`, `care_routine/*`, `care_task/*`, `caregiver/*`, `contract_history/*`, `contract_termination/*`, `email/*`, `notification/*`, `profile/*`, `service_attendance/*`, `service_request/*`, `status_history/*`, `storage/*`, `user/*`
- Controllers, services, repositories, entidades, DTOs e mappers desses módulos
- Testes em `api/src/test/java/`

### Banco e documentação

- Migrations em `api/src/main/resources/db/migration/`, com atenção especial a `V008`, `V011`, `V014`–`V025`, `V028`–`V030` e `V044`–`V050`
- `docs/modelagem-banco-dados.md`
- Documentos em `docs/`, incluindo `rf14-care-tasks.md`, relatórios de minimização/normalização e documentação de nomenclatura/migrations

### Frontend

- `front/package.json`, `front/app.json`, `front/tsconfig.json`, `front/eslint.config.js`
- `front/src/services/*`, `front/src/types/*`, `front/src/context/*`, `front/src/hooks/*`, `front/src/components/*`, `front/src/theme/*`, `front/src/utils/*`
- Telas/rotas em `front/src/app/`, com foco em perfil da pessoa assistida, solicitações/oportunidades, contratos, agenda, tarefas/ocorrências, diário, fotos, atendimento, relatórios e administração

## Limitações da análise

- Não houve conexão nem consulta a banco de dados real; nenhum dado real foi lido ou impresso.
- Os testes de integração usam H2 e não comprovam todos os detalhes específicos do PostgreSQL/Flyway em produção.
- Não foram executados testes dinâmicos de segurança, fuzzing, teste de carga ou inspeção de tráfego em ambiente implantado.
- A análise de autorização considera os fluxos alcançáveis pelo código e testes atuais; configurações de gateway, proxy, storage externo e infraestrutura fora do repositório não são avaliáveis.
- O diretório já possuía alterações locais em `ServiceAttendanceService.java`, seu artefato compilado e uma foto assistencial não rastreada. Elas foram preservadas e o código-fonte atual foi considerado na análise.
- A documentação Expo 56 não foi necessária porque nenhuma alteração de código Expo foi feita; o `front/package.json` atualmente declara Expo `~54.0.0`.

## Parecer final

O projeto possui uma base consistente de autenticação, autorização por participante, transações JPA, locks, constraints, separação modular e tratamento de erros. Entretanto, a exposição assistencial pré-vínculo, a lacuna de atomicidade nos uploads, a concentração de responsabilidades e a ausência de documentação OpenAPI impedem a classificação integral dos quatro RNFs como atendidos.

Análise concluída. Aguardo autorização para aplicar as correções recomendadas.
