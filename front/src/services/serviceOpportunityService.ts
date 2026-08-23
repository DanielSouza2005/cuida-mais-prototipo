import { apiRequest } from '@/services/api';
import type { ServiceRequestStatus } from '@/types/serviceRequest';
import type { ServiceOpportunity, ServiceOpportunityFilters, ServiceOpportunityPage } from '@/types/serviceOpportunity';
import type { LocationSuggestion } from '@/types/caregiverSearch';

function query(filters: ServiceOpportunityFilters, page: number, size: number) {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (filters.location?.city) params.set('city', filters.location.city);
  if (filters.location?.neighborhood) params.set('neighborhood', filters.location.neighborhood);
  if (filters.location?.state) params.set('state', filters.location.state);
  if (filters.origin) { params.set('originLat', String(filters.origin.latitude)); params.set('originLng', String(filters.origin.longitude)); }
  if (filters.hiringType) params.set('hiringType', filters.hiringType);
  return params.toString();
}

export function searchServiceOpportunities(filters: ServiceOpportunityFilters = {}, page = 0, size = 10) {
  return apiRequest<ServiceOpportunityPage>(`/api/caregiver/service-opportunities?${query(filters, page, size)}`);
}

export function getSentOpportunityApplications(status?: ServiceRequestStatus, page = 0, size = 10) {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (status) params.set('status', status);
  return apiRequest<ServiceOpportunityPage>(`/api/caregiver/service-opportunities/applications?${params.toString()}`);
}

export function getServiceOpportunity(id: string, origin?: ServiceOpportunityFilters['origin']) {
  const params = new URLSearchParams();
  if (origin) { params.set('originLat', String(origin.latitude)); params.set('originLng', String(origin.longitude)); }
  const suffix = params.toString();
  return apiRequest<ServiceOpportunity>(`/api/caregiver/service-opportunities/${id}${suffix ? `?${suffix}` : ''}`);
}

export function applyToServiceOpportunity(id: string) {
  return apiRequest<ServiceOpportunity>(`/api/caregiver/service-opportunities/${id}/apply`, { method: 'POST' });
}

export function getServiceOpportunityLocations(search: string) {
  const params = new URLSearchParams();
  if (search.trim()) params.set('query', search.trim());
  return apiRequest<LocationSuggestion[]>(`/api/caregiver/service-opportunities/locations?${params.toString()}`);
}

export function getAcceptedOpportunityContract(opportunityId: string) {
  return apiRequest<{ contractId: string }>(`/api/caregiver/service-opportunities/${opportunityId}/contract`);
}
