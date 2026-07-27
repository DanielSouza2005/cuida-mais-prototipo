import { useCallback, useState } from 'react';
import { router, useFocusEffect, useLocalSearchParams, type Href } from 'expo-router';
import { CalendarCheck2, Clock3, MapPin } from 'lucide-react-native';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { CareRoutineItemDetails } from '@/components/care-routine-item-details';
import { ContractStatusBadge } from '@/components/contract-history-card';
import { PrimaryButton } from '@/components/primary-button';
import { ProfileAvatar } from '@/components/profile-avatar';
import { ScreenContainer } from '@/components/screen-container';
import { getAgendaEventDetails } from '@/services/agendaService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { AgendaEventDetails } from '@/types/agenda';
import { contractHiringLabels, contractWeekdayLabels, formatCep } from '@/utils/contractsHistoryLabels';
import { formatDateBR, formatScheduleTime } from '@/utils/dateTime';

export default function AgendaEventDetailsScreen() {
  const params = useLocalSearchParams<{ contractId: string; eventDate: string }>();
  const contractId = Array.isArray(params.contractId) ? params.contractId[0] : params.contractId;
  const eventDate = Array.isArray(params.eventDate) ? params.eventDate[0] : params.eventDate;
  const [details, setDetails] = useState<AgendaEventDetails | null>(null);
  const [error, setError] = useState(false);

  const load = useCallback(async () => {
    if (!contractId || !eventDate) { setError(true); return; }
    setError(false);
    try {
      setDetails(await getAgendaEventDetails(contractId, eventDate));
    } catch {
      setError(true);
    }
  }, [contractId, eventDate]);

  useFocusEffect(useCallback(() => { void load(); }, [load]));

  if (!details && !error) {
    return <ScreenContainer contentStyle={styles.center}><ActivityIndicator color={colors.primary} /><Text style={styles.stateText}>Carregando detalhes do evento...</Text></ScreenContainer>;
  }
  if (!details || error) {
    return (
      <ScreenContainer contentStyle={styles.center}>
        <Text style={styles.errorTitle}>Não foi possível carregar este evento.</Text>
        <Text style={styles.stateText}>O serviço pode ter sido alterado ou não estar mais disponível na Agenda.</Text>
        <PrimaryButton label="Voltar" variant="secondary" onPress={() => router.back()} />
      </ScreenContainer>
    );
  }

  const { event, contract } = details;
  const startTime = formatScheduleTime(event.startDateTime.slice(11));
  const endTime = formatScheduleTime(event.endDateTime.slice(11));
  const caregiverInitials = contract.participant.name.split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase();

  return (
    <ScreenContainer contentStyle={styles.content}>
      <AppHeader showBack title="Detalhes do evento" subtitle={event.title} />

      {event.hasScheduledTermination && event.effectiveEndDate ? (
        <View style={styles.notice}>
          <CalendarCheck2 color="#755B00" size={21} />
          <Text style={styles.noticeText}>Encerramento agendado para {formatDateBR(event.effectiveEndDate)}.</Text>
        </View>
      ) : null}

      <Section title="Evento">
        <View style={styles.statusRow}><ContractStatusBadge status={event.status} /></View>
        <Info label="Data" value={formatDateBR(event.eventDate)} />
        <Info label="Horário" value={`${startTime} às ${endTime}`} />
        <Info label="Tipo de contratação" value={contractHiringLabels[event.hiringType]} />
      </Section>

      <Section title="Partes envolvidas">
        <View style={styles.personRow}>
          <ProfileAvatar imageUrl={contract.participant.profilePhotoUrl} initials={caregiverInitials} size={58} />
          <View style={styles.flex}><Text style={styles.personRole}>Cuidador(a)</Text><Text style={styles.personName}>{contract.participant.name}</Text></View>
        </View>
        {contract.responsible ? <Info label="Responsável" value={contract.responsible.name} /> : null}
      </Section>

      <Section title="Pessoa assistida">
        <Info label="Nome" value={contract.assistedPerson.name} />
        <Info label="Grau de dependência" value={contract.assistedPerson.dependencyLevel} />
        <Info label="Mobilidade" value={contract.assistedPerson.mobility} />
        {contract.assistedPerson.allergies ? <Info label="Alergias" value={contract.assistedPerson.allergies} /> : null}
        {contract.assistedPerson.foodRestrictions ? <Info label="Restrições alimentares" value={contract.assistedPerson.foodRestrictions} /> : null}
        {contract.assistedPerson.notes ? <Info label="Observações" value={contract.assistedPerson.notes} /> : null}
      </Section>

      <Section title="Endereço do cuidado">
        <View style={styles.addressHeading}><MapPin color={colors.primary} size={18} /><Text style={styles.addressText}>{contract.careAddress.street}, {contract.careAddress.number}</Text></View>
        {contract.careAddress.complement ? <Info label="Complemento" value={contract.careAddress.complement} /> : null}
        <Info label="Bairro" value={contract.careAddress.neighborhood} />
        <Info label="Cidade e estado" value={`${contract.careAddress.city} - ${contract.careAddress.state}`} />
        <Info label="CEP" value={formatCep(contract.careAddress.cep)} />
        {contract.careAddress.referencePoint ? <Info label="Ponto de referência" value={contract.careAddress.referencePoint} /> : null}
      </Section>

      {contract.careRoutine ? <Section title="Cuidados combinados"><Info label="Rotina de cuidados" value={contract.careRoutine.name} />{contract.careRoutine.items.map((care,index)=><CareRoutineItemDetails key={care.id??`${index}`} item={care} index={index}/>)}</Section> : null}

      <Section title="Contratação">
        <Info label="Data de início" value={formatDateBR(contract.startDate)} />
        {contract.endDate ? <Info label="Data final prevista" value={formatDateBR(contract.endDate)} /> : null}
        {contract.effectiveEndDate ? <Info label="Data efetiva de encerramento" value={formatDateBR(contract.effectiveEndDate)} /> : null}
        {contract.scheduleDays.map((schedule) => (
          <View key={`${schedule.weekday}-${schedule.startTime}`} style={styles.scheduleRow}>
            <Clock3 color={colors.primary} size={15} />
            <Text style={styles.scheduleText}>{contractWeekdayLabels[schedule.weekday]} · {formatScheduleTime(schedule.startTime)} às {formatScheduleTime(schedule.endTime)}</Text>
          </View>
        ))}
      </Section>

      {!contract.careRoutine && contract.activities.length ? <Section title="Cuidados informados anteriormente">
        <View style={styles.chips}>{contract.activities.map((activity) => <View key={activity} style={styles.chip}><Text style={styles.chipText}>{activity}</Text></View>)}</View>
      </Section> : null}

      <Section title="Necessidades da pessoa assistida">
        <Info label="Necessidades" value={contract.needsDescription} />
        {contract.additionalNotes ? <Info label="Observações adicionais" value={contract.additionalNotes} /> : null}
        {contract.negotiationNotes ? <Info label="Negociação" value={contract.negotiationNotes} /> : null}
      </Section>

      <PrimaryButton label="Ver contratação" onPress={() => router.push(`/responsible-contract/${contract.id}?itemType=CARE_CONTRACT` as Href)} />
    </ScreenContainer>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return <View style={styles.section}><Text style={styles.sectionTitle}>{title}</Text>{children}</View>;
}

function Info({ label, value }: { label: string; value: string }) {
  if (!value?.trim()) return null;
  return <View style={styles.info}><Text style={styles.infoLabel}>{label}</Text><Text style={styles.infoValue}>{value}</Text></View>;
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, gap: spacing.lg },
  center: { flexGrow: 1, alignItems: 'center', justifyContent: 'center', gap: spacing.md, padding: spacing.xl },
  stateText: { textAlign: 'center', fontFamily: fontFamily.regular, fontSize: 13, lineHeight: 20, color: colors.mutedForeground },
  errorTitle: { textAlign: 'center', fontFamily: fontFamily.bold, color: colors.destructive },
  notice: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: '#F0D77E', backgroundColor: '#FFF9E5' },
  noticeText: { flex: 1, fontFamily: fontFamily.semiBold, fontSize: 13, lineHeight: 20, color: '#755B00' },
  section: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card },
  sectionTitle: { fontFamily: fontFamily.extraBold, fontSize: 17, color: colors.foreground },
  statusRow: { flexDirection: 'row' },
  info: { gap: spacing.xxs },
  infoLabel: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground },
  infoValue: { fontFamily: fontFamily.medium, fontSize: 13, lineHeight: 20, color: colors.foreground },
  personRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.md },
  flex: { flex: 1, gap: spacing.xs },
  personRole: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground },
  personName: { fontFamily: fontFamily.bold, fontSize: 15, color: colors.foreground },
  addressHeading: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
  addressText: { flex: 1, fontFamily: fontFamily.bold, fontSize: 13, color: colors.foreground },
  scheduleRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
  scheduleText: { flex: 1, fontFamily: fontFamily.medium, fontSize: 12, lineHeight: 18, color: colors.foreground },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm },
  chip: { paddingHorizontal: spacing.md, paddingVertical: spacing.sm, borderRadius: radii.full, backgroundColor: colors.secondary },
  chipText: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.primary },
});
