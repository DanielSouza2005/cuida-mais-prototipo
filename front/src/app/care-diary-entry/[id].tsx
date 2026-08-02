import { useCallback, useState } from 'react';
import { router, useFocusEffect, useLocalSearchParams } from 'expo-router';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { CarePhoto } from '@/components/care-photo';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { getManualCare } from '@/services/careTaskService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { CareDiaryItem } from '@/types/careTasks';
import { formatDateBR, formatDateTimeLocal, formatScheduleTime } from '@/utils/dateTime';

export default function CareDiaryEntryDetailsScreen() {
  const params = useLocalSearchParams<{ id: string }>();
  const id = Array.isArray(params.id) ? params.id[0] : params.id;
  const { user } = useAuth();
  const caregiver = user?.userType === 'caregiver';
  const [item, setItem] = useState<CareDiaryItem>();
  const [error, setError] = useState(false);

  const load = useCallback(async () => {
    if (!id) return;
    setError(false);
    try { setItem(await getManualCare(id, caregiver)); } catch { setError(true); }
  }, [caregiver, id]);
  useFocusEffect(useCallback(() => { void load(); }, [load]));

  if (error) return <ScreenContainer contentStyle={styles.center}><Text style={styles.error}>Não foi possível abrir este cuidado avulso.</Text><PrimaryButton label="Voltar" variant="secondary" onPress={() => router.back()} /></ScreenContainer>;
  if (!item) return <ScreenContainer contentStyle={styles.center}><Text style={styles.muted}>Carregando cuidado...</Text></ScreenContainer>;

  return <ScreenContainer contentStyle={styles.content}>
    <AppHeader showBack title="Detalhes do cuidado" subtitle={item.title} />
    <View style={styles.badges}><View style={styles.sourceBadge}><Text style={styles.sourceText}>Avulso</Text></View><View style={styles.statusBadge}><Text style={styles.statusText}>{item.statusLabel}</Text></View></View>
    <Section title="Cuidado avulso">
      <Info label="Data do cuidado" value={formatDateBR(item.date)} />
      <Info label="Horário em que ocorreu" value={formatScheduleTime(item.time)} />
      <Info label="Tipo de cuidado" value={item.careTypeLabel} />
      <Info label="Título" value={item.title} />
      <Info label="Descrição do cuidado" value={item.description} />
      {item.notes ? <Info label="Observações adicionais" value={item.notes} /> : null}
      {item.important ? <Info label="Importância" value="Anotação importante" /> : null}
    </Section>
    <Section title="Vínculos do atendimento">
      <Info label="Pessoa assistida" value={item.assistedPersonName} />
      <Info label="Cuidador(a)" value={item.caregiverName} />
      <Info label="Contratação" value="Vinculada ao atendimento do dia" />
      {item.registeredAt ? <Info label="Data de registro" value={formatDateTimeLocal(item.registeredAt)} /> : null}
    </Section>
    {item.photos.length ? <Section title="Fotos/anexos"><View style={styles.photoGrid}>{item.photos.map((photo) => <CarePhoto key={photo.id} url={photo.url} />)}</View></Section> : null}
    <View style={styles.readOnly}><Text style={styles.readOnlyText}>{caregiver ? 'Este cuidado fica disponível apenas para consulta após o registro.' : 'Registro disponível somente para leitura.'}</Text></View>
  </ScreenContainer>;
}

function Section({ title, children }: { title: string; children: React.ReactNode }) { return <View style={styles.section}><Text style={styles.sectionTitle}>{title}</Text>{children}</View>; }
function Info({ label, value }: { label: string; value?: string }) { if (!value?.trim()) return null; return <View style={styles.info}><Text style={styles.infoLabel}>{label}</Text><Text style={styles.infoValue}>{value}</Text></View>; }
const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, gap: spacing.lg }, center: { flexGrow: 1, alignItems: 'center', justifyContent: 'center', gap: spacing.md, padding: spacing.xl }, error: { textAlign: 'center', fontFamily: fontFamily.bold, color: colors.destructive }, muted: { fontFamily: fontFamily.medium, color: colors.mutedForeground },
  badges: { flexDirection: 'row', gap: spacing.sm }, sourceBadge: { paddingHorizontal: spacing.md, paddingVertical: spacing.sm, borderRadius: radii.full, backgroundColor: '#E9F7EE' }, sourceText: { fontFamily: fontFamily.bold, fontSize: 11, color: '#287A4B' }, statusBadge: { paddingHorizontal: spacing.md, paddingVertical: spacing.sm, borderRadius: radii.full, backgroundColor: colors.secondary }, statusText: { fontFamily: fontFamily.bold, fontSize: 11, color: colors.primary },
  section: { gap: spacing.md, padding: spacing.lg, borderRadius: radii.xl, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card, ...shadows.card }, sectionTitle: { fontFamily: fontFamily.extraBold, fontSize: 17, color: colors.foreground }, info: { gap: spacing.xxs }, infoLabel: { fontFamily: fontFamily.semiBold, fontSize: 11, color: colors.mutedForeground }, infoValue: { fontFamily: fontFamily.medium, fontSize: 13, lineHeight: 20, color: colors.foreground }, photoGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm }, readOnly: { padding: spacing.md, borderRadius: radii.lg, backgroundColor: colors.secondary }, readOnlyText: { textAlign: 'center', fontFamily: fontFamily.semiBold, fontSize: 12, color: colors.secondaryForeground },
});
