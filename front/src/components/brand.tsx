import { StyleSheet, Text, View } from 'react-native';
import { colors, fontFamily, radii, shadows } from '@/theme/tokens';

export function Brand({ size = 'small', centered = false }: { size?: 'small' | 'large'; centered?: boolean }) {
  const large = size === 'large';
  return (
    <View style={[styles.row, centered && styles.centered]}>
      {!large && <BrandMark />}
      <Text style={[styles.name, large && styles.largeName]}>Cuidar<Text style={styles.plus}>+</Text></Text>
    </View>
  );
}

export function BrandMark() {
  return (
    <View style={styles.mark}>
      <Text style={styles.markText}>C+</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', alignItems: 'center', gap: 8 }, centered: { justifyContent: 'center' },
  mark: { width: 40, height: 40, borderRadius: radii.lg, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.primary, ...shadows.glow },
  markText: { fontFamily: fontFamily.bold, fontSize: 13, color: colors.primaryForeground },
  name: { fontFamily: fontFamily.extraBold, fontSize: 20, color: colors.foreground, letterSpacing: -0.5 },
  largeName: { fontSize: 27 }, plus: { color: colors.primary },
});
