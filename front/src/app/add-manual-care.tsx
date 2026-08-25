import { useEffect, useMemo, useState } from 'react';
import { router, useLocalSearchParams, type Href } from 'expo-router';
import * as ImagePicker from 'expo-image-picker';
import { Camera, ImagePlus, Trash2 } from 'lucide-react-native';
import { Alert, Image, Pressable, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { DatePickerField } from '@/components/date-picker-field';
import { OptionGroup } from '@/components/option-group';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { TimePickerField } from '@/components/time-picker-field';
import { useBlockNavigationWhenBusy } from '@/hooks/useBlockNavigationWhenBusy';
import { ApiError } from '@/services/api';
import { createManualCare, getManualCareFormData } from '@/services/careTaskService';
import { getContractAttendance } from '@/services/serviceAttendanceService';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';
import type { CareCompletionPhoto, ManualCareContractOption, ManualCareType } from '@/types/careTasks';
import type { AttendanceSummary } from '@/types/serviceAttendance';
import { todayDateOnly } from '@/utils/agendaDate';
import { deviceTimezone } from '@/utils/careTaskLabels';
import { displayDateToIso, isoDateToDisplay } from '@/utils/contractTerminationLabels';

const MAX_PHOTOS = 5;
const MAX_PHOTO_SIZE = 5 * 1024 * 1024;
const SUPPORTED_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp']);
const careTypes: { value: ManualCareType; label: string }[] = [
  { value: 'MEDICACAO', label: 'Medicação' }, { value: 'ALIMENTACAO', label: 'Alimentação' },
  { value: 'HIGIENE', label: 'Higiene' }, { value: 'MOBILIDADE', label: 'Mobilidade' },
  { value: 'COMPANHIA', label: 'Companhia' }, { value: 'OBSERVACAO', label: 'Observação' },
  { value: 'OCORRENCIA', label: 'Ocorrência' }, { value: 'OUTRO', label: 'Outro' },
];

export default function AddManualCareScreen() {
  const params = useLocalSearchParams<{ contractId?: string; date?: string }>();
  const initialEntryDate = typeof params.date === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(params.date) ? params.date : todayDateOnly();
  const contextualContractId = typeof params.contractId === 'string' ? params.contractId : '';
  const [entryDate, setEntryDate] = useState(initialEntryDate);
  const [contracts, setContracts] = useState<ManualCareContractOption[] | null>(null);
  const [contractId, setContractId] = useState('');
  const [occurredTime, setOccurredTime] = useState(initialEntryDate === todayDateOnly() ? currentTime() : '');
  const [careType, setCareType] = useState<ManualCareType | null>(null);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [notes, setNotes] = useState('');
  const [important, setImportant] = useState<'NAO' | 'SIM'>('NAO');
  const [photos, setPhotos] = useState<CareCompletionPhoto[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [loadError, setLoadError] = useState(false);
  const [attendance, setAttendance] = useState<AttendanceSummary | null>(null);
  const [attendanceLoading, setAttendanceLoading] = useState(false);
  useBlockNavigationWhenBusy(saving);

  useEffect(() => {
    let active = true;
    setLoading(true); setLoadError(false);
    getManualCareFormData(entryDate).then((result) => {
      if (!active) return;
      setContracts(result.contracts);
      const contextualContract = result.contracts.find((item) => item.contractId === contextualContractId);
      if (contextualContract) setContractId(contextualContract.contractId);
      else if (result.contracts.length === 1) setContractId(result.contracts[0].contractId);
      else setContractId((current) => result.contracts.some((item) => item.contractId === current) ? current : '');
    }).catch(() => { if (active) setLoadError(true); }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [contextualContractId, entryDate]);

  const selectedContract = useMemo(() => contracts?.find((item) => item.contractId === contractId), [contractId, contracts]);
  const attendanceActive = attendance?.status === 'IN_PROGRESS' || attendance?.status === 'CAN_END';

  useEffect(() => {
    let active = true;
    if (!contractId) { setAttendance(null); return () => { active = false; }; }
    setAttendanceLoading(true);
    getContractAttendance(contractId, entryDate)
      .then((result) => { if (active) setAttendance(result); })
      .catch(() => { if (active) setAttendance(null); })
      .finally(() => { if (active) setAttendanceLoading(false); });
    return () => { active = false; };
  }, [contractId, entryDate]);

  async function save() {
    if (saving) return;
    if (!selectedContract) return Alert.alert('Dados incompletos', 'Informe a contratação.');
    if (!attendanceActive) return Alert.alert('Atendimento não disponível', attendance?.status === 'ENDED' ? 'Este atendimento já foi encerrado. Não é possível registrar novos cuidados.' : 'Você precisa iniciar o atendimento antes de registrar cuidados.');
    if (!occurredTime.trim()) return Alert.alert('Dados incompletos', 'Informe o horário em que o cuidado ocorreu.');
    if (!isValidTime(occurredTime)) return Alert.alert('Horário inválido', 'Informe o horário no formato HH:mm.');
    if (!careType) return Alert.alert('Dados incompletos', 'Informe o tipo do cuidado.');
    if (!title.trim()) return Alert.alert('Dados incompletos', 'Informe o título do cuidado.');
    if (!description.trim()) return Alert.alert('Dados incompletos', 'Descreva o cuidado realizado ou observado.');
    setSaving(true);
    try {
      const created = await createManualCare({ contractId, assistedPersonId: selectedContract.assistedPersonId, entryDate, occurredTime, careType, title: title.trim(), description: description.trim(), notes: notes.trim() || undefined, timezone: deviceTimezone(), important: important === 'SIM', photos });
      Alert.alert('Cuidado registrado', 'Cuidado avulso registrado com sucesso.');
      router.replace(`/care-diary-entry/${created.id}` as Href);
    } catch (cause) {
      Alert.alert('Não foi possível registrar o cuidado avulso', cause instanceof ApiError ? cause.message : 'Tente novamente.');
    } finally { setSaving(false); }
  }

  function addAssets(assets: ImagePicker.ImagePickerAsset[]) {
    const selected = assets.slice(0, MAX_PHOTOS - photos.length);
    if (selected.some((asset) => Boolean(asset.fileSize && asset.fileSize > MAX_PHOTO_SIZE) || Boolean(asset.mimeType && !SUPPORTED_TYPES.has(asset.mimeType)))) {
      Alert.alert('Foto inválida', 'Use imagens JPEG, PNG ou WebP com até 5 MB.'); return;
    }
    setPhotos((current) => [...current, ...selected.map((asset, index) => {
      const type = (asset.mimeType && SUPPORTED_TYPES.has(asset.mimeType) ? asset.mimeType : 'image/jpeg') as CareCompletionPhoto['type'];
      const extension = type === 'image/png' ? 'png' : type === 'image/webp' ? 'webp' : 'jpg';
      return { uri: asset.uri, name: asset.fileName ?? `cuidado-avulso-${Date.now()}-${index}.${extension}`, type, file: asset.file };
    })]);
  }
  async function takePhoto() {
    const permission = await ImagePicker.requestCameraPermissionsAsync();
    if (!permission.granted) return Alert.alert('Permissão necessária', 'Permita o acesso à câmera para tirar a foto.');
    const result = await ImagePicker.launchCameraAsync({ mediaTypes: ['images'], quality: 0.8, cameraType: ImagePicker.CameraType.back });
    if (!result.canceled) addAssets(result.assets);
  }
  async function choosePhotos() {
    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permission.granted) return Alert.alert('Permissão necessária', 'Permita o acesso à galeria para escolher fotos.');
    const result = await ImagePicker.launchImageLibraryAsync({ mediaTypes: ['images'], allowsMultipleSelection: true, selectionLimit: MAX_PHOTOS - photos.length, quality: 0.8 });
    if (!result.canceled) addAssets(result.assets);
  }

  return <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
    <AppHeader showBack backDisabled={saving} title="Adicionar cuidado avulso" subtitle="Registre um cuidado que não estava previsto para este dia." />
    {loading ? <Text style={styles.state}>Carregando contratações...</Text> : loadError ? <Text style={styles.error}>Não foi possível carregar as contratações disponíveis.</Text> : contracts?.length === 0 ? <Text style={styles.state}>Não há contratação válida para registrar cuidados nesta data.</Text> : <>
      <OptionGroup required disabled={saving || Boolean(contextualContractId && selectedContract)} label="Contratação/atendimento" options={(contracts ?? []).map((item) => ({ value: item.contractId, label: item.contractLabel }))} value={contractId || null} onChange={(value) => setContractId(value as string)} />
      <AppTextInput required label="Pessoa assistida" value={selectedContract?.assistedPersonName ?? ''} editable={false} placeholder="Selecione a contratação" />
      {selectedContract ? <View style={attendanceActive ? styles.attendanceActive : styles.attendanceBlocked}><Text style={attendanceActive ? styles.attendanceActiveText : styles.attendanceBlockedText}>{attendanceLoading ? 'Verificando atendimento...' : attendanceActive ? 'Atendimento iniciado. O registro de cuidados está liberado.' : attendance?.status === 'ENDED' ? 'Este atendimento já foi encerrado.' : 'Inicie o atendimento antes de registrar um cuidado avulso.'}</Text></View> : null}
      <DatePickerField required disabled={saving || Boolean(contextualContractId)} label="Data do cuidado" value={isoDateToDisplay(entryDate)} onChange={(value) => setEntryDate(displayDateToIso(value))} maximumDate={new Date()} />
      <TimePickerField required disabled={saving} label="Horário em que ocorreu" value={occurredTime} onChange={setOccurredTime} />
      <OptionGroup required disabled={saving} label="Tipo de cuidado" options={careTypes} value={careType} onChange={(value) => setCareType(value as ManualCareType)} />
      <AppTextInput required disabled={saving} label="Título do cuidado" value={title} onChangeText={setTitle} maxLength={180} />
      <AppTextInput required disabled={saving} label="Descrição do cuidado" value={description} onChangeText={setDescription} multiline maxLength={2000} />
      <AppTextInput optional disabled={saving} label="Observações adicionais" value={notes} onChangeText={setNotes} multiline maxLength={1000} />
      <OptionGroup disabled={saving} label="Anotação importante" options={[{ value: 'NAO', label: 'Não' }, { value: 'SIM', label: 'Sim' }]} value={important} onChange={(value) => setImportant(value as 'NAO' | 'SIM')} />
      <View style={styles.photoSection}><Text style={styles.label}>Fotos/anexos (opcional)</Text><Text style={styles.hint}>{photos.length} de {MAX_PHOTOS} fotos</Text><View style={styles.photoActions}><Pressable disabled={saving || photos.length >= MAX_PHOTOS} onPress={() => void takePhoto()} style={styles.photoButton}><Camera color={colors.primary} size={18} /><Text style={styles.photoButtonText}>Tirar foto</Text></Pressable><Pressable disabled={saving || photos.length >= MAX_PHOTOS} onPress={() => void choosePhotos()} style={styles.photoButton}><ImagePlus color={colors.primary} size={18} /><Text style={styles.photoButtonText}>Escolher da galeria</Text></Pressable></View>{photos.length ? <View style={styles.photoGrid}>{photos.map((photo, index) => <View key={`${photo.uri}-${index}`}><Image source={{ uri: photo.uri }} style={styles.preview} /><Pressable accessibilityLabel="Remover foto" disabled={saving} onPress={() => setPhotos((current) => current.filter((_, itemIndex) => itemIndex !== index))} style={styles.remove}><Trash2 color="#FFFFFF" size={16} /></Pressable></View>)}</View> : null}</View>
      <PrimaryButton label={saving ? 'Salvando...' : 'Salvar cuidado'} loading={saving} disabled={saving || attendanceLoading || !attendanceActive} onPress={() => void save()} />
      <PrimaryButton label="Cancelar" variant="secondary" disabled={saving} onPress={() => router.back()} />
    </>}
  </ScreenContainer>;
}

function currentTime() { const now = new Date(); return `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`; }
function isValidTime(value: string) { if (!/^\d{2}:\d{2}$/.test(value)) return false; const [hours, minutes] = value.split(':').map(Number); return hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59; }
const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.xl, gap: spacing.lg }, state: { textAlign: 'center', fontFamily: fontFamily.medium, color: colors.mutedForeground }, error: { textAlign: 'center', fontFamily: fontFamily.bold, color: colors.destructive },
  attendanceActive: { padding: spacing.md, borderRadius: radii.lg, backgroundColor: '#E5F5EC' }, attendanceActiveText: { fontFamily: fontFamily.semiBold, fontSize: 12, lineHeight: 18, color: '#1E704A' }, attendanceBlocked: { padding: spacing.md, borderRadius: radii.lg, backgroundColor: '#FFF9E5' }, attendanceBlockedText: { fontFamily: fontFamily.semiBold, fontSize: 12, lineHeight: 18, color: '#755B00' },
  photoSection: { gap: spacing.sm }, label: { fontFamily: fontFamily.semiBold, fontSize: 13, color: colors.foreground }, hint: { fontFamily: fontFamily.medium, fontSize: 11, color: colors.mutedForeground }, photoActions: { flexDirection: 'row', gap: spacing.sm }, photoButton: { flex: 1, minHeight: 48, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: spacing.xs, padding: spacing.sm, borderWidth: 1, borderColor: colors.border, borderRadius: radii.lg, backgroundColor: colors.secondary }, photoButtonText: { fontFamily: fontFamily.bold, fontSize: 11, color: colors.primary, textAlign: 'center' }, photoGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm }, preview: { width: 84, height: 84, borderRadius: radii.md, backgroundColor: colors.muted }, remove: { position: 'absolute', top: 4, right: 4, width: 28, height: 28, alignItems: 'center', justifyContent: 'center', borderRadius: 14, backgroundColor: 'rgba(0,0,0,.65)' },
});
