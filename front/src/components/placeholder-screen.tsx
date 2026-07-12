import type { LucideIcon } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { ScreenContainer } from '@/components/screen-container';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

type Props = {
  title?: string;
  description?: string;
  icon: LucideIcon;
};

export function PlaceholderScreen({
  title = 'Em desenvolvimento',
  description = 'Esta funcionalidade estará disponível em uma próxima versão.',
  icon: Icon,
}: Props) {
  return (
    <ScreenContainer contentStyle={styles.content}>
      <View style={styles.card}>
        <View style={styles.iconBox}>
          <Icon color={colors.primary} size={24} strokeWidth={2.4} />
        </View>
        <Text style={styles.title}>{title}</Text>
        <Text style={styles.description}>{description}</Text>
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    flexGrow: 1,
    justifyContent: 'center',
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.lg,
  },
  card: {
    alignItems: 'center',
    gap: spacing.md,
    borderRadius: radii.xxl,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    padding: spacing.xxl,
    ...shadows.card,
  },
  iconBox: {
    width: 56,
    height: 56,
    borderRadius: radii.full,
    backgroundColor: colors.secondary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  title: {
    textAlign: 'center',
    fontFamily: fontFamily.extraBold,
    fontSize: 20,
    color: colors.foreground,
  },
  description: {
    maxWidth: 260,
    textAlign: 'center',
    fontFamily: fontFamily.regular,
    fontSize: 13,
    lineHeight: 20,
    color: colors.mutedForeground,
  },
});
