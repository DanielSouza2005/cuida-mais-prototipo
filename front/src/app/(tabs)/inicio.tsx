import { useCallback, useEffect, useMemo, useState } from 'react';
import { router, useFocusEffect, type Href } from 'expo-router';
import {
  Bell,
  BriefcaseBusiness,
  CalendarDays,
  ClipboardCheck,
  ClipboardList,
  Handshake,
  Search,
} from 'lucide-react-native';
import { Alert, Pressable, StyleSheet, Text, View } from 'react-native';

import { AttendanceCard } from '@/components/home/attendance-card';
import { NextCareCard } from '@/components/home/next-care-card';
import { QuickAccessGrid, type QuickAccessItem } from '@/components/home/quick-access-grid';
import { WellnessTipCard } from '@/components/home/wellness-tip-card';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { getCaregiverDayTasks, getResponsibleDayCareOccurrences } from '@/services/careTaskService';
import { getUnreadNotificationCount } from '@/services/notificationService';
import { subscribeNotificationsChanged } from '@/services/notificationEvents';
import { ApiError } from '@/services/api';
import { captureCurrentLocation, DeviceLocationError } from '@/services/deviceLocationService';
import { AttendanceResponseError, endAttendance, getTodayAttendance, startAttendance } from '@/services/serviceAttendanceService';
import { generateAttendanceReport } from '@/services/attendanceReportService';
import { colors, fontFamily, shadows, spacing } from '@/theme/tokens';
import type { TaskOccurrence } from '@/types/careTasks';
import type { AttendanceSummary } from '@/types/serviceAttendance';
import { todayDateOnly } from '@/utils/agendaDate';
import { deviceTimezone } from '@/utils/careTaskLabels';
import { getWellnessTip } from '@/utils/wellnessTips';

const routes = {
  requests: '/(tabs)/solicitacoes' as Href,
  search: '/(tabs)/buscar' as Href,
  contracts: '/(tabs)/contratacoes' as Href,
  agenda: '/(tabs)/agenda' as Href,
  opportunities: '/service-opportunities' as Href,
} as const;

export default function HomeScreen() {
  const { user } = useAuth();
  const isCaregiver = user?.userType === 'caregiver';
  const firstName = user?.fullName?.trim().split(/\s+/)[0] || 'você';
  const [unreadCount, setUnreadCount] = useState(0);
  const [nextCare, setNextCare] = useState<TaskOccurrence | null>(null);
  const [careLoading, setCareLoading] = useState(true);
  const [careError, setCareError] = useState(false);
  const [attendance, setAttendance] = useState<AttendanceSummary | null>(null);
  const [careContractId, setCareContractId] = useState<string | undefined>();
  const [attendanceLoading, setAttendanceLoading] = useState(isCaregiver);
  const [attendanceError, setAttendanceError] = useState<string | null>(null);
  const [attendanceActionLoading, setAttendanceActionLoading] = useState(false);
  const today = todayDateOnly();
  const dayCareRoute = (isCaregiver
    ? `/caregiver-tasks?date=${today}${careContractId ? `&contractId=${careContractId}` : ''}`
    : `/responsible-care-occurrences?date=${today}`) as Href;

  const loadNextCare = useCallback(async (contractId?: string) => {
    if (!user) return;
    setCareLoading(true);
    setCareError(false);
    try {
      const date = todayDateOnly();
      const timezone = deviceTimezone();
      const result = isCaregiver
        ? await getCaregiverDayTasks(date, timezone, { contractId })
        : await getResponsibleDayCareOccurrences(date, timezone);
      setNextCare(selectNextCare(result.content));
    } catch {
      setCareError(true);
    } finally {
      setCareLoading(false);
    }
  }, [isCaregiver, user]);

  const loadUnreadCount = useCallback(() => {
    if (!user) return;
    getUnreadNotificationCount().then((result) => setUnreadCount(result.count)).catch(() => setUnreadCount(0));
  }, [user]);

  const loadAttendance = useCallback(async () => {
    if (!isCaregiver) return;
    setAttendanceLoading(true);
    setAttendanceError(null);
    try {
      const response = await getTodayAttendance();
      const selected = selectAttendance(response.content);
      const careContext = selectCareAttendance(response.content);
      setAttendance(selected);
      setCareContractId(careContext?.contractId);
      if (careContext) void loadNextCare(careContext.contractId);
      else {
        setNextCare(null);
        setCareError(false);
        setCareLoading(false);
      }
    } catch (cause) {
      setAttendance(null);
      setCareContractId(undefined);
      setNextCare(null);
      setCareError(true);
      setCareLoading(false);
      setAttendanceError(attendanceLoadMessage(cause));
      if (__DEV__) {
        console.warn('[service-attendance] Falha ao carregar o resumo.', cause instanceof ApiError
          ? { type: 'api', status: cause.status }
          : { type: cause instanceof AttendanceResponseError ? 'mapping' : 'unexpected' });
      }
    } finally {
      setAttendanceLoading(false);
    }
  }, [isCaregiver, loadNextCare]);

  useEffect(() => subscribeNotificationsChanged(loadUnreadCount), [loadUnreadCount]);

  useFocusEffect(useCallback(() => {
    let active = true;
    if (isCaregiver) void loadAttendance();
    else void loadNextCare();
    if (user) getUnreadNotificationCount().then((result) => { if (active) setUnreadCount(result.count); }).catch(() => { if (active) setUnreadCount(0); });
    return () => { active = false; };
  }, [isCaregiver, loadAttendance, loadNextCare, user]));

  const quickAccessItems = useMemo<QuickAccessItem[]>(() => isCaregiver ? [
    { title: 'Buscar serviços', description: 'Encontre oportunidades de cuidado disponíveis.', icon: Search, iconColor: '#287A4B', iconBackground: '#E1F4EC', onPress: () => router.push(routes.opportunities) },
    { title: 'Solicitações', description: 'Avalie novos pedidos de cuidado.', icon: ClipboardList, iconColor: '#236FA0', iconBackground: '#DCEFFA', onPress: () => router.push(routes.requests) },
    { title: 'Agenda', description: 'Confira seus próximos atendimentos.', icon: CalendarDays, iconColor: '#A9573C', iconBackground: '#FCE9E1', onPress: () => router.push(routes.agenda) },
    { title: 'Cuidados do dia', description: 'Visualize os cuidados e registre quando o atendimento estiver em andamento.', icon: ClipboardCheck, iconColor: '#76611B', iconBackground: '#FAF1C9', onPress: () => router.push(dayCareRoute) },
  ] : [
    { title: 'Serviços', description: 'Publique oportunidades ou encontre cuidadores.', icon: BriefcaseBusiness, iconColor: '#236FA0', iconBackground: '#DCEFFA', onPress: () => router.push(routes.search) },
    { title: 'Contratações', description: 'Acompanhe seus serviços contratados.', icon: Handshake, iconColor: '#287A4B', iconBackground: '#E1F4EC', onPress: () => router.push(routes.contracts) },
    { title: 'Agenda', description: 'Veja os atendimentos programados.', icon: CalendarDays, iconColor: '#A9573C', iconBackground: '#FCE9E1', onPress: () => router.push(routes.agenda) },
    { title: 'Cuidados do dia', description: 'Acompanhe os cuidados previstos e realizados.', icon: ClipboardCheck, iconColor: '#76611B', iconBackground: '#FAF1C9', onPress: () => router.push(dayCareRoute) },
  ], [dayCareRoute, isCaregiver]);

  function openCare() {
    router.push(nextCare ? (`/task-occurrence/${nextCare.id}` as Href) : dayCareRoute);
  }

  async function recordAttendance() {
    if (!attendance || (!attendance.canStart && !attendance.canEnd)) return;
    const wasStarting = attendance.canStart;
    setAttendanceActionLoading(true);
    try {
      const location = await captureCurrentLocation();
      const payload = { ...location, attendanceDate: attendance.attendanceDate, deviceTimezone: deviceTimezone() };
      const updated = wasStarting
        ? await startAttendance(attendance.contractId, payload)
        : await endAttendance(attendance.contractId, payload);
      setAttendance(updated.status === 'ENDED' ? null : updated);
      setCareContractId(attendance.contractId);
      if (wasStarting) Alert.alert('Atendimento iniciado', 'O início e a localização foram registrados.');
      else {
        try {
          const report = await generateAttendanceReport(attendance.contractId, attendance.attendanceDate);
          router.push(`/attendance-report/${report.id}` as Href);
        } catch {
          router.push(`/attendance-report/retry?contractId=${attendance.contractId}&date=${attendance.attendanceDate}` as Href);
        }
      }
      if (wasStarting) void loadNextCare(attendance.contractId);
      if (!wasStarting) void loadAttendance();
    } catch (cause) {
      if (cause instanceof DeviceLocationError) {
        const message = cause.reason === 'permission'
          ? 'Para registrar o atendimento, permita o acesso à localização.'
          : cause.reason === 'mocked'
            ? 'Não é possível registrar o atendimento com uma localização simulada.'
            : cause.reason === 'stale'
              ? 'Não foi possível obter uma localização recente. Tente novamente em alguns instantes.'
              : 'Não foi possível obter sua localização. Tente novamente.';
        Alert.alert(cause.reason === 'permission' ? 'Permissão de localização necessária' : 'Localização indisponível', message);
      } else {
        Alert.alert('Não foi possível registrar o atendimento', cause instanceof ApiError ? cause.message : 'Tente novamente.');
      }
    } finally {
      setAttendanceActionLoading(false);
    }
  }

  return (
    <ScreenContainer contentStyle={styles.content}>
      <View style={styles.header}>
        <View style={styles.headerCopy}>
          <Text style={styles.greeting}>Olá, {firstName}</Text>
          <Text style={styles.subtitle}>Bem-vindo ao Cuidar+</Text>
        </View>
        {user ? (
          <Pressable accessibilityRole="button" accessibilityLabel="Notificações" onPress={() => router.push('/caregiver-notifications' as Href)} style={styles.notificationButton}>
            <Bell color={colors.primary} size={22} />
            {unreadCount > 0 ? <View style={styles.badge}><Text style={styles.badgeText}>{unreadCount > 99 ? '99+' : unreadCount}</Text></View> : null}
          </Pressable>
        ) : null}
      </View>

      {isCaregiver ? <AttendanceCard
        attendance={attendance}
        loading={attendanceLoading}
        error={attendanceError}
        actionLoading={attendanceActionLoading}
        nextCare={nextCare}
        careLoading={careLoading}
        careError={careError}
        onAction={() => void recordAttendance()}
        onCarePress={openCare}
        onCareRetry={() => void loadNextCare()}
        onDetails={() => attendance && router.push(`/agenda-event/${attendance.contractId}?eventDate=${attendance.attendanceDate}` as Href)}
        onManualCare={() => attendance && router.push({
          pathname: '/add-manual-care',
          params: { contractId: attendance.contractId, date: attendance.attendanceDate },
        })}
        onRetry={() => void loadAttendance()}
      /> : <NextCareCard care={nextCare} error={careError} isCaregiver={false} loading={careLoading} onPress={openCare} onRetry={() => void loadNextCare()} />}

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Acesso rápido</Text>
        <QuickAccessGrid items={quickAccessItems} />
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Área do bem-estar</Text>
        <WellnessTipCard tip={getWellnessTip()} />
      </View>
    </ScreenContainer>
  );
}

function selectAttendance(items: AttendanceSummary[]) {
  const rank: Record<AttendanceSummary['status'], number> = { CAN_END: 0, IN_PROGRESS: 1, CAN_START: 2, NOT_STARTED: 3, OUTSIDE_WINDOW: 4, MISSED: 5, ENDED: 6 };
  return items.filter((item) => item.status !== 'ENDED').sort((first, second) => rank[first.status] - rank[second.status] || first.scheduledStartTime.localeCompare(second.scheduledStartTime))[0] ?? null;
}

function selectCareAttendance(items: AttendanceSummary[]) {
  const rank: Record<AttendanceSummary['status'], number> = { CAN_END: 0, IN_PROGRESS: 1, CAN_START: 2, NOT_STARTED: 3, ENDED: 4, OUTSIDE_WINDOW: 5, MISSED: 6 };
  return [...items].sort((first, second) => rank[first.status] - rank[second.status]
    || (second.endRecord?.recordedAt ?? '').localeCompare(first.endRecord?.recordedAt ?? '')
    || first.scheduledStartTime.localeCompare(second.scheduledStartTime))[0] ?? null;
}

function attendanceLoadMessage(cause: unknown) {
  if (cause instanceof AttendanceResponseError) return 'Os dados do atendimento vieram em um formato inesperado. Tente novamente.';
  if (!(cause instanceof ApiError)) return 'Não foi possível carregar o atendimento. Tente novamente.';
  if (cause.status === 0) return 'Não foi possível conectar ao servidor para carregar o atendimento.';
  if (cause.status === 401) return 'Sua sessão expirou. Entre novamente para carregar o atendimento.';
  if (cause.status === 403) return 'Você não tem permissão para visualizar este atendimento.';
  if (cause.status === 404) return 'O atendimento solicitado não foi encontrado.';
  if (cause.status >= 500) return 'O servidor não conseguiu carregar o atendimento agora. Tente novamente em instantes.';
  return cause.message || 'Não foi possível carregar o atendimento. Tente novamente.';
}

function selectNextCare(items: TaskOccurrence[]) {
  return [...items]
    .filter((item) => item.status === 'ATRASADA' || item.status === 'PENDENTE')
    .sort((first, second) => {
      const statusDifference = Number(first.status !== 'ATRASADA') - Number(second.status !== 'ATRASADA');
      if (statusDifference !== 0) return statusDifference;
      return first.scheduledInstantUtc.localeCompare(second.scheduledInstantUtc);
    })[0] ?? null;
}

const styles = StyleSheet.create({
  content: { gap: spacing.xl, paddingHorizontal: spacing.xl, paddingBottom: spacing.xxl },
  header: { paddingTop: spacing.sm, flexDirection: 'row', alignItems: 'flex-start', justifyContent: 'space-between', gap: spacing.md },
  headerCopy: { flex: 1 },
  greeting: { fontFamily: fontFamily.extraBold, fontSize: 26, lineHeight: 32, color: colors.foreground },
  subtitle: { marginTop: spacing.xs, fontFamily: fontFamily.regular, fontSize: 14, color: colors.mutedForeground },
  notificationButton: { width: 46, height: 46, alignItems: 'center', justifyContent: 'center', borderRadius: 23, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card },
  badge: { position: 'absolute', top: -4, right: -5, minWidth: 20, height: 20, alignItems: 'center', justifyContent: 'center', paddingHorizontal: 5, borderRadius: 10, backgroundColor: colors.destructive },
  badgeText: { fontFamily: fontFamily.bold, fontSize: 9, color: colors.primaryForeground },
  section: { gap: spacing.md },
  sectionTitle: { fontFamily: fontFamily.extraBold, fontSize: 18, color: colors.foreground },
});
