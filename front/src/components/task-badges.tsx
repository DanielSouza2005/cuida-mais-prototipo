import { StyleSheet, Text, View } from 'react-native';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';
import type { TaskOccurrenceStatus, TaskPriority, TaskSeriesStatus } from '@/types/careTasks';
import { taskOccurrenceStatusLabels, taskPriorityLabels, taskSeriesStatusLabels } from '@/utils/careTaskLabels';

export function TaskStatusBadge({ status }: { status: TaskSeriesStatus | TaskOccurrenceStatus }) {
  const occurrence = ['PENDENTE', 'CONCLUIDA', 'ATRASADA', 'NAO_REALIZADA'].includes(status);
  const label = occurrence ? taskOccurrenceStatusLabels[status as TaskOccurrenceStatus] : taskSeriesStatusLabels[status as TaskSeriesStatus];
  return <View style={[styles.badge, status === 'CONCLUIDA' || status === 'ATIVA' ? styles.success : null, status === 'ATRASADA' || status === 'NAO_REALIZADA' ? styles.warning : null, status === 'CANCELADA' ? styles.neutral : null]}><Text style={styles.text}>{label}</Text></View>;
}

export function TaskPriorityBadge({ priority }: { priority: TaskPriority }) {
  return <View style={[styles.priority, priority === 'ALTA' && styles.high]}><Text style={styles.priorityText}>Prioridade {taskPriorityLabels[priority].toLowerCase()}</Text></View>;
}

const styles = StyleSheet.create({
  badge: { alignSelf: 'flex-start', paddingHorizontal: spacing.sm, paddingVertical: spacing.xs, borderRadius: radii.full, backgroundColor: colors.secondary },
  success: { backgroundColor: colors.mint }, warning: { backgroundColor: '#FFF0D8' }, neutral: { backgroundColor: colors.muted },
  text: { fontFamily: fontFamily.bold, fontSize: 10, color: colors.secondaryForeground },
  priority: { alignSelf: 'flex-start', paddingHorizontal: spacing.sm, paddingVertical: spacing.xs, borderRadius: radii.full, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card },
  high: { borderColor: colors.coral }, priorityText: { fontFamily: fontFamily.semiBold, fontSize: 10, color: colors.foreground },
});
