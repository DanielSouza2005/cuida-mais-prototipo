import { useCallback, useState } from 'react';
import { router, type Href, useFocusEffect } from 'expo-router';
import { Bell } from 'lucide-react-native';
import { Alert, Pressable, StyleSheet, Text, View } from 'react-native';
import { AppHeader } from '@/components/app-header';
import { LoadingState } from '@/components/loading-state';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { ApiError } from '@/services/api';
import { clearAllNotifications, getNotifications, readNotification } from '@/services/receivedServiceRequestService';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';
import type { CaregiverNotification } from '@/types/receivedServiceRequest';
import { formatDateTimeLocal } from '@/utils/dateTime';

export default function NotificationsScreen() {
  const { user } = useAuth();
  const [items, setItems] = useState<CaregiverNotification[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);

  useFocusEffect(useCallback(() => {
    let active = true;
    setLoading(true);
    setMessage(null);
    getNotifications()
      .then((result) => { if (active) setItems(result); })
      .catch(() => { if (active) setMessage('Não foi possível carregar as notificações.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []));

  async function open(item: CaregiverNotification) {
    try {
      await readNotification(item.id);
      setItems((current) => current.map((value) => value.id === item.id ? { ...value, readAt: new Date().toISOString() } : value));
      if (item.relatedEntityType === 'CARE_TASK') {
        if (user?.userType === 'caregiver') router.push('/caregiver-tasks' as Href);
        else router.push(`/care-task/${item.relatedEntityId}` as Href);
        return;
      }
      if (item.relatedEntityType === 'TASK_OCCURRENCE') {
        router.push(`/task-occurrence/${item.relatedEntityId}` as Href);
        return;
      }
      if (item.relatedEntityType === 'CARE_CONTRACT') {
        router.push(`/responsible-contract/${item.relatedEntityId}?itemType=CARE_CONTRACT` as Href);
        return;
      }
      if (item.relatedEntityType !== 'SERVICE_REQUEST') return;
      if (user?.userType === 'caregiver') router.push(`/caregiver-service-request/${item.relatedEntityId}` as Href);
      else if (user?.userType === 'family') router.push(`/responsible-service-request/${item.relatedEntityId}` as Href);
    } catch {
      setMessage('Não foi possível abrir a notificação.');
    }
  }

  function confirmClear() {
    Alert.alert('Limpar notificações', 'Deseja limpar todas as notificações?', [
      { text: 'Cancelar', style: 'cancel' },
      { text: 'Limpar', style: 'destructive', onPress: () => void clear() },
    ]);
  }

  async function clear() {
    try {
      await clearAllNotifications();
      setItems([]);
      Alert.alert('Tudo certo', 'Notificações limpas com sucesso.');
    } catch (error) {
      setMessage(error instanceof ApiError ? error.message : 'Não foi possível limpar as notificações.');
    }
  }

  if (loading) return <ScreenContainer contentStyle={styles.center}><LoadingState message="Carregando notificações..." /></ScreenContainer>;

  return (
    <ScreenContainer contentStyle={styles.content}>
      <AppHeader showBack title="Notificações" subtitle="Acompanhe solicitações e atualizações importantes." />
      <View style={styles.list}>
        {items.map((item) => (
          <Pressable key={item.id} onPress={() => void open(item)} style={({ pressed }) => [styles.card, pressed && styles.pressed]}>
            <View style={styles.icon}><Bell color={colors.primary} size={20} />{!item.readAt ? <View style={styles.dot} /> : null}</View>
            <View style={styles.flex}>
              <Text style={styles.title}>{item.title}</Text>
              <Text style={styles.message}>{item.message}</Text>
              <Text style={styles.date}>{formatDateTimeLocal(item.createdAt)}</Text>
            </View>
          </Pressable>
        ))}
        {items.length === 0 ? <Text style={styles.empty}>Nenhuma notificação encontrada.</Text> : null}
      </View>
      {items.length ? <PrimaryButton label="Limpar notificações" variant="secondary" onPress={confirmClear} /> : null}
      {message ? <Text style={styles.empty}>{message}</Text> : null}
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, gap: spacing.lg },
  center: { flexGrow: 1, justifyContent: 'center', padding: spacing.xl },
  list: { gap: spacing.md },
  card: { flexDirection: 'row', gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card },
  pressed: { opacity: 0.78 },
  icon: { width: 42, height: 42, borderRadius: 21, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.secondary },
  dot: { position: 'absolute', top: 2, right: 2, width: 9, height: 9, borderRadius: 5, backgroundColor: colors.destructive },
  flex: { flex: 1, gap: spacing.xs },
  title: { fontFamily: fontFamily.bold, fontSize: 14, color: colors.foreground },
  message: { fontFamily: fontFamily.regular, fontSize: 12, lineHeight: 18, color: colors.mutedForeground },
  date: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.primary },
  empty: { textAlign: 'center', fontFamily: fontFamily.medium, color: colors.mutedForeground },
});
