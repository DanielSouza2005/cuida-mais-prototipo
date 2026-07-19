import { useCallback, useMemo, useState } from 'react';
import { router, useFocusEffect, type Href } from 'expo-router';
import { CalendarDays, ChevronLeft, ChevronRight, Clock3, MapPin, RefreshCw } from 'lucide-react-native';
import { ActivityIndicator, Pressable, RefreshControl, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { ContractStatusBadge } from '@/components/contract-history-card';
import { ScreenContainer } from '@/components/screen-container';
import { getAgendaEvents } from '@/services/agendaService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { AgendaEvent, AgendaViewMode } from '@/types/agenda';
import {
  agendaDayLabel,
  agendaPeriodLabel,
  agendaRange,
  datesBetween,
  isTodayDate,
  moveAgendaPeriod,
  todayDateOnly,
} from '@/utils/agendaDate';
import { contractHiringLabels } from '@/utils/contractsHistoryLabels';
import { formatScheduleTime } from '@/utils/dateTime';

const viewOptions: { value: AgendaViewMode; label: string }[] = [
  { value: 'DAY', label: 'Dia' },
  { value: 'WEEK', label: 'Semana' },
  { value: 'MONTH', label: 'Mês' },
];

export default function AgendaScreen() {
  const [viewMode, setViewMode] = useState<AgendaViewMode>('MONTH');
  const [anchor, setAnchor] = useState(todayDateOnly);
  const [events, setEvents] = useState<AgendaEvent[] | null>(null);
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const [error, setError] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const range = useMemo(() => agendaRange(anchor, viewMode), [anchor, viewMode]);

  const load = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    setError(false);
    try {
      const response = await getAgendaEvents(range.startDate, range.endDate, viewMode);
      setEvents(response.content);
      setHasLoadedOnce(true);
    } catch {
      setError(true);
    } finally {
      setRefreshing(false);
    }
  }, [range.endDate, range.startDate, viewMode]);

  useFocusEffect(useCallback(() => { void load(); }, [load]));

  const eventsByDate = useMemo(() => events?.reduce<Record<string, AgendaEvent[]>>((result, event) => {
    (result[event.eventDate] ??= []).push(event);
    return result;
  }, {}) ?? {}, [events]);

  const visibleDates = useMemo(() => {
    if (viewMode === 'DAY') return [range.startDate];
    if (viewMode === 'WEEK') return datesBetween(range.startDate, range.endDate);
    return Object.keys(eventsByDate).sort();
  }, [eventsByDate, range.endDate, range.startDate, viewMode]);

  function selectMode(mode: AgendaViewMode) {
    setEvents(null);
    setViewMode(mode);
  }

  function move(direction: -1 | 1) {
    setEvents(null);
    setAnchor((current) => moveAgendaPeriod(current, viewMode, direction));
  }

  return (
    <ScreenContainer
      contentStyle={styles.content}
      scrollViewProps={{
        refreshControl: <RefreshControl refreshing={refreshing} onRefresh={() => void load(true)} tintColor={colors.primary} />,
      }}
    >
      <AppHeader title="Agenda" subtitle="Organize seus serviços e compromissos de cuidado." />

      <View style={styles.segmented}>
        {viewOptions.map((option) => (
          <Pressable
            key={option.value}
            accessibilityRole="button"
            accessibilityState={{ selected: option.value === viewMode }}
            onPress={() => selectMode(option.value)}
            style={[styles.segment, option.value === viewMode && styles.segmentActive]}
          >
            <Text style={[styles.segmentText, option.value === viewMode && styles.segmentTextActive]}>{option.label}</Text>
          </Pressable>
        ))}
      </View>

      <View style={styles.periodCard}>
        <Pressable accessibilityRole="button" accessibilityLabel="Período anterior" onPress={() => move(-1)} style={styles.iconButton}>
          <ChevronLeft color={colors.primary} size={22} />
        </Pressable>
        <View style={styles.periodCopy}>
          <Text style={styles.periodLabel}>{agendaPeriodLabel(anchor, viewMode)}</Text>
          <Pressable accessibilityRole="button" accessibilityLabel="Ir para hoje" onPress={() => { setEvents(null); setAnchor(todayDateOnly()); }}>
            <Text style={styles.goToToday}>Ir para hoje</Text>
          </Pressable>
        </View>
        <Pressable accessibilityRole="button" accessibilityLabel="Próximo período" onPress={() => move(1)} style={styles.iconButton}>
          <ChevronRight color={colors.primary} size={22} />
        </Pressable>
      </View>

      {events === null && !error ? (
        <State icon={<ActivityIndicator color={colors.primary} />} title={hasLoadedOnce ? 'Atualizando agenda...' : 'Carregando agenda...'} />
      ) : error ? (
        <State
          icon={<RefreshCw color={colors.destructive} size={24} />}
          title="Não foi possível carregar a agenda."
          action="Tentar novamente"
          onPress={() => { setEvents(null); void load(); }}
        />
      ) : events?.length === 0 ? (
        <State icon={<CalendarDays color={colors.primary} size={28} />} title="Nenhum serviço agendado para este período." />
      ) : (
        <View style={styles.sections}>
          {visibleDates.map((date) => (
            <View key={date} style={styles.daySection}>
              <View style={styles.dayHeading}>
                <Text style={styles.dayTitle}>{agendaDayLabel(date)}</Text>
                {isTodayDate(date) ? <View style={styles.todayBadge}><Text style={styles.todayBadgeText}>Hoje</Text></View> : null}
              </View>
              {(eventsByDate[date] ?? []).length ? eventsByDate[date].map((event) => <EventCard key={event.id} event={event} />) : (
                <Text style={styles.noServices}>Sem serviços</Text>
              )}
            </View>
          ))}
        </View>
      )}
    </ScreenContainer>
  );
}

function EventCard({ event }: { event: AgendaEvent }) {
  const start = formatScheduleTime(event.startDateTime.slice(11));
  const end = formatScheduleTime(event.endDateTime.slice(11));
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={`${event.title}, ${start} às ${end}`}
      onPress={() => router.push(`/agenda-event/${event.contractId}?eventDate=${event.eventDate}` as Href)}
      style={({ pressed }) => [styles.eventCard, pressed && styles.pressed]}
    >
      <View style={styles.eventTop}>
        <View style={styles.timePill}><Clock3 color={colors.primary} size={15} /><Text style={styles.time}>{start} às {end}</Text></View>
        <ContractStatusBadge status={event.status} />
      </View>
      <Text style={styles.eventTitle}>{event.title}</Text>
      <Text style={styles.participant}>Com {event.participantName}</Text>
      <Text style={styles.hiringType}>{contractHiringLabels[event.hiringType]}</Text>
      {event.careAddressSummary ? <View style={styles.location}><MapPin color={colors.mutedForeground} size={14} /><Text style={styles.locationText}>{event.careAddressSummary}</Text></View> : null}
    </Pressable>
  );
}

function State({ icon, title, action, onPress }: { icon: React.ReactNode; title: string; action?: string; onPress?: () => void }) {
  return (
    <View style={styles.state}>
      {icon}
      <Text style={styles.stateText}>{title}</Text>
      {action ? <Pressable accessibilityRole="button" onPress={onPress}><Text style={styles.retry}>{action}</Text></Pressable> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, gap: spacing.lg },
  segmented: { flexDirection: 'row', padding: spacing.xxs, borderRadius: radii.lg, backgroundColor: colors.muted },
  segment: { flex: 1, minHeight: 42, alignItems: 'center', justifyContent: 'center', borderRadius: radii.md },
  segmentActive: { backgroundColor: colors.card, ...shadows.card },
  segmentText: { fontFamily: fontFamily.semiBold, fontSize: 13, color: colors.mutedForeground },
  segmentTextActive: { color: colors.primary },
  periodCard: { minHeight: 72, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', padding: spacing.sm, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card },
  iconButton: { width: 44, height: 44, alignItems: 'center', justifyContent: 'center', borderRadius: radii.full, backgroundColor: colors.secondary },
  periodCopy: { flex: 1, alignItems: 'center', gap: spacing.xxs },
  periodLabel: { textAlign: 'center', fontFamily: fontFamily.bold, fontSize: 15, color: colors.foreground },
  goToToday: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.primary },
  sections: { gap: spacing.lg },
  daySection: { gap: spacing.md },
  dayHeading: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: spacing.md },
  dayTitle: { fontFamily: fontFamily.extraBold, fontSize: 16, color: colors.foreground },
  todayBadge: { paddingHorizontal: spacing.sm, paddingVertical: spacing.xs, borderRadius: radii.full, backgroundColor: colors.mint },
  todayBadgeText: { fontFamily: fontFamily.bold, fontSize: 10, color: colors.mintForeground },
  noServices: { paddingVertical: spacing.md, paddingHorizontal: spacing.lg, borderRadius: radii.lg, backgroundColor: colors.muted, fontFamily: fontFamily.regular, fontSize: 12, color: colors.mutedForeground },
  eventCard: { gap: spacing.sm, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, borderLeftWidth: 4, borderLeftColor: colors.primary, backgroundColor: colors.card, ...shadows.card },
  pressed: { opacity: 0.82, transform: [{ scale: 0.99 }] },
  eventTop: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: spacing.sm },
  timePill: { flexDirection: 'row', alignItems: 'center', gap: spacing.xs },
  time: { fontFamily: fontFamily.bold, fontSize: 13, color: colors.primary },
  eventTitle: { fontFamily: fontFamily.extraBold, fontSize: 16, color: colors.foreground },
  participant: { fontFamily: fontFamily.medium, fontSize: 13, color: colors.secondaryForeground },
  hiringType: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground },
  location: { flexDirection: 'row', alignItems: 'center', gap: spacing.xs },
  locationText: { flex: 1, fontFamily: fontFamily.regular, fontSize: 11, color: colors.mutedForeground },
  state: { minHeight: 220, alignItems: 'center', justifyContent: 'center', gap: spacing.md, padding: spacing.xl },
  stateText: { textAlign: 'center', fontFamily: fontFamily.medium, fontSize: 14, lineHeight: 21, color: colors.mutedForeground },
  retry: { fontFamily: fontFamily.bold, fontSize: 13, color: colors.primary },
});
