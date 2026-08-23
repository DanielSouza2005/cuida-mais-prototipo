import type { HiringType, RequestedActivity, ServiceRequestStatus } from '@/types/serviceRequest';

export const hiringLabels: Record<HiringType, string> = { PONTUAL: 'Serviço pontual', PERIODO_DETERMINADO: 'Período determinado', PERIODO_INDETERMINADO: 'Período indeterminado' };
export const statusLabels: Record<ServiceRequestStatus, string> = { ABERTA: 'Disponível', PENDENTE: 'Pendente', ACEITA: 'Aceita', REJEITADA: 'Rejeitada', CANCELADA: 'Cancelada', EXPIRADA: 'Expirada' };
export const weekdayLabels: Record<string, string> = { SEGUNDA: 'Segunda-feira', TERCA: 'Terça-feira', QUARTA: 'Quarta-feira', QUINTA: 'Quinta-feira', SEXTA: 'Sexta-feira', SABADO: 'Sábado', DOMINGO: 'Domingo' };
export const activityLabels: Record<RequestedActivity, string> = {
  HIGIENE_PESSOAL: 'Higiene pessoal', BANHO: 'Banho', ALIMENTACAO: 'Alimentação', LOCOMOCAO: 'Locomoção', COMPANHIA: 'Companhia',
  MEDICACAO_ORIENTADA: 'Apoio à rotina de medicação conforme orientação', CONSULTAS: 'Acompanhamento em consultas',
  ATIVIDADES_DOMESTICAS_LEVES: 'Atividades domésticas leves relacionadas ao cuidado', MONITORAMENTO_NOTURNO: 'Monitoramento noturno', OUTRO: 'Outro',
};
export const toOptions = <T extends string>(values: T[], labels: Record<string, string>) => values.map((value) => ({ value, label: labels[value] ?? value }));
