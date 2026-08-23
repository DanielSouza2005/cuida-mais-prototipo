import { useState } from 'react';
import { router, type Href } from 'expo-router';
import { ClipboardList, Search } from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

const options = [
  {
    value: 'PUBLICATIONS',
    title: 'Serviços publicados',
    description: 'Consulte suas publicações, acompanhe interessados e publique novas oportunidades.',
    icon: ClipboardList,
    route: '/responsible-service-publications' as Href,
  },
  {
    value: 'CAREGIVERS',
    title: 'Buscar cuidadores',
    description: 'Encontre profissionais disponíveis e envie uma solicitação de serviço.',
    icon: Search,
    route: '/caregiver-search' as Href,
  },
] as const;

export default function ResponsibleServicesScreen() {
  const [mode, setMode] = useState<(typeof options)[number]['value']>('PUBLICATIONS');
  const selected = options.find((option) => option.value === mode) ?? options[0];
  const SelectedIcon = selected.icon;
  return (
    <ScreenContainer contentStyle={styles.content}>
      <AppHeader title="Serviços" subtitle="Publique oportunidades ou encontre cuidadores para a pessoa assistida." />
      <View style={styles.tabs}>
        {options.map((option) => <Pressable key={option.value} accessibilityRole="tab" accessibilityState={{ selected: mode === option.value }} onPress={() => setMode(option.value)} style={[styles.tab, mode === option.value && styles.tabActive]}><Text style={[styles.tabText, mode === option.value && styles.tabTextActive]}>{option.title}</Text></Pressable>)}
      </View>
      <View style={styles.panel}>
        <View style={styles.panelIcon}><SelectedIcon color={colors.primary} size={26} strokeWidth={2.4} /></View>
        <Text style={styles.panelTitle}>{selected.title}</Text>
        <Text style={styles.panelText}>{selected.description}</Text>
        <PrimaryButton label={mode === 'PUBLICATIONS' ? 'Ver serviços publicados' : 'Buscar cuidadores'} icon={SelectedIcon} onPress={() => router.push(selected.route)} />
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, gap: spacing.xl },
  tabs: { flexDirection: 'row', padding: spacing.xxs, borderRadius: radii.lg, backgroundColor: colors.muted },
  tab: { flex: 1, minHeight: 44, alignItems: 'center', justifyContent: 'center', borderRadius: radii.md, paddingHorizontal: spacing.sm },
  tabActive: { backgroundColor: colors.card, ...shadows.card },
  tabText: { textAlign: 'center', fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground },
  tabTextActive: { color: colors.primary },
  panel: { alignItems: 'stretch', gap: spacing.md, padding: spacing.xl, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card },
  panelIcon: { width: 54, height: 54, alignItems: 'center', justifyContent: 'center', borderRadius: radii.lg, backgroundColor: colors.secondary },
  panelTitle: { fontFamily: fontFamily.extraBold, fontSize: 18, color: colors.foreground },
  panelText: { fontFamily: fontFamily.regular, fontSize: 13, lineHeight: 20, color: colors.mutedForeground },
});
