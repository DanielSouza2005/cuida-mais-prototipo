import { useEffect, useState } from 'react';
import { Pencil } from 'lucide-react-native';
import { ActivityIndicator, Image, Pressable, StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, shadows, spacing } from '@/theme/tokens';
import { getImageUrl } from '@/utils/imageUrl';

type Props = {
  initials?: string;
  name?: string;
  subtitle?: string;
  imageUrl?: string | null;
  size?: number;
  editable?: boolean;
  editDisabled?: boolean;
  editLoading?: boolean;
  onEditPress?: () => void;
};

export function ProfileAvatar({
  initials = 'CP',
  name,
  subtitle,
  imageUrl,
  size = 92,
  editable = false,
  editDisabled = false,
  editLoading = false,
  onEditPress,
}: Props) {
  const resolvedUrl = getImageUrl(imageUrl);
  const [failed, setFailed] = useState(false);
  useEffect(() => setFailed(false), [resolvedUrl]);
  return (
    <View style={styles.wrapper}>
      <View style={[styles.avatarFrame, { width: size, height: size, borderRadius: size / 2 }]}>
        <View style={[styles.avatar, { borderRadius: size / 2 }]}>
          {resolvedUrl && !failed ? (
            <Image
              source={{ uri: resolvedUrl }}
              style={[styles.image, { borderRadius: size / 2 }]}
              resizeMode="cover"
              onError={() => setFailed(true)}
              accessibilityLabel="Foto de perfil"
            />
          ) : <Text style={[styles.initials, { fontSize: size * 0.3 }]}>{initials}</Text>}
        </View>
        {editable && onEditPress ? (
          <Pressable
            accessibilityLabel="Editar foto do perfil"
            accessibilityRole="button"
            accessibilityState={{ busy: editLoading, disabled: editDisabled }}
            disabled={editDisabled}
            hitSlop={8}
            onPress={onEditPress}
            style={({ pressed }) => [styles.editButton, pressed && !editDisabled && styles.editButtonPressed, editDisabled && styles.editButtonDisabled]}
          >
            {editLoading
              ? <ActivityIndicator color={colors.primaryForeground} size="small" />
              : <Pencil color={colors.primaryForeground} size={17} strokeWidth={2.5} />}
          </Pressable>
        ) : null}
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
  avatarFrame: {
    position: 'relative',
    ...shadows.soft,
  },
  avatar: {
    width: '100%',
    height: '100%',
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 5,
    borderColor: colors.card,
    overflow: 'hidden',
  },
  initials: {
    fontFamily: fontFamily.extraBold,
    fontSize: 28,
    color: colors.primaryForeground,
  },
  image: {
    width: '100%',
    height: '100%',
  },
  editButton: {
    position: 'absolute',
    right: -2,
    bottom: -2,
    width: 36,
    height: 36,
    borderRadius: 18,
    borderWidth: 3,
    borderColor: colors.card,
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    ...shadows.soft,
  },
  editButtonPressed: {
    opacity: 0.82,
    transform: [{ scale: 0.96 }],
  },
  editButtonDisabled: {
    opacity: 0.72,
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
