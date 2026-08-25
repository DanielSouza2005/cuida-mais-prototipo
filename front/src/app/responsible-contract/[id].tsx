import { useCallback, useMemo, useState } from 'react';
import { router, useFocusEffect, useLocalSearchParams, type Href } from 'expo-router';
import { CalendarCheck2, Clock3, Info as InfoIcon, MapPin } from 'lucide-react-native';
import { ActivityIndicator, Linking, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { CareRoutineItemDetails } from '@/components/care-routine-item-details';
import { ContractStatusBadge } from '@/components/contract-history-card';
import { ProfileAvatar } from '@/components/profile-avatar';
import { ScreenContainer } from '@/components/screen-container';
import { PrimaryButton } from '@/components/primary-button';
import { useAuth } from '@/hooks/useAuth';
import { ApiError } from '@/services/api';
import { getResponsibleContractDetails } from '@/services/responsibleContractsService';
import { getContractAttendance } from '@/services/serviceAttendanceService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { ContractHistoryItem, ContractHistoryItemType, ContractHistoryStatus } from '@/types/contractsHistory';
import type { AttendanceSummary } from '@/types/serviceAttendance';
import { contractHiringLabels, contractWeekdayLabels, formatCep, formatContractDate, formatContractDateTime } from '@/utils/contractsHistoryLabels';
import { formatDateTimeLocal, formatScheduleTime } from '@/utils/dateTime';
import { todayDateOnly } from '@/utils/agendaDate';

const statusMessages: Record<ContractHistoryStatus, string> = {
  PENDENTE: 'A solicitação ainda está aguardando resposta do cuidador.',
  ACEITA: 'O cuidador aceitou esta solicitação.',
  AGENDADA: 'A contratação está agendada para a data prevista.',
  ATIVA: 'A contratação está ativa.',
  ENCERRAMENTO_AGENDADO: 'O encerramento foi solicitado e ficará agendado até a data informada.',
  ENCERRADA: 'Esta contratação já foi encerrada.',
  FINALIZADA: 'A contratação foi encerrada.',
  REJEITADA: 'O cuidador rejeitou esta solicitação.',
  CANCELADA: 'Esta solicitação ou contratação foi cancelada.',
  EXPIRADA: 'Esta solicitação expirou.',
};

const weekdayOrder = ['SEGUNDA', 'TERCA', 'QUARTA', 'QUINTA', 'SEXTA', 'SABADO', 'DOMINGO'];

export default function ResponsibleContractDetailsScreen() {
  const { user } = useAuth();
  const { id, itemType } = useLocalSearchParams<{ id: string; itemType: ContractHistoryItemType }>();
  const [item, setItem] = useState<ContractHistoryItem | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [attendance, setAttendance] = useState<AttendanceSummary | null>(null);

  useFocusEffect(useCallback(() => {
    let active = true;
    if (!id || (itemType !== 'SERVICE_REQUEST' && itemType !== 'CARE_CONTRACT')) { setLoading(false); return; }
    setLoading(true);
    getResponsibleContractDetails(itemType, id)
      .then(async (result) => {
        if (active) { setItem(result); setError(null); }
        if (itemType === 'CARE_CONTRACT') {
          try { const current = await getContractAttendance(id, todayDateOnly()); if (active) setAttendance(current); }
          catch { if (active) setAttendance(null); }
        }
      })
      .catch((reason) => { if (active) setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar os detalhes. Tente novamente.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [id, itemType]));

  const schedule = useMemo(() => item ? [...item.scheduleDays].sort((a, b) => weekdayOrder.indexOf(a.weekday) - weekdayOrder.indexOf(b.weekday)) : [], [item]);

  if (loading) return <ScreenContainer contentStyle={styles.center}><ActivityIndicator color={colors.primary} /><Text style={styles.stateText}>Carregando detalhes...</Text></ScreenContainer>;
  if (!item) return <ScreenContainer contentStyle={styles.center}><Text style={styles.errorTitle}>Registro não encontrado</Text><Text style={styles.stateText}>{error ?? 'Este registro não está disponível.'}</Text></ScreenContainer>;

  const isRequest = item.itemType === 'SERVICE_REQUEST';
  const isContract = item.itemType === 'CARE_CONTRACT';
  const caregiverView = user?.userType === 'caregiver' && isContract;
  const displayedParticipant = caregiverView && item.responsible ? { ...item.responsible, locationSummary: '', profilePhotoUrl: undefined } : item.participant;
  const initials = displayedParticipant.name.split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase();

  return (
    <ScreenContainer contentStyle={styles.content}>
      <AppHeader showBack title={isRequest ? 'Detalhes da solicitação' : 'Detalhes da contratação'} />

      <Section title="Status">
        <View style={styles.statusRow}><ContractStatusBadge status={item.status} /><Text style={styles.updated}>Atualizado em {formatContractDateTime(item.updatedAt)}</Text></View>
        <Text style={styles.contextMessage}>{statusMessages[item.status]}</Text>
      </Section>

      {isContract && item.status === 'ATIVA' ? (
        <Section title="Encerramento do serviço">
          <Text style={styles.actionDescription}>Se precisar finalizar este cuidado, informe a data efetiva e o motivo antes de confirmar.</Text>
          <PrimaryButton label="Encerrar serviço" onPress={() => router.push(`/contract-termination/${item.id}` as Href)} />
        </Section>
      ) : null}

      {isContract && item.status === 'AGENDADA' ? (
        <Section title="Cancelamento">
          <Text style={styles.actionDescription}>Como o serviço ainda não começou, esta contratação pode ser cancelada.</Text>
          <PrimaryButton label="Cancelar contratação" variant="secondary" onPress={() => router.push(`/contract-termination/${item.id}` as Href)} />
        </Section>
      ) : null}

      {isContract && item.status === 'ENCERRAMENTO_AGENDADO' ? (
        <View style={styles.scheduledCard}>
          <View style={styles.noticeIcon}><CalendarCheck2 color="#755B00" size={20} /></View>
          <View style={styles.flex}><Text style={styles.scheduledTitle}>Encerramento agendado</Text><Text style={styles.scheduledText}>Data efetiva: {formatContractDate(item.effectiveEndDate ?? '')}</Text><Text style={styles.scheduledText}>Motivo: {item.terminationReason ?? item.closureReason}</Text>{item.terminationRequestedByName ? <Text style={styles.scheduledText}>Solicitado por: {item.terminationRequestedByName}</Text> : null}</View>
        </View>
      ) : null}

      {isContract && (item.status === 'ENCERRADA' || item.status === 'FINALIZADA') ? <View style={styles.closedNotice}><InfoIcon color={colors.mutedForeground} size={20} /><Text style={styles.closedText}>Esta contratação já foi encerrada.</Text></View> : null}
      {isContract && item.status === 'CANCELADA' ? <View style={styles.closedNotice}><InfoIcon color={colors.mutedForeground} size={20} /><Text style={styles.closedText}>Esta contratação foi cancelada antes do início.</Text></View> : null}

      {isContract && (item.terminationRequestedAt || item.cancellationRequestedAt) ? (
        <Section title={item.status === 'CANCELADA' ? 'Informações do cancelamento' : 'Informações do encerramento'}>
          {item.status === 'CANCELADA' && item.cancellationRequestedByName ? <Info label="Solicitado por" value={item.cancellationRequestedByName} /> : null}
          {item.status !== 'CANCELADA' && item.terminationRequestedByName ? <Info label="Solicitado por" value={item.terminationRequestedByName} /> : null}
          {item.status === 'CANCELADA' && item.cancellationRequestedAt ? <Info label="Solicitado em" value={formatContractDateTime(item.cancellationRequestedAt)} /> : null}
          {item.status !== 'CANCELADA' && item.terminationRequestedAt ? <Info label="Solicitado em" value={formatContractDateTime(item.terminationRequestedAt)} /> : null}
          {item.status === 'CANCELADA' && item.canceledAt ? <Info label="Cancelado em" value={formatContractDateTime(item.canceledAt)} /> : null}
        </Section>
      ) : null}

      <Section title={caregiverView ? 'Responsável' : 'Cuidador'}>
        <View style={styles.caregiverRow}>
          <ProfileAvatar imageUrl={displayedParticipant.profilePhotoUrl} initials={initials} size={58} />
          <View style={styles.flex}><Text style={styles.caregiverName}>{displayedParticipant.name}</Text>{displayedParticipant.locationSummary ? <View style={styles.inline}><MapPin color={colors.mutedForeground} size={14} /><Text style={styles.secondaryText}>{displayedParticipant.locationSummary}</Text></View> : null}</View>
        </View>
        {!caregiverView && item.participant.id ? <PrimaryButton label="Ver perfil do cuidador" variant="secondary" onPress={() => router.push(`/caregiver-profile/${item.participant.id}` as Href)} style={styles.profileButton} /> : null}
      </Section>

      <Section title="Pessoa assistida">
        <Info label="Nome" value={item.assistedPerson.name} />
        <Info label="Grau de dependência" value={item.assistedPerson.dependencyLevel} />
        <Info label="Mobilidade" value={item.assistedPerson.mobility} />
        {item.assistedPerson.allergies ? <Info label="Alergias" value={item.assistedPerson.allergies} /> : null}
        {item.assistedPerson.foodRestrictions ? <Info label="Restrições alimentares" value={item.assistedPerson.foodRestrictions} /> : null}
        {item.assistedPerson.notes ? <Info label="Observações relevantes" value={item.assistedPerson.notes} /> : null}
      </Section>

      <Section title="Endereço do cuidado">
        <Info label="Endereço" value={`${item.careAddress.street}, ${item.careAddress.number}`} />
        {item.careAddress.complement ? <Info label="Complemento" value={item.careAddress.complement} /> : null}
        <Info label="Bairro" value={item.careAddress.neighborhood} />
        <Info label="Cidade e estado" value={`${item.careAddress.city} - ${item.careAddress.state}`} />
        <Info label="CEP" value={formatCep(item.careAddress.cep)} />
        {item.careAddress.referencePoint ? <Info label="Ponto de referência" value={item.careAddress.referencePoint} /> : null}
      </Section>

      {isContract && attendance ? <Section title="Atendimento de hoje">
        <Info label="Situação" value={attendance.statusLabel} />
        <Info label="Horário previsto" value={`${formatScheduleTime(attendance.scheduledStartTime)} às ${formatScheduleTime(attendance.scheduledEndTime)}`} />
        {attendance.startRecord ? <><Info label="Registro de início" value={formatDateTimeLocal(attendance.startRecord.recordedAt)} /><PrimaryButton label="Ver localização de início" variant="secondary" onPress={() => void Linking.openURL(`https://www.google.com/maps/search/?api=1&query=${attendance.startRecord!.latitude},${attendance.startRecord!.longitude}`)} /></> : null}
        {attendance.endRecord ? <><Info label="Registro de encerramento" value={formatDateTimeLocal(attendance.endRecord.recordedAt)} /><PrimaryButton label="Ver localização de encerramento" variant="secondary" onPress={() => void Linking.openURL(`https://www.google.com/maps/search/?api=1&query=${attendance.endRecord!.latitude},${attendance.endRecord!.longitude}`)} /></> : null}
        {attendance.status === 'ENDED' ? <PrimaryButton label="Ver relatório de atendimento" variant="secondary" onPress={() => router.push(`/attendance-report/retry?contractId=${item.id}&date=${attendance.attendanceDate}` as Href)} /> : null}
      </Section> : null}

      {item.careRoutine ? <Section title="Cuidados combinados"><Info label="Rotina de cuidados selecionada" value={item.careRoutine.name} />{item.careRoutine.items.map((care,index)=><CareRoutineItemDetails key={care.id??`${index}`} item={care} index={index}/>)}</Section> : null}

      <Section title="Contratação">
        <Info label="Tipo de contratação" value={contractHiringLabels[item.hiringType]} />
        <Info label="Data de início" value={formatContractDate(item.startDate)} />
        {item.endDate ? <Info label={item.status === 'FINALIZADA' || item.status === 'ENCERRADA' ? 'Data de término efetiva' : 'Data de término prevista'} value={formatContractDate(item.endDate)} /> : null}
        {item.effectiveEndDate && item.effectiveEndDate !== item.endDate ? <Info label="Data efetiva de término" value={formatContractDate(item.effectiveEndDate)} /> : null}
        {item.specificDates.length ? <Info label="Datas específicas" value={item.specificDates.map(formatContractDate).join(', ')} /> : null}
        {schedule.length ? <View style={styles.scheduleList}><Text style={styles.infoLabel}>Dias e horários</Text>{schedule.map((entry) => <View key={`${entry.weekday}-${entry.startTime}`} style={styles.scheduleRow}><Clock3 color={colors.primary} size={15} /><Text style={styles.infoValue}>{contractWeekdayLabels[entry.weekday]} · {formatScheduleTime(entry.startTime)} às {formatScheduleTime(entry.endTime)}</Text></View>)}</View> : null}
      </Section>

      {!item.careRoutine && item.activities.length ? <Section title="Cuidados informados anteriormente">
        <View style={styles.activityChips}>{item.activities.map((activity) => <View key={activity} style={styles.activityChip}><Text style={styles.activityText}>{activity}</Text></View>)}</View>
      </Section> : null}

      <Section title="Necessidades da pessoa assistida">
        <Info label="Necessidades" value={item.needsDescription} />
        {item.additionalNotes ? <Info label="Observações adicionais" value={item.additionalNotes} /> : null}
        {item.negotiationNotes ? <Info label="Negociação" value={item.negotiationNotes} /> : null}
      </Section>

      {item.rejectionReason || item.cancellationReason || item.closureReason ? <Section title="Motivo">{item.rejectionReason ? <Info label="Motivo da rejeição" value={item.rejectionReason} /> : null}{item.cancellationReason ? <Info label="Motivo do cancelamento" value={item.cancellationReason} /> : null}{item.closureReason ? <Info label="Motivo do encerramento" value={item.closureReason} /> : null}</Section> : null}

      <Section title="Histórico de status">
        <Text style={styles.timelineIntro}>Acompanhe as principais atualizações deste registro.</Text>
        <View style={styles.timeline}>{item.statusHistory.map((entry, index) => <View key={entry.id} style={styles.timelineEntry}><View style={styles.timelineRail}><View style={[styles.timelineDot, index === item.statusHistory.length - 1 && styles.timelineDotCurrent]} />{index < item.statusHistory.length - 1 ? <View style={styles.timelineLine} /> : null}</View><View style={styles.timelineCopy}><Text style={styles.timelineLabel}>{entry.label}</Text><Text style={styles.timelineDate}>{formatContractDateTime(entry.changedAt)}</Text><Text style={styles.timelineBy}>Atualizado por {entry.changedBy}</Text>{entry.reason ? <Text style={styles.timelineReason}>{entry.reason}</Text> : null}</View></View>)}</View>
      </Section>
    </ScreenContainer>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) { return <View style={styles.section}><Text style={styles.sectionTitle}>{title}</Text>{children}</View>; }
function Info({ label, value }: { label: string; value: string }) { if (!value.trim()) return null; return <View style={styles.info}><Text style={styles.infoLabel}>{label}</Text><Text style={styles.infoValue}>{value}</Text></View>; }

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, gap: spacing.lg },
  center: { flexGrow: 1, alignItems: 'center', justifyContent: 'center', gap: spacing.md, padding: spacing.xl },
  stateText: { textAlign: 'center', fontFamily: fontFamily.regular, fontSize: 13, lineHeight: 20, color: colors.mutedForeground }, errorTitle: { fontFamily: fontFamily.bold, color: colors.destructive },
  section: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card },
  sectionTitle: { fontFamily: fontFamily.extraBold, fontSize: 17, color: colors.foreground },
  statusRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: spacing.md },
  updated: { flex: 1, textAlign: 'right', fontFamily: fontFamily.medium, fontSize: 10, color: colors.mutedForeground },
  contextMessage: { padding: spacing.md, borderRadius: radii.md, backgroundColor: colors.secondary, fontFamily: fontFamily.medium, fontSize: 13, lineHeight: 20, color: colors.secondaryForeground },
  actionDescription: { fontFamily: fontFamily.regular, fontSize: 13, lineHeight: 20, color: colors.mutedForeground },
  scheduledCard: { flexDirection: 'row', gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: '#F0D77E', backgroundColor: '#FFF9E5' },
  noticeIcon: { width: 38, height: 38, alignItems: 'center', justifyContent: 'center', borderRadius: radii.full, backgroundColor: '#FFF0B8' },
  scheduledTitle: { fontFamily: fontFamily.bold, fontSize: 15, color: '#755B00' },
  scheduledText: { fontFamily: fontFamily.medium, fontSize: 12, lineHeight: 18, color: '#665423' },
  closedNotice: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, backgroundColor: colors.muted },
  closedText: { flex: 1, fontFamily: fontFamily.medium, fontSize: 13, lineHeight: 20, color: colors.secondaryForeground },
  caregiverRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.md },
  flex: { flex: 1, gap: spacing.xs }, caregiverName: { fontFamily: fontFamily.bold, fontSize: 15, color: colors.foreground }, inline: { flexDirection: 'row', alignItems: 'center', gap: spacing.xs }, secondaryText: { flex: 1, fontFamily: fontFamily.regular, fontSize: 11, color: colors.mutedForeground },
  profileButton: { minHeight: 48 },
  info: { gap: spacing.xxs }, infoLabel: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground }, infoValue: { fontFamily: fontFamily.medium, fontSize: 13, lineHeight: 20, color: colors.foreground },
  scheduleList: { gap: spacing.sm }, scheduleRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
  activityChips: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm }, activityChip: { paddingHorizontal: spacing.md, paddingVertical: spacing.sm, borderRadius: radii.full, backgroundColor: colors.secondary }, activityText: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.primary },
  timelineIntro: { fontFamily: fontFamily.regular, fontSize: 12, lineHeight: 18, color: colors.mutedForeground }, timeline: { gap: 0 },
  timelineEntry: { minHeight: 76, flexDirection: 'row', gap: spacing.md }, timelineRail: { width: 18, alignItems: 'center' }, timelineDot: { width: 12, height: 12, borderRadius: radii.full, borderWidth: 3, borderColor: colors.secondary, backgroundColor: colors.primary }, timelineDotCurrent: { width: 16, height: 16, borderColor: colors.mint, backgroundColor: colors.mintForeground }, timelineLine: { flex: 1, width: 2, backgroundColor: colors.border },
  timelineCopy: { flex: 1, gap: spacing.xxs, paddingBottom: spacing.lg }, timelineLabel: { fontFamily: fontFamily.bold, fontSize: 13, color: colors.foreground }, timelineDate: { fontFamily: fontFamily.medium, fontSize: 11, color: colors.primary }, timelineBy: { fontFamily: fontFamily.regular, fontSize: 10, color: colors.mutedForeground }, timelineReason: { marginTop: spacing.xs, fontFamily: fontFamily.regular, fontSize: 11, lineHeight: 17, color: colors.secondaryForeground },
});
