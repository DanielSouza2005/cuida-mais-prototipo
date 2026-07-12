import { router } from 'expo-router';
import { Bell, HeartPulse, Home, MapPin, Shield, User, Users } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { ProfileAvatar } from '@/components/profile-avatar';
import { ProfileTypeBadge } from '@/components/profile-type-badge';
import { ScreenContainer } from '@/components/screen-container';
import { SettingsRow } from '@/components/settings-row';
import { useAuth } from '@/hooks/useAuth';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

export default function EditProfileScreen() {
  const { user } = useAuth();
  const isCaregiver = user?.userType === 'caregiver';

  return (
    <ScreenContainer contentStyle={styles.content}>
      <AppHeader
        showBack
        title="Editar perfil"
        subtitle={isCaregiver ? 'Escolha uma seção do perfil do cuidador.' : 'Escolha uma seção do perfil.'}
      />

      <View style={styles.card}>
        <ProfileAvatar imageUrl={user?.profilePhotoUrl} initials="CP" name={user?.fullName} subtitle={isCaregiver ? 'Cuidador' : 'Responsável'} />
        {user ? <ProfileTypeBadge type={isCaregiver ? 'CUIDADOR' : 'RESPONSAVEL'} /> : null}
        <Text style={styles.note}>Atualize seus dados cadastrais por grupo para manter o perfil organizado.</Text>
      </View>

      <View style={styles.section}>
        <SettingsRow title="Informações pessoais" description="Nome, CPF, e-mail, telefone e nascimento" icon={User} onPress={() => router.push('/profile-personal-info')} />
        {isCaregiver ? (
          <>
            <SettingsRow title="Endereço" description="Localização e dados de endereço" icon={Home} onPress={() => router.push('/profile-caregiver-address')} />
            <SettingsRow title="Experiência" description="Trajetória, formação e biografia profissional" icon={HeartPulse} onPress={() => router.push('/profile-caregiver-experience')} />
            <SettingsRow title="Disponibilidade" description="Horários, modalidade de atendimento e agenda" icon={MapPin} onPress={() => router.push('/profile-caregiver-availability')} />
            <SettingsRow title="Serviços oferecidos" description="Atividades de cuidado disponíveis" icon={Users} onPress={() => router.push('/profile-caregiver-services')} />
          </>
        ) : null}
        <SettingsRow title="Notificações" description="Funcionalidade em desenvolvimento" icon={Bell} onPress={() => router.push('/profile-notifications')} />
        <SettingsRow title="Privacidade" description="Funcionalidade em desenvolvimento" icon={Shield} onPress={() => router.push('/profile-privacy')} />
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
  card: {
    gap: spacing.lg,
    padding: spacing.xl,
    borderRadius: radii.xxl,
    backgroundColor: colors.card,
    borderWidth: 1,
    borderColor: colors.border,
    ...shadows.card,
  },
  note: {
    textAlign: 'center',
    fontFamily: fontFamily.regular,
    fontSize: 13,
    lineHeight: 20,
    color: colors.mutedForeground,
  },
  section: {
    gap: spacing.md,
  },
});
