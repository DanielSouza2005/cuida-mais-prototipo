import { apiRequest } from '@/services/api';
import type {
  AuthResponse,
  RegisterCaregiverPayload,
  ForgotPasswordRequest,
  LoginRequest,
  MessageResponse,
  RegisterRequest,
  RegisterResponsiblePayload,
  ResetPasswordRequest,
  SignupRequest,
  User,
} from '@/types/auth';

function toApiRegisterRequest(payload: RegisterResponsiblePayload | RegisterCaregiverPayload): SignupRequest {
  return {
    fullName: payload.user.nome,
    cpf: payload.user.cpf,
    email: payload.user.email,
    password: payload.senha,
    birthDate: payload.user.dataNascimento,
    userType: payload.user.tipoUsuario === 'CUIDADOR' ? 'caregiver' : 'family',
    acceptedTerms: payload.acceptedTerms,
  };
}

export function signup(request: SignupRequest) {
  return apiRequest<AuthResponse>('/api/auth/register', {
    auth: false,
    method: 'POST',
    body: request,
  });
}

export const register = signup as (request: RegisterRequest) => Promise<AuthResponse>;

export function registerResponsible(payload: RegisterResponsiblePayload) {
  return signup(toApiRegisterRequest(payload));
}

export function registerCaregiver(payload: RegisterCaregiverPayload) {
  return signup(toApiRegisterRequest(payload));
}

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

export function updateProfile(request: Omit<RegisterRequest, 'password' | 'acceptedTerms'>, token?: string | null) {
  return apiRequest<User>('/api/users/me', {
    method: 'PUT',
    token,
    body: request,
  });
}

export function updateResponsibleProfile(payload: RegisterResponsiblePayload, token?: string | null) {
  return updateProfile({
    fullName: payload.user.nome,
    cpf: payload.user.cpf,
    email: payload.user.email,
    birthDate: payload.user.dataNascimento,
    userType: 'family',
  }, token);
}

export function updateCaregiverProfile(payload: RegisterCaregiverPayload, token?: string | null) {
  return updateProfile({
    fullName: payload.user.nome,
    cpf: payload.user.cpf,
    email: payload.user.email,
    birthDate: payload.user.dataNascimento,
    userType: 'caregiver',
  }, token);
}

export async function updateAssistedPerson(payload: RegisterResponsiblePayload['assistedPersons'][number]) {
  return payload;
}
