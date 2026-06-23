export type UserType = 'family' | 'caregiver';

export type User = {
  id: string;
  fullName: string;
  cpf: string;
  email: string;
  birthDate: string;
  userType: UserType;
};

export type AuthResponse = {
  user: User;
  token: string;
};

export type RegisterRequest = {
  fullName: string;
  cpf: string;
  email: string;
  password: string;
  birthDate: string;
  userType: UserType;
  acceptedTerms: boolean;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type ForgotPasswordRequest = {
  email: string;
};

export type ResetPasswordRequest = {
  token: string;
  password: string;
};

export type MessageResponse = {
  message: string;
};
