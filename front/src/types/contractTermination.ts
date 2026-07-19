import type { ContractHiringType, ContractHistoryStatus, ContractTerminationType, ContractWeekday } from '@/types/contractsHistory';

export type ParticipantRole = 'RESPONSAVEL' | 'CUIDADOR';
export type TerminationType = ContractTerminationType;

export type ContractTerminationContract = import('@/types/contractsHistory').ContractHistoryItem & {
  participantRole: ParticipantRole;
  caregiver: { id: string; name: string; profilePhotoUrl?: string; locationSummary: string };
  responsible: { id: string; name: string; relationship: string };
  effectiveEndDate?: string;
  terminationRequestedAt?: string;
  terminationRequestedBy?: string;
  terminationType?: TerminationType;
  terminationReason?: string;
  terminationAdditionalNotes?: string;
};

export type ContractTerminationPayload = {
  contractId: string;
  terminationType: TerminationType;
  effectiveEndDate: string;
  reason: string;
  additionalNotes?: string;
};

export type ContractTerminationDraft = ContractTerminationPayload;

export type ContractTerminationActionType = 'TERMINATION' | 'CANCELLATION' | 'NONE';

export type ContractTerminationFormData = {
  contractId: string;
  status: ContractHistoryStatus;
  actionType: ContractTerminationActionType;
  hiringType: ContractHiringType;
  startDate: string;
  endDate?: string;
  participantRole: ParticipantRole;
  assistedPersonName: string;
  otherPartyName: string;
  scheduleDays: { weekday: ContractWeekday; startTime: string; endTime: string }[];
  allowedTerminationTypes: TerminationType[];
};

export type ContractTerminationResponse = {
  contractId: string;
  status: 'ENCERRAMENTO_AGENDADO' | 'ENCERRADA' | 'CANCELADA';
  effectiveEndDate: string;
  terminationRequestedAt?: string;
  terminationRequestedBy: string;
  terminationType: TerminationType;
  reason: string;
  notes?: string;
  requestedByName: string;
  canceledAt?: string;
  updatedAt: string;
};
