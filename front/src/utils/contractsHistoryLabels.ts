import type { ContractHistoryCategory, ContractHistoryItem, ContractHistoryStatus, ContractHiringType, ContractWeekday } from '@/types/contractsHistory';

export const contractStatusLabels: Record<ContractHistoryStatus, string> = {
  PENDENTE: 'Pendente', ACEITA: 'Aceita', REJEITADA: 'Rejeitada', CANCELADA: 'Cancelada', EXPIRADA: 'Expirada',
  AGENDADA: 'Agendada', ATIVA: 'Ativa', FINALIZADA: 'Encerrada',
};

export const contractCategoryLabels: Record<ContractHistoryCategory, string> = {
  TODAS: 'Todas', PENDENTES: 'Pendentes', AGENDADAS: 'Agendadas', ATIVAS: 'Ativas',
  ENCERRADAS: 'Encerradas', REJEITADAS: 'Rejeitadas', CANCELADAS: 'Canceladas', EXPIRADAS: 'Expiradas',
};

export const contractHiringLabels: Record<ContractHiringType, string> = {
  PONTUAL: 'Serviço pontual', PERIODO_DETERMINADO: 'Período determinado', PERIODO_INDETERMINADO: 'Período indeterminado',
};

export const contractWeekdayLabels: Record<ContractWeekday, string> = {
  SEGUNDA: 'Segunda-feira', TERCA: 'Terça-feira', QUARTA: 'Quarta-feira', QUINTA: 'Quinta-feira',
  SEXTA: 'Sexta-feira', SABADO: 'Sábado', DOMINGO: 'Domingo',
};

const weekdayOrder: ContractWeekday[] = ['SEGUNDA', 'TERCA', 'QUARTA', 'QUINTA', 'SEXTA', 'SABADO', 'DOMINGO'];

export function formatContractDate(value: string) {
  const datePart = value.slice(0, 10);
  const [year, month, day] = datePart.split('-');
  return year && month && day ? `${day}/${month}/${year}` : value;
}

export function formatContractDateTime(value: string) {
  const time = value.includes('T') ? value.split('T')[1]?.slice(0, 5) : '';
  return `${formatContractDate(value)}${time ? ` às ${time}` : ''}`;
}

export function getContractReason(item: ContractHistoryItem) {
  return item.rejectionReason ?? item.cancellationReason ?? item.closureReason;
}

export function formatContractSchedule(item: ContractHistoryItem) {
  const sorted = [...item.scheduleDays].sort((a, b) => weekdayOrder.indexOf(a.weekday) - weekdayOrder.indexOf(b.weekday));
  if (!sorted.length) return '';
  const days = sorted.map((entry) => contractWeekdayLabels[entry.weekday]);
  const dayText = days.length === 1 ? days[0] : `${days.slice(0, -1).join(', ')} e ${days.at(-1)}`;
  const schedules = new Set(sorted.map((entry) => `${entry.startTime.slice(0, 5)} às ${entry.endTime.slice(0, 5)}`));
  return schedules.size === 1 ? `${dayText} · ${[...schedules][0]}` : sorted.map((entry) => `${contractWeekdayLabels[entry.weekday]} · ${entry.startTime.slice(0, 5)} às ${entry.endTime.slice(0, 5)}`).join('\n');
}

export function formatCep(value: string) {
  const digits = value.replace(/\D/g, '');
  return digits.length === 8 ? `${digits.slice(0, 5)}-${digits.slice(5)}` : value;
}
