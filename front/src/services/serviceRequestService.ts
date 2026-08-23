import { apiRequest } from '@/services/api';
import type { ServiceRequestDraft, ServiceRequestFormData, ServiceRequestPayload, ServiceRequestResponse } from '@/types/serviceRequest';

let currentDraft: ServiceRequestDraft | null = null;
let lastRequest: ServiceRequestResponse | null = null;

export function getServiceRequestFormData(caregiverId?: string) {
  return apiRequest<ServiceRequestFormData>(`/api/service-requests/form-data${caregiverId ? `?caregiverId=${encodeURIComponent(caregiverId)}` : ''}`);
}
export function createServiceRequest(payload: ServiceRequestPayload) {
  return apiRequest<ServiceRequestResponse>('/api/service-requests', { method: 'POST', body: payload }).then((response) => { lastRequest = response; return response; });
}
export function getServiceRequest(id: string) { return apiRequest<ServiceRequestResponse>(`/api/service-requests/${id}`); }
export function cancelServiceRequest(id: string) { return apiRequest<ServiceRequestResponse>(`/api/service-requests/${id}/cancel`, { method: 'PATCH' }).then((response) => { lastRequest = response; return response; }); }
export function getMyServiceRequests() { return apiRequest<ServiceRequestResponse[]>('/api/service-requests/my'); }
export function getServiceRequestDraft() { return currentDraft; }
export function saveServiceRequestDraft(draft: ServiceRequestDraft) { currentDraft = draft; }
export function getLastServiceRequest() { return lastRequest; }
