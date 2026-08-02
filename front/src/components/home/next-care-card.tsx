import { AlertCircle, CheckCircle2, ChevronRight, Clock3, UserRound } from 'lucide-react-native';
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { TaskOccurrence } from '@/types/careTasks';
import { formatScheduleTime } from '@/utils/dateTime';

type Props = {
  care: TaskOccurrence | null;
  isCaregiver: boolean;
  loading?: boolean;
  error?: boolean;
  onPress: () => void;
  onRetry: () => void;
};

export function NextCareCard({ care, isCaregiver, loading, error, onPress, onRetry }: Props) {
  return (
    <View style={styles.card}>
      <Text style={styles.eyebrow}>Resumo de hoje</Text>
      {loading ? (
        <View style={styles.stateRow}>
          <ActivityIndicator color={colors.primaryForeground} />
          <Text style={styles.stateText}>Buscando o próximo cuidado...</Text>
        </View>
      ) : error ? (
        <View style={styles.stateContent}>
          <AlertCircle color={colors.primaryForeground} size={26} />
          <Text style={styles.emptyTitle}>Não foi possível carregar o resumo.</Text>
          <Pressable accessibilityRole="button" onPress={onRetry} style={styles.secondaryButton}>
            <Text style={styles.secondaryButtonText}>Tentar novamente</Text>
          </Pressable>
        </View>
      ) : care ? (
        <>
          <View style={styles.headingRow}>
            <View style={styles.headingCopy}>
              <Text style={styles.title}>{care.title}</Text>
              <View style={[styles.status, care.status === 'ATRASADA' && styles.overdueStatus]}>
                <Text style={styles.statusText}>{care.status === 'ATRASADA' ? 'Atrasado' : 'Pendente'}</Text>
              </View>
            </View>
            <View style={styles.iconCircle}>
              <Clock3 color={colors.primary} size={23} strokeWidth={2.4} />
            </View>
          </View>
          <View style={styles.metaRow}>
            <UserRound color={colors.primaryForeground} size={16} />
            <Text style={styles.metaText}>{care.assistedPersonName}</Text>
            <View style={styles.dot} />
            <Text style={styles.metaText}>{formatScheduleTime(care.scheduledTime)}</Text>
          </View>
          <Pressable accessibilityRole="button" onPress={onPress} style={({ pressed }) => [styles.button, pressed && styles.pressed]}>
            <Text style={styles.buttonText}>{isCaregiver ? 'Confirmar agora' : 'Ver cuidado'}</Text>
            <ChevronRight color={colors.primary} size={18} strokeWidth={2.5} />
          </Pressable>
        </>
      ) : (
        <View style={styles.stateContent}>
          <CheckCircle2 color={colors.primaryForeground} size={28} />
          <Text style={styles.emptyTitle}>Nenhum cuidado pendente para hoje</Text>
          <Text style={styles.stateText}>Tudo certo por aqui. Consulte os cuidados do dia quando quiser.</Text>
          <Pressable accessibilityRole="button" onPress={onPress} style={styles.secondaryButton}>
            <Text style={styles.secondaryButtonText}>Ver cuidados do dia</Text>
          </Pressable>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  card: { gap: spacing.md, padding: spacing.xl, borderRadius: radii.xxl, backgroundColor: colors.primary, ...shadows.soft },
  eyebrow: { fontFamily: fontFamily.bold, fontSize: 11, letterSpacing: 0.7, textTransform: 'uppercase', color: '#DDF2FF' },
  headingRow: { flexDirection: 'row', alignItems: 'flex-start', gap: spacing.md },
  headingCopy: { flex: 1, alignItems: 'flex-start', gap: spacing.sm },
  title: { fontFamily: fontFamily.extraBold, fontSize: 21, lineHeight: 27, color: colors.primaryForeground },
  status: { paddingHorizontal: spacing.sm, paddingVertical: spacing.xs, borderRadius: radii.full, backgroundColor: 'rgba(255,255,255,0.18)' },
  overdueStatus: { backgroundColor: '#A93A35' },
  statusText: { fontFamily: fontFamily.bold, fontSize: 10, color: colors.primaryForeground },
  iconCircle: { width: 46, height: 46, alignItems: 'center', justifyContent: 'center', borderRadius: radii.full, backgroundColor: colors.primaryForeground },
  metaRow: { flexDirection: 'row', flexWrap: 'wrap', alignItems: 'center', gap: spacing.sm },
  metaText: { fontFamily: fontFamily.semiBold, fontSize: 13, color: colors.primaryForeground },
  dot: { width: 4, height: 4, borderRadius: radii.full, backgroundColor: '#DDF2FF' },
  button: { minHeight: 48, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: spacing.xs, paddingHorizontal: spacing.lg, borderRadius: radii.lg, backgroundColor: colors.primaryForeground },
  buttonText: { fontFamily: fontFamily.bold, fontSize: 14, color: colors.primary },
  pressed: { opacity: 0.84, transform: [{ scale: 0.99 }] },
  stateRow: { minHeight: 92, flexDirection: 'row', alignItems: 'center', gap: spacing.md },
  stateContent: { minHeight: 118, alignItems: 'flex-start', justifyContent: 'center', gap: spacing.sm },
  stateText: { flexShrink: 1, fontFamily: fontFamily.medium, fontSize: 12.5, lineHeight: 19, color: '#EAF7FF' },
  emptyTitle: { fontFamily: fontFamily.extraBold, fontSize: 18, lineHeight: 24, color: colors.primaryForeground },
  secondaryButton: { minHeight: 40, justifyContent: 'center', marginTop: spacing.xs, paddingHorizontal: spacing.md, borderRadius: radii.md, backgroundColor: 'rgba(255,255,255,0.18)' },
  secondaryButtonText: { fontFamily: fontFamily.bold, fontSize: 12, color: colors.primaryForeground },
});
