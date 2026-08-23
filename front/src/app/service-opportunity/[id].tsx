import { useEffect, useState } from 'react';
import { useLocalSearchParams } from 'expo-router';
import { Alert, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { LoadingState } from '@/components/loading-state';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { dependencyLevelOptions, mobilityOptions } from '@/constants/enums';
import { ApiError } from '@/services/api';
import { applyToServiceOpportunity, getServiceOpportunity } from '@/services/serviceOpportunityService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { ServiceOpportunity } from '@/types/serviceOpportunity';
import { formatDateBR, formatScheduleTime } from '@/utils/dateTime';
import { hiringLabels, statusLabels, weekdayLabels } from '@/utils/serviceRequestLabels';

export default function ServiceOpportunityDetailsScreen() {
  const { id, originLat, originLng } = useLocalSearchParams<{ id: string; originLat?: string; originLng?: string }>();
  const [item, setItem] = useState<ServiceOpportunity | null>(null);
  const [loading, setLoading] = useState(true);
  const [applying, setApplying] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    if (!id) { setLoading(false); return; }
    const latitude = Number(originLat), longitude = Number(originLng);
    const origin = Number.isFinite(latitude) && Number.isFinite(longitude) ? { latitude, longitude } : undefined;
    getServiceOpportunity(id, origin).then((result) => { if (active) setItem(result); }).catch((cause) => { if (active) setError(cause instanceof ApiError ? cause.message : 'Não foi possível carregar este serviço.'); }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [id, originLat, originLng]);

  async function apply() {
    if (!item || applying) return;
    setApplying(true);
    try {
      const updated = await applyToServiceOpportunity(item.id);
      setItem(updated);
      Alert.alert('Interesse enviado', 'Sua solicitação foi enviada ao responsável.');
    } catch (cause) {
      Alert.alert('Não foi possível enviar seu interesse', cause instanceof ApiError ? cause.message : 'Tente novamente.');
    } finally { setApplying(false); }
  }

  if (loading) return <ScreenContainer contentStyle={styles.center}><LoadingState message="Carregando serviço disponível..." /></ScreenContainer>;
  if (!item) return <ScreenContainer contentStyle={styles.center}><AppHeader showBack title="Serviço disponível" /><Text style={styles.error}>{error ?? 'Este serviço não está mais disponível.'}</Text></ScreenContainer>;
  const period = item.hiringType === 'PONTUAL' ? item.specificDates.map(formatDateBR).join(', ') : [formatDateBR(item.startDate), formatDateBR(item.endDate)].filter(Boolean).join(' até ');
  return <ScreenContainer contentStyle={styles.content}>
    <AppHeader showBack title="Detalhes do serviço" subtitle="Confira as informações públicas antes de demonstrar interesse." />
    {item.applicationStatus ? <View style={styles.application}><Text style={styles.applicationTitle}>Interesse enviado</Text><Text style={styles.applicationText}>Status: {statusLabels[item.applicationStatus]}</Text></View> : null}
    <Section title="Oportunidade"><Info label="Tipo de contratação" value={hiringLabels[item.hiringType]} /><Info label="Região aproximada" value={`${item.neighborhood} • ${item.city} - ${item.state}`} />{item.distanceKm != null ? <Info label="Distância" value={`${item.distanceKm.toFixed(1).replace('.', ',')} km de você`} /> : null}<Info label="Período" value={period || 'A combinar'} /></Section>
    <Section title="Pessoa assistida"><Info label="Identificação" value={item.assistedPersonAlias} /><Info label="Grau de dependência" value={label(item.dependencyLevel, dependencyLevelOptions)} /><Info label="Mobilidade" value={label(item.mobility, mobilityOptions)} /></Section>
    <Section title="Dias e horários">{item.scheduleDays.map((schedule) => <Text key={`${schedule.weekday}-${schedule.startTime}`} style={styles.text}>{weekdayLabels[schedule.weekday]} • {formatScheduleTime(schedule.startTime)} às {formatScheduleTime(schedule.endTime)}</Text>)}</Section>
    <Section title="Necessidades"><Text style={styles.text}>{item.needsDescription}</Text></Section>
    {item.careRoutine ? <Section title="Rotina de cuidados"><Info label="Rotina" value={item.careRoutine.name} />{item.careRoutine.items.map((care, index) => <View key={`${care.title}-${index}`} style={styles.careItem}><Text style={styles.careTitle}>{care.title}</Text>{care.description ? <Text style={styles.text}>{care.description}</Text> : null}{care.scheduledTime ? <Text style={styles.label}>Horário previsto: {formatScheduleTime(care.scheduledTime)}</Text> : null}</View>)}</Section> : null}
    <View style={styles.privacy}><Text style={styles.privacyTitle}>Privacidade protegida</Text><Text style={styles.privacyText}>Endereço completo, telefone, e-mail e CPF não são exibidos antes do aceite.</Text></View>
    {!item.applicationStatus && item.status === 'ABERTA' ? <PrimaryButton label={applying ? 'Enviando interesse...' : 'Tenho interesse'} loading={applying} disabled={applying} onPress={() => void apply()} /> : null}
  </ScreenContainer>;
}

function Section({ title, children }: { title: string; children: React.ReactNode }) { return <View style={styles.section}><Text style={styles.sectionTitle}>{title}</Text>{children}</View>; }
function Info({ label: infoLabel, value }: { label: string; value: string }) { return <View style={styles.info}><Text style={styles.label}>{infoLabel}</Text><Text style={styles.value}>{value}</Text></View>; }
function label(value: string, options: readonly { value: string; label: string }[]) { return options.find((item) => item.value === value)?.label ?? 'Não informado'; }

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, gap: spacing.lg }, center: { flexGrow: 1, justifyContent: 'center', padding: spacing.xl, gap: spacing.lg }, error: { textAlign: 'center', fontFamily: fontFamily.medium, color: colors.destructive }, section: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card }, sectionTitle: { fontFamily: fontFamily.extraBold, fontSize: 17, color: colors.foreground }, info: { gap: spacing.xxs }, label: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground }, value: { fontFamily: fontFamily.medium, fontSize: 13, lineHeight: 19, color: colors.foreground }, text: { fontFamily: fontFamily.regular, fontSize: 13, lineHeight: 20, color: colors.foreground }, careItem: { gap: spacing.xs, padding: spacing.md, borderRadius: radii.lg, backgroundColor: colors.muted }, careTitle: { fontFamily: fontFamily.bold, fontSize: 13, color: colors.foreground }, application: { gap: spacing.xs, padding: spacing.lg, borderRadius: radii.xl, backgroundColor: colors.mint }, applicationTitle: { fontFamily: fontFamily.bold, color: colors.mintForeground }, applicationText: { fontFamily: fontFamily.medium, fontSize: 12, color: colors.mintForeground }, privacy: { gap: spacing.xs, padding: spacing.lg, borderRadius: radii.xl, backgroundColor: colors.secondary }, privacyTitle: { fontFamily: fontFamily.bold, color: colors.primary }, privacyText: { fontFamily: fontFamily.regular, fontSize: 12, lineHeight: 18, color: colors.mutedForeground },
});
