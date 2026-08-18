import { createElement, useMemo, useState } from 'react';
import DateTimePicker, { type DateTimePickerEvent } from '@react-native-community/datetimepicker';
import { Clock } from 'lucide-react-native';
import { Modal, Platform, Pressable, StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

type Props = { label: string; value: string; onChange: (value: string) => void; required?: boolean; optional?: boolean; error?: string; disabled?: boolean; placeholder?: string };
const parseTime = (value: string) => { const [hour, minute] = value.split(':').map(Number); const date = new Date(); date.setHours(Number.isFinite(hour) ? hour : 8, Number.isFinite(minute) ? minute : 0, 0, 0); return date; };
const formatTime = (date: Date) => `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;

export function TimePickerField({ label, value, onChange, required, optional, error, disabled, placeholder = 'Selecionar horário' }: Props) {
  const initial = useMemo(() => parseTime(value), [value]);
  const displayValue = value ? formatTime(initial) : '';
  const [visible, setVisible] = useState(false);
  const [temporary, setTemporary] = useState(initial);

  function open() { if (!disabled) { setTemporary(parseTime(value)); setVisible(true); } }
  function handleNativeChange(event: DateTimePickerEvent, date?: Date) {
    if (Platform.OS === 'android') setVisible(false);
    if (event.type === 'set' && date) { setTemporary(date); if (Platform.OS === 'android') onChange(formatTime(date)); }
  }
  function handleWebChange(nextValue: string) { setTemporary(parseTime(nextValue)); }

  return <View style={styles.wrapper}>
    <Text style={styles.label}>{label}{optional ? ' (opcional)' : null}{required ? <Text style={styles.required}> *</Text> : null}</Text>
    <Pressable accessibilityLabel={`${label}. ${displayValue || placeholder}`} accessibilityRole="button" accessibilityState={{ disabled: Boolean(disabled) }} disabled={disabled} onPress={open} style={({ pressed }) => [styles.field, error && styles.fieldError, disabled && styles.disabled, pressed && !disabled && styles.pressed]}>
      <Clock color={colors.mutedForeground} size={19} /><Text style={[styles.value, !displayValue && styles.placeholder]}>{displayValue || placeholder}</Text>
    </Pressable>
    {error ? <Text style={styles.error}>{error}</Text> : null}
    {visible && Platform.OS === 'android' ? <DateTimePicker value={temporary} mode="time" is24Hour display="default" onChange={handleNativeChange} /> : null}
    <Modal transparent animationType="fade" visible={visible && Platform.OS !== 'android'} onRequestClose={() => setVisible(false)}>
      <View style={styles.backdrop}><View style={styles.modalCard}><Text style={styles.modalTitle}>Selecionar horário</Text>
        {Platform.OS === 'ios' ? <DateTimePicker value={temporary} mode="time" is24Hour display="spinner" onChange={handleNativeChange} /> : createElement('input', { 'aria-label': label, autoFocus: true, onChange: (event: { target: { value: string } }) => handleWebChange(event.target.value), style: webInputStyle, type: 'time', value: formatTime(temporary) })}
        <View style={styles.actions}><Pressable onPress={() => setVisible(false)} style={styles.action}><Text style={styles.cancel}>Cancelar</Text></Pressable><Pressable onPress={() => { onChange(formatTime(temporary)); setVisible(false); }} style={[styles.action, styles.confirm]}><Text style={styles.confirmText}>Confirmar</Text></Pressable></View>
      </View></View>
    </Modal>
  </View>;
}

const styles = StyleSheet.create({
  wrapper: { gap: spacing.xs }, label: { fontFamily: fontFamily.semiBold, fontSize: 13, color: colors.foreground }, required: { color: colors.destructive }, field: { minHeight: 54, flexDirection: 'row', alignItems: 'center', gap: spacing.md, paddingHorizontal: spacing.lg, borderRadius: radii.lg, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card }, fieldError: { borderColor: colors.destructive }, disabled: { opacity: 0.5 }, pressed: { opacity: 0.82 }, value: { flex: 1, fontFamily: fontFamily.medium, fontSize: 14, color: colors.foreground }, placeholder: { color: colors.mutedForeground }, error: { fontFamily: fontFamily.medium, fontSize: 12, color: colors.destructive },
  backdrop: { flex: 1, justifyContent: 'center', padding: spacing.xl, backgroundColor: 'rgba(27,45,64,0.34)' }, modalCard: { gap: spacing.lg, padding: spacing.lg, borderRadius: radii.xl, backgroundColor: colors.card, ...shadows.soft }, modalTitle: { fontFamily: fontFamily.bold, fontSize: 17, color: colors.foreground, textAlign: 'center' }, actions: { flexDirection: 'row', gap: spacing.sm }, action: { flex: 1, minHeight: 46, alignItems: 'center', justifyContent: 'center', borderRadius: radii.lg, backgroundColor: colors.secondary }, confirm: { backgroundColor: colors.primary }, cancel: { fontFamily: fontFamily.semiBold, color: colors.primary }, confirmText: { fontFamily: fontFamily.semiBold, color: colors.primaryForeground },
});

const webInputStyle = {
  minHeight: 52,
  border: `1px solid ${colors.border}`,
  borderRadius: radii.lg,
  padding: `0 ${spacing.lg}px`,
  backgroundColor: colors.card,
  color: colors.foreground,
  fontSize: 16,
};
