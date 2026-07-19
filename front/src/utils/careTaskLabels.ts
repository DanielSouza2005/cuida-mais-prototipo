import type { MedicationAdministrationRoute, MedicationUnit, TaskCategory, TaskOccurrenceStatus, TaskPriority, TaskRecurrenceType, TaskSeriesStatus, TaskWeekday } from '@/types/careTasks';

export const taskCategoryLabels: Record<TaskCategory, string> = {
  MEDICACAO: 'Medicação', ALIMENTACAO: 'Alimentação', HIDRATACAO: 'Hidratação', HIGIENE_BANHO: 'Higiene e banho',
  MOBILIDADE: 'Mobilidade', EXERCICIO: 'Exercício', CURATIVO: 'Curativo', SINAIS_VITAIS: 'Sinais vitais',
  CONSULTA_COMPROMISSO: 'Consulta ou compromisso', PERSONALIZADA: 'Personalizada',
};
export const taskPriorityLabels: Record<TaskPriority, string> = { BAIXA: 'Baixa', NORMAL: 'Normal', ALTA: 'Alta' };
export const taskSeriesStatusLabels: Record<TaskSeriesStatus, string> = { ATIVA: 'Ativa', PAUSADA: 'Pausada', CANCELADA: 'Cancelada', FINALIZADA: 'Finalizada' };
export const taskOccurrenceStatusLabels: Record<TaskOccurrenceStatus, string> = { PENDENTE: 'Pendente', CONCLUIDA: 'Concluída', ATRASADA: 'Atrasada', NAO_REALIZADA: 'Não realizada', CANCELADA: 'Cancelada' };
export const taskRecurrenceLabels: Record<TaskRecurrenceType, string> = { UNICA: 'Tarefa única', DIARIA: 'Diária', DIAS_ESPECIFICOS: 'Dias específicos', INTERVALO: 'Intervalo em dias', PERIODO_DETERMINADO: 'Período determinado', SEM_DATA_FINAL: 'Sem data final' };
export const taskWeekdayLabels: Record<TaskWeekday, string> = { SEGUNDA: 'Segunda-feira', TERCA: 'Terça-feira', QUARTA: 'Quarta-feira', QUINTA: 'Quinta-feira', SEXTA: 'Sexta-feira', SABADO: 'Sábado', DOMINGO: 'Domingo' };
export const medicationUnitLabels: Record<MedicationUnit, string> = { MG: 'mg', G: 'g', ML: 'ml', GOTAS: 'gotas', COMPRIMIDO: 'comprimido', CAPSULA: 'cápsula', APLICACAO: 'aplicação', PERSONALIZADA: 'Unidade personalizada' };
export const medicationRouteLabels: Record<MedicationAdministrationRoute, string> = { ORAL: 'Oral', TOPICA: 'Tópica', INALATORIA: 'Inalatória', SUBCUTANEA: 'Subcutânea', OUTRA: 'Outra' };
export function taskCategoryName(category: TaskCategory, custom?: string) { return category === 'PERSONALIZADA' && custom ? custom : taskCategoryLabels[category]; }
export function deviceTimezone() { return Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC'; }
