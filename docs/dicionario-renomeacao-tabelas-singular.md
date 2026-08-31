# Dicionário de renomeação de tabelas para o singular

Este dicionário registra a transição física da V042 para a V043. Todos os nomes finais estão em português, sem acentos e em `snake_case`. As tabelas filhas começam pelo nome singular do domínio principal.

| Nome atual | Novo nome | Tipo | Observação |
|---|---|---|---|
| `usuarios` | `usuario` | Principal | Identidade e autenticação. |
| `usuarios_tokens_redefinicao_senha` | `usuario_token_redefinicao_senha` | Filha | Token pertencente a um usuário. |
| `responsaveis` | `responsavel` | Principal | Perfil do responsável. |
| `cuidadores` | `cuidador` | Principal | Perfil do cuidador. |
| `cuidadores_disponibilidade_dias` | `cuidador_disponibilidade_dia` | Filha/coleção | Um dia disponível. |
| `cuidadores_disponibilidade_periodos` | `cuidador_disponibilidade_periodo` | Filha/coleção | Um período disponível. |
| `cuidadores_formacoes` | `cuidador_formacao` | Filha/coleção | Uma formação. |
| `cuidadores_modalidades` | `cuidador_modalidade` | Filha/coleção | Uma modalidade. |
| `cuidadores_servicos` | `cuidador_servico` | Filha/coleção | Um serviço oferecido. |
| `pessoas_assistidas` | `pessoa_assistida` | Principal | Pessoa que recebe cuidado. |
| `pessoas_assistidas_alergias` | `pessoa_assistida_alergia` | Filha/coleção | Uma alergia. |
| `pessoas_assistidas_contatos_emergencia` | `pessoa_assistida_contato_emergencia` | Filha | Um contato emergencial. |
| `pessoas_assistidas_restricoes_alimentares` | `pessoa_assistida_restricao_alimentar` | Filha/coleção | Uma restrição alimentar. |
| `solicitacoes_servico` | `solicitacao_servico` | Principal | Solicitação ou oportunidade. |
| `solicitacoes_servico_agenda_dias` | `solicitacao_servico_agenda_dia` | Filha/coleção | Um dia da agenda. |
| `solicitacoes_servico_atividades` | `solicitacao_servico_atividade` | Filha/coleção | Uma atividade solicitada. |
| `solicitacoes_servico_datas` | `solicitacao_servico_data` | Filha/coleção | Uma data específica. |
| `solicitacoes_servico_itens_cuidado_copias` | `solicitacao_servico_item_cuidado_copia` | Filha/cópia | Uma cópia de item acordado. |
| `solicitacoes_servico_itens_cuidado_copias_dias_semana` | `solicitacao_servico_item_cuidado_copia_dia_semana` | Filha/coleção | Um dia da cópia. |
| `solicitacoes_servico_contratacoes_historico_status` | `solicitacao_servico_contratacao_historico_status` | Histórico | Registra solicitação ou contratação. |
| `contratacoes` | `contratacao` | Principal | Vínculo contratado. |
| `rotinas_cuidado` | `rotina_cuidado` | Principal | Rotina reutilizável. |
| `rotinas_cuidado_itens` | `rotina_cuidado_item` | Filha | Item da rotina. |
| `rotinas_cuidado_itens_dias_semana` | `rotina_cuidado_item_dia_semana` | Filha/coleção | Um dia do item. |
| `tarefas_cuidado` | `tarefa_cuidado` | Principal | Série de tarefa. |
| `tarefas_cuidado_dias_semana` | `tarefa_cuidado_dia_semana` | Filha/coleção | Um dia da tarefa. |
| `tarefas_cuidado_auditoria` | `tarefa_cuidado_auditoria` | Auditoria | Um evento de auditoria. |
| `ocorrencias_cuidado` | `ocorrencia_cuidado` | Principal | Ocorrência agendada. |
| `ocorrencias_cuidado_fotos` | `ocorrencia_cuidado_foto` | Anexo | Uma foto. |
| `ocorrencias_cuidado_lembretes` | `ocorrencia_cuidado_lembrete` | Filha | Um lembrete. |
| `registros_diario_cuidado` | `registro_diario_cuidado` | Principal | Registro da linha do tempo. |
| `registros_atendimento` | `registro_atendimento` | Principal | Registro de presença. |
| `relatorios_atendimento` | `relatorio_atendimento` | Principal | Relatório do atendimento. |
| `notificacoes` | `notificacao` | Principal | Notificação interna. |
| `notificacoes_preferencias` | `notificacao_preferencia` | Filha/preferência | Preferência do usuário. |
| `flyway_schema_history` | `flyway_schema_history` | Infraestrutura | Mantida porque pertence ao Flyway. |

Não há colisão entre os nomes finais e objetos existentes. O maior identificador de tabela permanece abaixo do limite de 63 bytes do PostgreSQL.
