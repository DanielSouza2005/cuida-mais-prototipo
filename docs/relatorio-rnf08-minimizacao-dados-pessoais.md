# Relatório RNF08 — Minimização de Dados Pessoais

Data da análise: 06/09/2026. Estado analisado: backend Spring Boot, frontend React Native/Expo, migrations Flyway até a V047 e banco de desenvolvimento configurado.

## 1. Resumo executivo

- Foram avaliadas as **39 tabelas de domínio e 452 colunas** do schema `public`, além de entidades, DTOs, serviços, controllers, repositórios, telas, types, armazenamento local, logs e arquivos de upload.
- Pela definição ampla de dado pessoal, as 452 colunas de domínio podem integrar registros relativos a pessoas identificadas ou identificáveis. A prioridade deste relatório recai sobre os campos de conteúdo, identidade, saúde, localização, autenticação e auditoria.
- Foram identificados **quatro campos com remoção provável**, **onze frentes de redução/retenção** e **quatro decisões que exigem validação humana**.
- Há dados pessoais de identidade (`nome`, `e-mail`, `CPF`, telefone e nascimento), dados profissionais, endereço e coordenadas, dados assistenciais e de saúde, fotos, relatórios, histórico operacional, tokens e trilhas administrativas.
- Nenhuma coluna, dado, DTO, tela, validação, arquivo de upload ou contrato JSON foi removido ou modificado nesta análise.
- O banco consultado está na V047. As consultas foram somente leitura e retornaram apenas contagens; nenhum valor pessoal foi impresso.
- O frontend guarda o JWT no `expo-secure-store`, com fallback apenas em memória, e guarda o e-mail lembrado no mesmo armazenamento seguro. Não foi encontrado `AsyncStorage` nem log de payload pessoal completo.
- O frontend declara Expo SDK 54, embora a instrução vigente do repositório determine consulta à documentação 56. Isso é uma divergência de versão, não uma decisão de minimização, e não foi alterada.

### Resultado quantitativo dos candidatos no banco

| Dado | Total da tabela | Registros preenchidos/inativos | Resultado |
|---|---:|---:|---|
| `cuidador.experiencia` | 2 | 1 | Legado com dado a preservar ou migrar antes de remover |
| `pessoa_assistida.cpf` | 2 | 0 | Sem dado no ambiente analisado |
| `registro_atendimento.endereco_registrado` | 12 | 0 | Sempre gravado como `NULL` no código atual |
| `ocorrencia_cuidado_foto.nome_arquivo_original` | 4 | 4 | Metadado armazenado sem leitura funcional |
| `usuario_token_redefinicao_senha` | 12 | 12 inativos | Falta expurgo por retenção |
| `usuario_confirmacao_exclusao` | 1 | 1 inativo | Falta expurgo por retenção |
| `notificacao.removida_em` | — | 63 removidas logicamente | Conteúdo continua retido |
| Arquivos versionados em `api/uploads` | 6 | 3 fotos de perfil e 3 assistenciais | Dados binários pessoais dentro do repositório |

## 2. Método e critérios

Foram cruzados o catálogo do banco, as migrations V001–V047, as 27 entidades JPA, DTOs de entrada e saída, mappers, services, repositories, controllers, regras de autorização, e-mail, notificações, testes, types e telas do frontend. Para cada grupo de colunas, foi verificado se existe escrita, leitura, exibição, filtro, relatório, notificação ou justificativa em RF01–RF23.

As classificações usadas são: **Necessário**, **Necessário, mas deve ser reduzido**, **Opcional**, **Redundante**, **Legado**, **Não usado** e **Dúvida — validação manual**. IDs, FKs, estados, datas técnicas e versões são dados pessoais indiretos quando associados a uma pessoa, mas em geral são necessários para integridade, segurança, auditoria ou histórico.

## 3. Mapeamento do banco por tabela

As colunas abaixo agrupam campos de mesma finalidade para manter a matriz auditável. Dentro de cada grupo, todos os nomes físicos relevantes são explicitados.

| Tabela | Dados pessoais ou potencialmente pessoais | Sensível/alto impacto | Finalidade e uso | RF | Classificação |
|---|---|---:|---|---|---|
| `usuario` | `id`, `nome_completo`, `cpf`, `email`, `telefone`, `data_nascimento`, `url_foto_perfil`, `tipo_usuario`, `situacao_conta`, `senha_hash`, bloqueio/desbloqueio, `ultimo_login_em`, exclusão/anonimização e timestamps | Sim: credencial, CPF e foto | Cadastro, login, recuperação, perfil, administração, aprovação e exclusão | RF01–RF07, RF10, RF17, RF18, RF20–RF23 | Necessário; CPF, telefone e nascimento exigem validação de finalidade; respostas devem ser reduzidas |
| `usuario_token_redefinicao_senha` | `id`, `usuario_id`, `hash_token`, emissão, validade e uso | Sim: autenticação | Recuperação de senha de uso único | RF03, RF22 | Necessário, mas deve ter expurgo de inativos |
| `usuario_confirmacao_exclusao` | `id`, `usuario_id`, `hash_token`, emissão, validade e uso | Sim: autenticação | Reautenticação de cinco minutos | RF22 | Necessário, mas deve ter expurgo de inativos |
| `usuario_exclusao_auditoria` | referência interna, perfil, datas e resultado | Não contém identidade direta, mas é vinculável | Evidência mínima de anonimização | RF22 | Necessário; manter retenção formalizada |
| `responsavel` | vínculo familiar, preferência de contato, situação/aprovação, motivos, administrador e timestamps | Pode revelar relação familiar | Perfil e aprovação do responsável | RF01, RF04, RF21, RF22, RF23 | Necessário; preferência de contato e obrigatoriedade precisam revisão |
| `responsavel_historico_situacao` | perfil, estados, motivo, administrador e data | Potencialmente: justificativas livres | Auditoria de decisões | RF21 | Necessário; acesso administrativo e retenção controlada |
| `cuidador` | formação livre, `experiencia`, faixa de experiência, biografia, endereço completo, coordenadas, disponibilidade, serviços/modalidades livres, aprovação, motivos e timestamps | Endereço/coordenadas e textos livres | Perfil profissional, busca e aprovação | RF01, RF04–RF07, RF21–RF23 | `experiencia` legado; endereço exato deve ser reduzido; demais campos necessários/opcionais |
| `cuidador_historico_situacao` | perfil, estados, motivo, administrador e data | Potencialmente: justificativas livres | Auditoria profissional | RF21 | Necessário; retenção controlada |
| `cuidador_disponibilidade_dia` | cuidador e dia da semana | Não | Busca e agenda profissional | RF01, RF04–RF07 | Necessário |
| `cuidador_disponibilidade_periodo` | cuidador e período | Não | Busca e disponibilidade | RF01, RF04–RF07 | Necessário |
| `cuidador_formacao` | cuidador e formação | Não sensível; dado profissional | Qualificação pesquisável | RF01, RF04–RF07 | Necessário |
| `cuidador_modalidade` | cuidador e modalidade | Não | Modalidade de atendimento | RF01, RF04–RF07 | Necessário |
| `cuidador_servico` | cuidador e serviço oferecido | Não | Oferta e filtro | RF01, RF04–RF07 | Necessário |
| `pessoa_assistida` | nome, CPF, nascimento, dependência, mobilidade, alergias, restrições, medicamentos, observações, endereço completo, coordenadas, responsável e timestamps | Sim: saúde, endereço e terceiro vulnerável | Cadastro, cuidado, solicitação, histórico, busca e atendimento | RF01, RF04, RF08, RF10, RF13–RF19, RF23 | CPF não usado; demais necessários/opcionais com exposição mínima |
| `pessoa_assistida_alergia` | pessoa e categoria de alergia | Sim: saúde | Segurança do cuidado | RF01, RF04, RF08, RF13–RF16, RF19 | Necessário |
| `pessoa_assistida_contato_emergencia` | nome, telefone, vínculo, indicador de responsável e timestamps | Sim: dado de terceiro | Contato de emergência cadastrado | RF01, RF04, RF22 | Dúvida: não é usado fora do próprio perfil/exclusão |
| `pessoa_assistida_restricao_alimentar` | pessoa e restrição | Sim: saúde | Segurança alimentar | RF01, RF04, RF08, RF13–RF16, RF19 | Necessário |
| `solicitacao_servico` | participantes, pessoa, datas, necessidades, atividades, observações, negociação, rejeição/cancelamento, rotina, origem e timestamps | Sim: necessidades assistenciais | Solicitar, publicar, candidatar, decidir e contratar | RF08–RF10, RF12, RF17 | Necessário; detalhe antes do aceite deve ser reduzido |
| `solicitacao_servico_agenda_dia` | solicitação, dia e horários | Sim quando ligado à rotina de uma pessoa | Agenda acordada | RF08, RF12 | Necessário |
| `solicitacao_servico_atividade` | solicitação e atividade | Sim: tipo de cuidado | Escopo do atendimento | RF08 | Necessário |
| `solicitacao_servico_data` | solicitação e datas | Sim no contexto do cuidado | Datas pontuais | RF08, RF12 | Necessário |
| `solicitacao_servico_contratacao_historico_status` | entidade, estados, ator, motivo e data | Motivo livre pode conter dados | Rastreabilidade de decisões e término | RF09–RF11 | Necessário; retenção controlada |
| `solicitacao_servico_item_cuidado_copia` | títulos, descrições, categoria, recorrência, notas, medicamentos, lembretes, criticidade e foto obrigatória | Sim: plano de cuidado e medicação | Congelar o acordo e provisionar tarefas | RF08, RF13–RF15 | Necessário para integridade histórica |
| `solicitacao_servico_item_cuidado_copia_dia_semana` | item e dias | Sim no contexto assistencial | Recorrência congelada | RF08, RF13 | Necessário |
| `contratacao` | participantes, pessoa, vigência, estados, motivos, autores e timestamps | Sim no contexto assistencial | Vínculo, agenda, histórico e encerramento | RF09–RF12, RF15–RF19, RF22–RF23 | Necessário; retenção pós-término deve ser formalizada |
| `rotina_cuidado` | responsável, pessoa, nome, descrição, status e timestamps | Sim: plano assistencial | Modelo de cuidados | RF08, RF13 | Necessário |
| `rotina_cuidado_item` | título, descrição, categoria, recorrência, notas, medicação, lembretes, criticidade e exigência de foto | Sim: saúde e cuidado | Planejamento reutilizável | RF13–RF15 | Necessário |
| `rotina_cuidado_item_dia_semana` | item e dias | Sim no contexto assistencial | Recorrência | RF13 | Necessário |
| `tarefa_cuidado` | pessoa, contrato, criador/executor, conteúdo, medicação, agenda, fuso, alertas, foto, auditoria e timestamps | Sim: saúde e rotina | Tarefas, lembretes e execução | RF13–RF16, RF19 | Necessário |
| `tarefa_cuidado_dia_semana` | tarefa e dias | Sim no contexto assistencial | Recorrência da série | RF13 | Necessário |
| `tarefa_cuidado_auditoria` | tarefa, ocorrência, ator, ação, detalhes e data | Sim: trilha assistencial | Segurança e auditoria | RF13, RF15 | Necessário; detalhes livres e retenção controlados |
| `ocorrencia_cuidado` | pessoa, contrato, cuidador/executor, agenda, fuso, estado, conclusão, justificativa e anotação | Sim: execução do cuidado | Lembrete, registro, diário e relatório | RF12, RF14–RF16, RF19 | Necessário |
| `ocorrencia_cuidado_foto` | pais assistenciais, autor, nomes de arquivo, MIME, tamanho e data | Sim: imagem assistencial | Evidência de execução | RF15, RF16, RF19 | Foto necessária quando configurada; nome original não usado |
| `ocorrencia_cuidado_lembrete` | ocorrência, destinatário, tipo, agenda, envio/cancelamento, status e chave | Sim no contexto de saúde | Agendamento e deduplicação | RF14 | Necessário; retenção após estado terminal deve ser definida |
| `registro_diario_cuidado` | participantes, pessoa, título, notas, descrição, tipo, importância, momento e fuso | Sim: diário assistencial | Linha do tempo do cuidado | RF15, RF16, RF19, RF23 | Necessário; retenção e acesso mínimo |
| `registro_atendimento` | participantes, pessoa, horário, latitude, longitude, precisão, captura, endereço, fuso e janelas | Sim: localização precisa | Check-in/check-out e prova de atendimento | RF12, RF16, RF18, RF19 | Coordenadas necessárias; endereço registrado não usado |
| `relatorio_atendimento` | participantes, pessoa, textos gerado/editado/final, observações, enfermagem, estados de e-mail e timestamps | Sim: relatório e anotação de enfermagem | Geração, finalização e entrega | RF19, RF23 | Necessário; retenção formal e acesso mínimo |
| `notificacao` | destinatário, título, mensagem, alvo, leitura/remoção, deduplicação e data | Pode conter contexto pessoal | Comunicação de eventos | RF09, RF11, RF14, RF17–RF19, RF22 | Necessário, mas remoção lógica não minimiza retenção |
| `notificacao_preferencia` | usuário, tipo, escolha e timestamps | Preferência pessoal | Controle de comunicações | RF09, RF11, RF14, RF17–RF19, RF22 | Necessário |
| `flyway_schema_history` | usuário técnico do banco e metadados de execução | Técnico | Integridade do schema | RNF de integridade | Necessário; fora do domínio funcional |

## 4. Fluxos no backend e frontend

| Grupo de dado | Quem envia | Quem recebe/exibe | Persistência | Obrigatório | Avaliação |
|---|---|---|---|---:|---|
| Identidade do usuário | Cadastro e edição do próprio titular | Próprio titular; administrador; sessão recebe DTO completo | `usuario` | Nome, CPF, e-mail, telefone e nascimento no cadastro | CPF/nascimento/telefone não possuem regra funcional suficiente no código para justificar obrigatoriedade; validar |
| Senha/JWT/tokens | Usuário e backend | Backend; JWT fica no cliente | Hashes no banco; JWT e e-mail lembrado no SecureStore | Sim para autenticação | Necessário; não retornar hashes; expurgar tokens inativos |
| Perfil profissional | Cuidador | Busca/detalhe autenticado e administração | `cuidador` e coleções | Parte obrigatória, biografia/foto opcionais | Formação, serviços, modalidades, disponibilidade e faixa são justificáveis; `experiencia` não |
| Endereço do cuidador | Cuidador | Próprio perfil; busca recebe bairro/cidade/UF/distância | Endereço completo e coordenadas | Formulário exige endereço | Rua, número, CEP, complemento e referência não são usados após geocodificação; reduzir persistência |
| Pessoa assistida | Responsável | Responsável; cuidador convidado/contratado; admin não recebe | `pessoa_assistida` e coleções | Nome, nascimento e categorias assistenciais obrigatórios | CPF não usado; dados de saúde necessários, porém só no contexto de serviço autorizado |
| Contato de emergência | Responsável ou terceiro | Apenas próprio perfil | Tabela própria | Obrigatório no cadastro | Não há fluxo de emergência que leia o contato; finalidade precisa ser confirmada antes de manter |
| Solicitação direta | Responsável | Cuidador convidado | Solicitação e cópias | Dados operacionais obrigatórios | Hoje expõe nome, idade, endereço completo e saúde antes do aceite; avaliar visão progressiva |
| Oportunidade aberta | Responsável | Cuidadores autenticados | Solicitação/publicação | Sim | Implementa boa redução: alias, bairro/cidade/UF e categorias sem endereço completo, telefone, e-mail ou CPF |
| Atendimento | Dispositivo do cuidador | Cuidador e responsável do contrato | Coordenadas, precisão, captura, fuso e janelas | Localização obrigatória | Justificado por RF18; restringido aos participantes; endereço textual nunca usado |
| Fotos assistenciais | Cuidador | Participantes autorizados | Banco + sistema de arquivos | Só quando a tarefa exige ou o usuário anexa | Autorização está correta; nome original e arquivos no repositório devem ser eliminados/reduzidos |
| Diário/relatório | Cuidador e backend | Participantes autorizados | Tabelas assistenciais | Conforme fluxo | Necessário; definir retenção, evitar texto excessivo e manter `no-store` em imagens |
| Administração | Backend | Administrador | Dados já existentes + históricos | — | Listas são reduzidas; detalhes retornam alguns campos não usados pela UI e CPF completo |

## 5. Dados candidatos à remoção ou redução

### 5.1 Remoção provável

| Dado | Local | Classificação | Motivo | Dados existentes | Risco de remover | Recomendação |
|---|---|---|---|---:|---|---|
| `cuidador.experiencia` | Banco/backend/frontend type de perfil | Legado/não usado | Apenas é lido no perfil próprio; não é escrito no cadastro/edição nem usado em busca, detalhe ou aprovação. A fonte ativa é `tempo_experiencia`, com `biografia` para texto livre | 1 de 2 | Médio: o registro preenchido pode conter conteúdo distinto | Examinar/migrar semanticamente o único valor sem o expor; remover só depois |
| `pessoa_assistida.cpf` | Banco, entidade, requests, perfil e formulário | Não usado | É coletado, editado e devolvido apenas ao responsável; não identifica contrato, busca, cuidado, relatório ou segurança | 0 de 2 | Baixo neste ambiente; maior em outros ambientes | Remover após checar contagens nos demais ambientes |
| `registro_atendimento.endereco_registrado` | Banco, entidade e DTO | Não usado | O serviço define explicitamente `NULL`; nenhuma tela o usa | 0 de 12 | Baixo | Remover coluna, propriedade e campo de resposta |
| `ocorrencia_cuidado_foto.nome_arquivo_original` | Banco, entidade e storage | Não usado/redundante | É gravado, mas nunca consultado, exibido ou usado para download; o nome interno UUID é suficiente | 4 de 4 | Baixo | Remover e deixar de capturar o nome original |

### 5.2 Redução recomendada

| Dado/local | Problema | Ajuste recomendado |
|---|---|---|
| `AuthResponse`/`UserResponse` no login e cadastro | A sessão recebe CPF, telefone e nascimento embora o contexto de autenticação use principalmente ID, nome, e-mail, foto e papel | Criar resposta mínima de sessão; manter dados completos somente em `/api/profile/me` ou endpoint próprio autorizado |
| `CaregiverDetailsResponse.dataCadastro` e `status` | O frontend declara, mas não exibe nem usa os campos; a busca já filtra conta/perfil aprovados | Retirar do DTO de detalhe e type correspondente |
| DTOs administrativos de detalhe | `UserDetails.birthDate`, `ResponsibleDetails.phone`, datas internas e outros campos são retornados sem uso em algumas telas | Ajustar cada DTO ao que a respectiva tela realmente exibe; evitar DTO de detalhe universal |
| CPF na administração | CPF completo formatado é exibido em detalhe, embora a finalidade de RF20/RF21 não esteja documentada por ação | Mascarar por padrão e liberar valor integral somente se houver finalidade administrativa aprovada |
| Endereço do cuidador | Armazena CEP, rua, número, complemento e ponto de referência, mas busca/detalhe usam somente bairro, cidade, UF e coordenadas | Após geocodificar, persistir somente granularidade necessária; confirmar se endereço exato tem alguma finalidade operacional externa |
| Solicitação direta antes do aceite | Cuidador recebe nome, idade, endereço completo, alergias, restrições, medicamentos e observações antes de existir contrato | Expor inicialmente alias/faixa etária, bairro/cidade, distância e necessidades estritamente necessárias; liberar endereço e identidade após aceite/vínculo |
| `alergiasOutro` + `alergiasDetalhes` e equivalentes de restrição no cadastro | Quando a opção é `OUTRO`, o frontend inicial envia o mesmo texto para os dois campos | Separar “qual alergia/restrição” de “detalhes clínicos” ou manter apenas um campo quando não houver distinção real |
| Tokens de recuperação e confirmação de exclusão | Todos os registros encontrados estão usados ou expirados, mas continuam no banco | Criar política e rotina de expurgo de hashes inativos após janela curta de segurança/auditoria |
| Notificações removidas | 63 notificações marcadas como removidas continuam com título, mensagem e vínculo | Definir prazo e apagar fisicamente após a janela aprovada; preservar somente métricas anônimas, se necessárias |
| Fotos em `api/uploads` | Seis imagens pessoais/assistenciais estão versionadas; `.gitignore` não exclui uploads | Retirar os binários aprovados do repositório e ignorar diretórios de upload; usar storage externo/privado conforme o tipo |
| Ciclo de vida de arquivos | Trocar/excluir foto de perfil apenas limpa a URL em alguns fluxos; fotos assistenciais não têm expurgo físico geral; há diferença entre 4 metadados de foto assistencial no banco e 3 arquivos versionados observados | Implementar reconciliação sem imprimir nomes, remoção pós-transação e política de retenção; tratar ausências/órfãos |

### 5.3 Manter com justificativa

| Dado | Finalidade | RF/RNF relacionado |
|---|---|---|
| E-mail | Login, recuperação, aprovação, bloqueio, exclusão e relatório | RF02, RF03, RF19–RF22 |
| Hash de senha, hashes de token, situação da conta e último login | Autenticação, revogação lógica e segurança administrativa | RF02, RF03, RF20–RF22; segurança |
| Nome do usuário/cuidador | Identificação em perfil, contratação, agenda e administração | RF01, RF04–RF12, RF20–RF21 |
| Formação, faixa de experiência, serviços, modalidades e disponibilidade | Perfil profissional, filtro e decisão de contratação | RF05–RF07, RF21 |
| Bairro/cidade/UF e coordenadas do cuidador | Busca e ordenação por proximidade | RF06, RF07 |
| Nome e nascimento da pessoa assistida | Identificação pelo responsável e cálculo de idade para o cuidador autorizado | RF01, RF04, RF08, RF10, RF16, RF19 |
| Dependência, mobilidade, alergias, restrições, medicamentos e observações | Segurança e organização do cuidado | RF08, RF13–RF16, RF19 |
| Endereço e coordenadas do local de cuidado | Solicitação, deslocamento, busca de oportunidades e validação do atendimento | RF08, RF12, RF17, RF18 |
| Datas, horários, fusos e recorrência | Agenda, conversão temporal correta e lembretes | RF12–RF16, RF18–RF19 |
| Cópias de rotina e históricos de status | Preservar o que foi contratado e as decisões | RF08–RF13; integridade/auditoria |
| Coordenadas e precisão do check-in/check-out | Evidência de presença e visualização pelos participantes | RF18 |
| Fotos exigidas, diário e relatório | Evidência e registro assistencial | RF15, RF16, RF19 |
| Identificadores, FKs, versão otimista e timestamps | Integridade, autorização, concorrência e rastreabilidade | Todos os RFs aplicáveis; segurança/integridade |

### 5.4 Dúvida — decisão humana obrigatória

| Dado | Por que há dúvida | O que validar |
|---|---|---|
| CPF do usuário | É obrigatório e único, mas o código só usa unicidade, perfil e consulta administrativa; não há validação documental ou regra funcional material | Fundamento de identificação, fraude ou obrigação jurídica; se inexistente, remover ou tornar opcional |
| Data de nascimento do usuário | Obrigatória, porém não existe regra de maioridade, elegibilidade ou personalização baseada nela | Se há requisito de idade; caso contrário, remover ou coletar apenas confirmação de faixa etária |
| Telefone e preferência de contato | Telefone é obrigatório, mas não é usado em notificações nem entregue no fluxo de contato; o cuidador recebe a preferência sem o contato correspondente | Canal de contato pós-aceite, consentimento e necessidade; tornar telefone opcional se e-mail bastar |
| Contato de emergência de terceiro | Coletado obrigatoriamente, mas nenhuma emergência, solicitação, contrato ou relatório consulta o dado | Se existirá uso real e autorizado; caso contrário, remover a tabela/campos e a coleta |

## 6. Exposição e controles encontrados

### Controles adequados

- Senhas e tokens persistem somente como hash; DTOs sensíveis sobrescrevem `toString()` para não imprimir conteúdo.
- JWT e e-mail lembrado usam SecureStore; o fallback do JWT é apenas memória.
- Logs de frontend resumem endpoint, status e estrutura, sem imprimir corpo, token ou coordenadas.
- Oportunidades abertas não expõem nome real, CPF, e-mail, telefone nem endereço completo.
- Fotos assistenciais exigem que o solicitante seja responsável ou cuidador da ocorrência/diário e são devolvidas com `Cache-Control: no-store`.
- Atendimento e relatório verificam participação no contrato.
- Exclusão de conta anonimiza identidade elegível e preserva históricos compartilhados com referência reduzida.

### Pontos de atenção

- `/api/profile-photos/**` permite GET sem autenticação. A foto é voluntariamente pública no perfil profissional, mas o contrato de exposição precisa ser explícito; URLs antigas também podem continuar válidas se o arquivo não for apagado.
- O endpoint de cuidador é autenticado, mas a foto ligada a ele é pública por URL. Considerar autenticação ou URL temporária se a finalidade não for publicidade irrestrita.
- A solicitação direta libera endereço e dados assistenciais antes do aceite; o princípio de acesso progressivo recomenda reduzir esse conjunto.
- Conteúdos livres (`motivo`, `observações`, notas, diário e relatório) podem receber dados excessivos. A UI deve orientar o usuário a registrar apenas informação pertinente ao cuidado.
- Não há política técnica de retenção observável para históricos, localização, fotos, relatórios, tokens inativos e notificações removidas.

## 7. Obrigatoriedade dos formulários

| Campo | Situação atual | Avaliação |
|---|---|---|
| Foto e biografia do cuidador | Opcionais | Adequado |
| Complemento e ponto de referência | Opcionais | Adequado, mas endereço exato do cuidador pode não precisar ser persistido |
| CPF da pessoa assistida | Opcional | Ainda assim não possui uso e é candidato à remoção |
| Medicamentos e observações | Opcionais | Adequado; exibir apenas a participantes autorizados |
| Alergias e restrições | Resposta obrigatória, incluindo opção “não possui/não sabe” | Justificado por segurança do cuidado |
| Nome, CPF, telefone e nascimento do usuário | Obrigatórios | Nome/e-mail justificados; CPF, telefone e nascimento precisam validação de finalidade |
| Contato de emergência | Obrigatório | Sem consumidor funcional atual; validação manual necessária |
| Formação, faixa de experiência, serviço, modalidade, disponibilidade e endereço do cuidador | Obrigatórios no fluxo profissional | Justificados para RF05–RF07, com redução possível do endereço exato |

## 8. Plano de remoção proposto — não executado

Somente após autorização explícita e apenas para os itens aprovados:

1. Criar uma nova migration posterior à V047, sem editar migrations antigas, com guardas e cópia/migração quando necessária.
2. Para `cuidador.experiencia`, interromper a migration se houver conteúdo não tratado; o ambiente analisado possui um valor preenchido.
3. Remover apenas as colunas aprovadas e ajustar entidades, DTOs, services, types e telas correspondentes.
4. Separar DTO mínimo de sessão de DTO do próprio perfil e ajustar `AuthContext`/`authService`.
5. Implantar expurgo de tokens/notificações conforme prazos aprovados, sem apagar históricos compartilhados.
6. Corrigir ciclo de vida dos arquivos e remover do versionamento somente os binários expressamente aprovados, sem imprimir nomes ou conteúdo.
7. Atualizar `docs/modelagem-banco-dados.md` com o resultado efetivamente autorizado.
8. Validar Flyway em banco limpo e evoluído, Hibernate `validate`, testes backend, TypeScript, lint e regressão de RF01–RF23.

### Arquivos/áreas previstos por candidato

| Candidato | Áreas que seriam alteradas |
|---|---|
| `cuidador.experiencia` | migration nova, `CaregiverProfile`, `ProfileService`, types de perfil e modelagem |
| CPF da pessoa assistida | migration, `AssistedPerson`, requests/mappers de cadastro e perfil, `signup.tsx`, tela/type/service da pessoa assistida e documentação |
| Endereço registrado do atendimento | migration, `ServiceAttendanceRecord`, `AttendanceRecordResponse`, `ServiceAttendanceService`, type do frontend e documentação |
| Nome original de foto | migration, `CareOccurrencePhoto`, storage/service de fotos e documentação |
| Respostas excessivas | DTOs/mappers/services de autenticação, cuidador e administração; types/contexto/telas do frontend |
| Retenção | repositories/services e agendamentos de tokens/notificações/fotos, configurações e documentação |
| Uploads versionados | `.gitignore`, arquivos expressamente autorizados e configuração/documentação de armazenamento |

## 9. Riscos e limitações

- As contagens refletem somente o banco configurado neste ambiente. Produção, homologação e backups devem ser auditados separadamente antes de qualquer `DROP`, anonimização ou expurgo.
- Consumidores externos não versionados no repositório não puderam ser observados.
- Não existe política jurídica/organizacional de retenção versionada suficiente para definir prazos de apagamento. O relatório recomenda os pontos, mas não inventa prazos.
- A remoção de dado vazio neste ambiente ainda pode quebrar outro ambiente ou integração; migrations devem validar presença e conteúdo.
- Arquivos já presentes no histórico de versionamento exigem decisão específica sobre remoção do histórico remoto; remover apenas da árvore atual não elimina cópias históricas.

## 10. Conclusão

O RNF08 está parcialmente atendido: existem boas restrições de acesso, hashing, anonimização de conta, respostas reduzidas em oportunidades abertas e armazenamento seguro no cliente. Entretanto, há campos sem uso, coleta obrigatória sem finalidade técnica demonstrada, excesso em alguns DTOs, retenção indefinida e arquivos pessoais versionados.

**Análise concluída. Estes são os dados candidatos à remoção/redução. Aguardo autorização explícita para aplicar qualquer alteração.**
