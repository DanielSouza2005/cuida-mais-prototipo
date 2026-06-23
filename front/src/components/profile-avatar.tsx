import { StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

type Props = {
  initials?: string;
  name?: string;
  subtitle?: string;
};

export function ProfileAvatar({ initials = 'CP', name, subtitle }: Props) {
  return (
    <View style={styles.wrapper}>
      <View style={styles.avatar}>
        <Text style={styles.initials}>{initials}</Text>
      </View>
      {name ? (
        <View style={styles.copy}>
          <Text style={styles.name}>{name}</Text>
          {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    alignItems: 'center',
    gap: spacing.md,
  },
  avatar: {
    width: 92,
    height: 92,
    borderRadius: radii.xxxl,
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 5,
    borderColor: colors.card,
    ...shadows.soft,
  },
  initials: {
    fontFamily: fontFamily.extraBold,
    fontSize: 28,
    color: colors.primaryForeground,
  },
  copy: {
    alignItems: 'center',
    gap: spacing.xs,
  },
  name: {
    fontFamily: fontFamily.extraBold,
    fontSize: 22,
    color: colors.foreground,
  },
  subtitle: {
    fontFamily: fontFamily.medium,
    fontSize: 13,
    color: colors.mutedForeground,
  },
});
