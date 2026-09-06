import { router, useLocalSearchParams } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { Alert, StyleSheet, Text } from 'react-native';

import { ResponsibleDetail } from '@/app/administration';
import { AppHeader } from '@/components/app-header';
import { LoadingState } from '@/components/loading-state';
import { ScreenContainer } from '@/components/screen-container';
import * as admin from '@/services/adminService';
import { ApiError } from '@/services/api';
import { colors, fontFamily, spacing } from '@/theme/tokens';
import type { AdminResponsibleDetails } from '@/types/admin';

export default function AdminResponsibleDetailScreen() {
  const params=useLocalSearchParams<{responsibleId?:string|string[];from?:string;profile?:string;status?:string}>();
  const responsibleId=Array.isArray(params.responsibleId)?params.responsibleId[0]:params.responsibleId;
  const [value,setValue]=useState<AdminResponsibleDetails|null>(null); const [reason,setReason]=useState('');
  const [feedback,setFeedback]=useState<string|null>(null); const [loading,setLoading]=useState(true); const [busy,setBusy]=useState(false);
  function returnToOrigin(){const profile=params.profile==='caregivers'?'caregivers':'responsibles';const status=params.status&&params.status!=='ALL'?params.status:undefined;router.replace({pathname:'/admin/approvals',params:{profile,...(status?{status}:{})}});}
  const load=useCallback(async()=>{if(!responsibleId){setFeedback('Responsável não identificado.');setLoading(false);return;}setLoading(true);setFeedback(null);try{setValue(await admin.getResponsible(responsibleId));}catch(error){setFeedback(error instanceof ApiError?error.message:'Não foi possível abrir o responsável.');}finally{setLoading(false);}},[responsibleId]);
  useEffect(()=>{void load();},[load]);
  async function execute(action:()=>Promise<AdminResponsibleDetails>,success:string){if(busy)return;setBusy(true);setFeedback(null);try{setValue(await action());setReason('');Alert.alert('Tudo certo',success);}catch(error){setFeedback(error instanceof ApiError?error.message:'Não foi possível concluir a ação.');}finally{setBusy(false);}}
  function requireReason(action:()=>Promise<AdminResponsibleDetails>,message:string){if(busy)return;if(!reason.trim()){setFeedback('Informe o motivo antes de continuar.');return;}Alert.alert('Confirmar ação',message,[{text:'Cancelar',style:'cancel'},{text:'Confirmar',style:'destructive',onPress:()=>void execute(action,'Situação atualizada com sucesso.')}]);}
  return <ScreenContainer contentStyle={styles.content} keyboardAvoiding safeAreaEdges={['top','right','bottom','left']}>
    <AppHeader compact showBack onBackPress={returnToOrigin} title="Análise do responsável" subtitle="Confira o cadastro completo antes de tomar uma decisão."/>
    {feedback?<Text style={styles.error}>{feedback}</Text>:null}{loading?<LoadingState message="Carregando cadastro..."/>:null}
    {!loading&&value?<ResponsibleDetail value={value} reason={reason} setReason={setReason} busy={busy} close={returnToOrigin}
      approve={()=>Alert.alert('Aprovar responsável?','O acesso do responsável será liberado e um e-mail será enviado.',[{text:'Cancelar',style:'cancel'},{text:'Aprovar',onPress:()=>void execute(()=>admin.approveResponsible(value.id),'Responsável aprovado e notificação por e-mail solicitada.')}])}
      reject={()=>requireReason(()=>admin.rejectResponsible(value.id,reason.trim()),'O responsável será reprovado e receberá o motivo por e-mail.')}
      block={()=>requireReason(()=>admin.blockResponsible(value.id,reason.trim()),'O perfil do responsável será bloqueado imediatamente.')}/>:null}
  </ScreenContainer>;
}

const styles=StyleSheet.create({content:{paddingHorizontal:spacing.xl,paddingBottom:spacing.xxxl,gap:spacing.lg},error:{fontFamily:fontFamily.medium,fontSize:13,lineHeight:20,color:colors.destructive}});
