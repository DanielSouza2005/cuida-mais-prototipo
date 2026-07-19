import type { ParticipantRole, TerminationType } from '@/types/contractTermination';

export const terminationTypeLabels: Record<TerminationType, string> = {
  NA_DATA_PREVISTA: 'Encerramento na data prevista',
  ANTECIPADO_RESPONSAVEL: 'Encerramento antecipado pelo responsável',
  ANTECIPADO_CUIDADOR: 'Encerramento antecipado pelo cuidador',
  ACORDO_ENTRE_PARTES: 'Encerramento por acordo entre as partes',
  CANCELAMENTO_ANTES_INICIO: 'Cancelamento antes do início',
  AUTOMATICO_TERMINO_PERIODO: 'Encerramento automático ao término do período contratado',
};

export const participantRoleLabels: Record<ParticipantRole, string> = {
  RESPONSAVEL: 'Responsável',
  CUIDADOR: 'Cuidador',
};

export function localIsoDate(date = new Date()) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function displayDateToIso(value: string) {
  const [day, month, year] = value.split('/');
  return day && month && year ? `${year}-${month}-${day}` : '';
}

export function isoDateToDisplay(value: string) {
  const [year, month, day] = value.slice(0, 10).split('-');
  return year && month && day ? `${day}/${month}/${year}` : value;
}
