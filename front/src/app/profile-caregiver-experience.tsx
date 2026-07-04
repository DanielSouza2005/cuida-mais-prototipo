import { useEffect, useState } from 'react';
import { HeartPulse, Save } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { OptionGroup } from '@/components/option-group';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { caregiverEducationOptions, type CaregiverEducation } from '@/constants/enums';
import { ApiError } from '@/services/api';
import { getMyProfile, updateCaregiverExperience } from '@/services/profileService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

export default function ProfileCaregiverExperienceScreen() {
  const [experiencia, setExperiencia] = useState('');
  const [formacao, setFormacao] = useState<CaregiverEducation | null>(null);
  const [formacaoOutro, setFormacaoOutro] = useState('');
  const [biografia, setBiografia] = useState('');
  const [feedback, setFeedback] = useState<string | null>(null);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    let active = true;
    getMyProfile()
      .then((profile) => {
        if (!active) return;
        setExperiencia(profile.caregiverProfile?.experiencia ?? '');
        setFormacao(profile.caregiverProfile?.formacao ?? null);
        setFormacaoOutro(profile.caregiverProfile?.formacaoOutro ?? '');
        setBiografia(profile.caregiverProfile?.biografia ?? '');
      })
      .catch((error) => setFeedback(error instanceof ApiError ? error.message : 'Nao foi possivel carregar a experiencia.'))
      .finally(() => active && setIsLoading(false));

    return () => {
      active = false;
    };
  }, []);

  async function handleSave() {
    setFeedback(null);
    setIsSuccess(false);

    if (!experiencia.trim()) return setFeedback('Informe sua experiencia.');
    if (formacao === 'OUTRO' && !formacaoOutro.trim()) return setFeedback('Informe a formacao personalizada.');

    try {
      setIsSaving(true);
      const response = await updateCaregiverExperience({
        experiencia: experiencia.trim(),
        formacao,
        formacaoOutro: formacao === 'OUTRO' ? formacaoOutro.trim() : null,
        biografia: biografia.trim() || null,
      });
      setFeedback(response.message);
      setIsSuccess(true);
    } catch (error) {
      setFeedback(error instanceof ApiError ? error.message : 'Nao foi possivel salvar a experiencia.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <AppHeader showBack title="Experiencia" subtitle="Trajetoria, formacao e biografia profissional" />
      <View style={styles.card}>
        <AppTextInput required label="Experiencia" icon={HeartPulse} placeholder="Resumo da experiencia" value={experiencia} onChangeText={setExperiencia} multiline editable={!isLoading} />
        <OptionGroup optional label="Formacao" options={caregiverEducationOptions} value={formacao} onChange={(value) => setFormacao(value as CaregiverEducation)} />
        {formacao === 'OUTRO' ? (
          <AppTextInput required label="Formacao personalizada" icon={HeartPulse} placeholder="Informe sua formacao" value={formacaoOutro} onChangeText={setFormacaoOutro} editable={!isLoading} />
        ) : null}
        <AppTextInput optional label="Biografia profissional" icon={HeartPulse} placeholder="Apresentacao breve" value={biografia} onChangeText={setBiografia} multiline editable={!isLoading} />
        {feedback ? <Text style={[styles.feedback, isSuccess && styles.success]}>{feedback}</Text> : null}
        <PrimaryButton label={isSaving ? 'Salvando...' : 'Salvar alteracoes'} icon={Save} onPress={handleSave} disabled={isLoading || isSaving} />
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
