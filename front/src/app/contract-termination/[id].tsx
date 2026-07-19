import { useEffect, useMemo, useState } from 'react';
import { router, useLocalSearchParams, type Href } from 'expo-router';
import { CalendarClock, Check, Info } from 'lucide-react-native';
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { DatePickerField } from '@/components/date-picker-field';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { TerminationContractSummary } from '@/components/termination-contract-summary';
import { ApiError } from '@/services/api';
import { getTerminationFlow, getTerminationForm, saveTerminationFlow } from '@/services/contractTerminationService';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';
import type { ContractTerminationFormData, TerminationType } from '@/types/contractTermination';
import { displayDateToIso, isoDateToDisplay, localIsoDate, terminationTypeLabels } from '@/utils/contractTerminationLabels';

type Errors = { type?: string; date?: string; reason?: string };

export default function ContractTerminationFormScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const [contract, setContract] = useState<ContractTerminationFormData | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [terminationType, setTerminationType] = useState<TerminationType | undefined>();
  const [effectiveEndDate, setEffectiveEndDate] = useState('');
  const [reason, setReason] = useState('');
  const [additionalNotes, setAdditionalNotes] = useState('');
  const [errors, setErrors] = useState<Errors>({});

  useEffect(() => {
    if (!id) { setLoading(false); return; }
    getTerminationForm(id).then((result) => {
      setContract(result);
      const saved = getTerminationFlow(id);
      if (saved) {
        setTerminationType(saved.draft.terminationType); setEffectiveEndDate(isoDateToDisplay(saved.draft.effectiveEndDate)); setReason(saved.draft.reason); setAdditionalNotes(saved.draft.additionalNotes ?? '');
      } else if (result.actionType === 'CANCELLATION') {
        setTerminationType('CANCELAMENTO_ANTES_INICIO'); setEffectiveEndDate(isoDateToDisplay(localIsoDate()));
      }
    }).catch((error) => setLoadError(error instanceof ApiError ? error.message : 'Não foi possível carregar os dados da contratação.')).finally(() => setLoading(false));
  }, [id]);

  const cancellation = contract?.actionType === 'CANCELLATION';
  const types = useMemo(() => contract?.allowedTerminationTypes ?? [], [contract]);
  const today = useMemo(() => localIsoDate(), []);
  const minimumIsoDate = contract && contract.startDate > today && !cancellation ? contract.startDate : today;
  const minimumDate = new Date(`${minimumIsoDate}T12:00:00`);
  const futureDate = Boolean(effectiveEndDate && displayDateToIso(effectiveEndDate) > today);

  function selectType(type: TerminationType) {
    setTerminationType(type); setErrors((current) => ({ ...current, type: undefined }));
    if (type === 'NA_DATA_PREVISTA' && contract?.endDate) setEffectiveEndDate(isoDateToDisplay(contract.endDate));
  }

  function continueToReview() {
    if (!contract || !id) return;
    const nextErrors: Errors = {};
    const isoDate = displayDateToIso(effectiveEndDate);
    if (!terminationType) nextErrors.type = cancellation ? 'Selecione o tipo de cancelamento.' : 'Selecione o tipo de encerramento.';
    if (!cancellation && !isoDate) nextErrors.date = 'Informe a data efetiva de término.';
    else if (!cancellation && isoDate < today) nextErrors.date = 'A data efetiva não pode ser anterior à data atual.';
    else if (!cancellation && isoDate < contract.startDate) nextErrors.date = 'A data efetiva não pode ser anterior à data de início da contratação.';
    if (!reason.trim()) nextErrors.reason = 'Informe o motivo.';
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length) return;
    saveTerminationFlow(contract, { contractId: id, terminationType: terminationType!, effectiveEndDate: cancellation ? today : isoDate, reason: reason.trim(), additionalNotes: additionalNotes.trim() || undefined });
    router.push(`/contract-termination-review/${id}` as Href);
  }

  if (loading) return <ScreenContainer contentStyle={styles.center}><ActivityIndicator color={colors.primary} /><Text style={styles.stateText}>Carregando contratação...</Text></ScreenContainer>;
  if (!contract) return <State title="Não foi possível carregar" message={loadError ?? 'Não foi possível carregar os dados da contratação.'} />;
  if (contract.actionType === 'NONE') return <State title="Ação indisponível" message="Esta contratação não pode ser encerrada ou cancelada." />;

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <AppHeader showBack title={cancellation ? 'Cancelar contratação' : 'Encerrar serviço'} subtitle={cancellation ? 'Informe o motivo do cancelamento antes do início.' : 'Informe a data efetiva e o motivo do encerramento.'} />
      <TerminationContractSummary contract={contract} />

      <Section title={cancellation ? 'Tipo de cancelamento' : 'Tipo de encerramento'} required>
        <View style={styles.options}>{types.map((type) => {
          const selected = terminationType === type;
          const recommended = type === (contract.participantRole === 'RESPONSAVEL' ? 'ANTECIPADO_RESPONSAVEL' : 'ANTECIPADO_CUIDADOR');
          return <Pressable key={type} accessibilityRole="radio" accessibilityState={{ checked: selected }} onPress={() => selectType(type)} style={[styles.option, selected && styles.optionSelected]}><View style={[styles.radio, selected && styles.radioSelected]}>{selected ? <Check color={colors.primaryForeground} size={13} strokeWidth={3} /> : null}</View><View style={styles.optionCopy}><Text style={[styles.optionText, selected && styles.optionTextSelected]}>{terminationTypeLabels[type]}</Text>{recommended ? <Text style={styles.recommended}>Corresponde ao seu perfil</Text> : null}</View></Pressable>;
        })}</View>
        {errors.type ? <ErrorText message={errors.type} /> : null}
      </Section>

      <Section title={cancellation ? 'Data do cancelamento' : 'Data efetiva de término'}>
        {cancellation ? <View style={styles.readOnlyDate}><CalendarClock color={colors.primary} size={19} /><View><Text style={styles.readOnlyLabel}>Data do cancelamento</Text><Text style={styles.readOnlyValue}>Hoje, {isoDateToDisplay(today)}</Text></View></View> : <DatePickerField label="Data efetiva de término" required value={effectiveEndDate} minimumDate={minimumDate} error={errors.date} onChange={(value) => { setEffectiveEndDate(value); setErrors((current) => ({ ...current, date: undefined })); }} />}
        {!cancellation && futureDate ? <View style={styles.futureNotice}><CalendarClock color="#755B00" size={18} /><Text style={styles.futureText}>O encerramento ficará agendado até a data informada.</Text></View> : null}
        {!cancellation && effectiveEndDate && !futureDate ? <Text style={styles.helper}>Ao confirmar para hoje, o serviço será encerrado imediatamente.</Text> : null}
      </Section>

      <Section title="Informações do pedido">
        <AppTextInput label="Motivo" required multiline numberOfLines={4} value={reason} placeholder={cancellation ? 'Descreva o motivo do cancelamento.' : 'Descreva o motivo do encerramento.'} visualState={errors.reason ? 'error' : 'default'} onChangeText={(value) => { setReason(value); setErrors((current) => ({ ...current, reason: undefined })); }} />
        {errors.reason ? <ErrorText message={errors.reason} /> : null}
        <AppTextInput label="Observações complementares" optional multiline numberOfLines={4} value={additionalNotes} placeholder="Informe detalhes adicionais, se necessário." onChangeText={setAdditionalNotes} />
      </Section>

      <View style={styles.historyNotice}><Info color={colors.primary} size={20} /><Text style={styles.historyText}>{cancellation ? 'O cancelamento não excluirá o histórico da solicitação.' : 'O encerramento não excluirá registros, mensagens ou dados históricos vinculados ao serviço.'}</Text></View>
      <PrimaryButton label={cancellation ? 'Revisar cancelamento' : 'Revisar encerramento'} onPress={continueToReview} />
    </ScreenContainer>
  );
}

function Section({ title, required, children }: { title: string; required?: boolean; children: React.ReactNode }) { return <View style={styles.section}><Text style={styles.sectionTitle}>{title}{required ? <Text style={styles.required}> *</Text> : null}</Text>{children}</View>; }
function ErrorText({ message }: { message: string }) { return <Text accessibilityRole="alert" style={styles.error}>{message}</Text>; }
function State({ title, message }: { title: string; message: string }) { return <ScreenContainer contentStyle={styles.center}><Text style={styles.stateTitle}>{title}</Text><Text style={styles.stateText}>{message}</Text><PrimaryButton label="Voltar" variant="secondary" onPress={() => router.back()} /></ScreenContainer>; }

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, gap: spacing.lg }, center: { flexGrow: 1, alignItems: 'center', justifyContent: 'center', gap: spacing.md, padding: spacing.xl },
  stateTitle: { fontFamily: fontFamily.bold, fontSize: 17, color: colors.foreground }, stateText: { textAlign: 'center', fontFamily: fontFamily.regular, fontSize: 13, color: colors.mutedForeground },
  section: { gap: spacing.md }, sectionTitle: { fontFamily: fontFamily.extraBold, fontSize: 17, color: colors.foreground }, required: { color: colors.destructive },
  options: { gap: spacing.sm }, option: { minHeight: 58, flexDirection: 'row', alignItems: 'center', gap: spacing.md, padding: spacing.md, borderRadius: radii.lg, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card }, optionSelected: { borderColor: colors.primary, backgroundColor: colors.secondary },
  radio: { width: 22, height: 22, alignItems: 'center', justifyContent: 'center', borderRadius: radii.full, borderWidth: 2, borderColor: colors.border }, radioSelected: { borderColor: colors.primary, backgroundColor: colors.primary }, optionCopy: { flex: 1, gap: spacing.xxs }, optionText: { fontFamily: fontFamily.semiBold, fontSize: 13, lineHeight: 19, color: colors.foreground }, optionTextSelected: { color: colors.primary }, recommended: { fontFamily: fontFamily.medium, fontSize: 10, color: colors.mintForeground },
  readOnlyDate: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, padding: spacing.lg, borderRadius: radii.lg, backgroundColor: colors.muted }, readOnlyLabel: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground }, readOnlyValue: { fontFamily: fontFamily.bold, fontSize: 14, color: colors.foreground },
  futureNotice: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, padding: spacing.md, borderRadius: radii.md, backgroundColor: '#FFF9E5' }, futureText: { flex: 1, fontFamily: fontFamily.medium, fontSize: 12, lineHeight: 18, color: '#665423' }, helper: { fontFamily: fontFamily.medium, fontSize: 12, lineHeight: 18, color: colors.mintForeground },
  historyNotice: { flexDirection: 'row', alignItems: 'flex-start', gap: spacing.md, padding: spacing.lg, borderRadius: radii.lg, backgroundColor: colors.secondary }, historyText: { flex: 1, fontFamily: fontFamily.medium, fontSize: 12, lineHeight: 19, color: colors.secondaryForeground }, error: { fontFamily: fontFamily.medium, fontSize: 12, lineHeight: 18, color: colors.destructive },
});
