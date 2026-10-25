# Correções integradas de recuperação, horários noturnos e encerramento

## RF03 — Recuperação de senha

O fluxo ocorre integralmente no aplicativo: solicitação por e-mail, verificação de código numérico de seis dígitos e definição da nova senha. O código é gerado com `SecureRandom`, armazenado somente como BCrypt, expira em 10 minutos, aceita no máximo cinco erros e é invalidado por uso ou reenvio. O reenvio possui intervalo mínimo de 60 segundos e a origem é limitada em janela móvel.

A validação do código em `POST /api/auth/password-reset/verify-code` entrega uma autorização aleatória de cinco minutos, restrita à redefinição. `POST /api/auth/reset-password` consome essa autorização atomicamente sob bloqueio pessimista. Links e deep links deixaram de participar do fluxo; registros antigos foram invalidados pela V051. Respostas de solicitação permanecem genéricas para não enumerar contas.

## RF08, RF12 e RF17 — Intervalos que atravessam a meia-noite

Solicitações diretas e publicações usam a mesma política de intervalo. O dia selecionado é sempre o dia de início; se o horário final for menor, o término ocorre no dia seguinte. Horários iguais são recusados e a duração máxima é 16 horas, impedindo que `20:00–19:00` seja aceito como atendimento implícito de 23 horas.

A regra central é reutilizada na validação, agenda, janelas de presença e associação de cuidados ao atendimento. Assim, `20:00–08:00`, viradas de mês e `31/12–01/01` preservam um único atendimento, e um cuidado às 02:00 pertence à ocorrência iniciada no dia anterior. A agenda retorna `endDateTime` no dia seguinte e a interface sinaliza “dia seguinte”.

## RF18 — Encerramento sem localização

O check-in continua exigindo localização atual válida. No check-out, o aplicativo tenta obter a posição por até 10 segundos; permissão negada, serviço desativado, timeout ou indisponibilidade produzem um status explícito e a chamada de encerramento continua sem coordenadas.

O backend mantém autenticação, vínculo, início prévio, unicidade, janela temporal, horário real e auditoria. A V051 torna latitude, longitude, precisão e instante de captura opcionais somente quando `status_localizacao` não for `OBTIDA`; uma constraint impede combinações incoerentes e coordenadas artificiais.

## RF19, RNF20 e RNF25

Relatórios são gerados mesmo sem localização de encerramento e informam “Não registrada” e o motivo técnico. O encerramento, a persistência do status e a auditoria permanecem na mesma transação. A auditoria registra `ATENDIMENTO_ENCERRADO` e apenas o indicador resumido de localização, sem duplicar coordenadas. O envio do código de recuperação é disparado somente após o commit da solicitação.
