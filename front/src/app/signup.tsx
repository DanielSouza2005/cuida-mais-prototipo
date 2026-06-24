import { useState } from 'react';
import { Link, router } from 'expo-router';
import { Calendar, Check, IdCard, Lock, Mail, User } from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { BackButton } from '@/components/back-button';
import { BrandMark } from '@/components/brand';
import { AppTextInput } from '@/components/app-text-input';
import { PrimaryButton } from '@/components/primary-button';
import { RoleSelector, type Role } from '@/components/role-selector';
import { ScreenContainer } from '@/components/screen-container';
import { ApiError } from '@/services/api';
import { useAuth } from '@/hooks/useAuth';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';

const emailRegex = /\S+@\S+\.\S+/;

function getSignupFeedback(error: unknown) {
  if (error instanceof ApiError) {
    return error.message;
  }

  return 'Nao foi possivel criar sua conta. Tente novamente.';
}

export default function SignupScreen() {
  const { signup } = useAuth();
  const [role, setRole] = useState<Role>('family');
  const [acceptedTerms, setAcceptedTerms] = useState(true);
  const [fullName, setFullName] = useState('');
  const [cpf, setCpf] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [feedback, setFeedback] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSignup() {
    setFeedback(null);

    if (!fullName.trim()) {
      setFeedback('Informe seu nome completo.');
      return;
    }

    if (!cpf.trim()) {
      setFeedback('Informe seu CPF.');
      return;
    }

    if (!email.trim()) {
      setFeedback('Informe seu e-mail.');
      return;
    }

    if (!password) {
      setFeedback('Informe uma senha.');
      return;
    }

    if (!birthDate.trim()) {
      setFeedback('Informe sua data de nascimento.');
      return;
    }

    if (!emailRegex.test(email.trim())) {
      setFeedback('Informe um e-mail valido.');
      return;
    }

    if (!acceptedTerms) {
      setFeedback('Aceite os Termos e a Politica de Privacidade.');
      return;
    }

    try {
      setIsSubmitting(true);
      await signup({
        fullName: fullName.trim(),
        cpf: cpf.trim(),
        email: email.trim(),
        password,
        birthDate: birthDate.trim(),
        userType: role,
        acceptedTerms,
      });
      router.replace('/profile');
    } catch (error) {
      setFeedback(getSignupFeedback(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <View style={styles.topRow}>
        <BackButton />
        <BrandMark />
      </View>

      <View style={styles.heading}>
        <Text style={styles.title}>Criar sua conta</Text>
        <Text style={styles.subtitle}>Conte para nós como você quer cuidar.</Text>
      </View>

      <RoleSelector value={role} onChange={setRole} />

      <View style={styles.form}>
        <AppTextInput label="Nome completo" icon={User} placeholder="Maria da Silva" value={fullName} onChangeText={setFullName} autoCapitalize="words" />
        <AppTextInput label="CPF" icon={IdCard} placeholder="000.000.000-00" value={cpf} onChangeText={setCpf} keyboardType="number-pad" />
        <AppTextInput label="E-mail" icon={Mail} placeholder="seu@email.com" value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" />
        <AppTextInput label="Data de nascimento" icon={Calendar} placeholder="dd/mm/aaaa" value={birthDate} onChangeText={setBirthDate} keyboardType="number-pad" />
        <AppTextInput label="Senha" icon={Lock} placeholder="••••••••" value={password} onChangeText={setPassword} secureTextEntry />

        <Pressable
          accessibilityRole="checkbox"
          accessibilityState={{ checked: acceptedTerms }}
          onPress={() => setAcceptedTerms((value) => !value)}
          style={({ pressed }) => [styles.termsRow, pressed && styles.pressed]}
        >
          <View style={[styles.checkbox, acceptedTerms && styles.checkboxChecked]}>
            {acceptedTerms ? <Check color={colors.primaryForeground} size={14} strokeWidth={3} /> : null}
          </View>
          <Text style={styles.termsText}>
            Concordo com os <Text style={styles.termsLink}>Termos</Text> e a{' '}
            <Text style={styles.termsLink}>Política de Privacidade</Text>.
          </Text>
        </Pressable>

        {feedback ? <Text style={styles.errorText}>{feedback}</Text> : null}

        <PrimaryButton label={isSubmitting ? 'Criando conta...' : 'Criar conta'} onPress={handleSignup} disabled={isSubmitting} />
      </View>

      <Text style={styles.footerText}>
        Já tem conta? <Link href="/login" style={styles.footerLink}>Entrar</Link>
      </Text>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.xxl,
  },
  topRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: spacing.xl,
  },
  heading: {
    marginBottom: spacing.xl,
  },
  title: {
    fontFamily: fontFamily.extraBold,
    fontSize: 26,
    lineHeight: 32,
    letterSpacing: -0.7,
    color: colors.foreground,
  },
  subtitle: {
    marginTop: spacing.xs,
    marginBottom: spacing.xs,
    fontFamily: fontFamily.regular,
    fontSize: 13.5,
    lineHeight: 20,
    color: colors.mutedForeground,
  },
  form: {
    marginTop: spacing.xl,
    gap: spacing.md,
  },
  termsRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: spacing.sm,
    paddingTop: spacing.xs,
  },
  pressed: {
    opacity: 0.78,
  },
  checkbox: {
    width: 20,
    height: 20,
    borderRadius: radii.sm,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 1,
  },
  checkboxChecked: {
    borderColor: colors.primary,
    backgroundColor: colors.primary,
  },
  termsText: {
    flex: 1,
    fontFamily: fontFamily.regular,
    fontSize: 11.5,
    lineHeight: 17,
    color: colors.mutedForeground,
  },
  termsLink: {
    fontFamily: fontFamily.semiBold,
    color: colors.primary,
  },
  errorText: {
    fontFamily: fontFamily.medium,
    fontSize: 12,
    lineHeight: 18,
    color: colors.destructive,
  },
  footerText: {
    paddingTop: spacing.xl,
    textAlign: 'center',
    fontFamily: fontFamily.regular,
    fontSize: 13,
    color: colors.mutedForeground,
  },
  footerLink: {
    fontFamily: fontFamily.semiBold,
    color: colors.primary,
  },
});
