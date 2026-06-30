import { useEffect, useState } from 'react';
import { Calendar, HeartPulse, IdCard, Mail, MapPin, Phone, Save, User, Users } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { DatePickerField } from '@/components/date-picker-field';
import { PrimaryButton } from '@/components/primary-button';
import { ProfileAvatar } from '@/components/profile-avatar';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { ApiError } from '@/services/api';
import { updateProfile } from '@/services/authService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

const emailRegex = /\S+@\S+\.\S+/;

export default function EditProfileScreen() {
  const { user, restoreSession } = useAuth();
  const isCaregiver = user?.userType === 'caregiver';
  const [fullName, setFullName] = useState('');
  const [cpf, setCpf] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [feedback, setFeedback] = useState<string | null>(null);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const today = new Date();

  useEffect(() => {
    if (!user) return;

    setFullName(user.fullName);
    setCpf(user.cpf);
    setEmail(user.email);
    setBirthDate(user.birthDate);
  }, [user]);

  async function handleSave() {
    setFeedback(null);
    setIsSuccess(false);

    if (!fullName.trim()) {
      setFeedback('Informe seu nome completo.');
      return;
    }

    if (!cpf.trim()) {
      setFeedback('Informe seu CPF.');
      return;
    }

    if (!email.trim() || !emailRegex.test(email.trim())) {
      setFeedback('Informe um e-mail valido.');
      return;
    }

    if (!birthDate.trim()) {
      setFeedback('Informe sua data de nascimento.');
      return;
    }

    try {
      setIsSaving(true);
      await updateProfile({
        fullName: fullName.trim(),
        cpf: cpf.trim(),
        email: email.trim(),
        birthDate: birthDate.trim(),
        userType: isCaregiver ? 'caregiver' : 'family',
      });
      await restoreSession();
      setFeedback('Perfil atualizado com sucesso.');
      setIsSuccess(true);
    } catch (error) {
      setFeedback(error instanceof ApiError ? error.message : 'Nao foi possivel salvar o perfil.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <AppHeader
        showBack
        title="Editar perfil"
        subtitle={isCaregiver ? 'Dados pessoais e profissionais do cuidador.' : 'Dados pessoais, pessoa assistida e cuidado.'}
      />

      <View style={styles.card}>
        <ProfileAvatar initials="CP" name={user?.fullName} subtitle={isCaregiver ? 'Cuidador' : 'Responsavel'} />
        <AppTextInput label="Nome" icon={User} placeholder="Nome completo" value={fullName} onChangeText={setFullName} />
        <AppTextInput label="CPF" icon={IdCard} placeholder="000.000.000-00" value={cpf} onChangeText={setCpf} keyboardType="number-pad" />
        <AppTextInput label="E-mail" icon={Mail} placeholder="seu@email.com" value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" />
        <AppTextInput label="Telefone" icon={Phone} placeholder="(00) 00000-0000" value={phone} onChangeText={setPhone} keyboardType="phone-pad" />
        <DatePickerField required label="Data de nascimento" value={birthDate} onChange={setBirthDate} maxDate={today} />
      </View>

      <View style={styles.card}>
        {isCaregiver ? (
          <>
            <AppTextInput label="Experiencia" icon={HeartPulse} placeholder="Resumo da experiencia" />
            <AppTextInput label="Disponibilidade" icon={Calendar} placeholder="Dias e horarios" />
            <AppTextInput label="Endereco de atendimento" icon={MapPin} placeholder="CEP, rua, numero e cidade" />
            <AppTextInput label="Servicos oferecidos" icon={Users} placeholder="Servicos separados por virgula" />
            <AppTextInput label="Biografia profissional" icon={HeartPulse} placeholder="Apresentacao breve" />
          </>
        ) : (
          <>
            <AppTextInput label="Pessoa assistida" icon={Users} placeholder="Nome completo" />
            <AppTextInput label="Necessidades de cuidado" icon={HeartPulse} placeholder="Rotina e apoios necessarios" />
            <AppTextInput label="Endereco do cuidado" icon={MapPin} placeholder="Rua, numero, bairro e cidade" />
            <AppTextInput label="Contato de emergencia" icon={Phone} placeholder="Nome, telefone e vinculo" />
            <AppTextInput label="Observacoes importantes" icon={HeartPulse} placeholder="Alergias, medicamentos ou restricoes" />
          </>
        )}
        {feedback ? <Text style={[styles.feedbackText, isSuccess && styles.successText]}>{feedback}</Text> : null}
        <PrimaryButton label={isSaving ? 'Salvando...' : 'Salvar alteracoes'} icon={Save} onPress={handleSave} disabled={isSaving} />
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
  feedbackText: {
    fontFamily: fontFamily.medium,
    fontSize: 12,
    lineHeight: 18,
    color: colors.destructive,
  },
  successText: {
    color: colors.mintForeground,
  },
});
