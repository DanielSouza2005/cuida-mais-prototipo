import {
  AlarmClock,
  Ban,
  CalendarCheck,
  CalendarClock,
  CalendarX,
  CircleCheck,
  CircleX,
  ClipboardPlus,
  ClockAlert,
  Flag,
  HeartOff,
  HeartPlus,
  Info,
  ShieldAlert,
  TriangleAlert,
  type LucideIcon,
} from 'lucide-react-native';

type NotificationVisual = {
  icon: LucideIcon;
  color: string;
  backgroundColor: string;
  label: string;
  category: string;
};

const visuals: Record<string, NotificationVisual> = {
  SERVICE_REQUEST_CREATED: visual(ClipboardPlus, '#2477B3', '#E7F2FA', 'Nova solicitação', 'Solicitações'),
  SERVICE_REQUEST_ACCEPTED: visual(CircleCheck, '#25875A', '#E5F5EC', 'Solicitação aceita', 'Solicitações'),
  SERVICE_REQUEST_REJECTED: visual(CircleX, '#C4423A', '#FCEAE8', 'Solicitação rejeitada', 'Solicitações'),
  SERVICE_REQUEST_CANCELED: visual(Ban, '#8C4A4A', '#F5EDED', 'Solicitação cancelada', 'Solicitações'),
  SERVICE_REQUEST_EXPIRED: visual(ClockAlert, '#B76518', '#FFF0DF', 'Solicitação expirada', 'Solicitações'),
  CONTRACT_TERMINATION_SCHEDULED: visual(CalendarClock, '#B57413', '#FFF4DC', 'Encerramento agendado', 'Contratações'),
  CONTRACT_TERMINATED: visual(Flag, '#7254A3', '#F0EBF8', 'Serviço encerrado', 'Contratações'),
  CONTRACT_CANCELED_BEFORE_START: visual(CalendarX, '#B93655', '#FBE8EE', 'Contratação cancelada', 'Contratações'),
  CONTRACT_AUTOMATICALLY_TERMINATED: visual(CalendarCheck, '#5262A8', '#EBEDFA', 'Encerramento automático', 'Contratações'),
  CARE_TASK_CREATED: visual(HeartPlus, '#157D91', '#E3F5F7', 'Novo cuidado', 'Cuidados'),
  CARE_TASK_CANCELED: visual(HeartOff, '#B64F79', '#FAEAF1', 'Cuidado cancelado', 'Cuidados'),
  CARE_OCCURRENCE_REMINDER: visual(AlarmClock, '#2C86BD', '#E5F3FA', 'Lembrete de cuidado', 'Cuidados'),
  CARE_OCCURRENCE_OVERDUE: visual(ClockAlert, '#C45E17', '#FFF0E3', 'Cuidado atrasado', 'Cuidados'),
  CARE_OCCURRENCE_NOT_DONE: visual(TriangleAlert, '#C2342D', '#FCE7E5', 'Cuidado não realizado', 'Cuidados'),
  CARE_OCCURRENCE_COMPLETED: visual(CircleCheck, '#1E8B5C', '#E3F5EB', 'Cuidado concluído', 'Cuidados'),
  CARE_OCCURRENCE_RESPONSIBLE_ALERT: visual(ShieldAlert, '#B4770D', '#FFF3DA', 'Cuidado importante', 'Cuidados'),
};

const fallback = visual(Info, '#657681', '#EEF2F3', 'Notificação', 'Geral');

const legacyAliases: Record<string, string> = {
  TASK_OCCURRENCE_COMPLETED: 'CARE_OCCURRENCE_COMPLETED',
  TASK_OCCURRENCE_NOT_COMPLETED: 'CARE_OCCURRENCE_NOT_DONE',
  CARE_TASK_REMINDER: 'CARE_OCCURRENCE_REMINDER',
  CARE_TASK_OVERDUE: 'CARE_OCCURRENCE_OVERDUE',
  CARE_TASK_NOT_DONE: 'CARE_OCCURRENCE_NOT_DONE',
  CARE_TASK_RESPONSIBLE_ALERT: 'CARE_OCCURRENCE_RESPONSIBLE_ALERT',
};

function visual(icon: LucideIcon, color: string, backgroundColor: string, label: string, category: string): NotificationVisual {
  return { icon, color, backgroundColor, label, category };
}

export function getNotificationVisualConfig(type: string): NotificationVisual {
  return visuals[getCanonicalNotificationType(type)] ?? fallback;
}

export function getCanonicalNotificationType(type: string): string {
  return legacyAliases[type] ?? type;
}
