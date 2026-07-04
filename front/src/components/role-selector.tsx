import { Pressable, StyleSheet, Text, View } from 'react-native';
import type { LucideIcon } from 'lucide-react-native';
import { HeartHandshake, Users } from 'lucide-react-native';

import { colors, fontFamily, radii, spacing } from '@/theme/tokens';

export type Role = 'family' | 'caregiver';

type Props = {
  value: Role;
  onChange: (role: Role) => void;
};

const roles: { value: Role; title: string; description: string; icon: LucideIcon }[] = [
  {
    value: 'family',
    title: 'Sou responsável',
    description: 'Gerenciar o cuidado de uma pessoa assistida.',
    icon: Users,
  },
  {
    value: 'caregiver',
    title: 'Sou cuidador',
    description: 'Prestar serviços de cuidado profissional.',
    icon: HeartHandshake,
  },
];

export function RoleSelector({ value, onChange }: Props) {
  return (
    <View style={styles.wrapper}>
      <Text style={styles.label}>Tipo de conta</Text>
      <View style={styles.options}>
        {roles.map(({ value: roleValue, title, description, icon: Icon }) => {
          const active = roleValue === value;

          return (
            <Pressable
              key={roleValue}
              accessibilityRole="radio"
              accessibilityState={{ selected: active }}
              onPress={() => onChange(roleValue)}
              style={({ pressed }) => [
                styles.option,
                active && styles.activeOption,
                pressed && styles.pressed,
              ]}
            >
              <View style={[styles.iconBox, active && styles.activeIconBox]}>
                <Icon color={active ? colors.primaryForeground : colors.primary} size={20} />
              </View>
              <Text style={styles.title}>{title}</Text>
              <Text style={styles.description}>{description}</Text>
            </Pressable>
          );
        })}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    gap: spacing.sm,
  },
  label: {
    fontFamily: fontFamily.semiBold,
    fontSize: 13,
    color: colors.foreground,
  },
  options: {
    flexDirection: 'row',
    gap: spacing.md,
  },
  option: {
    flex: 1,
    minHeight: 132,
    borderRadius: radii.xl,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    padding: spacing.lg,
    gap: spacing.sm,
  },
  activeOption: {
    borderColor: colors.primary,
    backgroundColor: colors.secondary,
  },
  pressed: {
    opacity: 0.82,
  },
  iconBox: {
    width: 38,
    height: 38,
    borderRadius: radii.md,
    backgroundColor: colors.secondary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  activeIconBox: {
    backgroundColor: colors.primary,
  },
  title: {
    fontFamily: fontFamily.bold,
    fontSize: 13,
    color: colors.foreground,
  },
  description: {
    fontFamily: fontFamily.regular,
    fontSize: 11,
    lineHeight: 17,
    color: colors.mutedForeground,
  },
});
