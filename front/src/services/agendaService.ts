import { apiRequest } from '@/services/api';
import { mapContractDetails, type ApiContractDetails } from '@/services/responsibleContractsService';
import type { AgendaEventDetails, AgendaEventsResponse, AgendaViewMode } from '@/types/agenda';

export async function getAgendaEvents(startDate: string, endDate: string, viewMode: AgendaViewMode) {
  const query = new URLSearchParams({ startDate, endDate, viewMode });
  return apiRequest<AgendaEventsResponse>(`/api/agenda/events?${query.toString()}`);
}

export async function getAgendaEventDetails(contractId: string, eventDate: string): Promise<AgendaEventDetails> {
  const query = new URLSearchParams({ contractId, eventDate });
  const response = await apiRequest<{ event: AgendaEventDetails['event']; contract: ApiContractDetails }>(
    `/api/agenda/events/detail?${query.toString()}`,
  );
  return { event: response.event, contract: mapContractDetails(response.contract) };
}
