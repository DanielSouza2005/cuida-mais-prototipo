import type { LucideIcon } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

type Props = {
  eyebrow: string;
  title: string;
  description: string;
  icon: LucideIcon;
};

export function SummaryCard({ eyebrow, title, description, icon: Icon }: Props) {
  return (
    <View style={styles.card}>
      <View style={styles.iconBox}>
        <Icon color={colors.primary} size={20} strokeWidth={2.4} />
      </View>
      <View style={styles.copy}>
        <Text style={styles.eyebrow}>{eyebrow}</Text>
        <Text style={styles.title}>{title}</Text>
        <Text style={styles.description}>{description}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: 'row',
    gap: spacing.md,
    borderRadius: radii.xxl,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    padding: spacing.lg,
    ...shadows.card,
  },
  iconBox: {
    width: 44,
    height: 44,
    borderRadius: radii.full,
    backgroundColor: colors.secondary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  copy: {
    flex: 1,
    gap: spacing.xs,
  },
  eyebrow: {
    fontFamily: fontFamily.bold,
    fontSize: 11,
    letterSpacing: 0.5,
    textTransform: 'uppercase',
    color: colors.coral,
  },
  title: {
    fontFamily: fontFamily.extraBold,
    fontSize: 17,
    lineHeight: 23,
    color: colors.foreground,
  },
  description: {
    fontFamily: fontFamily.regular,
    fontSize: 12.5,
    lineHeight: 19,
    color: colors.mutedForeground,
  },
});
