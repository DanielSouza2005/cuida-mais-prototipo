import * as Location from 'expo-location';

export class DeviceLocationError extends Error {
  constructor(public readonly reason: 'permission' | 'unavailable' | 'mocked' | 'stale') {
    super(reason);
  }
}

export async function captureCurrentLocation() {
  const currentPermission = await Location.getForegroundPermissionsAsync();
  const permission = currentPermission.granted
    ? currentPermission
    : await Location.requestForegroundPermissionsAsync();
  if (!permission.granted) throw new DeviceLocationError('permission');
  if (!await Location.hasServicesEnabledAsync()) throw new DeviceLocationError('unavailable');

  for (let attempt = 0; attempt < 2; attempt++) {
    try {
      const position = await Location.getCurrentPositionAsync({ accuracy: Location.Accuracy.High });
      if (position.mocked) throw new DeviceLocationError('mocked');
      const receivedAt = Date.now();
      const nativeTimestamp = Number(position.timestamp);
      const nativeAge = Number.isFinite(nativeTimestamp) ? receivedAt - nativeTimestamp : 0;
      if (nativeAge > 60_000 || nativeAge < -30_000) {
        if (attempt === 0) continue;
        throw new DeviceLocationError('stale');
      }
      return {
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
        accuracy: position.coords.accuracy ?? 0,
        locationCapturedAt: new Date(receivedAt).toISOString(),
        mocked: false,
      };
    } catch (error) {
      if (error instanceof DeviceLocationError) {
        if (error.reason === 'stale' && attempt === 0) continue;
        throw error;
      }
      if (attempt === 1) throw new DeviceLocationError('unavailable');
    }
  }
  throw new DeviceLocationError('stale');
}
