import { Handshake, HeartPulse, MapPin, ShieldCheck, UserRound } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { PrivacyInfoCard, privacyInfoStyles } from '@/components/privacy-info-card';
import { ScreenContainer } from '@/components/screen-container';
import { spacing } from '@/theme/tokens';

const categories = [
  {
    title: 'Informações de cadastro',
    icon: UserRound,
    data: ['Nome', 'E-mail', 'Senha', 'Confirmação de faixa etária', 'Telefone, quando informado', 'Foto de perfil, quando informada'],
    purpose: 'Usamos essas informações para criar e proteger sua conta, identificar seu perfil na plataforma e permitir o uso das funcionalidades do Cuidar+.',
    note: 'Em vez de coletar sua data completa de nascimento, solicitamos apenas a confirmação de que você está na faixa etária permitida para usar a plataforma. O telefone é opcional e pode ser utilizado somente quando necessário para contato relacionado ao serviço.',
  },
  {
    title: 'Localização',
    icon: MapPin,
    data: ['Cidade', 'Bairro', 'Endereço', 'Coordenadas, quando utilizadas'],
    purpose: 'Usamos informações de localização para permitir a busca de cuidadores próximos, calcular distâncias e facilitar a organização dos serviços de cuidado.',
  },
  {
    title: 'Informações assistenciais',
    icon: HeartPulse,
    data: ['Mobilidade', 'Alergias', 'Restrições alimentares', 'Rotinas de cuidado', 'Medicações', 'Observações sobre o cuidado'],
    purpose: 'Usamos essas informações para organizar o cuidado domiciliar e fornecer ao cuidador envolvido os dados necessários para realizar o atendimento com mais segurança.',
    note: 'Essas informações podem envolver dados pessoais sensíveis relacionados à saúde e recebem cuidado adicional.',
  },
  {
    title: 'Dados de contratação',
    icon: Handshake,
    data: ['Cuidador', 'Responsável', 'Pessoa assistida', 'Datas e horários', 'Situação da solicitação ou contratação', 'Histórico de serviços'],
    purpose: 'Usamos esses dados para gerenciar solicitações, contratações, agenda, histórico dos serviços e comunicações relacionadas ao cuidado.',
  },
  {
    title: 'Segurança e funcionamento',
    icon: ShieldCheck,
    data: ['Dados de autenticação', 'Registros de acesso', 'Tokens de recuperação de senha', 'Eventos de segurança'],
    purpose: 'Usamos esses dados para proteger a conta, prevenir acessos indevidos, recuperar senha e manter a segurança da plataforma.',
    note: 'Também registramos algumas ações críticas, como mudanças de situação, bloqueios, exclusões, decisões administrativas e início ou encerramento de atendimento. Esses registros usam apenas as informações necessárias para segurança e rastreabilidade, sem copiar o conteúdo completo de dados assistenciais.',
  },
] as const;

export default function ProfilePrivacyDataUsageScreen() {
  return (
    <ScreenContainer contentStyle={styles.content}>
      <AppHeader showBack title="Como usamos seus dados" subtitle="Categorias de informações tratadas e suas finalidades." />
      {categories.map((category) => (
        <PrivacyInfoCard key={category.title} icon={category.icon} title={category.title}>
          <View style={privacyInfoStyles.list}>
            <Text style={privacyInfoStyles.label}>Dados envolvidos</Text>
            {category.data.map((item) => (
              <View key={item} style={privacyInfoStyles.itemRow}>
                <Text style={privacyInfoStyles.bullet}>•</Text>
                <Text style={privacyInfoStyles.item}>{item}</Text>
              </View>
            ))}
          </View>
          <View style={styles.purpose}>
            <Text style={privacyInfoStyles.label}>Para que usamos</Text>
            <Text style={privacyInfoStyles.paragraph}>{category.purpose}</Text>
          </View>
          {'note' in category ? <Text style={privacyInfoStyles.note}>{category.note}</Text> : null}
        </PrivacyInfoCard>
      ))}
      <Text style={privacyInfoStyles.note}>
        Em solicitações de exclusão, avaliamos os dados elegíveis para eliminação ou anonimização e os registros que precisam ser preservados.
      </Text>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, paddingBottom: spacing.xxl, gap: spacing.lg },
  purpose: { gap: spacing.sm },
});
