import { apiRequest } from '@/services/api';
import type { ContractTerminationDraft, ContractTerminationFormData, ContractTerminationResponse } from '@/types/contractTermination';

type SavedFlow = { form: ContractTerminationFormData; draft: ContractTerminationDraft };
const flows = new Map<string, SavedFlow>();
const results = new Map<string, ContractTerminationResponse>();

export function getTerminationForm(contractId: string) {
  return apiRequest<ContractTerminationFormData>(`/api/contracts/${contractId}/termination-form`);
}

export function terminateContract(contractId: string, payload: ContractTerminationDraft) {
  return apiRequest<ContractTerminationResponse>(`/api/contracts/${contractId}/terminate`, {
    method: 'POST',
    body: { terminationType: payload.terminationType, effectiveEndDate: payload.effectiveEndDate, reason: payload.reason, notes: payload.additionalNotes },
  });
}

export function cancelContractBeforeStart(contractId: string, payload: ContractTerminationDraft) {
  return apiRequest<ContractTerminationResponse>(`/api/contracts/${contractId}/cancel-before-start`, {
    method: 'POST',
    body: { reason: payload.reason, notes: payload.additionalNotes },
  });
}

export function saveTerminationFlow(form: ContractTerminationFormData, draft: ContractTerminationDraft) { flows.set(form.contractId, { form, draft }); }
export function getTerminationFlow(contractId: string) { return flows.get(contractId); }
export function saveTerminationResult(result: ContractTerminationResponse) { results.set(result.contractId, result); }
export function getTerminationResult(contractId: string) { return results.get(contractId); }
