import { Clock3 } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { ContractStatusBadge } from '@/components/contract-history-card';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { ContractTerminationFormData } from '@/types/contractTermination';
import { contractHiringLabels, contractWeekdayLabels, formatContractDate } from '@/utils/contractsHistoryLabels';
import { formatScheduleTime } from '@/utils/dateTime';

const weekdayOrder = ['SEGUNDA', 'TERCA', 'QUARTA', 'QUINTA', 'SEXTA', 'SABADO', 'DOMINGO'];

export function TerminationContractSummary({ contract, title = 'Resumo da contratação' }: { contract: ContractTerminationFormData; title?: string }) {
  const otherPartyLabel = contract.participantRole === 'RESPONSAVEL' ? 'Cuidador' : 'Responsável';
  const schedules = [...contract.scheduleDays].sort((a, b) => weekdayOrder.indexOf(a.weekday) - weekdayOrder.indexOf(b.weekday));

  return (
    <View style={styles.card}>
      <View style={styles.heading}><Text style={styles.title}>{title}</Text><ContractStatusBadge status={contract.status} /></View>
      <Row label={otherPartyLabel} value={contract.otherPartyName} />
      <Row label="Pessoa assistida" value={contract.assistedPersonName} />
      <Row label="Tipo de contratação" value={contractHiringLabels[contract.hiringType]} />
      <Row label="Data de início" value={formatContractDate(contract.startDate)} />
      {contract.endDate ? <Row label="Data final prevista" value={formatContractDate(contract.endDate)} /> : null}
      {schedules.length ? <View style={styles.block}><Text style={styles.label}>Dias e horários</Text>{schedules.map((schedule) => <View key={`${schedule.weekday}-${schedule.startTime}`} style={styles.schedule}><Clock3 color={colors.primary} size={14} /><Text style={styles.value}>{contractWeekdayLabels[schedule.weekday]} · {formatScheduleTime(schedule.startTime)} às {formatScheduleTime(schedule.endTime)}</Text></View>)}</View> : null}
    </View>
  );
}

function Row({ label, value }: { label: string; value: string }) { return <View style={styles.row}><Text style={styles.label}>{label}</Text><Text style={styles.value}>{value}</Text></View>; }

const styles = StyleSheet.create({
  card: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card },
  heading: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: spacing.md },
  title: { flex: 1, fontFamily: fontFamily.extraBold, fontSize: 17, color: colors.foreground },
  row: { gap: spacing.xxs }, block: { gap: spacing.sm },
  label: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground },
  value: { flex: 1, fontFamily: fontFamily.medium, fontSize: 13, lineHeight: 20, color: colors.foreground },
  schedule: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
});
