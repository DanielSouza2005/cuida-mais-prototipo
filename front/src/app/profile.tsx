import { useEffect, useMemo, useState } from 'react';
import { router } from 'expo-router';
import { Bell, HeartPulse, LogOut, MapPin, Pencil, Shield, User, Users } from 'lucide-react-native';
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
  const isCaregiver = user?.userType === 'caregiver';
  const profileLabel = isCaregiver ? 'Cuidador' : 'Responsavel';

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
        <ProfileAvatar initials={initials} name={user?.fullName ?? 'Cuidar+'} subtitle={user?.email ?? 'Perfil de prototipo'} />
        <Text style={styles.profileNote}>
          {user ? `Perfil de ${profileLabel.toLowerCase()}` : 'Carregando dados do perfil.'}
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Configuracoes</Text>
        <SettingsRow
          title="Editar perfil"
          description={isCaregiver ? 'Dados pessoais e profissionais' : 'Dados pessoais e pessoa assistida'}
          icon={Pencil}
          onPress={() => router.push('/edit-profile')}
        />
        <SettingsRow title="Informacoes pessoais" description="Nome, CPF, e-mail, telefone e nascimento" icon={User} />
        {isCaregiver ? (
          <>
            <SettingsRow title="Experiencia" description="Trajetoria, formacao e biografia profissional" icon={HeartPulse} />
            <SettingsRow title="Disponibilidade" description="Horarios, regiao e modalidade de atendimento" icon={MapPin} />
            <SettingsRow title="Servicos oferecidos" description="Atividades de cuidado disponiveis" icon={Users} />
          </>
        ) : (
          <>
            <SettingsRow title="Pessoa assistida" description="Perfil de cuidado e necessidades importantes" icon={Users} />
            <SettingsRow title="Endereco do cuidado" description="Local onde o cuidado sera realizado" icon={MapPin} />
            <SettingsRow title="Contato de emergencia" description="Nome, telefone e vinculo de apoio" icon={HeartPulse} />
          </>
        )}
        <SettingsRow title="Notificacoes" description="Preferencias de comunicacao" icon={Bell} />
        <SettingsRow title="Privacidade" description="Seguranca da conta e dados pessoais" icon={Shield} />
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
