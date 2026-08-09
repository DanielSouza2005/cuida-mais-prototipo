import Constants from 'expo-constants';
import * as Notifications from 'expo-notifications';
import { Platform } from 'react-native';

import { apiRequest } from '@/services/api';
import { getSessionItem, setSessionItem } from '@/services/sessionStorage';

export const PUSH_TOKEN_STORAGE_KEY = 'cuida_mais_expo_push_token';
export const PUSH_PERMISSION_NOTICE_KEY = 'cuida_mais_push_permission_notice';
export const PENDING_NOTIFICATION_KEY = 'cuida_mais_pending_notification';

export type PushRegistrationResult = 'granted' | 'denied' | 'unavailable';

if (Platform.OS !== 'web') {
  Notifications.setNotificationHandler({
    handleNotification: async () => ({
      shouldShowBanner: true,
      shouldShowList: true,
      shouldPlaySound: false,
      shouldSetBadge: false,
    }),
  });
}

function hasPermission(status: Notifications.NotificationPermissionsStatus) {
  return status.granted
    || status.ios?.status === Notifications.IosAuthorizationStatus.AUTHORIZED
    || status.ios?.status === Notifications.IosAuthorizationStatus.PROVISIONAL
    || status.ios?.status === Notifications.IosAuthorizationStatus.EPHEMERAL;
}

export async function getNotificationPermissionState(): Promise<PushRegistrationResult> {
  if (Platform.OS === 'web') return 'unavailable';
  try {
    return hasPermission(await Notifications.getPermissionsAsync()) ? 'granted' : 'denied';
  } catch {
    return 'unavailable';
  }
}

async function ensurePermission() {
  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync('default', {
      name: 'Notificações',
      importance: Notifications.AndroidImportance.HIGH,
      vibrationPattern: [0, 250, 250, 250],
      lightColor: '#2F8FC4',
      enableVibrate: true,
      showBadge: true,
    });
  }
  let status = await Notifications.getPermissionsAsync();
  if (!hasPermission(status) && status.canAskAgain) {
    status = await Notifications.requestPermissionsAsync({ ios: { allowAlert: true, allowBadge: true, allowSound: true } });
  }
  return hasPermission(status);
}

function projectId() {
  return process.env.EXPO_PUBLIC_EAS_PROJECT_ID
    ?? Constants.easConfig?.projectId
    ?? Constants.expoConfig?.extra?.eas?.projectId;
}

export async function registerCurrentDevice(authToken: string): Promise<PushRegistrationResult> {
  if (Platform.OS === 'web') return 'unavailable';
  try {
    if (!await ensurePermission()) return 'denied';
    const easProjectId = projectId();
    if (!easProjectId) return 'unavailable';
    const expoPushToken = (await Notifications.getExpoPushTokenAsync({ projectId: easProjectId })).data;
    await apiRequest('/api/notifications/push-tokens', {
      method: 'POST',
      token: authToken,
      body: {
        token: expoPushToken,
        platform: Platform.OS,
        appVersion: Constants.expoConfig?.version,
      },
    });
    await setSessionItem(PUSH_TOKEN_STORAGE_KEY, expoPushToken);
    return 'granted';
  } catch {
    return 'unavailable';
  }
}

export async function disableCurrentDevice(authToken: string | null) {
  if (!authToken) return;
  const expoPushToken = await getSessionItem(PUSH_TOKEN_STORAGE_KEY);
  if (!expoPushToken) return;
  await apiRequest('/api/notifications/push-tokens/current', {
    method: 'DELETE',
    token: authToken,
    body: { token: expoPushToken },
  });
}
