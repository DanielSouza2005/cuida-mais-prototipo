import { ApiError, apiRequest } from '@/services/api';
import type { AttendanceLocationPayload, AttendanceSummary, TodayAttendanceResponse } from '@/types/serviceAttendance';

const attendanceStatuses = new Set<AttendanceSummary['status']>([
  'NOT_STARTED', 'CAN_START', 'IN_PROGRESS', 'CAN_END', 'ENDED', 'OUTSIDE_WINDOW', 'MISSED',
]);

export class AttendanceResponseError extends Error {
  constructor() {
    super('A resposta do atendimento está em um formato incompatível.');
    this.name = 'AttendanceResponseError';
  }
}

export async function getTodayAttendance() {
  const endpoint = '/api/caregiver/today-attendance';
  try {
    const response = await apiRequest<unknown>(endpoint);
    if (__DEV__) console.debug('[service-attendance]', { endpoint, status: 200, payload: summarizeTodayResponse(response) });
    if (!isObject(response) || !Array.isArray(response.content) || !response.content.every(isAttendanceSummary)) {
      throw new AttendanceResponseError();
    }
    return response as TodayAttendanceResponse;
  } catch (cause) {
    if (__DEV__) console.warn('[service-attendance]', {
      endpoint,
      status: cause instanceof ApiError ? cause.status : 200,
      payload: cause instanceof AttendanceResponseError ? 'incompatible-response' : 'unavailable',
    });
    throw cause;
  }
}

export async function getContractAttendance(contractId: string, date: string) {
  const query = new URLSearchParams({ date });
  return requireAttendanceSummary(await apiRequest<unknown>(`/api/contracts/${contractId}/attendance?${query.toString()}`));
}

export async function startAttendance(contractId: string, payload: AttendanceLocationPayload) {
  return requireAttendanceSummary(await apiRequest<unknown>(`/api/caregiver/contracts/${contractId}/attendance/start`, { method: 'POST', body: payload }));
}

export async function endAttendance(contractId: string, payload: AttendanceLocationPayload) {
  return requireAttendanceSummary(await apiRequest<unknown>(`/api/caregiver/contracts/${contractId}/attendance/end`, { method: 'POST', body: payload }));
}

function requireAttendanceSummary(value: unknown) {
  if (!isAttendanceSummary(value)) throw new AttendanceResponseError();
  return value;
}

function isObject(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

function isAttendanceSummary(value: unknown): value is AttendanceSummary {
  if (!isObject(value)) return false;
  return typeof value.contractId === 'string'
    && typeof value.attendanceDate === 'string'
    && typeof value.assistedPersonName === 'string'
    && typeof value.scheduledStartTime === 'string'
    && typeof value.scheduledEndTime === 'string'
    && typeof value.status === 'string'
    && attendanceStatuses.has(value.status as AttendanceSummary['status'])
    && typeof value.statusLabel === 'string'
    && typeof value.canStart === 'boolean'
    && typeof value.canEnd === 'boolean'
    && typeof value.actionMessage === 'string';
}

function summarizeTodayResponse(value: unknown) {
  if (!isObject(value)) return typeof value;
  if (!Array.isArray(value.content)) return 'object-without-content-array';
  return `object{content:array[${value.content.length}]}`;
}
