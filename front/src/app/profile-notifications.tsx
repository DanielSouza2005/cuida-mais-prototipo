import { useCallback, useState } from 'react';
import { useFocusEffect } from 'expo-router';
import { StyleSheet, Switch, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { LoadingState } from '@/components/loading-state';
import { ScreenContainer } from '@/components/screen-container';
import { ApiError } from '@/services/api';
import { getNotificationPreferences, updateNotificationPreference } from '@/services/notificationService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { NotificationPreferenceGroup } from '@/types/notification';
import { getNotificationVisualConfig } from '@/utils/notificationCatalog';

export default function ProfileNotificationsScreen() {
  const [groups, setGroups] = useState<NotificationPreferenceGroup[]>([]);
  const [loading, setLoading] = useState(true);
  const [savingTypes, setSavingTypes] = useState<Set<string>>(new Set());
  const [message, setMessage] = useState<string | null>(null);
  const [isError, setIsError] = useState(false);
  const savingAny = savingTypes.size > 0;

  useFocusEffect(useCallback(() => {
    let active = true;
    setLoading(true);
    setMessage(null);
    setIsError(false);
    getNotificationPreferences()
      .then((response) => { if (active) setGroups(response.groups); })
      .catch(() => { if (active) { setIsError(true); setMessage('Não foi possível carregar as preferências de notificação.'); } })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []));

  async function change(type: string, enabled: boolean) {
    const previous = groups;
    setGroups((current) => current.map((group) => ({ ...group, items: group.items.map((item) => item.type === type ? { ...item, enabled } : item) })));
    setSavingTypes((current) => new Set(current).add(type));
    setMessage('Salvando preferência...');
    setIsError(false);
    try {
      const response = await updateNotificationPreference(type, enabled);
      setGroups(response.groups);
      setMessage('Preferências atualizadas.');
    } catch (error) {
      setGroups(previous);
      setIsError(true);
      setMessage(error instanceof ApiError ? error.message : 'Não foi possível salvar a preferência de notificação.');
    } finally {
      setSavingTypes((current) => { const next = new Set(current); next.delete(type); return next; });
    }
  }

  if (loading) return <ScreenContainer contentStyle={styles.center}><LoadingState message="Carregando preferências..." /></ScreenContainer>;

  return (
    <ScreenContainer contentStyle={styles.content}>
      <AppHeader showBack title="Notificações" subtitle="Escolha quais avisos deseja receber no aplicativo." />
      {groups.map((group) => (
        <View key={group.category} style={styles.section}>
          <Text style={styles.sectionTitle}>{group.categoryLabel}</Text>
          <View style={styles.card}>
            {group.items.map((item, index) => {
              const visual = getNotificationVisualConfig(item.type);
              const Icon = visual.icon;
              return (
                <View key={item.type} style={[styles.row, index > 0 && styles.divider]}>
                  <View style={[styles.icon, { backgroundColor: visual.backgroundColor }]}><Icon color={visual.color} size={20} /></View>
                  <View style={styles.copy}>
                    <View style={styles.titleRow}><Text style={styles.title}>{item.label}</Text>{item.required ? <Text style={styles.required}>Obrigatório</Text> : null}</View>
                    <Text style={styles.description}>{item.required ? 'Este aviso é necessário para o funcionamento do aplicativo.' : item.description}</Text>
                  </View>
                  <Switch
                    accessibilityLabel={`${item.label}: ${item.enabled ? 'ativada' : 'desativada'}`}
                    value={item.enabled}
                    disabled={!item.configurable || item.required || savingAny}
                    onValueChange={(value) => void change(item.type, value)}
                    trackColor={{ false: '#CAD3D8', true: '#9AD3BB' }}
                    thumbColor={item.enabled ? colors.primary : '#F7F8F8'}
                  />
                </View>
              );
            })}
          </View>
        </View>
      ))}
      {groups.length === 0 && !message ? <Text style={styles.feedback}>Nenhuma preferência de notificação disponível.</Text> : null}
      {message ? <Text accessibilityLiveRegion="polite" style={[styles.feedback, isError && styles.error]}>{message}</Text> : null}
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, paddingBottom: spacing.xl, gap: spacing.xl },
  center: { flexGrow: 1, justifyContent: 'center', padding: spacing.xl },
  section: { gap: spacing.md },
  sectionTitle: { fontFamily: fontFamily.extraBold, fontSize: 17, color: colors.foreground },
  card: { paddingHorizontal: spacing.lg, borderRadius: radii.xl, backgroundColor: colors.card, borderWidth: 1, borderColor: colors.border, ...shadows.card },
  row: { minHeight: 92, paddingVertical: spacing.lg, flexDirection: 'row', alignItems: 'center', gap: spacing.md },
  divider: { borderTopWidth: 1, borderTopColor: colors.border },
  icon: { width: 42, height: 42, borderRadius: radii.md, alignItems: 'center', justifyContent: 'center' },
  copy: { flex: 1, gap: spacing.xs },
  titleRow: { flexDirection: 'row', alignItems: 'center', flexWrap: 'wrap', gap: spacing.sm },
  title: { flexShrink: 1, fontFamily: fontFamily.bold, fontSize: 14, color: colors.foreground },
  description: { fontFamily: fontFamily.regular, fontSize: 12, lineHeight: 18, color: colors.mutedForeground },
  required: { paddingHorizontal: spacing.sm, paddingVertical: spacing.xxs, borderRadius: radii.full, backgroundColor: colors.muted, fontFamily: fontFamily.bold, fontSize: 9, color: colors.secondaryForeground },
  feedback: { textAlign: 'center', fontFamily: fontFamily.medium, fontSize: 12, color: colors.mutedForeground },
  error: { color: colors.destructive },
});
