import { useCallback, useState } from 'react';
import { router, useFocusEffect, useLocalSearchParams, type Href } from 'expo-router';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { CareRoutineItemDetails } from '@/components/care-routine-item-details';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { activateCareRoutine, deactivateCareRoutine, getCareRoutine } from '@/services/careRoutineService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { CareRoutine } from '@/types/careRoutine';

export default function CareRoutineDetailsScreen(){
  const params=useLocalSearchParams<{id:string}>();const id=Array.isArray(params.id)?params.id[0]:params.id;const [routine,setRoutine]=useState<CareRoutine|null>(null);const [error,setError]=useState(false);const [busy,setBusy]=useState(false);
  const load=useCallback(async()=>{if(!id)return;setError(false);try{setRoutine(await getCareRoutine(id));}catch{setError(true);}},[id]);useFocusEffect(useCallback(()=>{void load();},[load]));
  async function toggle(){if(!routine)return;setBusy(true);try{setRoutine(routine.active?await deactivateCareRoutine(routine.id):await activateCareRoutine(routine.id));}finally{setBusy(false);}}
  if(!routine&&!error)return <ScreenContainer contentStyle={styles.center}><Text style={styles.meta}>Carregando rotina...</Text></ScreenContainer>;
  if(!routine||error)return <ScreenContainer contentStyle={styles.center}><Text style={styles.error}>Rotina de cuidados não encontrada.</Text><PrimaryButton variant="secondary" label="Voltar" onPress={()=>router.back()}/></ScreenContainer>;
  return <ScreenContainer contentStyle={styles.content}><AppHeader showBack title={routine.name} subtitle="Detalhes da rotina de cuidados"/>
    <Section title="Rotina"><Info label="Nome" value={routine.name}/>{routine.description?<Info label="Descrição" value={routine.description}/>:null}<Info label="Pessoa assistida" value={routine.assistedPerson?.name??'Rotina geral'}/><Info label="Disponibilidade" value={routine.active?'Ativa para novas solicitações':'Inativa para novas solicitações'}/></Section>
    <Section title="Cuidados da rotina">{routine.items.map((item,index)=><CareRoutineItemDetails key={item.id??`${index}`} item={item} index={index}/>)}</Section>
    <PrimaryButton label="Editar rotina" onPress={()=>router.push(`/care-task-form?id=${routine.id}` as Href)}/><PrimaryButton variant="secondary" label={busy?'Atualizando...':routine.active?'Inativar rotina':'Reativar rotina'} disabled={busy} loading={busy} onPress={toggle}/>
  </ScreenContainer>;
}
function Section({title,children}:{title:string;children:React.ReactNode}){return <View style={styles.section}><Text style={styles.sectionTitle}>{title}</Text>{children}</View>};function Info({label,value}:{label:string;value:string}){return <View style={styles.info}><Text style={styles.label}>{label}</Text><Text style={styles.value}>{value}</Text></View>}
const styles=StyleSheet.create({content:{paddingHorizontal:spacing.xl,gap:spacing.lg},center:{flexGrow:1,justifyContent:'center',alignItems:'center',gap:spacing.md,padding:spacing.xl},section:{gap:spacing.md,padding:spacing.lg,borderRadius:radii.xl,borderWidth:1,borderColor:colors.border,backgroundColor:colors.card,...shadows.card},sectionTitle:{fontFamily:fontFamily.extraBold,fontSize:17,color:colors.foreground},info:{gap:spacing.xxs},label:{fontFamily:fontFamily.semiBold,fontSize:11,color:colors.mutedForeground},value:{fontFamily:fontFamily.medium,fontSize:13,lineHeight:20,color:colors.foreground},item:{flexDirection:'row',gap:spacing.md,paddingVertical:spacing.sm},order:{width:26,height:26,textAlign:'center',textAlignVertical:'center',borderRadius:radii.full,backgroundColor:colors.secondary,fontFamily:fontFamily.bold,color:colors.primary},flex:{flex:1,gap:spacing.xs},itemTitle:{fontFamily:fontFamily.bold,fontSize:14,color:colors.foreground},itemDescription:{fontFamily:fontFamily.regular,fontSize:12,lineHeight:18,color:colors.mutedForeground},meta:{fontFamily:fontFamily.regular,color:colors.mutedForeground},error:{fontFamily:fontFamily.bold,color:colors.destructive}});
