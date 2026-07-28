import Constants from 'expo-constants';
import { Platform } from 'react-native';

import { applyArtificialApiDelay } from '@/config/apiConfig';
import { AUTH_TOKEN_KEY, getSessionItem } from '@/services/sessionStorage';

const DEFAULT_API_URL = 'http://localhost:8080';
const ANDROID_EMULATOR_HOST = '10.0.2.2';

export type ApiErrorPayload = {
  timestamp?: string;
  status?: number;
  message?: string;
  fields?: Record<string, string>;
};

export class ApiError extends Error {
  status: number;
  fields: Record<string, string>;

  constructor(message: string, status: number, fields: Record<string, string> = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fields = fields;
  }
}

type ExpoConstantsWithHost = typeof Constants & {
  experienceUrl?: string | null;
  expoGoConfig?: {
    debuggerHost?: string | null;
  } | null;
  linkingUri?: string | null;
  manifest2?: {
    extra?: {
      expoClient?: {
        hostUri?: string | null;
      } | null;
    } | null;
  } | null;
  platform?: {
    hostUri?: string | null;
  } | null;
};

function stripTrailingSlash(value: string) {
  return value.replace(/\/$/, '');
}

function isLoopbackHost(hostname: string) {
  return ['localhost', '127.0.0.1', '::1', '0.0.0.0'].includes(hostname.toLowerCase());
}

function getHostFromUri(uri?: string | null) {
  if (!uri) return null;

  const withoutProtocol = uri.replace(/^[a-z]+:\/\//i, '');
  const hostWithPort = withoutProtocol.split('/')[0];
  const host = hostWithPort.split(':')[0];

  return host || null;
}

function getExpoDevServerHost() {
  const constants = Constants as ExpoConstantsWithHost;

  return getHostFromUri(
    constants.expoConfig?.hostUri
      ?? constants.expoGoConfig?.debuggerHost
      ?? constants.platform?.hostUri
      ?? constants.linkingUri
      ?? constants.experienceUrl
      ?? constants.manifest2?.extra?.expoClient?.hostUri,
  );
}

function resolveApiBaseUrl(configuredUrl: string) {
  const normalizedUrl = stripTrailingSlash(configuredUrl.trim() || DEFAULT_API_URL);

  try {
    const url = new URL(normalizedUrl);

    if (!isLoopbackHost(url.hostname)) {
      return stripTrailingSlash(url.toString());
    }

    const expoHost = getExpoDevServerHost();
    const isPhysicalDeviceViaExpo = expoHost && !isLoopbackHost(expoHost) && expoHost !== ANDROID_EMULATOR_HOST;

    if (Platform.OS === 'android' && !Constants.isDevice) {
      url.hostname = ANDROID_EMULATOR_HOST;
      return stripTrailingSlash(url.toString());
    }

    if (isPhysicalDeviceViaExpo) {
      url.hostname = expoHost;
      return stripTrailingSlash(url.toString());
    }

    return normalizedUrl;
  } catch {
    return normalizedUrl;
  }
}

export const API_BASE_URL = resolveApiBaseUrl(process.env.EXPO_PUBLIC_API_URL ?? DEFAULT_API_URL);

type RequestOptions = Omit<RequestInit, 'body'> & {
  auth?: boolean;
  body?: unknown;
  token?: string | null;
};

function getErrorMessage(payload: ApiErrorPayload | null, status: number) {
  if (status === 401) return 'Sua sessão expirou. Faça login novamente.';
  if (status >= 500) return 'Não foi possível concluir agora. Tente novamente em instantes.';

  if (payload?.fields) {
    const firstFieldMessage = Object.values(payload.fields)[0];
    if (firstFieldMessage) return firstFieldMessage;
  }

  if (payload?.message) return payload.message;

  return 'Não foi possível concluir a solicitação.';
}

async function readJson(response: Response) {
  const text = await response.text();
  if (!text) return null;

  try {
    return JSON.parse(text) as unknown;
  } catch {
    return null;
  }
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { auth = true, body, token, ...fetchOptions } = options;
  const headers = new Headers(options.headers);
  headers.set('Accept', 'application/json');

  const requestOptions: RequestInit = {
    ...fetchOptions,
    headers,
  };

  if (body !== undefined) {
    if (body instanceof FormData) {
      requestOptions.body = body;
    } else {
      headers.set('Content-Type', 'application/json');
      requestOptions.body = JSON.stringify(body);
    }
  }

  const authToken = token ?? (auth ? await getSessionItem(AUTH_TOKEN_KEY) : null);
  if (authToken) {
    headers.set('Authorization', `Bearer ${authToken}`);
  }

  let response: Response;
  try {
    await applyArtificialApiDelay();
    response = await fetch(`${API_BASE_URL}${path}`, requestOptions);
  } catch {
    throw new ApiError('Não foi possível conectar ao servidor.', 0);
  }

  const payload = await readJson(response);

  if (!response.ok) {
    const errorPayload = payload as ApiErrorPayload | null;
    throw new ApiError(
      getErrorMessage(errorPayload, response.status),
      response.status,
      errorPayload?.fields ?? {},
    );
  }

  return payload as T;
}

export async function apiImageDataUrl(path: string): Promise<string> {
  const token = await getSessionItem(AUTH_TOKEN_KEY);
  const headers = new Headers({ Accept: 'image/jpeg,image/png,image/webp' });
  if (token) headers.set('Authorization', `Bearer ${token}`);
  let response: Response;
  const url = /^https?:\/\//i.test(path)
    ? path
    : `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`;
  try { response = await fetch(url, { headers }); }
  catch { throw new ApiError('Não foi possível carregar esta foto.', 0); }
  if (!response.ok) throw new ApiError('Não foi possível carregar esta foto.', response.status);
  const blob = await response.blob();
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => typeof reader.result === 'string' ? resolve(reader.result) : reject(new ApiError('Não foi possível carregar esta foto.', 0));
    reader.onerror = () => reject(new ApiError('Não foi possível carregar esta foto.', 0));
    reader.readAsDataURL(blob);
  });
}
