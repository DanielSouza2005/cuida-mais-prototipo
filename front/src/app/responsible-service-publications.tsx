import { useCallback, useState } from 'react';
import { router, useFocusEffect, type Href } from 'expo-router';
import { CalendarDays, Plus, RefreshCw, UsersRound } from 'lucide-react-native';
import { Pressable, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { DatePickerField } from '@/components/date-picker-field';
import { OptionGroup } from '@/components/option-group';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { getServiceRequestFormData } from '@/services/serviceRequestService';
import { getServicePublications } from '@/services/servicePublicationService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { HiringType, ServiceRequestFormData, ServiceRequestStatus } from '@/types/serviceRequest';
import type { ServicePublication, ServicePublicationFilters, ServicePublicationPage } from '@/types/servicePublication';
import { formatDateBR, formatScheduleTime } from '@/utils/dateTime';
import { hiringLabels, statusLabels, weekdayLabels } from '@/utils/serviceRequestLabels';

const statusOptions = [
  { value: 'TODOS', label: 'Todas' }, { value: 'ABERTA', label: 'Publicadas' },
  { value: 'ACEITA', label: 'Aceitas' },
  { value: 'CANCELADA', label: 'Canceladas' }, { value: 'EXPIRADA', label: 'Expiradas' },
];
const hiringOptions = [{ value: 'TODOS', label: 'Todos os tipos' }, ...Object.entries(hiringLabels).map(([value, label]) => ({ value, label }))];

export default function ResponsibleServicePublicationsScreen() {
  const insets = useSafeAreaInsets();
  const [status, setStatus] = useState<ServiceRequestStatus | 'TODOS'>('TODOS');
  const [personId, setPersonId] = useState('TODOS');
  const [hiringType, setHiringType] = useState<HiringType | 'TODOS'>('TODOS');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [city, setCity] = useState('');
  const [neighborhood, setNeighborhood] = useState('');
  const [needs, setNeeds] = useState('');
  const [filters, setFilters] = useState<ServicePublicationFilters>({});
  const [formData, setFormData] = useState<ServiceRequestFormData | null>(null);
  const [page, setPage] = useState<ServicePublicationPage | null>(null);
  const [items, setItems] = useState<ServicePublication[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(false);

  const load = useCallback(async (nextPage = 0, append = false, refresh = false) => {
    if (refresh) setRefreshing(true); else setLoading(true);
    setError(false);
    try {
      const [result, form] = await Promise.all([getServicePublications(filters, nextPage), formData ? Promise.resolve(formData) : getServiceRequestFormData()]);
      setFormData(form);
      setPage(result);
      setItems((current) => append ? [...current, ...result.content.filter((item) => !current.some((existing) => existing.id === item.id))] : result.content);
    } catch { setError(true); }
    finally { setLoading(false); setRefreshing(false); }
  }, [filters, formData]);

  useFocusEffect(useCallback(() => { void load(); }, [load]));

  function applyFilters() {
    setFilters({
      status: status === 'TODOS' ? undefined : status,
      assistedPersonId: personId === 'TODOS' ? undefined : personId,
      hiringType: hiringType === 'TODOS' ? undefined : hiringType,
      startDate: toIsoDate(startDate), endDate: toIsoDate(endDate),
      city: city.trim() || undefined, neighborhood: neighborhood.trim() || undefined, needs: needs.trim() || undefined,
    });
  }

  const personOptions = [{ value: 'TODOS', label: 'Todas as pessoas' }, ...(formData?.assistedPersons ?? []).map((person) => ({ value: person.id, label: person.name }))];
  return <View style={styles.screen}><ScreenContainer contentStyle={styles.content} scrollViewProps={{ refreshControl: <RefreshControl refreshing={refreshing} onRefresh={() => void load(0, false, true)} /> }}>
    <AppHeader showBack title="Serviços publicados" subtitle="Acompanhe suas oportunidades e os cuidadores interessados." />
    <View style={styles.filters}>
      <OptionGroup label="Situação" options={statusOptions} value={status} onChange={(value) => setStatus(value as ServiceRequestStatus | 'TODOS')} />
      <OptionGroup label="Pessoa assistida" options={personOptions} value={personId} onChange={(value) => setPersonId(value as string)} />
      <OptionGroup label="Tipo de contratação" options={hiringOptions} value={hiringType} onChange={(value) => setHiringType(value as HiringType | 'TODOS')} />
      <DatePickerField label="Período a partir de" value={startDate} onChange={setStartDate} />
      <DatePickerField label="Período até" value={endDate} onChange={setEndDate} />
      <AppTextInput label="Cidade" optional value={city} onChangeText={setCity} placeholder="Ex.: São Paulo" />
      <AppTextInput label="Bairro" optional value={neighborhood} onChangeText={setNeighborhood} placeholder="Ex.: Centro" />
      <AppTextInput label="Cuidados necessários" optional value={needs} onChangeText={setNeeds} placeholder="Ex.: apoio com mobilidade" />
      <PrimaryButton variant="secondary" label="Aplicar filtros" onPress={applyFilters} />
    </View>
    {loading && !items.length ? <State text="Carregando serviços publicados..." /> : error ? <State text="Não foi possível carregar seus serviços." action={() => void load()} /> : !items.length ? <State text="Nenhum serviço encontrado com esses filtros." /> : <View style={styles.list}>{items.map((item) => <PublicationCard key={item.id} item={item} />)}</View>}
    {page && !page.last ? <PrimaryButton variant="secondary" label={loading ? 'Carregando...' : 'Carregar mais'} disabled={loading} onPress={() => void load(page.page + 1, true)} /> : null}
  </ScreenContainer>
    <Pressable accessibilityRole="button" accessibilityLabel="Publicar novo serviço" hitSlop={8} onPress={() => router.push('/request-service?publish=true' as Href)} style={({ pressed }) => [styles.fab, { bottom: Math.max(insets.bottom, spacing.lg) + spacing.lg }, pressed && styles.fabPressed]}>
      <Plus color={colors.primaryForeground} size={30} strokeWidth={2.7} />
    </Pressable>
  </View>;
}

function PublicationCard({ item }: { item: ServicePublication }) {
  const schedule = item.scheduleDays[0];
  const period = item.hiringType === 'PONTUAL' ? item.specificDates.map(formatDateBR).join(', ') : [formatDateBR(item.startDate), formatDateBR(item.endDate)].filter(Boolean).join(' até ');
  const situation = item.acceptedApplicantCount ? 'Aceito' : item.pendingApplicantCount ? 'Com interessados' : statusLabels[item.status];
  return <Pressable onPress={() => router.push(`/responsible-service-publication/${item.id}` as Href)} style={({ pressed }) => [styles.card, pressed && styles.pressed]}>
    <View style={styles.row}><View style={styles.icon}><CalendarDays color={colors.primary} size={20} /></View><View style={styles.flex}><Text style={styles.cardTitle}>{item.assistedPersonName}</Text><Text style={styles.muted}>{hiringLabels[item.hiringType]} • {item.neighborhood}, {item.city}</Text></View><View style={styles.badge}><Text style={styles.badgeText}>{situation}</Text></View></View>
    <Text numberOfLines={2} style={styles.text}>{item.needsDescription}</Text>
    <Text style={styles.muted}>{period || 'Período a combinar'}{schedule ? ` • ${weekdayLabels[schedule.weekday]} • ${formatScheduleTime(schedule.startTime)} às ${formatScheduleTime(schedule.endTime)}` : ''}</Text>
    <View style={styles.row}><UsersRound color={colors.mutedForeground} size={17} /><Text style={styles.muted}>{item.applicantCount} {item.applicantCount === 1 ? 'cuidador interessado' : 'cuidadores interessados'}</Text></View>
    <Text style={styles.link}>Ver detalhes</Text>
  </Pressable>;
}

function State({ text, action }: { text: string; action?: () => void }) { return <View style={styles.state}><RefreshCw color={colors.mutedForeground} size={24} /><Text style={styles.muted}>{text}</Text>{action ? <PrimaryButton variant="secondary" label="Tentar novamente" onPress={action} /> : null}</View>; }
function toIsoDate(value: string) { if (!value) return undefined; const [day, month, year] = value.split('/'); return year && month && day ? `${year}-${month}-${day}` : undefined; }

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: colors.background }, content: { paddingHorizontal: spacing.xl, paddingBottom: 112, gap: spacing.lg }, fab: { position: 'absolute', right: spacing.xl, width: 58, height: 58, alignItems: 'center', justifyContent: 'center', borderRadius: radii.full, backgroundColor: colors.primary, elevation: 6, shadowColor: colors.primary, shadowOpacity: .3, shadowRadius: 12, shadowOffset: { width: 0, height: 6 } }, fabPressed: { opacity: .82, transform: [{ scale: .97 }] }, filters: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card }, list: { gap: spacing.md }, card: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card }, pressed: { opacity: .82 }, row: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm }, flex: { flex: 1 }, icon: { width: 42, height: 42, alignItems: 'center', justifyContent: 'center', borderRadius: radii.lg, backgroundColor: colors.secondary }, cardTitle: { fontFamily: fontFamily.extraBold, fontSize: 15, color: colors.foreground }, text: { fontFamily: fontFamily.regular, fontSize: 13, lineHeight: 20, color: colors.foreground }, muted: { fontFamily: fontFamily.medium, fontSize: 11, color: colors.mutedForeground }, badge: { paddingHorizontal: spacing.sm, paddingVertical: spacing.xs, borderRadius: radii.full, backgroundColor: colors.secondary }, badgeText: { fontFamily: fontFamily.bold, fontSize: 9, color: colors.primary }, warning: { fontFamily: fontFamily.semiBold, fontSize: 11, lineHeight: 17, color: colors.destructive }, link: { fontFamily: fontFamily.bold, fontSize: 12, color: colors.primary }, state: { minHeight: 180, alignItems: 'center', justifyContent: 'center', gap: spacing.md },
});
