import { router, type Href } from 'expo-router';
import { Check, Lock, Mail } from 'lucide-react-native';
import { useEffect, useRef, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppTextInput } from '@/components/app-text-input';
import { BackButton } from '@/components/back-button';
import { Brand } from '@/components/brand';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { useBlockNavigationWhenBusy } from '@/hooks/useBlockNavigationWhenBusy';
import { ApiError } from '@/services/api';
import {
  clearRememberedEmail,
  getRememberedEmail,
  saveRememberedEmail,
} from '@/services/rememberMeService';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';

const emailRegex = /\S+@\S+\.\S+/;
const authenticatedHomeRoute = '/inicio' as Href;

function getLoginFeedback(error: unknown) {
  if (error instanceof ApiError) {
    return error.status === 401 ? 'E-mail ou senha inválidos.' : error.message;
  }

  return 'Não foi possível entrar.';
}

export default function LoginScreen() {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [feedback, setFeedback] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const hasEditedEmail = useRef(false);
  const hasChangedRememberMe = useRef(false);
  useBlockNavigationWhenBusy(isSubmitting);

  useEffect(() => {
    let isMounted = true;

    async function loadRememberedEmail() {
      const rememberedEmail = await getRememberedEmail();
      if (!isMounted || !rememberedEmail) return;

      if (!hasEditedEmail.current) setEmail(rememberedEmail);
      if (!hasChangedRememberMe.current) setRememberMe(true);
    }

    loadRememberedEmail();

    return () => {
      isMounted = false;
    };
  }, []);

  function handleEmailChange(value: string) {
    hasEditedEmail.current = true;
    setEmail(value);
  }

  function handleRememberMeChange() {
    if (isSubmitting) return;

    hasChangedRememberMe.current = true;
    setRememberMe((currentValue) => !currentValue);
  }

  async function handleLogin() {
    if (isSubmitting) return;

    setFeedback(null);
    const normalizedEmail = email.trim().toLowerCase();

    if (!normalizedEmail || !password) {
      setFeedback('Informe e-mail e senha.');
      return;
    }

    if (!emailRegex.test(normalizedEmail)) {
      setFeedback('Informe um e-mail válido.');
      return;
    }

    try {
      setIsSubmitting(true);
      await login({ email: normalizedEmail, password });

      if (rememberMe) {
        await saveRememberedEmail(normalizedEmail);
      } else {
        await clearRememberedEmail();
      }

      router.replace(authenticatedHomeRoute);
    } catch (error) {
      setFeedback(getLoginFeedback(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <View style={styles.topRow}>
        <BackButton disabled={isSubmitting} />
        <Brand />
      </View>

      <View style={styles.heading}>
        <Text style={styles.brandTitle}>
          Cuidar<Text style={styles.plus}>+</Text>
        </Text>
        <Text style={styles.subtitle}>Bem-vindo de volta à sua rotina de bem-estar.</Text>
      </View>

      <View style={styles.form}>
        <AppTextInput
          label="E-mail"
          icon={Mail}
          placeholder="seu@email.com"
          value={email}
          onChangeText={handleEmailChange}
          keyboardType="email-address"
          autoCapitalize="none"
          disabled={isSubmitting}
        />
        <View style={styles.passwordBlock}>
          <AppTextInput
            label="Senha"
            icon={Lock}
            placeholder="••••••••"
            value={password}
            onChangeText={setPassword}
            secureTextEntry
            disabled={isSubmitting}
          />
          <View style={styles.authOptions}>
            <Pressable
              accessibilityLabel="Lembrar de mim"
              accessibilityRole="checkbox"
              accessibilityState={{ checked: rememberMe, disabled: isSubmitting }}
              disabled={isSubmitting}
              hitSlop={spacing.xs}
              onPress={handleRememberMeChange}
              style={({ pressed }) => [
                styles.rememberButton,
                isSubmitting && styles.disabledControl,
                pressed && !isSubmitting && styles.pressedControl,
              ]}
            >
              <View style={[styles.checkbox, rememberMe && styles.checkedCheckbox]}>
                {rememberMe ? <Check aria-hidden color={colors.primaryForeground} size={14} strokeWidth={3} /> : null}
              </View>
              <Text style={styles.rememberLabel}>Lembrar de mim</Text>
            </Pressable>
            <Pressable disabled={isSubmitting} onPress={() => router.push('/forgot-password')} style={styles.forgotButton}>
              <Text style={[styles.forgotLink, isSubmitting && styles.disabledLink]}>Esqueci minha senha</Text>
            </Pressable>
          </View>
        </View>

        {feedback ? <Text style={styles.errorText}>{feedback}</Text> : null}

        <PrimaryButton label={isSubmitting ? 'Entrando...' : 'Entrar'} onPress={handleLogin} disabled={isSubmitting} loading={isSubmitting} />
      </View>

      <Text style={styles.footerText}>
        Não tem uma conta?{' '}
        <Text
          accessibilityRole="link"
          onPress={isSubmitting ? undefined : () => router.push('/signup')}
          style={[styles.footerLink, isSubmitting && styles.disabledLink]}
        >
          Cadastre-se
        </Text>
      </Text>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    flexGrow: 1,
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.lg,
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
  authOptions: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'center',
    justifyContent: 'space-between',
    columnGap: spacing.md,
    rowGap: spacing.xs,
  },
  rememberButton: {
    minHeight: 44,
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
  },
  checkbox: {
    width: 20,
    height: 20,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: radii.sm / 2,
    borderWidth: 1.5,
    borderColor: colors.mutedForeground,
    backgroundColor: colors.card,
  },
  checkedCheckbox: {
    borderColor: colors.primary,
    backgroundColor: colors.primary,
  },
  rememberLabel: {
    fontFamily: fontFamily.medium,
    fontSize: 12,
    color: colors.foreground,
  },
  forgotLink: {
    fontFamily: fontFamily.semiBold,
    fontSize: 12,
    color: colors.primary,
  },
  forgotButton: {
    minHeight: 44,
    justifyContent: 'center',
  },
  disabledControl: {
    opacity: 0.55,
  },
  pressedControl: {
    opacity: 0.7,
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
  disabledLink: {
    color: colors.mutedForeground,
    opacity: 0.55,
  },
});
