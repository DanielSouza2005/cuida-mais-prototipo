import { HeartHandshake } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

export function WellnessTipCard({ tip }: { tip: string }) {
  return (
    <View style={styles.card}>
      <View style={styles.iconBox}>
        <HeartHandshake color={colors.mintForeground} size={22} strokeWidth={2.3} />
      </View>
      <View style={styles.copy}>
        <Text style={styles.label}>Dica de hoje</Text>
        <Text style={styles.tip}>{tip}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { flexDirection: 'row', gap: spacing.md, padding: spacing.lg, borderRadius: radii.xxl, borderWidth: 1, borderColor: '#CBE8DA', backgroundColor: colors.mint, ...shadows.card },
  iconBox: { width: 44, height: 44, alignItems: 'center', justifyContent: 'center', borderRadius: radii.full, backgroundColor: colors.card },
  copy: { flex: 1, gap: spacing.xs },
  label: { fontFamily: fontFamily.bold, fontSize: 11, letterSpacing: 0.4, textTransform: 'uppercase', color: colors.mintForeground },
  tip: { fontFamily: fontFamily.semiBold, fontSize: 13.5, lineHeight: 21, color: colors.mintForeground },
});
