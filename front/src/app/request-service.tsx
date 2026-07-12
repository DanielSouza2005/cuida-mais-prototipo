import { useEffect, useState } from 'react';
import { router, useLocalSearchParams, type Href } from 'expo-router';
import { FileText, MapPin, UserRound } from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { DatePickerField } from '@/components/date-picker-field';
import { OptionGroup } from '@/components/option-group';
import { LoadingState } from '@/components/loading-state';
import { PrimaryButton } from '@/components/primary-button';
import { ProfileAvatar } from '@/components/profile-avatar';
import { ScreenContainer } from '@/components/screen-container';
import { TimePickerField } from '@/components/time-picker-field';
import { ApiError } from '@/services/api';
import { getServiceRequestDraft, getServiceRequestFormData, saveServiceRequestDraft } from '@/services/serviceRequestService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { HiringType, RequestedActivity, ServiceRequestDraft, ServiceRequestFormData } from '@/types/serviceRequest';
import { activityLabels, hiringLabels, toOptions, weekdayLabels } from '@/utils/serviceRequestLabels';

function parseDate(value: string) {
  const [day, month, year] = value.split('/').map(Number);
  return new Date(year, month - 1, day);
}
function formatAddress(address: ServiceRequestDraft['address']) { return `${address.street}, ${address.number}${address.complement ? `, ${address.complement}` : ''} • ${address.neighborhood} • ${address.city} - ${address.state}`; }

export default function RequestServiceFormScreen() {
  const { caregiverId } = useLocalSearchParams<{ caregiverId?: string }>();
  const storedDraft = getServiceRequestDraft();
  const [draft, setDraft] = useState<ServiceRequestDraft | null>(storedDraft?.caregiver.id === caregiverId ? storedDraft : null);
  const [formData, setFormData] = useState<ServiceRequestFormData | null>(null);
  const [isLoading, setIsLoading] = useState(!draft);
  const [pointDate, setPointDate] = useState('');
  const [feedback, setFeedback] = useState<string | null>(null);
  const today = new Date();

  useEffect(() => {
    if (!caregiverId || formData) return;
    let active = true;
    setIsLoading(true);
    getServiceRequestFormData(caregiverId).then((data) => {
      if (!active) return;
      setFormData(data);
      const person = data.assistedPersons[0];
      const address = data.careAddresses.find((item) => item.assistedPersonId === person.id);
      if (!address) throw new Error('missing-address');
      if (!draft) setDraft({ caregiver: data.caregiver, assistedPerson: person, address, hiringType: null, specificDates: [], startDate: '', endDate: '', weekDays: [], startTime: '', endTime: '', needsDescription: '', activities: [], otherActivity: '', additionalNotes: '', negotiation: '' });
    }).catch((error) => active && setFeedback(error instanceof ApiError ? error.message : 'Não foi possível carregar os dados da solicitação.')).finally(() => active && setIsLoading(false));
    return () => { active = false; };
  }, [caregiverId, draft, formData]);

  if (isLoading) return <ScreenContainer contentStyle={styles.center}><LoadingState message="Carregando dados da solicitação..." /></ScreenContainer>;

  if (!draft) {
    return <ScreenContainer contentStyle={styles.center}><Text style={styles.feedback}>{feedback ?? 'Selecione um cuidador antes de iniciar a solicitação.'}</Text><PrimaryButton label="Voltar para busca" onPress={() => router.replace('/(tabs)/buscar' as Href)} /></ScreenContainer>;
  }
  const currentDraft = draft;

  function update<K extends keyof ServiceRequestDraft>(key: K, value: ServiceRequestDraft[K]) {
    setDraft((current) => current ? { ...current, [key]: value } : current);
    setFeedback(null);
  }

  function addPointDate() {
    if (!pointDate || currentDraft.specificDates.includes(pointDate)) return;
    update('specificDates', [...currentDraft.specificDates, pointDate]);
    setPointDate('');
  }

  function validate() {
    if (!currentDraft.hiringType) return 'Selecione o tipo de contratação.';
    if (currentDraft.hiringType === 'PONTUAL' && currentDraft.specificDates.length === 0) return 'Informe pelo menos uma data do serviço.';
    if (currentDraft.hiringType !== 'PONTUAL' && !currentDraft.startDate) return 'Informe a data prevista de início.';
    if (currentDraft.hiringType === 'PERIODO_DETERMINADO' && !currentDraft.endDate) return 'Informe a data prevista de término.';
    if (currentDraft.startDate && currentDraft.endDate && parseDate(currentDraft.endDate) < parseDate(currentDraft.startDate)) return 'A data de término não pode ser anterior à data de início.';
    if (currentDraft.hiringType !== 'PONTUAL' && currentDraft.weekDays.length === 0) return 'Selecione pelo menos um dia da semana.';
    if (!currentDraft.startTime.trim()) return 'Informe o horário inicial.';
    if (!currentDraft.endTime.trim()) return 'Informe o horário final.';
    if (currentDraft.endTime <= currentDraft.startTime) return 'O horário final deve ser maior que o horário inicial.';
    if (!currentDraft.needsDescription.trim()) return 'Descreva as necessidades da pessoa assistida.';
    if (currentDraft.activities.length === 0) return 'Selecione pelo menos uma atividade esperada.';
    if (currentDraft.activities.includes('OUTRO') && !currentDraft.otherActivity.trim()) return 'Descreva a outra atividade esperada.';
    return null;
  }

  function continueToReview() {
    const error = validate();
    if (error) { setFeedback(error); return; }
    saveServiceRequestDraft(currentDraft);
    router.push('/request-service-review' as Href);
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <AppHeader showBack title="Solicitar serviço" />
      <Text style={styles.subtitle}>Preencha as informações para enviar a solicitação ao cuidador.</Text>

      <Section title="Cuidador selecionado">
        <View style={styles.caregiverRow}>
          <ProfileAvatar imageUrl={draft.caregiver.profilePhotoUrl} initials={draft.caregiver.name.slice(0, 2).toUpperCase()} size={64} />
          <View style={styles.flex}><Text style={styles.cardTitle}>{draft.caregiver.name}</Text><Text style={styles.cardText}>{[draft.caregiver.neighborhood, draft.caregiver.city, draft.caregiver.state].filter(Boolean).join(' • ')}</Text><Text style={styles.cardText}>{draft.caregiver.servicesOffered.map((item) => activityLabels[item]).join(', ')}</Text></View>
        </View>
      </Section>

      <Section title="Pessoa assistida *"><View style={styles.choiceList}>{(formData?.assistedPersons ?? [draft.assistedPerson]).map((person) => <Pressable key={person.id} onPress={() => { const address = formData?.careAddresses.find((item) => item.assistedPersonId === person.id); if (address) setDraft({ ...draft, assistedPerson: person, address }); }}><InfoCard icon={UserRound} title={person.name} text={person.summary} selected={draft.assistedPerson.id === person.id} /></Pressable>)}</View></Section>
      <Section title="Endereço do cuidado *"><InfoCard icon={MapPin} title="Endereço selecionado" text={formatAddress(draft.address)} selected /></Section>

      <Section title="Tipo de contratação">
        <OptionGroup required label="Escolha uma opção" options={toOptions(formData?.hiringTypeOptions ?? ['PONTUAL', 'PERIODO_DETERMINADO', 'PERIODO_INDETERMINADO'], hiringLabels)} value={draft.hiringType} onChange={(value) => update('hiringType', value as HiringType)} />
        <Text style={styles.helper}>Pontual: datas específicas. Determinado: início e fim. Indeterminado: apenas início.</Text>
      </Section>

      <Section title="Datas e frequência">
        {draft.hiringType === 'PONTUAL' ? <><DatePickerField required label="Data do serviço" value={pointDate} onChange={setPointDate} minDate={today} /><PrimaryButton label="Adicionar data" variant="secondary" onPress={addPointDate} disabled={!pointDate} /><View style={styles.chips}>{draft.specificDates.map((date) => <Pressable key={date} onPress={() => update('specificDates', draft.specificDates.filter((item) => item !== date))} style={styles.chip}><Text style={styles.chipText}>{date} ×</Text></Pressable>)}</View></> : null}
        {draft.hiringType && draft.hiringType !== 'PONTUAL' ? <DatePickerField required label="Data prevista de início" value={draft.startDate} onChange={(value) => update('startDate', value)} minDate={today} /> : null}
        {draft.hiringType === 'PERIODO_DETERMINADO' ? <DatePickerField required label="Data prevista de término" value={draft.endDate} onChange={(value) => update('endDate', value)} minDate={draft.startDate ? parseDate(draft.startDate) : today} /> : null}
        {draft.hiringType && draft.hiringType !== 'PONTUAL' ? <OptionGroup required multiple label="Dias da semana" options={toOptions(formData?.weekdayOptions ?? [], weekdayLabels)} value={draft.weekDays} onChange={(value) => update('weekDays', value as string[])} /> : null}
      </Section>

      <Section title="Horários">
        <View style={styles.inline}><View style={styles.inlineField}><TimePickerField required label="Horário inicial" value={draft.startTime} onChange={(value) => update('startTime', value)} /></View><View style={styles.inlineField}><TimePickerField required label="Horário final" value={draft.endTime} onChange={(value) => update('endTime', value)} /></View></View>
        <Text style={styles.helper}>Nesta versão visual, o mesmo horário será aplicado a todos os dias selecionados.</Text>
      </Section>

      <Section title="Necessidades e atividades">
        <AppTextInput required label="Descrição das necessidades da pessoa assistida" icon={FileText} placeholder="Descreva os cuidados necessários, limitações, rotina e pontos de atenção." value={draft.needsDescription} onChangeText={(value) => update('needsDescription', value)} multiline numberOfLines={4} textAlignVertical="top" />
        <OptionGroup required multiple label="Atividades esperadas" options={toOptions(formData?.activityOptions ?? draft.caregiver.servicesOffered, activityLabels)} value={draft.activities} onChange={(value) => update('activities', value as RequestedActivity[])} />
        {draft.activities.includes('OUTRO') ? <AppTextInput required label="Descreva a atividade" icon={FileText} placeholder="Informe a atividade esperada" value={draft.otherActivity} onChangeText={(value) => update('otherActivity', value)} /> : null}
        <AppTextInput optional label="Observações adicionais" icon={FileText} placeholder="Inclua preferências, orientações ou informações complementares." value={draft.additionalNotes} onChangeText={(value) => update('additionalNotes', value)} multiline numberOfLines={3} textAlignVertical="top" />
      </Section>

      <Section title="Negociação"><AppTextInput optional label="Forma de negociação" icon={FileText} placeholder="Exemplo: combinar valor diretamente com o cuidador." value={draft.negotiation} onChangeText={(value) => update('negotiation', value)} /><Text style={styles.helper}>O valor e as condições poderão ser combinados diretamente com o cuidador.</Text></Section>
      {feedback ? <Text style={styles.feedback}>{feedback}</Text> : null}
      <PrimaryButton label="Continuar para revisão" onPress={continueToReview} />
    </ScreenContainer>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) { return <View style={styles.section}><Text style={styles.sectionTitle}>{title}</Text>{children}</View>; }
function InfoCard({ icon: Icon, title, text, selected }: { icon: typeof UserRound; title: string; text: string; selected?: boolean }) { return <View style={[styles.infoCard, selected && styles.infoCardSelected]}><Icon color={colors.primary} size={22} /><View style={styles.flex}><Text style={styles.cardTitle}>{title}</Text><Text style={styles.cardText}>{text}</Text></View>{selected ? <Text style={styles.selected}>Selecionado</Text> : null}</View>; }

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, paddingBottom: spacing.lg, gap: spacing.lg }, center: { flexGrow: 1, justifyContent: 'center', padding: spacing.xl, gap: spacing.lg }, subtitle: { fontFamily: fontFamily.regular, fontSize: 14, lineHeight: 21, color: colors.mutedForeground },
  section: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, backgroundColor: colors.card, borderWidth: 1, borderColor: colors.border, ...shadows.card }, sectionTitle: { fontFamily: fontFamily.extraBold, fontSize: 17, color: colors.foreground },
  caregiverRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.md }, choiceList: { gap: spacing.sm }, infoCard: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, padding: spacing.md, borderRadius: radii.lg, backgroundColor: colors.muted, borderWidth: 1, borderColor: colors.border }, infoCardSelected: { backgroundColor: colors.secondary, borderColor: colors.primary }, flex: { flex: 1 }, cardTitle: { fontFamily: fontFamily.bold, fontSize: 14, color: colors.foreground }, cardText: { fontFamily: fontFamily.regular, fontSize: 12, lineHeight: 18, color: colors.mutedForeground }, selected: { fontFamily: fontFamily.semiBold, fontSize: 10, color: colors.mintForeground }, helper: { fontFamily: fontFamily.regular, fontSize: 11.5, lineHeight: 17, color: colors.mutedForeground },
  inline: { flexDirection: 'row', gap: spacing.sm }, inlineField: { flex: 1 }, chips: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm }, chip: { paddingHorizontal: spacing.md, paddingVertical: spacing.sm, borderRadius: radii.full, backgroundColor: colors.secondary }, chipText: { fontFamily: fontFamily.semiBold, fontSize: 12, color: colors.primary }, feedback: { fontFamily: fontFamily.semiBold, fontSize: 13, lineHeight: 19, color: colors.destructive },
});
