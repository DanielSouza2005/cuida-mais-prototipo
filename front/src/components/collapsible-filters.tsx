import type { PropsWithChildren } from 'react';
import { ChevronDown, ChevronUp, SlidersHorizontal } from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

type Props = PropsWithChildren<{
  activeFiltersCount?: number;
  accessibilityHint: string;
  collapsedAccessibilityLabel?: string;
  expanded: boolean;
  expandedAccessibilityLabel?: string;
  onToggle: () => void;
}>;

export function CollapsibleFilters({
  activeFiltersCount = 0,
  accessibilityHint,
  children,
  collapsedAccessibilityLabel = 'Mostrar filtros de busca',
  expanded,
  expandedAccessibilityLabel = 'Ocultar filtros de busca',
  onToggle,
}: Props) {
  return (
    <View style={styles.wrapper}>
      <Pressable
        accessibilityRole="button"
        accessibilityLabel={expanded ? expandedAccessibilityLabel : collapsedAccessibilityLabel}
        accessibilityHint={accessibilityHint}
        accessibilityState={{ expanded }}
        onPress={onToggle}
        style={({ pressed }) => [styles.button, pressed && styles.pressed]}
      >
        <View style={styles.buttonCopy}>
          <SlidersHorizontal color={colors.primary} size={20} strokeWidth={2.5} />
          <Text style={styles.buttonText}>Filtros</Text>
          {activeFiltersCount > 0 ? (
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{activeFiltersCount}</Text>
            </View>
          ) : null}
        </View>
        {expanded
          ? <ChevronUp color={colors.primary} size={20} strokeWidth={2.5} />
          : <ChevronDown color={colors.primary} size={20} strokeWidth={2.5} />}
      </Pressable>

      {expanded ? <View style={styles.panel}>{children}</View> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    gap: spacing.lg,
  },
  button: {
    minHeight: 52,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: spacing.md,
    borderRadius: radii.lg,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    paddingHorizontal: spacing.lg,
    ...shadows.card,
  },
  buttonCopy: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
  },
  buttonText: {
    fontFamily: fontFamily.bold,
    fontSize: 15,
    color: colors.foreground,
  },
  badge: {
    minWidth: 24,
    minHeight: 24,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: radii.full,
    backgroundColor: colors.primary,
    paddingHorizontal: spacing.xs,
  },
  badgeText: {
    fontFamily: fontFamily.bold,
    fontSize: 11,
    color: colors.primaryForeground,
  },
  panel: {
    gap: spacing.lg,
    borderRadius: radii.xl,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    padding: spacing.lg,
  },
  pressed: {
    opacity: 0.8,
  },
});
