import type { Href } from 'expo-router';

import type { ApiUserType } from '@/types/auth';

export type NotificationNavigationData = {
  notificationId?: string;
  notificationType?: string;
  relatedEntityType?: string;
  relatedEntityId?: string;
};

export function getNotificationHref(data: NotificationNavigationData, userType?: ApiUserType): Href | null {
  if (!data.relatedEntityId) return null;
  if (data.relatedEntityType === 'CARE_TASK') {
    return userType === 'caregiver'
      ? '/caregiver-tasks' as Href
      : `/care-task/${data.relatedEntityId}` as Href;
  }
  if (data.relatedEntityType === 'TASK_OCCURRENCE' || data.relatedEntityType === 'CARE_OCCURRENCE') {
    return `/task-occurrence/${data.relatedEntityId}` as Href;
  }
  if (data.relatedEntityType === 'CARE_CONTRACT') {
    return `/responsible-contract/${data.relatedEntityId}?itemType=CARE_CONTRACT` as Href;
  }
  if (data.relatedEntityType === 'SERVICE_REQUEST') {
    if (userType === 'caregiver') return `/caregiver-service-request/${data.relatedEntityId}` as Href;
    if (userType === 'family') return `/responsible-service-request/${data.relatedEntityId}` as Href;
  }
  return null;
}
