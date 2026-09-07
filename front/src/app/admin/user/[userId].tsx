import { router, useLocalSearchParams } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { Alert, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { LoadingState } from '@/components/loading-state';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import * as admin from '@/services/adminService';
import { ApiError } from '@/services/api';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { AdminUserDetails } from '@/types/admin';

const date=(value?:string|null)=>value?new Intl.DateTimeFormat('pt-BR',{dateStyle:'short',timeStyle:'short'}).format(new Date(value)):'Não informado';

export default function AdminUserDetailScreen(){
  const {user}=useAuth(); const params=useLocalSearchParams<{userId?:string|string[];from?:string;status?:string}>();
  const userId=Array.isArray(params.userId)?params.userId[0]:params.userId;
  const [value,setValue]=useState<AdminUserDetails|null>(null); const [reason,setReason]=useState('');
  const [feedback,setFeedback]=useState<string|null>(null); const [loading,setLoading]=useState(true); const [busy,setBusy]=useState(false);
  const returnToOrigin=()=>{const status=params.status&&params.status!=='ALL'?params.status:undefined;router.replace({pathname:'/admin/users',params:{...(status?{status}:{})}});};
  const load=useCallback(async()=>{if(!userId){setFeedback('Usuário não identificado.');setLoading(false);return;}setLoading(true);setFeedback(null);try{setValue(await admin.getUser(userId));}catch(error){setFeedback(error instanceof ApiError?error.message:'Não foi possível abrir o usuário.');}finally{setLoading(false);}},[userId]);
  useEffect(()=>{void load();},[load]);
  async function execute(action:()=>Promise<AdminUserDetails>,success:string,fallback:string){if(busy)return;setBusy(true);setFeedback(null);try{setValue(await action());setReason('');Alert.alert('Tudo certo',success);}catch(error){setFeedback(error instanceof ApiError?error.message:fallback);}finally{setBusy(false);}}
  function confirmBlock(){if(busy)return;if(!reason.trim()){setFeedback('Informe o motivo antes de continuar.');return;}Alert.alert('Bloquear usuário?','O usuário perderá o acesso ao sistema.',[{text:'Cancelar',style:'cancel'},{text:'Bloquear',style:'destructive',onPress:()=>value&&void execute(()=>admin.blockUser(value.id,reason.trim()),'Usuário bloqueado com sucesso.','Não foi possível bloquear o usuário.')}]);}
  function confirmUnblock(){if(busy||!value)return;Alert.alert('Deseja desbloquear este usuário?','O acesso será restaurado somente se o cadastro do perfil também estiver aprovado.',[{text:'Cancelar',style:'cancel'},{text:'Desbloquear',onPress:()=>void execute(()=>admin.unblockUser(value.id),'Usuário desbloqueado com sucesso.','Não foi possível desbloquear o usuário.')}]);}
  return <ScreenContainer contentStyle={styles.content} keyboardAvoiding safeAreaEdges={['top','right','bottom','left']}>
    <AppHeader compact showBack onBackPress={returnToOrigin} title="Detalhes do usuário" subtitle="Consulte os dados da conta e controle o acesso ao sistema."/>
    {feedback?<Text style={styles.error}>{feedback}</Text>:null}{loading?<LoadingState message="Carregando usuário..."/>:null}
    {!loading&&value?<View style={styles.card}><Info label="Nome" value={value.name}/><Info label="E-mail" value={value.email}/><Info label="Telefone" value={value.phone}/><Info label="Perfil" value={value.profileLabel}/><Info label="Situação da conta" value={value.accountStatusLabel}/><Info label="Último acesso" value={date(value.lastLoginAt)}/><Info label="Cadastro" value={date(value.createdAt)}/>{value.accountBlockReason?<Info label="Motivo do bloqueio" value={value.accountBlockReason}/>:null}
      {value.accountStatus==='BLOQUEADO'?<PrimaryButton label="Desbloquear usuário" loading={busy} onPress={confirmUnblock}/>:value.accountStatus==='ATIVO'&&value.id===user?.id?<Text style={styles.note}>Você não pode bloquear a própria conta administrativa.</Text>:value.accountStatus==='ATIVO'?<><AppTextInput label="Motivo do bloqueio" placeholder="Motivo breve, sem dados pessoais ou assistenciais" value={reason} onChangeText={setReason} multiline/><PrimaryButton label="Bloquear usuário" loading={busy} onPress={confirmBlock}/></>:<Text style={styles.note}>Contas inativas não possuem ações administrativas disponíveis.</Text>}
    </View>:null}
  </ScreenContainer>;
}

function Info({label,value}:{label:string;value?:string|null}){return <View style={styles.info}><Text style={styles.infoLabel}>{label}</Text><Text style={styles.infoValue}>{value||'Não informado'}</Text></View>}
const styles=StyleSheet.create({content:{paddingHorizontal:spacing.xl,paddingBottom:spacing.xxxl,gap:spacing.lg},card:{padding:spacing.xl,gap:spacing.md,borderRadius:radii.xxl,borderWidth:1,borderColor:colors.adminBorder,backgroundColor:colors.card,...shadows.card},info:{gap:spacing.xxs},infoLabel:{fontFamily:fontFamily.bold,fontSize:12,color:colors.mutedForeground},infoValue:{fontFamily:fontFamily.medium,fontSize:14,lineHeight:21,color:colors.foreground},error:{fontFamily:fontFamily.medium,fontSize:13,lineHeight:20,color:colors.destructive},note:{fontFamily:fontFamily.medium,fontSize:14,lineHeight:21,textAlign:'center',color:colors.mutedForeground,padding:spacing.lg}});
