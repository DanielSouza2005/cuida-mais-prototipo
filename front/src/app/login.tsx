import { router, type Href } from 'expo-router';
import { Check, Lock, Mail, Wifi } from 'lucide-react-native';
import { useCallback, useEffect, useRef, useState } from 'react';
import { ActivityIndicator, Keyboard, Pressable, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { AppTextInput } from '@/components/app-text-input';
import { BackButton } from '@/components/back-button';
import { BrandMark } from '@/components/brand';
import { FeedbackToast } from '@/components/feedback-toast';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { useBlockNavigationWhenBusy } from '@/hooks/useBlockNavigationWhenBusy';
import { ApiError } from '@/services/api';
import { checkApiHealth, HealthCheckTimeoutError } from '@/services/healthService';
import {
  clearRememberedEmail,
  getRememberedEmail,
  saveRememberedEmail,
} from '@/services/rememberMeService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

const emailRegex = /\S+@\S+\.\S+/;
const authenticatedHomeRoute = '/inicio' as Href;

type ConnectionToast = {
  description: string;
  id: number;
  title: string;
  variant: 'success' | 'error';
};

function getLoginFeedback(error: unknown) {
  if (error instanceof ApiError) {
    return error.status === 401 ? 'E-mail ou senha inválidos.' : error.message;
  }

  return 'Não foi possível entrar.';
}

export default function LoginScreen() {
  const { login } = useAuth();
  const insets = useSafeAreaInsets();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [feedback, setFeedback] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isTestingConnection, setIsTestingConnection] = useState(false);
  const [isKeyboardVisible, setIsKeyboardVisible] = useState(false);
  const [connectionToast, setConnectionToast] = useState<ConnectionToast | null>(null);
  const hasEditedEmail = useRef(false);
  const hasChangedRememberMe = useRef(false);
  const healthCheckInFlight = useRef(false);
  useBlockNavigationWhenBusy(isSubmitting);

  const dismissConnectionToast = useCallback(() => setConnectionToast(null), []);

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

  useEffect(() => {
    const showSubscription = Keyboard.addListener('keyboardDidShow', () => setIsKeyboardVisible(true));
    const hideSubscription = Keyboard.addListener('keyboardDidHide', () => setIsKeyboardVisible(false));

    return () => {
      showSubscription.remove();
      hideSubscription.remove();
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

  async function handleHealthCheck() {
    if (healthCheckInFlight.current) return;

    healthCheckInFlight.current = true;
    setConnectionToast(null);
    setIsTestingConnection(true);

    try {
      await checkApiHealth();
      setConnectionToast({
        description: 'A API respondeu corretamente.',
        id: Date.now(),
        title: 'Conexão funcionando',
        variant: 'success',
      });
    } catch (error) {
      const timedOut = error instanceof HealthCheckTimeoutError;
      setConnectionToast({
        description: timedOut
          ? 'A API demorou para responder. Tente novamente em instantes.'
          : 'Não foi possível conectar com a API. Verifique sua internet ou tente novamente.',
        id: Date.now(),
        title: timedOut ? 'Tempo esgotado' : 'Falha na conexão',
        variant: 'error',
      });
    } finally {
      healthCheckInFlight.current = false;
      setIsTestingConnection(false);
    }
  }

  return (
    <View style={styles.screen}>
      <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
        <View style={styles.topRow}>
          <BackButton disabled={isSubmitting} />
          <BrandMark />
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

      {connectionToast ? (
        <View
          pointerEvents="none"
          style={[styles.toastLayer, { bottom: insets.bottom + spacing.lg + 48 + spacing.md }]}
        >
          <FeedbackToast
            key={connectionToast.id}
            description={connectionToast.description}
            onDismiss={dismissConnectionToast}
            title={connectionToast.title}
            variant={connectionToast.variant}
          />
        </View>
      ) : null}

      {!isKeyboardVisible ? (
        <Pressable
          accessibilityLabel="Testar conexão com a API"
          accessibilityRole="button"
          accessibilityState={{ busy: isTestingConnection, disabled: isSubmitting || isTestingConnection }}
          disabled={isSubmitting || isTestingConnection}
          hitSlop={spacing.sm}
          onPress={handleHealthCheck}
          style={({ pressed }) => [
            styles.healthFab,
            {
              bottom: insets.bottom + spacing.lg,
              right: insets.right + spacing.lg,
            },
            (isSubmitting || isTestingConnection) && styles.healthFabDisabled,
            pressed && !isSubmitting && !isTestingConnection && styles.healthFabPressed,
          ]}
        >
          {isTestingConnection ? (
            <ActivityIndicator color={colors.primary} size="small" />
          ) : (
            <Wifi aria-hidden color={colors.primary} size={20} strokeWidth={2.4} />
          )}
        </Pressable>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    flexGrow: 1,
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.xxxl + 48,
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
  toastLayer: {
    position: 'absolute',
    left: spacing.lg,
    right: spacing.lg,
    zIndex: 2,
  },
  healthFab: {
    position: 'absolute',
    zIndex: 3,
    width: 48,
    height: 48,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: radii.full,
    backgroundColor: colors.card,
    ...shadows.card,
  },
  healthFabDisabled: {
    opacity: 0.62,
  },
  healthFabPressed: {
    opacity: 0.78,
    transform: [{ scale: 0.96 }],
  },
});
