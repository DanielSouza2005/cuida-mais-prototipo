import { CalendarClock, Camera, Clock3, UserRound } from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { TaskPriorityBadge, TaskStatusBadge } from '@/components/task-badges';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { CareDiaryItem, CareTask, TaskOccurrence } from '@/types/careTasks';
import { taskCategoryName, taskRecurrenceLabels } from '@/utils/careTaskLabels';
import { formatDateBR, formatScheduleTime } from '@/utils/dateTime';

export function CareTaskCard({ task, onPress }: { task: CareTask; onPress: () => void }) {
  return <Pressable accessibilityRole="button" onPress={onPress} style={({ pressed }) => [styles.card, pressed && styles.pressed]}>
    <View style={styles.top}><TaskStatusBadge status={task.status} /><TaskPriorityBadge priority={task.priority} /></View>
    <Text style={styles.title}>{task.title}</Text><Text style={styles.category}>{taskCategoryName(task.category, task.customCategory)} · {taskRecurrenceLabels[task.recurrenceType]}</Text>
    <Line icon={UserRound} text={`${task.assistedPersonName} · ${task.caregiverName}`} />
    <Line icon={Clock3} text={formatScheduleTime(task.scheduledTime)} />
    <Line icon={CalendarClock} text={task.nextOccurrenceDate ? `Próxima: ${formatDateBR(task.nextOccurrenceDate)} às ${formatScheduleTime(task.nextOccurrenceTime)}` : 'Sem próxima ocorrência'} />
  </Pressable>;
}

export function TaskOccurrenceCard({ occurrence, onPress, readOnly=false }: { occurrence: TaskOccurrence; onPress: () => void; readOnly?: boolean }) {
  return <Pressable accessibilityRole="button" onPress={onPress} style={({ pressed }) => [styles.card, occurrence.status==='ATRASADA'&&styles.overdue, pressed && styles.pressed]}>
    <View style={styles.top}><TaskStatusBadge status={occurrence.status} /><TaskPriorityBadge priority={occurrence.priority} /></View>
    <Text style={styles.time}>{formatScheduleTime(occurrence.scheduledTime)}</Text><Text style={styles.title}>{occurrence.important?'★ ':''}{occurrence.title}</Text>
    <Text style={styles.category}>{taskCategoryName(occurrence.category, occurrence.customCategory)}</Text>
    <Line icon={UserRound} text={occurrence.assistedPersonName} />
    {readOnly?<Line icon={UserRound} text={`Cuidador(a): ${occurrence.caregiverName}`}/>:null}
    {occurrence.photos?.length?<Line icon={Camera} text="Com foto"/>:null}
    {occurrence.description ? <Text numberOfLines={2} style={styles.description}>{occurrence.description}</Text> : null}
    {readOnly?<Text style={styles.details}>Ver detalhes</Text>:null}
  </Pressable>;
}

export function CareDiaryItemCard({ item, onPress, readOnly=false }: { item: CareDiaryItem; onPress: () => void; readOnly?: boolean }) {
  return <Pressable accessibilityRole="button" onPress={onPress} style={({ pressed }) => [styles.card, item.status==='ATRASADA'&&styles.overdue, pressed&&styles.pressed]}>
    <View style={styles.top}><View style={[styles.sourceBadge,item.sourceType==='MANUAL'&&styles.manualBadge]}><Text style={[styles.sourceText,item.sourceType==='MANUAL'&&styles.manualText]}>{item.sourceLabel}</Text></View><View style={styles.statusBadge}><Text style={styles.statusText}>{item.statusLabel}</Text></View></View>
    <Text style={styles.time}>{formatScheduleTime(item.time)}</Text><Text style={styles.title}>{item.important?'★ ':''}{item.title}</Text>
    <Text style={styles.category}>{item.careTypeLabel}</Text><Line icon={UserRound} text={item.assistedPersonName}/>
    {readOnly?<Line icon={UserRound} text={`Cuidador(a): ${item.caregiverName}`}/>:null}
    {item.photos?.length?<Line icon={Camera} text="Com foto"/>:null}
    {item.description?<Text numberOfLines={2} style={styles.description}>{item.description}</Text>:null}
    <Text style={styles.details}>Ver detalhes</Text>
  </Pressable>;
}

function Line({ icon: Icon, text }: { icon: typeof Clock3; text: string }) { return <View style={styles.line}><Icon color={colors.mutedForeground} size={14} /><Text style={styles.lineText}>{text}</Text></View>; }
const styles = StyleSheet.create({
  card: { gap: spacing.sm, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card }, overdue:{borderColor:colors.destructive,backgroundColor:'#FFF5F3'},
  pressed: { opacity: 0.82, transform: [{ scale: 0.99 }] }, top: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: spacing.sm },
  title: { fontFamily: fontFamily.extraBold, fontSize: 16, color: colors.foreground }, time: { fontFamily: fontFamily.extraBold, fontSize: 19, color: colors.primary },
  category: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.primary }, line: { flexDirection: 'row', alignItems: 'center', gap: spacing.xs },
  lineText: { flex: 1, fontFamily: fontFamily.medium, fontSize: 11, color: colors.mutedForeground }, description: { fontFamily: fontFamily.regular, fontSize: 12, lineHeight: 18, color: colors.secondaryForeground }, details:{fontFamily:fontFamily.bold,fontSize:12,color:colors.primary},
  sourceBadge:{paddingHorizontal:spacing.sm,paddingVertical:spacing.xs,borderRadius:radii.full,backgroundColor:colors.secondary},manualBadge:{backgroundColor:'#E9F7EE'},sourceText:{fontFamily:fontFamily.bold,fontSize:10,color:colors.primary},manualText:{color:'#287A4B'},statusBadge:{paddingHorizontal:spacing.sm,paddingVertical:spacing.xs,borderRadius:radii.full,backgroundColor:colors.muted},statusText:{fontFamily:fontFamily.bold,fontSize:10,color:colors.secondaryForeground},
});
