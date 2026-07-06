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
  disabled?: boolean;
  label: string;
  icon?: LucideIcon;
  optional?: boolean;
  required?: boolean;
  visualState?: 'default' | 'success' | 'error';
};

export function AppTextInput({
  label,
  icon: Icon,
  optional,
  required,
  secureTextEntry,
  disabled,
  editable: editableProp,
  textAlignVertical,
  visualState = 'default',
  style,
  ...props
}: Props) {
  const [focused, setFocused] = useState(false);
  const [hidden, setHidden] = useState(Boolean(secureTextEntry));
  const PasswordIcon = hidden ? EyeOff : Eye;
  const isSuccess = visualState === 'success';
  const isError = visualState === 'error';
  const editable = editableProp ?? !disabled;
  const isMultiline = Boolean(props.multiline);

  return (
    <View style={styles.wrapper}>
      <Text style={styles.label}>
        {label}
        {optional ? ' (opcional)' : null}
        {required ? <Text style={styles.requiredMark}> *</Text> : null}
      </Text>
      <View
        style={[
          styles.inputShell,
          isMultiline && styles.multilineShell,
          focused && styles.focused,
          isSuccess && styles.success,
          isError && styles.error,
          !editable && styles.disabledShell,
        ]}
      >
        {Icon ? <Icon color={focused ? colors.primary : colors.mutedForeground} size={19} /> : null}
        <TextInput
          placeholderTextColor={colors.mutedForeground}
          secureTextEntry={hidden}
          editable={editable}
          onFocus={(event) => {
            setFocused(true);
            props.onFocus?.(event);
          }}
          onBlur={(event) => {
            setFocused(false);
            props.onBlur?.(event);
          }}
          textAlignVertical={isMultiline ? 'top' : textAlignVertical}
          style={[styles.input, isMultiline && styles.multilineInput, style]}
          {...props}
        />
        {secureTextEntry && editable ? (
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
  requiredMark: {
    color: colors.destructive,
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
  multilineShell: {
    minHeight: 104,
    alignItems: 'flex-start',
    paddingVertical: spacing.md,
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
  disabledShell: {
    opacity: 0.62,
  },
  input: {
    flex: 1,
    minHeight: 50,
    paddingVertical: 0,
    fontFamily: fontFamily.medium,
    fontSize: 14,
    color: colors.foreground,
  },
  multilineInput: {
    minHeight: 76,
    paddingTop: 0,
    paddingBottom: 0,
  },
});
