# Análise de estruturas legadas

Análise histórica realizada em 30/08/2026 sobre o banco com Flyway na versão `039`. As estruturas legadas avaliadas foram removidas pela V040 e não chegaram à renomeação da V043. Os nomes plurais abaixo identificam o estado que foi auditado, não o schema vigente; a nomenclatura atual está no [dicionário da V043](dicionario-renomeacao-tabelas-singular.md).

## Escopo e método

Foram pesquisados backend, frontend, testes, migrações e documentação por nomes físicos, nomes Java/TypeScript e conceitos relacionados a grupos, itens de cuidado, snapshots, tarefas, ocorrências e históricos. No banco, foram verificados volume de dados, chaves estrangeiras, views, triggers e rotinas armazenadas. A inspeção do banco foi somente leitura.

| Estrutura | Tipo | Referências encontradas | Dados existentes | Substituto atual | Classificação | Ação recomendada |
|---|---|---|---:|---|---|---|
| `grupos_cuidado` | Tabela | Somente V038, V039 e documentação; FK recebida apenas de estruturas também legadas e de `solicitacoes_servico.care_group_id` | 0 | `rotinas_cuidado` | Legado removível | Remover após as filhas e a coluna dependente |
| `grupos_cuidado_itens` | Tabela | Somente V038, V039 e documentação | 0 | `rotinas_cuidado_itens` | Legado removível | Remover antes de `grupos_cuidado` |
| `solicitacoes_servico_grupos_cuidado_itens_copias` | Tabela | Somente V038, V039 e documentação; referenciada por `contratacoes_itens_cuidado` | 0 | `solicitacoes_servico_itens_cuidado_copias` | Legado removível | Remover após `contratacoes_itens_cuidado` |
| `contratacoes_itens_cuidado` | Tabela | Somente V038, V039 e documentação; referenciada pelo histórico legado | 0 | `tarefas_cuidado` e `ocorrencias_cuidado` | Legado removível | Remover após o histórico legado |
| `contratacoes_itens_cuidado_historico` | Tabela | Somente V038, V039 e documentação | 0 | `tarefas_cuidado_auditoria` e `registros_diario_cuidado` | Legado removível | Remover primeiro |
| `solicitacoes_servico.care_group_id` | Coluna/FK | Não é mapeada por JPA nem exposta por DTO/API; aparece apenas na documentação e no banco evoluído | 0 valores não nulos em 23 linhas | `rotina_cuidado_id` e snapshots estruturados | Legado removível | Remover a FK e a coluna |
| `solicitacoes_servico.care_group_name_snapshot` | Coluna | Não é mapeada por JPA nem exposta por DTO/API; aparece apenas na documentação e no banco evoluído | 0 valores não nulos em 23 linhas | `nome_rotina_copia` | Legado removível | Remover a coluna |
| `CareGroup`, `CareGroupItem`, `CareItemCopy`, `ContractCareItem` e variações | Conceitos de código | Nenhuma entidade, repositório, serviço, controller, DTO, rota ou tipo atual; nomes aparecem apenas em migrações históricas/documentação | — | Rotinas, snapshots estruturados, tarefas e ocorrências | Legado removível | Não há classe atual a excluir; retirar menções que descrevam schema vigente |
| V038 e V039 | Migrações históricas | Traduzem condicionalmente nomes antigos, inclusive os candidatos | — | V040 fará a remoção posterior | Em uso | Preservar imutáveis para compatibilidade do histórico Flyway |
| V012 e V013 no histórico do banco | Migrações ausentes do repositório atual | Banco registra `care task groups` e `care groups and contract items`; `ignore-migration-patterns=*:missing` permite o histórico evoluído | — | Cadeia limpa atual não cria as estruturas legadas | Dúvida/requer decisão manual | Não reconstruir nem alterar; cobrir ambos os estados com V040 condicional |
| `StatusHistory` / `solicitacoes_servico_contratacoes_historico_status` | Entidade/tabela | Entidade, repositório, serviço, serviços de solicitação/contrato, DTOs e testes ativos | Dados operacionais | Não se aplica | Em uso | Manter |
| `atividade`, `outra_atividade` e `solicitacoes_servico_atividades` | Colunas/coleção | Entidade `ServiceRequest`, serviço, DTOs, telas e RF08 | Dados operacionais | Não se aplica | Em uso | Manter |
| Rotinas, snapshots, tarefas, ocorrências, diário e auditoria atuais | Entidades/tabelas | Entidades, repositórios, serviços, controllers, DTOs, frontend e testes | 1 rotina, 3 itens, 47 snapshots, 41 tarefas, 70 ocorrências, 8 registros de diário e 68 auditorias | Não se aplica | Em uso | Manter |

## Evidências de substituição

O fluxo atual parte de `rotinas_cuidado` e `rotinas_cuidado_itens`, congela o conteúdo solicitado em `solicitacoes_servico_itens_cuidado_copias`, provisiona `tarefas_cuidado` e registra execução em `ocorrencias_cuidado`, `registros_diario_cuidado` e `tarefas_cuidado_auditoria`. Essas estruturas são mapeadas por JPA e consumidas pelos serviços e pelo frontend.

As tabelas legadas e os campos `care_group_*` estão vazios. Portanto, neste banco não há registros a migrar nem conteúdo histórico a preservar. A remoção elimina somente schema sem uso; os dados funcionais já residem no modelo atual.

Não foram encontradas views, triggers ou funções armazenadas que mencionem as estruturas candidatas. As únicas FKs são as do próprio subgrafo legado, as ligações desse subgrafo com tabelas atuais e a FK de `solicitacoes_servico.care_group_id`. Também não foi encontrada referência em integração versionada no repositório. Consumidores externos não versionados permanecem fora do alcance observável desta análise.

## Plano de remoção segura

1. Criar somente uma nova migração, `V040__remove_estruturas_legadas_nao_utilizadas.sql`; não editar migrações já aplicadas.
2. Antes de qualquer `DROP`, fazer a própria migração abortar se alguma tabela legada tiver linhas ou se algum campo `care_group_*` tiver valor não nulo. Assim, outro ambiente com dados históricos exige decisão e migração manual em vez de perda silenciosa.
3. Usar verificações condicionais para funcionar tanto no banco evoluído, que possui V012/V013 no histórico, quanto em um banco criado pela cadeia atual, onde essas migrações não existem.
4. Remover na ordem das dependências: histórico de itens, itens de contratação, cópias antigas da solicitação, itens de grupo, FK/colunas de solicitação e, por último, grupos.
5. Não usar `CASCADE`; uma dependência inesperada deve interromper a migração.
6. Atualizar a documentação para representar somente o schema vigente e conservar este relatório como rastreabilidade histórica.
7. Validar compilação/testes, mapeamento JPA e Flyway em schema limpo e em uma cópia/execução transacional do estado evoluído.

## Riscos e contenções

- Um ambiente diferente pode conter dados legados. A guarda de volume da V040 impedirá a remoção automática nesse caso.
- Uma integração externa não versionada pode consultar nomes antigos. A busca não encontrou qualquer evidência local; a ausência de `CASCADE` e a validação prévia reduzem o risco técnico, mas a confirmação organizacional de consumidores externos continua sendo responsabilidade operacional.
- O banco evoluído contém V012 e V013, mas os arquivos não estão na cadeia atual. A V040 será idempotente quanto à existência das estruturas, sem tentar reescrever o histórico Flyway.

## Resultado da implementação e validação

- A V040 foi criada e aplicada com sucesso ao banco de desenvolvimento configurado. O catálogo final possui 35 tabelas de domínio mais `flyway_schema_history`, sem as cinco tabelas e sem as duas colunas legadas.
- O fluxo evoluído foi ensaiado primeiro dentro de uma transação com rollback; o fluxo limpo foi validado em schema temporário pela cadeia completa V001–V040. A aplicação iniciou com `ddl-auto=validate` nos dois casos.
- Uma fixture temporária com uma linha em `grupos_cuidado` confirmou que a guarda interrompe a migração antes da remoção.
- A suíte backend passou com 93 testes, sem falhas, erros ou testes ignorados.
- O frontend passou em `tsc --noEmit` e `expo lint`. Não houve mudança funcional no frontend porque ele não referencia nomes físicos do banco.
- O PostgreSQL utilizado está na versão 17.7. O Flyway embarcado informou que sua faixa oficialmente testada vai até PostgreSQL 16; trata-se de aviso de compatibilidade da dependência atual, sem falha na V040.
