import { router } from 'expo-router';
import { ClipboardCheck, FileClock, ShieldAlert, UserRoundCheck } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { QuickAccessGrid, type QuickAccessItem } from '@/components/home/quick-access-grid';
import { ProfileTypeBadge } from '@/components/profile-type-badge';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { colors, fontFamily, spacing } from '@/theme/tokens';

export default function AdminHomeScreen() {
  const { user } = useAuth();
  const firstName = user?.fullName?.trim().split(/\s+/)[0] || 'administrador';
  const quickAccessItems: QuickAccessItem[] = [
    { title: 'Cuidadores pendentes', description: 'Analisar cadastros profissionais.', icon: UserRoundCheck, iconColor: colors.primary, iconBackground: colors.secondary, onPress: () => router.push({ pathname: '/admin/approvals', params: { profile: 'caregivers', status: 'PENDENTE' } }) },
    { title: 'Responsáveis pendentes', description: 'Analisar responsáveis cadastrados.', icon: ClipboardCheck, iconColor: colors.mintForeground, iconBackground: colors.mint, onPress: () => router.push({ pathname: '/admin/approvals', params: { profile: 'responsibles', status: 'PENDENTE' } }) },
    { title: 'Contas bloqueadas', description: 'Consultar e desbloquear contas.', icon: ShieldAlert, iconColor: colors.coral, iconBackground: colors.coralBackground, onPress: () => router.push({ pathname: '/admin/users', params: { status: 'BLOQUEADO' } }) },
    { title: 'Auditoria', description: 'Consultar ações críticas da plataforma.', icon: FileClock, iconColor: colors.sunnyForeground, iconBackground: colors.sunnyBackground, onPress: () => router.push('/admin/audit') },
  ];

  return <ScreenContainer contentStyle={styles.content}>
    <AppHeader />
    <View style={styles.greeting}>
      <View style={styles.greetingCopy}>
        <Text style={styles.title}>Olá, {firstName}</Text>
        <Text style={styles.subtitle}>Acesse as principais ferramentas administrativas.</Text>
      </View>
      <ProfileTypeBadge type="ADMIN" label="ADM" />
    </View>
    <View style={styles.section}>
      <Text style={styles.sectionTitle}>Acesso rápido</Text>
      <QuickAccessGrid items={quickAccessItems} />
    </View>
  </ScreenContainer>;
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, paddingBottom: spacing.xxl + 72, gap: spacing.xl },
  greeting: { flexDirection: 'row', alignItems: 'center', gap: spacing.md },
  greetingCopy: { flex: 1 },
  title: { fontFamily: fontFamily.extraBold, fontSize: 26, color: colors.foreground },
  subtitle: { marginTop: spacing.xs, fontFamily: fontFamily.regular, fontSize: 14, lineHeight: 21, color: colors.mutedForeground },
  section: { gap: spacing.md },
  sectionTitle: { fontFamily: fontFamily.extraBold, fontSize: 18, color: colors.foreground },
});
