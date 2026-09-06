import { router } from 'expo-router';
import { AlertTriangle, LockKeyhole, ShieldCheck, Trash2 } from 'lucide-react-native';
import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { ApiError } from '@/services/api';
import { deleteAccount, reauthenticateForAccountDeletion } from '@/services/authService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

type Step = 'information' | 'identity' | 'confirmation';

function errorMessage(error: unknown) {
  return error instanceof ApiError ? error.message : 'Não foi possível concluir agora. Tente novamente.';
}

export default function ProfileAccountDeletionScreen() {
  const { token, logout } = useAuth();
  const [step, setStep] = useState<Step>('information');
  const [password, setPassword] = useState('');
  const [confirmationToken, setConfirmationToken] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function confirmIdentity() {
    if (busy) return;
    if (!password) {
      setFeedback('Informe sua senha atual.');
      return;
    }
    try {
      setBusy(true);
      setFeedback(null);
      const response = await reauthenticateForAccountDeletion(password, token);
      setConfirmationToken(response.confirmationToken);
      setPassword('');
      setStep('confirmation');
    } catch (error) {
      setFeedback(errorMessage(error));
    } finally {
      setBusy(false);
    }
  }

  async function confirmDeletion() {
    if (busy || !confirmationToken) return;
    try {
      setBusy(true);
      setFeedback(null);
      await deleteAccount(confirmationToken, token);
      await logout();
      router.replace({ pathname: '/login', params: { accountDeleted: '1' } });
    } catch (error) {
      if (error instanceof ApiError && ['DELETION_CONFIRMATION_EXPIRED', 'INVALID_DELETION_CONFIRMATION'].includes(error.code ?? '')) {
        setConfirmationToken(null);
        setPassword('');
        setStep('identity');
      }
      setFeedback(errorMessage(error));
    } finally {
      setBusy(false);
    }
  }

  function cancel() {
    router.replace('/profile-privacy');
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <AppHeader showBack backDisabled={busy} onBackPress={cancel} title="Excluir conta" subtitle="Segurança da conta e dados pessoais" />

      {step === 'information' ? (
        <View style={styles.card}>
          <View style={styles.icon}><ShieldCheck color={colors.primary} size={28} /></View>
          <Text style={styles.title}>Antes de continuar</Text>
          <Text style={styles.intro}>Ao excluir sua conta:</Text>
          <View style={styles.list}>
            <Text style={styles.item}>• você perderá acesso ao Cuidar+;</Text>
            <Text style={styles.item}>• seus dados pessoais elegíveis serão eliminados ou anonimizados;</Text>
            <Text style={styles.item}>• determinadas informações poderão ser mantidas quando necessárias para obrigações aplicáveis, segurança ou exercício de direitos;</Text>
            <Text style={styles.item}>• essa ação não poderá ser desfeita.</Text>
          </View>
          <PrimaryButton label="Continuar" onPress={() => { setFeedback(null); setStep('identity'); }} />
          <PrimaryButton label="Cancelar" variant="secondary" onPress={cancel} />
        </View>
      ) : null}

      {step === 'identity' ? (
        <View style={styles.card}>
          <View style={styles.icon}><LockKeyhole color={colors.primary} size={28} /></View>
          <Text style={styles.title}>Confirmar identidade</Text>
          <Text style={styles.body}>Para sua segurança, informe novamente sua senha para continuar.</Text>
          <AppTextInput required label="Senha atual" secureTextEntry value={password}
            onChangeText={(value) => { setPassword(value); setFeedback(null); }} disabled={busy}
            autoCapitalize="none" autoCorrect={false} visualState={feedback ? 'error' : 'default'} />
          {feedback ? <Text accessibilityRole="alert" style={styles.error}>{feedback}</Text> : null}
          <PrimaryButton label="Confirmar identidade" loading={busy} disabled={busy} onPress={() => void confirmIdentity()} />
          <PrimaryButton label="Cancelar" variant="secondary" disabled={busy} onPress={cancel} />
        </View>
      ) : null}

      {step === 'confirmation' ? (
        <View style={[styles.card, styles.dangerCard]}>
          <View style={[styles.icon, styles.dangerIcon]}><AlertTriangle color={colors.destructive} size={28} /></View>
          <Text style={styles.title}>Tem certeza de que deseja excluir sua conta?</Text>
          <Text style={styles.body}>Essa ação não poderá ser desfeita.</Text>
          {feedback ? <Text accessibilityRole="alert" style={styles.error}>{feedback}</Text> : null}
          <PrimaryButton icon={Trash2} label="Excluir minha conta" variant="danger" loading={busy} disabled={busy} onPress={() => void confirmDeletion()} />
          <PrimaryButton label="Cancelar" variant="secondary" disabled={busy} onPress={cancel} />
        </View>
      ) : null}
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, paddingBottom: spacing.xxl, gap: spacing.xl },
  card: { gap: spacing.lg, padding: spacing.xl, borderRadius: radii.xxl, backgroundColor: colors.card, borderWidth: 1, borderColor: colors.border, ...shadows.card },
  dangerCard: { borderColor: '#F1C8C3' },
  icon: { width: 52, height: 52, alignItems: 'center', justifyContent: 'center', borderRadius: radii.full, backgroundColor: colors.secondary },
  dangerIcon: { backgroundColor: colors.coralBackground },
  title: { fontFamily: fontFamily.bold, fontSize: 20, lineHeight: 28, color: colors.foreground },
  intro: { fontFamily: fontFamily.semiBold, fontSize: 14, color: colors.foreground },
  list: { gap: spacing.md },
  item: { fontFamily: fontFamily.regular, fontSize: 14, lineHeight: 22, color: colors.mutedForeground },
  body: { fontFamily: fontFamily.regular, fontSize: 14, lineHeight: 22, color: colors.mutedForeground },
  error: { fontFamily: fontFamily.medium, fontSize: 13, lineHeight: 20, color: colors.destructive },
});
