import { StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, radii, spacing } from '@/theme/tokens';

type ProfileType = 'CUIDADOR' | 'RESPONSAVEL' | 'ADMIN';

type Props = {
  type: ProfileType;
  label?: string;
};

const labels: Record<ProfileType, string> = {
  CUIDADOR: 'Perfil de cuidador',
  RESPONSAVEL: 'Perfil de responsável',
  ADMIN: 'Perfil de administrador',
};

export function ProfileTypeBadge({ type, label }: Props) {
  const isCaregiver = type === 'CUIDADOR';

  return (
    <View style={[styles.badge, isCaregiver ? styles.caregiverBadge : styles.responsibleBadge]}>
      <Text style={[styles.text, isCaregiver ? styles.caregiverText : styles.responsibleText]}>
        {label ?? labels[type]}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    alignSelf: 'center',
    borderRadius: radii.full,
    borderWidth: 1,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.xs,
  },
  caregiverBadge: {
    backgroundColor: colors.secondary,
    borderColor: colors.primary,
  },
  responsibleBadge: {
    backgroundColor: colors.mint,
    borderColor: colors.sage,
  },
  text: {
    fontFamily: fontFamily.semiBold,
    fontSize: 12,
    lineHeight: 16,
  },
  caregiverText: {
    color: colors.primary,
  },
  responsibleText: {
    color: colors.mintForeground,
  },
});
