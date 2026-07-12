import { useCallback, useRef, useState } from 'react';
import { useFocusEffect } from 'expo-router';
import { ActivityIndicator, RefreshControl, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { LoadingState } from '@/components/loading-state';
import { OptionGroup } from '@/components/option-group';
import { ReceivedRequestCard } from '@/components/received-request-card';
import { ScreenContainer } from '@/components/screen-container';
import { ApiError } from '@/services/api';
import { getReceivedServiceRequests } from '@/services/receivedServiceRequestService';
import { colors, fontFamily, spacing } from '@/theme/tokens';
import type { ReceivedServiceRequest } from '@/types/receivedServiceRequest';
import type { ServiceRequestStatus } from '@/types/serviceRequest';

type Filter = ServiceRequestStatus | 'TODAS';
const PAGE_SIZE = 10;
const filterOptions = [
  { value: 'PENDENTE', label: 'Pendentes' },
  { value: 'TODAS', label: 'Todas' },
  { value: 'ACEITA', label: 'Aceitas' },
  { value: 'REJEITADA', label: 'Rejeitadas' },
  { value: 'CANCELADA', label: 'Canceladas' },
  { value: 'EXPIRADA', label: 'Expiradas' },
];

export default function CaregiverServiceRequestsScreen() {
  const [requests, setRequests] = useState<ReceivedServiceRequest[]>([]);
  const [filter, setFilter] = useState<Filter>('PENDENTE');
  const [page, setPage] = useState(0);
  const [last, setLast] = useState(true);
  const [initialLoading, setInitialLoading] = useState(true);
  const [filtering, setFiltering] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const requestId = useRef(0);
  const filterRef = useRef<Filter>(filter);
  const filteringRef = useRef(false);
  const loadingMoreRef = useRef(false);

  const loadFirstPage = useCallback(async (targetFilter: Filter, mode: 'focus' | 'filter' | 'refresh') => {
    const currentRequest = ++requestId.current;
    setError(null);
    loadingMoreRef.current = false;
    setLoadingMore(false);
    if (mode === 'filter') { filteringRef.current = true; setFiltering(true); setRequests([]); }
    if (mode === 'refresh') setRefreshing(true);
    try {
      const result = await getReceivedServiceRequests(targetFilter === 'TODAS' ? undefined : targetFilter, 0, PAGE_SIZE);
      if (currentRequest !== requestId.current || targetFilter !== filterRef.current) return;
      setRequests(result.content);
      setPage(result.page);
      setLast(result.last);
    } catch (reason) {
      if (currentRequest === requestId.current) setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar as solicitações.');
    } finally {
      if (currentRequest === requestId.current) {
        setInitialLoading(false);
        filteringRef.current = false;
        setFiltering(false);
        setRefreshing(false);
      }
    }
  }, []);

  useFocusEffect(useCallback(() => {
    void loadFirstPage(filterRef.current, 'focus');
    return () => { requestId.current += 1; };
  }, [loadFirstPage]));

  function changeFilter(value: string | string[]) {
    if (filteringRef.current || loadingMoreRef.current) return;
    if (Array.isArray(value)) return;
    const next = value as Filter;
    if (next === filter) return;
    filterRef.current = next;
    setFilter(next);
    setPage(0);
    setLast(true);
    void loadFirstPage(next, 'filter');
  }

  async function loadMore() {
    if (initialLoading || filteringRef.current || refreshing || loadingMoreRef.current || last) return;
    const nextPage = page + 1;
    const activeFilter = filterRef.current;
    const currentRequest = ++requestId.current;
    loadingMoreRef.current = true;
    setLoadingMore(true);
    try {
      const result = await getReceivedServiceRequests(activeFilter === 'TODAS' ? undefined : activeFilter, nextPage, PAGE_SIZE);
      if (currentRequest !== requestId.current || activeFilter !== filterRef.current) return;
      setRequests((current) => {
        const knownIds = new Set(current.map((item) => item.id));
        return [...current, ...result.content.filter((item) => !knownIds.has(item.id))];
      });
      setPage(result.page);
      setLast(result.last);
    } catch (reason) {
      if (currentRequest === requestId.current) setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar as solicitações.');
    } finally {
      loadingMoreRef.current = false;
      setLoadingMore(false);
    }
  }

  if (initialLoading) return <ScreenContainer contentStyle={styles.center}><LoadingState message="Carregando solicitações..." /></ScreenContainer>;

  return (
    <ScreenContainer
      contentStyle={styles.content}
      scrollViewProps={{
        onScroll: ({ nativeEvent }) => {
          const distanceFromBottom = nativeEvent.contentSize.height - nativeEvent.layoutMeasurement.height - nativeEvent.contentOffset.y;
          if (distanceFromBottom < 180) void loadMore();
        },
        scrollEventThrottle: 160,
        refreshControl: <RefreshControl refreshing={refreshing} onRefresh={() => void loadFirstPage(filterRef.current, 'refresh')} tintColor={colors.primary} colors={[colors.primary]} />,
      }}
    >
      <AppHeader title="Solicitações" subtitle="Veja os pedidos de serviço enviados por responsáveis." />
      <OptionGroup label="Exibir" options={filterOptions} value={filter} onChange={changeFilter} />
      {filtering ? <View style={styles.loadingRow}><ActivityIndicator color={colors.primary} size="small" /><Text style={styles.loadingText}>Atualizando solicitações...</Text></View> : null}
      {error ? <Text style={styles.empty}>{error}</Text> : null}
      <View style={styles.list}>
        {!filtering ? requests.map((request) => <ReceivedRequestCard key={request.id} request={request} />) : null}
        {!error && !filtering && requests.length === 0 ? <Text style={styles.empty}>{filter === 'PENDENTE' ? 'Nenhuma solicitação pendente no momento.' : 'Nenhuma solicitação encontrada.'}</Text> : null}
        {loadingMore ? <View style={styles.loadingRow}><ActivityIndicator color={colors.primary} size="small" /><Text style={styles.loadingText}>Carregando mais solicitações...</Text></View> : null}
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, gap: spacing.lg },
  center: { flexGrow: 1, justifyContent: 'center', padding: spacing.xl },
  list: { gap: spacing.md },
  loadingRow: { minHeight: 48, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: spacing.sm },
  loadingText: { fontFamily: fontFamily.medium, fontSize: 12, color: colors.mutedForeground },
  empty: { textAlign: 'center', padding: spacing.xl, fontFamily: fontFamily.medium, color: colors.mutedForeground },
});
