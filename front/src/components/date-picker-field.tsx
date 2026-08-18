import { createElement, useState } from 'react';
import DateTimePicker, { type DateTimePickerEvent } from '@react-native-community/datetimepicker';
import { Calendar } from 'lucide-react-native';
import { Modal, Platform, Pressable, StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

type Props = {
  disabled?: boolean;
  error?: string | null;
  label: string;
  maxDate?: Date;
  maximumDate?: Date;
  minDate?: Date;
  minimumDate?: Date;
  optional?: boolean;
  placeholder?: string;
  required?: boolean;
  value: string;
  onChange: (value: string) => void;
};

function formatDate(date: Date) {
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  return `${day}/${month}/${date.getFullYear()}`;
}

function formatIsoDate(date: Date) {
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}

function parseDate(value: string) {
  const [day, month, year] = value.split('/').map(Number);
  if (!day || !month || !year) return null;

  const date = new Date(year, month - 1, day);
  return date.getFullYear() === year && date.getMonth() === month - 1 && date.getDate() === day
    ? date
    : null;
}

function parseIsoDate(value: string) {
  const [year, month, day] = value.split('-').map(Number);
  if (!day || !month || !year) return null;
  const date = new Date(year, month - 1, day);
  return date.getFullYear() === year && date.getMonth() === month - 1 && date.getDate() === day
    ? date
    : null;
}

function startOfDay(date: Date) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function initialPickerDate(value: string, minimumDate?: Date, maximumDate?: Date) {
  const selected = parseDate(value);
  if (selected) return selected;
  const today = startOfDay(new Date());
  if (minimumDate && today < minimumDate) return minimumDate;
  if (maximumDate && today > maximumDate) return maximumDate;
  return today;
}

export function DatePickerField({
  disabled,
  error,
  label,
  maxDate,
  maximumDate,
  minDate,
  minimumDate,
  optional,
  placeholder = 'Selecionar data',
  required,
  value,
  onChange,
}: Props) {
  const minimumBoundary = minimumDate ?? minDate;
  const maximumBoundary = maximumDate ?? maxDate;
  const normalizedMinimum = minimumBoundary ? startOfDay(minimumBoundary) : undefined;
  const normalizedMaximum = maximumBoundary ? startOfDay(maximumBoundary) : undefined;
  const [visible, setVisible] = useState(false);
  const [temporary, setTemporary] = useState(() => initialPickerDate(value, normalizedMinimum, normalizedMaximum));

  function open() {
    if (disabled) return;
    setTemporary(initialPickerDate(value, normalizedMinimum, normalizedMaximum));
    setVisible(true);
  }

  function handleNativeChange(event: DateTimePickerEvent, date?: Date) {
    if (Platform.OS === 'android') setVisible(false);
    if (event.type !== 'set' || !date) return;
    setTemporary(date);
    if (Platform.OS === 'android') onChange(formatDate(date));
  }

  function handleWebChange(nextValue: string) {
    const date = parseIsoDate(nextValue);
    if (date) setTemporary(date);
  }

  return (
    <View style={styles.wrapper}>
      <Text style={styles.label}>
        {label}
        {optional ? ' (opcional)' : null}
        {required ? <Text style={styles.requiredMark}> *</Text> : null}
      </Text>

      <Pressable
        accessibilityLabel={`${label}. ${value || placeholder}`}
        accessibilityRole="button"
        accessibilityState={{ disabled: Boolean(disabled) }}
        disabled={disabled}
        onPress={open}
        style={({ pressed }) => [
          styles.inputShell,
          error && styles.inputError,
          disabled && styles.disabledShell,
          pressed && !disabled && styles.pressed,
        ]}
      >
        <Calendar color={colors.mutedForeground} size={19} />
        <Text style={[styles.valueText, !value && styles.placeholderText]}>{value || placeholder}</Text>
      </Pressable>
      {error ? <Text style={styles.errorText}>{error}</Text> : null}

      {visible && Platform.OS === 'android' ? (
        <DateTimePicker
          value={temporary}
          mode="date"
          display="default"
          minimumDate={normalizedMinimum}
          maximumDate={normalizedMaximum}
          onChange={handleNativeChange}
        />
      ) : null}

      <Modal
        animationType="fade"
        transparent
        visible={visible && Platform.OS !== 'android'}
        onRequestClose={() => setVisible(false)}
      >
        <View style={styles.modalBackdrop}>
          <View style={styles.modalCard}>
            <Text style={styles.modalTitle}>Selecionar data</Text>
            {Platform.OS === 'ios' ? (
              <DateTimePicker
                value={temporary}
                mode="date"
                display="spinner"
                minimumDate={normalizedMinimum}
                maximumDate={normalizedMaximum}
                onChange={handleNativeChange}
              />
            ) : (
              createElement('input', {
                'aria-label': label,
                autoFocus: true,
                max: normalizedMaximum ? formatIsoDate(normalizedMaximum) : undefined,
                min: normalizedMinimum ? formatIsoDate(normalizedMinimum) : undefined,
                onChange: (event: { target: { value: string } }) => handleWebChange(event.target.value),
                style: webInputStyle,
                type: 'date',
                value: formatIsoDate(temporary),
              })
            )}
            <View style={styles.actions}>
              <Pressable accessibilityRole="button" onPress={() => setVisible(false)} style={styles.action}>
                <Text style={styles.cancelText}>Cancelar</Text>
              </Pressable>
              <Pressable
                accessibilityRole="button"
                onPress={() => {
                  onChange(formatDate(temporary));
                  setVisible(false);
                }}
                style={[styles.action, styles.confirmAction]}
              >
                <Text style={styles.confirmText}>Confirmar</Text>
              </Pressable>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
}

export const AdvancedDatePickerField = DatePickerField;

const webInputStyle = {
  minHeight: 52,
  border: `1px solid ${colors.border}`,
  borderRadius: radii.lg,
  padding: `0 ${spacing.lg}px`,
  backgroundColor: colors.card,
  color: colors.foreground,
  fontSize: 16,
};

const styles = StyleSheet.create({
  wrapper: { gap: spacing.xs },
  label: { fontFamily: fontFamily.semiBold, fontSize: 13, color: colors.foreground },
  requiredMark: { color: colors.destructive },
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
  inputError: { borderColor: colors.destructive },
  disabledShell: { opacity: 0.5 },
  pressed: { opacity: 0.82 },
  valueText: { flex: 1, fontFamily: fontFamily.medium, fontSize: 14, color: colors.foreground },
  placeholderText: { color: colors.mutedForeground },
  errorText: { fontFamily: fontFamily.medium, fontSize: 12, lineHeight: 18, color: colors.destructive },
  modalBackdrop: { flex: 1, justifyContent: 'center', padding: spacing.xl, backgroundColor: 'rgba(27, 45, 64, 0.34)' },
  modalCard: { gap: spacing.lg, borderRadius: radii.xl, backgroundColor: colors.card, padding: spacing.lg, ...shadows.soft },
  modalTitle: { textAlign: 'center', fontFamily: fontFamily.bold, fontSize: 17, color: colors.foreground },
  actions: { flexDirection: 'row', gap: spacing.sm },
  action: { flex: 1, minHeight: 46, alignItems: 'center', justifyContent: 'center', borderRadius: radii.lg, backgroundColor: colors.secondary },
  confirmAction: { backgroundColor: colors.primary },
  cancelText: { fontFamily: fontFamily.semiBold, color: colors.primary },
  confirmText: { fontFamily: fontFamily.bold, color: colors.primaryForeground },
});
