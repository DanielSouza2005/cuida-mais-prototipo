import { router, useFocusEffect, type Href } from 'expo-router';
import {
  Bell,
  ClipboardList,
  HeartPulse,
  MapPin,
  Search,
  ShieldCheck,
  Stethoscope,
  UserRound,
  Users,
} from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { useCallback, useState } from 'react';

import { QuickActionCard } from '@/components/home/quick-action-card';
import { SummaryCard } from '@/components/home/summary-card';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import { getUnreadNotificationCount } from '@/services/receivedServiceRequestService';

const tabRoutes = {
  requests: '/solicitacoes' as Href,
  caregiverSearch: '/caregiver-search' as Href,
  cuidados: '/cuidados' as Href,
  mensagens: '/mensagens' as Href,
  perfil: '/perfil' as Href,
} as const;

export default function HomeScreen() {
  const { user } = useAuth();
  const firstName = user?.fullName?.trim().split(/\s+/)[0] ?? 'Daniel';
  const isCaregiver = user?.userType === 'caregiver';
  const [unreadCount,setUnreadCount]=useState(0);
  useFocusEffect(useCallback(()=>{if(user)getUnreadNotificationCount().then((result)=>setUnreadCount(result.count)).catch(()=>setUnreadCount(0));},[user]));

  return (
    <ScreenContainer contentStyle={styles.content}>
      <View style={styles.header}>
        <View>
          <Text style={styles.greeting}>Olá, {firstName}</Text>
          <Text style={styles.subtitle}>Bem-vindo ao Cuidar+</Text>
        </View>
        {user ? <Pressable accessibilityRole="button" accessibilityLabel="Notificações" onPress={() => router.push('/caregiver-notifications' as Href)} style={styles.notificationButton}><Bell color={colors.primary} size={22} />{unreadCount > 0 ? <View style={styles.badge}><Text style={styles.badgeText}>{unreadCount}</Text></View> : null}</Pressable> : null}
      </View>

      <SummaryCard
        eyebrow="Resumo de hoje"
        title="Nenhum cuidado agendado para hoje"
        description="Acompanhe suas atividades e informações importantes por aqui."
        icon={ShieldCheck}
      />

      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Acesso rápido</Text>
          <Text style={styles.sectionHint}>Funcionalidades em construção</Text>
        </View>

        <View style={styles.grid}>
          {isCaregiver ? (
            <QuickActionCard title="Solicitações" description="Avalie novos pedidos de cuidado." icon={ClipboardList} onPress={() => router.push(tabRoutes.requests)} />
          ) : (
            <QuickActionCard
              title="Buscar cuidadores"
              description="Encontre profissionais disponíveis"
              icon={Search}
              onPress={() => router.push(tabRoutes.caregiverSearch)}
            />
          )}
          <QuickActionCard
            title={isCaregiver ? 'Serviços oferecidos' : 'Pessoa assistida'}
            description={isCaregiver ? 'Organize os serviços do perfil.' : 'Consulte dados principais de cuidado.'}
            icon={isCaregiver ? Stethoscope : Users}
            onPress={() => router.push(tabRoutes.cuidados)}
          />
          <QuickActionCard
            title="Mensagens"
            description="Espaço preparado para conversas futuras."
            icon={HeartPulse}
            onPress={() => router.push(tabRoutes.mensagens)}
          />
          <QuickActionCard
            title="Perfil"
            description="Acesse dados da conta e preferências."
            icon={UserRound}
            onPress={() => router.push(tabRoutes.perfil)}
          />
        </View>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>{isCaregiver ? 'Área do cuidador' : 'Área do responsável'}</Text>
        <View style={styles.infoCard}>
          <View style={styles.infoIcon}>
            {isCaregiver ? (
              <Stethoscope color={colors.mintForeground} size={20} strokeWidth={2.4} />
            ) : (
              <MapPin color={colors.mintForeground} size={20} strokeWidth={2.4} />
            )}
          </View>
          <View style={styles.infoCopy}>
            <Text style={styles.infoTitle}>{isCaregiver ? 'Próximos atendimentos' : 'Endereço do cuidado'}</Text>
            <Text style={styles.infoText}>
              {isCaregiver
                ? 'Esta área será personalizada com disponibilidade e serviços nas próximas versões.'
                : 'Esta área será personalizada com dados da pessoa assistida nas próximas versões.'}
            </Text>
          </View>
        </View>
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.lg,
    gap: spacing.xl,
  },
  header: {
    paddingTop: spacing.sm,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    gap: spacing.md,
  },
  notificationButton: { width: 46, height: 46, borderRadius: 23, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.card, borderWidth: 1, borderColor: colors.border, ...shadows.card },
  badge: { position: 'absolute', top: -3, right: -3, minWidth: 20, height: 20, paddingHorizontal: 5, borderRadius: 10, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.destructive },
  badgeText: { fontFamily: fontFamily.bold, fontSize: 10, color: colors.primaryForeground },
  greeting: {
    fontFamily: fontFamily.extraBold,
    fontSize: 26,
    lineHeight: 32,
    color: colors.foreground,
  },
  subtitle: {
    marginTop: spacing.xs,
    fontFamily: fontFamily.regular,
    fontSize: 14,
    color: colors.mutedForeground,
  },
  section: {
    gap: spacing.md,
  },
  sectionHeader: {
    gap: spacing.xxs,
  },
  sectionTitle: {
    fontFamily: fontFamily.extraBold,
    fontSize: 18,
    color: colors.foreground,
  },
  sectionHint: {
    fontFamily: fontFamily.regular,
    fontSize: 12,
    color: colors.mutedForeground,
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    rowGap: spacing.md,
  },
  infoCard: {
    flexDirection: 'row',
    gap: spacing.md,
    borderRadius: radii.xxl,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.mint,
    padding: spacing.lg,
    ...shadows.card,
  },
  infoIcon: {
    width: 42,
    height: 42,
    borderRadius: radii.full,
    backgroundColor: colors.card,
    alignItems: 'center',
    justifyContent: 'center',
  },
  infoCopy: {
    flex: 1,
    gap: spacing.xs,
  },
  infoTitle: {
    fontFamily: fontFamily.bold,
    fontSize: 14,
    color: colors.mintForeground,
  },
  infoText: {
    fontFamily: fontFamily.regular,
    fontSize: 12.5,
    lineHeight: 19,
    color: colors.mintForeground,
  },
});
