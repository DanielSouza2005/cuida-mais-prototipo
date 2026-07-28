import { useCallback, useRef, useState } from 'react';
import { router, useFocusEffect, type Href } from 'expo-router';
import { ChevronLeft, ChevronRight, ClipboardCheck, RefreshCw } from 'lucide-react-native';
import { Pressable, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { AppHeader } from '@/components/app-header';
import { TaskOccurrenceCard } from '@/components/care-task-card';
import { DatePickerField } from '@/components/date-picker-field';
import { OptionGroup } from '@/components/option-group';
import { ScreenContainer } from '@/components/screen-container';
import { getCaregiverDayTasks } from '@/services/careTaskService';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';
import type { TaskOccurrence, TaskOccurrenceStatus } from '@/types/careTasks';
import { addDays, todayDateOnly } from '@/utils/agendaDate';
import { deviceTimezone, taskOccurrenceStatusLabels } from '@/utils/careTaskLabels';
import { displayDateToIso, isoDateToDisplay } from '@/utils/contractTerminationLabels';

const filters:(TaskOccurrenceStatus|undefined)[]=[undefined,'PENDENTE','ATRASADA','CONCLUIDA','NAO_REALIZADA'];
export default function CaregiverTasksScreen(){
  const [date,setDate]=useState(isoDateToDisplay(todayDateOnly()));const [status,setStatus]=useState<TaskOccurrenceStatus>();const [assistedPersonId,setAssistedPersonId]=useState('TODAS');
  const [people,setPeople]=useState<{value:string;label:string}[]>([]);const [items,setItems]=useState<TaskOccurrence[]|null>(null);const [error,setError]=useState(false);const [refreshing,setRefreshing]=useState(false);
  const requestSequence=useRef(0);
  const load=useCallback(async(refresh=false)=>{const requestId=++requestSequence.current;if(refresh)setRefreshing(true);setError(false);try{const content=(await getCaregiverDayTasks(displayDateToIso(date),deviceTimezone(),{status,assistedPersonId:assistedPersonId==='TODAS'?undefined:assistedPersonId})).content;if(requestId!==requestSequence.current)return;setItems([...new Map(content.map(item=>[item.id,item])).values()]);setPeople(current=>[...new Map([...current,...content.map(item=>({value:item.assistedPersonId,label:item.assistedPersonName}))].map(item=>[item.value,item])).values()]);}catch{if(requestId===requestSequence.current)setError(true);}finally{if(requestId===requestSequence.current)setRefreshing(false);}},[assistedPersonId,date,status]);
  useFocusEffect(useCallback(()=>{setItems(null);void load();return()=>{requestSequence.current++;};},[load]));
  function move(amount:number){setDate(isoDateToDisplay(addDays(displayDateToIso(date),amount)));}
  return <ScreenContainer contentStyle={styles.content} scrollViewProps={{refreshControl:<RefreshControl refreshing={refreshing} onRefresh={()=>void load(true)} tintColor={colors.primary}/>}}>
    <AppHeader showBack title="Cuidados do dia" subtitle="Acompanhe os cuidados previstos para a data selecionada."/>
    <View style={styles.dateNavigation}><Pressable accessibilityLabel="Dia anterior" onPress={()=>move(-1)} style={styles.dayButton}><ChevronLeft color={colors.primary}/></Pressable><View style={styles.dateField}><DatePickerField label="Data" value={date} onChange={setDate}/></View><Pressable accessibilityLabel="Próximo dia" onPress={()=>move(1)} style={styles.dayButton}><ChevronRight color={colors.primary}/></Pressable></View>
    <Pressable onPress={()=>setDate(isoDateToDisplay(todayDateOnly()))} style={styles.todayButton}><Text style={styles.retry}>Hoje</Text></Pressable>
    <View style={styles.filters}>{filters.map(item=><Pressable key={item??'TODAS'} onPress={()=>setStatus(item)} style={[styles.chip,status===item&&styles.active]}><Text style={[styles.chipText,status===item&&styles.activeText]}>{item?taskOccurrenceStatusLabels[item]:'Todos'}</Text></Pressable>)}</View>
    {people.length>1?<View style={styles.advanced}><OptionGroup label="Pessoa assistida" options={[{value:'TODAS',label:'Todas'},...people]} value={assistedPersonId} onChange={value=>setAssistedPersonId(value as string)}/></View>:null}
    {items===null&&!error?<State text="Carregando cuidados..."/>:error?<State text="Não foi possível carregar os cuidados." icon={<RefreshCw color={colors.destructive}/>} action={()=>void load()}/>:items?.length===0?<State text="Nenhum cuidado previsto para este dia." icon={<ClipboardCheck color={colors.primary}/>} />:<View style={styles.list}>{items?.map(item=><TaskOccurrenceCard key={item.id} occurrence={item} onPress={()=>router.push(`/task-occurrence/${item.id}` as Href)}/>)}</View>}
  </ScreenContainer>;
}
function State({text,icon,action}:{text:string;icon?:React.ReactNode;action?:()=>void}){return <View style={styles.state}>{icon}<Text style={styles.stateText}>{text}</Text>{action?<Pressable onPress={action}><Text style={styles.retry}>Tentar novamente</Text></Pressable>:null}</View>}
const styles=StyleSheet.create({content:{paddingHorizontal:spacing.xl,gap:spacing.lg},dateNavigation:{flexDirection:'row',alignItems:'flex-end',gap:spacing.sm},dateField:{flex:1},dayButton:{width:48,height:54,alignItems:'center',justifyContent:'center',borderRadius:radii.lg,borderWidth:1,borderColor:colors.border,backgroundColor:colors.card},todayButton:{alignSelf:'center',paddingHorizontal:spacing.xl,paddingVertical:spacing.sm,borderRadius:radii.full,backgroundColor:colors.secondary},filters:{flexDirection:'row',flexWrap:'wrap',gap:spacing.sm},chip:{minHeight:38,justifyContent:'center',paddingHorizontal:spacing.md,borderRadius:radii.full,borderWidth:1,borderColor:colors.border,backgroundColor:colors.card},active:{borderColor:colors.primary,backgroundColor:colors.secondary},chipText:{fontFamily:fontFamily.semiBold,fontSize:11,color:colors.mutedForeground},activeText:{color:colors.primary},advanced:{gap:spacing.md,padding:spacing.lg,borderRadius:radii.xl,borderWidth:1,borderColor:colors.border,backgroundColor:colors.card},list:{gap:spacing.md},state:{minHeight:220,alignItems:'center',justifyContent:'center',gap:spacing.md},stateText:{fontFamily:fontFamily.medium,color:colors.mutedForeground},retry:{fontFamily:fontFamily.bold,color:colors.primary}});
