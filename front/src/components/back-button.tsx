import { router } from 'expo-router';
import { ArrowLeft } from 'lucide-react-native';
import { Pressable, StyleSheet } from 'react-native';

import { colors, radii } from '@/theme/tokens';
import { guardPress } from '@/utils/interaction';

type Props = {
  onPress?: () => void;
  accessibilityLabel?: string;
  disabled?: boolean;
};

export function BackButton({ onPress, accessibilityLabel = 'Voltar', disabled = false }: Props) {
  const handlePress = onPress ?? (() => router.back());

  return (
    <Pressable
      accessibilityLabel={accessibilityLabel}
      accessibilityState={{ disabled }}
      disabled={disabled}
      onPress={guardPress(disabled, handlePress)}
      style={({ pressed }) => [styles.button, disabled && styles.disabled, pressed && !disabled && styles.pressed]}
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
  disabled: {
    opacity: 0.45,
  },
});
