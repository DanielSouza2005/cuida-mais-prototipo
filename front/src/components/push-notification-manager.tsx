import * as Notifications from 'expo-notifications';
import { router } from 'expo-router';
import { useCallback, useEffect, useRef } from 'react';
import { Alert, Linking, Platform } from 'react-native';

import { useAuth } from '@/hooks/useAuth';
import { emitNotificationsChanged } from '@/services/notificationEvents';
import { readNotification } from '@/services/notificationService';
import {
  PENDING_NOTIFICATION_KEY,
  PUSH_PERMISSION_NOTICE_KEY,
  registerCurrentDevice,
} from '@/services/pushNotificationService';
import { deleteSessionItem, getSessionItem, setSessionItem } from '@/services/sessionStorage';
import type { ApiUserType } from '@/types/auth';
import { getNotificationHref, type NotificationNavigationData } from '@/utils/notificationNavigation';

function payloadFrom(response: Notifications.NotificationResponse): NotificationNavigationData | null {
  const data = response.notification.request.content.data;
  const value: NotificationNavigationData = {
    notificationId: typeof data.notificationId === 'string' ? data.notificationId : undefined,
    notificationType: typeof data.notificationType === 'string' ? data.notificationType : undefined,
    relatedEntityType: typeof data.relatedEntityType === 'string' ? data.relatedEntityType : undefined,
    relatedEntityId: typeof data.relatedEntityId === 'string' ? data.relatedEntityId : undefined,
  };
  return value.notificationId || value.relatedEntityId ? value : null;
}

async function openNotification(data: NotificationNavigationData, userType: ApiUserType) {
  if (data.notificationId) {
    try {
      await readNotification(data.notificationId);
      emitNotificationsChanged();
    } catch {
      Alert.alert('Notificação', 'Não foi possível abrir esta notificação.');
      return;
    }
  }
  const href = getNotificationHref(data, userType);
  if (!href) {
    Alert.alert('Notificação', 'Não foi possível abrir esta notificação.');
    return;
  }
  router.push(href);
}

export function PushNotificationManager() {
  const { isAuthenticated, isLoading, token, user } = useAuth();
  const handledResponses = useRef(new Set<string>());
  const registering = useRef(false);

  const handleResponse = useCallback(async (response: Notifications.NotificationResponse) => {
    const identifier = response.notification.request.identifier;
    if (handledResponses.current.has(identifier)) return;
    handledResponses.current.add(identifier);
    const data = payloadFrom(response);
    Notifications.clearLastNotificationResponse();
    if (!data) return;
    if (!isAuthenticated || !user) {
      await setSessionItem(PENDING_NOTIFICATION_KEY, JSON.stringify(data));
      return;
    }
    await openNotification(data, user.userType);
  }, [isAuthenticated, user]);

  useEffect(() => {
    if (Platform.OS === 'web') return;
    const received = Notifications.addNotificationReceivedListener(() => emitNotificationsChanged());
    const response = Notifications.addNotificationResponseReceivedListener((value) => { void handleResponse(value); });
    const lastResponse = Notifications.getLastNotificationResponse();
    if (lastResponse) void handleResponse(lastResponse);
    return () => { received.remove(); response.remove(); };
  }, [handleResponse]);

  useEffect(() => {
    if (isLoading || !isAuthenticated || !token || !user || Platform.OS === 'web') return;
    let active = true;
    const register = async () => {
      if (registering.current) return;
      registering.current = true;
      try {
        const result = await registerCurrentDevice(token);
        if (!active) return;
        if (result === 'granted') await deleteSessionItem(PUSH_PERMISSION_NOTICE_KEY);
        if (result === 'denied' && !await getSessionItem(PUSH_PERMISSION_NOTICE_KEY)) {
          await setSessionItem(PUSH_PERMISSION_NOTICE_KEY, 'shown');
          Alert.alert(
            'Notificações desativadas',
            'As notificações estão desativadas. Alguns avisos podem não aparecer no dispositivo.',
            [
              { text: 'Agora não', style: 'cancel' },
              { text: 'Abrir configurações', onPress: () => void Linking.openSettings() },
            ],
          );
        }
      } finally {
        registering.current = false;
      }
    };
    void register();
    const tokenSubscription = Notifications.addPushTokenListener(() => { void register(); });
    return () => { active = false; tokenSubscription.remove(); };
  }, [isAuthenticated, isLoading, token, user]);

  useEffect(() => {
    if (isLoading || !isAuthenticated || !user) return;
    let active = true;
    getSessionItem(PENDING_NOTIFICATION_KEY).then(async (stored) => {
      if (!active || !stored) return;
      await deleteSessionItem(PENDING_NOTIFICATION_KEY);
      try { await openNotification(JSON.parse(stored) as NotificationNavigationData, user.userType); }
      catch { Alert.alert('Notificação', 'Não foi possível abrir esta notificação.'); }
    });
    return () => { active = false; };
  }, [isAuthenticated, isLoading, user]);

  return null;
}
