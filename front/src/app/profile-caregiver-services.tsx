import { useEffect, useState } from 'react';
import { HeartPulse, Save } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { OptionGroup } from '@/components/option-group';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { caregiverServiceOptions, type CaregiverService } from '@/constants/enums';
import { ApiError } from '@/services/api';
import { getMyProfile, updateCaregiverServices } from '@/services/profileService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

export default function ProfileCaregiverServicesScreen() {
  const [servicosOferecidos, setServicosOferecidos] = useState<CaregiverService[]>([]);
  const [servicoOutro, setServicoOutro] = useState('');
  const [feedback, setFeedback] = useState<string | null>(null);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    let active = true;
    getMyProfile()
      .then((profile) => {
        if (!active) return;
        setServicosOferecidos(profile.caregiverProfile?.servicosOferecidos ?? []);
        setServicoOutro(profile.caregiverProfile?.servicoOutro ?? '');
      })
      .catch((error) => setFeedback(error instanceof ApiError ? error.message : 'Nao foi possivel carregar os servicos.'))
      .finally(() => active && setIsLoading(false));

    return () => {
      active = false;
    };
  }, []);

  async function handleSave() {
    setFeedback(null);
    setIsSuccess(false);

    if (servicosOferecidos.length === 0) return setFeedback('Informe ao menos um servico oferecido.');
    if (servicosOferecidos.includes('OUTRO') && !servicoOutro.trim()) return setFeedback('Informe o servico personalizado.');

    try {
      setIsSaving(true);
      const response = await updateCaregiverServices({
        servicosOferecidos,
        servicoOutro: servicosOferecidos.includes('OUTRO') ? servicoOutro.trim() : null,
      });
      setFeedback(response.message);
      setIsSuccess(true);
    } catch (error) {
      setFeedback(error instanceof ApiError ? error.message : 'Nao foi possivel salvar os servicos.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <AppHeader showBack title="Servicos oferecidos" subtitle="Atividades de cuidado disponiveis" />
      <View style={styles.card}>
        <OptionGroup required multiple label="Servicos oferecidos" options={caregiverServiceOptions} value={servicosOferecidos} onChange={(value) => setServicosOferecidos(value as CaregiverService[])} />
        {servicosOferecidos.includes('OUTRO') ? (
          <AppTextInput required label="Servico personalizado" icon={HeartPulse} placeholder="Informe o servico" value={servicoOutro} onChangeText={setServicoOutro} editable={!isLoading} />
        ) : null}
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
