import { useEffect, useState } from 'react';
import { useLocalSearchParams } from 'expo-router';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { LoadingState } from '@/components/loading-state';
import { ScreenContainer } from '@/components/screen-container';
import { dependencyLevelOptions, mobilityOptions } from '@/constants/enums';
import { ApiError } from '@/services/api';
import { getResponsibleServiceRequestDetails } from '@/services/receivedServiceRequestService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { ResponsibleServiceRequest } from '@/types/receivedServiceRequest';
import { activityLabels, hiringLabels, statusLabels, weekdayLabels } from '@/utils/serviceRequestLabels';

const weekdayOrder = ['SEGUNDA','TERCA','QUARTA','QUINTA','SEXTA','SABADO','DOMINGO'];
const formatDate = (value: string) => { const [year, month, day] = value.split('-'); return year && month && day ? `${day}/${month}/${year}` : value; };
const formatCep = (value: string) => { const digits = value.replace(/\D/g, ''); return digits.length === 8 ? `${digits.slice(0, 5)}-${digits.slice(5)}` : value; };
const optionLabel = (value: string, options: readonly { value: string; label: string }[]) => options.find((item) => item.value === value)?.label ?? value;

export default function ResponsibleServiceRequestDetailsScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const [request, setRequest] = useState<ResponsibleServiceRequest | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    if (!id) { setLoading(false); return; }
    getResponsibleServiceRequestDetails(id)
      .then((result) => { if (active) setRequest(result); })
      .catch((reason) => { if (active) setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar os detalhes da solicitação.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [id]);

  if (loading) return <ScreenContainer contentStyle={styles.center}><LoadingState message="Carregando detalhes da solicitação..." /></ScreenContainer>;
  if (!request) return <ScreenContainer contentStyle={styles.center}><Text style={styles.empty}>{error ?? 'Solicitação não encontrada.'}</Text></ScreenContainer>;

  const statusMessage = request.status === 'ACEITA'
    ? 'O cuidador aceitou sua solicitação.'
    : request.status === 'REJEITADA'
      ? 'O cuidador rejeitou sua solicitação.'
      : request.status === 'PENDENTE'
        ? 'A solicitação ainda está pendente.'
        : request.status === 'CANCELADA'
          ? 'Esta solicitação foi cancelada.'
          : 'Esta solicitação expirou.';
  const schedule = [...request.scheduleDays].sort((a, b) => weekdayOrder.indexOf(a.weekday) - weekdayOrder.indexOf(b.weekday));

  return (
    <ScreenContainer contentStyle={styles.content}>
      <AppHeader showBack title="Detalhes da solicitação" />
      <Section title="Status da solicitação"><View style={styles.status}><Text style={styles.statusText}>{statusLabels[request.status]}</Text></View><Text style={styles.message}>{statusMessage}</Text>{request.answeredAt ? <Info label="Respondida em" value={new Date(request.answeredAt).toLocaleString('pt-BR')} /> : null}</Section>
      <Section title="Cuidador"><Info label="Nome" value={request.caregiver.name} />{request.caregiver.city ? <Info label="Localização" value={`${request.caregiver.city}${request.caregiver.state ? ` - ${request.caregiver.state}` : ''}`} /> : null}</Section>
      <Section title="Pessoa assistida"><Info label="Nome" value={request.assistedPerson.name} /><Info label="Dependência" value={optionLabel(request.assistedPerson.dependencyLevel, dependencyLevelOptions)} /><Info label="Mobilidade" value={optionLabel(request.assistedPerson.mobility, mobilityOptions)} /></Section>
      <Section title="Endereço do cuidado"><Info label="Endereço" value={`${request.careAddress.street}, ${request.careAddress.number}`} />{request.careAddress.complement ? <Info label="Complemento" value={request.careAddress.complement} /> : null}<Info label="Bairro" value={request.careAddress.neighborhood} /><Info label="Cidade" value={`${request.careAddress.city} - ${request.careAddress.state}`} />{request.careAddress.cep ? <Info label="CEP" value={formatCep(request.careAddress.cep)} /> : null}{request.careAddress.referencePoint ? <Info label="Ponto de referência" value={request.careAddress.referencePoint} /> : null}</Section>
      <Section title="Contratação"><Info label="Tipo" value={hiringLabels[request.hiringType]} /><Info label="Data de início" value={formatDate(request.startDate)} />{request.endDate ? <Info label="Data de término" value={formatDate(request.endDate)} /> : null}{request.specificDates.length ? <Info label="Datas específicas" value={request.specificDates.map(formatDate).join(', ')} /> : null}</Section>
      {schedule.length ? <Section title="Dias e horários">{schedule.map((item) => <Text key={item.weekday} style={styles.message}>{weekdayLabels[item.weekday]} • {item.startTime.slice(0, 5)} às {item.endTime.slice(0, 5)}</Text>)}</Section> : null}
      <Section title="Atividades solicitadas"><View style={styles.chips}>{request.activities.map((item) => <View key={item} style={styles.chip}><Text style={styles.chipText}>{activityLabels[item]}</Text></View>)}</View></Section>
      <Section title="Descrição das necessidades"><Text style={styles.message}>{request.needsDescription}</Text></Section>
      {request.additionalNotes ? <Section title="Observações adicionais"><Text style={styles.message}>{request.additionalNotes}</Text></Section> : null}
      {request.negotiationNotes ? <Section title="Negociação"><Text style={styles.message}>{request.negotiationNotes}</Text></Section> : null}
      {request.status === 'REJEITADA' && request.rejectionReason ? <Section title="Motivo da rejeição"><Text style={styles.message}>{request.rejectionReason}</Text></Section> : null}
    </ScreenContainer>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) { return <View style={styles.section}><Text style={styles.sectionTitle}>{title}</Text>{children}</View>; }
function Info({ label, value }: { label: string; value: string }) { return <View style={styles.info}><Text style={styles.label}>{label}</Text><Text style={styles.value}>{value}</Text></View>; }

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, gap: spacing.lg },
  center: { flexGrow: 1, justifyContent: 'center', padding: spacing.xl },
  empty: { textAlign: 'center', fontFamily: fontFamily.medium, color: colors.mutedForeground },
  section: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card },
  sectionTitle: { fontFamily: fontFamily.extraBold, fontSize: 17, color: colors.foreground },
  status: { alignSelf: 'flex-start', paddingHorizontal: spacing.md, paddingVertical: spacing.sm, borderRadius: radii.full, backgroundColor: colors.secondary },
  statusText: { fontFamily: fontFamily.bold, fontSize: 12, color: colors.foreground },
  info: { gap: spacing.xxs },
  label: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground },
  value: { fontFamily: fontFamily.medium, fontSize: 13, lineHeight: 19, color: colors.foreground },
  message: { fontFamily: fontFamily.regular, fontSize: 13, lineHeight: 20, color: colors.mutedForeground },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm },
  chip: { paddingHorizontal: spacing.md, paddingVertical: spacing.sm, borderRadius: radii.full, backgroundColor: colors.secondary },
  chipText: { fontFamily: fontFamily.semiBold, fontSize: 12, color: colors.primary },
});
