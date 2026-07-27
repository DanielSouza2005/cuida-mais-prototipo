import type { CareRoutine } from '@/types/careRoutine';

export type HiringType = 'PONTUAL' | 'PERIODO_DETERMINADO' | 'PERIODO_INDETERMINADO';
export type ServiceRequestStatus = 'PENDENTE' | 'ACEITA' | 'REJEITADA' | 'CANCELADA' | 'EXPIRADA';
export type RequestedActivity =
  | 'HIGIENE_PESSOAL' | 'BANHO' | 'ALIMENTACAO' | 'LOCOMOCAO' | 'COMPANHIA'
  | 'MEDICACAO_ORIENTADA' | 'CONSULTAS' | 'ATIVIDADES_DOMESTICAS_LEVES'
  | 'MONITORAMENTO_NOTURNO' | 'OUTRO';

export type RequestedScheduleDay = { weekday: string; startTime: string; endTime: string };
export type ServiceRequestCaregiver = { id: string; name: string; profilePhotoUrl?: string | null; city?: string; neighborhood?: string; state?: string; experienceRange?: string; servicesOffered: RequestedActivity[] };
export type ServiceRequestAssistedPerson = { id: string; name: string; birthDate: string; dependencyLevel: string; mobility: string; summary: string };
export type ServiceRequestAddress = { id: string; assistedPersonId: string; cep?: string; street: string; number: string; complement?: string; neighborhood: string; city: string; state: string; referencePoint?: string };
export type ServiceRequestFormData = { caregiver: ServiceRequestCaregiver; assistedPersons: ServiceRequestAssistedPerson[]; careAddresses: ServiceRequestAddress[]; weekdayOptions: string[]; hiringTypeOptions: HiringType[] };

export type ServiceRequestDraft = {
  caregiver: ServiceRequestCaregiver;
  assistedPerson: ServiceRequestAssistedPerson;
  address: ServiceRequestAddress;
  careRoutine: CareRoutine | null;
  hiringType: HiringType | null;
  specificDates: string[];
  startDate: string;
  endDate: string;
  weekDays: string[];
  startTime: string;
  endTime: string;
  needsDescription: string;
  additionalNotes: string;
  negotiation: string;
};

export type ServiceRequestPayload = {
  caregiverId: string; assistedPersonId: string; careAddressId: string; careRoutineId: string; hiringType: HiringType;
  startDate: string | null; endDate: string | null; specificDates: string[]; scheduleDays: RequestedScheduleDay[];
  needsDescription: string;
  additionalNotes: string | null; negotiationNotes: string | null;
};
export type ServiceRequestResponse = {
  id: string; status: ServiceRequestStatus; hiringType: HiringType; caregiverId: string; caregiverName: string; caregiverProfilePhotoUrl?: string | null;
  assistedPersonId: string; assistedPersonName: string; careAddress: string; startDate?: string | null; endDate?: string | null;
  careRoutine?: CareRoutine | null;
  specificDates: string[]; scheduleDays: RequestedScheduleDay[]; needsDescription: string; activities: RequestedActivity[];
  activityOther?: string | null; additionalNotes?: string | null; negotiationNotes?: string | null; createdAt: string; expiresAt: string; canceledAt?: string | null;
};
