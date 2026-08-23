import { useEffect } from 'react';
import { router, Tabs } from 'expo-router';
import { BriefcaseBusiness, CalendarDays, ClipboardCheck, Handshake, Home, MessageCircle, User } from 'lucide-react-native';
import { StyleSheet } from 'react-native';

import { LoadingState } from '@/components/loading-state';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

const tabBarIconSize = 20;

export default function MainTabNavigator() {
  const { isAuthenticated, isLoading, user } = useAuth();
  const isCaregiver = user?.userType === 'caregiver';

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.replace('/login');
    }
  }, [isAuthenticated, isLoading]);

  if (isLoading) {
    return (
      <ScreenContainer contentStyle={styles.loadingContent}>
        <LoadingState message="Carregando sua área principal..." />
      </ScreenContainer>
    );
  }

  if (!isAuthenticated) return null;

  return (
    <Tabs
      initialRouteName="inicio"
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.mutedForeground,
        tabBarLabelStyle: styles.label,
        tabBarStyle: styles.tabBar,
        tabBarItemStyle: styles.item,
      }}
    >
      <Tabs.Screen
        name="inicio"
        options={{
          title: 'Início',
          tabBarIcon: ({ color, focused }) => (
            <Home color={color} fill={focused ? colors.secondary : 'transparent'} size={tabBarIconSize} strokeWidth={2.4} />
          ),
        }}
      />
      <Tabs.Screen
        name="buscar"
        options={{
          title: 'Serviços',
          href: isCaregiver ? null : undefined,
          tabBarIcon: ({ color }) => <BriefcaseBusiness color={color} size={tabBarIconSize} strokeWidth={2.4} />,
        }}
      />
      <Tabs.Screen
        name="contratacoes"
        options={{
          title: 'Contratações',
          href: isCaregiver ? null : undefined,
          tabBarIcon: ({ color }) => <Handshake color={color} size={tabBarIconSize} strokeWidth={2.4} />,
        }}
      />
      <Tabs.Screen
        name="servicos"
        options={{
          title: 'Serviços',
          href: isCaregiver ? undefined : null,
          tabBarIcon: ({ color }) => <BriefcaseBusiness color={color} size={tabBarIconSize} strokeWidth={2.4} />,
        }}
      />
      <Tabs.Screen
        name="solicitacoes"
        options={{
          title: 'Solicitações',
          href: isCaregiver ? undefined : null,
          tabBarIcon: ({ color }) => <Handshake color={color} size={tabBarIconSize} strokeWidth={2.4} />,
        }}
      />
      <Tabs.Screen
        name="agenda"
        options={{
          title: 'Agenda',
          tabBarIcon: ({ color }) => <CalendarDays color={color} size={tabBarIconSize} strokeWidth={2.4} />,
        }}
      />
      <Tabs.Screen
        name="cuidados"
        options={{
          title: 'Cuidados',
          tabBarIcon: ({ color }) => <ClipboardCheck color={color} size={tabBarIconSize} strokeWidth={2.4} />,
        }}
      />
      <Tabs.Screen
        name="mensagens"
        options={{
          title: 'Mensagens',
          href: null,
          tabBarIcon: ({ color }) => <MessageCircle color={color} size={tabBarIconSize} strokeWidth={2.4} />,
        }}
      />
      <Tabs.Screen
        name="perfil"
        options={{
          title: 'Perfil',
          tabBarIcon: ({ color }) => <User color={color} size={tabBarIconSize} strokeWidth={2.4} />,
        }}
      />
    </Tabs>
  );
}

const styles = StyleSheet.create({
  loadingContent: {
    flexGrow: 1,
    justifyContent: 'center',
    paddingHorizontal: spacing.xl,
  },
  tabBar: {
    minHeight: 68,
    paddingTop: spacing.xs,
    paddingBottom: spacing.xs,
    borderTopWidth: 1,
    borderTopColor: colors.border,
    backgroundColor: colors.card,
    ...shadows.card,
  },
  item: {
    flex: 1,
    borderRadius: radii.lg,
  },
  label: {
    fontFamily: fontFamily.bold,
    fontSize: 9,
    marginTop: 1,
  },
});
