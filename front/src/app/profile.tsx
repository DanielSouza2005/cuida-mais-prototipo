import { useEffect, useMemo, useState } from 'react';
import { router } from 'expo-router';
import { Bell, HeartPulse, LogOut, Pencil, Shield, User } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { ProfileAvatar } from '@/components/profile-avatar';
import { ScreenContainer } from '@/components/screen-container';
import { SettingsRow } from '@/components/settings-row';
import { useAuth } from '@/hooks/useAuth';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

export default function ProfileScreen() {
  const { user, isAuthenticated, isLoading, logout } = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const initials = useMemo(() => {
    const name = user?.fullName?.trim();
    if (!name) return 'CP';

    return name
      .split(/\s+/)
      .slice(0, 2)
      .map((part) => part[0])
      .join('')
      .toUpperCase();
  }, [user?.fullName]);

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.replace('/login');
    }
  }, [isAuthenticated, isLoading]);

  async function handleLogout() {
    setIsLoggingOut(true);

    try {
      await logout();
    } finally {
      setIsLoggingOut(false);
      router.replace('/login');
    }
  }

  return (
    <ScreenContainer contentStyle={styles.content}>
      <AppHeader showBack />

      <View style={styles.profileCard}>
        <ProfileAvatar initials={initials} name={user?.fullName ?? 'Cuidador Plus'} subtitle={user?.email ?? 'Perfil de protótipo'} />
        <Text style={styles.profileNote}>
          {user ? `Perfil ${user.userType === 'caregiver' ? 'cuidador' : 'responsavel/familia'}` : 'Carregando dados do perfil.'}
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Configurações</Text>
        <SettingsRow
          title="Editar perfil"
          description="Nome, contato e preferências visuais"
          icon={Pencil}
          onPress={() => router.push('/edit-profile')}
        />
        <SettingsRow title="Informações pessoais" description="Dados ainda não conectados" icon={User} />
        <SettingsRow title="Plano de cuidado" description="Espaço para preferências futuras" icon={HeartPulse} />
        <SettingsRow title="Notificações" description="Somente estrutura visual" icon={Bell} />
        <SettingsRow title="Privacidade" description="Sem regras reais nesta etapa" icon={Shield} />
        <SettingsRow
          title={isLoggingOut ? 'Saindo...' : 'Sair'}
          description="Encerrar sessao neste aparelho"
          icon={LogOut}
          onPress={isLoggingOut ? undefined : handleLogout}
        />
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
  profileCard: {
    alignItems: 'center',
    gap: spacing.lg,
    padding: spacing.xl,
    borderRadius: radii.xxl,
    backgroundColor: colors.card,
    borderWidth: 1,
    borderColor: colors.border,
    ...shadows.card,
  },
  profileNote: {
    textAlign: 'center',
    fontFamily: fontFamily.regular,
    fontSize: 13,
    lineHeight: 20,
    color: colors.mutedForeground,
  },
  section: {
    gap: spacing.md,
  },
  sectionTitle: {
    fontFamily: fontFamily.extraBold,
    fontSize: 18,
    color: colors.foreground,
  },
});
