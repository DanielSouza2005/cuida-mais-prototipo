import * as Location from 'expo-location';

export class DeviceLocationError extends Error {
  constructor(public readonly reason: 'permission' | 'services-disabled' | 'timeout' | 'unavailable' | 'mocked' | 'stale') {
    super(reason);
  }
}

export async function captureCurrentLocation() {
  const currentPermission = await Location.getForegroundPermissionsAsync();
  const permission = currentPermission.granted
    ? currentPermission
    : await Location.requestForegroundPermissionsAsync();
  if (!permission.granted) throw new DeviceLocationError('permission');
  if (!await Location.hasServicesEnabledAsync()) throw new DeviceLocationError('services-disabled');

  for (let attempt = 0; attempt < 2; attempt++) {
    try {
      const position = await withTimeout(
        Location.getCurrentPositionAsync({ accuracy: Location.Accuracy.High }),
        10_000,
      );
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
        accuracy: position.coords.accuracy ?? null,
        locationCapturedAt: new Date(receivedAt).toISOString(),
        locationStatus: 'OBTIDA' as const,
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

export async function captureCurrentLocationForCheckout() {
  try {
    return await captureCurrentLocation();
  } catch (error) {
    const reason = error instanceof DeviceLocationError ? error.reason : 'unavailable';
    return {
      latitude: null,
      longitude: null,
      accuracy: null,
      locationCapturedAt: null,
      locationStatus: reason === 'permission' ? 'PERMISSAO_NEGADA' as const
        : reason === 'services-disabled' ? 'SERVICO_DESATIVADO' as const
          : reason === 'timeout' ? 'TIMEOUT' as const
            : 'INDISPONIVEL' as const,
      mocked: false,
    };
  }
}

function withTimeout<T>(promise: Promise<T>, timeoutMs: number) {
  return new Promise<T>((resolve, reject) => {
    const timeout = setTimeout(() => reject(new DeviceLocationError('timeout')), timeoutMs);
    promise.then(
      (value) => { clearTimeout(timeout); resolve(value); },
      (error) => { clearTimeout(timeout); reject(error); },
    );
  });
}
