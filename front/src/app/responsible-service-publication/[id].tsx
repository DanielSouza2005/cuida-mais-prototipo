import { useCallback, useState } from 'react';
import { router, useFocusEffect, useLocalSearchParams, type Href } from 'expo-router';
import { Alert, Pressable, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { LoadingState } from '@/components/loading-state';
import { PrimaryButton } from '@/components/primary-button';
import { ProfileAvatar } from '@/components/profile-avatar';
import { ScreenContainer } from '@/components/screen-container';
import { ApiError } from '@/services/api';
import { cancelServicePublication, getServicePublication } from '@/services/servicePublicationService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { ServicePublication } from '@/types/servicePublication';
import { formatDateBR, formatScheduleTime } from '@/utils/dateTime';
import { hiringLabels, statusLabels, weekdayLabels } from '@/utils/serviceRequestLabels';

export default function ResponsibleServicePublicationDetailsScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const [item, setItem] = useState<ServicePublication | null>(null);
  const [loading, setLoading] = useState(true);
  const [canceling, setCanceling] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!id) return;
    setLoading(true); setError(null);
    try { setItem(await getServicePublication(id)); }
    catch (cause) { setError(cause instanceof ApiError ? cause.message : 'Não foi possível carregar este serviço.'); }
    finally { setLoading(false); }
  }, [id]);
  useFocusEffect(useCallback(() => { void load(); }, [load]));

  function confirmCancel() {
    Alert.alert('Cancelar publicação?', 'O serviço deixará de aparecer para novos cuidadores. Interesses e contratações existentes serão preservados.', [
      { text: 'Voltar', style: 'cancel' },
      { text: 'Cancelar publicação', style: 'destructive', onPress: () => void cancelPublication() },
    ]);
  }
  async function cancelPublication() {
    if (!item || canceling) return;
    setCanceling(true);
    try { setItem(await cancelServicePublication(item.id)); }
    catch (cause) { Alert.alert('Não foi possível cancelar', cause instanceof ApiError ? cause.message : 'Tente novamente.'); }
    finally { setCanceling(false); }
  }

  if (loading && !item) return <ScreenContainer contentStyle={styles.center}><LoadingState message="Carregando serviço publicado..." /></ScreenContainer>;
  if (!item) return <ScreenContainer contentStyle={styles.center}><AppHeader showBack title="Serviço publicado" /><Text style={styles.error}>{error ?? 'Serviço não encontrado.'}</Text><PrimaryButton variant="secondary" label="Tentar novamente" onPress={() => void load()} /></ScreenContainer>;
  const period = item.hiringType === 'PONTUAL' ? item.specificDates.map(formatDateBR).join(', ') : [formatDateBR(item.startDate), formatDateBR(item.endDate)].filter(Boolean).join(' até ');
  return <ScreenContainer contentStyle={styles.content}>
    <AppHeader showBack title="Detalhes da publicação" subtitle={item.assistedPersonName} />
    <Section title="Serviço">
      <Info label="Situação" value={item.acceptedApplicantCount ? 'Aceito' : item.pendingApplicantCount ? 'Com interessados' : statusLabels[item.status]} />
      <Info label="Tipo de contratação" value={hiringLabels[item.hiringType]} />
      <Info label="Período" value={period || 'A combinar'} />
      <Info label="Região" value={`${item.neighborhood} • ${item.city} - ${item.state}`} />
      <Info label="Necessidades" value={item.needsDescription} />
      {item.scheduleDays.map((schedule) => <Info key={`${schedule.weekday}-${schedule.startTime}`} label={weekdayLabels[schedule.weekday]} value={`${formatScheduleTime(schedule.startTime)} às ${formatScheduleTime(schedule.endTime)}`} />)}
    </Section>
    <Section title={`Cuidadores interessados (${item.applicantCount})`}>
      {!item.applications.length ? <Text style={styles.muted}>Nenhum cuidador demonstrou interesse ainda.</Text> : item.applications.map((application) => <Pressable key={application.id} onPress={() => router.push(`/responsible-service-request/${application.id}` as Href)} style={styles.application}>
        <ProfileAvatar imageUrl={application.caregiverProfilePhotoUrl} initials={application.caregiverName.slice(0, 2).toUpperCase()} size={48} />
        <View style={styles.flex}><Text style={styles.applicationName}>{application.caregiverName}</Text><Text style={styles.muted}>{statusLabels[application.status]} • toque para ver e responder</Text></View>
      </Pressable>)}
    </Section>
    {item.status === 'ABERTA' ? <PrimaryButton variant="secondary" label={canceling ? 'Cancelando...' : 'Cancelar publicação'} disabled={canceling} onPress={confirmCancel} /> : null}
  </ScreenContainer>;
}

function Section({ title, children }: { title: string; children: React.ReactNode }) { return <View style={styles.section}><Text style={styles.sectionTitle}>{title}</Text>{children}</View>; }
function Info({ label, value }: { label: string; value: string }) { return <View style={styles.info}><Text style={styles.label}>{label}</Text><Text style={styles.value}>{value}</Text></View>; }
const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, gap: spacing.lg }, center: { flexGrow: 1, justifyContent: 'center', padding: spacing.xl, gap: spacing.lg }, section: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card }, sectionTitle: { fontFamily: fontFamily.extraBold, fontSize: 17, color: colors.foreground }, info: { gap: spacing.xxs }, label: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground }, value: { fontFamily: fontFamily.medium, fontSize: 13, lineHeight: 19, color: colors.foreground }, application: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, padding: spacing.md, borderRadius: radii.lg, backgroundColor: colors.muted }, applicationName: { fontFamily: fontFamily.bold, fontSize: 13, color: colors.foreground }, flex: { flex: 1 }, muted: { fontFamily: fontFamily.medium, fontSize: 11, color: colors.mutedForeground }, warning: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, backgroundColor: colors.secondary }, warningTitle: { fontFamily: fontFamily.bold, color: colors.destructive }, warningText: { fontFamily: fontFamily.regular, fontSize: 12, lineHeight: 18, color: colors.foreground }, error: { textAlign: 'center', fontFamily: fontFamily.medium, color: colors.destructive },
});
