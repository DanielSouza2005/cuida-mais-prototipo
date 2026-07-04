import { useMemo, useState } from 'react';
import { Calendar } from 'lucide-react-native';
import { Modal, Pressable, StyleSheet, Text, View } from 'react-native';

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

type PickerMode = 'day' | 'month' | 'year';

const monthNames = [
  'Janeiro',
  'Fevereiro',
  'Março',
  'Abril',
  'Maio',
  'Junho',
  'Julho',
  'Agosto',
  'Setembro',
  'Outubro',
  'Novembro',
  'Dezembro',
];

const shortMonthNames = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
const weekDays = ['D', 'S', 'T', 'Q', 'Q', 'S', 'S'];

function formatDate(date: Date) {
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  return `${day}/${month}/${year}`;
}

function parseDate(value: string) {
  const [day, month, year] = value.split('/').map(Number);
  if (!day || !month || !year) return null;

  const date = new Date(year, month - 1, day);
  if (date.getFullYear() !== year || date.getMonth() !== month - 1 || date.getDate() !== day) {
    return null;
  }

  return date;
}

function startOfDay(date: Date) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function getDecadeStart(year: number) {
  return Math.floor(year / 12) * 12;
}

export function AdvancedDatePickerField({
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
  const selectedDate = parseDate(value);
  const today = useMemo(() => startOfDay(new Date()), []);
  const maxBoundary = maxDate ?? maximumDate;
  const minBoundary = minDate ?? minimumDate;
  const normalizedMaxDate = maxBoundary ? startOfDay(maxBoundary) : undefined;
  const normalizedMinDate = minBoundary ? startOfDay(minBoundary) : undefined;
  const initialDate = selectedDate ?? normalizedMaxDate ?? today;
  const [visible, setVisible] = useState(false);
  const [pickerMode, setPickerMode] = useState<PickerMode>('day');
  const [visibleMonth, setVisibleMonth] = useState(new Date(initialDate.getFullYear(), initialDate.getMonth(), 1));
  const [yearRangeStart, setYearRangeStart] = useState(getDecadeStart(initialDate.getFullYear()));

  const days = useMemo(() => {
    const year = visibleMonth.getFullYear();
    const month = visibleMonth.getMonth();
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const blanks = Array.from({ length: firstDay }, () => null);
    const monthDays = Array.from({ length: daysInMonth }, (_, index) => new Date(year, month, index + 1));

    return [...blanks, ...monthDays];
  }, [visibleMonth]);

  function isDateDisabled(date: Date) {
    if (normalizedMaxDate && date > normalizedMaxDate) return true;
    if (normalizedMinDate && date < normalizedMinDate) return true;
    return false;
  }

  function changeMonth(delta: number) {
    setVisibleMonth((current) => new Date(current.getFullYear(), current.getMonth() + delta, 1));
  }

  function changeYear(delta: number) {
    setVisibleMonth((current) => new Date(current.getFullYear() + delta, current.getMonth(), 1));
  }

  function changeYearRange(delta: number) {
    setYearRangeStart((current) => current + delta * 12);
  }

  function handleSelect(date: Date) {
    if (isDateDisabled(date)) return;

    onChange(formatDate(date));
    setVisible(false);
  }

  function selectMonth(month: number) {
    setVisibleMonth((current) => new Date(current.getFullYear(), month, 1));
    setPickerMode('day');
  }

  function selectYear(year: number) {
    setVisibleMonth((current) => new Date(year, current.getMonth(), 1));
    setYearRangeStart(getDecadeStart(year));
    setPickerMode('month');
  }

  function openPicker() {
    if (disabled) return;

    const nextDate = selectedDate ?? normalizedMaxDate ?? today;
    setVisibleMonth(new Date(nextDate.getFullYear(), nextDate.getMonth(), 1));
    setYearRangeStart(getDecadeStart(nextDate.getFullYear()));
    setPickerMode('day');
    setVisible(true);
  }

  return (
    <View style={styles.wrapper}>
      <Text style={styles.label}>
        {label}
        {optional ? ' (opcional)' : null}
        {required ? <Text style={styles.requiredMark}> *</Text> : null}
      </Text>

      <Pressable
        accessibilityRole="button"
        disabled={disabled}
        onPress={openPicker}
        style={({ pressed }) => [styles.inputShell, disabled && styles.disabledShell, pressed && !disabled && styles.pressed]}
      >
        <Calendar color={colors.mutedForeground} size={19} />
        <Text style={[styles.valueText, !value && styles.placeholderText]}>
          {value || placeholder}
        </Text>
      </Pressable>
      {error ? <Text style={styles.errorText}>{error}</Text> : null}

      <Modal animationType="fade" transparent visible={visible} onRequestClose={() => setVisible(false)}>
        <View style={styles.modalBackdrop}>
          <View style={styles.modalCard}>
            <View style={styles.calendarHeader}>
              <Pressable accessibilityRole="button" onPress={() => pickerMode === 'year' ? changeYearRange(-1) : pickerMode === 'month' ? changeYear(-1) : changeMonth(-1)} style={styles.monthButton}>
                <Text style={styles.monthButtonText}>Voltar</Text>
              </Pressable>
              <Pressable accessibilityRole="button" onPress={() => setPickerMode(pickerMode === 'day' ? 'month' : 'year')} style={styles.titleButton}>
                <Text style={styles.monthTitle}>
                  {pickerMode === 'year'
                    ? `${yearRangeStart}-${yearRangeStart + 11}`
                    : pickerMode === 'month'
                      ? String(visibleMonth.getFullYear())
                      : `${monthNames[visibleMonth.getMonth()]} ${visibleMonth.getFullYear()}`}
                </Text>
              </Pressable>
              <Pressable accessibilityRole="button" onPress={() => pickerMode === 'year' ? changeYearRange(1) : pickerMode === 'month' ? changeYear(1) : changeMonth(1)} style={styles.monthButton}>
                <Text style={styles.monthButtonText}>Avançar</Text>
              </Pressable>
            </View>

            <View style={styles.quickRow}>
              <Pressable accessibilityRole="button" onPress={() => changeYear(-1)} style={styles.quickButton}>
                <Text style={styles.quickButtonText}>-1 ano</Text>
              </Pressable>
              <Pressable accessibilityRole="button" onPress={() => setPickerMode('year')} style={styles.quickButton}>
                <Text style={styles.quickButtonText}>Anos</Text>
              </Pressable>
              <Pressable accessibilityRole="button" onPress={() => changeYear(1)} style={styles.quickButton}>
                <Text style={styles.quickButtonText}>+1 ano</Text>
              </Pressable>
            </View>

            {pickerMode === 'day' ? (
              <>
                <View style={styles.weekRow}>
                  {weekDays.map((day, index) => (
                    <Text key={`${day}-${index}`} style={styles.weekDay}>{day}</Text>
                  ))}
                </View>

                <View style={styles.daysGrid}>
                  {days.map((date, index) => {
                    const selected = date && selectedDate && formatDate(date) === formatDate(selectedDate);
                    const dateDisabled = Boolean(date && isDateDisabled(date));

                    return (
                      <Pressable
                        accessibilityRole="button"
                        disabled={!date || dateDisabled}
                        key={date ? formatDate(date) : `blank-${index}`}
                        onPress={() => date && handleSelect(date)}
                        style={[
                          styles.dayCell,
                          selected && styles.selectedDay,
                          dateDisabled && styles.disabledDay,
                        ]}
                      >
                        <Text style={[
                          styles.dayText,
                          selected && styles.selectedDayText,
                          dateDisabled && styles.disabledDayText,
                        ]}>
                          {date ? date.getDate() : ''}
                        </Text>
                      </Pressable>
                    );
                  })}
                </View>
              </>
            ) : null}

            {pickerMode === 'month' ? (
              <View style={styles.optionGrid}>
                {shortMonthNames.map((month, index) => (
                  <Pressable key={month} accessibilityRole="button" onPress={() => selectMonth(index)} style={styles.gridOption}>
                    <Text style={styles.gridOptionText}>{month}</Text>
                  </Pressable>
                ))}
              </View>
            ) : null}

            {pickerMode === 'year' ? (
              <View style={styles.optionGrid}>
                {Array.from({ length: 12 }, (_, index) => yearRangeStart + index).map((year) => (
                  <Pressable key={year} accessibilityRole="button" onPress={() => selectYear(year)} style={styles.gridOption}>
                    <Text style={styles.gridOptionText}>{year}</Text>
                  </Pressable>
                ))}
              </View>
            ) : null}

            <PrimaryActions onClose={() => setVisible(false)} />
          </View>
        </View>
      </Modal>
    </View>
  );
}

export const DatePickerField = AdvancedDatePickerField;

function PrimaryActions({ onClose }: { onClose: () => void }) {
  return (
    <Pressable accessibilityRole="button" onPress={onClose} style={styles.closeButton}>
      <Text style={styles.closeButtonText}>Fechar</Text>
    </Pressable>
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
  disabledShell: {
    opacity: 0.5,
  },
  pressed: {
    opacity: 0.82,
  },
  valueText: {
    flex: 1,
    fontFamily: fontFamily.medium,
    fontSize: 14,
    color: colors.foreground,
  },
  placeholderText: {
    color: colors.mutedForeground,
  },
  errorText: {
    fontFamily: fontFamily.medium,
    fontSize: 12,
    lineHeight: 18,
    color: colors.destructive,
  },
  modalBackdrop: {
    flex: 1,
    justifyContent: 'center',
    padding: spacing.xl,
    backgroundColor: 'rgba(27, 45, 64, 0.34)',
  },
  modalCard: {
    borderRadius: radii.xl,
    backgroundColor: colors.card,
    padding: spacing.lg,
    gap: spacing.md,
    ...shadows.soft,
  },
  calendarHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: spacing.sm,
  },
  monthButton: {
    minHeight: 36,
    justifyContent: 'center',
    paddingHorizontal: spacing.sm,
  },
  monthButtonText: {
    fontFamily: fontFamily.semiBold,
    fontSize: 12,
    color: colors.primary,
  },
  titleButton: {
    flex: 1,
    minHeight: 36,
    justifyContent: 'center',
  },
  monthTitle: {
    textAlign: 'center',
    fontFamily: fontFamily.bold,
    fontSize: 15,
    color: colors.foreground,
  },
  quickRow: {
    flexDirection: 'row',
    gap: spacing.sm,
  },
  quickButton: {
    flex: 1,
    minHeight: 36,
    borderRadius: radii.md,
    backgroundColor: colors.secondary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  quickButtonText: {
    fontFamily: fontFamily.semiBold,
    fontSize: 12,
    color: colors.primary,
  },
  weekRow: {
    flexDirection: 'row',
  },
  weekDay: {
    flex: 1,
    textAlign: 'center',
    fontFamily: fontFamily.semiBold,
    fontSize: 12,
    color: colors.mutedForeground,
  },
  daysGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  dayCell: {
    width: `${100 / 7}%`,
    aspectRatio: 1,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: radii.full,
  },
  selectedDay: {
    backgroundColor: colors.primary,
  },
  disabledDay: {
    opacity: 0.35,
  },
  dayText: {
    fontFamily: fontFamily.medium,
    fontSize: 13,
    color: colors.foreground,
  },
  selectedDayText: {
    color: colors.primaryForeground,
  },
  disabledDayText: {
    color: colors.mutedForeground,
  },
  optionGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.sm,
  },
  gridOption: {
    width: '31%',
    minHeight: 46,
    borderRadius: radii.lg,
    backgroundColor: colors.secondary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  gridOptionText: {
    fontFamily: fontFamily.semiBold,
    fontSize: 13,
    color: colors.foreground,
  },
  closeButton: {
    minHeight: 44,
    borderRadius: radii.lg,
    backgroundColor: colors.secondary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  closeButtonText: {
    fontFamily: fontFamily.semiBold,
    fontSize: 14,
    color: colors.primary,
  },
});
