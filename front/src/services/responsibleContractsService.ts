import { apiRequest } from '@/services/api';
import type { ContractHistoryItem, ContractHistoryItemType, ContractsHistoryPage, ContractsHistoryQuery } from '@/types/contractsHistory';

type ApiListItem = {
  id: string; itemType: ContractHistoryItemType; serviceRequestId: string; contractId?: string | null;
  participantName: string; participantPhotoUrl?: string | null; assistedPersonName: string;
  participantRole?: ContractHistoryItem['participantRole']; hasScheduledTermination?: boolean;
  status: ContractHistoryItem['status']; statusGroup: ContractHistoryItem['statusGroup']; hiringType: ContractHistoryItem['hiringType'];
  startDate: string; endDate?: string | null; scheduleSummary?: string | null; updatedAt: string;
  rejectionReason?: string | null; terminationReason?: string | null; cancellationReason?: string | null; closureReason?: string | null;
  terminationType?: ContractHistoryItem['terminationType'] | null; effectiveEndDate?: string | null; terminationRequestedAt?: string | null;
};

type ApiPage = Omit<ContractsHistoryPage, 'content'> & { content: ApiListItem[] };

export type ApiContractDetails = {
  id: string; itemType: ContractHistoryItemType; serviceRequestId: string; contractId?: string | null;
  status: ContractHistoryItem['status']; statusLabel: string;
  caregiver: { id: string; name: string; profilePhotoUrl?: string | null; locationSummary?: string | null };
  responsible: { id: string; name: string };
  assistedPerson: { id: string; name: string; dependencyLevel: string; mobility: string; allergies: string[]; foodRestrictions: string[]; notes?: string | null };
  careAddress: { street?: string | null; number?: string | null; complement?: string | null; neighborhood?: string | null; city?: string | null; state?: string | null; cep?: string | null; referencePoint?: string | null };
  hiringType: ContractHistoryItem['hiringType']; startDate: string; endDate?: string | null; specificDates: string[];
  scheduleDays: ContractHistoryItem['scheduleDays']; activities: string[]; needsDescription: string;
  additionalNotes?: string | null; negotiationNotes?: string | null; rejectionReason?: string | null; cancellationReason?: string | null; closureReason?: string | null;
  terminationType?: ContractHistoryItem['terminationType'] | null; terminationReason?: string | null; terminationNotes?: string | null;
  terminationRequestedByName?: string | null; terminationRequestedAt?: string | null; effectiveEndDate?: string | null;
  cancellationRequestedByName?: string | null; cancellationRequestedAt?: string | null; canceledAt?: string | null;
  createdAt: string; updatedAt: string;
  statusHistory: { id?: string | null; label: string; previousStatus?: string | null; newStatus: string; reason?: string | null; changedByName: string; createdAt: string }[];
};

function listItem(item: ApiListItem): ContractHistoryItem {
  return {
    id: item.id, itemType: item.itemType, status: item.status, statusGroup: item.statusGroup,
    participant: { id: '', name: item.participantName, profilePhotoUrl: item.participantPhotoUrl ?? undefined, locationSummary: '' }, participantRole: item.participantRole, hasScheduledTermination: item.hasScheduledTermination,
    assistedPerson: { id: '', name: item.assistedPersonName, dependencyLevel: '', mobility: '' },
    careAddress: { cep: '', street: '', number: '', neighborhood: '', city: '', state: '' },
    hiringType: item.hiringType, startDate: item.startDate, endDate: item.endDate ?? undefined, specificDates: [], scheduleDays: [], scheduleSummary: item.scheduleSummary ?? undefined,
    activities: [], needsDescription: '', rejectionReason: item.rejectionReason ?? undefined, terminationReason: item.terminationReason ?? undefined, cancellationReason: item.cancellationReason ?? undefined,
    closureReason: item.closureReason ?? undefined, terminationType: item.terminationType ?? undefined, effectiveEndDate: item.effectiveEndDate ?? undefined,
    terminationRequestedAt: item.terminationRequestedAt ?? undefined, createdAt: item.updatedAt, updatedAt: item.updatedAt, statusHistory: [],
  };
}

export function mapContractDetails(item: ApiContractDetails): ContractHistoryItem {
  return {
    id: item.id, itemType: item.itemType, status: item.status,
    statusGroup: item.status === 'PENDENTE' ? 'PENDENTES' : item.status === 'ACEITA' || item.status === 'AGENDADA' ? 'AGENDADAS' : item.status === 'ATIVA' || item.status === 'ENCERRAMENTO_AGENDADO' ? 'ATIVAS' : item.status === 'FINALIZADA' || item.status === 'ENCERRADA' ? 'ENCERRADAS' : item.status === 'REJEITADA' ? 'REJEITADAS' : item.status === 'EXPIRADA' ? 'EXPIRADAS' : 'CANCELADAS',
    participant: { id: item.caregiver.id, name: item.caregiver.name, profilePhotoUrl: item.caregiver.profilePhotoUrl ?? undefined, locationSummary: item.caregiver.locationSummary ?? '' },
    responsible: item.responsible,
    assistedPerson: { id: item.assistedPerson.id, name: item.assistedPerson.name, dependencyLevel: item.assistedPerson.dependencyLevel, mobility: item.assistedPerson.mobility, allergies: item.assistedPerson.allergies.join(', '), foodRestrictions: item.assistedPerson.foodRestrictions.join(', '), notes: item.assistedPerson.notes ?? undefined },
    careAddress: { cep: item.careAddress.cep ?? '', street: item.careAddress.street ?? '', number: item.careAddress.number ?? '', complement: item.careAddress.complement ?? undefined, neighborhood: item.careAddress.neighborhood ?? '', city: item.careAddress.city ?? '', state: item.careAddress.state ?? '', referencePoint: item.careAddress.referencePoint ?? undefined },
    hiringType: item.hiringType, startDate: item.startDate, endDate: item.endDate ?? undefined, specificDates: item.specificDates, scheduleDays: item.scheduleDays,
    activities: item.activities, needsDescription: item.needsDescription, additionalNotes: item.additionalNotes ?? undefined, negotiationNotes: item.negotiationNotes ?? undefined,
    rejectionReason: item.rejectionReason ?? undefined, cancellationReason: item.cancellationReason ?? undefined, closureReason: item.closureReason ?? undefined,
    terminationType: item.terminationType ?? undefined, terminationReason: item.terminationReason ?? undefined, terminationNotes: item.terminationNotes ?? undefined,
    terminationRequestedByName: item.terminationRequestedByName ?? undefined, terminationRequestedAt: item.terminationRequestedAt ?? undefined, effectiveEndDate: item.effectiveEndDate ?? undefined,
    cancellationRequestedByName: item.cancellationRequestedByName ?? undefined, cancellationRequestedAt: item.cancellationRequestedAt ?? undefined, canceledAt: item.canceledAt ?? undefined,
    createdAt: item.createdAt, updatedAt: item.updatedAt,
    statusHistory: item.statusHistory.map((entry, index) => ({ id: entry.id ?? `${item.id}-${index}`, label: entry.label, previousStatus: entry.previousStatus ?? undefined, newStatus: entry.newStatus, reason: entry.reason ?? undefined, changedAt: entry.createdAt, changedBy: entry.changedByName })),
  };
}

export async function getResponsibleContracts(params: ContractsHistoryQuery): Promise<ContractsHistoryPage> {
  return getContracts('/api/responsible/contracts', params);
}

export async function getCaregiverContracts(params: ContractsHistoryQuery): Promise<ContractsHistoryPage> {
  return getContracts('/api/caregiver/contracts', params);
}

async function getContracts(path: string, params: ContractsHistoryQuery): Promise<ContractsHistoryPage> {
  const query = new URLSearchParams({ page: String(params.page), size: String(params.size) });
  if (params.statusGroup) query.set('statusGroup', params.statusGroup);
  if (params.participantName) query.set('participantName', params.participantName);
  if (params.startDateFrom) query.set('startDateFrom', params.startDateFrom);
  if (params.startDateTo) query.set('startDateTo', params.startDateTo);
  const response = await apiRequest<ApiPage>(`${path}?${query.toString()}`);
  return { ...response, content: response.content.map(listItem) };
}

export async function getResponsibleContractDetails(itemType: ContractHistoryItemType, id: string) {
  const response = await apiRequest<ApiContractDetails>(itemType === 'CARE_CONTRACT' ? `/api/contracts/${id}` : `/api/responsible/contracts/${itemType}/${id}`);
  return mapContractDetails(response);
}
