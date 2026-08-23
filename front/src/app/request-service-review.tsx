import { useState } from 'react';
import { router, type Href } from 'expo-router';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { CareRoutineItemDetails } from '@/components/care-routine-item-details';
import { PrimaryButton } from '@/components/primary-button';
import { ProfileAvatar } from '@/components/profile-avatar';
import { ScreenContainer } from '@/components/screen-container';
import { ApiError } from '@/services/api';
import { createServiceRequest, getServiceRequestDraft } from '@/services/serviceRequestService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { ServiceRequestPayload } from '@/types/serviceRequest';
import { hiringLabels, weekdayLabels } from '@/utils/serviceRequestLabels';

function toIso(value: string) { if (!value) return null; const [day, month, year] = value.split('/'); return `${year}-${month}-${day}`; }
function formatAddress(address: { street: string; number: string; neighborhood: string; city: string; state: string }) { return `${address.street}, ${address.number} • ${address.neighborhood} • ${address.city} - ${address.state}`; }

export default function RequestServiceReviewScreen() {
  const draft = getServiceRequestDraft();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [feedback, setFeedback] = useState<string | null>(null);
  if (!draft?.hiringType || !draft.careRoutine) return <ScreenContainer contentStyle={styles.center}><Text style={styles.text}>Preencha a solicitação e selecione uma rotina de cuidados antes de revisar.</Text><PrimaryButton label="Voltar ao formulário" onPress={() => router.replace('/request-service' as Href)} /></ScreenContainer>;

  async function confirm() {
    if (!draft?.hiringType) return;
    setIsSubmitting(true);
    const days = draft.hiringType === 'PONTUAL' ? draft.specificDates : draft.weekDays;
    const payload: ServiceRequestPayload = {
      caregiverId: draft.caregiver?.id ?? null, assistedPersonId: draft.assistedPerson.id, careAddressId: draft.address.id, careRoutineId: draft.careRoutine!.id, hiringType: draft.hiringType,
      startDate: draft.hiringType === 'PONTUAL' ? null : toIso(draft.startDate), endDate: draft.hiringType === 'PERIODO_DETERMINADO' ? toIso(draft.endDate) : null,
      specificDates: draft.specificDates.map((date) => toIso(date)!), scheduleDays: days.map((weekday) => ({ weekday: draft.hiringType === 'PONTUAL' ? weekdayForDate(weekday) : weekday, startTime: draft.startTime, endTime: draft.endTime })),
      needsDescription: draft.needsDescription, additionalNotes: draft.additionalNotes || null, negotiationNotes: draft.negotiation || null,
    };
    try { await createServiceRequest(payload); router.replace('/request-service-success' as Href); }
    catch (error) { setFeedback(error instanceof ApiError ? error.message : 'Não foi possível enviar a solicitação. Tente novamente.'); }
    finally { setIsSubmitting(false); }
  }

  return (
    <ScreenContainer contentStyle={styles.content}>
      <AppHeader showBack title={draft.publication ? 'Revisar publicação' : 'Revisar solicitação'} />
      <Text style={styles.subtitle}>{draft.publication ? 'Confira as informações que ficarão disponíveis aos cuidadores.' : 'Confira as informações antes de enviar ao cuidador.'}</Text>
      {!draft.publication && draft.caregiver ? <Card title="Cuidador"><View style={styles.personRow}><ProfileAvatar imageUrl={draft.caregiver.profilePhotoUrl} initials={draft.caregiver.name.slice(0, 2).toUpperCase()} size={58} /><View style={styles.flex}><Strong>{draft.caregiver.name}</Strong><Text style={styles.text}>{[draft.caregiver.neighborhood, draft.caregiver.city, draft.caregiver.state].filter(Boolean).join(' • ')}</Text></View></View></Card> : null}
      <Card title="Pessoa assistida"><Strong>{draft.assistedPerson.name}</Strong><Text style={styles.text}>{draft.assistedPerson.summary}</Text></Card>
      <Card title="Endereço do cuidado"><Text style={styles.text}>{formatAddress(draft.address)}</Text>{draft.address.referencePoint ? <Text style={styles.text}>{draft.address.referencePoint}</Text> : null}</Card>
      {draft.careRoutine ? <Card title="Rotina de cuidados selecionada"><Strong>{draft.careRoutine.name}</Strong><Text style={styles.rowLabel}>Cuidados combinados</Text>{draft.careRoutine.items.map((item,index)=><CareRoutineItemDetails key={item.id??`${index}`} item={item} index={index}/>)}</Card> : null}
      <Card title="Contratação"><Row label="Tipo" value={hiringLabels[draft.hiringType]} /><Row label="Datas" value={draft.hiringType === 'PONTUAL' ? draft.specificDates.join(', ') : [draft.startDate, draft.endDate].filter(Boolean).join(' até ')} />{draft.hiringType !== 'PONTUAL' && draft.weekDays.length ? <Row label="Dias" value={draft.weekDays.map((day) => weekdayLabels[day] ?? day).join(', ')} /> : null}<Row label="Horário" value={`${draft.startTime} às ${draft.endTime}`} /></Card>
      <Card title="Necessidades da pessoa assistida"><Text style={styles.text}>{draft.needsDescription}</Text>{draft.additionalNotes ? <Row label="Observações adicionais" value={draft.additionalNotes} /> : null}{draft.negotiation ? <Row label="Negociação" value={draft.negotiation} /> : null}</Card>
      <Card title="Status inicial"><View style={styles.status}><Text style={styles.statusText}>{draft.publication ? 'Disponível' : 'Pendente'}</Text></View><Text style={styles.text}>{draft.publication ? 'O serviço ficará disponível apenas quando a pessoa assistida autorizar ser encontrada por cuidadores.' : 'A solicitação ficará pendente até ser aceita, rejeitada, cancelada ou expirada.'}</Text><Text style={styles.notice}>O registro expira automaticamente após 15 dias.</Text></Card>
      {feedback ? <Text style={styles.error}>{feedback}</Text> : null}
      <View style={styles.actions}><PrimaryButton label="Voltar e editar" variant="secondary" onPress={() => router.back()} disabled={isSubmitting} /><PrimaryButton label={isSubmitting ? (draft.publication ? 'Publicando serviço...' : 'Enviando solicitação...') : (draft.publication ? 'Publicar serviço' : 'Confirmar solicitação')} onPress={confirm} loading={isSubmitting} /></View>
    </ScreenContainer>
  );
}

function Card({ title, children }: { title: string; children: React.ReactNode }) { return <View style={styles.card}><Text style={styles.cardTitle}>{title}</Text>{children}</View>; }
function Strong({ children }: { children: React.ReactNode }) { return <Text style={styles.strong}>{children}</Text>; }
function Row({ label, value }: { label: string; value: string }) { return <View style={styles.row}><Text style={styles.rowLabel}>{label}</Text><Text style={styles.rowValue}>{value || 'Não informado'}</Text></View>; }
function weekdayForDate(value: string) { const [day, month, year] = value.split('/').map(Number); return ['DOMINGO','SEGUNDA','TERCA','QUARTA','QUINTA','SEXTA','SABADO'][new Date(year, month - 1, day).getDay()]; }

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, paddingBottom: spacing.lg, gap: spacing.lg }, center: { flexGrow: 1, justifyContent: 'center', padding: spacing.xl, gap: spacing.lg }, subtitle: { fontFamily: fontFamily.regular, fontSize: 14, color: colors.mutedForeground }, card: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, backgroundColor: colors.card, borderWidth: 1, borderColor: colors.border, ...shadows.card }, cardTitle: { fontFamily: fontFamily.extraBold, fontSize: 17, color: colors.foreground }, strong: { fontFamily: fontFamily.bold, fontSize: 14, color: colors.foreground }, text: { fontFamily: fontFamily.regular, fontSize: 13, lineHeight: 20, color: colors.mutedForeground }, personRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.md }, flex: { flex: 1 }, row: { gap: spacing.xxs }, rowLabel: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground }, rowValue: { fontFamily: fontFamily.medium, fontSize: 13, lineHeight: 19, color: colors.foreground }, status: { alignSelf: 'flex-start', paddingHorizontal: spacing.md, paddingVertical: spacing.sm, borderRadius: radii.full, backgroundColor: colors.sunny }, statusText: { fontFamily: fontFamily.bold, fontSize: 12, color: colors.foreground }, notice: { fontFamily: fontFamily.semiBold, fontSize: 12, lineHeight: 18, color: colors.mintForeground }, error: { fontFamily: fontFamily.semiBold, fontSize: 13, color: colors.destructive }, actions: { gap: spacing.md },
});
