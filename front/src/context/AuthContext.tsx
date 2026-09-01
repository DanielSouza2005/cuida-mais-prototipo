import { createContext, useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';

import * as authService from '@/services/authService';
import { AUTH_TOKEN_KEY, deleteSessionItem, getSessionItem, setSessionItem } from '@/services/sessionStorage';
import type {
  AuthResponse,
  CaregiverRegistrationResponse,
  RegistrationReviewResponse,
  LoginRequest,
  RegisterCaregiverPayload,
  RegisterResponsiblePayload,
  SignupRequest,
  User,
} from '@/types/auth';

type AuthContextValue = {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (request: LoginRequest) => Promise<AuthResponse>;
  logout: () => Promise<void>;
  register: (request: SignupRequest) => Promise<AuthResponse>;
  registerCaregiver: (request: RegisterCaregiverPayload) => Promise<CaregiverRegistrationResponse>;
  registerResponsible: (request: RegisterResponsiblePayload) => Promise<RegistrationReviewResponse>;
  restoreSession: () => Promise<void>;
  refreshUser: () => Promise<User | null>;
  signup: (request: SignupRequest) => Promise<AuthResponse>;
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
    await setSessionItem(AUTH_TOKEN_KEY, response.token);
  }, []);

  const restoreSession = useCallback(async () => {
    setIsLoading(true);

    try {
      const storedToken = await getSessionItem(AUTH_TOKEN_KEY);
      if (!storedToken) {
        setToken(null);
        setUser(null);
        return;
      }

      const profile = await authService.getMe(storedToken);
      setToken(storedToken);
      setUser(profile);
    } catch {
      await deleteSessionItem(AUTH_TOKEN_KEY);
      setToken(null);
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    restoreSession();
  }, [restoreSession]);

  const refreshUser = useCallback(async () => {
    if (!token) return null;
    const profile = await authService.getMe(token);
    setUser(profile);
    return profile;
  }, [token]);

  const login = useCallback(async (request: LoginRequest) => {
    const response = await authService.login(request);
    await persistSession(response);
    return response;
  }, [persistSession]);

  const signup = useCallback(async (request: SignupRequest) => {
    const response = await authService.signup(request);
    await persistSession(response);
    return response;
  }, [persistSession]);

  const register = signup;

  const registerResponsible = useCallback(async (request: RegisterResponsiblePayload) => {
    return authService.registerResponsible(request);
  }, []);

  const registerCaregiver = useCallback(async (request: RegisterCaregiverPayload) => {
    return authService.registerCaregiver(request);
  }, []);

  const logout = useCallback(async () => {
    const currentToken = token;
    setUser(null);
    setToken(null);
    await deleteSessionItem(AUTH_TOKEN_KEY);

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
    registerCaregiver,
    registerResponsible,
    restoreSession,
    refreshUser,
    logout,
    signup,
  }), [
    isLoading,
    login,
    logout,
    register,
    registerCaregiver,
    registerResponsible,
    restoreSession,
    refreshUser,
    signup,
    token,
    user,
  ]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
