import * as SecureStore from 'expo-secure-store';
import { createContext, useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';

import * as authService from '@/services/authService';
import type { AuthResponse, LoginRequest, RegisterRequest, User } from '@/types/auth';

const TOKEN_KEY = 'cuida_mais_auth_token';

type AuthContextValue = {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (request: LoginRequest) => Promise<AuthResponse>;
  register: (request: RegisterRequest) => Promise<AuthResponse>;
  logout: () => Promise<void>;
};

export const AuthContext = createContext<AuthContextValue | null>(null);

type Props = {
  children: ReactNode;
};

export function AuthProvider({ children }: Props) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const persistSession = useCallback(async (response: AuthResponse) => {
    setUser(response.user);
    setToken(response.token);
    await SecureStore.setItemAsync(TOKEN_KEY, response.token);
  }, []);

  useEffect(() => {
    let mounted = true;

    async function restoreSession() {
      try {
        const storedToken = await SecureStore.getItemAsync(TOKEN_KEY);
        if (!storedToken) return;

        const profile = await authService.getMe(storedToken);
        if (!mounted) return;

        setToken(storedToken);
        setUser(profile);
      } catch {
        await SecureStore.deleteItemAsync(TOKEN_KEY);
        if (!mounted) return;

        setToken(null);
        setUser(null);
      } finally {
        if (mounted) setIsLoading(false);
      }
    }

    restoreSession();

    return () => {
      mounted = false;
    };
  }, []);

  const login = useCallback(async (request: LoginRequest) => {
    const response = await authService.login(request);
    await persistSession(response);
    return response;
  }, [persistSession]);

  const register = useCallback(async (request: RegisterRequest) => {
    const response = await authService.register(request);
    await persistSession(response);
    return response;
  }, [persistSession]);

  const logout = useCallback(async () => {
    const currentToken = token;
    setUser(null);
    setToken(null);
    await SecureStore.deleteItemAsync(TOKEN_KEY);

    try {
      await authService.logout(currentToken);
    } catch {
      // Local session cleanup is enough for this stateless JWT backend.
    }
  }, [token]);

  const value = useMemo<AuthContextValue>(() => ({
    user,
    token,
    isAuthenticated: Boolean(token),
    isLoading,
    login,
    register,
    logout,
  }), [isLoading, login, logout, register, token, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
