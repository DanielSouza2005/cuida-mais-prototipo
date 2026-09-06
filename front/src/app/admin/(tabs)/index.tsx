import { router, type Href } from 'expo-router';
import { ClipboardCheck, ShieldAlert, UserRoundCheck, Users } from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { ProfileTypeBadge } from '@/components/profile-type-badge';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

export default function AdminHomeScreen(){
  const {user}=useAuth();
  const firstName=user?.fullName?.trim().split(/\s+/)[0]||'administrador';
  return <ScreenContainer contentStyle={styles.content}><AppHeader/><View style={styles.greeting}><View style={styles.greetingCopy}><Text style={styles.title}>Olá, {firstName}</Text><Text style={styles.subtitle}>Acesse as principais ferramentas administrativas.</Text></View><ProfileTypeBadge type="ADMIN" label="ADM"/></View>
    <Text style={styles.sectionTitle}>Acesso rápido</Text><View style={styles.shortcuts}>
      <Shortcut title="Cuidadores pendentes" description="Analisar cadastros profissionais" icon={UserRoundCheck} tone="purple" onPress={()=>router.push({pathname:'/admin/approvals',params:{profile:'caregivers',status:'PENDENTE'}})}/>
      <Shortcut title="Responsáveis pendentes" description="Analisar responsáveis cadastrados" icon={ClipboardCheck} tone="mint" onPress={()=>router.push({pathname:'/admin/approvals',params:{profile:'responsibles',status:'PENDENTE'}})}/>
      <Shortcut title="Contas bloqueadas" description="Consultar e desbloquear contas" icon={ShieldAlert} tone="coral" onPress={()=>router.push({pathname:'/admin/users',params:{status:'BLOQUEADO'}})}/>
      <Shortcut title="Todos os usuários" description="Pesquisar a base de usuários" icon={Users} tone="blue" onPress={()=>router.push('/admin/users' as Href)}/>
    </View>
  </ScreenContainer>;
}
type Tone='purple'|'mint'|'blue'|'coral';
const toneBackground:Record<Tone,string>={purple:colors.adminBackground,mint:colors.mint,blue:colors.secondary,coral:colors.coralBackground};
const toneForeground:Record<Tone,string>={purple:colors.adminForeground,mint:colors.mintForeground,blue:colors.primary,coral:colors.coral};
function Shortcut({title,description,icon:Icon,onPress,tone}:{title:string;description:string;icon:typeof Users;onPress:()=>void;tone:Tone}){return <Pressable onPress={onPress} style={({pressed})=>[styles.shortcut,pressed&&styles.pressed]}><View style={[styles.icon,{backgroundColor:toneBackground[tone]}]}><Icon color={toneForeground[tone]} size={21}/></View><View style={styles.grow}><Text style={styles.shortcutTitle}>{title}</Text><Text style={styles.shortcutDescription}>{description}</Text></View></Pressable>}
const styles=StyleSheet.create({content:{paddingHorizontal:spacing.xl,paddingBottom:spacing.xxl,gap:spacing.xl},greeting:{flexDirection:'row',alignItems:'center',gap:spacing.md},greetingCopy:{flex:1},title:{fontFamily:fontFamily.extraBold,fontSize:26,color:colors.foreground},subtitle:{marginTop:spacing.xs,fontFamily:fontFamily.regular,fontSize:14,lineHeight:21,color:colors.mutedForeground},sectionTitle:{fontFamily:fontFamily.extraBold,fontSize:18,color:colors.foreground},shortcuts:{gap:spacing.md},shortcut:{flexDirection:'row',alignItems:'center',gap:spacing.md,padding:spacing.lg,borderRadius:radii.xl,borderWidth:1,borderColor:colors.border,backgroundColor:colors.card,...shadows.card},pressed:{opacity:.82,transform:[{scale:.99}]},icon:{width:44,height:44,alignItems:'center',justifyContent:'center',borderRadius:radii.lg},grow:{flex:1},shortcutTitle:{fontFamily:fontFamily.bold,fontSize:14,color:colors.foreground},shortcutDescription:{marginTop:spacing.xxs,fontFamily:fontFamily.regular,fontSize:12,lineHeight:18,color:colors.mutedForeground}});
