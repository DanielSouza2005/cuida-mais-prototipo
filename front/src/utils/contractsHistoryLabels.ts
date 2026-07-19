import type { ContractHistoryCategory, ContractHistoryItem, ContractHistoryStatus, ContractHiringType, ContractWeekday } from '@/types/contractsHistory';
import { formatDateBR, formatDateTimeLocal, formatScheduleTime } from '@/utils/dateTime';

export const contractStatusLabels: Record<ContractHistoryStatus, string> = {
  PENDENTE: 'Pendente', ACEITA: 'Aceita', REJEITADA: 'Rejeitada', CANCELADA: 'Cancelada', EXPIRADA: 'Expirada',
  AGENDADA: 'Agendada', ATIVA: 'Ativa', ENCERRAMENTO_AGENDADO: 'Encerramento agendado', ENCERRADA: 'Encerrada', FINALIZADA: 'Encerrada',
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
  return formatDateBR(value);
}

export function formatContractDateTime(value: string) {
  return formatDateTimeLocal(value);
}

export function getContractReason(item: ContractHistoryItem) {
  return item.rejectionReason ?? item.cancellationReason ?? item.closureReason;
}

export function formatContractSchedule(item: ContractHistoryItem) {
  const sorted = [...item.scheduleDays].sort((a, b) => weekdayOrder.indexOf(a.weekday) - weekdayOrder.indexOf(b.weekday));
  if (!sorted.length) return '';
  const days = sorted.map((entry) => contractWeekdayLabels[entry.weekday]);
  const dayText = days.length === 1 ? days[0] : `${days.slice(0, -1).join(', ')} e ${days.at(-1)}`;
  const schedules = new Set(sorted.map((entry) => `${formatScheduleTime(entry.startTime)} às ${formatScheduleTime(entry.endTime)}`));
  return schedules.size === 1 ? `${dayText} · ${[...schedules][0]}` : sorted.map((entry) => `${contractWeekdayLabels[entry.weekday]} · ${formatScheduleTime(entry.startTime)} às ${formatScheduleTime(entry.endTime)}`).join('\n');
}

export function formatCep(value: string) {
  const digits = value.replace(/\D/g, '');
  return digits.length === 8 ? `${digits.slice(0, 5)}-${digits.slice(5)}` : value;
}
