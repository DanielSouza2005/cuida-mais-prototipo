import { apiRequest } from '@/services/api';
import type { CareRoutine, CareRoutineFormData, CareRoutinePayload } from '@/types/careRoutine';

const base = '/api/responsible/care-routines';
export function listCareRoutines(filters?: { assistedPersonId?: string; active?: boolean }) {
  const query = new URLSearchParams();
  if (filters?.assistedPersonId) query.set('assistedPersonId', filters.assistedPersonId);
  if (filters?.active !== undefined) query.set('active', String(filters.active));
  const search = query.toString();
  return apiRequest<CareRoutine[]>(`${base}${search ? `?${search}` : ''}`);
}
export function getCareRoutine(id: string) { return apiRequest<CareRoutine>(`${base}/${id}`); }
export function getCareRoutineFormData() { return apiRequest<CareRoutineFormData>(`${base}/form-data`); }
export function createCareRoutine(payload: CareRoutinePayload) { return apiRequest<CareRoutine>(base, { method: 'POST', body: payload }); }
export function updateCareRoutine(id: string, payload: CareRoutinePayload) { return apiRequest<CareRoutine>(`${base}/${id}`, { method: 'PUT', body: payload }); }
export function deactivateCareRoutine(id: string) { return apiRequest<CareRoutine>(`${base}/${id}/deactivate`, { method: 'PATCH' }); }
export function activateCareRoutine(id: string) { return apiRequest<CareRoutine>(`${base}/${id}/activate`, { method: 'PATCH' }); }
