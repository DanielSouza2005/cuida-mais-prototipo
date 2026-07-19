import { useState } from 'react';
import { router, useLocalSearchParams, type Href } from 'expo-router';
import { AlertTriangle, ArrowLeft, CheckCircle2 } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { TerminationContractSummary } from '@/components/termination-contract-summary';
import { ApiError } from '@/services/api';
import { cancelContractBeforeStart, getTerminationFlow, saveTerminationResult, terminateContract } from '@/services/contractTerminationService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import { contractStatusLabels, formatContractDate } from '@/utils/contractsHistoryLabels';
import { localIsoDate, terminationTypeLabels } from '@/utils/contractTerminationLabels';

export default function ContractTerminationReviewScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const [confirming, setConfirming] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const flow = id ? getTerminationFlow(id) : undefined;
  if (!flow) return <ScreenContainer contentStyle={styles.center}><Text style={styles.stateTitle}>Dados da revisão indisponíveis</Text><Text style={styles.stateText}>Volte ao formulário e confira as informações.</Text><PrimaryButton label="Voltar ao formulário" variant="secondary" onPress={() => router.back()} /></ScreenContainer>;
  const { form: contract, draft } = flow;
  const cancellation = contract.actionType === 'CANCELLATION';
  const expectedStatus = cancellation ? 'CANCELADA' : draft.effectiveEndDate > localIsoDate() ? 'ENCERRAMENTO_AGENDADO' : 'ENCERRADA';

  async function confirm() {
    if (!id || confirming) return;
    setConfirming(true); setError(null);
    try {
      const result = cancellation ? await cancelContractBeforeStart(id, draft) : await terminateContract(id, draft);
      saveTerminationResult(result);
      router.replace(`/contract-termination-success/${id}` as Href);
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível concluir esta ação. Tente novamente.');
      setConfirming(false);
    }
  }

  return (
    <ScreenContainer contentStyle={styles.content}>
      <AppHeader showBack title={cancellation ? 'Revisar cancelamento' : 'Revisar encerramento'} subtitle="Confira as informações antes de confirmar." />
      <TerminationContractSummary contract={contract} title="Contratação" />

      <View style={styles.card}>
        <Text style={styles.title}>{cancellation ? 'Cancelamento' : 'Encerramento'}</Text>
        <Row label={cancellation ? 'Tipo de cancelamento' : 'Tipo de encerramento'} value={terminationTypeLabels[draft.terminationType]} />
        <Row label={cancellation ? 'Data do cancelamento' : 'Data efetiva'} value={formatContractDate(draft.effectiveEndDate)} />
        <Row label="Motivo" value={draft.reason} />
        {draft.additionalNotes ? <Row label="Observações complementares" value={draft.additionalNotes} /> : null}
      </View>

      <View style={styles.resultCard}><CheckCircle2 color={colors.mintForeground} size={23} /><View style={styles.flex}><Text style={styles.resultLabel}>Status após a confirmação</Text><Text style={styles.resultValue}>{contractStatusLabels[expectedStatus]}</Text></View></View>
      {!cancellation ? <View style={styles.warning}><AlertTriangle color="#A54E31" size={20} /><Text style={styles.warningText}>Depois de encerrada, a contratação não poderá voltar ao status ativo.</Text></View> : null}
      {error ? <Text accessibilityRole="alert" style={styles.error}>{error}</Text> : null}
      <PrimaryButton label={cancellation ? 'Confirmar cancelamento' : 'Confirmar encerramento'} loading={confirming} onPress={confirm} />
      <PrimaryButton label="Voltar e editar" icon={ArrowLeft} variant="secondary" disabled={confirming} onPress={() => router.back()} />
    </ScreenContainer>
  );
}

function Row({ label, value }: { label: string; value: string }) { return <View style={styles.row}><Text style={styles.label}>{label}</Text><Text style={styles.value}>{value}</Text></View>; }
const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, gap: spacing.lg }, center: { flexGrow: 1, alignItems: 'center', justifyContent: 'center', gap: spacing.md, padding: spacing.xl }, stateTitle: { textAlign: 'center', fontFamily: fontFamily.bold, fontSize: 17, color: colors.foreground }, stateText: { textAlign: 'center', fontFamily: fontFamily.regular, fontSize: 13, lineHeight: 20, color: colors.mutedForeground },
  card: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card }, title: { fontFamily: fontFamily.extraBold, fontSize: 17, color: colors.foreground }, row: { gap: spacing.xxs }, label: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground }, value: { fontFamily: fontFamily.medium, fontSize: 13, lineHeight: 20, color: colors.foreground },
  resultCard: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, backgroundColor: colors.mint }, flex: { flex: 1 }, resultLabel: { fontFamily: fontFamily.medium, fontSize: 11, color: colors.mintForeground }, resultValue: { fontFamily: fontFamily.extraBold, fontSize: 16, color: colors.mintForeground },
  warning: { flexDirection: 'row', alignItems: 'flex-start', gap: spacing.md, padding: spacing.lg, borderRadius: radii.lg, backgroundColor: '#FDEDE7' }, warningText: { flex: 1, fontFamily: fontFamily.medium, fontSize: 12, lineHeight: 19, color: '#833E28' }, error: { textAlign: 'center', fontFamily: fontFamily.medium, fontSize: 12, color: colors.destructive },
});
