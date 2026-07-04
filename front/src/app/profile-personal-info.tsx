import { useEffect, useState } from 'react';
import { IdCard, Mail, Phone, Save, User } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { DatePickerField } from '@/components/date-picker-field';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { ApiError } from '@/services/api';
import { getMyProfile, updatePersonalInfo } from '@/services/profileService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import { formatCpf, formatPhone, isValidEmailFormat } from '@/utils/masks';

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
      .catch((error) => setFeedback(error instanceof ApiError ? error.message : 'Nao foi possivel carregar o perfil.'))
      .finally(() => active && setIsLoading(false));

    return () => {
      active = false;
    };
  }, []);

  async function handleSave() {
    setFeedback(null);
    setIsSuccess(false);

    if (!nome.trim()) return setFeedback('Informe seu nome completo.');
    if (!isValidEmailFormat(email)) return setFeedback('Informe um e-mail valido.');
    if (!telefone.trim()) return setFeedback('Informe seu telefone.');
    if (!dataNascimento.trim()) return setFeedback('Informe sua data de nascimento.');

    try {
      setIsSaving(true);
      const response = await updatePersonalInfo({
        nome: nome.trim(),
        email: email.trim(),
        telefone,
        dataNascimento,
      });
      await restoreSession();
      setFeedback(response.message);
      setIsSuccess(true);
    } catch (error) {
      setFeedback(error instanceof ApiError ? error.message : 'Nao foi possivel salvar as informacoes.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <AppHeader showBack title="Informacoes pessoais" subtitle="Nome, CPF, e-mail, telefone e nascimento" />
      <View style={styles.card}>
        <AppTextInput required label="Nome completo" icon={User} placeholder="Nome completo" value={nome} onChangeText={setNome} editable={!isLoading} />
        <AppTextInput label="CPF" icon={IdCard} placeholder="000.000.000-00" value={cpf} editable={false} />
        <AppTextInput required label="E-mail" icon={Mail} placeholder="seu@email.com" value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" editable={!isLoading} />
        <AppTextInput required label="Telefone" icon={Phone} placeholder="(00) 00000-0000" value={telefone} onChangeText={(value) => setTelefone(formatPhone(value))} keyboardType="phone-pad" editable={!isLoading} />
        <DatePickerField required label="Data de nascimento" value={dataNascimento} onChange={setDataNascimento} maxDate={new Date()} disabled={isLoading} />
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
