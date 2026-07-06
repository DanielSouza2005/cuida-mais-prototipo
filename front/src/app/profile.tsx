import { useEffect, useMemo, useState } from 'react';
import { router, type Href } from 'expo-router';
import { Bell, HeartPulse, Home, LogOut, MapPin, Shield, User, Users } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { ProfileAvatar } from '@/components/profile-avatar';
import { ProfileTypeBadge } from '@/components/profile-type-badge';
import { ScreenContainer } from '@/components/screen-container';
import { SettingsRow } from '@/components/settings-row';
import { useAuth } from '@/hooks/useAuth';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

const routes = {
  assistedPerson: '/profile-assisted-person' as Href,
  careAddress: '/profile-care-address' as Href,
  emergencyContact: '/profile-emergency-contact' as Href,
};

export default function ProfileScreen() {
  const { user, isAuthenticated, isLoading, logout } = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const isCaregiver = user?.userType === 'caregiver';

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
        <ProfileAvatar initials={initials} name={user?.fullName ?? 'Cuidar+'} subtitle={user?.email ?? 'Perfil de protótipo'} />
        {user ? <ProfileTypeBadge type={isCaregiver ? 'CUIDADOR' : 'RESPONSAVEL'} /> : <Text style={styles.profileNote}>Carregando dados do perfil.</Text>}
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Configurações</Text>
        <SettingsRow title="Informações pessoais" description="Nome, CPF, e-mail, telefone e nascimento" icon={User} onPress={() => router.push('/profile-personal-info')} />
        {isCaregiver ? (
          <>
            <SettingsRow title="Endereço" description="Localização e dados de endereço" icon={Home} onPress={() => router.push('/profile-caregiver-address')} />
            <SettingsRow title="Experiência" description="Trajetória, formação e biografia profissional" icon={HeartPulse} onPress={() => router.push('/profile-caregiver-experience')} />
            <SettingsRow title="Disponibilidade" description="Horários, modalidade de atendimento e agenda" icon={MapPin} onPress={() => router.push('/profile-caregiver-availability')} />
            <SettingsRow title="Serviços oferecidos" description="Atividades de cuidado disponíveis" icon={Users} onPress={() => router.push('/profile-caregiver-services')} />
          </>
        ) : (
          <>
            <SettingsRow title="Pessoa assistida" description="Perfil de cuidado e necessidades importantes" icon={Users} onPress={() => router.push(routes.assistedPerson)} />
            <SettingsRow title="Endereço do cuidado" description="Local onde o cuidado será realizado" icon={MapPin} onPress={() => router.push(routes.careAddress)} />
            <SettingsRow title="Contato de emergência" description="Nome, telefone e vínculo de apoio" icon={HeartPulse} onPress={() => router.push(routes.emergencyContact)} />
          </>
        )}
        <SettingsRow title="Notificações" description="Preferências de comunicação" icon={Bell} onPress={() => router.push('/profile-notifications')} />
        <SettingsRow title="Privacidade" description="Segurança da conta e dados pessoais" icon={Shield} onPress={() => router.push('/profile-privacy')} />
        <SettingsRow
          title={isLoggingOut ? 'Saindo...' : 'Sair'}
          description="Encerrar sessão neste aparelho"
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
