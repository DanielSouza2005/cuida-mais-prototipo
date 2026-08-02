import * as ImagePicker from 'expo-image-picker';
import { router, useSegments, type Href } from 'expo-router';
import { Bell, ClipboardCheck, IdCard, LogOut, Shield } from 'lucide-react-native';
import { useEffect, useMemo, useState } from 'react';
import { Alert, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { ProfileAvatar } from '@/components/profile-avatar';
import { ProfileTypeBadge } from '@/components/profile-type-badge';
import { ScreenContainer } from '@/components/screen-container';
import { SettingsRow } from '@/components/settings-row';
import { useAuth } from '@/hooks/useAuth';
import { ApiError } from '@/services/api';
import { deleteProfilePhoto, updateProfilePhoto } from '@/services/profileService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import { MAX_PROFILE_PHOTO_SIZE, toSelectedProfilePhoto } from '@/utils/profilePhoto';

export default function ProfileScreen() {
  const { user, isAuthenticated, isLoading, logout, refreshUser } = useAuth();
  const segments = useSegments();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [isUpdatingPhoto, setIsUpdatingPhoto] = useState(false);
  const isCaregiver = user?.userType === 'caregiver';
  const isTabRoute = (segments as string[])[0] === '(tabs)';

  const initials = useMemo(() => {
    const name = user?.fullName?.trim();
    if (!name) return 'CP';

    return name
      .split(/\s+/)
      .slice(0, 2)
      .map((part) => part[0])
      .join('')
      .toUpperCase();
  }, [user?.fullName]);

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.replace('/login');
    }
  }, [isAuthenticated, isLoading]);

  async function handleLogout() {
    setIsLoggingOut(true);

    try {
      await logout();
    } finally {
      setIsLoggingOut(false);
      router.replace('/login');
    }
  }

  async function uploadPhoto(asset: ImagePicker.ImagePickerAsset) {
    if (asset.fileSize && asset.fileSize > MAX_PROFILE_PHOTO_SIZE) {
      Alert.alert('Foto muito grande', 'Escolha uma foto de até 5 MB.');
      return;
    }
    setIsUpdatingPhoto(true);
    try {
      await updateProfilePhoto(toSelectedProfilePhoto(asset));
      await refreshUser();
      Alert.alert('Tudo certo', 'Foto atualizada com sucesso.');
    } catch (error) {
      Alert.alert('Não foi possível atualizar a foto', error instanceof ApiError ? error.message : 'Tente novamente.');
    } finally {
      setIsUpdatingPhoto(false);
    }
  }

  async function choosePhoto(source: 'camera' | 'library') {
    const permission = source === 'camera'
      ? await ImagePicker.requestCameraPermissionsAsync()
      : await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permission.granted) {
      Alert.alert('Permissão negada', 'Permissão negada. Você pode tentar novamente depois.');
      return;
    }
    const result = source === 'camera'
      ? await ImagePicker.launchCameraAsync({ mediaTypes: ['images'], allowsEditing: true, aspect: [1, 1], quality: 0.8, cameraType: ImagePicker.CameraType.front })
      : await ImagePicker.launchImageLibraryAsync({ mediaTypes: ['images'], allowsEditing: true, aspect: [1, 1], quality: 0.8 });
    if (!result.canceled && result.assets[0]) await uploadPhoto(result.assets[0]);
  }

  async function removePhoto() {
    setIsUpdatingPhoto(true);
    try {
      await deleteProfilePhoto();
      await refreshUser();
      Alert.alert('Tudo certo', 'Foto removida com sucesso.');
    } catch (error) {
      Alert.alert('Não foi possível remover a foto', error instanceof ApiError ? error.message : 'Tente novamente.');
    } finally {
      setIsUpdatingPhoto(false);
    }
  }

  function showPhotoOptions() {
    Alert.alert(user?.profilePhotoUrl ? 'Editar foto' : 'Adicionar foto', undefined, [
      { text: 'Tirar foto', onPress: () => void choosePhoto('camera') },
      { text: 'Escolher da galeria', onPress: () => void choosePhoto('library') },
      ...(user?.profilePhotoUrl ? [{
        text: 'Remover foto',
        style: 'destructive' as const,
        onPress: () => Alert.alert('Remover foto?', 'O avatar com suas iniciais voltará a ser exibido.', [
          { text: 'Cancelar', style: 'cancel' },
          { text: 'Remover foto', style: 'destructive', onPress: () => void removePhoto() },
        ]),
      }] : []),
      { text: 'Cancelar', style: 'cancel' },
    ]);
  }

  return (
    <ScreenContainer
      contentStyle={[styles.content, isTabRoute && styles.tabContent]}
      safeAreaEdges={isTabRoute ? ['top', 'right', 'left'] : undefined}
    >
      <AppHeader showBack={!isTabRoute} />

      <View style={styles.profileCard}>
        <ProfileAvatar
          imageUrl={user?.profilePhotoUrl}
          initials={initials}
          name={user?.fullName ?? 'Cuidar+'}
          subtitle={user?.email ?? 'Perfil de protótipo'}
          editable={isCaregiver}
          editDisabled={isUpdatingPhoto}
          editLoading={isUpdatingPhoto}
          onEditPress={isCaregiver ? showPhotoOptions : undefined}
        />
        {user ? <ProfileTypeBadge type={isCaregiver ? 'CUIDADOR' : 'RESPONSAVEL'} /> : <Text style={styles.profileNote}>Carregando dados do perfil.</Text>}
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Configurações</Text>
        <SettingsRow
          title="Dados cadastrais"
          description={isCaregiver
            ? 'Gerencie suas informações pessoais, experiência, disponibilidade e serviços oferecidos.'
            : 'Gerencie suas informações pessoais, pessoa assistida, endereço do cuidado e contato de emergência.'}
          icon={IdCard}
          onPress={() => router.push('/profile-registration-settings' as Href)}
        />
        {!isCaregiver ? <SettingsRow title="Rotina de Cuidados" description="Cadastre rotinas para reutilizar nas solicitações de serviço" icon={ClipboardCheck} onPress={() => router.push('/care-tasks' as Href)} /> : null}
        <SettingsRow title="Notificações" description="Preferências de comunicação" icon={Bell} onPress={() => router.push('/profile-notifications')} />
        <SettingsRow title="Privacidade" description="Segurança da conta e dados pessoais" icon={Shield} onPress={() => router.push('/profile-privacy')} />
        <SettingsRow
          title={isLoggingOut ? 'Saindo...' : 'Sair'}
          description="Encerrar sessão neste aparelho"
          icon={LogOut}
          onPress={isLoggingOut ? undefined : handleLogout}
        />
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.lg,
    gap: spacing.xl,
  },
  tabContent: {
    paddingBottom: spacing.md,
  },
  profileCard: {
    alignItems: 'center',
    gap: spacing.lg,
    padding: spacing.xl,
    borderRadius: radii.xxl,
    backgroundColor: colors.card,
    borderWidth: 1,
    borderColor: colors.border,
    ...shadows.card,
  },
  profileNote: {
    textAlign: 'center',
    fontFamily: fontFamily.regular,
    fontSize: 13,
    lineHeight: 20,
    color: colors.mutedForeground,
  },
  section: {
    gap: spacing.md,
  },
  sectionTitle: {
    fontFamily: fontFamily.extraBold,
    fontSize: 18,
    color: colors.foreground,
  },
});
