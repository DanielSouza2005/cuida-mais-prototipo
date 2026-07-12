import { Shield } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { ScreenContainer } from '@/components/screen-container';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

export default function ProfilePrivacyScreen() {
  return (
    <ScreenContainer contentStyle={styles.content}>
      <AppHeader showBack title="Privacidade" subtitle="Segurança da conta e dados pessoais" />
      <View style={styles.card}>
        <Shield color={colors.primary} size={30} />
        <Text style={styles.title}>Funcionalidade em desenvolvimento.</Text>
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.lg,
    gap: spacing.xl,
  },
  card: {
    gap: spacing.lg,
    alignItems: 'center',
    padding: spacing.xl,
    borderRadius: radii.xxl,
    backgroundColor: colors.card,
    borderWidth: 1,
    borderColor: colors.border,
    ...shadows.card,
  },
  title: {
    textAlign: 'center',
    fontFamily: fontFamily.bold,
    fontSize: 16,
    color: colors.foreground,
  },
});
