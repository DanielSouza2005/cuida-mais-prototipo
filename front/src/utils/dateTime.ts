const dateOnlyPattern = /^(\d{4})-(\d{2})-(\d{2})$/;

function validDate(value: string | Date) {
  const date = value instanceof Date ? value : new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

function parts(value: string | Date, options: Intl.DateTimeFormatOptions) {
  const date = validDate(value);
  if (!date) return null;
  return Object.fromEntries(
    new Intl.DateTimeFormat('pt-BR', options)
      .formatToParts(date)
      .filter((part) => part.type !== 'literal')
      .map((part) => [part.type, part.value]),
  );
}

/** Formata uma data civil (AAAA-MM-DD) sem criar Date e sem aplicar fuso horário. */
export function formatDateBR(value?: string | null) {
  if (!value) return '';
  const match = dateOnlyPattern.exec(value.slice(0, 10));
  return match ? `${match[3]}/${match[2]}/${match[1]}` : value;
}

/** Formata um instante ISO no fuso horário configurado no dispositivo. */
export function formatDateTimeLocal(value: string | Date) {
  const formatted = parts(value, {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit', hourCycle: 'h23',
  });
  if (!formatted) return typeof value === 'string' ? value : '';
  return `${formatted.day}/${formatted.month}/${formatted.year} às ${formatted.hour}:${formatted.minute}`;
}

/** Formata apenas a data local de um instante, preservando a conversão de fuso. */
export function formatInstantDateLocal(value: string | Date) {
  const formatted = parts(value, { day: '2-digit', month: '2-digit', year: 'numeric' });
  if (!formatted) return typeof value === 'string' ? value : '';
  return `${formatted.day}/${formatted.month}/${formatted.year}`;
}

/** Formata apenas o horário local de um instante. */
export function formatTimeLocal(value: string | Date) {
  const formatted = parts(value, { hour: '2-digit', minute: '2-digit', hourCycle: 'h23' });
  if (!formatted) return typeof value === 'string' ? value : '';
  return `${formatted.hour}:${formatted.minute}`;
}

/** Horário de agenda é um horário civil e nunca deve sofrer conversão de fuso. */
export function formatScheduleTime(value?: string | null) {
  if (!value) return '';
  const [hour, minute] = value.split(':');
  return hour && minute ? `${hour.padStart(2, '0')}:${minute.padStart(2, '0')}` : value;
}
