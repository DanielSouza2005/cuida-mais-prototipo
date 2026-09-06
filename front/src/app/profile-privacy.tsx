import { router, type Href } from 'expo-router';
import { FileText, HeartHandshake, ListChecks, Trash2 } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { ScreenContainer } from '@/components/screen-container';
import { SettingsRow } from '@/components/settings-row';
import { colors, fontFamily, spacing } from '@/theme/tokens';

export default function ProfilePrivacyScreen() {
  return (
    <ScreenContainer contentStyle={styles.content}>
      <AppHeader showBack title="Privacidade" subtitle="Entenda como seus dados são usados e conheça suas opções." />

      <View style={styles.introduction}>
        <Text style={styles.introductionTitle}>Transparência e controle</Text>
        <Text style={styles.introductionText}>Consulte informações sobre privacidade sem exibir seus dados pessoais nesta área.</Text>
      </View>

      <View style={styles.options}>
        <SettingsRow
          title="Como usamos seus dados"
          description="Entenda quais informações são tratadas e para quais finalidades."
          icon={ListChecks}
          onPress={() => router.push('/profile-privacy-data-usage' as Href)}
        />
        <SettingsRow
          title="Seus direitos"
          description="Conheça os principais direitos relacionados à privacidade."
          icon={HeartHandshake}
          onPress={() => router.push('/profile-privacy-rights' as Href)}
        />
        <SettingsRow
          title="Política de Privacidade"
          description="Leia as informações completas sobre privacidade no Cuidar+."
          icon={FileText}
          onPress={() => router.push('/profile-privacy-policy' as Href)}
        />
        <SettingsRow
          title="Excluir minha conta"
          description="Solicite a exclusão da sua conta e dos dados pessoais elegíveis, conforme as regras de segurança e retenção."
          icon={Trash2}
          variant="danger"
          onPress={() => router.push('/profile-account-deletion' as Href)}
        />
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, paddingBottom: spacing.xxl, gap: spacing.xl },
  introduction: { gap: spacing.sm },
  introductionTitle: { fontFamily: fontFamily.extraBold, fontSize: 18, color: colors.foreground },
  introductionText: { fontFamily: fontFamily.regular, fontSize: 14, lineHeight: 22, color: colors.mutedForeground },
  options: { gap: spacing.md },
});
