# Modelagem do banco de dados — Cuidar+

## Visão geral

O banco PostgreSQL organiza autenticação e perfis, solicitações e contratações, rotinas e execução dos cuidados, atendimento geolocalizado, relatórios e notificações. O modelo físico utiliza nomes em português do Brasil, `snake_case` e sem acentos. O modelo lógico exibido no texto acadêmico usa acentuação normal.

Todas as chaves primárias são UUID. Na relação abaixo, **Sim** significa `NOT NULL`; **Não** significa que a coluna aceita `NULL`. `timestamptz` representa `timestamp with time zone`.

O mapa relacional completo, incluindo tabelas mantidas, está em [dicionario-nomenclatura-relacional.md](dicionario-nomenclatura-relacional.md). O histórico da primeira tradução está em [dicionario-renomeacao-banco.md](dicionario-renomeacao-banco.md).

## Padrão de nomenclatura

Tabelas principais têm nomes diretos no plural. Tabelas filhas, coleções, históricos, anexos e preferências começam pelo nome da entidade principal, formando grupos previsíveis em ordem alfabética. Tabelas associativas identificam as entidades relacionadas. O schema físico não usa acentos, cedilha, espaços ou hífens.

Exemplos de agrupamento:

- `rotinas_cuidado`, `rotinas_cuidado_itens`, `rotinas_cuidado_itens_dias_semana`;
- `ocorrencias_cuidado`, `ocorrencias_cuidado_fotos`, `ocorrencias_cuidado_lembretes`;
- `notificacoes`, `notificacoes_preferencias`;
- `solicitacoes_servico`, `solicitacoes_servico_agenda_dias`, `solicitacoes_servico_atividades`, `solicitacoes_servico_datas`;
- `cuidadores`, `cuidadores_disponibilidade_dias`, `cuidadores_disponibilidade_periodos`, `cuidadores_formacoes`, `cuidadores_modalidades`, `cuidadores_servicos`.

## Tabelas finais agrupadas por domínio

- Identidade: `usuarios`, `usuarios_tokens_redefinicao_senha`, `responsaveis`, `cuidadores`, `cuidadores_disponibilidade_dias`, `cuidadores_disponibilidade_periodos`, `cuidadores_formacoes`, `cuidadores_modalidades`, `cuidadores_servicos`.
- Pessoas assistidas: `pessoas_assistidas`, `pessoas_assistidas_alergias`, `pessoas_assistidas_contatos_emergencia`, `pessoas_assistidas_restricoes_alimentares`.
- Solicitações e publicações: `solicitacoes_servico`, `solicitacoes_servico_agenda_dias`, `solicitacoes_servico_atividades`, `solicitacoes_servico_datas`, `solicitacoes_servico_itens_cuidado_copias`, `solicitacoes_servico_itens_cuidado_copias_dias_semana`, `solicitacoes_servico_contratacoes_historico_status`.
- Contratações: `contratacoes`, `contratacoes_itens_cuidado`, `contratacoes_itens_cuidado_historico`.
- Rotinas e grupos: `rotinas_cuidado`, `rotinas_cuidado_itens`, `rotinas_cuidado_itens_dias_semana`, `grupos_cuidado`, `grupos_cuidado_itens`, `solicitacoes_servico_grupos_cuidado_itens_copias`. As tabelas de grupos são compatibilidade histórica e podem não existir em instalações iniciadas sem as antigas V012/V013.
- Planejamento e execução do cuidado: `tarefas_cuidado`, `tarefas_cuidado_dias_semana`, `tarefas_cuidado_auditoria`, `ocorrencias_cuidado`, `ocorrencias_cuidado_fotos`, `ocorrencias_cuidado_lembretes`, `registros_diario_cuidado`.
- Atendimento e relatório: `registros_atendimento`, `relatorios_atendimento`.
- Notificações: `notificacoes`, `notificacoes_preferencias`.
- Infraestrutura: `flyway_schema_history`, mantida com o nome oficial do Flyway.

## Usuários e perfis

### `usuarios` — Usuários

Dados comuns de autenticação e identificação de responsáveis, cuidadores e administradores.

| Coluna | Tipo | Obrigatória | Regra |
|---|---|---:|---|
| `id` | uuid | Sim | PK. |
| `nome_completo` | varchar(140) | Sim | Nome completo. |
| `cpf` | varchar(11) | Sim | Único. |
| `email` | varchar(180) | Sim | Único. |
| `senha_hash` | varchar(255) | Sim | Hash da senha. |
| `data_nascimento` | date | Sim | Data de nascimento. |
| `telefone` | varchar(20) | Não | Telefone de contato. |
| `url_foto_perfil` | varchar(500) | Não | Foto do perfil. |
| `tipo_usuario` | varchar(20) | Sim | Enum de papel do usuário. |
| `status` | varchar(30) | Sim | Estado cadastral. |
| `criado_em` | timestamptz | Sim | Criação. |
| `atualizado_em` | timestamptz | Sim | Última alteração. |

### `usuarios_tokens_redefinicao_senha` — Tokens de redefinição de senha

| Coluna | Tipo | Obrigatória | Regra |
|---|---|---:|---|
| `id` | uuid | Sim | PK. |
| `usuario_id` | uuid | Sim | FK → `usuarios.id`. |
| `hash_token` | varchar(64) | Sim | Único. |
| `expira_em` | timestamptz | Sim | Validade. |
| `usado_em` | timestamptz | Não | Uso efetivo. |
| `criado_em` | timestamptz | Sim | Criação. |

### `responsaveis` — Responsáveis

| Coluna | Tipo | Obrigatória | Regra |
|---|---|---:|---|
| `id` | uuid | Sim | PK. |
| `usuario_id` | uuid | Sim | FK única → `usuarios.id`. |
| `parentesco` | varchar(40) | Sim | Enum de parentesco. |
| `parentesco_outro` | varchar(120) | Não | Complemento livre. |
| `preferencia_contato` | varchar(30) | Sim | Canal preferido. |
| `criado_em` | timestamptz | Sim | Criação. |
| `atualizado_em` | timestamptz | Sim | Alteração. |

### `cuidadores` — Cuidadores

| Colunas | Tipo/obrigatoriedade |
|---|---|
| `id`; `usuario_id` | uuid Sim; uuid Sim, FK única → `usuarios.id` |
| `formacao`; `formacao_outro`; `tempo_experiencia` | varchar(40) Não; varchar(180) Não; varchar(30) Não |
| `experiencia`; `biografia` | varchar(500) Não; varchar(500) Não |
| `cep`; `rua`; `numero`; `complemento`; `bairro`; `cidade`; `estado`; `ponto_referencia` | varchar(9/180/30/120/120/120/2/180), Não |
| `latitude`; `longitude` | numeric(10,7), Não |
| `horario_inicio`; `horario_fim` | time, Não |
| `observacao`; `modalidade_outro`; `servico_outro` | varchar(500/180/180), Não |
| `criado_em`; `atualizado_em` | timestamptz Sim; timestamptz Sim |

### `pessoas_assistidas` — Pessoas assistidas

| Colunas | Tipo/obrigatoriedade |
|---|---|
| `id`; `usuario_responsavel_id` | uuid Sim, PK; uuid Sim, FK → `usuarios.id` |
| `nome`; `cpf`; `data_nascimento` | varchar(140) Sim; varchar(11) Não; date Sim |
| `grau_dependencia`; `mobilidade`; `mobilidade_outro` | varchar(30) Sim; varchar(30) Sim; varchar(120) Não |
| `alergias_outro`; `alergias_detalhes` | varchar(180) Não; varchar(500) Não |
| `restricoes_alimentares_outro`; `restricoes_alimentares_detalhes` | varchar(180) Não; varchar(500) Não |
| `medicamentos`; `observacoes` | varchar(500), Não |
| `cep`; `rua`; `numero`; `complemento`; `bairro`; `cidade`; `estado`; `ponto_referencia` | varchar(9/180/30/120/120/120/2/180), Não |
| `latitude`; `longitude` | numeric(10,7), Não |
| `criado_em`; `atualizado_em` | timestamptz Sim; timestamptz Sim |

### Tabelas auxiliares de perfil

| Tabela | Colunas | Relacionamento/finalidade |
|---|---|---|
| `pessoas_assistidas_contatos_emergencia` | `id` uuid Sim; `pessoa_assistida_id` uuid Sim; `nome` varchar(140) Sim; `telefone` varchar(20) Sim; `vinculo` varchar(120) Sim; `contato_responsavel` boolean Sim; `criado_em`/`atualizado_em` timestamptz Sim | FK única para `pessoas_assistidas.id`. |
| `pessoas_assistidas_alergias` | `pessoa_assistida_id` uuid Sim; `alergia` varchar(40) Sim | FK para pessoa assistida; coleção de enums. |
| `pessoas_assistidas_restricoes_alimentares` | `pessoa_assistida_id` uuid Sim; `restricao` varchar(40) Sim | FK para pessoa assistida; coleção de enums. |
| `cuidadores_modalidades` | `perfil_cuidador_id` uuid Sim; `modalidade` varchar(40) Sim | FK para cuidador. |
| `cuidadores_servicos` | `perfil_cuidador_id` uuid Sim; `servico` varchar(50) Sim | FK para cuidador. |
| `cuidadores_disponibilidade_dias` | `perfil_cuidador_id` uuid Sim; `dia_semana` varchar(20) Sim | FK para cuidador. |
| `cuidadores_disponibilidade_periodos` | `perfil_cuidador_id` uuid Sim; `periodo` varchar(30) Sim | FK para cuidador. |
| `cuidadores_formacoes` | `perfil_cuidador_id` uuid Sim; `formacao` varchar(40) Sim | Par único por cuidador e formação. |

## Solicitações e contratações

### `solicitacoes_servico` — Solicitações de serviço

Representa tanto o convite direto do responsável quanto uma oportunidade aberta e a candidatura derivada dela.

| Colunas | Tipo/obrigatoriedade |
|---|---|
| `id` | uuid Sim, PK |
| `usuario_responsavel_id`; `usuario_cuidador_id`; `pessoa_assistida_id` | uuid Sim; uuid Não; uuid Sim — FKs |
| `usuario_solicitante_id`; `oportunidade_origem_id` | uuid Sim, FK → `usuarios`; uuid Não, autorreferência |
| `tipo_contratacao`; `status`; `iniciado_por` | varchar(40) Sim; varchar(30) Sim; varchar(20) Sim |
| `data_inicio`; `data_fim` | date Não; date Não |
| `descricao_necessidades` | varchar(2000) Sim |
| `outra_atividade`; `observacoes_adicionais`; `observacoes_negociacao` | varchar(500/2000/1000), Não |
| `motivo_rejeicao`; `motivo_cancelamento` | varchar(1000), Não |
| `rotina_cuidado_id`; `nome_rotina_snapshot` | uuid Não, FK → `rotinas_cuidado`; varchar(140) Não |
| `criado_em`; `atualizado_em`; `expira_em`; `cancelado_em` | timestamptz Sim; Sim; Sim; Não |

### Tabelas auxiliares das solicitações

| Tabela | Colunas | Chave/relacionamento |
|---|---|---|
| `solicitacoes_servico_datas` | `solicitacao_servico_id` uuid Sim; `data_servico` date Sim | PK composta; FK com exclusão em cascata. |
| `solicitacoes_servico_agenda_dias` | `solicitacao_servico_id` uuid Sim; `dia_semana` varchar(20) Sim; `horario_inicio`/`horario_fim` time Sim | PK composta; grade semanal. |
| `solicitacoes_servico_atividades` | `solicitacao_servico_id` uuid Sim; `atividade` varchar(50) Sim | PK composta. |

### `contratacoes` — Contratações

| Colunas | Tipo/obrigatoriedade |
|---|---|
| `id`; `solicitacao_servico_id` | uuid Sim, PK; uuid Sim, FK única |
| `usuario_responsavel_id`; `usuario_cuidador_id`; `pessoa_assistida_id` | uuid Sim, FKs |
| `status`; `data_inicio`; `data_fim` | varchar(30) Sim; date Sim; date Não |
| `motivo_cancelamento`; `motivo_encerramento` | varchar(1000), Não |
| `tipo_encerramento`; `motivo_solicitacao_encerramento`; `observacoes_encerramento` | varchar(50/1000/1000), Não |
| `usuario_solicitante_encerramento_id`; `usuario_solicitante_cancelamento_id` | uuid Não, FKs → `usuarios.id` |
| `encerramento_solicitado_em`; `cancelamento_solicitado_em`; `cancelado_em` | timestamptz Não |
| `data_fim_efetiva` | date Não |
| `criado_em`; `atualizado_em` | timestamptz Sim |

### `solicitacoes_servico_contratacoes_historico_status` — Histórico de status

| Coluna | Tipo | Obrigatória | Regra |
|---|---|---:|---|
| `id` | uuid | Sim | PK. |
| `tipo_entidade` | varchar(40) | Sim | Solicitação ou contratação. |
| `entidade_id` | uuid | Sim | Identificador polimórfico. |
| `status_anterior` | varchar(30) | Não | Estado anterior. |
| `novo_status` | varchar(30) | Sim | Novo estado. |
| `usuario_alteracao_id` | uuid | Não | FK → `usuarios.id`; pode ser ação automática. |
| `motivo` | varchar(1000) | Não | Justificativa. |
| `criado_em` | timestamptz | Sim | Instante da transição. |

## Rotinas e cuidados

### `rotinas_cuidado` e `rotinas_cuidado_itens`

| Tabela | Colunas |
|---|---|
| `rotinas_cuidado` | `id` uuid Sim; `usuario_responsavel_id` uuid Sim FK; `pessoa_assistida_id` uuid Não FK; `nome` varchar(140) Sim; `descricao` varchar(1000) Não; `ativo` boolean Sim; `criado_em`/`atualizado_em` timestamptz Sim. |
| `rotinas_cuidado_itens` | `id` uuid Sim; `rotina_cuidado_id` uuid Sim FK; `titulo` varchar(140) Sim; `descricao` varchar(1000) Não; `ordem_exibicao` integer Sim; `ativo` boolean Sim; `categoria` varchar(40) Não; `categoria_personalizada` varchar(120) Não; `prioridade` varchar(20) Não; `tipo_recorrencia` varchar(40) Não; `horario_previsto` time Não; `intervalo_dias` integer Não; campos de lembrete boolean/integer; `importante`, `notificar_responsavel_se_importante` e `exige_foto_conclusao` boolean Sim; campos de medicamento varchar; `anotacoes` varchar(2000) Não; `criado_em`/`atualizado_em` timestamptz Sim. |

Os campos de medicamento são `nome_medicamento` varchar(180), `dosagem_medicamento` varchar(80), `unidade_medicamento` varchar(30), `unidade_personalizada_medicamento` varchar(80), `via_administracao_medicamento` varchar(30), `via_personalizada_medicamento` varchar(120) e `instrucoes_medicamento` varchar(1000), todos opcionais. Os campos de lembrete são `lembrete_habilitado` boolean opcional, `minutos_antecedencia_lembrete` integer opcional, `lembrar_no_horario_previsto` boolean obrigatório, `lembrete_atraso_habilitado` boolean obrigatório, `minutos_para_atraso` integer opcional, `repetir_enquanto_pendente` boolean obrigatório e `intervalo_repeticao_minutos` integer opcional.

### Snapshots e recorrência de rotina

| Tabela | Colunas/relacionamentos |
|---|---|
| `solicitacoes_servico_itens_cuidado_copias` | Mesmos campos estruturados do item de rotina, exceto `ativo` e `atualizado_em`; inclui `id`, `solicitacao_servico_id`, `rotina_cuidado_original_id`, `item_rotina_cuidado_original_id` opcional, `ordem_exibicao` e `criado_em`. Preserva o conteúdo contratado. |
| `rotinas_cuidado_itens_dias_semana` | `item_rotina_cuidado_id` uuid Sim FK; `dia_semana` varchar(20) Sim. |
| `solicitacoes_servico_itens_cuidado_copias_dias_semana` | `item_snapshot_id` uuid Sim FK; `dia_semana` varchar(20) Sim. |

### `tarefas_cuidado` — Tarefas de cuidado

| Grupo | Colunas |
|---|---|
| Identificação | `id` uuid Sim PK; `titulo` varchar(140) Sim; `descricao` varchar(2000) Não; `categoria` varchar(40) Sim; `categoria_personalizada` varchar(120) Não; `prioridade` varchar(20) Sim; `status` varchar(20) Sim. |
| Recorrência | `tipo_recorrencia` varchar(40) Sim; `data_inicio` date Sim; `data_fim` date Não; `horario_previsto` time Sim; `intervalo_dias` integer Não; `fuso_horario` varchar(80) Sim. |
| Participantes | `pessoa_assistida_id`, `contratacao_id`, `responsavel_criador_id`, `cuidador_executor_id` uuid Sim e FKs. |
| Linhagem | `serie_anterior_id`, `item_snapshot_origem_id`, `tarefa_duplicada_de_id` uuid Não e FKs. |
| Lembretes | `lembrete_habilitado` boolean Sim; `minutos_antecedencia_lembrete` integer Não; demais campos de lembrete descritos acima. |
| Evidência | `importante`, `notificar_responsavel_se_importante`, `exige_foto_conclusao` boolean Sim. |
| Medicamento | Os sete campos opcionais de medicamento descritos acima. |
| Auditoria | `anotacoes` varchar(2000) Não; `usuario_criacao_id`/`usuario_atualizacao_id` uuid Sim; `criado_em`/`atualizado_em` timestamptz Sim; `versao` bigint Sim. |

### `ocorrencias_cuidado` — Ocorrências de cuidado

| Colunas | Tipo/obrigatoriedade |
|---|---|
| `id`; `tarefa_id`; `contratacao_id`; `pessoa_assistida_id`; `usuario_cuidador_id` | uuid Sim; PK/FKs |
| `data_prevista`; `horario_previsto`; `instante_previsto_utc`; `fuso_horario` | date Sim; time Sim; timestamptz Sim; varchar(80) Sim |
| `status` | varchar(25) Sim |
| `concluido_em`; `cancelado_em`; `status_atualizado_em` | timestamptz Não |
| `usuario_executor_id` | uuid Não, FK → `usuarios.id` |
| `motivo_nao_realizacao`; `anotacao_execucao` | varchar(1000), Não |
| `excecao`; `marcada_nao_realizada_automaticamente` | boolean Sim |
| `criado_em`; `atualizado_em`; `versao` | timestamptz Sim; timestamptz Sim; bigint Sim |

A combinação `tarefa_id`, `data_prevista` e `horario_previsto` é única.

### Diário, auditoria, lembretes e fotos

| Tabela | Colunas principais |
|---|---|
| `registros_diario_cuidado` | `id` uuid Sim; `ocorrencia_id` uuid Não e único; `contratacao_id`, `pessoa_assistida_id`, `usuario_responsavel_id`, `usuario_cuidador_id`, `usuario_criacao_id` uuid Sim; `tipo_atividade`, `tipo_origem`, `tipo_cuidado` varchar Sim; `data_registro` date Sim; `fuso_horario` varchar(80) Sim; `titulo` varchar(180) Sim; `descricao` varchar(2000) Não; `anotacoes` varchar(1000) Não; `importante` boolean Sim; `ocorrido_em`/`criado_em` timestamptz Sim. |
| `tarefas_cuidado_auditoria` | `id` uuid Sim; `tarefa_id` uuid Sim; `ocorrencia_id` uuid Não; `usuario_ator_id` uuid Não; `acao` varchar(40) Sim; `detalhes` varchar(500) Não; `criado_em` timestamptz Sim. |
| `ocorrencias_cuidado_lembretes` | `id` uuid Sim; `ocorrencia_id`/`usuario_destinatario_id` uuid Sim; `tipo_lembrete` varchar(40) Sim; `previsto_em` timestamptz Sim; `enviado_em`/`cancelado_em` timestamptz Não; `status` varchar(20) Sim; `chave_deduplicacao` varchar(220) Sim e única; `criado_em`/`atualizado_em` timestamptz Sim. |
| `ocorrencias_cuidado_fotos` | `id` uuid Sim; `ocorrencia_id` ou `registro_atividade_id` uuid (exatamente um preenchido); `usuario_envio_id` uuid Sim; `nome_arquivo` varchar(80) Sim e único; `nome_arquivo_original` varchar(255) Não; `tipo_conteudo` varchar(30) Sim; `tamanho_arquivo` bigint Sim; `criado_em` timestamptz Sim. |
| `tarefas_cuidado_dias_semana` | `tarefa_id` uuid Sim; `dia_semana` varchar(20) Sim; PK composta. |

## Atendimento e relatório

### `registros_atendimento` — Registros de atendimento

| Coluna | Tipo | Obrigatória |
|---|---|---:|
| `id` | uuid | Sim |
| `contratacao_id`; `cuidador_id`; `responsavel_id`; `pessoa_assistida_id` | uuid | Sim |
| `data_atendimento` | date | Sim |
| `tipo_registro` | varchar(10) | Sim |
| `registrado_em`; `localizacao_capturada_em` | timestamptz | Sim |
| `latitude`; `longitude`; `precisao` | double precision | Sim |
| `endereco_registrado` | varchar(500) | Não |
| `fuso_dispositivo` | varchar(80) | Sim |
| `horario_inicio_previsto`; `horario_fim_previsto` | time | Sim |
| `janela_permitida_inicio`; `janela_permitida_fim` | timestamptz | Sim |
| `dentro_janela_permitida` | boolean | Sim |
| `criado_em`; `atualizado_em` | timestamptz | Sim |

Há checks para tipo (`START`/`END`), latitude, longitude e precisão. A combinação contratação/data/tipo é única.

### `relatorios_atendimento` — Relatórios de atendimento

| Colunas | Tipo/obrigatoriedade |
|---|---|
| `id`; `contratacao_id`; `registro_inicio_atendimento_id`; `registro_fim_atendimento_id`; `cuidador_id`; `responsavel_id`; `pessoa_assistida_id` | uuid Sim, PK/FKs |
| `data_atendimento` | date Sim |
| `texto_gerado`; `anotacoes_enfermagem` | text Sim |
| `texto_editado`; `texto_final` | text Não |
| `observacoes_adicionais` | varchar(4000) Não |
| `status`; `status_email` | varchar(20) Sim |
| `email_solicitado_em`; `email_enviado_em`; `proxima_tentativa_email_em` | timestamptz Não |
| `tentativas_email` | integer Sim |
| `mensagem_erro_email` | varchar(500) Não |
| `gerado_em`; `criado_em`; `atualizado_em` | timestamptz Sim |
| `editado_em`; `finalizado_em` | timestamptz Não |

A combinação contratação/data é única. O status do relatório admite `DRAFT` e `FINALIZED`; a entrega admite `NOT_SENT`, `PENDING`, `SENT` e `FAILED`.

## Notificações

| Tabela | Colunas |
|---|---|
| `notificacoes` | `id` uuid Sim; `usuario_destinatario_id` uuid Sim FK; `tipo` varchar(50) Sim; `titulo` varchar(180) Sim; `mensagem` varchar(500) Sim; `tipo_entidade_relacionada` varchar(40) Sim; `entidade_relacionada_id` uuid Sim; `lida_em`/`removida_em` timestamptz Não; `chave_deduplicacao` varchar(220) Não e única quando preenchida; `criado_em` timestamptz Sim. |
| `notificacoes_preferencias` | `id` uuid Sim; `usuario_id` uuid Sim FK; `tipo_notificacao` varchar(64) Sim; `habilitado` boolean Sim; `criado_em`/`atualizado_em` timestamptz Sim; par usuário/tipo único. |

## Relacionamentos principais

- Um `usuario` possui no máximo um perfil de responsável ou cuidador.
- Um responsável possui várias `pessoas_assistidas`; cada pessoa assistida possui um contato de emergência e coleções de alergias/restrições.
- Uma `solicitacao_servico` liga responsável, pessoa assistida e, quando definido, cuidador; sua aceitação origina uma `contratacao` única.
- Uma contratação provisiona `tarefas_cuidado`; cada tarefa gera várias `ocorrencias_cuidado` e seus lembretes/auditorias.
- Uma ocorrência concluída pode originar um `registro_diario_cuidado` e fotos; cuidados avulsos usam o mesmo diário sem ocorrência planejada.
- Uma contratação possui registros de início e fim de atendimento por data; esses registros sustentam um `relatorio_atendimento`.
- Notificações referenciam entidades de negócio de forma polimórfica por tipo e UUID.

## Enums e status

Os valores dos enums permanecem como códigos estáveis sem acento (por exemplo, `RESPONSAVEL`, `CUIDADOR`, `PENDENTE`, `FINALIZADA`, `START` e `END`). Eles são dados de integração e não rótulos de interface. O aplicativo continua convertendo esses códigos para textos visíveis corretamente acentuados em português.

## Segurança da migração

- As migrations V031–V039 usam apenas operações de rename; não removem nem recriam tabelas.
- Renomes preservam dados, constraints e índices vinculados no PostgreSQL.
- PKs, FKs, uniques, checks e índices são renomeados depois das tabelas e colunas.
- Cada lote possui `lock_timeout` de 10 segundos e `statement_timeout` de 2 minutos, evitando espera indefinida por locks.
- O banco limpo executa as migrations disponíveis até V039; bancos existentes executam apenas as migrations ainda pendentes.
- Antes da aplicação em um banco com dados importantes, deve ser realizado backup e teste em uma cópia representativa.

### Checklist operacional

- [ ] Backup realizado.
- [x] Cadeia disponível V001–V039 testada em PostgreSQL limpo temporário.
- [x] V039 testada no banco local com dados e contagens verificadas.
- [x] Contagens das 30 tabelas renomeadas conferidas antes/depois.
- [x] Aplicação iniciada com `ddl-auto=validate` no banco limpo e no banco com dados.
- [ ] Endpoints de autenticação, perfil, agenda, contratação, cuidados, diário, atendimento, relatório e notificações validados.
- [ ] RF09, RF10, RF11, RF12, RF14, RF15, RF17, RF18, RF19 e RF20 validados no ambiente integrado.
