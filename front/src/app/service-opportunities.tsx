import { useCallback, useEffect, useState } from 'react';
import { router, useFocusEffect, useLocalSearchParams, useSegments, type Href } from 'expo-router';
import * as Location from 'expo-location';
import { BriefcaseBusiness, Navigation, RefreshCw } from 'lucide-react-native';
import { Pressable, RefreshControl, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { LocationCombobox } from '@/components/location-combobox';
import { OptionGroup } from '@/components/option-group';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { getSentOpportunityApplications, getServiceOpportunityLocations, searchServiceOpportunities } from '@/services/serviceOpportunityService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { HiringType } from '@/types/serviceRequest';
import type { ServiceOpportunity, ServiceOpportunityFilters, ServiceOpportunityPage } from '@/types/serviceOpportunity';
import type { LocationSuggestion } from '@/types/caregiverSearch';
import { dependencyLevelOptions, mobilityOptions } from '@/constants/enums';
import { formatDateBR, formatScheduleTime } from '@/utils/dateTime';
import { hiringLabels, statusLabels, weekdayLabels } from '@/utils/serviceRequestLabels';

const hiringOptions = [{ value: 'TODOS', label: 'Todos' }, ...Object.entries(hiringLabels).map(([value, label]) => ({ value, label }))];

export default function ServiceOpportunitiesScreen() {
  const params = useLocalSearchParams<{ mode?: string; notice?: string }>();
  const segments = useSegments();
  const isTabRoute = (segments as string[])[0] === '(tabs)';
  const [mode, setMode] = useState<'AVAILABLE' | 'SENT'>(params.mode === 'SENT' ? 'SENT' : 'AVAILABLE');
  const [locationQuery, setLocationQuery] = useState('');
  const [selectedLocation, setSelectedLocation] = useState<LocationSuggestion | null>(null);
  const [origin, setOrigin] = useState<ServiceOpportunityFilters['origin']>(null);
  const [locationSuggestions, setLocationSuggestions] = useState<LocationSuggestion[]>([]);
  const [locationsLoading, setLocationsLoading] = useState(false);
  const [currentLocationLoading, setCurrentLocationLoading] = useState(false);
  const [locationError, setLocationError] = useState<string | null>(null);
  const [hiringType, setHiringType] = useState<HiringType | 'TODOS'>('TODOS');
  const [filters, setFilters] = useState<ServiceOpportunityFilters>({});
  const [page, setPage] = useState<ServiceOpportunityPage | null>(null);
  const [items, setItems] = useState<ServiceOpportunity[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(false);

  useEffect(() => {
    let active = true;
    setLocationsLoading(true);
    const timeout = setTimeout(() => getServiceOpportunityLocations(locationQuery).then((items) => { if (active) setLocationSuggestions(items); }).catch(() => { if (active) setLocationSuggestions([]); }).finally(() => { if (active) setLocationsLoading(false); }), 300);
    return () => { active = false; clearTimeout(timeout); };
  }, [locationQuery]);

  const load = useCallback(async (nextPage = 0, append = false, refresh = false) => {
    if (refresh) setRefreshing(true); else setLoading(true);
    setError(false);
    try {
      const result = mode === 'AVAILABLE'
        ? await searchServiceOpportunities(filters, nextPage)
        : await getSentOpportunityApplications(undefined, nextPage);
      setPage(result);
      setItems((current) => append ? [...current, ...result.content.filter((item) => !current.some((existing) => existing.id === item.id))] : result.content);
    } catch { setError(true); }
    finally { setLoading(false); setRefreshing(false); }
  }, [filters, mode]);

  useFocusEffect(useCallback(() => { void load(); }, [load]));

  useEffect(() => { if (params.mode === 'SENT') setMode('SENT'); }, [params.mode]);

  function applyFilters() {
    setFilters({ location: selectedLocation, origin, hiringType: hiringType === 'TODOS' ? undefined : hiringType });
  }

  function changeLocationText(value: string) {
    setLocationQuery(value); setLocationError(null);
    if (!value.trim() || origin || (selectedLocation && value !== selectedLocation.label)) { setSelectedLocation(null); setOrigin(null); }
  }
  function selectLocation(value: LocationSuggestion) { setSelectedLocation(value); setOrigin(null); setLocationQuery(value.label); setLocationError(null); }
  function clearLocation() { setSelectedLocation(null); setOrigin(null); setLocationQuery(''); setLocationError(null); }
  async function handleUseCurrentLocation() {
    setCurrentLocationLoading(true); setLocationError(null);
    try {
      const permission = await Location.requestForegroundPermissionsAsync();
      if (permission.status !== Location.PermissionStatus.GRANTED) { setLocationError('Permita o acesso à localização para calcular a distância real.'); return; }
      const position = await Location.getCurrentPositionAsync({ accuracy: Location.Accuracy.Balanced });
      setSelectedLocation(null); setLocationQuery('Minha localização atual'); setOrigin({ latitude: position.coords.latitude, longitude: position.coords.longitude });
    } catch { setLocationError('Não foi possível obter sua localização agora.'); }
    finally { setCurrentLocationLoading(false); }
  }

  function changeMode(next: 'AVAILABLE' | 'SENT') {
    setMode(next); setItems([]); setPage(null);
  }

  return <ScreenContainer contentStyle={styles.content} scrollViewProps={{ refreshControl: <RefreshControl refreshing={refreshing} onRefresh={() => void load(0, false, true)} /> }}>
    <AppHeader showBack={!isTabRoute} title="Buscar serviços" subtitle="Encontre oportunidades publicadas por responsáveis." />
    <View style={styles.tabs}>{([['AVAILABLE', 'Disponíveis'], ['SENT', 'Interesses enviados']] as const).map(([value, label]) => <Pressable key={value} onPress={() => changeMode(value)} style={[styles.tab, mode === value && styles.tabActive]}><Text style={[styles.tabText, mode === value && styles.tabTextActive]}>{label}</Text></Pressable>)}</View>
    {params.notice === 'contract-unavailable' ? <View style={styles.notice}><Text style={styles.noticeTitle}>Interesse aceito</Text><Text style={styles.noticeText}>Não foi possível abrir a contratação específica. Acompanhe abaixo seus interesses enviados.</Text></View> : null}
    {mode === 'AVAILABLE' ? <View style={styles.filters}>
      <LocationCombobox value={locationQuery} selectedLocation={selectedLocation} suggestions={locationSuggestions} loading={locationsLoading} disabled={loading} onChangeText={changeLocationText} onSelectLocation={selectLocation} onClear={clearLocation} />
      <PrimaryButton label="Usar minha localização" icon={Navigation} variant="secondary" loading={currentLocationLoading} onPress={() => void handleUseCurrentLocation()} />
      {locationError ? <Text style={styles.locationError}>{locationError}</Text> : null}
      <OptionGroup label="Tipo de contratação" options={hiringOptions} value={hiringType} onChange={(value) => setHiringType(value as HiringType | 'TODOS')} />
      <PrimaryButton label="Buscar serviços" onPress={applyFilters} />
    </View> : null}
    {loading && !items.length ? <State text="Carregando serviços disponíveis..." /> : error ? <State text="Não foi possível carregar os serviços." action={() => void load()} /> : !items.length ? <State text={mode === 'AVAILABLE' ? 'Nenhum serviço disponível com esses filtros.' : 'Você ainda não enviou interesses.'} /> : <View style={styles.list}>{items.map((item) => <OpportunityCard key={`${item.id}-${item.applicationId ?? ''}`} item={item} origin={filters.origin} />)}</View>}
    {page && !page.last ? <PrimaryButton variant="secondary" label={loading ? 'Carregando...' : 'Carregar mais'} disabled={loading} onPress={() => void load(page.page + 1, true)} /> : null}
  </ScreenContainer>;
}

function OpportunityCard({ item, origin }: { item: ServiceOpportunity; origin?: ServiceOpportunityFilters['origin'] }) {
  const firstSchedule = item.scheduleDays[0];
  const period = item.hiringType === 'PONTUAL' ? item.specificDates.map(formatDateBR).join(', ') : [formatDateBR(item.startDate), formatDateBR(item.endDate)].filter(Boolean).join(' até ');
  return <Pressable onPress={() => router.push({ pathname: '/service-opportunity/[id]', params: { id: item.id, ...(origin ? { originLat: String(origin.latitude), originLng: String(origin.longitude) } : {}) } } as Href)} style={({ pressed }) => [styles.card, pressed && styles.pressed]}>
    <View style={styles.cardTop}><View style={styles.icon}><BriefcaseBusiness color={colors.primary} size={20} /></View><View style={styles.flex}><Text style={styles.title}>{hiringLabels[item.hiringType]}</Text><Text style={styles.region}>{item.neighborhood} • {item.city} - {item.state}</Text></View>{item.applicationStatus ? <View style={styles.status}><Text style={styles.statusText}>{statusLabels[item.applicationStatus]}</Text></View> : null}</View>
    <Text style={styles.needs} numberOfLines={3}>{item.needsDescription}</Text>
    <Text style={styles.meta}>{period || 'Período a combinar'}</Text>
    {firstSchedule ? <Text style={styles.meta}>{weekdayLabels[firstSchedule.weekday]} • {formatScheduleTime(firstSchedule.startTime)} às {formatScheduleTime(firstSchedule.endTime)}</Text> : null}
    <Text style={styles.meta}>{label(item.dependencyLevel, dependencyLevelOptions)} • {label(item.mobility, mobilityOptions)}</Text>
    {item.distanceKm != null ? <Text style={styles.distance}>{item.distanceKm.toFixed(1).replace('.', ',')} km de você</Text> : null}
    <Text style={styles.link}>Ver detalhes</Text>
  </Pressable>;
}

function label(value: string, options: readonly { value: string; label: string }[]) { return options.find((item) => item.value === value)?.label ?? 'Não informado'; }
function State({ text, action }: { text: string; action?: () => void }) { return <View style={styles.state}><RefreshCw color={colors.mutedForeground} size={24} /><Text style={styles.stateText}>{text}</Text>{action ? <PrimaryButton variant="secondary" label="Tentar novamente" onPress={action} /> : null}</View>; }

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, gap: spacing.lg }, tabs: { flexDirection: 'row', padding: spacing.xxs, borderRadius: radii.lg, backgroundColor: colors.muted }, tab: { flex: 1, minHeight: 42, alignItems: 'center', justifyContent: 'center', borderRadius: radii.md }, tabActive: { backgroundColor: colors.card, ...shadows.card }, tabText: { fontFamily: fontFamily.semiBold, fontSize: 12, color: colors.mutedForeground }, tabTextActive: { color: colors.primary }, notice: { gap: spacing.xs, padding: spacing.md, borderRadius: radii.lg, backgroundColor: colors.secondary }, noticeTitle: { fontFamily: fontFamily.bold, color: colors.primary }, noticeText: { fontFamily: fontFamily.regular, fontSize: 12, lineHeight: 18, color: colors.mutedForeground }, filters: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, backgroundColor: colors.card, borderWidth: 1, borderColor: colors.border }, list: { gap: spacing.md }, card: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card }, pressed: { opacity: .82 }, cardTop: { flexDirection: 'row', alignItems: 'center', gap: spacing.md }, icon: { width: 42, height: 42, alignItems: 'center', justifyContent: 'center', borderRadius: radii.lg, backgroundColor: colors.secondary }, flex: { flex: 1 }, title: { fontFamily: fontFamily.extraBold, fontSize: 15, color: colors.foreground }, region: { fontFamily: fontFamily.medium, fontSize: 11, color: colors.mutedForeground }, needs: { fontFamily: fontFamily.regular, fontSize: 13, lineHeight: 20, color: colors.foreground }, meta: { fontFamily: fontFamily.medium, fontSize: 11, color: colors.mutedForeground }, distance: { alignSelf: 'flex-start', fontFamily: fontFamily.bold, fontSize: 11, color: colors.primary, backgroundColor: colors.secondary, borderRadius: radii.full, paddingHorizontal: spacing.sm, paddingVertical: spacing.xs }, locationError: { fontFamily: fontFamily.medium, fontSize: 12, color: colors.destructive }, link: { fontFamily: fontFamily.bold, fontSize: 12, color: colors.primary }, status: { paddingHorizontal: spacing.sm, paddingVertical: spacing.xs, borderRadius: radii.full, backgroundColor: colors.secondary }, statusText: { fontFamily: fontFamily.bold, fontSize: 9, color: colors.primary }, state: { minHeight: 180, alignItems: 'center', justifyContent: 'center', gap: spacing.md }, stateText: { textAlign: 'center', fontFamily: fontFamily.medium, color: colors.mutedForeground },
});
