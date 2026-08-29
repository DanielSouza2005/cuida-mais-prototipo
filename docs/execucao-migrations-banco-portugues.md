# Execução segura das migrations de tradução

## Motivo da espera prolongada

`ALTER TABLE ... RENAME` é uma operação rápida de catálogo, mas precisa obter um `ACCESS EXCLUSIVE LOCK`. A migration monolítica anterior não limitava a espera. Uma transação antiga, outra instância da API, um worker ou até uma ferramenta de administração com transação aberta podia bloquear o primeiro rename por horas.

As migrations foram divididas em lotes independentes:

| Migration | Escopo |
|---|---|
| V031 | Usuários e perfis principais. |
| V032 | Contatos e coleções auxiliares dos perfis. |
| V033 | Solicitações, contratações e histórico. |
| V034 | Rotinas e snapshots. |
| V035 | Tarefas, ocorrências, diário, auditoria, lembretes e fotos. |
| V036 | Notificações, atendimento e relatórios. |
| V037 | Constraints e índices. |
| V038 | Estruturas legadas de grupos e itens de cuidado e remoção do termo `snapshot`. |
| V039 | Padronização relacional: tabelas filhas passam a iniciar pelo domínio principal. |

Cada arquivo é uma transação Flyway própria. Se um lote falhar, os anteriores permanecem concluídos e a próxima execução retoma a partir dele. Cada lote espera no máximo 10 segundos por um lock e possui limite total de 2 minutos por statement.

A V037 restringe explicitamente sua varredura às 35 tabelas de domínio. Objetos de infraestrutura, especialmente `flyway_schema_history`, não são alterados. Essa restrição evita tentar renomear a chave primária da tabela que o próprio Flyway mantém bloqueada durante a execução.

## 1. Interromper a execução antiga

Se a V031 antiga ainda estiver em execução, cancele primeiro a operação no cliente que a iniciou. Em PostgreSQL, o cancelamento de uma migration Flyway em execução dentro de transação desfaz o lote incompleto.

Para localizar a sessão e seus bloqueadores, execute com uma conta que possa consultar `pg_stat_activity`:

```sql
SELECT
  activity.pid,
  activity.usename,
  activity.application_name,
  activity.state,
  activity.wait_event_type,
  activity.wait_event,
  now() - activity.query_start AS duracao,
  pg_blocking_pids(activity.pid) AS pids_bloqueadores,
  left(activity.query, 500) AS consulta
FROM pg_stat_activity activity
WHERE activity.datname = current_database()
  AND activity.pid <> pg_backend_pid()
ORDER BY activity.query_start;
```

Uma migration bloqueada normalmente aparece com `wait_event_type = 'Lock'` e um ou mais valores em `pids_bloqueadores`.

Para inspecionar somente os bloqueadores:

```sql
SELECT
  blocked.pid AS pid_bloqueado,
  blocker.pid AS pid_bloqueador,
  blocker.usename,
  blocker.application_name,
  blocker.state,
  now() - blocker.xact_start AS duracao_transacao,
  left(blocker.query, 500) AS consulta_bloqueadora
FROM pg_stat_activity blocked
CROSS JOIN LATERAL unnest(pg_blocking_pids(blocked.pid)) blocking_pid
JOIN pg_stat_activity blocker ON blocker.pid = blocking_pid;
```

Prefira encerrar normalmente a aplicação ou confirmar/descartar a transação no cliente que mantém o lock. Se isso não for possível, um administrador pode cancelar somente a consulta identificada:

```sql
SELECT pg_cancel_backend(<pid_confirmado>);
```

`pg_terminate_backend` deve ficar restrito ao último recurso e somente depois de confirmar o PID e o impacto da transação.

## 2. Conferir o estado do Flyway

```sql
SELECT installed_rank, version, description, installed_on, execution_time, success
FROM flyway_schema_history
WHERE version BETWEEN '31' AND '37'
ORDER BY installed_rank;
```

- Se não houver V031, a execução antiga foi revertida ou não chegou a ser registrada; prossiga normalmente.
- Se houver V031 com `success = false`, faça o reparo do histórico Flyway antes de tentar novamente.
- Se a V031 monolítica anterior aparecer com `success = true`, não execute estes arquivos sobre esse banco sem uma análise específica: o schema provavelmente já foi traduzido e o checksum da V031 será diferente.

As funções auxiliares e os renames são idempotentes quanto ao estado antigo/novo. Isso permite recuperar uma execução manual parcialmente confirmada, mas estados ambíguos interrompem a migration em vez de esconder inconsistências.

## 3. Janela de manutenção

Antes de reiniciar o Flyway:

1. Pare todas as instâncias da API e workers conectados ao banco.
2. Feche ou confirme transações abertas em IDEs e ferramentas SQL.
3. Confirme que não existem sessões `idle in transaction`.
4. Realize backup ou snapshot do banco.
5. Inicie uma única instância responsável por executar o Flyway.
6. Depois da V037, inicie as demais instâncias.

Consulta útil para transações esquecidas:

```sql
SELECT pid, usename, application_name, state, now() - xact_start AS duracao, left(query, 500)
FROM pg_stat_activity
WHERE datname = current_database()
  AND state = 'idle in transaction'
ORDER BY xact_start;
```

## 4. Validação final

```sql
SELECT version, description, execution_time, success
FROM flyway_schema_history
WHERE version BETWEEN '31' AND '37'
ORDER BY installed_rank;
```

Todas as sete versões devem estar com `success = true`. Em seguida, inicie a aplicação com `spring.jpa.hibernate.ddl-auto=validate` e execute os fluxos de regressão descritos em [modelagem-banco-dados.md](modelagem-banco-dados.md).
