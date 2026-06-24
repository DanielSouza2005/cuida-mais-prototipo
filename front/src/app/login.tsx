import { useState } from 'react';
import { Link, router } from 'expo-router';
import { Lock, Mail } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { BackButton } from '@/components/back-button';
import { BrandMark } from '@/components/brand';
import { AppTextInput } from '@/components/app-text-input';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { ApiError } from '@/services/api';
import { useAuth } from '@/hooks/useAuth';
import { colors, fontFamily, spacing } from '@/theme/tokens';

const emailRegex = /\S+@\S+\.\S+/;

function getLoginFeedback(error: unknown) {
  if (error instanceof ApiError) {
    return error.status === 401 ? 'E-mail ou senha inválidos.' : error.message;
  }

  return 'Nao foi possivel entrar.';
}

export default function LoginScreen() {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [feedback, setFeedback] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleLogin() {
    setFeedback(null);

    if (!email.trim() || !password) {
      setFeedback('Informe e-mail e senha.');
      return;
    }

    if (!emailRegex.test(email.trim())) {
      setFeedback('Informe um e-mail valido.');
      return;
    }

    try {
      setIsSubmitting(true);
      await login({ email: email.trim(), password });
      router.replace('/profile');
    } catch (error) {
      setFeedback(getLoginFeedback(error));
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
        <Text style={styles.brandTitle}>
          Cuidar<Text style={styles.plus}>+</Text>
        </Text>
        <Text style={styles.subtitle}>Bem-vindo de volta à sua rotina de bem-estar.</Text>
      </View>

      <View style={styles.form}>
        <AppTextInput label="E-mail" icon={Mail} placeholder="seu@email.com" value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" />
        <View style={styles.passwordBlock}>
          <AppTextInput label="Senha" icon={Lock} placeholder="••••••••" value={password} onChangeText={setPassword} secureTextEntry />
          <Link href="/forgot-password" style={styles.forgotLink}>
            Esqueci minha senha
          </Link>
        </View>

        {feedback ? <Text style={styles.errorText}>{feedback}</Text> : null}

        <PrimaryButton label={isSubmitting ? 'Entrando...' : 'Entrar'} onPress={handleLogin} disabled={isSubmitting} />
      </View>

      <Text style={styles.footerText}>
        Não tem uma conta? <Link href="/signup" style={styles.footerLink}>Cadastre-se</Link>
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
  heading: {
    marginBottom: spacing.xxl,
  },
  brandTitle: {
    fontFamily: fontFamily.extraBold,
    fontSize: 28,
    lineHeight: 34,
    letterSpacing: -0.8,
    color: colors.foreground,
  },
  plus: {
    color: colors.primary,
  },
  subtitle: {
    marginTop: spacing.xs,
    fontFamily: fontFamily.regular,
    fontSize: 14,
    color: colors.mutedForeground,
  },
  form: {
    gap: spacing.lg,
  },
  passwordBlock: {
    gap: spacing.sm,
  },
  forgotLink: {
    alignSelf: 'flex-end',
    fontFamily: fontFamily.semiBold,
    fontSize: 12,
    color: colors.primary,
  },
  errorText: {
    fontFamily: fontFamily.medium,
    fontSize: 12,
    lineHeight: 18,
    color: colors.destructive,
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
