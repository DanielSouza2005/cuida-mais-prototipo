import { useState } from 'react';
import { Link } from 'expo-router';
import { KeyRound, Mail } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { BackButton } from '@/components/back-button';
import { AppTextInput } from '@/components/app-text-input';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { ApiError } from '@/services/api';
import { forgotPassword } from '@/services/authService';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';

const emailRegex = /\S+@\S+\.\S+/;
const genericSuccessMessage = 'Se o e-mail estiver cadastrado, enviaremos as instrucoes de recuperacao.';

export default function ForgotPasswordScreen() {
  const [email, setEmail] = useState('');
  const [feedback, setFeedback] = useState<string | null>(null);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleForgotPassword() {
    setFeedback(null);
    setIsSuccess(false);

    if (!email.trim()) {
      setFeedback('Informe seu e-mail.');
      return;
    }

    if (!emailRegex.test(email.trim())) {
      setFeedback('Informe um e-mail valido.');
      return;
    }

    try {
      setIsSubmitting(true);
      await forgotPassword({ email: email.trim() });
      setFeedback(genericSuccessMessage);
      setIsSuccess(true);
    } catch (error) {
      setFeedback(error instanceof ApiError ? error.message : 'Nao foi possivel enviar o link.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <View style={styles.topRow}>
        <BackButton />
      </View>

      <View style={styles.iconBox}>
        <KeyRound color={colors.primary} size={30} strokeWidth={2.2} />
      </View>

      <View style={styles.heading}>
        <Text style={styles.title}>Esqueceu sua senha?</Text>
        <Text style={styles.subtitle}>
          Sem problemas. Informe seu e-mail e enviaremos um link seguro para você redefinir.
        </Text>
      </View>

      <View style={styles.form}>
        <AppTextInput label="E-mail" icon={Mail} placeholder="seu@email.com" value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" />
        {feedback ? <Text style={[styles.feedbackText, isSuccess && styles.successText]}>{feedback}</Text> : null}
        <PrimaryButton label={isSubmitting ? 'Enviando...' : 'Enviar link de recuperacao'} onPress={handleForgotPassword} disabled={isSubmitting} />
      </View>

      <Text style={styles.footerText}>
        Lembrou a senha? <Link href="/login" style={styles.footerLink}>Entrar</Link>
      </Text>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    flexGrow: 1,
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.xxl,
  },
  topRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: spacing.xxl,
  },
  iconBox: {
    width: 64,
    height: 64,
    borderRadius: radii.xl,
    backgroundColor: colors.secondary,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: spacing.xl,
  },
  heading: {
    gap: spacing.sm,
  },
  title: {
    fontFamily: fontFamily.extraBold,
    fontSize: 26,
    lineHeight: 32,
    letterSpacing: -0.7,
    color: colors.foreground,
  },
  subtitle: {
    fontFamily: fontFamily.regular,
    fontSize: 14,
    lineHeight: 22,
    color: colors.mutedForeground,
  },
  form: {
    marginTop: spacing.xxl,
    gap: spacing.lg,
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
  footerText: {
    marginTop: 'auto',
    paddingTop: spacing.xxl,
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
