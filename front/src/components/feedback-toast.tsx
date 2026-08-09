import { CircleCheck, TriangleAlert } from 'lucide-react-native';
import { useEffect, useRef } from 'react';
import { Animated, StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

type Props = {
  description: string;
  onDismiss: () => void;
  title: string;
  variant: 'success' | 'error';
};

const TOAST_DURATION_MS = 4_500;

export function FeedbackToast({ description, onDismiss, title, variant }: Props) {
  const opacity = useRef(new Animated.Value(0)).current;
  const translateY = useRef(new Animated.Value(8)).current;
  const success = variant === 'success';
  const Icon = success ? CircleCheck : TriangleAlert;

  useEffect(() => {
    Animated.parallel([
      Animated.timing(opacity, { duration: 180, toValue: 1, useNativeDriver: true }),
      Animated.timing(translateY, { duration: 180, toValue: 0, useNativeDriver: true }),
    ]).start();

    const dismissTimeout = setTimeout(onDismiss, TOAST_DURATION_MS);
    return () => clearTimeout(dismissTimeout);
  }, [onDismiss, opacity, translateY]);

  return (
    <Animated.View
      accessibilityLiveRegion="polite"
      accessibilityRole="alert"
      style={[
        styles.toast,
        success ? styles.successToast : styles.errorToast,
        { opacity, transform: [{ translateY }] },
      ]}
    >
      <View style={[styles.iconContainer, success ? styles.successIcon : styles.errorIcon]}>
        <Icon color={success ? colors.mintForeground : colors.destructive} size={20} strokeWidth={2.5} />
      </View>
      <View style={styles.copy}>
        <Text style={styles.title}>{title}</Text>
        <Text style={styles.description}>{description}</Text>
      </View>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  toast: {
    minHeight: 76,
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: spacing.md,
    borderWidth: 1,
    borderRadius: radii.lg,
    padding: spacing.md,
    ...shadows.soft,
  },
  successToast: {
    backgroundColor: '#F2FBF7',
    borderColor: '#BFE5D4',
  },
  errorToast: {
    backgroundColor: '#FFF7F5',
    borderColor: '#F1C8C3',
  },
  iconContainer: {
    width: 36,
    height: 36,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: radii.full,
  },
  successIcon: {
    backgroundColor: colors.mint,
  },
  errorIcon: {
    backgroundColor: '#FBE4E1',
  },
  copy: {
    flex: 1,
    gap: spacing.xxs,
  },
  title: {
    color: colors.foreground,
    fontFamily: fontFamily.bold,
    fontSize: 14,
    lineHeight: 20,
  },
  description: {
    color: colors.mutedForeground,
    fontFamily: fontFamily.regular,
    fontSize: 12,
    lineHeight: 18,
  },
});
