import { router } from 'expo-router';
import { ArrowLeft } from 'lucide-react-native';
import { Pressable, StyleSheet } from 'react-native';

import { colors, radii } from '@/theme/tokens';

type Props = {
  onPress?: () => void;
  accessibilityLabel?: string;
};

export function BackButton({ onPress, accessibilityLabel = 'Voltar' }: Props) {
  return (
    <Pressable
      accessibilityLabel={accessibilityLabel}
      onPress={onPress ?? (() => router.back())}
      style={({ pressed }) => [styles.button, pressed && styles.pressed]}
    >
      <ArrowLeft color={colors.foreground} size={18} strokeWidth={2.4} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  button: {
    width: 44,
    height: 44,
    borderRadius: radii.lg,
    backgroundColor: colors.card,
    borderWidth: 1,
    borderColor: colors.border,
    alignItems: 'center',
    justifyContent: 'center',
  },
  pressed: {
    opacity: 0.72,
    transform: [{ scale: 0.98 }],
  },
});
