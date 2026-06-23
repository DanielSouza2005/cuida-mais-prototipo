import { apiRequest } from '@/services/api';
import type {
  AuthResponse,
  ForgotPasswordRequest,
  LoginRequest,
  MessageResponse,
  RegisterRequest,
  ResetPasswordRequest,
  User,
} from '@/types/auth';

export function register(request: RegisterRequest) {
  return apiRequest<AuthResponse>('/api/auth/register', {
    method: 'POST',
    body: request,
  });
}

export function login(request: LoginRequest) {
  return apiRequest<AuthResponse>('/api/auth/login', {
    method: 'POST',
    body: request,
  });
}

export function forgotPassword(request: ForgotPasswordRequest) {
  return apiRequest<MessageResponse>('/api/auth/forgot-password', {
    method: 'POST',
    body: request,
  });
}

export function resetPassword(request: ResetPasswordRequest) {
  return apiRequest<MessageResponse>('/api/auth/reset-password', {
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
