import type { LucideIcon } from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

export type QuickAccessItem = {
  title: string;
  description: string;
  icon: LucideIcon;
  iconColor: string;
  iconBackground: string;
  onPress: () => void;
};

export function QuickAccessGrid({ items }: { items: QuickAccessItem[] }) {
  return (
    <View style={styles.grid}>
      {items.map(({ title, description, icon: Icon, iconColor, iconBackground, onPress }) => (
        <Pressable
          accessibilityRole="button"
          accessibilityLabel={`${title}. ${description}`}
          key={title}
          onPress={onPress}
          style={({ pressed }) => [styles.card, pressed && styles.pressed]}
        >
          <View style={[styles.iconBox, { backgroundColor: iconBackground }]}>
            <Icon color={iconColor} size={20} strokeWidth={2.4} />
          </View>
          <Text style={styles.title}>{title}</Text>
          <Text style={styles.description}>{description}</Text>
        </Pressable>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  grid: { flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'space-between', rowGap: spacing.md },
  card: { width: '48%', minHeight: 132, gap: spacing.sm, padding: spacing.md, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card },
  pressed: { opacity: 0.78, transform: [{ scale: 0.99 }] },
  iconBox: { width: 40, height: 40, alignItems: 'center', justifyContent: 'center', borderRadius: radii.full },
  title: { fontFamily: fontFamily.bold, fontSize: 13, lineHeight: 18, color: colors.foreground },
  description: { fontFamily: fontFamily.regular, fontSize: 11.5, lineHeight: 17, color: colors.mutedForeground },
});
