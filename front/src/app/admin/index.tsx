import { useCallback, useState } from 'react';
import { router, useFocusEffect, type Href } from 'expo-router';
import { ClipboardCheck, ShieldAlert, UserRoundCheck, Users } from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { LoadingState } from '@/components/loading-state';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { ApiError } from '@/services/api';
import { getDashboard } from '@/services/adminService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { AdminDashboardSummary } from '@/types/admin';

export default function AdminHomeScreen(){
  const {user}=useAuth(); const [summary,setSummary]=useState<AdminDashboardSummary|null>(null); const [loading,setLoading]=useState(true); const [error,setError]=useState<string|null>(null);
  const load=useCallback(async()=>{setLoading(true);setError(null);try{setSummary(await getDashboard());}catch(cause){setError(cause instanceof ApiError?cause.message:'Não foi possível carregar o resumo administrativo.');}finally{setLoading(false);}},[]);
  useFocusEffect(useCallback(()=>{void load();},[load]));
  const firstName=user?.fullName?.trim().split(/\s+/)[0]||'administrador';
  return <ScreenContainer contentStyle={styles.content}><AppHeader/><View><Text style={styles.title}>Olá, {firstName}</Text><Text style={styles.subtitle}>Acompanhe contas e cadastros que precisam de atenção.</Text></View>
    {loading&&!summary?<LoadingState message="Carregando resumo..."/>:summary?<View style={styles.metrics}>
      <Metric label="Cuidadores pendentes" value={summary.pendingCaregivers}/><Metric label="Responsáveis pendentes" value={summary.pendingResponsibles}/><Metric label="Total de usuários" value={summary.totalUsers}/><Metric label="Contas bloqueadas" value={summary.blockedUsers}/><Metric label="Aprovações nos últimos 7 dias" value={summary.recentApprovals} wide/>
    </View>:null}{error?<View style={styles.errorBox}><Text style={styles.error}>{error}</Text><PrimaryButton label="Tentar novamente" variant="secondary" loading={loading} onPress={()=>void load()}/></View>:null}<Text style={styles.sectionTitle}>Acesso rápido</Text><View style={styles.shortcuts}>
      <Shortcut title="Cuidadores pendentes" description="Analisar cadastros profissionais" icon={UserRoundCheck} onPress={()=>router.push({pathname:'/admin/approvals',params:{profile:'caregivers',status:'PENDENTE'}})}/>
      <Shortcut title="Responsáveis pendentes" description="Analisar responsáveis cadastrados" icon={ClipboardCheck} onPress={()=>router.push({pathname:'/admin/approvals',params:{profile:'responsibles',status:'PENDENTE'}})}/>
      <Shortcut title="Contas bloqueadas" description="Consultar e desbloquear contas" icon={ShieldAlert} onPress={()=>router.push({pathname:'/admin/users',params:{status:'BLOQUEADO'}})}/>
      <Shortcut title="Todos os usuários" description="Pesquisar a base de usuários" icon={Users} onPress={()=>router.push('/admin/users' as Href)}/>
    </View>
  </ScreenContainer>;
}
function Metric({label,value,wide}:{label:string;value:number;wide?:boolean}){return <View style={[styles.metric,wide&&styles.metricWide]}><Text style={styles.metricValue}>{value}</Text><Text style={styles.metricLabel}>{label}</Text></View>}
function Shortcut({title,description,icon:Icon,onPress}:{title:string;description:string;icon:typeof Users;onPress:()=>void}){return <Pressable onPress={onPress} style={styles.shortcut}><View style={styles.icon}><Icon color={colors.primary} size={21}/></View><View style={styles.grow}><Text style={styles.shortcutTitle}>{title}</Text><Text style={styles.shortcutDescription}>{description}</Text></View></Pressable>}
const styles=StyleSheet.create({content:{paddingHorizontal:spacing.xl,paddingBottom:spacing.xxl,gap:spacing.xl},title:{fontFamily:fontFamily.extraBold,fontSize:26,color:colors.foreground},subtitle:{marginTop:spacing.xs,fontFamily:fontFamily.regular,fontSize:14,lineHeight:21,color:colors.mutedForeground},metrics:{flexDirection:'row',flexWrap:'wrap',gap:spacing.md},metric:{width:'47%',minHeight:112,justifyContent:'center',padding:spacing.lg,borderRadius:radii.xl,borderWidth:1,borderColor:colors.border,backgroundColor:colors.card,...shadows.card},metricWide:{width:'100%'},metricValue:{fontFamily:fontFamily.extraBold,fontSize:28,color:colors.primary},metricLabel:{marginTop:spacing.xs,fontFamily:fontFamily.medium,fontSize:12,lineHeight:18,color:colors.mutedForeground},sectionTitle:{fontFamily:fontFamily.extraBold,fontSize:18,color:colors.foreground},shortcuts:{gap:spacing.md},shortcut:{flexDirection:'row',alignItems:'center',gap:spacing.md,padding:spacing.lg,borderRadius:radii.xl,borderWidth:1,borderColor:colors.border,backgroundColor:colors.card},icon:{width:42,height:42,alignItems:'center',justifyContent:'center',borderRadius:radii.lg,backgroundColor:colors.secondary},grow:{flex:1},shortcutTitle:{fontFamily:fontFamily.bold,fontSize:14,color:colors.foreground},shortcutDescription:{marginTop:spacing.xxs,fontFamily:fontFamily.regular,fontSize:12,color:colors.mutedForeground},errorBox:{gap:spacing.md},error:{fontFamily:fontFamily.medium,fontSize:13,color:colors.destructive}});
