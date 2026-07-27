import type { HiringType, RequestedActivity, ServiceRequestStatus } from '@/types/serviceRequest';
import type { CareRoutine } from '@/types/careRoutine';

export type ReceivedServiceRequest = {
  id: string; status: ServiceRequestStatus; createdAt: string; expiresAt: string; hasScheduleConflict: boolean;
  responsible: { id: string; name: string; relationship?: string | null; contactPreference?: string | null };
  assistedPerson: { id: string; name: string; age: number; dependencyLevel: string; mobility: string; allergies: string[]; foodRestrictions: string[]; medications?: string | null; notes?: string | null };
  careAddress: { cep?: string | null; street: string; number: string; complement?: string | null; neighborhood: string; city: string; state: string; referencePoint?: string | null }; distanceKm?: number | null;
  careRoutine?: CareRoutine | null;
  hiringType: HiringType; startDate: string; endDate?: string; specificDates: string[];
  scheduleDays: { weekday: string; startTime: string; endTime: string }[];
  needsDescription: string; activities: RequestedActivity[]; additionalNotes?: string; negotiationNotes?: string; rejectionReason?: string;
};
export type CaregiverNotification = { id: string; type: string; title: string; message: string; relatedEntityType: string; relatedEntityId: string; readAt?: string | null; createdAt: string };
export type RejectionReasonPayload = { reason?: string };
export type ReceivedServiceRequestPage = { content: ReceivedServiceRequest[]; page: number; size: number; totalElements: number; totalPages: number; last: boolean };
export type ResponsibleServiceRequest = {
  id: string; status: ServiceRequestStatus;
  caregiver: { id: string; name: string; profilePhotoUrl?: string | null; city?: string | null; state?: string | null };
  assistedPerson: { id: string; name: string; dependencyLevel: string; mobility: string };
  careAddress: { street: string; number: string; complement?: string | null; neighborhood: string; city: string; state: string; cep?: string | null; referencePoint?: string | null };
  careRoutine?: CareRoutine | null;
  hiringType: HiringType; startDate: string; endDate?: string | null; specificDates: string[];
  scheduleDays: { weekday: string; startTime: string; endTime: string }[]; activities: RequestedActivity[];
  needsDescription: string; additionalNotes?: string | null; negotiationNotes?: string | null; rejectionReason?: string | null;
  createdAt: string; expiresAt: string; answeredAt?: string | null;
};
