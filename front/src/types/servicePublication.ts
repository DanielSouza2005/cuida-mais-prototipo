import type { HiringType, ServiceRequestStatus } from '@/types/serviceRequest';

export type ServicePublicationApplication = {
  id: string;
  status: ServiceRequestStatus;
  caregiverId: string;
  caregiverName: string;
  caregiverProfilePhotoUrl?: string | null;
  createdAt: string;
};

export type ServicePublication = {
  id: string;
  status: ServiceRequestStatus;
  hiringType: HiringType;
  assistedPersonId: string;
  assistedPersonName: string;
  startDate?: string | null;
  endDate?: string | null;
  specificDates: string[];
  scheduleDays: { weekday: string; startTime: string; endTime: string }[];
  city: string;
  neighborhood: string;
  state: string;
  needsDescription: string;
  applicantCount: number;
  pendingApplicantCount: number;
  acceptedApplicantCount: number;
  applications: ServicePublicationApplication[];
  createdAt: string;
  expiresAt: string;
};

export type ServicePublicationPage = {
  content: ServicePublication[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
};

export type ServicePublicationFilters = {
  status?: ServiceRequestStatus;
  assistedPersonId?: string;
  hiringType?: HiringType;
  startDate?: string;
  endDate?: string;
  city?: string;
  neighborhood?: string;
  needs?: string;
};
