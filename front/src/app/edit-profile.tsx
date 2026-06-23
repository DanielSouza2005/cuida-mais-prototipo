import { MapPin, Phone, Save, User } from 'lucide-react-native';
import { StyleSheet, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { PrimaryButton } from '@/components/primary-button';
import { ProfileAvatar } from '@/components/profile-avatar';
import { ScreenContainer } from '@/components/screen-container';
import { colors, radii, shadows, spacing } from '@/theme/tokens';

export default function EditProfileScreen() {
  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <AppHeader
        showBack
        title="Editar perfil"
        subtitle="Base visual para dados pessoais, preferências e avatar."
      />

      <View style={styles.card}>
        <ProfileAvatar initials="CP" />
        <AppTextInput label="Nome" icon={User} placeholder="Cuidador Plus" />
        <AppTextInput label="Telefone" icon={Phone} placeholder="(00) 00000-0000" keyboardType="phone-pad" />
        <AppTextInput label="Cidade" icon={MapPin} placeholder="Sua cidade" />
        <PrimaryButton label="Salvar alterações" icon={Save} disabled />
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.xxl,
    gap: spacing.xl,
  },
  card: {
    gap: spacing.lg,
    padding: spacing.xl,
    borderRadius: radii.xxl,
    backgroundColor: colors.card,
    borderWidth: 1,
    borderColor: colors.border,
    ...shadows.card,
  },
});
