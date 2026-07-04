import { useEffect, useMemo, useRef, useState } from 'react';
import { router, useLocalSearchParams } from 'expo-router';
import { Lock, ShieldCheck } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { BackButton } from '@/components/back-button';
import { AppTextInput } from '@/components/app-text-input';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { ApiError } from '@/services/api';
import { resetPassword } from '@/services/authService';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';

function getResetPasswordFeedback(error: unknown) {
  if (error instanceof ApiError) {
    return error.message;
  }

  return 'Não foi possível redefinir a senha. Tente novamente.';
}

export default function ResetPasswordScreen() {
  const params = useLocalSearchParams<{ token?: string }>();
  const redirectTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const initialToken = useMemo(() => {
    if (typeof params.token === 'string') return params.token;
    return '';
  }, [params.token]);
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [feedback, setFeedback] = useState<string | null>(null);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const token = initialToken.trim();
  const canRequestNewLink = !token || (!isSuccess && feedback?.toLowerCase().includes('token'));

  useEffect(() => () => {
    if (redirectTimeoutRef.current) clearTimeout(redirectTimeoutRef.current);
  }, []);

  async function handleResetPassword() {
    setFeedback(null);
    setIsSuccess(false);

    if (!token || !password || !confirmPassword) {
      setFeedback(token ? 'Informe a nova senha.' : 'Link de recuperação inválido ou incompleto.');
      return;
    }

    if (password !== confirmPassword) {
      setFeedback('As senhas não conferem.');
      return;
    }

    if (password.length < 6) {
      setFeedback('A senha deve ter pelo menos 6 caracteres.');
      return;
    }

    try {
      setIsSubmitting(true);
      const response = await resetPassword({ token, newPassword: password });
      setFeedback(response.message);
      setIsSuccess(true);
      redirectTimeoutRef.current = setTimeout(() => router.replace('/login'), 1200);
    } catch (error) {
      setFeedback(getResetPasswordFeedback(error));
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
        <ShieldCheck color={colors.mintForeground} size={30} strokeWidth={2.2} />
      </View>

      <View style={styles.heading}>
        <Text style={styles.title}>Crie uma nova senha</Text>
        <Text style={styles.subtitle}>Use o link recebido por e-mail e informe uma senha com pelo menos 6 caracteres.</Text>
      </View>

      <View style={styles.form}>
        {token ? (
          <>
            <AppTextInput label="Nova senha" icon={Lock} placeholder="********" value={password} onChangeText={setPassword} secureTextEntry />
            <AppTextInput label="Confirmar nova senha" icon={Lock} placeholder="********" value={confirmPassword} onChangeText={setConfirmPassword} secureTextEntry />

            <View style={styles.rules}>
              <Text style={styles.rule}>- Minimo 6 caracteres</Text>
              <Text style={styles.rule}>- O link expira e so pode ser usado uma vez</Text>
            </View>
          </>
        ) : (
          <Text style={styles.feedbackText}>Link de recuperação inválido ou incompleto.</Text>
        )}

        {feedback ? <Text style={[styles.feedbackText, isSuccess && styles.successText]}>{feedback}</Text> : null}

        {token ? (
          <PrimaryButton
            label={isSubmitting ? 'Salvando...' : 'Salvar nova senha'}
            onPress={handleResetPassword}
            disabled={isSubmitting || isSuccess}
          />
        ) : null}

        {canRequestNewLink ? (
          <PrimaryButton
            label="Solicitar novo link"
            variant="secondary"
            onPress={() => router.replace('/forgot-password')}
            disabled={isSubmitting}
          />
        ) : null}
      </View>
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
    backgroundColor: colors.mint,
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
  rules: {
    gap: spacing.xs,
    paddingLeft: spacing.xs,
  },
  rule: {
    fontFamily: fontFamily.regular,
    fontSize: 12,
    color: colors.mutedForeground,
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
