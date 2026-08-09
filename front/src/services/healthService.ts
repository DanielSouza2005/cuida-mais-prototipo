import { apiRequest } from '@/services/api';

const HEALTH_CHECK_TIMEOUT_MS = 8_000;

export type ApiHealth = {
  status: string;
  service: string;
  timestamp?: string;
};

export class HealthCheckTimeoutError extends Error {
  constructor() {
    super('Health check timed out.');
    this.name = 'HealthCheckTimeoutError';
  }
}

export async function checkApiHealth(): Promise<ApiHealth> {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), HEALTH_CHECK_TIMEOUT_MS);

  try {
    return await apiRequest<ApiHealth>('/health', {
      auth: false,
      method: 'GET',
      signal: controller.signal,
    });
  } catch (error) {
    if (controller.signal.aborted) {
      throw new HealthCheckTimeoutError();
    }

    throw error;
  } finally {
    clearTimeout(timeout);
  }
}
