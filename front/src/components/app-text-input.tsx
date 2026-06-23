import { useState } from 'react';
import {
  StyleSheet,
  Text,
  TextInput,
  type TextInputProps,
  View,
} from 'react-native';
import type { LucideIcon } from 'lucide-react-native';
import { Eye, EyeOff } from 'lucide-react-native';

import { colors, fontFamily, radii, spacing } from '@/theme/tokens';

type Props = TextInputProps & {
  label: string;
  icon?: LucideIcon;
  visualState?: 'default' | 'success' | 'error';
};

export function AppTextInput({
  label,
  icon: Icon,
  secureTextEntry,
  visualState = 'default',
  style,
  ...props
}: Props) {
  const [focused, setFocused] = useState(false);
  const [hidden, setHidden] = useState(Boolean(secureTextEntry));
  const PasswordIcon = hidden ? EyeOff : Eye;
  const isSuccess = visualState === 'success';
  const isError = visualState === 'error';

  return (
    <View style={styles.wrapper}>
      <Text style={styles.label}>{label}</Text>
      <View
        style={[
          styles.inputShell,
          focused && styles.focused,
          isSuccess && styles.success,
          isError && styles.error,
        ]}
      >
        {Icon ? <Icon color={focused ? colors.primary : colors.mutedForeground} size={19} /> : null}
        <TextInput
          placeholderTextColor={colors.mutedForeground}
          secureTextEntry={hidden}
          onFocus={(event) => {
            setFocused(true);
            props.onFocus?.(event);
          }}
          onBlur={(event) => {
            setFocused(false);
            props.onBlur?.(event);
          }}
          style={[styles.input, style]}
          {...props}
        />
        {secureTextEntry ? (
          <PasswordIcon
            color={colors.mutedForeground}
            size={19}
            onPress={() => setHidden((value) => !value)}
          />
        ) : null}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    gap: spacing.xs,
  },
  label: {
    fontFamily: fontFamily.semiBold,
    fontSize: 13,
    color: colors.foreground,
  },
  inputShell: {
    minHeight: 54,
    borderRadius: radii.lg,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
    paddingHorizontal: spacing.lg,
  },
  focused: {
    borderColor: colors.primary,
    backgroundColor: colors.primaryForeground,
  },
  success: {
    borderColor: colors.accent,
  },
  error: {
    borderColor: colors.destructive,
  },
  input: {
    flex: 1,
    minHeight: 50,
    paddingVertical: 0,
    fontFamily: fontFamily.medium,
    fontSize: 14,
    color: colors.foreground,
  },
});
