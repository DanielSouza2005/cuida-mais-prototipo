export type AttendanceReportStatus = 'DRAFT' | 'FINALIZED';
export type AttendanceReportEmailStatus = 'NOT_SENT' | 'PENDING' | 'SENT' | 'FAILED';

export type AttendanceReport = {
  id: string;
  contractId: string;
  attendanceDate: string;
  assistedPersonName: string;
  caregiverName: string;
  scheduledStartTime: string;
  scheduledEndTime: string;
  startedAt: string;
  endedAt: string;
  generatedText: string;
  editableText: string;
  finalText?: string | null;
  additionalNotes?: string | null;
  nursingNotes: string;
  status: AttendanceReportStatus;
  statusLabel: string;
  emailStatus: AttendanceReportEmailStatus;
  generatedAt: string;
  editedAt?: string | null;
  finalizedAt?: string | null;
};

export type UpdateAttendanceReport = { editedText: string; additionalNotes?: string | null };
