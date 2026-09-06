import { router, type Href } from 'expo-router';
import { CheckCircle2, Trash2 } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { PrimaryButton } from '@/components/primary-button';
import { PrivacyInfoCard, privacyInfoStyles } from '@/components/privacy-info-card';
import { ScreenContainer } from '@/components/screen-container';
import { spacing } from '@/theme/tokens';

const rights = [
  'Confirmar se tratamos seus dados',
  'Acessar informações sobre seus dados',
  'Corrigir dados incompletos ou desatualizados',
  'Solicitar exclusão de dados elegíveis',
  'Saber com quem os dados podem ser compartilhados',
  'Revogar consentimentos, quando aplicável',
];

export default function ProfilePrivacyRightsScreen() {
  return (
    <ScreenContainer contentStyle={styles.content}>
      <AppHeader showBack title="Seus direitos" subtitle="Conheça suas opções relacionadas à privacidade." />
      <PrivacyInfoCard icon={CheckCircle2} title="Você pode solicitar">
        <Text style={privacyInfoStyles.paragraph}>
          Informações sobre o tratamento dos seus dados pessoais e o atendimento dos direitos aplicáveis à sua situação.
        </Text>
        <View style={privacyInfoStyles.list}>
          {rights.map((right) => (
            <View key={right} style={privacyInfoStyles.itemRow}>
              <Text style={privacyInfoStyles.bullet}>•</Text>
              <Text style={privacyInfoStyles.item}>{right}</Text>
            </View>
          ))}
        </View>
        <Text style={privacyInfoStyles.note}>
          Algumas solicitações podem depender de validação de identidade e análise de obrigações aplicáveis, segurança, prevenção de fraudes ou exercício de direitos. Por isso, certos registros podem precisar ser mantidos.
        </Text>
      </PrivacyInfoCard>
      <PrimaryButton icon={Trash2} label="Excluir minha conta" variant="danger" onPress={() => router.push('/profile-account-deletion' as Href)} />
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, paddingBottom: spacing.xxl, gap: spacing.xl },
});
