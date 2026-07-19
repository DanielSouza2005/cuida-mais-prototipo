import type { AgendaViewMode } from '@/types/agenda';

const monthNames = [
  'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
  'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro',
];

export type LocalDateParts = { year: number; month: number; day: number };

/** Extrai uma data civil sem interpretar YYYY-MM-DD como um instante UTC. */
export function parseDateOnlyToParts(value: string): LocalDateParts | null {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
  if (!match) return null;

  const parts = { year: Number(match[1]), month: Number(match[2]), day: Number(match[3]) };
  const validationDate = new Date(parts.year, parts.month - 1, parts.day, 12);
  if (
    validationDate.getFullYear() !== parts.year
    || validationDate.getMonth() + 1 !== parts.month
    || validationDate.getDate() !== parts.day
  ) return null;
  return parts;
}

function localDateParts(value: string | Date): LocalDateParts | null {
  if (typeof value === 'string') return parseDateOnlyToParts(value);
  if (Number.isNaN(value.getTime())) return null;
  return { year: value.getFullYear(), month: value.getMonth() + 1, day: value.getDate() };
}

export function isSameLocalDate(first: string | Date, second: string | Date) {
  const firstParts = localDateParts(first);
  const secondParts = localDateParts(second);
  return Boolean(
    firstParts
    && secondParts
    && firstParts.year === secondParts.year
    && firstParts.month === secondParts.month
    && firstParts.day === secondParts.day,
  );
}

export function isTodayDate(value: string | Date, now = new Date()) {
  return isSameLocalDate(value, now);
}

export function dateOnlyToLocal(value: string) {
  const parts = parseDateOnlyToParts(value);
  if (!parts) return new Date(Number.NaN);
  return new Date(parts.year, parts.month - 1, parts.day, 12);
}

export function localToDateOnly(value: Date) {
  const year = value.getFullYear();
  const month = String(value.getMonth() + 1).padStart(2, '0');
  const day = String(value.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function todayDateOnly() {
  return localToDateOnly(new Date());
}

export function addDays(value: string, amount: number) {
  const date = dateOnlyToLocal(value);
  date.setDate(date.getDate() + amount);
  return localToDateOnly(date);
}

export function agendaRange(anchor: string, mode: AgendaViewMode) {
  if (mode === 'DAY') return { startDate: anchor, endDate: anchor };
  const date = dateOnlyToLocal(anchor);
  if (mode === 'WEEK') {
    const daysFromMonday = (date.getDay() + 6) % 7;
    date.setDate(date.getDate() - daysFromMonday);
    const startDate = localToDateOnly(date);
    return { startDate, endDate: addDays(startDate, 6) };
  }
  return {
    startDate: localToDateOnly(new Date(date.getFullYear(), date.getMonth(), 1, 12)),
    endDate: localToDateOnly(new Date(date.getFullYear(), date.getMonth() + 1, 0, 12)),
  };
}

export function moveAgendaPeriod(anchor: string, mode: AgendaViewMode, direction: -1 | 1) {
  const date = dateOnlyToLocal(anchor);
  if (mode === 'DAY') date.setDate(date.getDate() + direction);
  if (mode === 'WEEK') date.setDate(date.getDate() + 7 * direction);
  if (mode === 'MONTH') date.setMonth(date.getMonth() + direction, 1);
  return localToDateOnly(date);
}

export function agendaPeriodLabel(anchor: string, mode: AgendaViewMode) {
  const range = agendaRange(anchor, mode);
  if (mode === 'DAY') return formatShortDate(anchor, true);
  if (mode === 'WEEK') return `${formatShortDate(range.startDate)} a ${formatShortDate(range.endDate)}`;
  const date = dateOnlyToLocal(anchor);
  return `${monthNames[date.getMonth()]} de ${date.getFullYear()}`;
}

export function formatShortDate(value: string, includeYear = false) {
  const [year, month, day] = value.split('-');
  return includeYear ? `${day}/${month}/${year}` : `${day}/${month}`;
}

export function agendaDayLabel(value: string) {
  const date = dateOnlyToLocal(value);
  const weekday = new Intl.DateTimeFormat('pt-BR', { weekday: 'long' }).format(date);
  return `${weekday.charAt(0).toUpperCase()}${weekday.slice(1)}, ${formatShortDate(value)}`;
}

export function datesBetween(startDate: string, endDate: string) {
  const values: string[] = [];
  for (let current = startDate; current <= endDate; current = addDays(current, 1)) values.push(current);
  return values;
}
