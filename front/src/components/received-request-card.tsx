import { router, type Href } from 'expo-router';
import { AlertTriangle, ArrowRight, MapPin } from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { ReceivedServiceRequest } from '@/types/receivedServiceRequest';
import { hiringLabels, statusLabels, weekdayLabels } from '@/utils/serviceRequestLabels';

export function ReceivedRequestCard({ request }: { request: ReceivedServiceRequest }) {
  const schedule = request.scheduleDays[0];
  return <Pressable onPress={() => router.push(`/caregiver-service-request/${request.id}` as Href)} style={({ pressed }) => [styles.card, pressed && styles.pressed]}>
    <View style={styles.top}><View style={styles.flex}><Text style={styles.name}>{request.responsible.name}</Text><Text style={styles.person}>Para cuidar de {request.assistedPerson.name}</Text></View><View style={[styles.status, request.status === 'PENDENTE' && styles.pending]}><Text style={styles.statusText}>{statusLabels[request.status]}</Text></View></View>
    <Text style={styles.meta}>{hiringLabels[request.hiringType]} • Início em {request.startDate.split('-').reverse().join('/')}</Text>
    {schedule ? <Text style={styles.meta}>{weekdayLabels[schedule.weekday]} • {schedule.startTime.slice(0,5)} às {schedule.endTime.slice(0,5)}</Text> : null}
    <View style={styles.location}><MapPin color={colors.mutedForeground} size={15} /><Text style={styles.meta}>{request.careAddress.neighborhood}, {request.careAddress.city}{request.distanceKm != null ? ` • ${request.distanceKm.toFixed(1).replace('.', ',')} km` : ''}</Text></View>
    {request.hasScheduleConflict ? <View style={styles.warning}><AlertTriangle color={colors.coral} size={16} /><Text style={styles.warningText}>Possível conflito de horário</Text></View> : null}
    <View style={styles.link}><Text style={styles.linkText}>Ver detalhes</Text><ArrowRight color={colors.primary} size={17} /></View>
  </Pressable>;
}
const styles = StyleSheet.create({ card: { gap: spacing.sm, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card }, pressed: { opacity: 0.78 }, top: { flexDirection: 'row', gap: spacing.sm, alignItems: 'flex-start' }, flex: { flex: 1 }, name: { fontFamily: fontFamily.bold, fontSize: 15, color: colors.foreground }, person: { fontFamily: fontFamily.regular, fontSize: 12, color: colors.mutedForeground }, meta: { fontFamily: fontFamily.medium, fontSize: 12, lineHeight: 18, color: colors.mutedForeground }, status: { paddingHorizontal: spacing.sm, paddingVertical: spacing.xs, borderRadius: radii.full, backgroundColor: colors.secondary }, pending: { backgroundColor: colors.sunny }, statusText: { fontFamily: fontFamily.bold, fontSize: 10, color: colors.foreground }, location: { flexDirection: 'row', alignItems: 'center', gap: spacing.xs }, warning: { flexDirection: 'row', alignItems: 'center', gap: spacing.xs, padding: spacing.sm, borderRadius: radii.md, backgroundColor: '#FFF1EB' }, warningText: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.coral }, link: { flexDirection: 'row', justifyContent: 'flex-end', alignItems: 'center', gap: spacing.xs }, linkText: { fontFamily: fontFamily.bold, fontSize: 12, color: colors.primary } });
