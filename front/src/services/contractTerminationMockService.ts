import { initialContractsTerminationMock } from '@/mocks/contractsTerminationMock';
import type { ContractHistoryCategory } from '@/types/contractsHistory';
import type { ContractTerminationContract, ContractTerminationDraft, ContractTerminationPayload, ContractTerminationResponse, TerminationType } from '@/types/contractTermination';
import { localIsoDate } from '@/utils/contractTerminationLabels';

let contracts = initialContractsTerminationMock.map((contract) => ({ ...contract, statusHistory: [...contract.statusHistory] }));
const drafts = new Map<string, ContractTerminationDraft>();
const delay = (milliseconds = 350) => new Promise((resolve) => setTimeout(resolve, milliseconds));

export async function getContractTerminationDetails(contractId: string) {
  await delay(120);
  return contracts.find((contract) => contract.id === contractId) ?? null;
}

export function getTerminationContracts(category: ContractHistoryCategory = 'TODAS') {
  if (category === 'TODAS') return [...contracts];
  return contracts.filter((contract) => contract.statusGroup === category);
}

export function getTerminationTypesForContract(contract: ContractTerminationContract): TerminationType[] {
  if (contract.status === 'AGENDADA') return ['CANCELAMENTO_ANTES_INICIO'];
  const types: TerminationType[] = ['ANTECIPADO_RESPONSAVEL', 'ANTECIPADO_CUIDADOR', 'ACORDO_ENTRE_PARTES'];
  return contract.hiringType === 'PERIODO_DETERMINADO' && contract.endDate ? ['NA_DATA_PREVISTA', ...types] : types;
}

export function saveContractTerminationDraft(draft: ContractTerminationDraft) { drafts.set(draft.contractId, { ...draft }); }
export function getContractTerminationDraft(contractId: string) { return drafts.get(contractId); }

export async function requestContractTermination(contractId: string, payload: ContractTerminationPayload): Promise<ContractTerminationResponse> {
  await delay(650);
  const index = contracts.findIndex((contract) => contract.id === contractId);
  if (index < 0) throw new Error('Contratação não encontrada.');
  const contract = contracts[index];
  const today = localIsoDate();
  const cancellation = contract.status === 'AGENDADA';
  const status = cancellation ? 'CANCELADA' : payload.effectiveEndDate > today ? 'ENCERRAMENTO_AGENDADO' : 'ENCERRADA';
  const requestedAt = new Date().toISOString();
  const requestedBy = contract.participantRole === 'RESPONSAVEL' ? contract.responsible.name : contract.caregiver.name;
  const eventLabel = cancellation ? 'Contratação cancelada' : status === 'ENCERRADA' ? 'Serviço encerrado' : 'Encerramento agendado';
  contracts[index] = {
    ...contract,
    status,
    statusGroup: status === 'CANCELADA' ? 'CANCELADAS' : status === 'ENCERRADA' ? 'ENCERRADAS' : 'ATIVAS',
    effectiveEndDate: payload.effectiveEndDate,
    terminationRequestedAt: requestedAt,
    terminationRequestedBy: requestedBy,
    terminationType: payload.terminationType,
    terminationReason: payload.reason,
    terminationAdditionalNotes: payload.additionalNotes,
    cancellationReason: cancellation ? payload.reason : contract.cancellationReason,
    closureReason: cancellation ? contract.closureReason : payload.reason,
    updatedAt: requestedAt,
    statusHistory: [
      ...contract.statusHistory,
      ...(!cancellation ? [{ id: `${contractId}-request-${requestedAt}`, label: 'Encerramento solicitado', previousStatus: contract.status, newStatus: contract.status, reason: payload.reason, changedAt: requestedAt, changedBy: requestedBy }] : []),
      { id: `${contractId}-result-${requestedAt}`, label: eventLabel, previousStatus: contract.status, newStatus: status, reason: payload.reason, changedAt: status === 'ENCERRAMENTO_AGENDADO' ? `${payload.effectiveEndDate}T00:00:00` : requestedAt, changedBy: requestedBy },
    ],
  };
  drafts.delete(contractId);
  return { contractId, status, effectiveEndDate: payload.effectiveEndDate, terminationRequestedAt: requestedAt, terminationRequestedBy: requestedBy, requestedByName: requestedBy, terminationType: payload.terminationType, reason: payload.reason, updatedAt: requestedAt };
}
