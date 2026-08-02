export type TaskCategory = 'MEDICACAO' | 'ALIMENTACAO' | 'HIDRATACAO' | 'HIGIENE_BANHO' | 'MOBILIDADE' | 'EXERCICIO' | 'CURATIVO' | 'SINAIS_VITAIS' | 'CONSULTA_COMPROMISSO' | 'PERSONALIZADA';
export type TaskPriority = 'BAIXA' | 'NORMAL' | 'ALTA';
export type TaskRecurrenceType = 'UNICA' | 'DIARIA' | 'DIAS_ESPECIFICOS' | 'INTERVALO' | 'PERIODO_DETERMINADO' | 'SEM_DATA_FINAL';
export type TaskSeriesStatus = 'ATIVA' | 'PAUSADA' | 'CANCELADA' | 'FINALIZADA';
export type TaskOccurrenceStatus = 'PENDENTE' | 'CONCLUIDA' | 'ATRASADA' | 'NAO_REALIZADA' | 'CANCELADA';
export type CareRecordSourceType = 'PLANNED' | 'MANUAL';
export type ManualCareType = 'MEDICACAO' | 'ALIMENTACAO' | 'HIGIENE' | 'MOBILIDADE' | 'COMPANHIA' | 'OBSERVACAO' | 'OCORRENCIA' | 'OUTRO';
export type TaskEditScope = 'SOMENTE_ESTA_OCORRENCIA' | 'ESTA_E_FUTURAS' | 'TODA_A_SERIE';
export type TaskWeekday = 'SEGUNDA' | 'TERCA' | 'QUARTA' | 'QUINTA' | 'SEXTA' | 'SABADO' | 'DOMINGO';
export type MedicationUnit = 'MG' | 'G' | 'ML' | 'GOTAS' | 'COMPRIMIDO' | 'CAPSULA' | 'APLICACAO' | 'PERSONALIZADA';
export type MedicationAdministrationRoute = 'ORAL' | 'TOPICA' | 'INALATORIA' | 'SUBCUTANEA' | 'OUTRA';

export type Medication = {
  name: string; dosage: string; unit: MedicationUnit; customUnit?: string;
  administrationRoute: MedicationAdministrationRoute; customAdministrationRoute?: string; additionalInstructions?: string;
};

export type CareTask = {
  id: string; title: string; description?: string; category: TaskCategory; customCategory?: string;
  priority: TaskPriority; recurrenceType: TaskRecurrenceType; startDate: string; endDate?: string;
  scheduledTime: string; intervalDays?: number; weekdays: TaskWeekday[]; timezone: string;
  reminderEnabled: boolean; reminderMinutesBefore?: number; notes?: string; status: TaskSeriesStatus;
  assistedPersonId: string; assistedPersonName: string; contractId: string; caregiverId: string; caregiverName: string;
  nextOccurrenceDate?: string; nextOccurrenceTime?: string; version: number; createdAt: string; updatedAt: string;
};

export type TaskOccurrence = {
  id: string; taskId: string; title: string; description?: string; category: TaskCategory; customCategory?: string;
  priority: TaskPriority; scheduledDate: string; scheduledTime: string; scheduledInstantUtc: string; timezone: string;
  status: TaskOccurrenceStatus; assistedPersonId: string; assistedPersonName: string; contractId: string;
  caregiverId: string; caregiverName: string; responsibleId: string; responsibleName: string; medication?: Medication;
  taskNotes?: string; important: boolean; reminderEnabled: boolean; reminderMinutesBefore?: number;
  reminderAtScheduledTime: boolean; overdueReminderEnabled: boolean; overdueAfterMinutes?: number;
  repeatWhilePending: boolean; repeatIntervalMinutes?: number; hiringTypeLabel: string;
  contractStartDate: string; contractEndDate?: string; careAddress?: string; dependencyLabel: string; mobilityLabel: string;
  completedAt?: string; executedByName?: string; nonCompletionReason?: string; executionNote?: string;
  activityRecordId?: string; exception: boolean; requiresCompletionPhoto: boolean; autoMarkedNotDone: boolean;
  photos: { id: string; url: string; contentType: string; fileSize: number; createdAt: string }[];
  taskVersion: number; version: number;
};

export type CareCompletionPhoto = { uri: string; name: string; type: 'image/jpeg' | 'image/png' | 'image/webp'; file?: Blob };

export type TaskAudit = { id: string; action: string; actorName: string; details?: string; createdAt: string };
export type CareTaskDetails = { task: CareTask; medication?: Medication; audit: TaskAudit[] };
export type CareTaskPage = { content: CareTask[]; page: number; size: number; totalElements: number; totalPages: number; last: boolean };
export type TaskOccurrencePage = { content: TaskOccurrence[]; page: number; size: number; totalElements: number; totalPages: number; last: boolean };
export type CareDiaryItem = {
  id: string; sourceType: CareRecordSourceType; sourceLabel: string; occurrenceId?: string; manualEntryId?: string;
  contractId: string; date: string; time: string; occurredAt: string; registeredAt?: string; careType: string;
  careTypeLabel: string; title: string; description?: string; notes?: string; status: TaskOccurrenceStatus | 'REALIZADO';
  statusLabel: string; assistedPersonId: string; assistedPersonName: string; caregiverName: string; important: boolean;
  photos: { id: string; url: string; contentType: string; fileSize: number; createdAt: string }[];
};
export type CareDiaryResponse = { content: CareDiaryItem[] };
export type ManualCareContractOption = { contractId: string; contractLabel: string; assistedPersonId: string; assistedPersonName: string };
export type ManualCareFormData = { date: string; contracts: ManualCareContractOption[] };
export type ManualCarePayload = {
  contractId: string; assistedPersonId: string; entryDate: string; occurredTime: string; careType: ManualCareType;
  title: string; description: string; notes?: string; timezone: string; important: boolean; photos: CareCompletionPhoto[];
};
export type TaskContractOption = {
  contractId: string; status: 'ATIVA' | 'ENCERRAMENTO_AGENDADO'; startDate: string; endDate?: string; effectiveEndDate?: string;
  assistedPersonId: string; assistedPersonName: string; caregiverId: string; caregiverName: string;
};
export type TaskFormData = { contracts: TaskContractOption[] };

export type CareTaskPayload = {
  title: string; description?: string | null; category: TaskCategory; customCategory?: string | null; priority: TaskPriority;
  recurrenceType: TaskRecurrenceType; startDate: string; endDate?: string | null; scheduledTime: string; intervalDays?: number | null;
  weekdays: TaskWeekday[]; timezone: string; reminderEnabled: boolean; reminderMinutesBefore?: number | null; notes?: string | null;
  assistedPersonId: string; contractId: string; caregiverId: string; medication?: Medication | null;
};
