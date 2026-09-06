import { router, useLocalSearchParams } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { Alert, StyleSheet, Text } from 'react-native';

import { CaregiverDetail } from '@/app/administration';
import { AppHeader } from '@/components/app-header';
import { LoadingState } from '@/components/loading-state';
import { ScreenContainer } from '@/components/screen-container';
import * as admin from '@/services/adminService';
import { ApiError } from '@/services/api';
import { colors, fontFamily, spacing } from '@/theme/tokens';
import type { AdminCaregiverDetails } from '@/types/admin';

export default function AdminCaregiverDetailScreen() {
  const params = useLocalSearchParams<{ caregiverId?: string | string[]; from?: string; profile?: string; status?: string }>();
  const caregiverId = Array.isArray(params.caregiverId) ? params.caregiverId[0] : params.caregiverId;
  const [value, setValue] = useState<AdminCaregiverDetails | null>(null);
  const [reason, setReason] = useState('');
  const [feedback, setFeedback] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);

  function returnToOrigin() {
    const profile = params.profile === 'responsibles' ? 'responsibles' : 'caregivers';
    const status = params.status && params.status !== 'ALL' ? params.status : undefined;
    router.replace({ pathname: '/admin/approvals', params: { profile, ...(status ? { status } : {}) } });
  }

  const load = useCallback(async () => {
    if (!caregiverId) {
      setFeedback('Cuidador não identificado.');
      setLoading(false);
      return;
    }
    setLoading(true);
    setFeedback(null);
    try { setValue(await admin.getCaregiver(caregiverId)); }
    catch (error) { setFeedback(error instanceof ApiError ? error.message : 'Não foi possível abrir o cuidador.'); }
    finally { setLoading(false); }
  }, [caregiverId]);

  useEffect(() => { void load(); }, [load]);

  async function execute(action: () => Promise<AdminCaregiverDetails>, success: string) {
    if (busy) return;
    setBusy(true); setFeedback(null);
    try { setValue(await action()); setReason(''); Alert.alert('Tudo certo', success); }
    catch (error) { setFeedback(error instanceof ApiError ? error.message : 'Não foi possível concluir a ação.'); }
    finally { setBusy(false); }
  }

  function requireReason(action: () => Promise<AdminCaregiverDetails>, message: string) {
    if (busy) return;
    if (!reason.trim()) { setFeedback('Informe o motivo antes de continuar.'); return; }
    Alert.alert('Confirmar ação', message, [
      { text: 'Cancelar', style: 'cancel' },
      { text: 'Confirmar', style: 'destructive', onPress: () => void execute(action, 'Situação atualizada com sucesso.') },
    ]);
  }

  return <ScreenContainer contentStyle={styles.content} keyboardAvoiding safeAreaEdges={['top','right','bottom','left']}>
    <AppHeader compact showBack onBackPress={returnToOrigin} title="Análise do cuidador" subtitle="Confira o cadastro completo antes de tomar uma decisão." />
    {feedback ? <Text style={styles.error}>{feedback}</Text> : null}
    {loading ? <LoadingState message="Carregando cadastro..." /> : null}
    {!loading && value ? <CaregiverDetail value={value} reason={reason} setReason={setReason} busy={busy} close={returnToOrigin}
      approve={() => Alert.alert('Aprovar cuidador?','O acesso profissional será liberado e um e-mail será enviado.',[
        {text:'Cancelar',style:'cancel'},
        {text:'Aprovar',onPress:()=>void execute(()=>admin.approveCaregiver(value.id),'Cuidador aprovado e notificação por e-mail solicitada.')},
      ])}
      reject={() => requireReason(() => admin.rejectCaregiver(value.id,reason.trim()),'O cuidador será reprovado e receberá o motivo por e-mail.')}
      block={() => requireReason(() => admin.blockCaregiver(value.id,reason.trim()),'O perfil profissional será bloqueado imediatamente.')}/>:null}
  </ScreenContainer>;
}

const styles=StyleSheet.create({
  content:{paddingHorizontal:spacing.xl,paddingBottom:spacing.xxxl,gap:spacing.lg},
  error:{fontFamily:fontFamily.medium,fontSize:13,lineHeight:20,color:colors.destructive},
});
