import { router } from 'expo-router';
import { Clock3 } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';

export default function RegistrationPendingScreen() {
  return <ScreenContainer contentStyle={styles.content}>
    <AppHeader />
    <View style={styles.card}>
      <View style={styles.icon}><Clock3 color={colors.primary} size={34} /></View>
      <Text style={styles.title}>Cadastro enviado para análise</Text>
      <Text style={styles.description}>Você será avisado por e-mail quando a avaliação for concluída. Após a aprovação, o acesso às funcionalidades do seu perfil será liberado.</Text>
      <PrimaryButton label="Ir para o login" onPress={() => router.replace('/login')} />
    </View>
  </ScreenContainer>;
}

const styles = StyleSheet.create({
  content:{paddingHorizontal:spacing.xl,gap:spacing.xxl},
  card:{marginTop:spacing.xxl,padding:spacing.xl,gap:spacing.lg,backgroundColor:colors.card,borderColor:colors.border,borderWidth:1,borderRadius:radii.xxl},
  icon:{width:64,height:64,borderRadius:radii.full,backgroundColor:colors.secondary,alignItems:'center',justifyContent:'center',alignSelf:'center'},
  title:{fontFamily:fontFamily.extraBold,fontSize:24,lineHeight:31,color:colors.foreground,textAlign:'center'},
  description:{fontFamily:fontFamily.regular,fontSize:14,lineHeight:22,color:colors.mutedForeground,textAlign:'center'},
});
