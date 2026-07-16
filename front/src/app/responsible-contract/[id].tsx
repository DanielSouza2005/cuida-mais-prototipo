import { useEffect, useMemo, useState } from 'react';
import { router, useLocalSearchParams, type Href } from 'expo-router';
import { Clock3, MapPin } from 'lucide-react-native';
import { ActivityIndicator, Image, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { ContractStatusBadge } from '@/components/contract-history-card';
import { ScreenContainer } from '@/components/screen-container';
import { PrimaryButton } from '@/components/primary-button';
import { ApiError } from '@/services/api';
import { getResponsibleContractDetails } from '@/services/responsibleContractsService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { ContractHistoryItem, ContractHistoryItemType, ContractHistoryStatus } from '@/types/contractsHistory';
import { contractHiringLabels, contractWeekdayLabels, formatCep, formatContractDate, formatContractDateTime } from '@/utils/contractsHistoryLabels';

const statusMessages: Record<ContractHistoryStatus, string> = {
  PENDENTE: 'A solicitação ainda está aguardando resposta do cuidador.',
  ACEITA: 'O cuidador aceitou esta solicitação.',
  AGENDADA: 'A contratação está agendada para a data prevista.',
  ATIVA: 'A contratação está ativa.',
  FINALIZADA: 'A contratação foi encerrada.',
  REJEITADA: 'O cuidador rejeitou esta solicitação.',
  CANCELADA: 'Esta solicitação ou contratação foi cancelada.',
  EXPIRADA: 'Esta solicitação expirou.',
};

const weekdayOrder = ['SEGUNDA', 'TERCA', 'QUARTA', 'QUINTA', 'SEXTA', 'SABADO', 'DOMINGO'];

export default function ResponsibleContractDetailsScreen() {
  const { id, itemType } = useLocalSearchParams<{ id: string; itemType: ContractHistoryItemType }>();
  const [item, setItem] = useState<ContractHistoryItem | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    if (!id || (itemType !== 'SERVICE_REQUEST' && itemType !== 'CARE_CONTRACT')) { setLoading(false); return; }
    getResponsibleContractDetails(itemType, id)
      .then((result) => { if (active) setItem(result); })
      .catch((reason) => { if (active) setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar os detalhes. Tente novamente.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [id, itemType]);

  const schedule = useMemo(() => item ? [...item.scheduleDays].sort((a, b) => weekdayOrder.indexOf(a.weekday) - weekdayOrder.indexOf(b.weekday)) : [], [item]);

  if (loading) return <ScreenContainer contentStyle={styles.center}><ActivityIndicator color={colors.primary} /><Text style={styles.stateText}>Carregando detalhes...</Text></ScreenContainer>;
  if (!item) return <ScreenContainer contentStyle={styles.center}><Text style={styles.errorTitle}>Registro não encontrado</Text><Text style={styles.stateText}>{error ?? 'Este registro não está disponível.'}</Text></ScreenContainer>;

  const isRequest = item.itemType === 'SERVICE_REQUEST';
  const initials = item.participant.name.split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase();

  return (
    <ScreenContainer contentStyle={styles.content}>
      <AppHeader showBack title={isRequest ? 'Detalhes da solicitação' : 'Detalhes da contratação'} />

      <Section title="Status">
        <View style={styles.statusRow}><ContractStatusBadge status={item.status} /><Text style={styles.updated}>Atualizado em {formatContractDateTime(item.updatedAt)}</Text></View>
        <Text style={styles.contextMessage}>{statusMessages[item.status]}</Text>
      </Section>

      <Section title="Cuidador">
        <View style={styles.caregiverRow}>
          {item.participant.profilePhotoUrl ? <Image source={{ uri: item.participant.profilePhotoUrl }} style={styles.avatar} /> : <View style={styles.avatarFallback}><Text style={styles.avatarText}>{initials}</Text></View>}
          <View style={styles.flex}><Text style={styles.caregiverName}>{item.participant.name}</Text><View style={styles.inline}><MapPin color={colors.mutedForeground} size={14} /><Text style={styles.secondaryText}>{item.participant.locationSummary}</Text></View></View>
        </View>
        {item.participant.id ? <PrimaryButton label="Ver perfil do cuidador" variant="secondary" onPress={() => router.push(`/caregiver-profile/${item.participant.id}` as Href)} style={styles.profileButton} /> : null}
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

      <Section title="Contratação">
        <Info label="Tipo de contratação" value={contractHiringLabels[item.hiringType]} />
        <Info label="Data de início" value={formatContractDate(item.startDate)} />
        {item.endDate ? <Info label={item.status === 'FINALIZADA' ? 'Data de término efetiva' : 'Data de término prevista'} value={formatContractDate(item.endDate)} /> : null}
        {item.specificDates.length ? <Info label="Datas específicas" value={item.specificDates.map(formatContractDate).join(', ')} /> : null}
        {schedule.length ? <View style={styles.scheduleList}><Text style={styles.infoLabel}>Dias e horários</Text>{schedule.map((entry) => <View key={`${entry.weekday}-${entry.startTime}`} style={styles.scheduleRow}><Clock3 color={colors.primary} size={15} /><Text style={styles.infoValue}>{contractWeekdayLabels[entry.weekday]} · {entry.startTime.slice(0, 5)} às {entry.endTime.slice(0, 5)}</Text></View>)}</View> : null}
      </Section>

      <Section title="Atividades e necessidades">
        <View style={styles.activityChips}>{item.activities.map((activity) => <View key={activity} style={styles.activityChip}><Text style={styles.activityText}>{activity}</Text></View>)}</View>
        <Info label="Descrição das necessidades" value={item.needsDescription} />
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
  caregiverRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.md }, avatar: { width: 58, height: 58, borderRadius: radii.full },
  avatarFallback: { width: 58, height: 58, borderRadius: radii.full, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.secondary }, avatarText: { fontFamily: fontFamily.bold, fontSize: 17, color: colors.primary },
  flex: { flex: 1, gap: spacing.xs }, caregiverName: { fontFamily: fontFamily.bold, fontSize: 15, color: colors.foreground }, inline: { flexDirection: 'row', alignItems: 'center', gap: spacing.xs }, secondaryText: { flex: 1, fontFamily: fontFamily.regular, fontSize: 11, color: colors.mutedForeground },
  profileButton: { minHeight: 48 },
  info: { gap: spacing.xxs }, infoLabel: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground }, infoValue: { fontFamily: fontFamily.medium, fontSize: 13, lineHeight: 20, color: colors.foreground },
  scheduleList: { gap: spacing.sm }, scheduleRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
  activityChips: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm }, activityChip: { paddingHorizontal: spacing.md, paddingVertical: spacing.sm, borderRadius: radii.full, backgroundColor: colors.secondary }, activityText: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.primary },
  timelineIntro: { fontFamily: fontFamily.regular, fontSize: 12, lineHeight: 18, color: colors.mutedForeground }, timeline: { gap: 0 },
  timelineEntry: { minHeight: 76, flexDirection: 'row', gap: spacing.md }, timelineRail: { width: 18, alignItems: 'center' }, timelineDot: { width: 12, height: 12, borderRadius: radii.full, borderWidth: 3, borderColor: colors.secondary, backgroundColor: colors.primary }, timelineDotCurrent: { width: 16, height: 16, borderColor: colors.mint, backgroundColor: colors.mintForeground }, timelineLine: { flex: 1, width: 2, backgroundColor: colors.border },
  timelineCopy: { flex: 1, gap: spacing.xxs, paddingBottom: spacing.lg }, timelineLabel: { fontFamily: fontFamily.bold, fontSize: 13, color: colors.foreground }, timelineDate: { fontFamily: fontFamily.medium, fontSize: 11, color: colors.primary }, timelineBy: { fontFamily: fontFamily.regular, fontSize: 10, color: colors.mutedForeground }, timelineReason: { marginTop: spacing.xs, fontFamily: fontFamily.regular, fontSize: 11, lineHeight: 17, color: colors.secondaryForeground },
});
