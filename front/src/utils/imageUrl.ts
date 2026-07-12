import { API_BASE_URL } from '@/services/api';

export function getImageUrl(pathOrUrl?: string | null) {
  if (!pathOrUrl) return null;
  if (/^https?:\/\//i.test(pathOrUrl) || pathOrUrl.startsWith('file:') || pathOrUrl.startsWith('data:') || pathOrUrl.startsWith('blob:')) {
    return pathOrUrl;
  }
  return `${API_BASE_URL}${pathOrUrl.startsWith('/') ? '' : '/'}${pathOrUrl}`;
}
