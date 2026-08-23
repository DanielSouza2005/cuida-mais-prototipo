import { router, type Href } from 'expo-router';
import { useState } from 'react';
import { CheckCircle2 } from 'lucide-react-native';
import { Alert, StyleSheet, Text, View } from 'react-native';

import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { ApiError } from '@/services/api';
import { cancelServiceRequest, getLastServiceRequest } from '@/services/serviceRequestService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

import { hiringLabels, statusLabels } from '@/utils/serviceRequestLabels';
import { formatDateBR, formatInstantDateLocal, formatScheduleTime } from '@/utils/dateTime';

export default function RequestServiceSuccessScreen() {
  const initialRequest = getLastServiceRequest();
  const [request, setRequest] = useState(initialRequest);
  const [isCanceling, setIsCanceling] = useState(false);
  if (!request) return <ScreenContainer contentStyle={styles.center}><Text style={styles.description}>Nenhuma solicitação foi enviada nesta sessão.</Text><PrimaryButton label="Voltar para busca" onPress={() => router.replace('/(tabs)/buscar' as Href)} /></ScreenContainer>;
  const currentRequest = request;
  const isPublication = request.status === 'ABERTA';
  const start = request.hiringType === 'PONTUAL' ? request.specificDates[0] : request.startDate;
  function confirmCancel() { Alert.alert('Cancelar solicitação', 'Deseja cancelar esta solicitação?', [{ text: 'Voltar', style: 'cancel' }, { text: 'Cancelar solicitação', style: 'destructive', onPress: () => void cancel() }]); }
  async function cancel() { setIsCanceling(true); try { const updated=await cancelServiceRequest(currentRequest.id); setRequest(updated); Alert.alert('Tudo certo', 'Solicitação cancelada com sucesso.'); } catch(error) { Alert.alert('Não foi possível cancelar', error instanceof ApiError ? error.message : 'Tente novamente.'); } finally { setIsCanceling(false); } }
  return (
    <ScreenContainer contentStyle={styles.content}>
      <View style={styles.successCard}><View style={styles.iconCircle}><CheckCircle2 color={colors.mintForeground} size={48} /></View><Text style={styles.title}>{isPublication ? 'Serviço publicado' : 'Solicitação enviada'}</Text><Text style={styles.description}>{isPublication ? 'A oportunidade foi cadastrada com sucesso.' : 'Sua solicitação foi enviada ao cuidador.'}</Text><View style={styles.status}><Text style={styles.statusText}>{statusLabels[request.status]}</Text></View></View>
      <View style={styles.summary}>{request.caregiverName ? <Summary label="Cuidador" value={request.caregiverName} /> : null}<Summary label="Pessoa assistida" value={request.assistedPersonName} /><Summary label="Tipo de contratação" value={hiringLabels[request.hiringType]} /><Summary label="Data de início" value={formatDateBR(start) || 'Não informada'} />{request.scheduleDays[0] ? <Summary label="Horário" value={`${formatScheduleTime(request.scheduleDays[0].startTime)} às ${formatScheduleTime(request.scheduleDays[0].endTime)}`} /> : null}<Summary label="Expira em" value={formatInstantDateLocal(request.expiresAt)} /></View>
      <Text style={styles.info}>{isPublication ? 'Cuidadores só poderão encontrar este serviço se a autorização de visibilidade da pessoa assistida estiver ativada.' : 'O cuidador poderá aceitar ou rejeitar a solicitação. Enquanto ela estiver pendente, você poderá cancelá-la nesta tela.'}</Text>
      <View style={styles.actions}>{request.status === 'PENDENTE' || request.status === 'ABERTA' ? <PrimaryButton label={isCanceling ? 'Cancelando...' : (isPublication ? 'Retirar publicação' : 'Cancelar solicitação')} variant="secondary" onPress={confirmCancel} loading={isCanceling} /> : null}{!isPublication ? <PrimaryButton label="Voltar para busca" onPress={() => router.replace('/(tabs)/buscar' as Href)} /> : null}{request.caregiverId ? <PrimaryButton label="Ver perfil do cuidador" variant="secondary" onPress={() => router.replace(`/caregiver-profile/${request.caregiverId}`)} /> : null}<PrimaryButton label="Ir para início" variant="secondary" onPress={() => router.replace('/(tabs)/inicio')} /></View>
    </ScreenContainer>
  );
}

function Summary({ label, value }: { label: string; value: string }) { return <View style={styles.summaryRow}><Text style={styles.label}>{label}</Text><Text style={styles.value}>{value}</Text></View>; }
const styles = StyleSheet.create({
  content: { flexGrow: 1, justifyContent: 'center', padding: spacing.xl, gap: spacing.lg }, center: { flexGrow: 1, justifyContent: 'center', padding: spacing.xl, gap: spacing.lg }, successCard: { alignItems: 'center', gap: spacing.md, padding: spacing.xl, borderRadius: radii.xxl, backgroundColor: colors.card, borderWidth: 1, borderColor: colors.border, ...shadows.card }, iconCircle: { width: 82, height: 82, borderRadius: 41, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.mint }, title: { fontFamily: fontFamily.extraBold, fontSize: 24, color: colors.foreground, textAlign: 'center' }, description: { fontFamily: fontFamily.regular, fontSize: 14, lineHeight: 21, color: colors.mutedForeground, textAlign: 'center' }, status: { paddingHorizontal: spacing.lg, paddingVertical: spacing.sm, borderRadius: radii.full, backgroundColor: colors.sunny }, statusText: { fontFamily: fontFamily.bold, fontSize: 13, color: colors.foreground }, summary: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, backgroundColor: colors.card, borderWidth: 1, borderColor: colors.border }, summaryRow: { gap: spacing.xxs }, label: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground }, value: { fontFamily: fontFamily.bold, fontSize: 14, color: colors.foreground }, info: { fontFamily: fontFamily.regular, fontSize: 13, lineHeight: 20, color: colors.mutedForeground, textAlign: 'center' }, actions: { gap: spacing.md },
});
