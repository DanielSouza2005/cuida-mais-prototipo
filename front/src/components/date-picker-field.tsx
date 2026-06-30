import { useMemo, useState } from 'react';
import { Calendar } from 'lucide-react-native';
import { Modal, Pressable, StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

type Props = {
  label: string;
  maxDate?: Date;
  optional?: boolean;
  required?: boolean;
  value: string;
  onChange: (value: string) => void;
};

const monthNames = [
  'Janeiro',
  'Fevereiro',
  'Marco',
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

export function DatePickerField({ label, maxDate, optional, required, value, onChange }: Props) {
  const selectedDate = parseDate(value);
  const today = useMemo(() => startOfDay(new Date()), []);
  const normalizedMaxDate = maxDate ? startOfDay(maxDate) : undefined;
  const initialDate = selectedDate ?? normalizedMaxDate ?? today;
  const [visible, setVisible] = useState(false);
  const [visibleMonth, setVisibleMonth] = useState(new Date(initialDate.getFullYear(), initialDate.getMonth(), 1));

  const days = useMemo(() => {
    const year = visibleMonth.getFullYear();
    const month = visibleMonth.getMonth();
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const blanks = Array.from({ length: firstDay }, () => null);
    const monthDays = Array.from({ length: daysInMonth }, (_, index) => new Date(year, month, index + 1));

    return [...blanks, ...monthDays];
  }, [visibleMonth]);

  function changeMonth(delta: number) {
    setVisibleMonth((current) => new Date(current.getFullYear(), current.getMonth() + delta, 1));
  }

  function handleSelect(date: Date) {
    if (normalizedMaxDate && date > normalizedMaxDate) return;

    onChange(formatDate(date));
    setVisible(false);
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
        onPress={() => setVisible(true)}
        style={({ pressed }) => [styles.inputShell, pressed && styles.pressed]}
      >
        <Calendar color={colors.mutedForeground} size={19} />
        <Text style={[styles.valueText, !value && styles.placeholderText]}>
          {value || 'Selecionar data'}
        </Text>
      </Pressable>

      <Modal animationType="fade" transparent visible={visible} onRequestClose={() => setVisible(false)}>
        <View style={styles.modalBackdrop}>
          <View style={styles.modalCard}>
            <View style={styles.calendarHeader}>
              <Pressable accessibilityRole="button" onPress={() => changeMonth(-1)} style={styles.monthButton}>
                <Text style={styles.monthButtonText}>Anterior</Text>
              </Pressable>
              <Text style={styles.monthTitle}>
                {monthNames[visibleMonth.getMonth()]} {visibleMonth.getFullYear()}
              </Text>
              <Pressable accessibilityRole="button" onPress={() => changeMonth(1)} style={styles.monthButton}>
                <Text style={styles.monthButtonText}>Proximo</Text>
              </Pressable>
            </View>

            <View style={styles.weekRow}>
              {weekDays.map((day, index) => (
                <Text key={`${day}-${index}`} style={styles.weekDay}>{day}</Text>
              ))}
            </View>

            <View style={styles.daysGrid}>
              {days.map((date, index) => {
                const selected = date && selectedDate && formatDate(date) === formatDate(selectedDate);
                const disabled = Boolean(date && normalizedMaxDate && date > normalizedMaxDate);

                return (
                  <Pressable
                    accessibilityRole="button"
                    disabled={!date || disabled}
                    key={date ? formatDate(date) : `blank-${index}`}
                    onPress={() => date && handleSelect(date)}
                    style={[
                      styles.dayCell,
                      selected && styles.selectedDay,
                      disabled && styles.disabledDay,
                    ]}
                  >
                    <Text style={[
                      styles.dayText,
                      selected && styles.selectedDayText,
                      disabled && styles.disabledDayText,
                    ]}>
                      {date ? date.getDate() : ''}
                    </Text>
                  </Pressable>
                );
              })}
            </View>

            <PrimaryActions onClose={() => setVisible(false)} />
          </View>
        </View>
      </Modal>
    </View>
  );
}

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
  monthTitle: {
    flex: 1,
    textAlign: 'center',
    fontFamily: fontFamily.bold,
    fontSize: 15,
    color: colors.foreground,
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
