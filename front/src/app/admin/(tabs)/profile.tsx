import { router } from 'expo-router';
import { LogOut } from 'lucide-react-native';
import { useMemo, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { ProfileAvatar } from '@/components/profile-avatar';
import { ProfileTypeBadge } from '@/components/profile-type-badge';
import { ScreenContainer } from '@/components/screen-container';
import { SettingsRow } from '@/components/settings-row';
import { useAuth } from '@/hooks/useAuth';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

export default function AdminProfileScreen() {
  const { user, logout } = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const initials = useMemo(() => user?.fullName?.trim().split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase() || 'AD', [user?.fullName]);

  async function handleLogout() {
    setIsLoggingOut(true);
    try {
      await logout();
    } finally {
      router.replace('/login');
    }
  }

  return (
    <ScreenContainer contentStyle={styles.content} safeAreaEdges={['top', 'right', 'left']}>
      <AppHeader title="Perfil" subtitle="Seus dados de acesso administrativo." />
      <View style={styles.profileCard}>
        <ProfileAvatar
          imageUrl={user?.profilePhotoUrl}
          initials={initials}
          name={user?.fullName ?? 'Administrador'}
          subtitle={user?.email ?? 'Conta administrativa'}
        />
        <ProfileTypeBadge type="ADMIN" />
      </View>
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Conta</Text>
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
  content: { paddingHorizontal: spacing.xl, paddingBottom: spacing.md, gap: spacing.xl },
  profileCard: { alignItems: 'center', gap: spacing.lg, padding: spacing.xl, borderRadius: radii.xxl, backgroundColor: colors.card, borderWidth: 1, borderColor: colors.border, ...shadows.card },
  section: { gap: spacing.md },
  sectionTitle: { fontFamily: fontFamily.extraBold, fontSize: 18, color: colors.foreground },
});
