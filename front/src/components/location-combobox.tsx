import { MapPin, X } from 'lucide-react-native';
import { useState } from 'react';
import { ActivityIndicator, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { LocationSuggestion } from '@/types/caregiverSearch';

export function LocationCombobox({ value, selectedLocation, suggestions, loading, disabled, onChangeText, onSelectLocation, onClear }: {
  value: string;
  selectedLocation: LocationSuggestion | null;
  suggestions: LocationSuggestion[];
  loading: boolean;
  disabled: boolean;
  onChangeText: (value: string) => void;
  onSelectLocation: (location: LocationSuggestion) => void;
  onClear: () => void;
}) {
  const [focused, setFocused] = useState(false);
  const showSuggestions = focused && value.trim().length > 0;

  return <View style={styles.locationCombobox}>
    <Text style={styles.inputLabel}>Localização</Text>
    <View style={[styles.locationInputShell, showSuggestions && styles.locationInputShellOpen, focused && styles.locationInputFocused, disabled && styles.disabledShell]}>
      <MapPin color={focused ? colors.primary : colors.mutedForeground} size={19} strokeWidth={2.4} />
      <TextInput value={value} onChangeText={onChangeText} onFocus={() => setFocused(true)} editable={!disabled} placeholder="Buscar cidade, bairro ou endereço" placeholderTextColor={colors.mutedForeground} returnKeyType="search" style={styles.locationInput} />
      {value ? <Pressable accessibilityRole="button" accessibilityLabel="Limpar localização" disabled={disabled} onPress={onClear} style={styles.locationClearButton}><X color={colors.mutedForeground} size={16} strokeWidth={2.5} /></Pressable> : null}
    </View>
    {showSuggestions ? <View style={styles.suggestionBox}>
      {loading ? <View style={styles.suggestionLoading}><ActivityIndicator color={colors.primary} size="small" /><Text style={styles.noSuggestionText}>Buscando locais...</Text></View> : suggestions.length ? suggestions.map((suggestion) => {
        const selected = selectedLocation?.id === suggestion.id;
        return <Pressable key={suggestion.id} accessibilityRole="button" accessibilityState={{ selected }} onPress={() => { onSelectLocation(suggestion); setFocused(false); }} style={({ pressed }) => [styles.suggestionItem, selected && styles.suggestionItemSelected, pressed && styles.pressed]}>
          <View style={styles.suggestionIcon}><MapPin color={selected ? colors.primary : colors.mutedForeground} size={14} strokeWidth={2.4} /></View>
          <View style={styles.suggestionCopy}><Text style={styles.suggestionLabel}>{suggestion.label}</Text><Text style={styles.suggestionType}>{suggestion.type === 'CITY' ? 'Cidade' : 'Bairro'}</Text></View>
        </Pressable>;
      }) : <Text style={styles.noSuggestionText}>Nenhum local encontrado</Text>}
    </View> : null}
    <Text style={styles.locationHint}>Busque por cidade, bairro ou endereço.</Text>
  </View>;
}

const styles = StyleSheet.create({
  locationCombobox: { gap: spacing.xs }, inputLabel: { fontFamily: fontFamily.semiBold, fontSize: 13, color: colors.foreground },
  locationInputShell: { minHeight: 54, borderRadius: radii.lg, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, flexDirection: 'row', alignItems: 'center', gap: spacing.md, paddingHorizontal: spacing.lg },
  locationInputShellOpen: { borderBottomLeftRadius: 0, borderBottomRightRadius: 0, borderBottomColor: colors.card }, locationInputFocused: { borderColor: colors.primary, backgroundColor: colors.primaryForeground }, disabledShell: { opacity: .62 },
  locationInput: { flex: 1, minHeight: 50, paddingVertical: 0, fontFamily: fontFamily.medium, fontSize: 14, color: colors.foreground }, locationClearButton: { width: 30, height: 30, borderRadius: radii.full, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.muted },
  locationHint: { fontFamily: fontFamily.regular, fontSize: 11, lineHeight: 16, color: colors.mutedForeground }, suggestionBox: { marginTop: -spacing.xs, borderBottomLeftRadius: radii.xl, borderBottomRightRadius: radii.xl, borderWidth: 1, borderTopWidth: 0, borderColor: colors.border, backgroundColor: colors.card, overflow: 'hidden', ...shadows.card },
  suggestionLoading: { minHeight: 54, flexDirection: 'row', alignItems: 'center', gap: spacing.sm, paddingHorizontal: spacing.md }, suggestionItem: { minHeight: 58, flexDirection: 'row', alignItems: 'center', gap: spacing.md, paddingHorizontal: spacing.md, paddingVertical: spacing.sm, borderBottomWidth: 1, borderBottomColor: colors.border }, suggestionItemSelected: { backgroundColor: colors.secondary }, suggestionIcon: { width: 32, height: 32, borderRadius: radii.full, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.secondary }, suggestionCopy: { flex: 1, minWidth: 0 }, suggestionLabel: { fontFamily: fontFamily.bold, fontSize: 13, color: colors.foreground }, suggestionType: { marginTop: 2, fontFamily: fontFamily.medium, fontSize: 11, color: colors.mutedForeground }, noSuggestionText: { padding: spacing.md, fontFamily: fontFamily.medium, fontSize: 13, color: colors.mutedForeground }, pressed: { opacity: .8 },
});
