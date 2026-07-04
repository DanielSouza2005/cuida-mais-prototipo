import { apiRequest } from '@/services/api';
import type { Address } from '@/types/auth';
import type {
  CaregiverAvailabilityUpdatePayload,
  CaregiverExperienceUpdatePayload,
  CaregiverModalitiesUpdatePayload,
  CaregiverServicesUpdatePayload,
  MyProfile,
  PersonalInfoUpdatePayload,
  ProfileMessageResponse,
} from '@/types/profile';

function toIsoDate(value: string) {
  const [day, month, year] = value.split('/');
  if (!day || !month || !year) return value;
  return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;
}

export function getMyProfile() {
  return apiRequest<MyProfile>('/api/profile/me', {
    method: 'GET',
  });
}

export function updatePersonalInfo(payload: PersonalInfoUpdatePayload) {
  return apiRequest<ProfileMessageResponse>('/api/profile/personal-info', {
    method: 'PATCH',
    body: {
      ...payload,
      dataNascimento: toIsoDate(payload.dataNascimento),
    },
  });
}

export function updateCaregiverAddress(payload: Address) {
  return apiRequest<ProfileMessageResponse>('/api/profile/caregiver/address', {
    method: 'PATCH',
    body: payload,
  });
}

export function updateCaregiverExperience(payload: CaregiverExperienceUpdatePayload) {
  return apiRequest<ProfileMessageResponse>('/api/profile/caregiver/experience', {
    method: 'PATCH',
    body: payload,
  });
}

export function updateCaregiverAvailability(payload: CaregiverAvailabilityUpdatePayload) {
  return apiRequest<ProfileMessageResponse>('/api/profile/caregiver/availability', {
    method: 'PATCH',
    body: payload,
  });
}

export function updateCaregiverModalities(payload: CaregiverModalitiesUpdatePayload) {
  return apiRequest<ProfileMessageResponse>('/api/profile/caregiver/modalities', {
    method: 'PATCH',
    body: payload,
  });
}

export function updateCaregiverServices(payload: CaregiverServicesUpdatePayload) {
  return apiRequest<ProfileMessageResponse>('/api/profile/caregiver/services', {
    method: 'PATCH',
    body: payload,
  });
}
