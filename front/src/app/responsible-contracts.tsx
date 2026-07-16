import { useCallback, useRef, useState } from 'react';
import { useFocusEffect, useSegments } from 'expo-router';
import { Search, SlidersHorizontal } from 'lucide-react-native';
import { ActivityIndicator, Pressable, RefreshControl, StyleSheet, Text, TextInput, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { ContractHistoryCard } from '@/components/contract-history-card';
import { DatePickerField } from '@/components/date-picker-field';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { ApiError } from '@/services/api';
import { getResponsibleContracts } from '@/services/responsibleContractsService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { ContractHistoryCategory, ContractHistoryItem, ContractsHistoryPage } from '@/types/contractsHistory';
import { contractCategoryLabels } from '@/utils/contractsHistoryLabels';

const categories: ContractHistoryCategory[] = ['TODAS', 'PENDENTES', 'AGENDADAS', 'ATIVAS', 'ENCERRADAS', 'REJEITADAS', 'CANCELADAS', 'EXPIRADAS'];
const pageSize = 5;
type AppliedFilters = { participantName: string; startDateFrom: string; startDateTo: string };
const emptyFilters: AppliedFilters = { participantName: '', startDateFrom: '', startDateTo: '' };

function toIsoDate(value: string) {
  const [day, month, year] = value.split('/');
  return day && month && year ? `${year}-${month}-${day}` : '';
}

export default function ResponsibleContractsScreen() {
  const [category, setCategory] = useState<ContractHistoryCategory>('TODAS');
  const [draftFilters, setDraftFilters] = useState<AppliedFilters>(emptyFilters);
  const [appliedFilters, setAppliedFilters] = useState<AppliedFilters>(emptyFilters);
  const [items, setItems] = useState<ContractHistoryItem[]>([]);
  const [page, setPage] = useState<ContractsHistoryPage | null>(null);
  const [loadingMessage, setLoadingMessage] = useState('Carregando contratações...');
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [dateError, setDateError] = useState<string | null>(null);
  const requestVersion = useRef(0);
  const hasLoaded = useRef(false);
  const loadingMoreRef = useRef(false);
  const categoryRef = useRef(category);
  const filtersRef = useRef(appliedFilters);
  const segments = useSegments();
  const isTabRoute = (segments as string[])[0] === '(tabs)';

  const fetchFirstPage = useCallback(async (nextCategory: ContractHistoryCategory, filters: AppliedFilters, message: string, refresh = false) => {
    const version = ++requestVersion.current;
    setLoadingMessage(message); setError(null);
    if (refresh) setRefreshing(true); else setIsLoading(true);
    try {
      const result = await getResponsibleContracts({
        statusGroup: nextCategory === 'TODAS' ? undefined : nextCategory,
        participantName: filters.participantName,
        startDateFrom: toIsoDate(filters.startDateFrom), startDateTo: toIsoDate(filters.startDateTo), page: 0, size: pageSize,
      });
      if (version !== requestVersion.current) return;
      setItems(result.content); setPage(result);
      hasLoaded.current = true;
    } catch (reason) {
      if (version !== requestVersion.current) return;
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar as contratações. Tente novamente.');
      if (!hasLoaded.current) { setItems([]); setPage(null); }
    } finally { if (version === requestVersion.current) { setIsLoading(false); setRefreshing(false); } }
  }, []);

  useFocusEffect(useCallback(() => {
    void fetchFirstPage(categoryRef.current, filtersRef.current, hasLoaded.current ? 'Atualizando contratações...' : 'Carregando contratações...', hasLoaded.current);
    return () => { requestVersion.current += 1; };
  }, [fetchFirstPage]));

  function selectCategory(nextCategory: ContractHistoryCategory) {
    if (nextCategory === category || isLoading) return;
    setCategory(nextCategory);
    categoryRef.current = nextCategory;
    void fetchFirstPage(nextCategory, appliedFilters, 'Atualizando contratações...');
  }

  function applyFilters() {
    const from = toIsoDate(draftFilters.startDateFrom); const to = toIsoDate(draftFilters.startDateTo);
    if (from && to && to < from) { setDateError('A data final não pode ser anterior à data inicial.'); return; }
    setDateError(null); const next = { ...draftFilters, participantName: draftFilters.participantName.trim() };
    setAppliedFilters(next); filtersRef.current = next; void fetchFirstPage(category, next, 'Aplicando filtros...');
  }

  function clearFilters() {
    setDraftFilters(emptyFilters); setAppliedFilters(emptyFilters); filtersRef.current = emptyFilters; setDateError(null);
    void fetchFirstPage(category, emptyFilters, 'Aplicando filtros...');
  }

  async function loadMore() {
    if (!page || page.last || loadingMoreRef.current) return;
    const version = ++requestVersion.current;
    loadingMoreRef.current = true; setIsLoadingMore(true);
    try {
      const result = await getResponsibleContracts({
        statusGroup: category === 'TODAS' ? undefined : category,
        participantName: appliedFilters.participantName,
        startDateFrom: toIsoDate(appliedFilters.startDateFrom), startDateTo: toIsoDate(appliedFilters.startDateTo), page: page.page + 1, size: pageSize,
      });
      if (version !== requestVersion.current) return;
      setItems((current) => [...current, ...result.content.filter((entry) => !current.some((existing) => existing.id === entry.id))]); setPage(result);
    } catch (reason) { if (version === requestVersion.current) setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar mais registros. Tente novamente.'); }
    finally { loadingMoreRef.current = false; setIsLoadingMore(false); }
  }

  const hasActiveFilters = Boolean(appliedFilters.participantName || appliedFilters.startDateFrom || appliedFilters.startDateTo || category !== 'TODAS');

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content} scrollViewProps={{
      onScroll: ({ nativeEvent }) => { const distance = nativeEvent.contentSize.height - nativeEvent.layoutMeasurement.height - nativeEvent.contentOffset.y; if (distance < 180) void loadMore(); },
      scrollEventThrottle: 160,
      refreshControl: <RefreshControl refreshing={refreshing} onRefresh={() => void fetchFirstPage(categoryRef.current, filtersRef.current, 'Atualizando contratações...', true)} tintColor={colors.primary} colors={[colors.primary]} />,
    }}>
      <AppHeader showBack={!isTabRoute} title="Contratações" subtitle="Acompanhe suas solicitações e serviços contratados." />

      <View style={styles.categories} accessibilityRole="tablist">
        {categories.map((entry) => {
          const active = category === entry;
          return <Pressable key={entry} accessibilityRole="tab" accessibilityState={{ selected: active }} disabled={isLoading} onPress={() => selectCategory(entry)} style={[styles.chip, active && styles.chipActive]}><Text style={[styles.chipText, active && styles.chipTextActive]}>{contractCategoryLabels[entry]}</Text></Pressable>;
        })}
      </View>

      <View style={styles.filtersCard}>
        <View style={styles.filterTitleRow}><View style={styles.filterIcon}><SlidersHorizontal color={colors.primary} size={18} /></View><View><Text style={styles.filterTitle}>Filtrar registros</Text><Text style={styles.filterSubtitle}>Combine participante e período</Text></View></View>
        <View style={styles.field}><Text style={styles.label}>Buscar por cuidador</Text><View style={styles.inputShell}><Search color={colors.mutedForeground} size={19} /><TextInput accessibilityLabel="Buscar por cuidador" autoCapitalize="words" placeholder="Nome do cuidador" placeholderTextColor={colors.mutedForeground} value={draftFilters.participantName} onChangeText={(participantName) => setDraftFilters((current) => ({ ...current, participantName }))} style={styles.input} /></View></View>
        <View style={styles.dateRow}><View style={styles.dateField}><DatePickerField label="De" optional value={draftFilters.startDateFrom} onChange={(startDateFrom) => { setDateError(null); setDraftFilters((current) => ({ ...current, startDateFrom })); }} /></View><View style={styles.dateField}><DatePickerField label="Até" optional value={draftFilters.startDateTo} onChange={(startDateTo) => { setDateError(null); setDraftFilters((current) => ({ ...current, startDateTo })); }} /></View></View>
        {dateError ? <Text accessibilityRole="alert" style={styles.errorText}>{dateError}</Text> : null}
        <View style={styles.actions}><PrimaryButton label="Aplicar filtros" onPress={applyFilters} disabled={isLoading} style={styles.actionButton} /><PrimaryButton label="Limpar filtros" variant="secondary" onPress={clearFilters} disabled={isLoading || (!draftFilters.participantName && !draftFilters.startDateFrom && !draftFilters.startDateTo)} style={styles.actionButton} /></View>
      </View>

      {isLoading ? <View style={styles.state}><ActivityIndicator color={colors.primary} /><Text style={styles.stateText}>{loadingMessage}</Text></View> : null}
      {!isLoading && error ? <View style={styles.errorState}><Text style={styles.errorTitle}>Não foi possível carregar</Text><Text style={styles.stateText}>{error}</Text><PrimaryButton label="Tentar novamente" variant="secondary" onPress={() => fetchFirstPage(category, appliedFilters, 'Carregando contratações...')} /></View> : null}
      {!isLoading && !error && !items.length ? <View style={styles.emptyState}><Text style={styles.emptyTitle}>Nenhum registro encontrado</Text><Text style={styles.stateText}>{hasActiveFilters ? 'Nenhum registro encontrado para este filtro.' : 'Nenhum registro encontrado.'}</Text></View> : null}
      {!isLoading && !error && items.length ? <View style={styles.list}><View style={styles.resultHeader}><Text style={styles.resultTitle}>Seus registros</Text><Text style={styles.resultCount}>{page?.totalElements ?? items.length} {(page?.totalElements ?? items.length) === 1 ? 'resultado' : 'resultados'}</Text></View>{items.map((entry) => <ContractHistoryCard key={entry.id} item={entry} />)}</View> : null}
      {!isLoading && !error && page && !page.last ? <PrimaryButton label={isLoadingMore ? 'Carregando mais registros...' : 'Carregar mais'} variant="secondary" loading={isLoadingMore} onPress={loadMore} /> : null}
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, gap: spacing.xl },
  categories: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm },
  chip: { minHeight: 40, justifyContent: 'center', paddingHorizontal: spacing.md, borderRadius: radii.full, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card },
  chipActive: { borderColor: colors.primary, backgroundColor: colors.primary },
  chipText: { fontFamily: fontFamily.semiBold, fontSize: 12, color: colors.secondaryForeground },
  chipTextActive: { color: colors.primaryForeground },
  filtersCard: { gap: spacing.lg, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card },
  filterTitleRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.md },
  filterIcon: { width: 40, height: 40, alignItems: 'center', justifyContent: 'center', borderRadius: radii.md, backgroundColor: colors.secondary },
  filterTitle: { fontFamily: fontFamily.bold, fontSize: 15, color: colors.foreground },
  filterSubtitle: { fontFamily: fontFamily.regular, fontSize: 11, color: colors.mutedForeground },
  field: { gap: spacing.xs }, label: { fontFamily: fontFamily.semiBold, fontSize: 13, color: colors.foreground },
  inputShell: { minHeight: 54, flexDirection: 'row', alignItems: 'center', gap: spacing.md, paddingHorizontal: spacing.lg, borderWidth: 1, borderColor: colors.border, borderRadius: radii.lg, backgroundColor: colors.card },
  input: { flex: 1, paddingVertical: spacing.md, fontFamily: fontFamily.medium, fontSize: 14, color: colors.foreground },
  dateRow: { flexDirection: 'row', gap: spacing.md }, dateField: { flex: 1 },
  errorText: { fontFamily: fontFamily.medium, fontSize: 12, lineHeight: 18, color: colors.destructive },
  actions: { gap: spacing.sm }, actionButton: { minHeight: 50 },
  state: { minHeight: 160, alignItems: 'center', justifyContent: 'center', gap: spacing.md },
  stateText: { textAlign: 'center', fontFamily: fontFamily.regular, fontSize: 13, lineHeight: 20, color: colors.mutedForeground },
  errorState: { gap: spacing.md, padding: spacing.xl, borderRadius: radii.xl, backgroundColor: '#FFF5F3' },
  errorTitle: { textAlign: 'center', fontFamily: fontFamily.bold, fontSize: 15, color: colors.destructive },
  emptyState: { alignItems: 'center', gap: spacing.sm, padding: spacing.xxl, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card },
  emptyTitle: { fontFamily: fontFamily.bold, fontSize: 15, color: colors.foreground },
  list: { gap: spacing.md }, resultHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  resultTitle: { fontFamily: fontFamily.extraBold, fontSize: 18, color: colors.foreground }, resultCount: { fontFamily: fontFamily.medium, fontSize: 11, color: colors.mutedForeground },
});
