import { Pressable, StyleSheet, Text, View } from 'react-native';

import type { Option } from '@/constants/enums';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';

type Props<T extends string> = {
  label: string;
  multiple?: boolean;
  optional?: boolean;
  options: readonly Option<T>[];
  required?: boolean;
  value: T | T[] | null;
  onChange: (value: T | T[]) => void;
};

export function OptionGroup<T extends string>({
  label,
  multiple,
  optional,
  options,
  required,
  value,
  onChange,
}: Props<T>) {
  const selectedValues = Array.isArray(value) ? value : value ? [value] : [];

  function handlePress(optionValue: T) {
    if (!multiple) {
      onChange(optionValue);
      return;
    }

    const nextValue = selectedValues.includes(optionValue)
      ? selectedValues.filter((selected) => selected !== optionValue)
      : [...selectedValues, optionValue];

    onChange(nextValue);
  }

  return (
    <View style={styles.wrapper}>
      <Text style={styles.label}>
        {label}
        {optional ? ' (opcional)' : null}
        {required ? <Text style={styles.requiredMark}> *</Text> : null}
      </Text>
      <View style={styles.options}>
        {options.map((option) => {
          const selected = selectedValues.includes(option.value);

          return (
            <Pressable
              accessibilityRole={multiple ? 'checkbox' : 'radio'}
              accessibilityState={multiple ? { checked: selected } : { selected }}
              key={option.value}
              onPress={() => handlePress(option.value)}
              style={({ pressed }) => [
                styles.option,
                selected && styles.selectedOption,
                pressed && styles.pressed,
              ]}
            >
              <Text style={[styles.optionText, selected && styles.selectedOptionText]}>
                {option.label}
              </Text>
            </Pressable>
          );
        })}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    gap: spacing.sm,
  },
  label: {
    fontFamily: fontFamily.semiBold,
    fontSize: 13,
    color: colors.foreground,
  },
  requiredMark: {
    color: colors.destructive,
  },
  options: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.sm,
  },
  option: {
    minHeight: 40,
    borderRadius: radii.full,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    justifyContent: 'center',
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  selectedOption: {
    borderColor: colors.primary,
    backgroundColor: colors.secondary,
  },
  pressed: {
    opacity: 0.78,
  },
  optionText: {
    fontFamily: fontFamily.medium,
    fontSize: 12,
    lineHeight: 16,
    color: colors.foreground,
  },
  selectedOptionText: {
    color: colors.primary,
  },
});
