import { forwardRef } from 'react';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  type PressableProps,
} from 'react-native';
import type { LucideIcon } from 'lucide-react-native';

import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import { guardPress } from '@/utils/interaction';

type Props = PressableProps & {
  label: string;
  icon?: LucideIcon;
  loading?: boolean;
  variant?: 'primary' | 'secondary';
};

export const PrimaryButton = forwardRef<React.ElementRef<typeof Pressable>, Props>(
  ({ label, icon: Icon, loading, variant = 'primary', disabled, accessibilityState, onPress, style, ...props }, ref) => {
    const secondary = variant === 'secondary';
    const isDisabled = Boolean(disabled || loading);
    const foregroundColor = secondary ? colors.primary : colors.primaryForeground;

    return (
      <Pressable
        ref={ref}
        accessibilityRole="button"
        accessibilityState={{ ...accessibilityState, disabled: isDisabled, busy: loading }}
        disabled={isDisabled}
        onPress={guardPress(isDisabled, onPress ?? undefined)}
        style={(state) => [
          styles.button,
          secondary && styles.secondary,
          isDisabled && styles.disabled,
          state.pressed && !isDisabled && styles.pressed,
          typeof style === 'function' ? style(state) : style,
        ]}
        {...props}
      >
        <Text style={[styles.label, secondary && styles.secondaryLabel, isDisabled && styles.disabledLabel]}>
          {label}
        </Text>
        {loading ? (
          <ActivityIndicator color={colors.mutedForeground} size="small" />
        ) : Icon ? (
          <Icon
            color={foregroundColor}
            size={18}
            strokeWidth={2.4}
          />
        ) : null}
      </Pressable>
    );
  },
);

PrimaryButton.displayName = 'PrimaryButton';

const styles = StyleSheet.create({
  button: {
    minHeight: 56,
    borderRadius: radii.lg,
    backgroundColor: colors.primary,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: spacing.sm,
    paddingHorizontal: spacing.xl,
    ...shadows.glow,
  },
  secondary: {
    backgroundColor: colors.secondary,
    borderWidth: 1,
    borderColor: colors.border,
    ...shadows.card,
  },
  pressed: {
    opacity: 0.86,
    transform: [{ scale: 0.99 }],
  },
  disabled: {
    backgroundColor: colors.muted,
    shadowOpacity: 0,
    elevation: 0,
  },
  label: {
    fontFamily: fontFamily.semiBold,
    fontSize: 15,
    color: colors.primaryForeground,
  },
  secondaryLabel: {
    color: colors.primary,
  },
  disabledLabel: {
    color: colors.mutedForeground,
  },
});
