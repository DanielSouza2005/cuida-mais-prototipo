import { StyleSheet, Text, View } from 'react-native';

import { BackButton } from '@/components/back-button';
import { BrandMark } from '@/components/brand';
import { colors, fontFamily, spacing } from '@/theme/tokens';

type Props = {
  backDisabled?: boolean;
  title?: string;
  subtitle?: string;
  showBack?: boolean;
  onBackPress?: () => void;
  compact?: boolean;
};

export function AppHeader({ backDisabled = false, title, subtitle, showBack = false, onBackPress, compact = false }: Props) {
  return (
    <View style={[styles.wrapper, compact && styles.compactWrapper]}>
      <View style={styles.topRow}>
        {showBack ? <BackButton disabled={backDisabled} onPress={onBackPress} /> : <BrandMark />}
        {showBack ? <BrandMark /> : null}
      </View>
      {title ? (
        <View style={styles.copy}>
          <Text style={[styles.title, compact && styles.compactTitle]}>{title}</Text>
          {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    gap: spacing.xxl,
  },
  compactWrapper: {
    gap: spacing.lg,
  },
  topRow: {
    minHeight: 44,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  copy: {
    gap: spacing.sm,
  },
  title: {
    fontFamily: fontFamily.extraBold,
    fontSize: 30,
    lineHeight: 37,
    letterSpacing: -0.9,
    color: colors.foreground,
  },
  compactTitle: {
    fontSize: 26,
    lineHeight: 32,
    letterSpacing: -0.5,
  },
  subtitle: {
    fontFamily: fontFamily.regular,
    fontSize: 14,
    lineHeight: 22,
    color: colors.mutedForeground,
  },
});
