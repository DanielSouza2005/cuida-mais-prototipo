export type ContractHistoryItemType = 'SERVICE_REQUEST' | 'CARE_CONTRACT';
export type ContractHistoryStatus = 'PENDENTE' | 'ACEITA' | 'REJEITADA' | 'CANCELADA' | 'EXPIRADA' | 'AGENDADA' | 'ATIVA' | 'ENCERRAMENTO_AGENDADO' | 'ENCERRADA' | 'FINALIZADA';
export type ContractHistoryStatusGroup = 'PENDENTES' | 'AGENDADAS' | 'ATIVAS' | 'ENCERRADAS' | 'REJEITADAS' | 'CANCELADAS' | 'EXPIRADAS';
export type ContractHistoryCategory = 'TODAS' | ContractHistoryStatusGroup;
export type ContractHiringType = 'PONTUAL' | 'PERIODO_DETERMINADO' | 'PERIODO_INDETERMINADO';
export type ContractWeekday = 'SEGUNDA' | 'TERCA' | 'QUARTA' | 'QUINTA' | 'SEXTA' | 'SABADO' | 'DOMINGO';
export type ContractTerminationType = 'NA_DATA_PREVISTA' | 'ANTECIPADO_RESPONSAVEL' | 'ANTECIPADO_CUIDADOR' | 'ACORDO_ENTRE_PARTES' | 'CANCELAMENTO_ANTES_INICIO' | 'AUTOMATICO_TERMINO_PERIODO';

export type ContractStatusHistoryEntry = {
  id: string;
  label: string;
  previousStatus?: string;
  newStatus: string;
  reason?: string;
  changedAt: string;
  changedBy: string;
};

export type ContractHistoryItem = {
  id: string;
  itemType: ContractHistoryItemType;
  status: ContractHistoryStatus;
  statusGroup: ContractHistoryStatusGroup;
  participant: { id: string; name: string; profilePhotoUrl?: string; locationSummary: string };
  participantRole?: 'RESPONSAVEL' | 'CUIDADOR';
  hasScheduledTermination?: boolean;
  responsible?: { id: string; name: string };
  assistedPerson: {
    id: string;
    name: string;
    dependencyLevel: string;
    mobility: string;
    allergies?: string;
    foodRestrictions?: string;
    notes?: string;
  };
  careAddress: {
    cep: string;
    street: string;
    number: string;
    complement?: string;
    neighborhood: string;
    city: string;
    state: string;
    referencePoint?: string;
  };
  hiringType: ContractHiringType;
  startDate: string;
  endDate?: string;
  specificDates: string[];
  scheduleDays: { weekday: ContractWeekday; startTime: string; endTime: string }[];
  scheduleSummary?: string;
  activities: string[];
  needsDescription: string;
  additionalNotes?: string;
  negotiationNotes?: string;
  rejectionReason?: string;
  cancellationReason?: string;
  closureReason?: string;
  terminationType?: ContractTerminationType;
  terminationReason?: string;
  terminationNotes?: string;
  terminationRequestedByName?: string;
  terminationRequestedAt?: string;
  effectiveEndDate?: string;
  cancellationRequestedByName?: string;
  cancellationRequestedAt?: string;
  canceledAt?: string;
  createdAt: string;
  updatedAt: string;
  statusHistory: ContractStatusHistoryEntry[];
};

export type ContractsHistoryQuery = {
  statusGroup?: ContractHistoryStatusGroup;
  participantName?: string;
  startDateFrom?: string;
  startDateTo?: string;
  page: number;
  size: number;
};

export type ContractsHistoryPage = {
  content: ContractHistoryItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
};
