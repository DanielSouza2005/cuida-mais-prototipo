import * as SecureStore from 'expo-secure-store';

export const AUTH_TOKEN_KEY = 'cuida_mais_auth_token';

const memoryStorage = new Map<string, string>();

async function canUseSecureStore() {
  return SecureStore.isAvailableAsync();
}

export async function getSessionItem(key: string) {
  if (await canUseSecureStore()) {
    const value = await SecureStore.getItemAsync(key);
    if (value) memoryStorage.set(key, value);
    return value;
  }

  return memoryStorage.get(key) ?? null;
}

export async function setSessionItem(key: string, value: string) {
  memoryStorage.set(key, value);

  if (await canUseSecureStore()) {
    await SecureStore.setItemAsync(key, value);
  }
}

export async function deleteSessionItem(key: string) {
  memoryStorage.delete(key);

  if (await canUseSecureStore()) {
    await SecureStore.deleteItemAsync(key);
  }
}
