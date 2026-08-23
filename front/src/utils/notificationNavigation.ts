import type { Href } from 'expo-router';

import type { ApiUserType } from '@/types/auth';
import { getAcceptedOpportunityContract } from '@/services/serviceOpportunityService';

export type NotificationNavigationData = {
  notificationId?: string;
  notificationType?: string;
  type?: string;
  relatedEntityType?: string;
  relatedEntityId?: string;
};

export function getNotificationHref(data: NotificationNavigationData, userType?: ApiUserType): Href | null {
  const notificationType = data.notificationType ?? data.type;
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
    if (notificationType === 'SERVICE_PUBLICATION_CREATED' || notificationType === 'SERVICE_PUBLICATION_CANCELED' || notificationType === 'SERVICE_PUBLICATION_EXPIRED' || notificationType === 'SERVICE_PUBLICATION_STATUS_UPDATED') return `/responsible-service-publication/${data.relatedEntityId}` as Href;
    if (notificationType === 'SERVICE_OPPORTUNITY_APPLICATION_ACCEPTED') return '/service-opportunities?mode=SENT&notice=contract-unavailable' as Href;
    if (notificationType === 'SERVICE_OPPORTUNITY_APPLICATION_REJECTED') return `/service-opportunity/${data.relatedEntityId}` as Href;
    if (userType === 'caregiver') return `/caregiver-service-request/${data.relatedEntityId}` as Href;
    if (userType === 'family') return `/responsible-service-request/${data.relatedEntityId}` as Href;
  }
  return null;
}

export async function resolveNotificationHref(data: NotificationNavigationData, userType?: ApiUserType): Promise<Href | null> {
  const notificationType = data.notificationType ?? data.type;
  if (userType === 'caregiver' && notificationType === 'SERVICE_OPPORTUNITY_APPLICATION_ACCEPTED' && data.relatedEntityType === 'SERVICE_REQUEST' && data.relatedEntityId) {
    try {
      const contract = await getAcceptedOpportunityContract(data.relatedEntityId);
      return `/responsible-contract/${contract.contractId}?itemType=CARE_CONTRACT` as Href;
    } catch {
      return '/service-opportunities?mode=SENT&notice=contract-unavailable' as Href;
    }
  }
  return getNotificationHref(data, userType);
}
