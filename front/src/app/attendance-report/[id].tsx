import { useCallback, useState } from 'react';
import { router, useFocusEffect, useLocalSearchParams, type Href } from 'expo-router';
import { Alert, StyleSheet, Text, View } from 'react-native';
import { FileCheck2 } from 'lucide-react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { LoadingState } from '@/components/loading-state';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { ApiError } from '@/services/api';
import { finalizeAttendanceReport, generateAttendanceReport, getAttendanceReport, getAttendanceReportById, updateAttendanceReport } from '@/services/attendanceReportService';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';
import type { AttendanceReport } from '@/types/attendanceReport';
import { formatDateBR, formatDateTimeLocal, formatScheduleTime } from '@/utils/dateTime';

export default function AttendanceReportScreen() {
  const { user } = useAuth();
  const params = useLocalSearchParams<{ id: string; contractId?: string; date?: string }>();
  const id = one(params.id);
  const retryContractId = one(params.contractId);
  const retryDate = one(params.date);
  const caregiver = user?.userType === 'caregiver';
  const [report, setReport] = useState<AttendanceReport | null>(null);
  const [text, setText] = useState('');
  const [additionalNotes, setAdditionalNotes] = useState('');
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState<'save' | 'finalize' | 'generate' | null>(null);
  const [error, setError] = useState<string | null>(null);

  const applyReport = useCallback((value: AttendanceReport) => {
    setReport(value);
    setText(value.finalText ?? value.editableText);
    setAdditionalNotes(value.additionalNotes ?? '');
    setError(null);
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      if (!id) throw new Error('missing');
      if (id === 'retry' && retryContractId && retryDate) applyReport(await getAttendanceReport(retryContractId, retryDate));
      else if (id === 'retry') throw new Error('retry');
      else applyReport(await getAttendanceReportById(id));
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'Atendimento encerrado. Não foi possível gerar o relatório agora.');
    } finally { setLoading(false); }
  }, [applyReport, id, retryContractId, retryDate]);

  useFocusEffect(useCallback(() => { void load(); }, [load]));

  async function generate() {
    if (!retryContractId || !retryDate) return;
    setBusy('generate');
    try {
      const value = await generateAttendanceReport(retryContractId, retryDate);
      applyReport(value);
      router.replace(`/attendance-report/${value.id}` as Href);
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'Não foi possível gerar o relatório. Tente novamente.');
    } finally { setBusy(null); }
  }

  async function save() {
    if (!report || !text.trim()) { Alert.alert('Relatório vazio', 'Informe o texto do relatório antes de salvar.'); return; }
    setBusy('save');
    try {
      applyReport(await updateAttendanceReport(report.contractId, report.attendanceDate, { editedText: text.trim(), additionalNotes: additionalNotes.trim() || null }));
      Alert.alert('Relatório salvo', 'O rascunho foi salvo com sucesso.');
    } catch (cause) { Alert.alert('Não foi possível salvar o relatório', message(cause)); }
    finally { setBusy(null); }
  }

  function confirmFinalize() {
    if (!report || !text.trim()) { Alert.alert('Relatório vazio', 'Informe o texto do relatório antes de finalizar.'); return; }
    Alert.alert('Finalizar relatório', 'Depois de finalizado, o relatório ficará disponível para o responsável e não poderá mais ser editado.', [
      { text: 'Cancelar', style: 'cancel' },
      { text: 'Finalizar', onPress: () => void finalizeReport() },
    ]);
  }

  async function finalizeReport() {
    if (!report) return;
    setBusy('finalize');
    try {
      const value = await finalizeAttendanceReport(report.contractId, report.attendanceDate, { editedText: text.trim(), additionalNotes: additionalNotes.trim() || null });
      applyReport(value);
      Alert.alert('Relatório finalizado', value.emailStatus === 'SENT'
        ? 'O responsável foi notificado e recebeu uma cópia por e-mail.'
        : value.emailStatus === 'PENDING'
          ? 'O responsável foi notificado. A cópia por e-mail será enviada em segundo plano.'
          : 'O responsável foi notificado. A cópia por e-mail não pôde ser enviada, mas o relatório está disponível no aplicativo.');
    } catch (cause) { Alert.alert('Não foi possível finalizar o relatório', message(cause)); }
    finally { setBusy(null); }
  }

  if (loading) return <ScreenContainer contentStyle={styles.center}><LoadingState message="Carregando relatório..." /></ScreenContainer>;
  if (!report) return <ScreenContainer contentStyle={styles.content}>
    <AppHeader showBack title="Relatório de atendimento" />
    <View style={styles.stateCard}><FileCheck2 color={colors.primary} size={30} /><Text style={styles.stateTitle}>Relatório indisponível</Text><Text style={styles.muted}>{error ?? 'Não foi possível carregar o relatório.'}</Text></View>
    {caregiver && retryContractId && retryDate ? <PrimaryButton label="Tentar gerar relatório novamente" loading={busy === 'generate'} onPress={() => void generate()} /> : null}
    <PrimaryButton label="Voltar" variant="secondary" onPress={() => router.back()} />
  </ScreenContainer>;

  const editable = caregiver && report.status === 'DRAFT';
  const visibleText = report.status === 'FINALIZED' ? report.finalText ?? report.editableText : text;
  return <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
    <AppHeader showBack title="Relatório de atendimento" subtitle={report.statusLabel} />
    <View style={styles.card}>
      <Info label="Pessoa assistida" value={report.assistedPersonName} />
      <Info label="Cuidador" value={report.caregiverName} />
      <Info label="Data" value={formatDateBR(report.attendanceDate)} />
      <Info label="Horário previsto" value={`${formatScheduleTime(report.scheduledStartTime)} às ${formatScheduleTime(report.scheduledEndTime)}`} />
      <Info label="Início registrado" value={formatDateTimeLocal(report.startedAt)} />
      <Info label="Encerramento registrado" value={formatDateTimeLocal(report.endedAt)} />
    </View>
    {editable ? <>
      <AppTextInput label="Texto final do relatório" required multiline value={text} onChangeText={setText} style={styles.reportInput} maxLength={30000} />
      <AppTextInput label="Observações adicionais" optional multiline value={additionalNotes} onChangeText={setAdditionalNotes} maxLength={4000} />
      <PrimaryButton label="Salvar relatório" variant="secondary" loading={busy === 'save'} disabled={Boolean(busy)} onPress={() => void save()} />
      <PrimaryButton label="Finalizar relatório" loading={busy === 'finalize'} disabled={Boolean(busy)} onPress={confirmFinalize} />
    </> : <>
      <View style={styles.card}><Text style={styles.sectionTitle}>Texto final</Text><Text style={styles.reportText}>{visibleText}</Text></View>
      {report.additionalNotes ? <View style={styles.card}><Text style={styles.sectionTitle}>Observações adicionais</Text><Text style={styles.reportText}>{report.additionalNotes}</Text></View> : null}
      <View style={styles.card}><Text style={styles.sectionTitle}>Anotações de enfermagem</Text><Text style={styles.reportText}>{report.nursingNotes}</Text></View>
    </>}
  </ScreenContainer>;
}

function Info({ label, value }: { label: string; value: string }) { return <View style={styles.info}><Text style={styles.label}>{label}</Text><Text style={styles.value}>{value}</Text></View>; }
function one(value?: string | string[]) { return Array.isArray(value) ? value[0] : value; }
function message(cause: unknown) { return cause instanceof ApiError ? cause.message : 'Tente novamente.'; }

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, paddingBottom: spacing.xxl, gap: spacing.lg },
  center: { flexGrow: 1, justifyContent: 'center', padding: spacing.xl },
  card: { padding: spacing.lg, gap: spacing.md, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card },
  stateCard: { alignItems: 'center', padding: spacing.xl, gap: spacing.sm, borderRadius: radii.xl, backgroundColor: colors.card, borderWidth: 1, borderColor: colors.border },
  stateTitle: { fontFamily: fontFamily.bold, fontSize: 17, color: colors.foreground },
  muted: { textAlign: 'center', fontFamily: fontFamily.regular, fontSize: 13, lineHeight: 20, color: colors.mutedForeground },
  info: { gap: spacing.xxs }, label: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground }, value: { fontFamily: fontFamily.medium, fontSize: 14, color: colors.foreground },
  sectionTitle: { fontFamily: fontFamily.bold, fontSize: 16, color: colors.foreground },
  reportText: { fontFamily: fontFamily.regular, fontSize: 13, lineHeight: 21, color: colors.foreground },
  reportInput: { minHeight: 320 },
});
