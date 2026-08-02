import { router, type Href } from 'expo-router';
import { HeartPulse, Home, MapPin, User, Users } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { ScreenContainer } from '@/components/screen-container';
import { SettingsRow } from '@/components/settings-row';
import { useAuth } from '@/hooks/useAuth';
import { colors, fontFamily, spacing } from '@/theme/tokens';

type RegistrationOption = {
  title: string;
  description: string;
  icon: typeof User;
  route: Href;
};

const responsibleOptions: RegistrationOption[] = [
  { title: 'Informações pessoais', description: 'Atualize nome, telefone e dados básicos.', icon: User, route: '/profile-personal-info' },
  { title: 'Pessoa assistida', description: 'Gerencie os dados da pessoa que receberá os cuidados.', icon: Users, route: '/profile-assisted-person' },
  { title: 'Endereço do cuidado', description: 'Atualize o local onde o cuidado será realizado.', icon: MapPin, route: '/profile-care-address' },
  { title: 'Contato de emergência', description: 'Configure quem deve ser acionado em caso de necessidade.', icon: HeartPulse, route: '/profile-emergency-contact' },
];

const caregiverOptions: RegistrationOption[] = [
  { title: 'Informações pessoais', description: 'Atualize nome, telefone e dados básicos.', icon: User, route: '/profile-personal-info' },
  { title: 'Experiência', description: 'Atualize sua formação, experiência e apresentação profissional.', icon: HeartPulse, route: '/profile-caregiver-experience' },
  { title: 'Disponibilidade', description: 'Informe seus dias, horários e modalidades de atendimento.', icon: MapPin, route: '/profile-caregiver-availability' },
  { title: 'Serviços oferecidos', description: 'Gerencie os cuidados e serviços que você oferece.', icon: Users, route: '/profile-caregiver-services' },
  { title: 'Endereço', description: 'Atualize sua localização e seus dados de endereço.', icon: Home, route: '/profile-caregiver-address' },
];

export default function ProfileRegistrationSettingsScreen() {
  const { user } = useAuth();
  const options = user?.userType === 'caregiver' ? caregiverOptions : responsibleOptions;

  return (
    <ScreenContainer contentStyle={styles.content}>
      <AppHeader showBack title="Dados cadastrais" subtitle="Escolha o que deseja consultar ou editar." />
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Informações vinculadas ao perfil</Text>
        {options.map((option) => (
          <SettingsRow
            key={option.title}
            title={option.title}
            description={option.description}
            icon={option.icon}
            onPress={() => router.push(option.route)}
          />
        ))}
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.xl,
    gap: spacing.xl,
  },
  section: {
    gap: spacing.md,
  },
  sectionTitle: {
    fontFamily: fontFamily.extraBold,
    fontSize: 18,
    color: colors.foreground,
  },
});
