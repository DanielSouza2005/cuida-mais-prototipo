import { Link, type Href } from 'expo-router';
import type { LucideIcon } from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

export type PrototypeMenuItem = {
  href: Href;
  label: string;
  route: string;
  icon: LucideIcon;
};

type Props = {
  items: PrototypeMenuItem[];
};

export function PrototypeMenu({ items }: Props) {
  return (
    <View style={styles.list}>
      {items.map(({ href, label, route, icon: Icon }) => (
        <Link key={route} href={href} asChild>
          <Pressable style={({ pressed }) => [styles.card, pressed && styles.pressed]}>
            <View style={styles.iconBox}>
              <Icon color={colors.primary} size={21} strokeWidth={2.2} />
            </View>
            <View style={styles.cardText}>
              <Text style={styles.cardTitle}>{label}</Text>
              <Text style={styles.route}>{route}</Text>
            </View>
            <Text style={styles.arrow}>›</Text>
          </Pressable>
        </Link>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  list: {
    gap: spacing.md,
  },
  card: {
    minHeight: 76,
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
    padding: spacing.lg,
    borderRadius: radii.lg,
    backgroundColor: colors.card,
    borderWidth: 1,
    borderColor: colors.border,
    ...shadows.card,
  },
  pressed: {
    opacity: 0.8,
  },
  iconBox: {
    width: 44,
    height: 44,
    borderRadius: radii.md,
    backgroundColor: colors.secondary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  cardText: {
    flex: 1,
  },
  cardTitle: {
    fontFamily: fontFamily.bold,
    fontSize: 14,
    color: colors.foreground,
  },
  route: {
    marginTop: 2,
    fontFamily: fontFamily.regular,
    fontSize: 11,
    color: colors.mutedForeground,
  },
  arrow: {
    fontFamily: fontFamily.regular,
    fontSize: 26,
    color: colors.mutedForeground,
  },
});
