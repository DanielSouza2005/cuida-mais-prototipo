import { useEffect, useState } from 'react';
import { HeartPulse, Save } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { LoadingState } from '@/components/loading-state';
import { OptionGroup } from '@/components/option-group';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { useBlockNavigationWhenBusy } from '@/hooks/useBlockNavigationWhenBusy';
import {
  caregiverEducationOptions,
  caregiverExperienceRangeOptions,
  type CaregiverEducation,
  type CaregiverExperienceRange,
} from '@/constants/enums';
import { ApiError } from '@/services/api';
import { getMyProfile, updateCaregiverExperience } from '@/services/profileService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

export default function ProfileCaregiverExperienceScreen() {
  const [tempoExperiencia, setTempoExperiencia] = useState<CaregiverExperienceRange | null>(null);
  const [formacoes, setFormacoes] = useState<CaregiverEducation[]>([]);
  const [formacaoOutro, setFormacaoOutro] = useState('');
  const [biografia, setBiografia] = useState('');
  const [feedback, setFeedback] = useState<string | null>(null);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const formDisabled = isLoading || isSaving;
  useBlockNavigationWhenBusy(isSaving);

  useEffect(() => {
    let active = true;
    getMyProfile()
      .then((profile) => {
        if (!active) return;
        setTempoExperiencia(profile.caregiverProfile?.tempoExperiencia ?? null);
        setFormacoes(profile.caregiverProfile?.formacoes ?? (profile.caregiverProfile?.formacao ? [profile.caregiverProfile.formacao] : []));
        setFormacaoOutro(profile.caregiverProfile?.formacaoOutro ?? '');
        setBiografia(profile.caregiverProfile?.biografia ?? '');
      })
      .catch((error) => setFeedback(error instanceof ApiError ? error.message : 'Não foi possível carregar a experiência.'))
      .finally(() => active && setIsLoading(false));

    return () => {
      active = false;
    };
  }, []);

  async function handleSave() {
    setFeedback(null);
    setIsSuccess(false);

    if (!tempoExperiencia) return setFeedback('Informe seu tempo de experiência.');
    if (formacoes.includes('OUTRO') && !formacaoOutro.trim()) return setFeedback('Informe a formação personalizada.');

    try {
      setIsSaving(true);
      const response = await updateCaregiverExperience({
        tempoExperiencia,
        formacoes,
        formacaoOutro: formacoes.includes('OUTRO') ? formacaoOutro.trim() : null,
        biografia: biografia.trim() || null,
      });
      setFeedback(response.message);
      setIsSuccess(true);
    } catch (error) {
      setFeedback(error instanceof ApiError ? error.message : 'Não foi possível salvar a experiência.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <AppHeader showBack backDisabled={isSaving} title="Experiência" subtitle="Trajetória, formação e biografia profissional" />
      <View style={styles.card}>
        {isLoading ? (
          <LoadingState />
        ) : (
          <>
            <OptionGroup required label="Experiência" options={caregiverExperienceRangeOptions} value={tempoExperiencia} onChange={(value) => setTempoExperiencia(value as CaregiverExperienceRange)} disabled={formDisabled} />
            <OptionGroup multiple optional label="Formação" options={caregiverEducationOptions} value={formacoes} onChange={(value) => setFormacoes(value as CaregiverEducation[])} disabled={formDisabled} />
            {formacoes.includes('OUTRO') ? (
              <AppTextInput required label="Formação personalizada" icon={HeartPulse} placeholder="Informe sua formação" value={formacaoOutro} onChangeText={setFormacaoOutro} disabled={formDisabled} />
            ) : null}
            <AppTextInput optional label="Biografia profissional" icon={HeartPulse} placeholder="Apresentação breve" value={biografia} onChangeText={setBiografia} multiline numberOfLines={3} textAlignVertical="top" disabled={formDisabled} />
          </>
        )}
        {feedback ? <Text style={[styles.feedback, isSuccess && styles.success]}>{feedback}</Text> : null}
        <PrimaryButton label={isSaving ? 'Salvando...' : 'Salvar alterações'} icon={Save} onPress={handleSave} disabled={formDisabled} loading={isSaving} />
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
