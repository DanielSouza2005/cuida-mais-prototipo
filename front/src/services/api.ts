const DEFAULT_API_URL = 'http://localhost:8080';

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

export const API_BASE_URL = (process.env.EXPO_PUBLIC_API_URL ?? DEFAULT_API_URL).replace(/\/$/, '');

type RequestOptions = Omit<RequestInit, 'body'> & {
  body?: unknown;
  token?: string | null;
};

function getErrorMessage(payload: ApiErrorPayload | null, status: number) {
  if (payload?.fields) {
    const firstFieldMessage = Object.values(payload.fields)[0];
    if (firstFieldMessage) return firstFieldMessage;
  }

  if (payload?.message) return payload.message;

  if (status === 401) return 'E-mail ou senha invalidos.';
  if (status >= 500) return 'Nao foi possivel concluir agora. Tente novamente em instantes.';
  return 'Nao foi possivel concluir a solicitacao.';
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
  const { body, token, ...fetchOptions } = options;
  const headers = new Headers(options.headers);
  headers.set('Accept', 'application/json');

  const requestOptions: RequestInit = {
    ...fetchOptions,
    headers,
  };

  if (body !== undefined) {
    headers.set('Content-Type', 'application/json');
    requestOptions.body = JSON.stringify(body);
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, requestOptions);
  } catch {
    throw new ApiError('Nao foi possivel conectar ao servidor.', 0);
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
