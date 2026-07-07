import type { LucideIcon } from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View, type PressableProps } from 'react-native';

import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

type Props = PressableProps & {
  title: string;
  description: string;
  icon: LucideIcon;
};

export function QuickActionCard({ title, description, icon: Icon, style, ...props }: Props) {
  return (
    <Pressable
      accessibilityRole="button"
      style={(state) => [
        styles.card,
        state.pressed && styles.pressed,
        typeof style === 'function' ? style(state) : style,
      ]}
      {...props}
    >
      <View style={styles.iconBox}>
        <Icon color={colors.primary} size={18} strokeWidth={2.4} />
      </View>
      <Text style={styles.title}>{title}</Text>
      <Text style={styles.description}>{description}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    width: '48%',
    minHeight: 132,
    borderRadius: radii.xl,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    padding: spacing.md,
    gap: spacing.sm,
    ...shadows.card,
  },
  pressed: {
    opacity: 0.78,
    transform: [{ scale: 0.99 }],
  },
  iconBox: {
    width: 36,
    height: 36,
    borderRadius: radii.full,
    backgroundColor: colors.mint,
    alignItems: 'center',
    justifyContent: 'center',
  },
  title: {
    fontFamily: fontFamily.bold,
    fontSize: 13,
    lineHeight: 18,
    color: colors.foreground,
  },
  description: {
    fontFamily: fontFamily.regular,
    fontSize: 11.5,
    lineHeight: 17,
    color: colors.mutedForeground,
  },
});
