import { apiRequest } from '@/services/api';
import type { CareCompletionPhoto, CareDiaryItem, CareDiaryResponse, CareTaskDetails, CareTaskPage, CareTaskPayload, ManualCareFormData, ManualCarePayload, TaskCategory, TaskContractOption, TaskEditScope, TaskFormData, TaskOccurrence, TaskOccurrencePage, TaskOccurrenceStatus, TaskPriority, TaskSeriesStatus } from '@/types/careTasks';

function queryString(values: Record<string, string | number | undefined>) {
  const query = new URLSearchParams();
  Object.entries(values).forEach(([key, value]) => { if (value !== undefined && value !== '') query.set(key, String(value)); });
  return query.toString();
}

export function getTaskFormData() { return apiRequest<TaskFormData>('/api/responsible/care-tasks/form-data'); }
export function createCareTask(payload: CareTaskPayload) { return apiRequest<CareTaskDetails>('/api/responsible/care-tasks', { method: 'POST', body: payload }); }
export function getCareTasks(params: { search?: string; category?: TaskCategory; status?: TaskSeriesStatus; priority?: TaskPriority; assistedPersonId?: string; caregiverId?: string; occurrenceStatus?: TaskOccurrenceStatus; startDate?: string; endDate?: string; page?: number; size?: number } = {}) {
  return apiRequest<CareTaskPage>(`/api/responsible/care-tasks?${queryString({ ...params, page: params.page ?? 0, size: params.size ?? 20 })}`);
}
export function getCareTask(id: string) { return apiRequest<CareTaskDetails>(`/api/responsible/care-tasks/${id}`); }
export function updateCareTask(id: string, payload: CareTaskPayload & { scope: TaskEditScope; occurrenceId?: string; occurrenceVersion?: number; version: number }) {
  const { assistedPersonId: _assistedPersonId, contractId: _contractId, caregiverId: _caregiverId, ...body } = payload;
  return apiRequest<CareTaskDetails>(`/api/responsible/care-tasks/${id}`, { method: 'PUT', body });
}
export function taskAction(id: string, action: 'pause' | 'reactivate' | 'cancel', version: number, reason?: string) {
  return apiRequest<CareTaskDetails>(`/api/responsible/care-tasks/${id}/${action}`, { method: 'PATCH', body: { version, reason } });
}
export function getTaskOccurrences(id: string, params: { startDate: string; endDate: string; status?: TaskOccurrenceStatus; history?: boolean; page?: number; size?: number }) {
  return apiRequest<TaskOccurrencePage>(`/api/responsible/care-tasks/${id}/occurrences?${queryString({ ...params, history: params.history ? 'true' : 'false', page: params.page ?? 0, size: params.size ?? 20 })}`);
}
export function cancelTaskOccurrence(id: string, version: number, reason?: string) { return apiRequest<TaskOccurrence>(`/api/responsible/care-tasks/occurrences/${id}/cancel`, { method: 'PATCH', body: { version, reason } }); }
export function getResponsibleOccurrence(id: string) { return apiRequest<TaskOccurrence>(`/api/responsible/care-tasks/occurrences/${id}`); }
export function getCaregiverDayTasks(date: string, timezone: string, filters: { category?: TaskCategory; status?: TaskOccurrenceStatus; assistedPersonId?: string; contractId?: string } = {}) {
  return apiRequest<TaskOccurrencePage>(`/api/caregiver/tasks?${queryString({ date, timezone, ...filters, page: 0, size: 50 })}`);
}
export function getCaregiverOccurrence(id: string) { return apiRequest<TaskOccurrence>(`/api/caregiver/care-tasks/occurrences/${id}`); }
export function getResponsibleDayCareOccurrences(date:string,timezone:string,status?:TaskOccurrenceStatus,page=0,size=50,contractId?:string){return apiRequest<TaskOccurrencePage>(`/api/responsible/care-occurrences?${queryString({date,timezone,status,page,size,contractId})}`);}
export function getResponsibleCareOccurrence(id:string){return apiRequest<TaskOccurrence>(`/api/responsible/care-occurrences/${id}`);}
export function completeTaskOccurrence(id: string, version: number, executionNote: string | undefined, photos: CareCompletionPhoto[]) {
  const form = new FormData(); form.append('version', String(version)); if (executionNote) form.append('notes', executionNote);
  photos.forEach((photo) => form.append('photos', photo.file ?? ({ uri: photo.uri, name: photo.name, type: photo.type } as unknown as Blob)));
  return apiRequest<TaskOccurrence>(`/api/caregiver/care-tasks/occurrences/${id}/complete`, { method: 'PATCH', body: form });
}
export function markTaskOccurrenceNotCompleted(id: string, version: number, reason: string, executionNote?: string) { return apiRequest<TaskOccurrence>(`/api/caregiver/care-tasks/occurrences/${id}/not-completed`, { method: 'PATCH', body: { reason, executionNote, version } }); }

export function getManualCareFormData(date: string) { return apiRequest<ManualCareFormData>(`/api/caregiver/care-occurrences/manual/form-data?${queryString({ date })}`); }
export function createManualCare(payload: ManualCarePayload) {
  const form = new FormData();
  form.append('contractId', payload.contractId); form.append('assistedPersonId', payload.assistedPersonId);
  form.append('entryDate', payload.entryDate); form.append('occurredTime', payload.occurredTime);
  form.append('careType', payload.careType); form.append('title', payload.title); form.append('description', payload.description);
  form.append('timezone', payload.timezone); form.append('important', String(payload.important));
  if (payload.notes) form.append('notes', payload.notes);
  payload.photos.forEach((photo) => form.append('photos', photo.file ?? ({ uri: photo.uri, name: photo.name, type: photo.type } as unknown as Blob)));
  return apiRequest<CareDiaryItem>('/api/caregiver/care-occurrences/manual', { method: 'POST', body: form });
}
export function getCaregiverDiary(date: string, timezone: string, filters: { assistedPersonId?: string; contractId?: string } = {}) {
  return apiRequest<CareDiaryResponse>(`/api/caregiver/assisted-person-diary?${queryString({ date, timezone, ...filters })}`);
}
export function getResponsibleDiary(date: string, timezone: string, filters: { assistedPersonId?: string; contractId?: string } = {}) {
  return apiRequest<CareDiaryResponse>(`/api/responsible/assisted-person-diary?${queryString({ date, timezone, ...filters })}`);
}
export function getManualCare(id: string, caregiver: boolean) {
  return apiRequest<CareDiaryItem>(`/api/${caregiver ? 'caregiver' : 'responsible'}/care-occurrences/manual/${id}`);
}

export function optionForTask(task: { contractId: string; assistedPersonId: string; assistedPersonName: string; caregiverId: string; caregiverName: string; startDate: string; endDate?: string }): TaskContractOption {
  return { contractId: task.contractId, assistedPersonId: task.assistedPersonId, assistedPersonName: task.assistedPersonName, caregiverId: task.caregiverId, caregiverName: task.caregiverName, startDate: task.startDate, endDate: task.endDate, status: 'ATIVA' };
}
