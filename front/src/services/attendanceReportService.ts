import { apiRequest } from '@/services/api';
import type { AttendanceReport, UpdateAttendanceReport } from '@/types/attendanceReport';

export function generateAttendanceReport(contractId: string, date: string) {
  return apiRequest<AttendanceReport>(`/api/caregiver/contracts/${contractId}/attendance/${date}/report/generate`, { method: 'POST' });
}

export function getAttendanceReport(contractId: string, date: string) {
  return apiRequest<AttendanceReport>(`/api/contracts/${contractId}/attendance/${date}/report`);
}

export function getAttendanceReportById(reportId: string) {
  return apiRequest<AttendanceReport>(`/api/attendance-reports/${reportId}`);
}

export function updateAttendanceReport(contractId: string, date: string, payload: UpdateAttendanceReport) {
  return apiRequest<AttendanceReport>(`/api/caregiver/contracts/${contractId}/attendance/${date}/report`, { method: 'PUT', body: payload });
}

export function finalizeAttendanceReport(contractId: string, date: string, payload: UpdateAttendanceReport) {
  return apiRequest<AttendanceReport>(`/api/caregiver/contracts/${contractId}/attendance/${date}/report/finalize`, { method: 'POST', body: payload });
}
