import { StyleSheet, Text, View } from 'react-native';

import { BackButton } from '@/components/back-button';
import { Brand } from '@/components/brand';
import { colors, fontFamily, spacing } from '@/theme/tokens';

type Props = {
  backDisabled?: boolean;
  title?: string;
  subtitle?: string;
  showBack?: boolean;
};

export function AppHeader({ backDisabled = false, title, subtitle, showBack = false }: Props) {
  return (
    <View style={styles.wrapper}>
      <View style={styles.topRow}>
        {showBack ? <BackButton disabled={backDisabled} /> : <Brand />}
        {showBack ? <Brand /> : null}
      </View>
      {title ? (
        <View style={styles.copy}>
          <Text style={styles.title}>{title}</Text>
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
  subtitle: {
    fontFamily: fontFamily.regular,
    fontSize: 14,
    lineHeight: 22,
    color: colors.mutedForeground,
  },
});
