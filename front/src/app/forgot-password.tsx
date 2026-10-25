import { useEffect, useState } from 'react';
import { router } from 'expo-router';
import { KeyRound, Lock, Mail, ShieldCheck } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { BackButton } from '@/components/back-button';
import { AppTextInput } from '@/components/app-text-input';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { useBlockNavigationWhenBusy } from '@/hooks/useBlockNavigationWhenBusy';
import { ApiError } from '@/services/api';
import { forgotPassword, resetPassword, verifyPasswordResetCode } from '@/services/authService';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';

type Step = 'email' | 'code' | 'password' | 'success';
const emailRegex = /\S+@\S+\.\S+/;
const genericMessage = 'Se o e-mail estiver cadastrado, você receberá um código de verificação.';

export default function ForgotPasswordScreen() {
  const [step, setStep] = useState<Step>('email');
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [authorization, setAuthorization] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [feedback, setFeedback] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [resendSeconds, setResendSeconds] = useState(0);
  useBlockNavigationWhenBusy(busy);

  useEffect(() => {
    if (resendSeconds <= 0) return;
    const timer = setInterval(() => setResendSeconds((value) => Math.max(0, value - 1)), 1000);
    return () => clearInterval(timer);
  }, [resendSeconds]);

  async function requestCode(resending = false) {
    if (busy) return;
    const normalizedEmail = email.trim().toLowerCase();
    if (!emailRegex.test(normalizedEmail)) return setFeedback('Informe um e-mail válido.');
    setBusy(true); setFeedback(null);
    try {
      await forgotPassword({ email: normalizedEmail });
      setEmail(normalizedEmail); setCode(''); setAuthorization(''); setResendSeconds(60); setStep('code');
      setFeedback(resending ? 'Se o e-mail estiver cadastrado, um novo código será enviado.' : genericMessage);
    } catch (error) {
      setFeedback(error instanceof ApiError ? error.message : 'Não foi possível solicitar o código. Tente novamente.');
    } finally { setBusy(false); }
  }

  async function verifyCode() {
    if (busy) return;
    if (!/^\d{6}$/.test(code)) return setFeedback('Informe o código de seis dígitos.');
    setBusy(true); setFeedback(null);
    try {
      const response = await verifyPasswordResetCode({ email, code });
      setAuthorization(response.authorization); setCode(''); setStep('password');
    } catch (error) {
      setFeedback(error instanceof ApiError ? error.message : 'Não foi possível verificar o código.');
    } finally { setBusy(false); }
  }

  async function changePassword() {
    if (busy) return;
    if (password.length < 6) return setFeedback('A senha deve ter pelo menos 6 caracteres.');
    if (password !== confirmPassword) return setFeedback('As senhas não conferem.');
    setBusy(true); setFeedback(null);
    try {
      await resetPassword({ authorization, newPassword: password });
      setAuthorization(''); setPassword(''); setConfirmPassword(''); setEmail(''); setStep('success');
    } catch (error) {
      setFeedback(error instanceof ApiError ? error.message : 'Não foi possível redefinir a senha. Tente novamente.');
    } finally { setBusy(false); }
  }

  const title = step === 'email' ? 'Esqueceu sua senha?' : step === 'code' ? 'Verifique o código' : step === 'password' ? 'Crie uma nova senha' : 'Senha redefinida';
  const subtitle = step === 'email' ? 'Informe seu e-mail para receber um código de verificação.'
    : step === 'code' ? `Digite o código de seis dígitos enviado para ${email}.`
      : step === 'password' ? 'Informe e confirme sua nova senha.'
        : 'Senha redefinida com sucesso. Entre com sua nova senha.';

  return <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
    <View style={styles.topRow}><BackButton disabled={busy} /></View>
    <View style={styles.iconBox}>{step === 'password' || step === 'success' ? <ShieldCheck color={colors.mintForeground} size={30} /> : <KeyRound color={colors.primary} size={30} />}</View>
    <View style={styles.heading}><Text style={styles.title}>{title}</Text><Text style={styles.subtitle}>{subtitle}</Text></View>
    <View style={styles.form}>
      {step === 'email' ? <><AppTextInput label="E-mail" icon={Mail} placeholder="seu@email.com" value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" disabled={busy} /><PrimaryButton label="Enviar código" onPress={() => void requestCode()} loading={busy} disabled={busy} /></> : null}
      {step === 'code' ? <><AppTextInput label="Código de verificação" icon={KeyRound} placeholder="000000" value={code} onChangeText={(value) => setCode(value.replace(/\D/g, '').slice(0, 6))} keyboardType="number-pad" maxLength={6} disabled={busy} /><PrimaryButton label="Verificar código" onPress={() => void verifyCode()} loading={busy} disabled={busy} /><PrimaryButton label={resendSeconds ? `Reenviar código em ${resendSeconds}s` : 'Reenviar código'} variant="secondary" onPress={() => void requestCode(true)} disabled={busy || resendSeconds > 0} /></> : null}
      {step === 'password' ? <><AppTextInput label="Nova senha" icon={Lock} placeholder="********" value={password} onChangeText={setPassword} secureTextEntry disabled={busy} /><AppTextInput label="Confirmar nova senha" icon={Lock} placeholder="********" value={confirmPassword} onChangeText={setConfirmPassword} secureTextEntry disabled={busy} /><Text style={styles.rule}>Mínimo de 6 caracteres.</Text><PrimaryButton label="Redefinir senha" onPress={() => void changePassword()} loading={busy} disabled={busy} /></> : null}
      {feedback ? <Text style={styles.feedback}>{feedback}</Text> : null}
      {step === 'success' ? <PrimaryButton label="Voltar para o login" onPress={() => router.replace('/login')} /> : null}
    </View>
  </ScreenContainer>;
}

const styles = StyleSheet.create({
  content: { flexGrow: 1, paddingHorizontal: spacing.xl, paddingBottom: spacing.lg }, topRow: { flexDirection: 'row', marginBottom: spacing.xxl },
  iconBox: { width: 64, height: 64, borderRadius: radii.xl, backgroundColor: colors.secondary, alignItems: 'center', justifyContent: 'center', marginBottom: spacing.xl },
  heading: { gap: spacing.sm }, title: { fontFamily: fontFamily.extraBold, fontSize: 26, lineHeight: 32, color: colors.foreground },
  subtitle: { fontFamily: fontFamily.regular, fontSize: 14, lineHeight: 22, color: colors.mutedForeground }, form: { marginTop: spacing.xxl, gap: spacing.lg },
  feedback: { fontFamily: fontFamily.medium, fontSize: 12, lineHeight: 18, color: colors.mutedForeground }, rule: { fontFamily: fontFamily.regular, fontSize: 12, color: colors.mutedForeground },
});
