import { StyleSheet, Text, View } from 'react-native';

import { colors, fontFamily, radii, spacing } from '@/theme/tokens';
import type { CareRoutineItem } from '@/types/careRoutine';
import { medicationRouteLabels, medicationUnitLabels, taskPriorityLabels, taskWeekdayLabels } from '@/utils/careTaskLabels';
import { formatScheduleTime } from '@/utils/dateTime';

const recurrenceLabels = { UNICA:'Uma vez',DIARIA:'Diariamente',DIAS_ESPECIFICOS:'Em dias específicos',INTERVALO:'Em intervalo de dias',PERIODO_DETERMINADO:'Durante o período combinado',SEM_DATA_FINAL:'Recorrente' } as const;

export function CareRoutineItemDetails({item,index,compact=false}:{item:CareRoutineItem;index?:number;compact?:boolean}){
  const medication=item.medication;
  return <View style={[styles.card,compact&&styles.compact]}>
    <View style={styles.heading}>{index!==undefined?<Text style={styles.order}>{index+1}</Text>:null}<View style={styles.flex}><Text style={styles.category}>{item.categoryLabel??'Cuidado'}</Text><Text style={styles.title}>{item.title}</Text></View></View>
    {item.description?<Text style={styles.description}>{item.description}</Text>:null}
    {item.priority?<Line label="Prioridade" value={taskPriorityLabels[item.priority]}/>:null}
    {item.recurrenceType?<Line label="Frequência" value={recurrenceLabels[item.recurrenceType]}/>:null}
    {item.scheduledTime?<Line label="Horário previsto" value={formatScheduleTime(item.scheduledTime)}/>:null}
    {item.recurrenceType==='INTERVALO'&&item.intervalDays?<Line label="Intervalo" value={`A cada ${item.intervalDays} dias`}/>:null}
    {item.weekdays?.length?<Line label="Dias" value={item.weekdays.map(day=>taskWeekdayLabels[day]).join(', ')}/>:null}
    {medication?<View style={styles.medication}><Text style={styles.medicationTitle}>Dados da medicação</Text><Line label="Medicamento" value={medication.name}/><Line label="Dosagem" value={`${medication.dosage} ${medication.unit==='PERSONALIZADA'?(medication.customUnit??''):medicationUnitLabels[medication.unit]}`.trim()}/><Line label="Forma de administração" value={medication.administrationRoute==='OUTRA'?(medication.customAdministrationRoute??'Outra'):medicationRouteLabels[medication.administrationRoute]}/>{medication.additionalInstructions?<Line label="Orientações" value={medication.additionalInstructions}/>:null}</View>:null}
    {item.notes?<Line label="Observações" value={item.notes}/>:null}
    {item.reminderEnabled?<View style={styles.medication}><Text style={styles.medicationTitle}>Lembretes</Text>{item.reminderMinutesBefore?<Line label="Avisar antes" value={`${item.reminderMinutesBefore} minutos antes`}/>:null}{item.reminderAtScheduledTime?<Line label="No horário previsto" value="Ativado"/>:null}{item.overdueReminderEnabled?<Line label="Aviso de atraso" value={`Após ${item.overdueAfterMinutes??0} minutos`}/>:null}{item.repeatWhilePending?<Line label="Repetir enquanto pendente" value={`A cada ${item.repeatIntervalMinutes??0} minutos`}/>:null}{item.important?<Line label="Importância" value="Cuidado importante"/>:null}{item.notifyResponsibleIfImportant?<Line label="Acompanhamento" value="Responsável será avisado em caso de atraso ou não realização"/>:null}</View>:null}
    {item.requiresCompletionPhoto?<Line label="Comprovação" value="Foto obrigatória para concluir"/>:null}
  </View>;
}
function Line({label,value}:{label:string;value:string}){return <View style={styles.line}><Text style={styles.label}>{label}</Text><Text style={styles.value}>{value}</Text></View>}
const styles=StyleSheet.create({card:{gap:spacing.sm,padding:spacing.md,borderRadius:radii.lg,backgroundColor:colors.muted},compact:{padding:spacing.sm},heading:{flexDirection:'row',gap:spacing.sm,alignItems:'center'},order:{width:26,height:26,textAlign:'center',textAlignVertical:'center',borderRadius:radii.full,backgroundColor:colors.secondary,fontFamily:fontFamily.bold,color:colors.primary},flex:{flex:1},category:{fontFamily:fontFamily.bold,fontSize:10,color:colors.primary,textTransform:'uppercase'},title:{fontFamily:fontFamily.bold,fontSize:14,color:colors.foreground},description:{fontFamily:fontFamily.regular,fontSize:12,lineHeight:18,color:colors.mutedForeground},line:{gap:spacing.xxs},label:{fontFamily:fontFamily.semiBold,fontSize:10,color:colors.mutedForeground},value:{fontFamily:fontFamily.medium,fontSize:12,lineHeight:18,color:colors.foreground},medication:{gap:spacing.sm,padding:spacing.md,borderRadius:radii.md,backgroundColor:'#FFF9E5'},medicationTitle:{fontFamily:fontFamily.bold,fontSize:12,color:'#755B00'}});
