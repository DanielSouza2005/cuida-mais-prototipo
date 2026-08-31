# Análise de campos candidatos à remoção

Análise histórica realizada em 30/08/2026 sobre o schema `public` na V041, antes de qualquer remoção. Os nomes plurais abaixo identificam aquele estado. A V042 executou a remoção aprovada e a V043 renomeou as tabelas de domínio para o singular; a correspondência atual está no [dicionário da V043](dicionario-renomeacao-tabelas-singular.md).

## Resultado por campo

| Campo | Onde aparece e como é usado | Dados no banco | Classificação | Ação recomendada | Justificativa |
|---|---|---:|---|---|---|
| `cuidadores.formacao` | `CaregiverProfile`, compatibilidade singular nos DTOs de cadastro/edição e resposta de perfil | 3 valores; os 3 já existem em `cuidadores_formacoes` | Legado removível | Migrar defensivamente e remover | A fonte ativa de cadastro, edição, busca, filtro e perfil é a coleção `formacoes`; a V004 já migrou o singular para a coleção. |
| `cuidadores.latitude` | `AddressFields`, cadastro/edição/endereço do perfil e cálculo Haversine em `CaregiverSearchService` | 0 de 3 cuidadores | Necessário para regra atual | Manter | Mesmo vazias neste banco, são a coordenada de destino da busca por proximidade de cuidadores (RF06/RF07). Ausência de dados não elimina a regra ativa. |
| `cuidadores.longitude` | Mesmo fluxo de `cuidadores.latitude` | 0 de 3 cuidadores | Necessário para regra atual | Manter | O cálculo de distância exige o par latitude/longitude. |
| `pessoas_assistidas.latitude` | `AddressFields`, cadastro/edição do endereço do cuidado e cálculo Haversine em `ServiceOpportunityService` | 0 de 1 pessoa | Necessário para regra atual | Manter | É a coordenada de destino na busca de oportunidades pelo cuidador (RF08/RF17) e representa o endereço do cuidado. |
| `pessoas_assistidas.longitude` | Mesmo fluxo de `pessoas_assistidas.latitude` | 0 de 1 pessoa | Necessário para regra atual | Manter | O cálculo de distância exige o par latitude/longitude. |
| `tarefas_cuidado.fuso_horario` | `CareTask`, recorrência, data local, geração de ocorrências, edição, DTOs e atualização pelo dispositivo | 41 valores, todos `America/Sao_Paulo` | Necessário para regra atual | Manter | Define como `data_prevista` + `horario_previsto` viram `instante_previsto_utc` e sustenta RF13/RF14. |
| `registros_diario_cuidado.fuso_horario` | `CareActivityRecord`, cuidado manual, conversão de horário local e resposta do diário | 8 valores, todos `America/Sao_Paulo` | Necessário para regra atual | Manter | Preserva o contexto civil do registro e sustenta ordenação/exibição do diário e relatório (RF15/RF16/RF19). |
| `ocorrencias_cuidado.fuso_horario` | `TaskOccurrence`, geração/edição, expiração, validação do dia, lembretes e respostas | 70 valores, todos `America/Sao_Paulo` | Necessário para regra atual | Manter | É necessário para interpretar o instante UTC no dia local correto, calcular atrasos e operar lembretes (RF13–RF16/RF19). |

Não há campos classificados como dúvida. Somente `cuidadores.formacao` atende aos critérios de remoção.

## Evidências da formação normalizada

- `cuidadores_formacoes` contém 7 registros para os 3 cuidadores.
- Os 3 valores não nulos de `cuidadores.formacao` possuem linha equivalente na coleção normalizada; não há formação singular sem equivalente.
- Cadastro e edição já enviam `formacoes` no frontend e persistem `CaregiverProfile.formacoes` por `@ElementCollection`.
- A busca e seus filtros consultam exclusivamente `profile.getFormacoes()`.
- Os DTOs de busca e detalhe expõem `formacoes`; o singular permanece apenas como compatibilidade legada no perfil próprio e nos requests antigos.
- A migração de origem V004 já copiava `formacao` para `caregiver_formations`; a nova migração repetirá a cópia de forma idempotente antes do `DROP` para proteger bancos evoluídos divergentes.

## Impacto nos requisitos funcionais

- RF05, RF06 e RF07 continuam usando `cuidadores_formacoes`; o conceito de formação não será removido.
- RF06 e RF17 dependem das coordenadas mantidas para cálculo de distância.
- RF13, RF14, RF15, RF16 e RF19 dependem dos fusos mantidos para datas civis, instantes UTC, lembretes, diário e relatório.
- RF18 continua usando somente as coordenadas e o fuso próprios de `registros_atendimento`, que não fazem parte dos candidatos.

## Plano aprovado pela análise

1. Criar a V042 para copiar defensivamente qualquer `cuidadores.formacao` ausente em `cuidadores_formacoes`, verificar a convergência e remover somente a coluna singular.
2. Remover a propriedade singular da entidade e a compatibilidade singular dos DTOs/serviços.
3. Manter `formacoes` e `formacaoOutro` como contrato oficial e retirar o fallback singular do frontend.
4. Atualizar o inventário e a modelagem para 416 colunas.
5. Validar a V042 em banco evoluído e cadeia limpa, Hibernate, testes backend, TypeScript e lint.

## Resultado da execução

- A V042 foi ensaiada no banco evoluído dentro de transação com rollback; a impressão digital e as 7 linhas de `cuidadores_formacoes` foram preservadas.
- A cadeia limpa V001–V042 foi aplicada em schema temporário e convergiu para 35 tabelas de domínio mais `flyway_schema_history`, com 416 colunas.
- A V042 foi aplicada ao schema principal; `cuidadores.formacao` não existe mais e os sete campos mantidos continuam presentes.
- A aplicação iniciou com `ddl-auto=validate`, sem tentativa do Hibernate de acessar a coluna removida.
- A suíte backend concluiu 93 testes sem falhas, erros ou testes ignorados; TypeScript (`tsc --noEmit`) e `expo lint` também passaram.
- O PostgreSQL utilizado está na versão 17.7. O Flyway embarcado avisa que sua faixa oficialmente testada vai até PostgreSQL 16, mas a migração e as validações foram concluídas sem falha.
