import { apiRequest } from '@/services/api';
import type {
  AuthResponse,
  ForgotPasswordRequest,
  LoginRequest,
  MessageResponse,
  RegisterRequest,
  ResetPasswordRequest,
  SignupRequest,
  User,
} from '@/types/auth';

export function signup(request: SignupRequest) {
  return apiRequest<AuthResponse>('/api/auth/register', {
    auth: false,
    method: 'POST',
    body: request,
  });
}

export const register = signup as (request: RegisterRequest) => Promise<AuthResponse>;

export function login(request: LoginRequest) {
  return apiRequest<AuthResponse>('/api/auth/login', {
    auth: false,
    method: 'POST',
    body: request,
  });
}

export function forgotPassword(request: ForgotPasswordRequest) {
  return apiRequest<MessageResponse>('/api/auth/forgot-password', {
    auth: false,
    method: 'POST',
    body: request,
  });
}

export function resetPassword(request: ResetPasswordRequest) {
  return apiRequest<MessageResponse>('/api/auth/reset-password', {
    auth: false,
    method: 'POST',
    body: request,
  });
}

export function logout(token: string | null) {
  return apiRequest<MessageResponse>('/api/auth/logout', {
    method: 'POST',
    token,
  });
}

export function getMe(token: string) {
  return apiRequest<User>('/api/users/me', {
    method: 'GET',
    token,
  });
}
