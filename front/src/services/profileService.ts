import { apiRequest } from '@/services/api';
import type { Address, SelectedProfilePhoto } from '@/types/auth';
import { appendProfilePhoto } from '@/utils/profilePhoto';
import type {
  CaregiverAvailabilityUpdatePayload,
  CaregiverExperienceUpdatePayload,
  CaregiverModalitiesUpdatePayload,
  CaregiverServicesUpdatePayload,
  AssistedPersonUpdatePayload,
  EmergencyContactUpdatePayload,
  MyProfile,
  PersonalInfoUpdatePayload,
  ProfileMessageResponse,
  ResponsibleProfileUpdatePayload,
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

export type ProfilePhotoResponse = { profilePhotoUrl: string | null };

export function updateProfilePhoto(photo: SelectedProfilePhoto) {
  const form = new FormData();
  appendProfilePhoto(form, photo);
  return apiRequest<ProfilePhotoResponse>('/api/profile/photo', {
    method: 'PATCH',
    body: form,
  });
}

export function deleteProfilePhoto() {
  return apiRequest<ProfilePhotoResponse>('/api/profile/photo', {
    method: 'DELETE',
  });
}

export function updatePersonalInfo(payload: PersonalInfoUpdatePayload) {
  return apiRequest<ProfileMessageResponse>('/api/profile/personal-info', {
    method: 'PATCH',
    body: payload,
  });
}

export function updateResponsibleProfile(payload: ResponsibleProfileUpdatePayload) {
  return apiRequest<ProfileMessageResponse>('/api/profile/responsible', {
    method: 'PATCH',
    body: payload,
  });
}

export function updateAssistedPerson(id: string, payload: AssistedPersonUpdatePayload) {
  return apiRequest<ProfileMessageResponse>(`/api/profile/assisted-persons/${id}`, {
    method: 'PATCH',
    body: {
      ...payload,
      dataNascimento: toIsoDate(payload.dataNascimento),
    },
  });
}

export function updateCareAddress(id: string, payload: Address) {
  return apiRequest<ProfileMessageResponse>(`/api/profile/assisted-persons/${id}/care-address`, {
    method: 'PATCH',
    body: payload,
  });
}

export function updateEmergencyContact(id: string, payload: EmergencyContactUpdatePayload) {
  return apiRequest<ProfileMessageResponse>(`/api/profile/assisted-persons/${id}/emergency-contact`, {
    method: 'PATCH',
    body: payload,
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
