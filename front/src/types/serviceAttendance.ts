export type AttendanceStatus =
  | 'NOT_STARTED'
  | 'CAN_START'
  | 'IN_PROGRESS'
  | 'CAN_END'
  | 'ENDED'
  | 'OUTSIDE_WINDOW'
  | 'MISSED';

export type AttendanceRecord = {
  id: string;
  type: 'START' | 'END';
  label: string;
  recordedAt: string;
  attendanceDate: string;
  latitude: number;
  longitude: number;
  accuracy: number;
  addressSnapshot?: string | null;
  deviceTimezone: string;
  scheduledStartTime: string;
  scheduledEndTime: string;
  allowedWindowStart: string;
  allowedWindowEnd: string;
  withinAllowedWindow: boolean;
};

export type AttendanceSummary = {
  contractId: string;
  attendanceDate: string;
  assistedPersonName: string;
  scheduledStartTime: string;
  scheduledEndTime: string;
  startWindowStart: string;
  startWindowEnd: string;
  endWindowStart: string;
  endWindowEnd: string;
  status: AttendanceStatus;
  statusLabel: string;
  canStart: boolean;
  canEnd: boolean;
  actionMessage: string;
  startRecord?: AttendanceRecord | null;
  endRecord?: AttendanceRecord | null;
};

export type AttendanceLocationPayload = {
  attendanceDate: string;
  latitude: number;
  longitude: number;
  accuracy: number;
  locationCapturedAt: string;
  mocked: boolean;
  deviceTimezone: string;
};

export type TodayAttendanceResponse = { content: AttendanceSummary[] };
