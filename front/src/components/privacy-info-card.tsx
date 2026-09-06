import type { LucideIcon } from 'lucide-react-native';
import type { PropsWithChildren } from 'react';
import { StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

type Props = PropsWithChildren<{
  icon: LucideIcon;
  title: string;
  description?: string;
}>;

export function PrivacyInfoCard({ icon: Icon, title, description, children }: Props) {
  return (
    <View style={styles.card}>
      <View style={styles.heading}>
        <View style={styles.iconBox}><Icon color={colors.primary} size={20} /></View>
        <View style={styles.headingCopy}>
          <Text style={styles.title}>{title}</Text>
          {description ? <Text style={styles.description}>{description}</Text> : null}
        </View>
      </View>
      {children}
    </View>
  );
}

export const privacyInfoStyles = StyleSheet.create({
  paragraph: { fontFamily: fontFamily.regular, fontSize: 14, lineHeight: 22, color: colors.mutedForeground },
  label: { fontFamily: fontFamily.bold, fontSize: 13, color: colors.foreground },
  list: { gap: spacing.sm },
  itemRow: { flexDirection: 'row', alignItems: 'flex-start', gap: spacing.sm },
  bullet: { marginTop: 1, fontFamily: fontFamily.bold, fontSize: 14, lineHeight: 22, color: colors.primary },
  item: { flex: 1, fontFamily: fontFamily.regular, fontSize: 14, lineHeight: 22, color: colors.mutedForeground },
  note: { padding: spacing.lg, borderRadius: radii.lg, backgroundColor: colors.sunnyBackground, fontFamily: fontFamily.medium, fontSize: 13, lineHeight: 21, color: colors.foreground },
});

const styles = StyleSheet.create({
  card: { gap: spacing.lg, padding: spacing.xl, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card },
  heading: { flexDirection: 'row', alignItems: 'center', gap: spacing.md },
  iconBox: { width: 42, height: 42, alignItems: 'center', justifyContent: 'center', borderRadius: radii.md, backgroundColor: colors.secondary },
  headingCopy: { flex: 1, gap: spacing.xxs },
  title: { fontFamily: fontFamily.bold, fontSize: 16, lineHeight: 22, color: colors.foreground },
  description: { fontFamily: fontFamily.regular, fontSize: 12, lineHeight: 18, color: colors.mutedForeground },
});
