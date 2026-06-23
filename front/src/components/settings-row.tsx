import { ChevronRight } from 'lucide-react-native';
import type { LucideIcon } from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, radii, spacing } from '@/theme/tokens';

type Props = {
  title: string;
  description?: string;
  icon: LucideIcon;
  onPress?: () => void;
};

export function SettingsRow({ title, description, icon: Icon, onPress }: Props) {
  return (
    <Pressable
      accessibilityRole={onPress ? 'button' : undefined}
      onPress={onPress}
      style={({ pressed }) => [styles.row, pressed && onPress && styles.pressed]}
    >
      <View style={styles.iconBox}>
        <Icon color={colors.primary} size={20} />
      </View>
      <View style={styles.copy}>
        <Text style={styles.title}>{title}</Text>
        {description ? <Text style={styles.description}>{description}</Text> : null}
      </View>
      <ChevronRight color={colors.mutedForeground} size={18} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  row: {
    minHeight: 70,
    borderRadius: radii.lg,
    backgroundColor: colors.card,
    borderWidth: 1,
    borderColor: colors.border,
    padding: spacing.lg,
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
  },
  pressed: {
    opacity: 0.76,
  },
  iconBox: {
    width: 42,
    height: 42,
    borderRadius: radii.md,
    backgroundColor: colors.secondary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  copy: {
    flex: 1,
    gap: spacing.xxs,
  },
  title: {
    fontFamily: fontFamily.bold,
    fontSize: 14,
    color: colors.foreground,
  },
  description: {
    fontFamily: fontFamily.regular,
    fontSize: 12,
    color: colors.mutedForeground,
  },
});
