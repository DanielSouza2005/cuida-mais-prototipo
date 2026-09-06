import { useEffect } from 'react';
import { router, Stack } from 'expo-router';
import { StyleSheet } from 'react-native';

import { LoadingState } from '@/components/loading-state';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { spacing } from '@/theme/tokens';

export default function AdminNavigator() {
  const { isAuthenticated, isLoading, user } = useAuth();

  useEffect(() => {
    if (!isLoading && (!isAuthenticated || user?.userType !== 'admin')) router.replace('/login');
  }, [isAuthenticated, isLoading, user?.userType]);

  if (isLoading || user?.userType !== 'admin') {
    return <ScreenContainer contentStyle={styles.loading}><LoadingState message="Validando acesso administrativo..." /></ScreenContainer>;
  }

  return <Stack screenOptions={{ headerShown: false }} />;
}

const styles = StyleSheet.create({
  loading: { flexGrow: 1, justifyContent: 'center', paddingHorizontal: spacing.xl },
});
