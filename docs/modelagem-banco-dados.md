# Modelagem do Banco de Dados — Cuidar+

## 1. Visão geral

O PostgreSQL do Cuidar+ sustenta o cuidado domiciliar desde cadastro e autenticação até solicitação, contratação, rotina, execução dos cuidados, presença geolocalizada, relatório, notificações e administração. Este documento descreve o schema `public` após a V046 e foi conferido nas migrations V001–V046, entidades JPA, enums, repositories e serviços.

O modelo vigente possui 37 tabelas de domínio e `flyway_schema_history`. **Sim** significa `NOT NULL`; **Não**, que aceita `NULL`. **PK**, **FK** e **Unique** indicam chave primária, estrangeira e unicidade. `timestamptz` abrevia `timestamp with time zone`. As chaves de domínio usam `uuid`.

## 2. Padrão de nomenclatura

As tabelas foram nomeadas no singular, em português, sem acentuação e em `snake_case`. A escolha do singular representa cada tabela como uma entidade ou conceito do modelo de dados, como `usuario`, `cuidador`, `contratacao` e `ocorrencia_cuidado`. Tabelas filhas começam pelo agregado também no singular, como `rotina_cuidado_item`, `ocorrencia_cuidado_foto` e `notificacao_preferencia`. A `PortuguesePhysicalNamingStrategy` traduz os nomes lógicos históricos das entidades Java para o modelo físico, sem alterar classes ou contratos JSON. `flyway_schema_history` mantém o nome definido pelo Flyway; códigos de enum podem permanecer em inglês por serem valores de integração.

## 3. Organização por domínios

- **Usuários e perfis:** `usuario`, `usuario_token_redefinicao_senha`, `responsavel`, `cuidador`, suas cinco coleções e os históricos de situação de responsável e cuidador.
- **Pessoas assistidas:** `pessoa_assistida`, alergias, restrições alimentares e contato de emergência.
- **Solicitações e contratações:** `solicitacao_servico`, suas seis tabelas filhas, `contratacao` e histórico de status.
- **Planejamento e cuidado:** rotinas, tarefas, ocorrências, fotos, lembretes, auditoria e diário.
- **Atendimento e comunicação:** registros de atendimento, relatórios, notificações e preferências.
- **Infraestrutura:** `flyway_schema_history`.

## 4. Dicionário de tabelas

## 4.1 `usuario`

**Nome lógico:** Usuários. **Finalidade:** identidade, autenticação e controle administrativo da conta. **Entidade:** `User`. **Requisitos relacionados:** RF01, RF02, RF03, RF04, RF05, RF06, RF07, RF10, RF17, RF18, RF20 e RF21.

**Papel nos requisitos:** principal em cadastro, autenticação e perfis; apoio na recuperação de senha e nos fluxos que identificam participantes.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador do usuário. |
| `data_nascimento` | date | Sim | — | Data de nascimento. |
| `cpf` | varchar(11) | Sim | Unique | CPF normalizado e único. |
| `criado_em` | timestamptz | Sim | — | Criação da conta. |
| `email` | varchar(180) | Sim | Unique | E-mail de acesso e comunicação. |
| `nome_completo` | varchar(140) | Sim | — | Nome completo. |
| `senha_hash` | varchar(255) | Sim | — | Hash da senha; o valor original não é salvo. |
| `atualizado_em` | timestamptz | Sim | — | Última atualização. |
| `tipo_usuario` | varchar(20) | Sim | Check/Enum | Papel do usuário. |
| `telefone` | varchar(20) | Não | — | Telefone de contato. |
| `situacao_conta` | varchar(30) | Sim | Check/Enum | Situação geral: `ATIVO`, `BLOQUEADO` ou `INATIVO`. |
| `motivo_bloqueio` | varchar(1000) | Não | — | Justificativa administrativa do bloqueio. |
| `bloqueado_em` | timestamptz | Não | — | Data e hora do bloqueio. |
| `bloqueado_por_usuario_id` | uuid | Não | FK | Administrador que bloqueou a conta. |
| `desbloqueado_em` | timestamptz | Não | — | Data e hora do último desbloqueio. |
| `desbloqueado_por_usuario_id` | uuid | Não | FK | Administrador que desbloqueou a conta. |
| `ultimo_login_em` | timestamptz | Não | — | Último login bem-sucedido. |
| `url_foto_perfil` | varchar(500) | Não | — | Local da foto de perfil. |

**Relacionamentos e regras:** raiz referenciada pelos demais domínios; e-mail e CPF não se repetem. Cada conta pode ter no máximo um perfil de cada tipo. O check de papel aceita `RESPONSAVEL`, `CUIDADOR`, `ADMIN` e os legados `FAMILY`, `CAREGIVER`. Somente conta `ATIVO` autentica; apenas contas `ATIVO` podem ser bloqueadas e apenas contas `BLOQUEADO` podem ser desbloqueadas. Bloqueio e desbloqueio não alteram a aprovação do perfil e solicitam a comunicação por e-mail somente após a confirmação da transação. A V046 cria, apenas quando ainda não existe nenhum administrador, a conta inicial necessária para acessar a área administrativa; a senha inicial deve ser alterada após o primeiro acesso.

## 4.2 `usuario_token_redefinicao_senha`

**Nome lógico:** Tokens de redefinição. **Finalidade:** recuperação de senha. **Entidade:** `PasswordResetToken`. **Requisitos relacionados:** RF03.

**Papel nos requisitos:** principal; controla validade e uso único do token de recuperação.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador do token. |
| `criado_em` | timestamptz | Sim | — | Emissão. |
| `expira_em` | timestamptz | Sim | — | Validade máxima. |
| `hash_token` | varchar(64) | Sim | Unique | Hash do token. |
| `usado_em` | timestamptz | Não | — | Momento do consumo. |
| `usuario_id` | uuid | Sim | FK | Dono do token. |

**Relacionamentos e regras:** `usuario_id` → `usuario.id`; token expirado ou já usado é recusado.

## 4.3 `responsavel`

**Nome lógico:** Responsáveis. **Finalidade:** dados específicos de quem organiza o cuidado e sua aprovação administrativa. **Entidade:** `ResponsibleProfile`. **Requisitos relacionados:** RF01, RF04 e RF21.

**Papel nos requisitos:** principal no cadastro e gerenciamento do perfil do responsável.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador do perfil. |
| `usuario_id` | uuid | Sim | FK, Unique | Conta vinculada. |
| `parentesco` | varchar(40) | Sim | Enum | Relação com a pessoa assistida. |
| `parentesco_outro` | varchar(120) | Não | — | Complemento para `OUTRO`. |
| `preferencia_contato` | varchar(30) | Sim | Enum | Canal preferencial. |
| `situacao_aprovacao` | varchar(30) | Sim | Check/Enum | `PENDENTE`, `APROVADO`, `REPROVADO` ou `BLOQUEADO`. |
| `analisado_em` | timestamptz | Não | — | Data e hora da última análise. |
| `analisado_por_usuario_id` | uuid | Não | FK | Administrador da última análise. |
| `motivo_reprovacao` | varchar(1000) | Não | — | Motivo informado na reprovação. |
| `motivo_bloqueio` | varchar(1000) | Não | — | Motivo do bloqueio do perfil. |
| `criado_em` | timestamptz | Sim | — | Criação. |
| `atualizado_em` | timestamptz | Sim | — | Atualização. |

**Relacionamentos e regras:** `usuario_id` → `usuario.id`; a unique implementa relação um para um. A V045 preserva responsáveis anteriores como `APROVADO` e define novos cadastros como `PENDENTE`. Pessoas assistidas referenciam a conta em `usuario`, não este `id`; o responsável só autentica após aprovação e com conta ativa.

### 4.3.1 `responsavel_historico_situacao`

**Finalidade:** trilha de auditoria das decisões administrativas sobre o cadastro do responsável. **Entidade:** `ResponsibleStatusHistory`. **Requisito relacionado:** RF21.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador do evento. |
| `responsavel_id` | uuid | Sim | FK | Cadastro de responsável analisado. |
| `situacao_anterior` | varchar(30) | Não | Enum | Situação antes da ação. |
| `situacao_nova` | varchar(30) | Sim | Check/Enum | Situação definida pelo administrador. |
| `motivo` | varchar(1000) | Não | — | Justificativa da reprovação ou bloqueio. |
| `usuario_administrador_id` | uuid | Sim | FK | Administrador autor da decisão. |
| `criado_em` | timestamptz | Sim | — | Data e hora da ação. |

**Relacionamentos e regras:** FKs → `responsavel.id` e `usuario.id`. Aprovação e reprovação partem exclusivamente de `PENDENTE`; bloqueio parte exclusivamente de `APROVADO`. Cada decisão válida gera um registro imutável e solicita a notificação por e-mail após a confirmação da transação. Uma transição inválida retorna conflito sem criar histórico nem solicitar e-mail.

## 4.4 `cuidador`

**Nome lógico:** Cuidadores. **Finalidade:** qualificação, apresentação, endereço, disponibilidade e aprovação profissional. **Entidade:** `CaregiverProfile`. **Requisitos relacionados:** RF01, RF04, RF05, RF06, RF07 e RF21.

**Papel nos requisitos:** principal no perfil profissional, busca e visualização; apoio ao cadastro geral.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador do perfil. |
| `usuario_id` | uuid | Sim | FK, Unique | Conta vinculada. |
| `formacao_outro` | varchar(180) | Não | — | Formação livre. |
| `experiencia` | varchar(500) | Não | — | Experiência profissional. |
| `biografia` | varchar(500) | Não | — | Apresentação pública. |
| `cep` | varchar(9) | Não | — | CEP. |
| `rua` | varchar(180) | Não | — | Logradouro. |
| `numero` | varchar(30) | Não | — | Número. |
| `complemento` | varchar(120) | Não | — | Complemento. |
| `bairro` | varchar(120) | Não | — | Bairro. |
| `cidade` | varchar(120) | Não | — | Cidade. |
| `estado` | varchar(2) | Não | — | UF. |
| `ponto_referencia` | varchar(180) | Não | — | Referência de localização. |
| `horario_inicio` | time | Não | — | Início personalizado. |
| `horario_fim` | time | Não | — | Fim personalizado. |
| `observacao` | varchar(500) | Não | — | Observação de disponibilidade. |
| `modalidade_outro` | varchar(180) | Não | — | Modalidade livre. |
| `servico_outro` | varchar(180) | Não | — | Serviço livre. |
| `criado_em` | timestamptz | Sim | — | Criação. |
| `atualizado_em` | timestamptz | Sim | — | Atualização. |
| `tempo_experiencia` | varchar(30) | Não | Enum | Faixa de experiência. |
| `latitude` | numeric(10,7) | Não | — | Latitude para busca. |
| `longitude` | numeric(10,7) | Não | — | Longitude para busca. |
| `situacao_aprovacao` | varchar(30) | Sim | Check/Enum | `PENDENTE`, `APROVADO`, `REPROVADO` ou `BLOQUEADO`. |
| `analisado_em` | timestamptz | Não | — | Data e hora da última análise. |
| `analisado_por_usuario_id` | uuid | Não | FK | Administrador da última análise. |
| `motivo_reprovacao` | varchar(1000) | Não | — | Motivo informado na reprovação. |
| `motivo_bloqueio_profissional` | varchar(1000) | Não | — | Motivo do bloqueio profissional. |

**Relacionamentos e regras:** `usuario_id` → `usuario.id`; relação um para um. As cinco tabelas seguintes guardam coleções. `cuidador_formacao` é a fonte oficial das qualificações profissionais. A V044 migra cuidadores anteriores como `APROVADO` para preservar os fluxos existentes; depois troca o padrão para `PENDENTE`. Somente cuidador aprovado e com conta ativa aparece em busca, perfil público ou solicitação.

### 4.4.1 `cuidador_historico_situacao`

**Finalidade:** trilha de auditoria das decisões administrativas sobre o cadastro profissional. **Entidade:** `CaregiverStatusHistory`. **Requisito relacionado:** RF21.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador do evento. |
| `cuidador_id` | uuid | Sim | FK | Cadastro profissional analisado. |
| `situacao_anterior` | varchar(30) | Não | Enum | Situação antes da ação. |
| `situacao_nova` | varchar(30) | Sim | Check/Enum | Situação definida pelo administrador. |
| `motivo` | varchar(1000) | Não | — | Justificativa da reprovação ou bloqueio. |
| `usuario_administrador_id` | uuid | Sim | FK | Administrador autor da decisão. |
| `criado_em` | timestamptz | Sim | — | Data e hora da ação. |

**Relacionamentos e regras:** FKs → `cuidador.id` e `usuario.id`. Aprovação e reprovação partem exclusivamente de `PENDENTE`; o bloqueio profissional parte exclusivamente de `APROVADO`. Cada decisão válida gera um registro imutável e solicita a notificação por e-mail após a confirmação da transação; uma transição inválida retorna conflito sem criar histórico nem solicitar e-mail. O cuidador mantém também a última situação para consultas eficientes.

## 4.5 `cuidador_disponibilidade_dia`

**Finalidade:** dias disponíveis. **Entidade:** coleção de `CaregiverAvailability`. **Requisitos relacionados:** RF01, RF04, RF05, RF06 e RF07.

**Papel nos requisitos:** apoio; detalha a disponibilidade semanal do cuidador.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `perfil_cuidador_id` | uuid | Sim | FK | Cuidador proprietário. |
| `dia_semana` | varchar(20) | Sim | Enum | Dia disponível. |

**Relacionamentos e regras:** FK → `cuidador.id`. Não há PK/unique física; a coleção `Set` reduz duplicidade na aplicação.

## 4.6 `cuidador_disponibilidade_periodo`

**Finalidade:** períodos disponíveis. **Entidade:** coleção de `CaregiverAvailability`. **Requisitos relacionados:** RF01, RF04, RF05, RF06 e RF07.

**Papel nos requisitos:** apoio; complementa a disponibilidade por período do dia.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `perfil_cuidador_id` | uuid | Sim | FK | Cuidador proprietário. |
| `periodo` | varchar(30) | Sim | Enum | Manhã, tarde, noite ou outro período. |

**Relacionamentos e regras:** FK → `cuidador.id`; `HORARIO_PERSONALIZADO` usa os horários da tabela pai. Sem PK/unique física.

## 4.7 `cuidador_formacao`

**Finalidade:** múltiplas qualificações. **Entidade:** coleção de `CaregiverProfile`. **Requisitos relacionados:** RF01, RF04, RF05, RF06 e RF07.

**Papel nos requisitos:** apoio; qualifica o perfil profissional exibido e pesquisado.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `perfil_cuidador_id` | uuid | Sim | FK, Unique composto | Cuidador. |
| `formacao` | varchar(40) | Sim | Enum, Unique composto | Qualificação. |

**Relacionamentos e regras:** FK → `cuidador.id`; o par cuidador/formação é único.

## 4.8 `cuidador_modalidade`

**Finalidade:** modalidades aceitas pelo cuidador. **Entidade:** coleção de `CaregiverProfile`. **Requisitos relacionados:** RF01, RF04, RF05, RF06 e RF07.

**Papel nos requisitos:** apoio; informa as modalidades de atendimento do perfil.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `perfil_cuidador_id` | uuid | Sim | FK | Cuidador. |
| `modalidade` | varchar(40) | Sim | Enum | Forma de atendimento. |

**Relacionamentos e regras:** FK → `cuidador.id`; `OUTRO` é detalhado no pai. Sem PK/unique física.

## 4.9 `cuidador_servico`

**Finalidade:** serviços oferecidos. **Entidade:** coleção de `CaregiverProfile`. **Requisitos relacionados:** RF01, RF04, RF05, RF06 e RF07.

**Papel nos requisitos:** apoio; informa os serviços usados na apresentação e pesquisa do cuidador.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `perfil_cuidador_id` | uuid | Sim | FK | Cuidador. |
| `servico` | varchar(50) | Sim | Enum | Serviço oferecido. |

**Relacionamentos e regras:** FK → `cuidador.id`; `OUTRO` é detalhado no pai. Sem PK/unique física.

## 4.10 `pessoa_assistida`

**Nome lógico:** Pessoas assistidas. **Finalidade:** dados pessoais, necessidades e endereço de cuidado. **Entidade:** `AssistedPerson`. **Requisitos relacionados:** RF01, RF04, RF08, RF10, RF17 e RF18.

**Papel nos requisitos:** principal no cadastro/gerenciamento; apoio na solicitação, histórico, busca de serviço e atendimento.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador. |
| `usuario_responsavel_id` | uuid | Sim | FK | Responsável proprietário. |
| `nome` | varchar(140) | Sim | — | Nome completo. |
| `cpf` | varchar(11) | Não | — | CPF opcional, sem unique. |
| `data_nascimento` | date | Sim | — | Nascimento. |
| `grau_dependencia` | varchar(30) | Sim | Enum | Nível de dependência. |
| `mobilidade` | varchar(30) | Sim | Enum | Condição de mobilidade. |
| `mobilidade_outro` | varchar(120) | Não | — | Mobilidade livre. |
| `alergias_outro` | varchar(180) | Não | — | Alergia livre. |
| `alergias_detalhes` | varchar(500) | Não | — | Detalhes de alergias. |
| `restricoes_alimentares_outro` | varchar(180) | Não | — | Restrição livre. |
| `restricoes_alimentares_detalhes` | varchar(500) | Não | — | Detalhes alimentares. |
| `medicamentos` | varchar(500) | Não | — | Resumo de medicamentos. |
| `observacoes` | varchar(500) | Não | — | Observações de cuidado. |
| `cep` | varchar(9) | Não | — | CEP do cuidado. |
| `rua` | varchar(180) | Não | — | Logradouro. |
| `numero` | varchar(30) | Não | — | Número. |
| `complemento` | varchar(120) | Não | — | Complemento. |
| `bairro` | varchar(120) | Não | — | Bairro. |
| `cidade` | varchar(120) | Não | — | Cidade. |
| `estado` | varchar(2) | Não | — | UF. |
| `ponto_referencia` | varchar(180) | Não | — | Referência. |
| `criado_em` | timestamptz | Sim | — | Criação. |
| `atualizado_em` | timestamptz | Sim | — | Atualização. |
| `latitude` | numeric(10,7) | Não | — | Latitude do cuidado. |
| `longitude` | numeric(10,7) | Não | — | Longitude do cuidado. |

**Relacionamentos e regras:** `usuario_responsavel_id` → `usuario.id`; é referenciada pelos fluxos de serviço e cuidado. A solicitação exige que pertença ao responsável e tenha endereço. Dados de saúde e localização são sensíveis.

## 4.11 `pessoa_assistida_alergia`

**Finalidade:** categorias de alergia. **Entidade:** coleção de `AssistedPerson`. **Requisitos relacionados:** RF01 e RF04.

**Papel nos requisitos:** apoio; complementa o cadastro clínico da pessoa assistida.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `pessoa_assistida_id` | uuid | Sim | FK | Pessoa assistida. |
| `alergia` | varchar(40) | Sim | Enum | Categoria de alergia. |

**Relacionamentos e regras:** FK → `pessoa_assistida.id`; sem PK/unique física.

## 4.12 `pessoa_assistida_contato_emergencia`

**Finalidade:** contato de emergência. **Entidade:** `EmergencyContact`. **Requisitos relacionados:** RF01 e RF04.

**Papel nos requisitos:** apoio; complementa o cadastro da pessoa assistida.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador. |
| `pessoa_assistida_id` | uuid | Sim | FK, Unique | Pessoa vinculada. |
| `nome` | varchar(140) | Sim | — | Nome do contato. |
| `telefone` | varchar(20) | Sim | — | Telefone. |
| `vinculo` | varchar(120) | Sim | — | Relação com a pessoa. |
| `contato_responsavel` | boolean | Sim | — | Indica se é o responsável. |
| `criado_em` | timestamptz | Sim | — | Criação. |
| `atualizado_em` | timestamptz | Sim | — | Atualização. |

**Relacionamentos e regras:** FK → `pessoa_assistida.id`; a unique limita a um contato por pessoa.

## 4.13 `pessoa_assistida_restricao_alimentar`

**Finalidade:** categorias de restrição alimentar. **Entidade:** coleção de `AssistedPerson`. **Requisitos relacionados:** RF01 e RF04.

**Papel nos requisitos:** apoio; complementa o cadastro clínico da pessoa assistida.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `pessoa_assistida_id` | uuid | Sim | FK | Pessoa assistida. |
| `restricao` | varchar(40) | Sim | Enum | Restrição alimentar. |

**Relacionamentos e regras:** FK → `pessoa_assistida.id`; sem PK/unique física.

## 4.14 `solicitacao_servico`

**Nome lógico:** Solicitações e publicações de serviço. **Finalidade:** representa convite direto, publicação aberta e candidatura de cuidador. **Entidade:** `ServiceRequest`. **Requisitos relacionados:** RF08, RF09, RF10 e RF17.

**Papel nos requisitos:** principal para solicitar, responder, publicar e demonstrar interesse; apoio ao histórico.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador. |
| `usuario_responsavel_id` | uuid | Sim | FK | Responsável pelo serviço. |
| `usuario_cuidador_id` | uuid | Não | FK | Cuidador convidado ou interessado. |
| `pessoa_assistida_id` | uuid | Sim | FK | Pessoa que receberá cuidado. |
| `tipo_contratacao` | varchar(40) | Sim | Enum | Modalidade temporal da contratação. |
| `status` | varchar(30) | Sim | Enum | Situação da solicitação/publicação. |
| `data_inicio` | date | Não | — | Início previsto. |
| `data_fim` | date | Não | — | Fim previsto. |
| `descricao_necessidades` | varchar(2000) | Sim | — | Necessidades do atendimento. |
| `outra_atividade` | varchar(500) | Não | — | Atividade não padronizada. |
| `observacoes_adicionais` | varchar(2000) | Não | — | Informações complementares. |
| `observacoes_negociacao` | varchar(1000) | Não | — | Observações negociadas. |
| `criado_em` | timestamptz | Sim | — | Criação. |
| `atualizado_em` | timestamptz | Sim | — | Atualização. |
| `expira_em` | timestamptz | Sim | — | Prazo para resposta. |
| `cancelado_em` | timestamptz | Não | — | Cancelamento. |
| `motivo_rejeicao` | varchar(1000) | Não | — | Justificativa de rejeição. |
| `motivo_cancelamento` | varchar(1000) | Não | — | Justificativa de cancelamento. |
| `rotina_cuidado_id` | uuid | Não | FK | Rotina moderna escolhida. |
| `nome_rotina_copia` | varchar(140) | Não | — | Nome congelado da rotina. |
| `iniciado_por` | varchar(20) | Sim | Enum | Origem: responsável ou cuidador. |
| `usuario_solicitante_id` | uuid | Sim | FK | Usuário que iniciou o registro. |
| `oportunidade_origem_id` | uuid | Não | FK, Unique parcial | Publicação da qual nasceu a candidatura. |

**Relacionamentos e regras:** FKs para `usuario`, `pessoa_assistida`, `rotina_cuidado` e autorreferência. Uma candidatura por publicação/cuidador é garantida por índice unique parcial. Solicitação direta inicia `PENDENTE`; publicação sem cuidador inicia `ABERTA`; vencidas tornam-se `EXPIRADA`. Datas e horários variam conforme `tipo_contratacao`, e o fim não pode anteceder o início. Não existe tabela física `publicacoes_servico`.

## 4.15 `solicitacao_servico_agenda_dia`

**Finalidade:** grade semanal do serviço. **Entidade:** coleção de `ServiceRequest`. **Requisitos relacionados:** RF08 e RF12.

**Papel nos requisitos:** principal; define horários da solicitação e alimenta a agenda.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `solicitacao_servico_id` | uuid | Sim | PK/FK | Solicitação. |
| `dia_semana` | varchar(20) | Sim | PK/Enum | Dia agendado. |
| `horario_inicio` | time | Sim | — | Início diário. |
| `horario_fim` | time | Sim | — | Fim diário. |

**Relacionamentos e regras:** FK → `solicitacao_servico.id`, com exclusão em cascata. O fim deve ser posterior ao início; PK composta impede dia repetido.

## 4.16 `solicitacao_servico_atividade`

**Finalidade:** atividades solicitadas. **Entidade:** coleção de `ServiceRequest`. **Requisitos relacionados:** RF08.

**Papel nos requisitos:** principal; especifica o escopo do serviço solicitado.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `solicitacao_servico_id` | uuid | Sim | PK/FK | Solicitação. |
| `atividade` | varchar(50) | Sim | PK/Enum | Serviço necessário. |

**Relacionamentos e regras:** FK → `solicitacao_servico.id`, `ON DELETE CASCADE`; PK composta evita repetição.

## 4.17 `solicitacao_servico_data`

**Finalidade:** datas específicas de serviço pontual. **Entidade:** coleção de `ServiceRequest`. **Requisitos relacionados:** RF08 e RF12.

**Papel nos requisitos:** principal; define datas pontuais da solicitação e da agenda.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `solicitacao_servico_id` | uuid | Sim | PK/FK | Solicitação. |
| `data_servico` | date | Sim | PK | Data solicitada. |

**Relacionamentos e regras:** FK → `solicitacao_servico.id`, `ON DELETE CASCADE`; contratação pontual exige ao menos uma data.

## 4.18 `solicitacao_servico_contratacao_historico_status`

**Nome lógico:** Histórico de status. **Finalidade:** registra transições de solicitações e contratações. **Entidade:** `StatusHistory`. **Requisitos relacionados:** RF09, RF10 e RF11.

**Papel nos requisitos:** principal; preserva aceite/rejeição e histórico de mudanças e encerramentos.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador. |
| `tipo_entidade` | varchar(40) | Sim | Enum | Domínio do registro. |
| `entidade_id` | uuid | Sim | Referência lógica | UUID da solicitação ou contratação. |
| `status_anterior` | varchar(30) | Não | — | Situação anterior. |
| `novo_status` | varchar(30) | Sim | — | Situação resultante. |
| `usuario_alteracao_id` | uuid | Não | FK | Autor; nulo em automação. |
| `motivo` | varchar(1000) | Não | — | Justificativa. |
| `criado_em` | timestamptz | Sim | — | Momento da transição. |

**Relacionamentos e regras:** somente `usuario_alteracao_id` possui FK → `usuario.id`; `entidade_id` é polimórfico e não tem FK física. Índice por tipo, entidade e data apoia a linha do tempo.

## 4.19 `solicitacao_servico_item_cuidado_copia`

**Nome lógico:** Cópias dos itens de rotina. **Finalidade:** congela o cuidado aceito, preservando-o contra edições futuras. **Entidade:** `ServiceRequestCareItemSnapshot`. **Requisitos relacionados:** RF08 e RF13.

**Papel nos requisitos:** apoio; preserva os itens solicitados e origina tarefas após a contratação.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador da cópia. |
| `solicitacao_servico_id` | uuid | Sim | FK | Solicitação proprietária. |
| `rotina_cuidado_original_id` | uuid | Sim | FK | Rotina de origem. |
| `item_rotina_cuidado_original_id` | uuid | Não | FK | Item original; vira nulo se removido. |
| `titulo` | varchar(140) | Sim | — | Título congelado. |
| `descricao` | varchar(1000) | Não | — | Descrição congelada. |
| `ordem_exibicao` | integer | Sim | — | Ordem na rotina. |
| `criado_em` | timestamptz | Sim | — | Criação da cópia. |
| `categoria` | varchar(40) | Não | Enum | Categoria. |
| `categoria_personalizada` | varchar(120) | Não | — | Categoria livre. |
| `prioridade` | varchar(20) | Não | Enum | Prioridade. |
| `tipo_recorrencia` | varchar(40) | Não | Enum | Recorrência. |
| `horario_previsto` | time | Não | — | Horário planejado. |
| `intervalo_dias` | integer | Não | — | Intervalo em dias. |
| `lembrete_habilitado` | boolean | Não | — | Habilita antecedência. |
| `minutos_antecedencia_lembrete` | integer | Não | — | Antecedência. |
| `anotacoes` | varchar(2000) | Não | — | Notas do cuidado. |
| `nome_medicamento` | varchar(180) | Não | — | Medicamento. |
| `dosagem_medicamento` | varchar(80) | Não | — | Dosagem. |
| `unidade_medicamento` | varchar(30) | Não | Enum | Unidade. |
| `unidade_personalizada_medicamento` | varchar(80) | Não | — | Unidade livre. |
| `via_administracao_medicamento` | varchar(30) | Não | Enum | Via de administração. |
| `via_personalizada_medicamento` | varchar(120) | Não | — | Via livre. |
| `instrucoes_medicamento` | varchar(1000) | Não | — | Instruções. |
| `lembrar_no_horario_previsto` | boolean | Sim | — | Lembrete no horário. |
| `lembrete_atraso_habilitado` | boolean | Sim | — | Habilita alerta de atraso. |
| `minutos_para_atraso` | integer | Não | — | Tolerância para atraso. |
| `repetir_enquanto_pendente` | boolean | Sim | — | Repete lembrete. |
| `intervalo_repeticao_minutos` | integer | Não | — | Intervalo de repetição. |
| `importante` | boolean | Sim | — | Marca criticidade. |
| `notificar_responsavel_se_importante` | boolean | Sim | — | Alerta o responsável. |
| `exige_foto_conclusao` | boolean | Sim | — | Exige evidência fotográfica. |

**Relacionamentos e regras:** solicitação → `solicitacao_servico`; rotina → `rotina_cuidado`; item → `rotina_cuidado_item` com `ON DELETE SET NULL`. A cópia é a fonte de provisionamento das tarefas contratadas e deve permanecer imutável.

## 4.20 `solicitacao_servico_item_cuidado_copia_dia_semana`

**Finalidade:** dias semanais congelados na cópia. **Entidade:** coleção de `ServiceRequestCareItemSnapshot`. **Requisitos relacionados:** RF08 e RF13.

**Papel nos requisitos:** apoio; preserva a recorrência semanal acordada.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `item_copia_id` | uuid | Sim | PK/FK | Cópia do item. |
| `dia_semana` | varchar(20) | Sim | PK/Enum | Dia congelado. |

**Relacionamentos e regras:** FK → `solicitacao_servico_item_cuidado_copia.id`, `ON DELETE CASCADE`; PK composta.

## 4.21 `contratacao`

**Finalidade:** vínculo aceito entre responsável, cuidador e pessoa assistida. **Entidade:** `CareContract`. **Requisitos relacionados:** RF09, RF10, RF11, RF12, RF15, RF16, RF17 e RF18.

**Papel nos requisitos:** principal para histórico, encerramento e agenda; apoio aos fluxos que exigem vínculo ativo.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador. |
| `solicitacao_servico_id` | uuid | Sim | FK, Unique | Solicitação aceita. |
| `usuario_responsavel_id` | uuid | Sim | FK | Responsável. |
| `usuario_cuidador_id` | uuid | Sim | FK | Cuidador. |
| `pessoa_assistida_id` | uuid | Sim | FK | Pessoa atendida. |
| `status` | varchar(30) | Sim | Enum | Situação do vínculo. |
| `data_inicio` | date | Sim | — | Início. |
| `data_fim` | date | Não | — | Fim previsto. |
| `criado_em` | timestamptz | Sim | — | Criação. |
| `atualizado_em` | timestamptz | Sim | — | Atualização. |
| `motivo_cancelamento` | varchar(1000) | Não | — | Motivo do cancelamento. |
| `motivo_encerramento` | varchar(1000) | Não | — | Motivo do encerramento. |
| `tipo_encerramento` | varchar(50) | Não | Enum | Modalidade de término. |
| `motivo_solicitacao_encerramento` | varchar(1000) | Não | — | Justificativa solicitada. |
| `observacoes_encerramento` | varchar(1000) | Não | — | Observações do término. |
| `usuario_solicitante_encerramento_id` | uuid | Não | FK | Autor do pedido de encerramento. |
| `encerramento_solicitado_em` | timestamptz | Não | — | Momento do pedido. |
| `data_fim_efetiva` | date | Não | — | Data efetiva. |
| `cancelado_em` | timestamptz | Não | — | Momento do cancelamento. |
| `usuario_solicitante_cancelamento_id` | uuid | Não | FK | Autor do cancelamento. |
| `cancelamento_solicitado_em` | timestamptz | Não | — | Momento da solicitação. |

**Relacionamentos e regras:** FKs → solicitação, usuários e pessoa assistida. Uma solicitação gera no máximo uma contratação. Antes do início, cancela-se; após início, encerra-se. A aceitação define `AGENDADA` ou `ATIVA` e provisiona tarefas da rotina.

## 4.22 `rotina_cuidado`

**Finalidade:** modelos reutilizáveis de cuidados. **Entidade:** `CareRoutine`. **Requisitos relacionados:** RF08 e RF13.

**Papel nos requisitos:** principal na lista de tarefas; apoio à solicitação que seleciona uma rotina.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador. |
| `usuario_responsavel_id` | uuid | Sim | FK | Responsável proprietário. |
| `pessoa_assistida_id` | uuid | Não | FK | Pessoa específica, se houver. |
| `nome` | varchar(140) | Sim | — | Nome da rotina. |
| `descricao` | varchar(1000) | Não | — | Descrição. |
| `ativo` | boolean | Sim | — | Disponibilidade para uso. |
| `criado_em` | timestamptz | Sim | — | Criação. |
| `atualizado_em` | timestamptz | Sim | — | Atualização. |

**Relacionamentos e regras:** FKs → `usuario` e `pessoa_assistida`; possui itens com cascade. Só responsáveis gerenciam rotinas; uma rotina usada deve estar ativa, pertencer ao responsável/pessoa e conter ao menos um cuidado.

## 4.23 `rotina_cuidado_item`

**Finalidade:** cuidados que compõem uma rotina. **Entidade:** `CareRoutineItem`. **Requisitos relacionados:** RF13.

**Papel nos requisitos:** principal; define os cuidados planejados da rotina.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador. |
| `rotina_cuidado_id` | uuid | Sim | FK | Rotina pai. |
| `titulo` | varchar(140) | Sim | — | Título do cuidado. |
| `descricao` | varchar(1000) | Não | — | Descrição. |
| `ordem_exibicao` | integer | Sim | — | Ordem. |
| `ativo` | boolean | Sim | — | Ativação. |
| `criado_em` | timestamptz | Sim | — | Criação. |
| `atualizado_em` | timestamptz | Sim | — | Atualização. |
| `categoria` | varchar(40) | Não | Enum | Categoria. |
| `categoria_personalizada` | varchar(120) | Não | — | Categoria livre. |
| `prioridade` | varchar(20) | Não | Enum | Prioridade. |
| `tipo_recorrencia` | varchar(40) | Não | Enum | Recorrência. |
| `horario_previsto` | time | Não | — | Horário. |
| `intervalo_dias` | integer | Não | — | Intervalo em dias. |
| `lembrete_habilitado` | boolean | Não | — | Habilita antecedência. |
| `minutos_antecedencia_lembrete` | integer | Não | — | Antecedência. |
| `anotacoes` | varchar(2000) | Não | — | Notas. |
| `nome_medicamento` | varchar(180) | Não | — | Medicamento. |
| `dosagem_medicamento` | varchar(80) | Não | — | Dosagem. |
| `unidade_medicamento` | varchar(30) | Não | Enum | Unidade. |
| `unidade_personalizada_medicamento` | varchar(80) | Não | — | Unidade livre. |
| `via_administracao_medicamento` | varchar(30) | Não | Enum | Via. |
| `via_personalizada_medicamento` | varchar(120) | Não | — | Via livre. |
| `instrucoes_medicamento` | varchar(1000) | Não | — | Instruções. |
| `lembrar_no_horario_previsto` | boolean | Sim | — | Lembrete pontual. |
| `lembrete_atraso_habilitado` | boolean | Sim | — | Alerta de atraso. |
| `minutos_para_atraso` | integer | Não | — | Tolerância. |
| `repetir_enquanto_pendente` | boolean | Sim | — | Repete alerta. |
| `intervalo_repeticao_minutos` | integer | Não | — | Intervalo de repetição. |
| `importante` | boolean | Sim | — | Criticidade. |
| `notificar_responsavel_se_importante` | boolean | Sim | — | Alerta o responsável. |
| `exige_foto_conclusao` | boolean | Sim | — | Exige foto. |

**Relacionamentos e regras:** FK → `rotina_cuidado.id`, `ON DELETE CASCADE`. Categoria personalizada exige texto; recorrência semanal exige dias; intervalo deve ser positivo; configurações de lembrete exigem seus minutos; dados de medicamento só são aceitos para categoria `MEDICACAO`.

## 4.24 `rotina_cuidado_item_dia_semana`

**Finalidade:** recorrência semanal do item. **Entidade:** coleção de `CareRoutineItem`. **Requisitos relacionados:** RF13.

**Papel nos requisitos:** apoio; define em quais dias o item deve gerar tarefas.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `item_rotina_cuidado_id` | uuid | Sim | PK/FK | Item da rotina. |
| `dia_semana` | varchar(20) | Sim | PK/Enum | Dia de execução. |

**Relacionamentos e regras:** FK → `rotina_cuidado_item.id`, cascade; PK composta.

## 4.25 `tarefa_cuidado`

**Finalidade:** séries operacionais que geram ocorrências. **Entidade:** `CareTask`. **Requisitos relacionados:** RF13, RF14 e RF15.

**Papel nos requisitos:** principal na lista e nos lembretes; apoio ao registro das execuções.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador da série. |
| `titulo` | varchar(140) | Sim | — | Título. |
| `descricao` | varchar(2000) | Não | — | Descrição. |
| `categoria` | varchar(40) | Sim | Enum | Categoria. |
| `categoria_personalizada` | varchar(120) | Não | — | Categoria livre. |
| `prioridade` | varchar(20) | Sim | Enum | Prioridade. |
| `tipo_recorrencia` | varchar(40) | Sim | Enum | Regra de repetição. |
| `data_inicio` | date | Sim | — | Início da série. |
| `data_fim` | date | Não | Check | Fim, nunca antes do início. |
| `horario_previsto` | time | Sim | — | Horário local. |
| `intervalo_dias` | integer | Não | Check | Intervalo positivo. |
| `fuso_horario` | varchar(80) | Sim | — | Fuso IANA. |
| `lembrete_habilitado` | boolean | Sim | Check | Habilita antecedência. |
| `minutos_antecedencia_lembrete` | integer | Não | Check | Antecedência não negativa. |
| `anotacoes` | varchar(2000) | Não | — | Notas. |
| `status` | varchar(20) | Sim | Enum | Estado da série. |
| `pessoa_assistida_id` | uuid | Sim | FK | Pessoa. |
| `contratacao_id` | uuid | Sim | FK | Contratação. |
| `responsavel_criador_id` | uuid | Sim | FK | Responsável criador. |
| `cuidador_executor_id` | uuid | Sim | FK | Cuidador executor. |
| `serie_anterior_id` | uuid | Não | FK | Série substituída. |
| `nome_medicamento` | varchar(180) | Não | — | Medicamento. |
| `dosagem_medicamento` | varchar(80) | Não | — | Dosagem. |
| `unidade_medicamento` | varchar(30) | Não | Enum | Unidade. |
| `unidade_personalizada_medicamento` | varchar(80) | Não | — | Unidade livre. |
| `via_administracao_medicamento` | varchar(30) | Não | Enum | Via. |
| `via_personalizada_medicamento` | varchar(120) | Não | — | Via livre. |
| `instrucoes_medicamento` | varchar(1000) | Não | — | Instruções. |
| `criado_em` | timestamptz | Sim | — | Criação. |
| `atualizado_em` | timestamptz | Sim | — | Atualização. |
| `usuario_criacao_id` | uuid | Sim | FK | Autor da criação. |
| `usuario_atualizacao_id` | uuid | Sim | FK | Autor da atualização. |
| `versao` | bigint | Sim | Lock otimista | Versão concorrente. |
| `item_copia_origem_id` | uuid | Não | FK, Unique parcial | Cópia contratada de origem. |
| `lembrar_no_horario_previsto` | boolean | Sim | — | Alerta pontual. |
| `lembrete_atraso_habilitado` | boolean | Sim | — | Alerta de atraso. |
| `minutos_para_atraso` | integer | Não | — | Tolerância. |
| `repetir_enquanto_pendente` | boolean | Sim | — | Repetição. |
| `intervalo_repeticao_minutos` | integer | Não | — | Intervalo da repetição. |
| `importante` | boolean | Sim | — | Criticidade. |
| `notificar_responsavel_se_importante` | boolean | Sim | — | Notificação especial. |
| `exige_foto_conclusao` | boolean | Sim | — | Evidência obrigatória. |
| `tarefa_duplicada_de_id` | uuid | Não | FK | Série canônica da duplicata. |

**Relacionamentos e regras:** FKs para contratação, pessoa, usuários, cópia e autorreferências. Uma cópia origina no máximo uma série canônica. Somente séries ativas pausam; somente pausadas reativam; canceladas/finalizadas não executam. Alterações futuras podem encerrar a série e criar sucessora.

## 4.26 `tarefa_cuidado_dia_semana`

**Finalidade:** dias de recorrência da série. **Entidade:** coleção de `CareTask`. **Requisitos relacionados:** RF13.

**Papel nos requisitos:** apoio; materializa a recorrência semanal da tarefa.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `tarefa_id` | uuid | Sim | PK/FK | Série. |
| `dia_semana` | varchar(20) | Sim | PK/Enum | Dia. |

**Relacionamentos e regras:** FK → `tarefa_cuidado.id`; PK composta.

## 4.27 `tarefa_cuidado_auditoria`

**Finalidade:** trilha de mudanças em tarefas e ocorrências. **Entidade:** `TaskAuditEntry`. **Requisitos relacionados:** RF13 e RF15.

**Papel nos requisitos:** principal na auditoria do registro de atividades e apoio à gestão das tarefas.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador. |
| `tarefa_id` | uuid | Sim | FK | Tarefa auditada. |
| `ocorrencia_id` | uuid | Não | FK | Ocorrência, quando aplicável. |
| `usuario_ator_id` | uuid | Não | FK | Autor; nulo para automação. |
| `acao` | varchar(40) | Sim | Enum | Evento auditado. |
| `detalhes` | varchar(500) | Não | — | Contexto. |
| `criado_em` | timestamptz | Sim | — | Momento. |

**Relacionamentos e regras:** FKs → tarefa, ocorrência e usuário. A nulabilidade do ator permite expiração e outros processos automáticos.

## 4.28 `ocorrencia_cuidado`

**Finalidade:** execução prevista ou realizada de uma tarefa em data e horário. **Entidade:** `TaskOccurrence`. **Requisitos relacionados:** RF12, RF14, RF15, RF16 e RF19.

**Papel nos requisitos:** principal nos lembretes e registros de execução; apoio à agenda, diário e relatório.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador. |
| `tarefa_id` | uuid | Sim | FK, Unique composto | Série de origem. |
| `contratacao_id` | uuid | Sim | FK | Contratação. |
| `pessoa_assistida_id` | uuid | Sim | FK | Pessoa. |
| `usuario_cuidador_id` | uuid | Sim | FK | Cuidador previsto. |
| `data_prevista` | date | Sim | Unique composto | Data local. |
| `horario_previsto` | time | Sim | Unique composto | Hora local. |
| `instante_previsto_utc` | timestamptz | Sim | — | Instante UTC para processamento. |
| `fuso_horario` | varchar(80) | Sim | — | Fuso da previsão. |
| `status` | varchar(25) | Sim | Enum | Estado da ocorrência. |
| `concluido_em` | timestamptz | Não | — | Conclusão. |
| `usuario_executor_id` | uuid | Não | FK | Usuário que registrou. |
| `motivo_nao_realizacao` | varchar(1000) | Não | — | Justificativa. |
| `anotacao_execucao` | varchar(1000) | Não | — | Nota da execução. |
| `cancelado_em` | timestamptz | Não | — | Cancelamento. |
| `excecao` | boolean | Sim | — | Indica exceção à série. |
| `criado_em` | timestamptz | Sim | — | Criação. |
| `atualizado_em` | timestamptz | Sim | — | Atualização. |
| `versao` | bigint | Sim | Lock otimista | Versão concorrente. |
| `marcada_nao_realizada_automaticamente` | boolean | Sim | — | Marca ação automática. |
| `status_atualizado_em` | timestamptz | Não | — | Última mudança de estado. |

**Relacionamentos e regras:** FKs → tarefa, contratação, pessoa e usuários. `tarefa_id,data_prevista,horario_previsto` é único; índice adicional inclui a contratação. Só se atualiza no dia previsto e em contratação válida; foto é exigida quando configurada; não realização exige justificativa; estados terminais não são reexecutados; conclusão cria um registro diário único.

## 4.29 `ocorrencia_cuidado_foto`

**Finalidade:** evidências fotográficas de ocorrência ou cuidado avulso. **Entidade:** `CareOccurrencePhoto`. **Requisitos relacionados:** RF15, RF16 e RF19.

**Papel nos requisitos:** apoio; comprova cuidados planejados ou avulsos e integra o relatório.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador. |
| `ocorrencia_id` | uuid | Não | FK | Ocorrência planejada. |
| `usuario_envio_id` | uuid | Sim | FK | Autor do envio. |
| `nome_arquivo` | varchar(80) | Sim | Unique | Nome interno. |
| `nome_arquivo_original` | varchar(255) | Não | — | Nome recebido. |
| `tipo_conteudo` | varchar(30) | Sim | — | MIME type. |
| `tamanho_arquivo` | bigint | Sim | — | Tamanho em bytes. |
| `criado_em` | timestamptz | Sim | — | Envio. |
| `registro_atividade_id` | uuid | Não | FK | Cuidado diário avulso. |

**Relacionamentos e regras:** FKs → ocorrência, diário e usuário; os dois pais usam cascade. Check exige exatamente um entre `ocorrencia_id` e `registro_atividade_id`. O serviço limita a cinco fotos e controla tipo/tamanho no armazenamento.

## 4.30 `ocorrencia_cuidado_lembrete`

**Finalidade:** agenda e resultado dos lembretes. **Entidade:** `TaskReminder`. **Requisitos relacionados:** RF14.

**Papel nos requisitos:** principal; controla agendamento, deduplicação e entrega dos lembretes.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador. |
| `ocorrencia_id` | uuid | Sim | FK | Ocorrência. |
| `usuario_destinatario_id` | uuid | Sim | FK | Destinatário. |
| `tipo_lembrete` | varchar(40) | Sim | Enum | Momento/finalidade. |
| `previsto_em` | timestamptz | Sim | — | Disparo previsto. |
| `enviado_em` | timestamptz | Não | — | Envio efetivo. |
| `cancelado_em` | timestamptz | Não | — | Cancelamento. |
| `status` | varchar(20) | Sim | Enum | Estado do processamento. |
| `chave_deduplicacao` | varchar(220) | Sim | Unique | Idempotência. |
| `criado_em` | timestamptz | Sim | — | Criação. |
| `atualizado_em` | timestamptz | Sim | — | Atualização. |

**Relacionamentos e regras:** FKs → ocorrência e usuário. A chave impede disparos duplicados; lembretes futuros são cancelados quando tarefa/ocorrência deixa de ser executável.

## 4.31 `registro_diario_cuidado`

**Finalidade:** diário cronológico de cuidados planejados e avulsos. **Entidade:** `CareActivityRecord`. **Requisitos relacionados:** RF15, RF16 e RF19.

**Papel nos requisitos:** principal no registro e no diário; apoio à consolidação do relatório.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador. |
| `ocorrencia_id` | uuid | Não | FK, Unique | Ocorrência concluída. |
| `contratacao_id` | uuid | Sim | FK | Contratação. |
| `pessoa_assistida_id` | uuid | Sim | FK | Pessoa. |
| `usuario_responsavel_id` | uuid | Sim | FK | Responsável. |
| `usuario_cuidador_id` | uuid | Sim | FK | Cuidador. |
| `tipo_atividade` | varchar(40) | Sim | Código | `TAREFA_CONCLUIDA` ou `CUIDADO_AVULSO`. |
| `titulo` | varchar(180) | Sim | — | Título. |
| `anotacoes` | varchar(1000) | Não | — | Notas. |
| `ocorrido_em` | timestamptz | Sim | — | Momento real. |
| `criado_em` | timestamptz | Sim | — | Persistência. |
| `tipo_origem` | varchar(20) | Sim | Enum | `PLANNED` ou `MANUAL`. |
| `data_registro` | date | Sim | — | Data local. |
| `fuso_horario` | varchar(80) | Sim | — | Fuso. |
| `tipo_cuidado` | varchar(40) | Sim | Código | Categoria planejada ou tipo manual. |
| `descricao` | varchar(2000) | Não | — | Descrição. |
| `importante` | boolean | Sim | — | Sinalização. |
| `usuario_criacao_id` | uuid | Sim | FK | Autor. |

**Relacionamentos e regras:** FKs → ocorrência, contratação, pessoa e usuários. Uma ocorrência gera no máximo um diário; cuidado manual fica sem ocorrência. Registro exige participante autorizado, pessoa compatível e atendimento válido na data.

## 4.32 `registro_atendimento`

**Finalidade:** início/fim real do atendimento com localização e janela permitida. **Entidade:** `ServiceAttendanceRecord`. **Requisitos relacionados:** RF12, RF16, RF18 e RF19.

**Papel nos requisitos:** principal no check-in/check-out; apoio à agenda, ao diário e ao relatório.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador. |
| `contratacao_id` | uuid | Sim | FK, Unique composto | Contratação. |
| `cuidador_id` | uuid | Sim | FK | Cuidador. |
| `responsavel_id` | uuid | Sim | FK | Responsável. |
| `pessoa_assistida_id` | uuid | Sim | FK | Pessoa. |
| `data_atendimento` | date | Sim | Unique composto | Data local. |
| `tipo_registro` | varchar(10) | Sim | Check/Unique composto | `START` ou `END`. |
| `registrado_em` | timestamptz | Sim | — | Momento real. |
| `latitude` | double precision | Sim | Check | Latitude entre -90 e 90. |
| `longitude` | double precision | Sim | Check | Longitude entre -180 e 180. |
| `precisao` | double precision | Sim | Check | Precisão entre 0 e 1000 metros. |
| `localizacao_capturada_em` | timestamptz | Sim | — | Momento da captura. |
| `endereco_registrado` | varchar(500) | Não | — | Texto do local. |
| `fuso_dispositivo` | varchar(80) | Sim | — | Fuso do dispositivo. |
| `horario_inicio_previsto` | time | Sim | — | Início planejado. |
| `horario_fim_previsto` | time | Sim | — | Fim planejado. |
| `janela_permitida_inicio` | timestamptz | Sim | — | Limite inicial. |
| `janela_permitida_fim` | timestamptz | Sim | — | Limite final. |
| `dentro_janela_permitida` | boolean | Sim | — | Resultado da validação temporal. |
| `criado_em` | timestamptz | Sim | — | Criação. |
| `atualizado_em` | timestamptz | Sim | — | Atualização. |

**Relacionamentos e regras:** FKs → contratação, usuários e pessoa; contratação usa `ON DELETE CASCADE`. O triplo contratação/data/tipo é único. Só o cuidador contratado registra; `END` depende de `START`; a agenda e a janela são validadas e a localização é obrigatória.

## 4.33 `relatorio_atendimento`

**Finalidade:** consolida o atendimento e controla finalização e e-mail assíncrono. **Entidade:** `AttendanceReport`. **Requisitos relacionados:** RF19.

**Papel nos requisitos:** principal; gera, edita, finaliza e controla o envio assíncrono do relatório.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador. |
| `contratacao_id` | uuid | Sim | FK, Unique composto | Contratação. |
| `data_atendimento` | date | Sim | Unique composto | Data. |
| `registro_inicio_atendimento_id` | uuid | Sim | FK | Registro `START`. |
| `registro_fim_atendimento_id` | uuid | Sim | FK | Registro `END`. |
| `cuidador_id` | uuid | Sim | FK | Cuidador. |
| `responsavel_id` | uuid | Sim | FK | Responsável. |
| `pessoa_assistida_id` | uuid | Sim | FK | Pessoa. |
| `texto_gerado` | text | Sim | — | Texto automático inicial. |
| `texto_editado` | text | Não | — | Versão editada. |
| `texto_final` | text | Não | — | Conteúdo congelado final. |
| `observacoes_adicionais` | varchar(4000) | Não | — | Complemento do cuidador. |
| `anotacoes_enfermagem` | text | Sim | — | Consolidação clínica/operacional. |
| `status` | varchar(20) | Sim | Check/Enum | `DRAFT` ou `FINALIZED`. |
| `status_email` | varchar(20) | Sim | Check/Enum | Estado da entrega. |
| `email_enviado_em` | timestamptz | Não | — | Envio concluído. |
| `mensagem_erro_email` | varchar(500) | Não | — | Último erro. |
| `gerado_em` | timestamptz | Sim | — | Geração. |
| `editado_em` | timestamptz | Não | — | Última edição. |
| `finalizado_em` | timestamptz | Não | — | Finalização. |
| `criado_em` | timestamptz | Sim | — | Criação. |
| `atualizado_em` | timestamptz | Sim | — | Atualização. |
| `email_solicitado_em` | timestamptz | Não | — | Entrada na fila. |
| `tentativas_email` | integer | Sim | — | Número de tentativas. |
| `proxima_tentativa_email_em` | timestamptz | Não | — | Próximo retry. |

**Relacionamentos e regras:** FKs → contratação, dois registros de atendimento, usuários e pessoa. Uma contratação tem no máximo um relatório por data. Relatório nasce após encerramento, em `DRAFT`; só o cuidador edita/finaliza; finalizado não é alterado e fica disponível ao responsável; e-mail usa estados e retentativas assíncronas.

## 4.34 `notificacao`

**Finalidade:** notificações internas dos eventos do sistema. **Entidade:** `Notification`. **Requisitos relacionados:** RF09, RF11, RF14, RF17, RF18 e RF19.

**Papel nos requisitos:** apoio; comunica decisões, encerramentos, lembretes, interesses, atendimento e relatório.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador. |
| `usuario_destinatario_id` | uuid | Sim | FK | Destinatário. |
| `tipo` | varchar(50) | Sim | Enum | Evento de negócio. |
| `titulo` | varchar(180) | Sim | — | Título exibido. |
| `mensagem` | varchar(500) | Sim | — | Mensagem exibida. |
| `tipo_entidade_relacionada` | varchar(40) | Sim | Enum | Tipo do alvo. |
| `entidade_relacionada_id` | uuid | Sim | Referência lógica | UUID do alvo. |
| `lida_em` | timestamptz | Não | — | Leitura. |
| `removida_em` | timestamptz | Não | — | Remoção lógica. |
| `criado_em` | timestamptz | Sim | — | Criação. |
| `chave_deduplicacao` | varchar(220) | Não | Unique parcial | Idempotência opcional. |

**Relacionamentos e regras:** FK física apenas para `usuario`; o alvo é polimórfico. Preferências podem suprimir eventos configuráveis. A chave, quando preenchida, impede duplicação; remoção é lógica.

## 4.35 `notificacao_preferencia`

**Finalidade:** habilitação de tipos de notificação por usuário. **Entidade:** `UserNotificationPreference`. **Requisitos relacionados:** RF09, RF11, RF14, RF17, RF18 e RF19.

**Papel nos requisitos:** apoio; aplica a escolha do usuário aos eventos notificáveis.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `id` | uuid | Sim | PK | Identificador. |
| `usuario_id` | uuid | Sim | FK, Unique composto | Usuário. |
| `tipo_notificacao` | varchar(64) | Sim | Enum, Unique composto | Evento configurado. |
| `habilitado` | boolean | Sim | — | Permite ou bloqueia. |
| `criado_em` | timestamptz | Sim | — | Criação. |
| `atualizado_em` | timestamptz | Sim | — | Atualização. |

**Relacionamentos e regras:** FK → `usuario.id`, `ON DELETE CASCADE`; o par usuário/tipo é único. Tipos antigos de tarefa foram normalizados para códigos de ocorrência pela V022.

## 4.36 `flyway_schema_history`

**Nome lógico:** Histórico de migrations. **Finalidade:** infraestrutura de versionamento do schema. **Entidade:** gerenciada pelo Flyway, sem entidade JPA. **Requisitos relacionados:** nenhum RF funcional.

**Papel nos requisitos:** infraestrutura; garante a evolução técnica do schema, sem atender diretamente a uma funcionalidade.

| Coluna | Tipo | Obrigatório | Chave | Descrição |
|---|---|---:|---|---|
| `installed_rank` | integer | Sim | PK | Ordem de instalação. |
| `version` | varchar(50) | Não | — | Versão da migration; pode ser nula em repeatable. |
| `description` | varchar(200) | Sim | — | Descrição. |
| `type` | varchar(20) | Sim | — | Tipo do artefato. |
| `script` | varchar(1000) | Sim | — | Nome do script. |
| `checksum` | integer | Não | — | Soma de verificação. |
| `installed_by` | varchar(100) | Sim | — | Usuário do banco. |
| `installed_on` | timestamp | Sim | — | Instalação, padrão `now()`. |
| `execution_time` | integer | Sim | — | Duração em milissegundos. |
| `success` | boolean | Sim | — | Resultado. |

**Relacionamentos e regras:** não participa do domínio; o Flyway controla escrita, valida checksum e consulta `success`. Seu nome não deve ser traduzido.

## 5. Relacionamentos principais

- `usuario` é a raiz de identidade; `responsavel` e `cuidador` especializam uma conta em relações um para um.
- Um responsável cadastra várias `pessoa_assistida`; cada pessoa tem coleções clínicas e até um contato de emergência.
- `solicitacao_servico` une responsável, pessoa e, quando conhecido, cuidador. Publicações abertas e candidaturas usam a mesma tabela e a autorreferência `oportunidade_origem_id`.
- A aceitação produz uma única `contratacao`, cujo histórico de estados compartilha a tabela polimórfica com solicitações.
- `rotina_cuidado` possuem itens; ao solicitar, esses itens são copiados para preservar o conteúdo acordado.
- A contratação provisiona `tarefa_cuidado`; uma tarefa gera várias `ocorrencia_cuidado`, lembretes e auditorias.
- Uma ocorrência concluída gera no máximo um `registro_diario_cuidado`; cuidados manuais usam o diário sem ocorrência. Fotos ligam-se exclusivamente a um desses dois pais.
- Por contratação e data, `registro_atendimento` guarda `START` e `END`; ambos sustentam um `relatorio_atendimento` único.
- `notificacao` aponta para entidades de negócio por tipo e UUID, mas somente o destinatário possui FK física.

## 6. Status e tipos enumerados

As colunas são `varchar`, não enums nativos do PostgreSQL. Os valores vêm dos enums Java, exceto onde indicado.

### Identidade e perfil

| Campo/enum | Valores e significado |
|---|---|
| `tipo_usuario` | `RESPONSAVEL` (organiza o cuidado), `CUIDADOR` (executa), `ADMIN`; `FAMILY` e `CAREGIVER` são legados. |
| `Parentesco` | `FILHO`, `CONJUGE`, `NETO`, `IRMAO`, `SOBRINHO`, `TUTOR_LEGAL`, `RESPONSAVEL_CONTRATUAL`, `AMIGO`, `OUTRO`. |
| `PreferenciaContato` | `WHATSAPP`, `LIGACAO`, `EMAIL`, `SMS`, `QUALQUER`. |
| `FormacaoCuidador` | curso de cuidador, técnico/auxiliar/superior em enfermagem, primeiros socorros, experiência prática ou outro. |
| `TempoExperiencia` | menos de 1; 1–2; 3–5; 6–10; mais de 10 anos. |
| `ModalidadeAtendimento` | diurno, noturno, 12x36, 24h, eventual, fins de semana, consultas ou outro. |
| `ServicoOferecido` | higiene, banho, alimentação, locomoção, companhia, medicação orientada, consultas, tarefas leves, monitoramento noturno ou outro. |
| `DiaSemana` | `SEGUNDA` a `DOMINGO`. |
| `PeriodoDisponibilidade` | `MANHA`, `TARDE`, `NOITE`, `MADRUGADA`, `INTEGRAL`, `HORARIO_PERSONALIZADO`. |
| `GrauDependencia` | `BAIXA`, `MODERADA`, `ALTA`, `TOTAL`, `NAO_SEI_INFORMAR`. |
| `Mobilidade` | independente, bengala, andador, cadeira de rodas, acamado, auxílio de pessoa ou outro. |
| `Alergia` | não possui, medicamentos, alimentos, higiene, látex, poeira, outro ou não informado. |
| `RestricaoAlimentar` | não possui, diabética, hipossódica, pastosa, líquida, sem lactose/glúten, vegetariana, outra ou não informada. |

### Solicitações e contratações

| Campo/enum | Valores e significado |
|---|---|
| `HiringType` | `PONTUAL`, `PERIODO_DETERMINADO`, `PERIODO_INDETERMINADO`. |
| `ServiceRequestStatus` | `ABERTA`, `PENDENTE`, `ACEITA`, `REJEITADA`, `CANCELADA`, `EXPIRADA`. |
| `ServiceRequestInitiator` | `RESPONSIBLE` (responsável) ou `CAREGIVER` (cuidador). |
| `CareContractStatus` | `AGENDADA`, `ATIVA`, `ENCERRAMENTO_AGENDADO`, `ENCERRADA`, `FINALIZADA`, `CANCELADA`. |
| `ContractTerminationType` | término previsto, antecipado por uma parte, acordo, cancelamento antes do início ou término automático. |
| `StatusHistoryEntityType` | `SERVICE_REQUEST` ou `CARE_CONTRACT`. |

### Planejamento e execução

| Campo/enum | Valores e significado |
|---|---|
| `TaskCategory` | medicação, alimentação, hidratação, higiene/banho, mobilidade, exercício, curativo, sinais vitais, compromisso ou personalizada. |
| `TaskPriority` | `BAIXA`, `NORMAL`, `ALTA`. |
| `TaskRecurrenceType` | única, diária, dias específicos, intervalo, período determinado ou sem data final. |
| `TaskSeriesStatus` | `ATIVA`, `PAUSADA`, `CANCELADA`, `FINALIZADA`. |
| `TaskOccurrenceStatus` | `PENDENTE`, `CONCLUIDA`, `ATRASADA`, `NAO_REALIZADA`, `CANCELADA`. |
| `MedicationUnit` | mg, g, ml, gotas, comprimido, cápsula, aplicação ou personalizada. |
| `MedicationAdministrationRoute` | oral, tópica, inalatória, subcutânea ou outra. |
| `TaskReminderType` | antes, no horário, atraso, repetição pendente ou alerta ao responsável. |
| `TaskReminderStatus` | `SCHEDULED`, `SENT`, `CANCELED`, `SKIPPED`, `FAILED`. |
| `CareRecordSourceType` | `PLANNED` ou `MANUAL`. |
| `ManualCareType` | medicação, alimentação, higiene, mobilidade, companhia, observação, ocorrência ou outro. |
| `TaskAuditAction` | criação/alteração/pausa/reativação/cancelamento e eventos de ocorrência, foto, notificação e lembrete. |

### Atendimento, relatório e notificações

| Campo/enum | Valores e significado |
|---|---|
| `AttendanceRecordType` | `START` (início) e `END` (encerramento). |
| `AttendanceReportStatus` | `DRAFT` (rascunho) e `FINALIZED` (finalizado). |
| `AttendanceReportEmailStatus` | `NOT_SENT`, `PENDING`, `SENT`, `FAILED`. |
| `RelatedEntityType` | solicitação, contratação, tarefa, ocorrência (código antigo ou atual) e relatório. |
| `NotificationType` | eventos de solicitação/publicação/interesse, contratação, atendimento, relatório, tarefa e ocorrência. A lista canônica está em `NotificationType.java`; códigos antigos de tarefa continuam declarados por compatibilidade. |

Os valores persistíveis de `NotificationType` são:

| Grupo | Valores e significado |
|---|---|
| Solicitação direta | `SERVICE_REQUEST_CREATED` (criada), `SERVICE_REQUEST_ACCEPTED` (aceita), `SERVICE_REQUEST_REJECTED` (rejeitada), `SERVICE_REQUEST_CANCELED` (cancelada), `SERVICE_REQUEST_EXPIRED` (expirada). |
| Publicação e interesse | `SERVICE_PUBLICATION_CREATED`, `SERVICE_PUBLICATION_CANCELED`, `SERVICE_PUBLICATION_EXPIRED`, `SERVICE_PUBLICATION_STATUS_UPDATED`; `SERVICE_OPPORTUNITY_APPLICATION_CREATED`, `SERVICE_OPPORTUNITY_APPLICATION_ACCEPTED`, `SERVICE_OPPORTUNITY_APPLICATION_REJECTED`. |
| Contratação | `CONTRACT_TERMINATION_SCHEDULED` (término agendado), `CONTRACT_TERMINATED` (encerrada), `CONTRACT_CANCELED_BEFORE_START` (cancelada antes do início), `CONTRACT_AUTOMATICALLY_TERMINATED` (término automático). |
| Atendimento e relatório | `SERVICE_ATTENDANCE_STARTED`, `SERVICE_ATTENDANCE_ENDED`, `ATTENDANCE_REPORT_AVAILABLE`. |
| Cuidado — códigos atuais | `CARE_TASK_CREATED`, `CARE_TASK_CANCELED`, `CARE_OCCURRENCE_REMINDER`, `CARE_OCCURRENCE_OVERDUE`, `CARE_OCCURRENCE_NOT_DONE`, `CARE_OCCURRENCE_COMPLETED`, `CARE_OCCURRENCE_RESPONSIBLE_ALERT`. |
| Cuidado — compatibilidade | `TASK_OCCURRENCE_COMPLETED`, `TASK_OCCURRENCE_NOT_COMPLETED`, `CARE_TASK_REMINDER`, `CARE_TASK_OVERDUE`, `CARE_TASK_NOT_DONE`, `CARE_TASK_RESPONSIBLE_ALERT`; a V022 normaliza preferências antigas para os códigos atuais de ocorrência. |

As ações persistíveis de `TaskAuditAction` são `CRIADA`, `ALTERADA`, `PAUSADA`, `REATIVADA` e `CANCELADA` para a série; `OCORRENCIA_ALTERADA`, `OCORRENCIA_CANCELADA`, `OCORRENCIA_CONCLUIDA`, `OCORRENCIA_NAO_REALIZADA` e `OCORRENCIA_NAO_REALIZADA_AUTOMATICAMENTE` para execuções; e `FOTO_ANEXADA`, `NOTIFICACAO_INTERNA_CRIADA` e `LEMBRETE_CANCELADO` para efeitos associados.

### Campos textuais sem enum/check completo

- `usuario.situacao_conta` possui check para `ATIVO`, `BLOQUEADO` e `INATIVO`; é independente das aprovações de `responsavel` e `cuidador`.
- `registro_diario_cuidado.tipo_atividade` usa atualmente `TAREFA_CONCLUIDA` e `CUIDADO_AVULSO`; `tipo_cuidado` recebe `TaskCategory` ou `ManualCareType` como texto.

## 7. Observações de integridade e regras de negócio

### Interface e transições administrativas (RF20/RF21)

A navegação do administrador possui as áreas principais **Início**, **Usuários**, **Aprovações** e **Perfil**. O Início consulta `GET /api/admin/dashboard` para exibir cuidadores e responsáveis pendentes, total de usuários, contas bloqueadas e aprovações dos últimos sete dias. **Usuários** concentra pesquisa, filtros, detalhes, bloqueio e desbloqueio de contas. **Aprovações** abre em cuidadores pendentes e também permite alternar para responsáveis, preservando a mesma regra administrativa para os dois perfis.

As transições aceitas são:

| Ação | Transição permitida |
|---|---|
| Aprovar cuidador ou responsável | `PENDENTE` → `APROVADO` |
| Reprovar cuidador ou responsável | `PENDENTE` → `REPROVADO` |
| Bloquear perfil profissional/cadastral | `APROVADO` → `BLOQUEADO` |
| Bloquear conta | `ATIVO` → `BLOQUEADO` |
| Desbloquear conta | `BLOQUEADO` → `ATIVO` |

Desbloquear uma conta não altera a situação de aprovação do perfil. Os botões da interface acompanham o estado atual, mas a regra definitiva é aplicada transacionalmente no backend e uma tentativa incompatível retorna HTTP 409. Histórico e e-mail só são produzidos para mudanças válidas; a solicitação de e-mail ocorre após a confirmação da transação para não comunicar uma alteração que tenha sofrido rollback.

- Uniques evitam duplicidade de e-mail, CPF, token, perfil por usuário, solicitação contratada, candidatura por cuidador, ocorrência por agenda, lembrete/notificação por chave, diário por ocorrência, marcação de presença por tipo e relatório por data.
- Cascades existem nas coleções e cópias dependentes, em registros de atendimento/relatórios ligados à contratação e nas fotos ligadas ao pai. Outras FKs não definem cascade e exigem exclusão coordenada.
- Checks físicos validam tipo e coordenadas do atendimento, status do relatório, datas/intervalo/lembrete da tarefa, papel do usuário e exclusividade do pai da foto.
- `versao` em tarefas e ocorrências implementa bloqueio otimista contra atualizações concorrentes.
- Ações administrativas de conta e de aprovação carregam o alvo com bloqueio pessimista, validam a transição dentro da transação e evitam efeitos duplicados em requisições concorrentes.
- Cópias de rotina mantêm o conteúdo acordado mesmo se a rotina original for editada; a FK do item original usa `ON DELETE SET NULL`.
- Históricos e notificações possuem referências polimórficas sem FK física; a aplicação deve garantir que tipo e UUID correspondam.
- Índices cobrem consultas por participante/status/data, agenda, ocorrências vencidas, lembretes devidos, entrega de e-mail e linhas do tempo.

## 8. Critérios de rastreabilidade

As matrizes classificam a participação como **Principal** quando a persistência é indispensável à execução do requisito, **Apoio** quando complementa uma tabela principal, **Indireta** quando participa apenas por encadeamento e **Infraestrutura** quando não implementa funcionalidade de usuário. Estruturas removidas não integram as matrizes do schema vigente; a seção 12 aponta o relatório histórico correspondente.

## 9. Requisitos funcionais considerados na modelagem

| RF | Nome resumido | Descrição |
|---|---|---|
| RF01 | Cadastro de usuário | Cadastro de responsáveis e cuidador. |
| RF02 | Autenticação | Login e logout dos usuários. |
| RF03 | Recuperação de senha | Redefinição de senha por token temporário. |
| RF04 | Gerenciamento de perfil | Consulta e edição dos dados cadastrais. |
| RF05 | Cadastro de cuidador com perfil profissional | Formação, experiência, serviços, modalidades e disponibilidade do cuidador. |
| RF06 | Busca de cuidador | Pesquisa de cuidador por filtros e localização. |
| RF07 | Perfil do cuidador | Visualização detalhada do cuidador. |
| RF08 | Solicitação de serviço | Solicitação de atendimento entre responsável e cuidador. |
| RF09 | Aceite/rejeição de solicitação | Resposta à solicitação de serviço. |
| RF10 | Histórico de contratações | Consulta de contratações e histórico de serviços. |
| RF11 | Encerramento do serviço | Cancelamento ou encerramento da contratação. |
| RF12 | Agenda de cuidados | Visualização dos atendimentos e cuidados agendados. |
| RF13 | Lista de tarefas | Organização das tarefas e cuidados planejados vinculados à rotina e à contratação. |
| RF14 | Lembretes de medicação e tarefas | Lembretes e alertas dos cuidados planejados. |
| RF15 | Registro de atividades | Execução, não realização, auditoria e histórico das atividades de cuidado. |
| RF16 | Diário da pessoa assistida | Registros manuais e cuidados avulsos. |
| RF17 | Busca de serviços pelo cuidador | Busca de serviços publicados e manifestação de interesse. |
| RF18 | Check-in e check-out do serviço com localização | Início e encerramento do atendimento com horário real e localização. |
| RF19 | Relatório de atendimento e anotações de enfermagem | Geração, edição, finalização e envio do relatório. |
| RF20 | Gerenciamento administrativo de usuários | Consulta, detalhamento, bloqueio e desbloqueio de contas por administrador. |
| RF21 | Aprovação e reprovação de perfis | Análise de cuidadores e responsáveis, histórico, restrição de acesso e comunicação por e-mail. |

## 10. Rastreabilidade: requisitos funcionais para tabelas

| Requisito | Nome do requisito | Tabelas principais | Tabelas de apoio | Justificativa |
|---|---|---|---|---|
| RF01 | Cadastro de usuário | `usuario`, `responsavel`, `pessoa_assistida` | `cuidador`, `cuidador_formacao`, `cuidador_modalidade`, `cuidador_servico`, `cuidador_disponibilidade_dia`, `cuidador_disponibilidade_periodo`, `pessoa_assistida_alergia`, `pessoa_assistida_restricao_alimentar`, `pessoa_assistida_contato_emergencia` | Separa identidade, perfis e dados da pessoa que receberá o cuidado. |
| RF02 | Autenticação | `usuario` | — | Consulta identidade, papel, status e hash da senha. |
| RF03 | Recuperação de senha | `usuario_token_redefinicao_senha` | `usuario` | Associa à conta um token com validade e uso único. |
| RF04 | Gerenciamento de perfil | `usuario`, `responsavel`, `cuidador`, `pessoa_assistida` | `cuidador_formacao`, `cuidador_modalidade`, `cuidador_servico`, `cuidador_disponibilidade_dia`, `cuidador_disponibilidade_periodo`, `pessoa_assistida_alergia`, `pessoa_assistida_restricao_alimentar`, `pessoa_assistida_contato_emergencia` | Permite consultar e atualizar dados comuns e específicos. |
| RF05 | Cadastro de cuidador com perfil profissional | `cuidador`, `usuario` | `cuidador_formacao`, `cuidador_modalidade`, `cuidador_servico`, `cuidador_disponibilidade_dia`, `cuidador_disponibilidade_periodo` | Complementa a conta com qualificação, oferta e disponibilidade profissional. |
| RF06 | Busca de cuidador | `cuidador`, `usuario` | `cuidador_formacao`, `cuidador_modalidade`, `cuidador_servico`, `cuidador_disponibilidade_dia`, `cuidador_disponibilidade_periodo` | Fornece identificação, localização, formação, serviços e disponibilidade usados nos filtros. |
| RF07 | Perfil do cuidador | `cuidador`, `usuario` | `cuidador_formacao`, `cuidador_modalidade`, `cuidador_servico`, `cuidador_disponibilidade_dia`, `cuidador_disponibilidade_periodo` | Compõe a visualização detalhada do profissional. |
| RF08 | Solicitação de serviço | `solicitacao_servico`, `solicitacao_servico_agenda_dia`, `solicitacao_servico_data`, `solicitacao_servico_atividade` | `pessoa_assistida`, `rotina_cuidado`, `solicitacao_servico_item_cuidado_copia`, `solicitacao_servico_item_cuidado_copia_dia_semana` | Registra participantes, período, atividades e cópia imutável da rotina solicitada. |
| RF09 | Aceite/rejeição de solicitação | `solicitacao_servico`, `solicitacao_servico_contratacao_historico_status` | `contratacao`, `notificacao`, `notificacao_preferencia` | Persiste a decisão, sua transição histórica, o vínculo aceito e a comunicação. |
| RF10 | Histórico de contratações | `contratacao`, `solicitacao_servico_contratacao_historico_status` | `solicitacao_servico`, `pessoa_assistida`, `usuario` | Reconstrói vínculo, participantes, serviço de origem e mudanças de estado. |
| RF11 | Encerramento do serviço | `contratacao`, `solicitacao_servico_contratacao_historico_status` | `notificacao`, `notificacao_preferencia` | Registra tipo, motivo, solicitante, data efetiva e comunicação do término. |
| RF12 | Agenda de cuidados | `contratacao`, `solicitacao_servico_agenda_dia`, `solicitacao_servico_data` | `registro_atendimento`, `ocorrencia_cuidado` | Combina vigência contratual, agenda prevista e eventos operacionais do dia. |
| RF13 | Lista de tarefas | `rotina_cuidado`, `rotina_cuidado_item`, `tarefa_cuidado` | `rotina_cuidado_item_dia_semana`, `tarefa_cuidado_dia_semana`, `tarefa_cuidado_auditoria`, `solicitacao_servico_item_cuidado_copia`, `solicitacao_servico_item_cuidado_copia_dia_semana` | Transforma o modelo de rotina contratado em séries de tarefas rastreáveis. |
| RF14 | Lembretes de medicação e tarefas | `ocorrencia_cuidado_lembrete`, `ocorrencia_cuidado`, `tarefa_cuidado` | `notificacao`, `notificacao_preferencia` | Calcula disparos por ocorrência e respeita preferências do destinatário. |
| RF15 | Registro de atividades | `ocorrencia_cuidado`, `registro_diario_cuidado`, `tarefa_cuidado_auditoria` | `ocorrencia_cuidado_foto`, `tarefa_cuidado`, `contratacao` | Registra resultado, justificativa, evidência e trilha de auditoria do cuidado. |
| RF16 | Diário da pessoa assistida | `registro_diario_cuidado` | `ocorrencia_cuidado`, `ocorrencia_cuidado_foto`, `contratacao`, `registro_atendimento` | Unifica cuidados planejados e avulsos dentro de atendimento autorizado. |
| RF17 | Busca de serviços pelo cuidador | `solicitacao_servico` | `pessoa_assistida`, `usuario`, `contratacao`, `notificacao`, `notificacao_preferencia` | A mesma tabela representa publicação e candidatura, com apoio de participantes e comunicação. |
| RF18 | Check-in e check-out do serviço com localização | `registro_atendimento` | `contratacao`, `pessoa_assistida`, `usuario`, `notificacao`, `notificacao_preferencia` | Valida vínculo, horário e localização e comunica início ou fim. |
| RF19 | Relatório de atendimento e anotações de enfermagem | `relatorio_atendimento` | `registro_atendimento`, `ocorrencia_cuidado`, `registro_diario_cuidado`, `ocorrencia_cuidado_foto`, `notificacao`, `notificacao_preferencia` | Consolida presença e cuidados, finaliza o texto e controla o envio assíncrono na própria tabela. |
| RF20 | Gerenciamento administrativo de usuários | `usuario` | — | Mantém situação da conta, motivo, datas e administradores responsáveis por bloqueio e desbloqueio. |
| RF21 | Aprovação e reprovação de perfis | `responsavel`, `responsavel_historico_situacao`, `cuidador`, `cuidador_historico_situacao` | `usuario`, `cuidador_formacao`, `cuidador_modalidade`, `cuidador_servico`, `cuidador_disponibilidade_dia`, `cuidador_disponibilidade_periodo` | Persiste as decisões, suas auditorias e os dados analisados; somente perfis aprovados e ativos acessam os fluxos funcionais. |

Não existe tabela separada para envio de e-mail: `status_email`, datas, tentativas, próxima tentativa e mensagem de erro ficam em `relatorio_atendimento`.

## 11. Rastreabilidade: tabelas para requisitos funcionais

| Tabela | Requisitos relacionados | Tipo de participação | Justificativa |
|---|---|---|---|
| `usuario` | RF01, RF02, RF03, RF04, RF05, RF06, RF07, RF10, RF17, RF18, RF20, RF21 | Principal/Apoio | Base de identidade, autenticação e situação da conta; identifica participantes e administradores. |
| `usuario_token_redefinicao_senha` | RF03 | Principal | Persiste o token, sua validade e consumo. |
| `responsavel` | RF01, RF04, RF21 | Principal | Especializa a conta do responsável e mantém sua situação de aprovação. |
| `responsavel_historico_situacao` | RF21 | Principal | Audita cada decisão administrativa sobre o responsável. |
| `cuidador` | RF01, RF04, RF05, RF06, RF07, RF21 | Principal/Apoio | Mantém o perfil profissional, sua situação de aprovação e dados analisados. |
| `cuidador_historico_situacao` | RF21 | Principal | Audita cada decisão administrativa sobre o cuidador. |
| `cuidador_disponibilidade_dia` | RF01, RF04, RF05, RF06, RF07 | Apoio | Detalha os dias disponíveis. |
| `cuidador_disponibilidade_periodo` | RF01, RF04, RF05, RF06, RF07 | Apoio | Detalha os períodos disponíveis. |
| `cuidador_formacao` | RF01, RF04, RF05, RF06, RF07 | Apoio | Mantém múltiplas qualificações. |
| `cuidador_modalidade` | RF01, RF04, RF05, RF06, RF07 | Apoio | Mantém modalidades de atendimento. |
| `cuidador_servico` | RF01, RF04, RF05, RF06, RF07 | Apoio | Mantém serviços oferecidos. |
| `pessoa_assistida` | RF01, RF04, RF08, RF10, RF17, RF18 | Principal/Apoio | Centraliza a pessoa, necessidades e endereço do cuidado. |
| `pessoa_assistida_alergia` | RF01, RF04 | Apoio | Complementa o cadastro clínico. |
| `pessoa_assistida_contato_emergencia` | RF01, RF04 | Apoio | Complementa o cadastro com contato emergencial. |
| `pessoa_assistida_restricao_alimentar` | RF01, RF04 | Apoio | Complementa o cadastro clínico e alimentar. |
| `solicitacao_servico` | RF08, RF09, RF10, RF17 | Principal/Apoio | Unifica pedido direto, publicação, candidatura e origem do contrato. |
| `solicitacao_servico_agenda_dia` | RF08, RF12 | Principal | Define a grade semanal solicitada e agendada. |
| `solicitacao_servico_atividade` | RF08 | Principal | Define as atividades requeridas. |
| `solicitacao_servico_data` | RF08, RF12 | Principal | Define datas pontuais do serviço. |
| `solicitacao_servico_contratacao_historico_status` | RF09, RF10, RF11 | Principal | Registra a linha do tempo de solicitações e contratos. |
| `solicitacao_servico_item_cuidado_copia` | RF08, RF13 | Apoio | Preserva o item de rotina acordado e origina tarefa. |
| `solicitacao_servico_item_cuidado_copia_dia_semana` | RF08, RF13 | Apoio | Preserva os dias do item acordado. |
| `contratacao` | RF09, RF10, RF11, RF12, RF15, RF16, RF17, RF18 | Principal/Apoio | Materializa o vínculo aceito e autoriza operações posteriores. |
| `rotina_cuidado` | RF08, RF13 | Principal/Apoio | Modelo reutilizável selecionado na solicitação. |
| `rotina_cuidado_item` | RF13 | Principal | Define cada cuidado planejado. |
| `rotina_cuidado_item_dia_semana` | RF13 | Apoio | Define recorrência semanal do item. |
| `tarefa_cuidado` | RF13, RF14, RF15 | Principal/Apoio | Série operacional exibida, lembrada e executada. |
| `tarefa_cuidado_dia_semana` | RF13 | Apoio | Materializa a recorrência semanal. |
| `tarefa_cuidado_auditoria` | RF13, RF15 | Principal/Apoio | Preserva mudanças e ações de execução. |
| `ocorrencia_cuidado` | RF12, RF14, RF15, RF16, RF19 | Principal/Apoio | Representa cada execução em data e horário. |
| `ocorrencia_cuidado_foto` | RF15, RF16, RF19 | Apoio | Evidência de cuidados planejados ou avulsos. |
| `ocorrencia_cuidado_lembrete` | RF14 | Principal | Agenda e deduplica os lembretes. |
| `registro_diario_cuidado` | RF15, RF16, RF19 | Principal/Apoio | Linha cronológica usada no diário e relatório. |
| `registro_atendimento` | RF12, RF16, RF18, RF19 | Principal/Apoio | Registra presença e delimita atendimento válido. |
| `relatorio_atendimento` | RF19 | Principal | Armazena relatório, finalização e entrega por e-mail. |
| `notificacao` | RF09, RF11, RF14, RF17, RF18, RF19 | Apoio | Comunica eventos desses requisitos. |
| `notificacao_preferencia` | RF09, RF11, RF14, RF17, RF18, RF19 | Apoio | Controla quais eventos são recebidos. |
| `flyway_schema_history` | Nenhum RF funcional | Infraestrutura | Versiona a evolução técnica do schema. |

## 13. Pontos de atenção

### 13.1 Compatibilidade e legado

- A V040 é condicional: uma instalação limpa e um banco evoluído vazio convergem para o mesmo catálogo sem as estruturas antigas.
- A migração aborta se detectar qualquer dado legado, exigindo tratamento manual em vez de exclusão silenciosa.
- V012 e V013 constam no histórico do banco evoluído, mas não no repositório atual; V038 e V039 permanecem imutáveis e a configuração aceita migrations ausentes.
- Não foram encontrados seeds SQL ativos fora das migrations.

### 13.2 Integridade referencial

- Coleções de alergias, restrições, disponibilidade, modalidades e serviços não têm PK/unique física; o `Set` da aplicação reduz duplicidades, mas inserções externas ainda podem repeti-las.
- `entidade_id` do histórico e `entidade_relacionada_id` da notificação são referências polimórficas sem FK física.
- Os estados administrativos possuem checks físicos; alguns códigos históricos do diário continuam como texto e devem acompanhar o código.

### 13.3 Padronização de nomenclatura

- As annotations `@Table` usam nomes históricos em inglês; a correspondência com o schema depende da `PortuguesePhysicalNamingStrategy`.
- A V041 conclui a tradução das colunas de domínio que ainda continham `snapshot`, usando `copia` sem alterar nomes Java ou contratos JSON.
- A V042 remove somente `cuidador.formacao`; a formação profissional continua normalizada em `cuidador_formacao`.
- As coordenadas de `cuidador` e `pessoa_assistida` são mantidas porque alimentam as buscas por distância de RF06 e RF17.
- Os fusos de tarefas, ocorrências e diário são mantidos porque preservam a data civil, a conversão UTC, os lembretes e a linha do tempo.

### 13.4 Segurança e proteção de dados

- CPF, saúde, endereço, coordenadas, relatórios e fotos são dados pessoais ou sensíveis e exigem autenticação, autorização, retenção adequada e acesso mínimo necessário.
- Fotos e coordenadas devem ser disponibilizadas somente aos participantes autorizados da contratação.

### 13.5 Melhorias futuras

- Avaliar constraints de unicidade nas coleções e checks para status textuais.
- Documentar explicitamente a política de retenção de tokens, fotos, localização, notificações e relatórios.
- Manter as duas matrizes atualizadas quando um RF, enum, tabela ou relacionamento mudar.
