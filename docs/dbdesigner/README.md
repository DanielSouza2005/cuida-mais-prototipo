# Script DBDesigner — Cuidar+

## Origem

Os arquivos desta pasta foram gerados por introspecção direta do schema `public` do banco PostgreSQL local de desenvolvimento (`localhost:5433/cuida_plus`), no estado registrado pelo Flyway até a versão `050`. A extração consultou somente os catálogos do PostgreSQL, dentro de uma transação marcada como somente leitura.

## Arquivos

- `cuida_mais_schema_dbdesigner.sql`: versão principal para importação visual no DBDesigner.
- `cuida_mais_schema_postgresql.sql`: versão fiel aos tipos, defaults, constraints e índices retornados pelo PostgreSQL.

## O que foi incluído

- 40 tabelas de domínio e 467 colunas;
- 40 chaves primárias;
- 87 chaves estrangeiras;
- 14 constraints de unicidade;
- 19 constraints de verificação;
- 106 índices existentes, dos quais 52 são índices secundários declarados explicitamente nos arquivos. Os outros 54 são produzidos pelas constraints de chave primária ou unicidade.

Todas as 40 tabelas de domínio possuem chave primária. A migration `V050` acrescentou chaves compostas a `cuidador_disponibilidade_dia`, `cuidador_disponibilidade_periodo`, `cuidador_formacao`, `cuidador_modalidade`, `cuidador_servico`, `pessoa_assistida_alergia` e `pessoa_assistida_restricao_alimentar`, sem remover registros.

## Principais grupos funcionais

| Grupo | Tabelas principais | Requisitos relacionados |
|---|---|---|
| Identidade e perfis | `usuario`, `responsavel`, `cuidador`, `pessoa_assistida` e tabelas associativas dos perfis | RF01–RF07, RF20, RF21 e RNF08 |
| Recuperação e exclusão da conta | `usuario_token_redefinicao_senha`, `usuario_confirmacao_exclusao`, `usuario_exclusao_auditoria` | RF03 e RF22 |
| Solicitações e contratações | `solicitacao_servico`, suas tabelas auxiliares, `contratacao` e histórico de status | RF08–RF13 e RF18 |
| Rotinas, tarefas e execução do cuidado | `rotina_cuidado`, `tarefa_cuidado`, `ocorrencia_cuidado`, lembretes, fotos e diário | RF14–RF17 |
| Atendimento e relatório | `registro_atendimento`, `relatorio_atendimento` | RF19 |
| Notificações | `notificacao`, `notificacao_preferencia` | RF09, RF11, RF14 e RF17–RF22 |
| Administração e auditoria | históricos de situação e `auditoria_acao_critica` | RF20, RF21 e RNF25 |

## Adaptações da versão DBDesigner

Para reduzir problemas de importação, a versão DBDesigner converte `uuid` em `varchar(36)`, timestamps com ou sem fuso em `timestamp`, `json`/`jsonb` em `text`, `numeric` em `decimal`, `character varying` em `varchar` e tipos `time` em `time`. Defaults de UUID e sequências são omitidos somente nessa versão. Casts específicos do PostgreSQL foram removidos, verificações baseadas em `ANY (ARRAY...)` foram convertidas para `IN (...)`, e índices não usam qualificação `public` nem `USING btree`.

A versão PostgreSQL deve ser usada como referência técnica quando for necessário conferir o tipo ou a expressão exata existente no banco.

## O que foi omitido

- todos os dados e registros reais;
- valores de usuários, senhas, hashes e tokens;
- notificações e eventos de auditoria armazenados;
- a tabela `flyway_schema_history`, por ser infraestrutura do Flyway;
- ownership, grants, extensões, procedures e comandos administrativos.

Os nomes estruturais `senha_hash`, `hash_token` e a tabela de redefinição de senha aparecem porque fazem parte do schema, mas nenhum valor dessas colunas foi consultado ou exportado.

## Importação

Importe `cuida_mais_schema_dbdesigner.sql` como um script SQL PostgreSQL no DBDesigner. O arquivo cria primeiro todas as tabelas, adiciona depois as chaves estrangeiras e, por fim, declara os índices secundários. Dependendo da edição do importador, índices parciais com cláusula `WHERE` podem exigir confirmação manual; eles foram preservados porque fazem parte da estrutura real.
