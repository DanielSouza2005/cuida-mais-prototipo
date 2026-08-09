import * as SecureStore from 'expo-secure-store';

const REMEMBERED_EMAIL_KEY = 'auth.rememberedEmail';

export async function getRememberedEmail() {
  try {
    if (!(await SecureStore.isAvailableAsync())) return null;

    return await SecureStore.getItemAsync(REMEMBERED_EMAIL_KEY);
  } catch {
    return null;
  }
}

export async function saveRememberedEmail(email: string) {
  try {
    if (!(await SecureStore.isAvailableAsync())) return;

    await SecureStore.setItemAsync(REMEMBERED_EMAIL_KEY, email.trim().toLowerCase());
  } catch {
    // Remembering the e-mail is optional and must never prevent login.
  }
}

export async function clearRememberedEmail() {
  try {
    if (!(await SecureStore.isAvailableAsync())) return;

    await SecureStore.deleteItemAsync(REMEMBERED_EMAIL_KEY);
  } catch {
    // Clearing the preference is optional and must never prevent login.
  }
}
