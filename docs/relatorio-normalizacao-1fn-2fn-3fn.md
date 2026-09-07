# Relatório de Normalização — Cuidar+

## Resumo

- Total de tabelas avaliadas: 40.
- Total de problemas de 1FN: 8 achados-raiz — sete relações associativas sem chave primária declarada e um campo assistencial potencialmente multivalorado.
- Total de problemas de 2FN: 0 dependências parciais próprias; a conformidade formal de sete relações estava condicionada à declaração de sua chave completa.
- Total de problemas de 3FN: 0 dependências transitivas injustificadas; a conformidade formal das mesmas sete relações estava condicionada à correção anterior.
- Problemas corrigíveis com baixo risco: 7.
- Problemas que exigem decisão humana: 1.
- Duplicidades justificadas por histórico/snapshot/auditoria: 15 estruturas ou categorias documentadas.

O diagnóstico foi realizado sobre o schema `public` do PostgreSQL local, no estado Flyway `049`, por consultas somente leitura a `pg_catalog` e `information_schema`, e confrontado com as entidades JPA, migrations e a modelagem vigente. O schema possuía 40 tabelas, 467 colunas, 33 chaves primárias, 87 chaves estrangeiras, 15 constraints de unicidade e 19 constraints de verificação.

Para esta revisão, “problema de 2FN” contabiliza apenas dependência parcial de chave composta, e “problema de 3FN” apenas dependência transitiva não justificada. A ausência de chave declarada é contabilizada uma vez como causa-raiz de 1FN, embora também impedisse afirmar formalmente as formas seguintes antes da correção.

## Análise por tabela

| Tabela | 1FN | 2FN | 3FN | Problema encontrado | Impacto | Recomendação |
|---|---|---|---|---|---|---|
| `auditoria_acao_critica` | Conforme | Conforme | Conforme | Nenhum; JSON guarda somente estados resumidos de auditoria, não relação operacional principal | Baixo | Manter por RNF25 |
| `contratacao` | Conforme | Conforme | Conforme | Referências das partes também existem na solicitação de origem | Histórico | Manter vínculo contratual explícito |
| `cuidador` | Conforme | Conforme | Conforme | Nenhum | — | Manter |
| `cuidador_disponibilidade_dia` | Pendente | Condicionada | Condicionada | Sem PK ou UNIQUE no par cuidador/dia | Duplicidade possível fora do JPA | Adicionar PK composta |
| `cuidador_disponibilidade_periodo` | Pendente | Condicionada | Condicionada | Sem PK ou UNIQUE no par cuidador/período | Duplicidade possível fora do JPA | Adicionar PK composta |
| `cuidador_formacao` | Pendente | Condicionada | Condicionada | Possui UNIQUE, mas não PK declarada | Identificação relacional inconsistente | Promover o par à PK composta |
| `cuidador_historico_situacao` | Conforme | Conforme | Conforme | Repetição intencional de estados e motivo | Histórico administrativo | Manter |
| `cuidador_modalidade` | Pendente | Condicionada | Condicionada | Sem PK ou UNIQUE no par cuidador/modalidade | Duplicidade possível fora do JPA | Adicionar PK composta |
| `cuidador_servico` | Pendente | Condicionada | Condicionada | Sem PK ou UNIQUE no par cuidador/serviço | Duplicidade possível fora do JPA | Adicionar PK composta |
| `notificacao` | Conforme | Conforme | Conforme | Conteúdo e alvo polimórfico representam o evento entregue | Snapshot de comunicação | Manter |
| `notificacao_preferencia` | Conforme | Conforme | Conforme | Nenhum | — | Manter |
| `ocorrencia_cuidado` | Conforme | Conforme | Conforme | Estado e evidências pertencem à execução prevista | Histórico assistencial | Manter |
| `ocorrencia_cuidado_foto` | Conforme | Conforme | Conforme | Dois pais possíveis controlados por CHECK exclusivo | Baixo | Manter |
| `ocorrencia_cuidado_lembrete` | Conforme | Conforme | Conforme | Estado de envio é histórico operacional | Histórico | Manter |
| `pessoa_assistida` | Revisão humana | Condicionada | Condicionada | `medicamentos` aceita uma lista/resumo livre em uma coluna | Possível violação de atomicidade e dado assistencial sensível | Definir granularidade antes de normalizar |
| `pessoa_assistida_alergia` | Pendente | Condicionada | Condicionada | Sem PK ou UNIQUE no par pessoa/alergia | Duplicidade possível fora do JPA | Adicionar PK composta |
| `pessoa_assistida_contato_emergencia` | Conforme | Conforme | Conforme | Nenhum | — | Manter |
| `pessoa_assistida_restricao_alimentar` | Pendente | Condicionada | Condicionada | Sem PK ou UNIQUE no par pessoa/restrição | Duplicidade possível fora do JPA | Adicionar PK composta |
| `registro_atendimento` | Conforme | Conforme | Conforme | Coordenadas, precisão e horário registram o instante do atendimento | Evidência histórica | Manter |
| `registro_diario_cuidado` | Conforme | Conforme | Conforme | Narrativa e instruções pertencem ao registro ocorrido | Histórico assistencial | Manter |
| `relatorio_atendimento` | Conforme | Conforme | Conforme | Texto gerado, editado e final preserva evolução do documento | Snapshot documental | Manter |
| `responsavel` | Conforme | Conforme | Conforme | Nenhum | — | Manter |
| `responsavel_historico_situacao` | Conforme | Conforme | Conforme | Repetição intencional de estados e motivo | Histórico administrativo | Manter |
| `rotina_cuidado` | Conforme | Conforme | Conforme | Nenhum | — | Manter |
| `rotina_cuidado_item` | Conforme | Conforme | Conforme | Instruções são narrativa atômica do item | Baixo | Manter |
| `rotina_cuidado_item_dia_semana` | Conforme | Conforme | Conforme | PK composta já representa a relação completa | — | Manter |
| `solicitacao_servico` | Conforme | Conforme | Conforme | Dados acordados e referências de origem são preservados | Snapshot da negociação | Manter |
| `solicitacao_servico_agenda_dia` | Conforme | Conforme | Conforme | Horários dependem do par solicitação/dia | — | Manter PK composta |
| `solicitacao_servico_atividade` | Conforme | Conforme | Conforme | Atividade pertence integralmente à solicitação | — | Manter |
| `solicitacao_servico_contratacao_historico_status` | Conforme | Conforme | Conforme | Estado anterior/novo e entidade são o próprio evento histórico | Histórico | Manter |
| `solicitacao_servico_data` | Conforme | Conforme | Conforme | PK composta já representa a relação completa | — | Manter |
| `solicitacao_servico_item_cuidado_copia` | Conforme | Conforme | Conforme | Cópia preserva o item apresentado/contratado | Snapshot contratual | Manter |
| `solicitacao_servico_item_cuidado_copia_dia_semana` | Conforme | Conforme | Conforme | Dias pertencem ao item copiado, com chave adequada | Snapshot contratual | Manter |
| `tarefa_cuidado` | Conforme | Conforme | Conforme | Alguns campos copiam a rotina que originou a tarefa | Snapshot operacional | Manter |
| `tarefa_cuidado_auditoria` | Conforme | Conforme | Conforme | Ação, autor e instante constituem o evento | Auditoria específica | Manter |
| `tarefa_cuidado_dia_semana` | Conforme | Conforme | Conforme | PK composta já representa a relação completa | — | Manter |
| `usuario` | Conforme | Conforme | Conforme | Nenhum; dados comuns não são repetidos nos perfis | — | Manter RNF08 |
| `usuario_confirmacao_exclusao` | Conforme | Conforme | Conforme | Confirmação é evidência temporária do fluxo | Segurança/RF22 | Manter |
| `usuario_exclusao_auditoria` | Conforme | Conforme | Conforme | Referência anonimizada e resultado preservam responsabilização | Auditoria/RF22 | Manter |
| `usuario_token_redefinicao_senha` | Conforme | Conforme | Conforme | Hash, expiração e uso pertencem ao token técnico | Segurança | Manter |

## Problemas de 1FN

| Tabela | Coluna/estrutura | Problema | Correção proposta |
|---|---|---|---|
| `cuidador_disponibilidade_dia` | `(perfil_cuidador_id, dia_semana)` | Relação sem chave declarada | PK composta, após validar duplicidades |
| `cuidador_disponibilidade_periodo` | `(perfil_cuidador_id, periodo)` | Relação sem chave declarada | PK composta, após validar duplicidades |
| `cuidador_formacao` | `(perfil_cuidador_id, formacao)` | UNIQUE identifica a linha, mas falta PK formal | Substituir a unicidade redundante por PK composta |
| `cuidador_modalidade` | `(perfil_cuidador_id, modalidade)` | Relação sem chave declarada | PK composta, após validar duplicidades |
| `cuidador_servico` | `(perfil_cuidador_id, servico)` | Relação sem chave declarada | PK composta, após validar duplicidades |
| `pessoa_assistida_alergia` | `(pessoa_assistida_id, alergia)` | Relação sem chave declarada | PK composta, após validar duplicidades |
| `pessoa_assistida_restricao_alimentar` | `(pessoa_assistida_id, restricao)` | Relação sem chave declarada | PK composta, após validar duplicidades |
| `pessoa_assistida` | `medicamentos` | A interface orienta “Liste se houver”; múltiplos medicamentos podem ocupar um único valor textual | Decidir se o campo é resumo narrativo indivisível ou coleção estruturada; não migrar sem definir nome, dose, frequência e retenção |

Todos os campos são escalares no PostgreSQL. Formações, serviços, modalidades, disponibilidade, alergias, restrições, recorrências, fotos, preferências e históricos já usam relações próprias. `auditoria_acao_critica.valor_anterior_resumido` e `valor_novo_resumido` são JSON de auditoria com lista segura de chaves, não substituem uma relação de domínio e foram mantidos.

## Problemas de 2FN

| Tabela | Coluna/estrutura | Problema | Correção proposta |
|---|---|---|---|
| — | — | Nenhuma dependência parcial foi encontrada. As tabelas associativas contêm somente as FKs/valores que compõem a relação; horários de `solicitacao_servico_agenda_dia` dependem da solicitação e do dia completos. | Declarar as sete chaves faltantes para completar a conformidade formal |

## Problemas de 3FN

| Tabela | Coluna/estrutura | Problema | Correção proposta |
|---|---|---|---|
| — | — | Nenhuma dependência transitiva injustificada foi encontrada. | Manter snapshots e históricos documentados |

## Duplicidades justificadas

| Tabela | Campo/estrutura | Justificativa |
|---|---|---|
| `contratacao` | IDs das partes e pessoa assistida | Mantém vínculo direto e durável do contrato, mesmo com solicitação de origem |
| `solicitacao_servico` | Endereço, agenda, valores, atividades e observações negociadas | Preserva o contexto submetido e acordado |
| `solicitacao_servico_item_cuidado_copia` e dias | Cópia de itens da rotina | Impede que alterações posteriores da rotina reescrevam o acordo |
| `tarefa_cuidado` | Dados derivados do item de rotina | Materializa a execução agendada sem alterar o modelo-base |
| `cuidador_historico_situacao` | Situações e motivo | Linha do tempo administrativa RF21 |
| `responsavel_historico_situacao` | Situações e motivo | Linha do tempo administrativa RF20/RF21 |
| `solicitacao_servico_contratacao_historico_status` | Estados anterior e novo | Linha do tempo de solicitação/contratação |
| `tarefa_cuidado_auditoria` | Ação, autor e instante | Linha do tempo específica da tarefa |
| `auditoria_acao_critica` | Estado anterior/novo resumido | Auditoria transversal RNF25, com minimização |
| `registro_atendimento` | Localização e instante capturados | Evidência do atendimento realizado |
| `registro_diario_cuidado` | Narrativa da execução | Registro histórico assistencial RF17 |
| `relatorio_atendimento` | Texto gerado, editado e final | Preserva elaboração e finalização RF19 |
| `notificacao` | Título, mensagem e alvo lógico | Snapshot da comunicação enviada ao usuário |
| `ocorrencia_cuidado` e fotos | Estado e evidências da ocorrência | Preserva execução, não realização ou cancelamento |
| `usuario_exclusao_auditoria` | Referência anonimizada e resultado | Evidência mínima e não reversível do RF22 |

## Alterações propostas

| Alteração | Tipo | Risco | Requer autorização humana? |
|---|---|---|---|
| Adicionar PK composta às sete tabelas associativas sem PK | Corrigir automaticamente | Baixo: colunas já são NOT NULL e a auditoria encontrou zero grupos duplicados | Não |
| Remover a UNIQUE redundante de `cuidador_formacao` somente após criar sua PK equivalente | Correção estrutural sem dados | Baixo: a PK preserva a mesma unicidade | Não |
| Estruturar `pessoa_assistida.medicamentos` em tabela filha | Corrigir com migration de preservação e mudança de contrato | Alto: dado assistencial sensível e sem granularidade definida | Sim |
| Remover ou decompor snapshots, históricos e auditorias | Manter com justificativa | Alto e incompatível com RFs/RNFs | Sim; não recomendado |

## Validação pré-alteração

As sete tabelas candidatas possuem ambas as colunas `NOT NULL`. Consultas `GROUP BY ... HAVING COUNT(*) > 1`, executadas em transação somente leitura, retornaram zero grupos duplicados em todas elas. Portanto, a criação das chaves compostas não exige deduplicação nem descarte de dados.

## Resultado pós-alteração

A migration `V050__normalizacao_chaves_tabelas_associativas.sql` foi aplicada ao banco local evoluído de V049 para V050. A introspecção posterior confirmou:

- zero grupos duplicados nas sete relações;
- sete novas PKs compostas presentes;
- remoção da UNIQUE redundante de `cuidador_formacao`;
- 40 tabelas de domínio, todas com PK;
- 467 colunas, 87 FKs, 14 constraints UNIQUE, 19 CHECKs e 106 índices de domínio.

Um schema temporário isolado recebeu, do zero, todas as 48 migrations disponíveis no repositório, de V001 a V050. O teste confirmou as sete PKs e removeu o schema temporário ao final. O banco evoluído também foi iniciado com Flyway atualizado e `Hibernate validate`; a aplicação alcançou o estado `ACCEPTING_TRAFFIC` em porta dinâmica.

A suíte permanente da API executou 136 testes, sem falhas, erros ou testes ignorados. O verificador isolado de schema limpo acrescentou um teste bem-sucedido durante a validação. No frontend, `tsc --noEmit`, lint e a inicialização do Metro foram aprovados. Como a alteração é exclusivamente de constraints e não muda payloads, nenhuma modificação de frontend, DTO, mapper, serviço ou entidade foi necessária.

Após V050, os sete achados estruturais de baixo risco estão corrigidos e as respectivas tabelas atendem formalmente à 1FN, 2FN e 3FN. Permanece para decisão humana apenas a semântica de `pessoa_assistida.medicamentos`; nenhuma informação assistencial foi alterada ou removida. Snapshots, históricos, RF01–RF23, RNF08 e RNF25 foram preservados.
