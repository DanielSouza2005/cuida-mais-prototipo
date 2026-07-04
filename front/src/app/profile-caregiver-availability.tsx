import { useEffect, useState } from 'react';
import { Calendar, HeartPulse, Save } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { OptionGroup } from '@/components/option-group';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import {
  careModalityOptions,
  dayPeriodOptions,
  weekDayOptions,
  type CareModality,
  type DayPeriod,
  type WeekDay,
} from '@/constants/enums';
import { ApiError } from '@/services/api';
import { getMyProfile, updateCaregiverAvailability, updateCaregiverModalities } from '@/services/profileService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

export default function ProfileCaregiverAvailabilityScreen() {
  const [diasSemana, setDiasSemana] = useState<WeekDay[]>([]);
  const [periodos, setPeriodos] = useState<DayPeriod[]>([]);
  const [horarioInicio, setHorarioInicio] = useState('');
  const [horarioFim, setHorarioFim] = useState('');
  const [observacao, setObservacao] = useState('');
  const [modalidades, setModalidades] = useState<CareModality[]>([]);
  const [modalidadeOutro, setModalidadeOutro] = useState('');
  const [feedback, setFeedback] = useState<string | null>(null);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const usesCustomSchedule = periodos.includes('HORARIO_PERSONALIZADO');

  useEffect(() => {
    let active = true;
    getMyProfile()
      .then((profile) => {
        if (!active) return;
        const availability = profile.caregiverProfile?.disponibilidade;
        setDiasSemana(availability?.diasSemana ?? []);
        setPeriodos(availability?.periodos ?? []);
        setHorarioInicio(availability?.horarioInicio ?? '');
        setHorarioFim(availability?.horarioFim ?? '');
        setObservacao(availability?.observacao ?? '');
        setModalidades(profile.caregiverProfile?.modalidades ?? []);
        setModalidadeOutro(profile.caregiverProfile?.modalidadeOutro ?? '');
      })
      .catch((error) => setFeedback(error instanceof ApiError ? error.message : 'Não foi possível carregar a disponibilidade.'))
      .finally(() => active && setIsLoading(false));

    return () => {
      active = false;
    };
  }, []);

  async function handleSave() {
    setFeedback(null);
    setIsSuccess(false);

    if (diasSemana.length === 0) return setFeedback('Informe ao menos um dia de disponibilidade.');
    if (periodos.length === 0) return setFeedback('Informe ao menos um período de disponibilidade.');
    if (usesCustomSchedule && (!horarioInicio.trim() || !horarioFim.trim())) return setFeedback('Informe horário inicial e final.');
    if (modalidades.length === 0) return setFeedback('Informe ao menos uma modalidade de atendimento.');
    if (modalidades.includes('OUTRO') && !modalidadeOutro.trim()) return setFeedback('Informe a modalidade personalizada.');

    try {
      setIsSaving(true);
      await updateCaregiverAvailability({
        diasSemana,
        periodos,
        horarioInicio: usesCustomSchedule ? horarioInicio.trim() : null,
        horarioFim: usesCustomSchedule ? horarioFim.trim() : null,
        observacao: observacao.trim() || null,
      });
      const response = await updateCaregiverModalities({
        modalidades,
        modalidadeOutro: modalidades.includes('OUTRO') ? modalidadeOutro.trim() : null,
      });
      setFeedback(response.message);
      setIsSuccess(true);
    } catch (error) {
      setFeedback(error instanceof ApiError ? error.message : 'Não foi possível salvar a disponibilidade.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <AppHeader showBack title="Disponibilidade" subtitle="Horários, modalidade de atendimento e agenda" />
      <View style={styles.card}>
        <OptionGroup required multiple label="Dias da semana" options={weekDayOptions} value={diasSemana} onChange={(value) => setDiasSemana(value as WeekDay[])} />
        <OptionGroup required multiple label="Períodos" options={dayPeriodOptions} value={periodos} onChange={(value) => setPeriodos(value as DayPeriod[])} />
        {usesCustomSchedule ? (
          <>
            <AppTextInput required label="Horário inicial" icon={Calendar} placeholder="08:00" value={horarioInicio} onChangeText={setHorarioInicio} editable={!isLoading} />
            <AppTextInput required label="Horário final" icon={Calendar} placeholder="18:00" value={horarioFim} onChangeText={setHorarioFim} editable={!isLoading} />
          </>
        ) : null}
        <OptionGroup required multiple label="Modalidades de atendimento" options={careModalityOptions} value={modalidades} onChange={(value) => setModalidades(value as CareModality[])} />
        {modalidades.includes('OUTRO') ? (
          <AppTextInput required label="Modalidade personalizada" icon={HeartPulse} placeholder="Informe a modalidade" value={modalidadeOutro} onChangeText={setModalidadeOutro} editable={!isLoading} />
        ) : null}
        <AppTextInput optional label="Observação de disponibilidade" icon={Calendar} placeholder="Detalhes de agenda" value={observacao} onChangeText={setObservacao} multiline editable={!isLoading} />
        {feedback ? <Text style={[styles.feedback, isSuccess && styles.success]}>{feedback}</Text> : null}
        <PrimaryButton label={isSaving ? 'Salvando...' : 'Salvar alterações'} icon={Save} onPress={handleSave} disabled={isLoading || isSaving} />
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.xxl,
    gap: spacing.xl,
  },
  card: {
    gap: spacing.lg,
    padding: spacing.xl,
    borderRadius: radii.xxl,
    backgroundColor: colors.card,
    borderWidth: 1,
    borderColor: colors.border,
    ...shadows.card,
  },
  feedback: {
    fontFamily: fontFamily.medium,
    fontSize: 12,
    lineHeight: 18,
    color: colors.destructive,
  },
  success: {
    color: colors.mintForeground,
  },
});
