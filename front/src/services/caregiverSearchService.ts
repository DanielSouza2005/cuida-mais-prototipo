import { apiRequest } from '@/services/api';
import type {
  CaregiverProfileDetails,
  CaregiverSearchFilters,
  CaregiverSearchPageResponse,
  LocationSuggestion,
} from '@/types/caregiverSearch';

const PAGE_SIZE = 5;

function appendList(params: URLSearchParams, key: string, values: string[]) {
  if (values.length > 0) {
    params.set(key, values.join(','));
  }
}

export function searchCaregivers(filters: CaregiverSearchFilters) {
  const params = new URLSearchParams();
  params.set('page', String(filters.page));
  params.set('size', String(PAGE_SIZE));

  if (filters.query.trim()) {
    params.set('name', filters.query.trim());
  }

  if (filters.location?.city) {
    params.set('city', filters.location.city);
  }

  if (filters.location?.neighborhood) {
    params.set('neighborhood', filters.location.neighborhood);
  }

  if (filters.location?.state) {
    params.set('state', filters.location.state);
  }

  if (filters.origin) {
    params.set('originLat', String(filters.origin.latitude));
    params.set('originLng', String(filters.origin.longitude));
  }

  appendList(params, 'availabilityPeriods', filters.availability);
  appendList(params, 'services', filters.services);
  appendList(params, 'modalities', filters.modalities);

  return apiRequest<CaregiverSearchPageResponse>(`/api/caregivers/search?${params.toString()}`, {
    method: 'GET',
  });
}

export function getCaregiverLocations(query: string) {
  const params = new URLSearchParams();
  if (query.trim()) {
    params.set('query', query.trim());
  }

  return apiRequest<LocationSuggestion[]>(`/api/caregivers/locations?${params.toString()}`, {
    method: 'GET',
  });
}

export function getCaregiverDetails(id: string, origin?: CaregiverSearchFilters['origin']) {
  const params = new URLSearchParams();
  if (origin) {
    params.set('originLat', String(origin.latitude));
    params.set('originLng', String(origin.longitude));
  }

  const query = params.toString();
  return apiRequest<CaregiverProfileDetails>(`/api/caregivers/${id}${query ? `?${query}` : ''}`, {
    method: 'GET',
  });
}
