import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

type Props = {
  message?: string;
};

export function LoadingState({ message = 'Carregando dados...' }: Props) {
  return (
    <View style={styles.card}>
      <ActivityIndicator color={colors.primary} size="small" />
      <Text style={styles.text}>{message}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    minHeight: 120,
    borderRadius: radii.xl,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    alignItems: 'center',
    justifyContent: 'center',
    gap: spacing.md,
    padding: spacing.xl,
    ...shadows.card,
  },
  text: {
    fontFamily: fontFamily.medium,
    fontSize: 13,
    color: colors.mutedForeground,
  },
});
