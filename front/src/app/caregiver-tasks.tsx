import { useCallback, useRef, useState } from 'react';
import { router, useFocusEffect, useLocalSearchParams, useSegments, type Href } from 'expo-router';
import { ChevronLeft, ChevronRight, ClipboardCheck, Plus, RefreshCw } from 'lucide-react-native';
import { Pressable, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { AppHeader } from '@/components/app-header';
import { CareDiaryItemCard } from '@/components/care-task-card';
import { DatePickerField } from '@/components/date-picker-field';
import { OptionGroup } from '@/components/option-group';
import { ScreenContainer } from '@/components/screen-container';
import { getCaregiverDiary } from '@/services/careTaskService';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';
import type { CareDiaryItem, TaskOccurrenceStatus } from '@/types/careTasks';
import { addDays, todayDateOnly } from '@/utils/agendaDate';
import { deviceTimezone, taskOccurrenceStatusLabels } from '@/utils/careTaskLabels';
import { displayDateToIso, isoDateToDisplay } from '@/utils/contractTerminationLabels';

const filters: (TaskOccurrenceStatus | undefined)[] = [undefined, 'PENDENTE', 'ATRASADA', 'CONCLUIDA', 'NAO_REALIZADA'];

export default function CaregiverTasksScreen() {
  const segments = useSegments();
  const isTabRoute = (segments as string[])[0] === '(tabs)';
  const params = useLocalSearchParams<{ date?: string; contractId?: string }>();
  const initialDate = typeof params.date === 'string' ? params.date : todayDateOnly();
  const contractId = typeof params.contractId === 'string' ? params.contractId : undefined;
  const [date, setDate] = useState(isoDateToDisplay(initialDate));
  const [status, setStatus] = useState<TaskOccurrenceStatus>();
  const [assistedPersonId, setAssistedPersonId] = useState('TODAS');
  const [people, setPeople] = useState<{ value: string; label: string }[]>([]);
  const [items, setItems] = useState<CareDiaryItem[] | null>(null);
  const [error, setError] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const requestSequence = useRef(0);
  const insets = useSafeAreaInsets();

  const load = useCallback(async (refresh = false) => {
    const requestId = ++requestSequence.current;
    if (refresh) setRefreshing(true);
    setError(false);
    try {
      const content = (await getCaregiverDiary(displayDateToIso(date), deviceTimezone(), {
        assistedPersonId: assistedPersonId === 'TODAS' ? undefined : assistedPersonId,
        contractId,
      })).content;
      if (requestId !== requestSequence.current) return;
      setItems(content.filter((item) => matchesStatus(item, status)));
      setPeople((current) => [...new Map([...current, ...content.map((item) => ({ value: item.assistedPersonId, label: item.assistedPersonName }))].map((item) => [item.value, item])).values()]);
    } catch {
      if (requestId === requestSequence.current) setError(true);
    } finally {
      if (requestId === requestSequence.current) setRefreshing(false);
    }
  }, [assistedPersonId, contractId, date, status]);

  useFocusEffect(useCallback(() => {
    setItems(null);
    void load();
    return () => { requestSequence.current++; };
  }, [load]));

  function move(amount: number) { setDate(isoDateToDisplay(addDays(displayDateToIso(date), amount))); }
  function openManualForm() { router.push(`/add-manual-care?date=${displayDateToIso(date)}` as Href); }

  return <View style={styles.screen}><ScreenContainer contentStyle={styles.content} scrollViewProps={{ refreshControl: <RefreshControl refreshing={refreshing} onRefresh={() => void load(true)} tintColor={colors.primary} /> }}>
    <AppHeader showBack={!isTabRoute} title="Cuidados do dia" subtitle="Cuidados planejados e anotações do dia em ordem cronológica." />
    <View style={styles.dateNavigation}>
      <Pressable accessibilityLabel="Dia anterior" onPress={() => move(-1)} style={styles.dayButton}><ChevronLeft color={colors.primary} /></Pressable>
      <View style={styles.dateField}><DatePickerField label="Data" value={date} onChange={setDate} /></View>
      <Pressable accessibilityLabel="Próximo dia" onPress={() => move(1)} style={styles.dayButton}><ChevronRight color={colors.primary} /></Pressable>
    </View>
    <Pressable onPress={() => setDate(isoDateToDisplay(todayDateOnly()))} style={styles.todayButton}><Text style={styles.retry}>Hoje</Text></Pressable>
    <View style={styles.filters}>{filters.map((item) => <Pressable key={item ?? 'TODAS'} onPress={() => setStatus(item)} style={[styles.chip, status === item && styles.active]}><Text style={[styles.chipText, status === item && styles.activeText]}>{item ? taskOccurrenceStatusLabels[item] : 'Todos'}</Text></Pressable>)}</View>
    {people.length > 1 ? <View style={styles.advanced}><OptionGroup label="Pessoa assistida" options={[{ value: 'TODAS', label: 'Todas' }, ...people]} value={assistedPersonId} onChange={(value) => setAssistedPersonId(value as string)} /></View> : null}
    {items === null && !error ? <State text="Carregando cuidados..." /> : error ? <State text="Não foi possível carregar os cuidados." icon={<RefreshCw color={colors.destructive} />} action={() => void load()} /> : items?.length === 0 ? <State text="Nenhum cuidado ou anotação neste dia." icon={<ClipboardCheck color={colors.primary} />} /> : <View style={styles.list}>{items?.map((item) => <CareDiaryItemCard key={`${item.sourceType}-${item.id}`} item={item} onPress={() => router.push((item.sourceType === 'MANUAL' ? `/care-diary-entry/${item.id}` : `/task-occurrence/${item.id}`) as Href)} />)}</View>}
  </ScreenContainer>
    <Pressable
      accessibilityLabel="Adicionar cuidado avulso"
      accessibilityRole="button"
      hitSlop={8}
      onPress={openManualForm}
      style={({ pressed }) => [styles.fab, { bottom: Math.max(insets.bottom, spacing.lg) + spacing.lg }, pressed && styles.fabPressed]}
    >
      <Plus color={colors.primaryForeground} size={30} strokeWidth={2.6} />
    </Pressable>
  </View>;
}

function matchesStatus(item: CareDiaryItem, status?: TaskOccurrenceStatus) {
  if (!status) return true;
  if (item.sourceType === 'MANUAL') return status === 'CONCLUIDA';
  return item.status === status;
}
function State({ text, icon, action }: { text: string; icon?: React.ReactNode; action?: () => void }) { return <View style={styles.state}>{icon}<Text style={styles.stateText}>{text}</Text>{action ? <Pressable onPress={action}><Text style={styles.retry}>Tentar novamente</Text></Pressable> : null}</View>; }
const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: colors.background },
  content: { paddingHorizontal: spacing.xl, paddingBottom: 112, gap: spacing.lg },
  fab: { position: 'absolute', right: spacing.xl, width: 58, height: 58, alignItems: 'center', justifyContent: 'center', borderRadius: radii.full, backgroundColor: colors.primary, elevation: 6, shadowColor: colors.primary, shadowOpacity: 0.3, shadowRadius: 12, shadowOffset: { width: 0, height: 6 } },
  fabPressed: { opacity: 0.82, transform: [{ scale: 0.97 }] },
  dateNavigation: { flexDirection: 'row', alignItems: 'flex-end', gap: spacing.sm }, dateField: { flex: 1 }, dayButton: { width: 48, height: 54, alignItems: 'center', justifyContent: 'center', borderRadius: radii.lg, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card },
  todayButton: { alignSelf: 'center', paddingHorizontal: spacing.xl, paddingVertical: spacing.sm, borderRadius: radii.full, backgroundColor: colors.secondary }, filters: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm },
  chip: { minHeight: 38, justifyContent: 'center', paddingHorizontal: spacing.md, borderRadius: radii.full, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card }, active: { borderColor: colors.primary, backgroundColor: colors.secondary }, chipText: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground }, activeText: { color: colors.primary },
  advanced: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card }, list: { gap: spacing.md }, state: { minHeight: 220, alignItems: 'center', justifyContent: 'center', gap: spacing.md }, stateText: { textAlign: 'center', fontFamily: fontFamily.medium, color: colors.mutedForeground }, retry: { fontFamily: fontFamily.bold, color: colors.primary },
});
