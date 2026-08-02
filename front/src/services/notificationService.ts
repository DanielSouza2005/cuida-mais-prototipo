import { apiRequest } from '@/services/api';
import type { NotificationItem, NotificationPreferenceGroup, NotificationPreferencesResponse } from '@/types/notification';
import { getCanonicalNotificationType } from '@/utils/notificationCatalog';

export function getNotifications() {
  return apiRequest<NotificationItem[]>('/api/notifications').then(uniqueById);
}

function uniqueById(items: NotificationItem[]) {
  return [...new Map(items.map((item) => [item.id, item])).values()];
}

export function getUnreadNotificationCount() {
  return apiRequest<{ count: number }>('/api/notifications/unread-count');
}

export function readNotification(id: string) {
  return apiRequest<NotificationItem>(`/api/notifications/${id}/read`, { method: 'PATCH' });
}

export function clearAllNotifications() {
  return apiRequest<{ message: string }>('/api/notifications/clear-all', { method: 'PATCH' });
}

export function getNotificationPreferences() {
  return apiRequest<NotificationPreferencesResponse>('/api/notifications/preferences').then(normalizeNotificationPreferences);
}

export function updateNotificationPreference(type: string, enabled: boolean) {
  return apiRequest<NotificationPreferencesResponse>('/api/notifications/preferences', {
    method: 'PATCH',
    body: { preferences: [{ type, enabled }] },
  }).then(normalizeNotificationPreferences);
}

export function normalizeNotificationPreferences(response: NotificationPreferencesResponse): NotificationPreferencesResponse {
  type Candidate = {
    group: Pick<NotificationPreferenceGroup, 'category' | 'categoryLabel'>;
    item: NotificationPreferenceGroup['items'][number];
    canonicalSource: boolean;
  };

  const uniqueItems = new Map<string, Candidate>();
  for (const group of response.groups) {
    for (const item of group.items) {
      const canonicalType = getCanonicalNotificationType(item.type);
      const candidate: Candidate = {
        group: { category: group.category, categoryLabel: group.categoryLabel },
        item: { ...item, type: canonicalType },
        canonicalSource: item.type === canonicalType,
      };
      const current = uniqueItems.get(canonicalType);
      if (!current || candidate.canonicalSource) uniqueItems.set(canonicalType, candidate);
    }
  }

  const groups = new Map<string, NotificationPreferenceGroup>();
  for (const { group, item } of uniqueItems.values()) {
    const current = groups.get(group.category) ?? { ...group, items: [] };
    current.items.push(item);
    groups.set(group.category, current);
  }
  return { groups: [...groups.values()] };
}
