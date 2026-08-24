import { AlertCircle, CheckCircle2, ChevronRight, Clock3, MapPin, PlayCircle, Square, UserRound } from 'lucide-react-native';
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { TaskOccurrence } from '@/types/careTasks';
import type { AttendanceSummary } from '@/types/serviceAttendance';
import { formatDateTimeLocal, formatScheduleTime } from '@/utils/dateTime';

type Props = {
  attendance: AttendanceSummary | null;
  loading: boolean;
  error: string | null;
  actionLoading: boolean;
  nextCare: TaskOccurrence | null;
  careLoading: boolean;
  careError: boolean;
  onAction: () => void;
  onCarePress: () => void;
  onCareRetry: () => void;
  onDetails: () => void;
  onRetry: () => void;
};

export function AttendanceCard({ attendance, loading, error, actionLoading, nextCare, careLoading, careError, onAction, onCarePress, onCareRetry, onDetails, onRetry }: Props) {
  const active = attendance?.status === 'IN_PROGRESS' || attendance?.status === 'CAN_END';

  return (
    <View style={[styles.card, active && styles.activeCard]}>
      <View style={styles.heading}>
        <View style={styles.icon}><Clock3 color={colors.primary} size={21} /></View>
        <View style={styles.flex}><Text style={styles.eyebrow}>Resumo de hoje</Text><Text style={styles.title}>{attendance?.assistedPersonName ?? 'Serviço contratado'}</Text></View>
      </View>
      {loading ? <View style={styles.state}><ActivityIndicator color={colors.primary} /><Text style={styles.message}>Buscando o atendimento de hoje...</Text></View>
        : error ? <><Text style={styles.error}>{error}</Text><SmallButton label="Tentar novamente" onPress={onRetry} /></>
        : !attendance ? <View style={styles.state}><CheckCircle2 color={colors.primary} size={23} /><Text style={styles.message}>Nenhum atendimento programado para hoje.</Text></View>
        : <>
          <View style={styles.schedule}><Text style={styles.scheduleLabel}>Horário previsto</Text><Text style={styles.scheduleValue}>{formatScheduleTime(attendance.scheduledStartTime)} às {formatScheduleTime(attendance.scheduledEndTime)}</Text></View>
          {active ? <View style={styles.activeStatus}><View style={styles.activeDot} /><Text style={styles.activeStatusText}>Atendimento em andamento</Text></View>
            : <View style={styles.status}><Text style={styles.statusText}>{attendance.statusLabel}</Text></View>}
          {attendance.startRecord ? <Text style={styles.record}>Iniciado em {formatDateTimeLocal(attendance.startRecord.recordedAt)}</Text> : null}
          {attendance.endRecord ? <Text style={styles.record}>Encerrado em {formatDateTimeLocal(attendance.endRecord.recordedAt)}</Text> : null}
          <Text style={styles.message}>{attendance.actionMessage}</Text>
          {attendance.canStart || attendance.canEnd ? (
            <Pressable accessibilityRole="button" disabled={actionLoading} onPress={onAction} style={({ pressed }) => [styles.primaryButton, pressed && styles.pressed, actionLoading && styles.disabled]}>
              {actionLoading ? <ActivityIndicator color={colors.primaryForeground} /> : attendance.canStart ? <PlayCircle color={colors.primaryForeground} size={19} /> : <Square color={colors.primaryForeground} size={17} />}
              <Text style={styles.primaryText}>{actionLoading ? 'Obtendo localização...' : attendance.canStart ? 'Iniciar atendimento' : 'Encerrar atendimento'}</Text>
            </Pressable>
          ) : null}
          <Pressable accessibilityRole="button" onPress={onDetails} style={styles.detailsButton}><MapPin color={colors.primary} size={17} /><Text style={styles.detailsText}>Ver detalhes do atendimento</Text></Pressable>
          <View style={styles.divider} />
          {active ? <CareSummary care={nextCare} error={careError} loading={careLoading} onPress={onCarePress} onRetry={onCareRetry} />
            : <Text style={styles.lockedCare}>{attendance.status === 'ENDED'
              ? 'O atendimento foi encerrado. Os registros do dia continuam disponíveis nos detalhes.'
              : 'Inicie o atendimento para visualizar e registrar os cuidados de hoje.'}</Text>}
        </>}
    </View>
  );
}

function CareSummary({ care, loading, error, onPress, onRetry }: { care: TaskOccurrence | null; loading: boolean; error: boolean; onPress: () => void; onRetry: () => void }) {
  if (loading) return <View style={styles.state}><ActivityIndicator color={colors.primary} /><Text style={styles.message}>Buscando o próximo cuidado...</Text></View>;
  if (error) return <View style={styles.careState}><AlertCircle color={colors.destructive} size={21} /><Text style={styles.error}>Não foi possível carregar os cuidados de hoje.</Text><SmallButton label="Tentar novamente" onPress={onRetry} /></View>;
  if (!care) return <View style={styles.careState}><CheckCircle2 color={colors.primary} size={22} /><Text style={styles.message}>Nenhum cuidado pendente para este atendimento.</Text><SmallButton label="Ver cuidados do dia" onPress={onPress} /></View>;
  return <View style={styles.careContent}>
    <Text style={styles.careEyebrow}>Próximo cuidado</Text>
    <Text style={styles.careTitle}>{care.title}</Text>
    <View style={styles.careMeta}><UserRound color={colors.primary} size={15} /><Text style={styles.careMetaText}>{care.assistedPersonName}</Text><View style={styles.dot} /><Text style={styles.careMetaText}>{formatScheduleTime(care.scheduledTime)}</Text></View>
    <Pressable accessibilityRole="button" onPress={onPress} style={({ pressed }) => [styles.careButton, pressed && styles.pressed]}><Text style={styles.careButtonText}>Abrir cuidado</Text><ChevronRight color={colors.primaryForeground} size={18} /></Pressable>
  </View>;
}

function SmallButton({ label, onPress }: { label: string; onPress: () => void }) { return <Pressable onPress={onPress} style={styles.smallButton}><Text style={styles.detailsText}>{label}</Text></Pressable>; }

const styles = StyleSheet.create({
  card: { gap: spacing.md, padding: spacing.xl, borderRadius: radii.xxl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card }, activeCard: { borderColor: colors.primary, borderWidth: 2 },
  heading: { flexDirection: 'row', alignItems: 'center', gap: spacing.md }, icon: { width: 44, height: 44, alignItems: 'center', justifyContent: 'center', borderRadius: radii.full, backgroundColor: colors.secondary }, flex: { flex: 1, gap: spacing.xxs },
  eyebrow: { fontFamily: fontFamily.bold, fontSize: 11, textTransform: 'uppercase', color: colors.coral }, title: { fontFamily: fontFamily.extraBold, fontSize: 18, color: colors.foreground },
  state: { minHeight: 72, flexDirection: 'row', alignItems: 'center', gap: spacing.md }, schedule: { flexDirection: 'row', justifyContent: 'space-between', gap: spacing.md }, scheduleLabel: { fontFamily: fontFamily.semiBold, fontSize: 12, color: colors.mutedForeground }, scheduleValue: { fontFamily: fontFamily.bold, fontSize: 13, color: colors.foreground },
  status: { alignSelf: 'flex-start', paddingHorizontal: spacing.md, paddingVertical: spacing.xs, borderRadius: radii.full, backgroundColor: colors.secondary }, statusText: { fontFamily: fontFamily.bold, fontSize: 11, color: colors.primary },
  activeStatus: { alignSelf: 'flex-start', flexDirection: 'row', alignItems: 'center', gap: spacing.sm, paddingHorizontal: spacing.md, paddingVertical: spacing.sm, borderRadius: radii.full, backgroundColor: colors.mint }, activeDot: { width: 8, height: 8, borderRadius: radii.full, backgroundColor: '#287A4B' }, activeStatusText: { fontFamily: fontFamily.bold, fontSize: 11, color: '#287A4B' },
  record: { fontFamily: fontFamily.semiBold, fontSize: 12, color: colors.foreground }, message: { flex: 1, fontFamily: fontFamily.medium, fontSize: 12, lineHeight: 18, color: colors.mutedForeground }, error: { fontFamily: fontFamily.semiBold, fontSize: 12, color: colors.destructive },
  primaryButton: { minHeight: 50, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: spacing.sm, paddingHorizontal: spacing.lg, borderRadius: radii.lg, backgroundColor: colors.primary }, primaryText: { fontFamily: fontFamily.bold, fontSize: 14, color: colors.primaryForeground },
  detailsButton: { minHeight: 42, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: spacing.xs }, detailsText: { fontFamily: fontFamily.bold, fontSize: 12, color: colors.primary }, smallButton: { alignSelf: 'flex-start', paddingVertical: spacing.sm }, pressed: { opacity: 0.84 }, disabled: { opacity: 0.65 },
  divider: { height: 1, backgroundColor: colors.border }, lockedCare: { fontFamily: fontFamily.medium, fontSize: 12, lineHeight: 19, color: colors.mutedForeground }, careState: { minHeight: 68, alignItems: 'flex-start', gap: spacing.sm }, careContent: { gap: spacing.sm }, careEyebrow: { fontFamily: fontFamily.bold, fontSize: 10, letterSpacing: 0.5, textTransform: 'uppercase', color: colors.coral }, careTitle: { fontFamily: fontFamily.extraBold, fontSize: 17, lineHeight: 23, color: colors.foreground }, careMeta: { flexDirection: 'row', flexWrap: 'wrap', alignItems: 'center', gap: spacing.sm }, careMetaText: { fontFamily: fontFamily.semiBold, fontSize: 12, color: colors.mutedForeground }, dot: { width: 4, height: 4, borderRadius: radii.full, backgroundColor: colors.mutedForeground }, careButton: { minHeight: 46, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: spacing.xs, marginTop: spacing.xs, borderRadius: radii.lg, backgroundColor: colors.primary }, careButtonText: { fontFamily: fontFamily.bold, fontSize: 13, color: colors.primaryForeground },
});
