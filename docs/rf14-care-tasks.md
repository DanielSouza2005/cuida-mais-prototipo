# RF14 — Lista de Tarefas

## Visão técnica

O RF14 usa uma série (`care_tasks`) como configuração da rotina e ocorrências (`task_occurrences`) como execuções independentes. A funcionalidade reutiliza os usuários, pessoas assistidas e contratos existentes. Não há duplicação de responsáveis, cuidador ou dados contratuais.

Uma tarefa sem data final não é materializada indefinidamente. Na criação, a API gera no máximo 60 dias. Consultas futuras estendem somente o intervalo solicitado, com limite de 90 dias. A verificação em aplicação e a restrição única `(task_id, scheduled_date, scheduled_time)` tornam a geração idempotente.

Datas recorrentes são armazenadas como `LocalDate`, horários como `LocalTime` e o timezone como identificador IANA. Cada ocorrência também persiste o instante UTC calculado com `ZonedDateTime`. Assim, uma rotina às 14h permanece às 14h no timezone cadastrado e o instante real pode ser comparado com segurança.

O status `ATRASADA` é calculado durante a consulta quando uma ocorrência ainda está `PENDENTE` e seu instante UTC já passou. A exibição correta não depende da execução de um scheduler.

Ao concluir uma ocorrência, a API cria um registro em `care_activity_records` na mesma transação. Essa tabela é a integração persistente mínima com o futuro RF16. Uma falha nesse registro reverte a conclusão.

## Endpoints do responsável

- `POST /api/responsible/care-tasks`
- `GET /api/responsible/care-tasks/form-data`
- `GET /api/responsible/care-tasks`
- `GET /api/responsible/care-tasks/{taskId}`
- `PUT /api/responsible/care-tasks/{taskId}`
- `PATCH /api/responsible/care-tasks/{taskId}/pause`
- `PATCH /api/responsible/care-tasks/{taskId}/reactivate`
- `PATCH /api/responsible/care-tasks/{taskId}/cancel`
- `GET /api/responsible/care-tasks/{taskId}/occurrences`
- `GET /api/responsible/care-tasks/occurrences/{occurrenceId}`
- `PATCH /api/responsible/care-tasks/occurrences/{occurrenceId}/cancel`

A listagem aceita busca, categoria, situação da série, situação da ocorrência, prioridade, pessoa assistida, cuidador e período. A listagem de ocorrências aceita período, situação, paginação e ordenação de histórico.

## Endpoints do cuidador

- `GET /api/caregiver/care-tasks/day`
- `GET /api/caregiver/care-tasks/occurrences`
- `GET /api/caregiver/care-tasks/occurrences/{occurrenceId}`
- `PATCH /api/caregiver/care-tasks/occurrences/{occurrenceId}/complete`
- `PATCH /api/caregiver/care-tasks/occurrences/{occurrenceId}/not-completed`

Todos os endpoints resolvem o usuário pelo JWT. IDs de usuário enviados pelo cliente não determinam autorização.

## Regras de autorização

- O responsável só gerencia tarefas de uma pessoa assistida e contratação próprias.
- O cuidador informado deve ser o cuidador do contrato.
- Somente contratos ativos ou com encerramento agendado podem receber tarefas.
- O cuidador só consulta e executa ocorrências vinculadas a ele.
- Contratos encerrados ou cancelados não geram ocorrências e bloqueiam execução.
- Versões divergentes retornam conflito, evitando sobrescrita silenciosa.

## Roteiro manual essencial

1. Entre como responsável e abra `Perfil > Rotina de cuidados`.
2. Crie tarefas única, diária, em dias específicos, por intervalo, por período e sem data final.
3. Crie uma tarefa de medicação e confira o aviso informativo.
4. Pause, reative e cancele uma série.
5. Abra uma ocorrência e teste os três escopos de edição.
6. Entre como o cuidador do contrato e abra `Tarefas de hoje`.
7. Conclua uma ocorrência com observação e confirme o registro de atividade.
8. Marque outra como não realizada, validando a justificativa obrigatória.
9. Confirme que outro cuidador não consegue abrir a ocorrência alterando o ID.
10. Encerre o contrato e confirme que não são criadas novas ocorrências nem permitidas novas execuções.
11. Confira uma tarefa vencida como `Atrasada` sem executar processo agendado.
12. Altere a versão em duas sessões e confirme a resposta de conflito na segunda atualização.
