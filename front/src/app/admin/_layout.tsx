import { useEffect } from 'react';
import { router, Tabs } from 'expo-router';
import { ClipboardCheck, Home, User, Users } from 'lucide-react-native';
import { StyleSheet } from 'react-native';

import { LoadingState } from '@/components/loading-state';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

export default function AdminTabNavigator() {
  const { isAuthenticated, isLoading, user } = useAuth();
  useEffect(() => {
    if (!isLoading && (!isAuthenticated || user?.userType !== 'admin')) router.replace('/login');
  }, [isAuthenticated, isLoading, user?.userType]);
  if (isLoading || user?.userType !== 'admin') return <ScreenContainer contentStyle={styles.loading}><LoadingState message="Validando acesso administrativo..."/></ScreenContainer>;
  return <Tabs initialRouteName="index" screenOptions={{headerShown:false,tabBarActiveTintColor:colors.primary,tabBarInactiveTintColor:colors.mutedForeground,tabBarLabelStyle:styles.label,tabBarStyle:styles.tabBar,tabBarItemStyle:styles.item}}>
    <Tabs.Screen name="index" options={{title:'Início',tabBarIcon:({color,focused})=><Home color={color} fill={focused?colors.secondary:'transparent'} size={20} strokeWidth={2.4}/>}}/>
    <Tabs.Screen name="users" options={{title:'Usuários',tabBarIcon:({color})=><Users color={color} size={20} strokeWidth={2.4}/>}}/>
    <Tabs.Screen name="approvals" options={{title:'Aprovações',tabBarIcon:({color})=><ClipboardCheck color={color} size={20} strokeWidth={2.4}/>}}/>
    <Tabs.Screen name="profile" options={{title:'Perfil',tabBarIcon:({color})=><User color={color} size={20} strokeWidth={2.4}/>}}/>
  </Tabs>;
}

const styles=StyleSheet.create({loading:{flexGrow:1,justifyContent:'center',paddingHorizontal:spacing.xl},tabBar:{minHeight:68,paddingTop:spacing.xs,paddingBottom:spacing.xs,borderTopWidth:1,borderTopColor:colors.border,backgroundColor:colors.card,...shadows.card},item:{flex:1,borderRadius:radii.lg},label:{fontFamily:fontFamily.bold,fontSize:9,marginTop:1}});
