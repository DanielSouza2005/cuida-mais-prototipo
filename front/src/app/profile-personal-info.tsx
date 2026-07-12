import { useEffect, useState } from 'react';
import { Calendar, IdCard, Mail, Phone, Save, User } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { LoadingState } from '@/components/loading-state';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { useBlockNavigationWhenBusy } from '@/hooks/useBlockNavigationWhenBusy';
import { ApiError } from '@/services/api';
import { getMyProfile, updatePersonalInfo } from '@/services/profileService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import { formatCpf, formatPhone } from '@/utils/masks';

export default function ProfilePersonalInfoScreen() {
  const { restoreSession } = useAuth();
  const [nome, setNome] = useState('');
  const [cpf, setCpf] = useState('');
  const [email, setEmail] = useState('');
  const [telefone, setTelefone] = useState('');
  const [dataNascimento, setDataNascimento] = useState('');
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
        setNome(profile.user.fullName ?? '');
        setCpf(formatCpf(profile.user.cpf ?? ''));
        setEmail(profile.user.email ?? '');
        setTelefone(formatPhone(profile.user.phone ?? ''));
        setDataNascimento(profile.user.birthDate ?? '');
      })
      .catch((error) => setFeedback(error instanceof ApiError ? error.message : 'Não foi possível carregar o perfil.'))
      .finally(() => active && setIsLoading(false));

    return () => {
      active = false;
    };
  }, []);

  async function handleSave() {
    setFeedback(null);
    setIsSuccess(false);

    if (!nome.trim()) return setFeedback('Informe seu nome completo.');
    if (!telefone.trim()) return setFeedback('Informe seu telefone.');

    try {
      setIsSaving(true);
      const response = await updatePersonalInfo({
        nome: nome.trim(),
        telefone,
      });
      await restoreSession();
      setFeedback(response.message);
      setIsSuccess(true);
    } catch (error) {
      setFeedback(error instanceof ApiError ? error.message : 'Não foi possível salvar as informações.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <AppHeader showBack backDisabled={isSaving} title="Informações pessoais" subtitle="Nome, CPF, e-mail, telefone e nascimento" />
      {isLoading ? (
        <LoadingState />
      ) : (
      <View style={styles.card}>
        <AppTextInput required label="Nome completo" icon={User} placeholder="Nome completo" value={nome} onChangeText={setNome} disabled={formDisabled} />
        <AppTextInput label="CPF" icon={IdCard} placeholder="000.000.000-00" value={cpf} editable={false} />
        <AppTextInput label="E-mail" icon={Mail} placeholder="seu@email.com" value={email} editable={false} />
        <AppTextInput required label="Telefone" icon={Phone} placeholder="(00) 00000-0000" value={telefone} onChangeText={(value) => setTelefone(formatPhone(value))} keyboardType="phone-pad" disabled={formDisabled} />
        <AppTextInput label="Data de nascimento" icon={Calendar} placeholder="00/00/0000" value={dataNascimento} editable={false} />
        {feedback ? <Text style={[styles.feedback, isSuccess && styles.success]}>{feedback}</Text> : null}
        <PrimaryButton label={isSaving ? 'Salvando...' : 'Salvar alterações'} icon={Save} loading={isSaving} onPress={handleSave} disabled={formDisabled} />
      </View>
      )}
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.lg,
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
