import type { ImagePickerAsset } from 'expo-image-picker';

import type { SelectedProfilePhoto } from '@/types/auth';

export const MAX_PROFILE_PHOTO_SIZE = 5 * 1024 * 1024;

export function toSelectedProfilePhoto(asset: ImagePickerAsset): SelectedProfilePhoto {
  const type = asset.mimeType && ['image/jpeg', 'image/png', 'image/webp'].includes(asset.mimeType)
    ? asset.mimeType
    : 'image/jpeg';
  const extension = type === 'image/png' ? 'png' : type === 'image/webp' ? 'webp' : 'jpg';
  return {
    uri: asset.uri,
    name: asset.fileName ?? `profile-photo.${extension}`,
    type,
  };
}

export function appendProfilePhoto(form: FormData, photo: SelectedProfilePhoto) {
  form.append('photo', photo as unknown as Blob);
}
