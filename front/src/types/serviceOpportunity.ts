import type { HiringType, ServiceRequestStatus } from '@/types/serviceRequest';
import type { LocationSuggestion } from '@/types/caregiverSearch';

export type ServiceOpportunity = {
  id: string;
  applicationId?: string | null;
  status: ServiceRequestStatus;
  applicationStatus?: ServiceRequestStatus | null;
  hiringType: HiringType;
  startDate?: string | null;
  endDate?: string | null;
  specificDates: string[];
  scheduleDays: { weekday: string; startTime: string; endTime: string }[];
  city: string;
  neighborhood: string;
  state: string;
  assistedPersonAlias: string;
  dependencyLevel: string;
  mobility: string;
  needsDescription: string;
  careRoutine?: { id: string; name: string; items: { title: string; description?: string | null; category?: string | null; customCategory?: string | null; scheduledTime?: string | null }[] } | null;
  distanceKm?: number | null;
  createdAt: string;
  expiresAt: string;
};

export type ServiceOpportunityPage = {
  content: ServiceOpportunity[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
};

export type ServiceOpportunityFilters = {
  location?: LocationSuggestion | null;
  origin?: { latitude: number; longitude: number } | null;
  hiringType?: HiringType;
};
