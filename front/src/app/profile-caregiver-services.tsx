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
  const formDisabled = isLoading || isSaving;
  useBlockNavigationWhenBusy(isSaving);

  useEffect(() => {
    let active = true;
    getMyProfile()
      .then((profile) => {
        if (!active) return;
        setServicosOferecidos(profile.caregiverProfile?.servicosOferecidos ?? []);
        setServicoOutro(profile.caregiverProfile?.servicoOutro ?? '');
      })
      .catch((error) => setFeedback(error instanceof ApiError ? error.message : 'Não foi possível carregar os serviços.'))
      .finally(() => active && setIsLoading(false));

    return () => {
      active = false;
    };
  }, []);

  async function handleSave() {
    setFeedback(null);
    setIsSuccess(false);

    if (servicosOferecidos.length === 0) return setFeedback('Informe ao menos um serviço oferecido.');
    if (servicosOferecidos.includes('OUTRO') && !servicoOutro.trim()) return setFeedback('Informe o serviço personalizado.');

    try {
      setIsSaving(true);
      const response = await updateCaregiverServices({
        servicosOferecidos,
        servicoOutro: servicosOferecidos.includes('OUTRO') ? servicoOutro.trim() : null,
      });
      setFeedback(response.message);
      setIsSuccess(true);
    } catch (error) {
      setFeedback(error instanceof ApiError ? error.message : 'Não foi possível salvar os serviços.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <AppHeader showBack backDisabled={isSaving} title="Serviços oferecidos" subtitle="Atividades de cuidado disponíveis" />
      <View style={styles.card}>
        {isLoading ? (
          <LoadingState />
        ) : (
          <>
            <OptionGroup required multiple label="Serviços oferecidos" options={caregiverServiceOptions} value={servicosOferecidos} onChange={(value) => setServicosOferecidos(value as CaregiverService[])} disabled={formDisabled} />
            {servicosOferecidos.includes('OUTRO') ? (
              <AppTextInput required label="Serviço personalizado" icon={HeartPulse} placeholder="Informe o serviço" value={servicoOutro} onChangeText={setServicoOutro} disabled={formDisabled} />
            ) : null}
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
