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
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { NextCareCard } from '@/components/home/next-care-card';
import { QuickAccessGrid, type QuickAccessItem } from '@/components/home/quick-access-grid';
import { WellnessTipCard } from '@/components/home/wellness-tip-card';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { getCaregiverDayTasks, getResponsibleDayCareOccurrences } from '@/services/careTaskService';
import { getUnreadNotificationCount } from '@/services/notificationService';
import { subscribeNotificationsChanged } from '@/services/notificationEvents';
import { colors, fontFamily, shadows, spacing } from '@/theme/tokens';
import type { TaskOccurrence } from '@/types/careTasks';
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
  const today = todayDateOnly();
  const dayCareRoute = (isCaregiver ? `/caregiver-tasks?date=${today}` : `/responsible-care-occurrences?date=${today}`) as Href;

  const loadNextCare = useCallback(async () => {
    if (!user) return;
    setCareLoading(true);
    setCareError(false);
    try {
      const date = todayDateOnly();
      const timezone = deviceTimezone();
      const result = isCaregiver
        ? await getCaregiverDayTasks(date, timezone)
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

  useEffect(() => subscribeNotificationsChanged(loadUnreadCount), [loadUnreadCount]);

  useFocusEffect(useCallback(() => {
    let active = true;
    void loadNextCare();
    if (user) getUnreadNotificationCount().then((result) => { if (active) setUnreadCount(result.count); }).catch(() => { if (active) setUnreadCount(0); });
    return () => { active = false; };
  }, [loadNextCare, user]));

  const quickAccessItems = useMemo<QuickAccessItem[]>(() => isCaregiver ? [
    { title: 'Buscar serviços', description: 'Encontre oportunidades de cuidado disponíveis.', icon: Search, iconColor: '#287A4B', iconBackground: '#E1F4EC', onPress: () => router.push(routes.opportunities) },
    { title: 'Solicitações', description: 'Avalie novos pedidos de cuidado.', icon: ClipboardList, iconColor: '#236FA0', iconBackground: '#DCEFFA', onPress: () => router.push(routes.requests) },
    { title: 'Agenda', description: 'Confira seus próximos atendimentos.', icon: CalendarDays, iconColor: '#A9573C', iconBackground: '#FCE9E1', onPress: () => router.push(routes.agenda) },
    { title: 'Cuidados de hoje', description: 'Visualize e registre os cuidados do dia.', icon: ClipboardCheck, iconColor: '#76611B', iconBackground: '#FAF1C9', onPress: () => router.push(dayCareRoute) },
  ] : [
    { title: 'Serviços', description: 'Publique oportunidades ou encontre cuidadores.', icon: BriefcaseBusiness, iconColor: '#236FA0', iconBackground: '#DCEFFA', onPress: () => router.push(routes.search) },
    { title: 'Contratações', description: 'Acompanhe seus serviços contratados.', icon: Handshake, iconColor: '#287A4B', iconBackground: '#E1F4EC', onPress: () => router.push(routes.contracts) },
    { title: 'Agenda', description: 'Veja os atendimentos programados.', icon: CalendarDays, iconColor: '#A9573C', iconBackground: '#FCE9E1', onPress: () => router.push(routes.agenda) },
    { title: 'Cuidados do dia', description: 'Acompanhe os cuidados previstos e realizados.', icon: ClipboardCheck, iconColor: '#76611B', iconBackground: '#FAF1C9', onPress: () => router.push(dayCareRoute) },
  ], [dayCareRoute, isCaregiver]);

  function openCare() {
    router.push(nextCare ? (`/task-occurrence/${nextCare.id}` as Href) : dayCareRoute);
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

      <NextCareCard care={nextCare} error={careError} isCaregiver={isCaregiver} loading={careLoading} onPress={openCare} onRetry={() => void loadNextCare()} />

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
