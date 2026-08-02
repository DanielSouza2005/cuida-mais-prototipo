import { useCallback, useRef, useState } from 'react';
import { router, useFocusEffect, useLocalSearchParams, type Href } from 'expo-router';
import { ChevronLeft, ChevronRight, ClipboardCheck, RefreshCw } from 'lucide-react-native';
import { Pressable, RefreshControl, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { CareDiaryItemCard } from '@/components/care-task-card';
import { DatePickerField } from '@/components/date-picker-field';
import { ScreenContainer } from '@/components/screen-container';
import { getResponsibleDiary } from '@/services/careTaskService';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';
import type { CareDiaryItem, TaskOccurrenceStatus } from '@/types/careTasks';
import { addDays, todayDateOnly } from '@/utils/agendaDate';
import { deviceTimezone, taskOccurrenceStatusLabels } from '@/utils/careTaskLabels';
import { displayDateToIso, isoDateToDisplay } from '@/utils/contractTerminationLabels';

const filters: (TaskOccurrenceStatus | undefined)[] = [undefined, 'PENDENTE', 'ATRASADA', 'CONCLUIDA', 'NAO_REALIZADA'];

export default function ResponsibleCareOccurrencesScreen() {
  const params = useLocalSearchParams<{ date?: string; contractId?: string }>();
  const initialDate = typeof params.date === 'string' ? params.date : todayDateOnly();
  const contractId = typeof params.contractId === 'string' ? params.contractId : undefined;
  const [date, setDate] = useState(isoDateToDisplay(initialDate));
  const [status, setStatus] = useState<TaskOccurrenceStatus>();
  const [items, setItems] = useState<CareDiaryItem[] | null>(null);
  const [error, setError] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const requestSequence = useRef(0);

  const load = useCallback(async (refresh = false) => {
    const requestId = ++requestSequence.current;
    if (refresh) setRefreshing(true);
    setError(false);
    try {
      const content = (await getResponsibleDiary(displayDateToIso(date), deviceTimezone(), { contractId })).content;
      if (requestId !== requestSequence.current) return;
      setItems(content.filter((item) => !status || (item.sourceType === 'MANUAL' ? status === 'CONCLUIDA' : item.status === status)));
    } catch {
      if (requestId === requestSequence.current) setError(true);
    } finally {
      if (requestId === requestSequence.current) setRefreshing(false);
    }
  }, [contractId, date, status]);

  useFocusEffect(useCallback(() => { setItems(null); void load(); return () => { requestSequence.current++; }; }, [load]));
  function move(amount: number) { setDate(isoDateToDisplay(addDays(displayDateToIso(date), amount))); }

  return <ScreenContainer contentStyle={styles.content} scrollViewProps={{ refreshControl: <RefreshControl refreshing={refreshing} onRefresh={() => void load(true)} tintColor={colors.primary} /> }}>
    <AppHeader showBack title="Diário da Pessoa Assistida" subtitle="Veja os cuidados realizados e anotações do dia em ordem cronológica." />
    <View style={styles.dateNavigation}><Pressable accessibilityLabel="Dia anterior" onPress={() => move(-1)} style={styles.dayButton}><ChevronLeft color={colors.primary} /></Pressable><View style={styles.dateField}><DatePickerField label="Data" value={date} onChange={setDate} /></View><Pressable accessibilityLabel="Próximo dia" onPress={() => move(1)} style={styles.dayButton}><ChevronRight color={colors.primary} /></Pressable></View>
    <Pressable onPress={() => setDate(isoDateToDisplay(todayDateOnly()))} style={styles.todayButton}><Text style={styles.retry}>Hoje</Text></Pressable>
    <View style={styles.filters}>{filters.map((item) => <Pressable key={item ?? 'TODOS'} onPress={() => setStatus(item)} style={[styles.chip, status === item && styles.active]}><Text style={[styles.chipText, status === item && styles.activeText]}>{item ? taskOccurrenceStatusLabels[item] : 'Todos'}</Text></Pressable>)}</View>
    {items === null && !error ? <State text="Carregando cuidados e anotações..." /> : error ? <State text="Não foi possível carregar o diário." icon={<RefreshCw color={colors.destructive} />} action={() => void load()} /> : items?.length === 0 ? <State text="Nenhum cuidado ou anotação neste dia." icon={<ClipboardCheck color={colors.primary} />} /> : <View style={styles.list}>{items?.map((item) => <CareDiaryItemCard key={`${item.sourceType}-${item.id}`} item={item} readOnly onPress={() => router.push((item.sourceType === 'MANUAL' ? `/care-diary-entry/${item.id}` : `/task-occurrence/${item.id}`) as Href)} />)}</View>}
  </ScreenContainer>;
}
function State({ text, icon, action }: { text: string; icon?: React.ReactNode; action?: () => void }) { return <View style={styles.state}>{icon}<Text style={styles.stateText}>{text}</Text>{action ? <Pressable onPress={action}><Text style={styles.retry}>Tentar novamente</Text></Pressable> : null}</View>; }
const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, gap: spacing.lg }, dateNavigation: { flexDirection: 'row', alignItems: 'flex-end', gap: spacing.sm }, dateField: { flex: 1 }, dayButton: { width: 48, height: 54, alignItems: 'center', justifyContent: 'center', borderRadius: radii.lg, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card },
  todayButton: { alignSelf: 'center', paddingHorizontal: spacing.xl, paddingVertical: spacing.sm, borderRadius: radii.full, backgroundColor: colors.secondary }, filters: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm }, chip: { minHeight: 38, justifyContent: 'center', paddingHorizontal: spacing.md, borderRadius: radii.full, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card }, active: { borderColor: colors.primary, backgroundColor: colors.secondary }, chipText: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground }, activeText: { color: colors.primary }, list: { gap: spacing.md }, state: { minHeight: 220, alignItems: 'center', justifyContent: 'center', gap: spacing.md }, stateText: { textAlign: 'center', fontFamily: fontFamily.medium, color: colors.mutedForeground }, retry: { fontFamily: fontFamily.bold, color: colors.primary },
});
