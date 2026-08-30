# Colunas candidatas à tradução

Revisão realizada em 30/08/2026 sobre o schema `public` após a V040. O catálogo contém 36 tabelas e 417 colunas. O inventário técnico integral, incluindo tipo, nulabilidade, chaves, checks, defaults e índices, está em [Inventário técnico de colunas](inventario-colunas-schema.md).

## Método

Todas as colunas foram decompostas em tokens e comparadas com o vocabulário físico em português. As candidatas foram cruzadas com entidades e coleções JPA, `PortuguesePhysicalNamingStrategy`, repositories, SQL nativo, serviços, DTOs, frontend, migrations e documentação. Os exemplos `care_group_id` e `care_group_name_snapshot` não existem mais: foram removidos pela V040 após análise de legado.

## Classificação das candidatas

| Tabela | Coluna atual | Motivo da suspeita | Nome sugerido | Dados atuais | Classificação | Ação |
|---|---|---|---|---:|---|---|
| `solicitacoes_servico` | `nome_rotina_snapshot` | `snapshot` é termo inglês e o domínio já usa `copia` | `nome_rotina_copia` | 16 valores em 23 linhas | Traduzir | Renomear preservando valores |
| `solicitacoes_servico_itens_cuidado_copias_dias_semana` | `item_snapshot_id` | FK ainda usa `snapshot`, embora a tabela pai use `copias` | `item_copia_id` | 0 linhas | Traduzir | Renomear coluna e normalizar nome da FK |
| `tarefas_cuidado` | `item_snapshot_origem_id` | FK de origem ainda usa `snapshot` | `item_copia_origem_id` | 38 valores em 41 linhas | Traduzir | Renomear coluna, FK e índice unique parcial |

As três colunas são ativas. Elas não são candidatas à remoção: guardam o nome congelado da rotina, a recorrência semanal da cópia e a origem da tarefa provisionada. Participam de RF08 e RF13 e apoiam RF12, RF14 e RF15 por encadeamento.

## Termos mantidos com justificativa

| Termo físico | Classificação | Justificativa |
|---|---|---|
| `id` | Manter com justificativa | Identificador técnico universal e padrão de todas as PKs/FKs. |
| `email` | Manter com justificativa | Termo técnico consolidado no domínio e no vocabulário oficial do produto. |
| `status` | Manter com justificativa | Termo de domínio consolidado, usado uniformemente em entidades, enums e documentação. |
| `token` e `hash` | Manter com justificativa | Termos técnicos de segurança; traduções seriam menos precisas. |
| `url` | Manter com justificativa | Sigla técnica universal. |
| `cpf` e `cep` | Manter com justificativa | Siglas brasileiras oficiais. |
| `latitude` e `longitude` | Manter com justificativa | Termos geográficos também válidos em português. |
| `utc` | Manter com justificativa | Padrão internacional de tempo. |
| Colunas de `flyway_schema_history` | Manter com justificativa | Estrutura de infraestrutura criada e controlada pelo Flyway; não pertence ao modelo de domínio. |

Não foram encontradas outras colunas de domínio em inglês. Não há item classificado como remoção ou dúvida manual nesta revisão.

## Impacto no código

- `ServiceRequest.careRoutineNameSnapshot`, `ServiceRequestCareItemSnapshot.weekdays` e `CareTask.sourceSnapshotItem` continuam com os mesmos nomes Java e o mesmo contrato de API.
- Apenas os valores físicos correspondentes em `PortuguesePhysicalNamingStrategy` precisam mudar.
- Nenhuma query nativa ativa referencia as três colunas. A única query nativa identificada opera sobre `ocorrencias_cuidado` com nomes já em português.
- O frontend não conhece os nomes físicos e não requer alteração. Propriedades JSON em inglês são contratos da API, não colunas do banco.
- Migrações V001–V040 permanecem imutáveis; a convergência será feita exclusivamente pela V041.

## Plano de tradução segura

1. Criar `V041__traduzir_colunas_restantes_para_portugues.sql` usando somente `ALTER TABLE ... RENAME COLUMN`.
2. Tornar cada rename condicional para aceitar tanto o nome anterior quanto o estado já traduzido e falhar em estados ambíguos.
3. Recalcular nomes de constraints e índices apenas nas tabelas afetadas, respeitando o limite de 63 caracteres usado nas V038/V039.
4. Atualizar os três valores físicos da estratégia de nomenclatura sem alterar propriedades Java, DTOs ou JSON.
5. Atualizar o dicionário principal e manter os documentos de migrations anteriores explicitamente históricos.
6. Comparar contagens e valores não nulos antes/depois; validar Flyway em banco evoluído e limpo, Hibernate, testes backend, TypeScript e lint.

## Resultado da execução

A V041 foi aplicada ao banco evoluído e também validada pela cadeia limpa V001–V041 em schema temporário. Nos dois cenários, o catálogo final contém 35 tabelas de domínio mais `flyway_schema_history`, 417 colunas, os três nomes novos e nenhum dos três nomes anteriores.

- Os 16 nomes de rotina e as 38 referências de origem não nulas foram preservados; a coleção semanal continuou vazia.
- As FKs, a PK composta e o índice unique parcial foram preservados e receberam nomes coerentes com `item_copia_id` e `item_copia_origem_id`.
- Uma execução transacional com rollback comparou a impressão digital dos valores antes e depois e também confirmou a segurança da execução condicional repetida.
- O Hibernate iniciou com `ddl-auto=validate`, sem divergência entre o mapeamento JPA e o catálogo físico.
- A suíte backend concluiu 93 testes sem falhas, erros ou testes ignorados; TypeScript (`tsc --noEmit`) e `expo lint` também passaram.
- O PostgreSQL utilizado está na versão 17.7. O Flyway embarcado avisa que sua faixa oficialmente testada vai até PostgreSQL 16, mas não houve falha de migração ou validação.
