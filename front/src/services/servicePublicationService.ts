import { apiRequest } from '@/services/api';
import type { ServicePublication, ServicePublicationFilters, ServicePublicationPage } from '@/types/servicePublication';

function query(filters: ServicePublicationFilters, page: number, size: number) {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  Object.entries(filters).forEach(([key, value]) => { if (value) params.set(key, value); });
  return params.toString();
}

export function getServicePublications(filters: ServicePublicationFilters = {}, page = 0, size = 10) {
  return apiRequest<ServicePublicationPage>(`/api/responsible/service-publications?${query(filters, page, size)}`);
}

export function getServicePublication(id: string) {
  return apiRequest<ServicePublication>(`/api/responsible/service-publications/${id}`);
}

export function cancelServicePublication(id: string) {
  return apiRequest<ServicePublication>(`/api/responsible/service-publications/${id}/cancel`, { method: 'PATCH' });
}
