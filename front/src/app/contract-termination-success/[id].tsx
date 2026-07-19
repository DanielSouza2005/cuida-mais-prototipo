import { router, useLocalSearchParams, type Href } from 'expo-router';
import { Bell, Check, FileText } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { getTerminationFlow, getTerminationResult } from '@/services/contractTerminationService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import { contractStatusLabels, formatContractDate } from '@/utils/contractsHistoryLabels';

export default function ContractTerminationSuccessScreen() {
  const { user } = useAuth();
  const { id } = useLocalSearchParams<{ id: string }>();
  const result = id ? getTerminationResult(id) : undefined;
  const flow = id ? getTerminationFlow(id) : undefined;
  const contractsTab = (user?.userType === 'caregiver' ? '/(tabs)/agenda' : '/(tabs)/contratacoes') as Href;
  if (!result || !flow || !id) return <ScreenContainer contentStyle={styles.center}><Text style={styles.title}>Resultado indisponível</Text><PrimaryButton label="Voltar para contratações" onPress={() => router.replace(contractsTab)} /></ScreenContainer>;

  const cancellation = result.status === 'CANCELADA';
  const scheduled = result.status === 'ENCERRAMENTO_AGENDADO';
  const title = cancellation ? 'Contratação cancelada' : scheduled ? 'Encerramento agendado' : 'Serviço encerrado';
  const message = cancellation ? 'A contratação foi cancelada antes do início.' : scheduled ? 'O encerramento foi agendado para a data informada.' : 'O serviço foi encerrado com sucesso.';
  const otherPartyLabel = flow.form.participantRole === 'RESPONSAVEL' ? 'Cuidador' : 'Responsável';

  return (
    <ScreenContainer contentStyle={styles.content}>
      <View style={styles.hero}><View style={styles.successIcon}><Check color={colors.primaryForeground} size={34} strokeWidth={3} /></View><Text style={styles.title}>{title}</Text><Text style={styles.message}>{message}</Text></View>
      <View style={styles.card}><View style={styles.cardHeading}><FileText color={colors.primary} size={20} /><Text style={styles.cardTitle}>Resumo</Text></View><Row label="Pessoa assistida" value={flow.form.assistedPersonName} /><Row label={otherPartyLabel} value={flow.form.otherPartyName} /><Row label={cancellation ? 'Data do cancelamento' : 'Data efetiva'} value={formatContractDate(result.effectiveEndDate)} /><Row label="Status" value={contractStatusLabels[result.status]} /><Row label="Motivo" value={result.reason} /></View>
      <View style={styles.notice}><Bell color={colors.primary} size={20} /><Text style={styles.noticeText}>A outra parte será notificada sobre esta alteração.</Text></View>
      <PrimaryButton label="Voltar para contratações" onPress={() => router.replace(contractsTab)} />
      <PrimaryButton label="Ver detalhe da contratação" variant="secondary" onPress={() => router.replace(`/responsible-contract/${id}?itemType=CARE_CONTRACT` as Href)} />
    </ScreenContainer>
  );
}

function Row({ label, value }: { label: string; value: string }) { return <View style={styles.row}><Text style={styles.label}>{label}</Text><Text style={styles.value}>{value}</Text></View>; }
const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, gap: spacing.lg }, center: { flexGrow: 1, alignItems: 'center', justifyContent: 'center', gap: spacing.lg, padding: spacing.xl }, hero: { alignItems: 'center', gap: spacing.sm, paddingVertical: spacing.xl }, successIcon: { width: 72, height: 72, alignItems: 'center', justifyContent: 'center', marginBottom: spacing.sm, borderRadius: radii.full, backgroundColor: colors.accent, ...shadows.glow }, title: { textAlign: 'center', fontFamily: fontFamily.extraBold, fontSize: 25, color: colors.foreground }, message: { textAlign: 'center', fontFamily: fontFamily.regular, fontSize: 14, lineHeight: 22, color: colors.mutedForeground },
  card: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card }, cardHeading: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm }, cardTitle: { fontFamily: fontFamily.extraBold, fontSize: 17, color: colors.foreground }, row: { gap: spacing.xxs }, label: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground }, value: { fontFamily: fontFamily.medium, fontSize: 13, lineHeight: 20, color: colors.foreground }, notice: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, padding: spacing.lg, borderRadius: radii.lg, backgroundColor: colors.secondary }, noticeText: { flex: 1, fontFamily: fontFamily.medium, fontSize: 12, lineHeight: 19, color: colors.secondaryForeground },
});
