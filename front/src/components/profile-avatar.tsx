import { useEffect, useState } from 'react';
import { Image, StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, shadows, spacing } from '@/theme/tokens';
import { getImageUrl } from '@/utils/imageUrl';

type Props = {
  initials?: string;
  name?: string;
  subtitle?: string;
  imageUrl?: string | null;
  size?: number;
};

export function ProfileAvatar({ initials = 'CP', name, subtitle, imageUrl, size = 92 }: Props) {
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
