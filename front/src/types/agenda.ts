import type {
  ContractHistoryItem,
  ContractHistoryStatus,
  ContractHiringType,
} from '@/types/contractsHistory';
import type { AttendanceStatus, AttendanceSummary } from '@/types/serviceAttendance';

export type AgendaViewMode = 'DAY' | 'WEEK' | 'MONTH';

export type AgendaEvent = {
  id: string;
  contractId: string;
  title: string;
  description: string;
  startDateTime: string;
  endDateTime: string;
  eventDate: string;
  status: Extract<ContractHistoryStatus, 'AGENDADA' | 'ATIVA' | 'ENCERRAMENTO_AGENDADO'>;
  hiringType: ContractHiringType;
  participantName: string;
  participantPhotoUrl?: string | null;
  assistedPersonName: string;
  careAddressSummary?: string | null;
  sourceType: 'CARE_CONTRACT';
  hasScheduledTermination: boolean;
  effectiveEndDate?: string | null;
  attendanceStatus: AttendanceStatus;
  attendanceStatusLabel: string;
};

export type AgendaEventsResponse = { content: AgendaEvent[] };

export type AgendaEventDetails = {
  event: AgendaEvent;
  contract: ContractHistoryItem;
  attendance: AttendanceSummary;
};
