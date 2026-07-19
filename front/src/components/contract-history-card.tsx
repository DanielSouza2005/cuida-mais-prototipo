import { router, type Href } from 'expo-router';
import { ArrowRight, CalendarDays, Clock3, UserRound } from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { ProfileAvatar } from '@/components/profile-avatar';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { ContractHistoryItem, ContractHistoryStatus } from '@/types/contractsHistory';
import { contractHiringLabels, contractStatusLabels, formatContractDate, formatContractDateTime, formatContractSchedule, getContractReason } from '@/utils/contractsHistoryLabels';

const statusColors: Record<ContractHistoryStatus, { background: string; foreground: string }> = {
  PENDENTE: { background: '#FFF4C7', foreground: '#755B00' },
  ACEITA: { background: colors.mint, foreground: colors.mintForeground },
  AGENDADA: { background: colors.secondary, foreground: colors.primary },
  ATIVA: { background: colors.mint, foreground: colors.mintForeground },
  ENCERRAMENTO_AGENDADO: { background: '#FFF4C7', foreground: '#755B00' },
  ENCERRADA: { background: colors.muted, foreground: colors.secondaryForeground },
  FINALIZADA: { background: colors.muted, foreground: colors.secondaryForeground },
  REJEITADA: { background: '#FDE8E5', foreground: colors.destructive },
  CANCELADA: { background: '#FDEDE7', foreground: '#A54E31' },
  EXPIRADA: { background: '#EEEAF5', foreground: '#665577' },
};

export function ContractStatusBadge({ status }: { status: ContractHistoryStatus }) {
  const palette = statusColors[status];
  return <View style={[styles.badge, { backgroundColor: palette.background }]}><Text style={[styles.badgeText, { color: palette.foreground }]}>{contractStatusLabels[status]}</Text></View>;
}

export function ContractHistoryCard({ item, viewer = 'RESPONSAVEL' }: { item: ContractHistoryItem; viewer?: 'RESPONSAVEL' | 'CUIDADOR' }) {
  const schedule = item.scheduleSummary || formatContractSchedule(item);
  const reason = getContractReason(item);
  const initials = item.participant.name.split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase();
  const period = `${formatContractDate(item.startDate)}${item.endDate ? ` a ${formatContractDate(item.endDate)}` : ''}`;

  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={`Ver detalhes do registro com ${item.participant.name}`}
      onPress={() => router.push((item.itemType === 'SERVICE_REQUEST' && viewer === 'CUIDADOR' ? `/caregiver-service-request/${item.id}` : `/responsible-contract/${item.id}?itemType=${item.itemType}`) as Href)}
      style={({ pressed }) => [styles.card, pressed && styles.pressed]}
    >
      <View style={styles.topRow}>
        <ProfileAvatar imageUrl={item.participant.profilePhotoUrl} initials={initials} size={46} />
        <View style={styles.personCopy}><Text style={styles.name}>{item.participant.name}</Text><Text style={styles.assisted}>Pessoa assistida: {item.assistedPerson.name}</Text></View>
        <ContractStatusBadge status={item.status} />
      </View>

      <View style={styles.divider} />
      <View style={styles.metaRow}><CalendarDays color={colors.primary} size={16} /><Text style={styles.meta}><Text style={styles.metaStrong}>{contractHiringLabels[item.hiringType]}</Text> · {period}</Text></View>
      {schedule ? <View style={styles.metaRow}><Clock3 color={colors.primary} size={16} /><Text style={styles.meta}>{schedule}</Text></View> : null}
      <View style={styles.metaRow}><UserRound color={colors.primary} size={16} /><Text style={styles.meta}>Atualizado em {formatContractDateTime(item.updatedAt)}</Text></View>
      {reason ? <View style={styles.reason}><Text style={styles.reasonLabel}>Motivo</Text><Text numberOfLines={2} style={styles.reasonText}>{reason}</Text></View> : null}
      <View style={styles.link}><Text style={styles.linkText}>Ver detalhes</Text><ArrowRight color={colors.primary} size={17} /></View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card },
  pressed: { opacity: 0.8, transform: [{ scale: 0.995 }] },
  topRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.md },
  personCopy: { flex: 1, gap: spacing.xxs },
  name: { fontFamily: fontFamily.bold, fontSize: 15, color: colors.foreground },
  assisted: { fontFamily: fontFamily.regular, fontSize: 11, lineHeight: 16, color: colors.mutedForeground },
  badge: { alignSelf: 'flex-start', paddingHorizontal: spacing.sm, paddingVertical: spacing.xs, borderRadius: radii.full },
  badgeText: { fontFamily: fontFamily.bold, fontSize: 10 },
  divider: { height: 1, backgroundColor: colors.border },
  metaRow: { flexDirection: 'row', alignItems: 'flex-start', gap: spacing.sm },
  meta: { flex: 1, fontFamily: fontFamily.medium, fontSize: 12, lineHeight: 19, color: colors.mutedForeground },
  metaStrong: { fontFamily: fontFamily.semiBold, color: colors.foreground },
  reason: { gap: spacing.xxs, padding: spacing.md, borderRadius: radii.md, backgroundColor: colors.muted },
  reasonLabel: { fontFamily: fontFamily.bold, fontSize: 10, textTransform: 'uppercase', letterSpacing: 0.5, color: colors.mutedForeground },
  reasonText: { fontFamily: fontFamily.medium, fontSize: 12, lineHeight: 18, color: colors.foreground },
  link: { flexDirection: 'row', justifyContent: 'flex-end', alignItems: 'center', gap: spacing.xs },
  linkText: { fontFamily: fontFamily.bold, fontSize: 12, color: colors.primary },
});
