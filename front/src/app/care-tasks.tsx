import { useCallback, useState } from 'react';
import { router, useFocusEffect, type Href } from 'expo-router';
import { ClipboardCheck, Plus } from 'lucide-react-native';
import { ActivityIndicator, Pressable, RefreshControl, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { listCareRoutines } from '@/services/careRoutineService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { CareRoutine } from '@/types/careRoutine';

export default function CareRoutinesScreen() {
  const [items, setItems] = useState<CareRoutine[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(false);
  const load = useCallback(async (refresh = false) => { if (refresh) setRefreshing(true); else setLoading(true); setError(false); try { setItems(await listCareRoutines()); } catch { setError(true); } finally { setLoading(false); setRefreshing(false); } }, []);
  useFocusEffect(useCallback(() => { void load(); }, [load]));

  return <ScreenContainer contentStyle={styles.content} scrollViewProps={{ refreshControl: <RefreshControl refreshing={refreshing} onRefresh={() => void load(true)} tintColor={colors.primary} /> }}>
    <AppHeader showBack title="Rotina de Cuidados" subtitle="Cadastre rotinas com cuidados que poderão ser reutilizados nas solicitações." />
    <PrimaryButton label="Nova rotina" onPress={() => router.push('/care-task-form' as Href)} />
    {loading ? <View style={styles.state}><ActivityIndicator color={colors.primary} /><Text style={styles.stateText}>Carregando rotinas...</Text></View> : null}
    {!loading && error ? <View style={styles.state}><Text style={styles.error}>Não foi possível carregar suas rotinas.</Text><PrimaryButton variant="secondary" label="Tentar novamente" onPress={() => void load()} /></View> : null}
    {!loading && !error && !items.length ? <View style={styles.empty}><ClipboardCheck size={34} color={colors.primary} /><Text style={styles.emptyTitle}>Nenhuma rotina de cuidados cadastrada.</Text><Text style={styles.stateText}>Crie uma rotina para reutilizar seus cuidados nas solicitações de serviço.</Text></View> : null}
    {!loading && !error ? items.map((routine) => <Pressable key={routine.id} style={styles.card} onPress={() => router.push(`/care-task/${routine.id}` as Href)}>
      <View style={styles.cardHead}><Text style={styles.cardTitle}>{routine.name}</Text><View style={[styles.badge, !routine.active && styles.badgeInactive]}><Text style={styles.badgeText}>{routine.active ? 'Ativa' : 'Inativa'}</Text></View></View>
      {routine.assistedPerson ? <Text style={styles.meta}>Pessoa assistida: {routine.assistedPerson.name}</Text> : <Text style={styles.meta}>Rotina geral</Text>}
      <Text style={styles.meta}>{routine.items.length} {routine.items.length === 1 ? 'cuidado' : 'cuidados'}</Text>
      {routine.items.some(item=>item.categoryLabel)?<Text style={styles.meta}>Tipos: {[...new Set(routine.items.map(item=>item.categoryLabel).filter(Boolean))].join(', ')}</Text>:null}
      <View style={styles.linkRow}><Text style={styles.link}>Ver detalhes</Text><Plus size={16} color={colors.primary} /></View>
    </Pressable>) : null}
  </ScreenContainer>;
}

const styles = StyleSheet.create({
  content:{paddingHorizontal:spacing.xl,gap:spacing.lg}, state:{alignItems:'center',gap:spacing.md,paddingVertical:spacing.xxl},stateText:{textAlign:'center',fontFamily:fontFamily.regular,fontSize:13,lineHeight:20,color:colors.mutedForeground},error:{fontFamily:fontFamily.semiBold,color:colors.destructive},empty:{alignItems:'center',gap:spacing.sm,padding:spacing.xl,borderRadius:radii.xl,backgroundColor:colors.muted},emptyTitle:{fontFamily:fontFamily.bold,fontSize:15,color:colors.foreground},card:{gap:spacing.sm,padding:spacing.lg,borderRadius:radii.xl,borderWidth:1,borderColor:colors.border,backgroundColor:colors.card,...shadows.card},cardHead:{flexDirection:'row',alignItems:'center',gap:spacing.sm},cardTitle:{flex:1,fontFamily:fontFamily.extraBold,fontSize:16,color:colors.foreground},badge:{paddingHorizontal:spacing.sm,paddingVertical:spacing.xs,borderRadius:radii.full,backgroundColor:colors.secondary},badgeInactive:{backgroundColor:colors.muted},badgeText:{fontFamily:fontFamily.bold,fontSize:10,color:colors.primary},meta:{fontFamily:fontFamily.regular,fontSize:12,color:colors.mutedForeground},linkRow:{flexDirection:'row',alignItems:'center',gap:spacing.xs,alignSelf:'flex-start'},link:{fontFamily:fontFamily.bold,fontSize:12,color:colors.primary}
});
