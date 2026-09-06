import {
  BookOpenText,
  Building2,
  Clock3,
  Database,
  FilePenLine,
  Handshake,
  HeartPulse,
  LifeBuoy,
  LockKeyhole,
  Scale,
  Target,
} from 'lucide-react-native';
import { StyleSheet, Text } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { PrivacyInfoCard, privacyInfoStyles } from '@/components/privacy-info-card';
import { ScreenContainer } from '@/components/screen-container';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';

const sections = [
  {
    title: '1. Responsável pelo tratamento',
    icon: Building2,
    text: 'O tratamento dos dados ocorre no contexto da plataforma Cuidar+, responsável por disponibilizar os recursos de cadastro, busca de cuidadores, solicitação de serviços, agenda, registros de cuidado e funcionalidades relacionadas. O projeto ainda não possui uma razão social informada nesta política.',
  },
  {
    title: '2. Dados coletados',
    icon: Database,
    text: 'Podemos tratar dados de cadastro, endereço e localização; dados profissionais e de disponibilidade dos cuidadores; informações da pessoa assistida; e dados de solicitações, contratações, agenda, atendimento e segurança necessários ao funcionamento da plataforma.',
  },
  {
    title: '3. Finalidades',
    icon: Target,
    text: 'Usamos os dados para identificar usuários, proteger contas, permitir a busca de cuidadores, organizar solicitações e serviços, apoiar o cuidado domiciliar, registrar atividades, produzir relatórios, enviar comunicações necessárias e manter a plataforma segura.',
  },
  {
    title: '4. Dados pessoais sensíveis',
    icon: HeartPulse,
    text: 'Informações da pessoa assistida podem envolver dados sensíveis relacionados à saúde, como mobilidade, alergias, restrições alimentares, medicações, rotinas e registros de cuidado. Esses dados recebem cuidado adicional e são usados para as finalidades relacionadas ao atendimento.',
  },
  {
    title: '5. Compartilhamento',
    icon: Handshake,
    text: 'Dados necessários ao serviço podem ser compartilhados entre responsável e cuidador, incluindo agenda, necessidades de cuidado e informações essenciais da pessoa assistida. Provedores técnicos de hospedagem, banco de dados, envio de comunicações e segurança também podem tratar dados na medida necessária ao funcionamento da plataforma.',
  },
  {
    title: '6. Retenção',
    icon: Clock3,
    text: 'Os dados podem ser mantidos enquanto forem necessários às finalidades da plataforma, às obrigações aplicáveis, à segurança, à preservação de registros e ao exercício de direitos. Quando não forem mais necessários, os dados elegíveis poderão ser eliminados ou anonimizados.',
  },
  {
    title: '7. Segurança',
    icon: LockKeyhole,
    text: 'Adotamos medidas técnicas e administrativas de proteção, como autenticação, controle de acesso, proteção de credenciais, validações no backend e restrições conforme o perfil. Essas medidas reduzem riscos, mas nenhum sistema pode garantir segurança absoluta.',
  },
  {
    title: '8. Direitos dos titulares',
    icon: Scale,
    text: 'Você pode solicitar confirmação e informações sobre o tratamento, acesso, correção de dados incompletos ou desatualizados, exclusão de dados elegíveis, informações sobre compartilhamento e revogação de consentimento quando aplicável.',
  },
  {
    title: '9. Solicitações de privacidade',
    icon: BookOpenText,
    text: 'As opções disponíveis em Configurações → Privacidade permitem consultar informações e solicitar a exclusão da conta. Algumas solicitações exigem confirmação de identidade e análise de obrigações aplicáveis, segurança ou exercício de direitos.',
  },
  {
    title: '10. Contato',
    icon: LifeBuoy,
    text: 'Canal de privacidade em definição pela equipe do projeto. Quando disponibilizado, o contato institucional será informado nesta seção.',
  },
  {
    title: '11. Alterações desta política',
    icon: FilePenLine,
    text: 'Esta política poderá ser atualizada para refletir melhorias no sistema, mudanças aplicáveis ou ajustes nas funcionalidades. A versão vigente será apresentada nesta tela.',
  },
] as const;

export default function ProfilePrivacyPolicyScreen() {
  return (
    <ScreenContainer contentStyle={styles.content}>
      <AppHeader showBack title="Política de Privacidade" subtitle="Como o Cuidar+ trata e protege informações pessoais." />
      <Text style={styles.introduction}>
        O Cuidar+ trata dados pessoais para possibilitar o cadastro, a conexão entre responsáveis e cuidadores, a organização do cuidado domiciliar e a segurança da plataforma.
      </Text>
      <Text style={styles.informative}>Conteúdo institucional e informativo.</Text>
      {sections.map((section) => (
        <PrivacyInfoCard key={section.title} icon={section.icon} title={section.title}>
          <Text style={privacyInfoStyles.paragraph}>{section.text}</Text>
        </PrivacyInfoCard>
      ))}
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, paddingBottom: spacing.xxl, gap: spacing.lg },
  introduction: { fontFamily: fontFamily.regular, fontSize: 14, lineHeight: 22, color: colors.mutedForeground },
  informative: { alignSelf: 'flex-start', paddingHorizontal: spacing.md, paddingVertical: spacing.sm, borderRadius: radii.full, backgroundColor: colors.secondary, fontFamily: fontFamily.bold, fontSize: 11, color: colors.secondaryForeground },
});
