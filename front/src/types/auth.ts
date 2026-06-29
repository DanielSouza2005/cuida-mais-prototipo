export type UserType = 'RESPONSAVEL' | 'CUIDADOR' | 'ADMIN';

export type ApiUserType = 'family' | 'caregiver' | 'admin';

export type User = {
  id: string;
  fullName: string;
  cpf: string;
  email: string;
  birthDate: string;
  userType: ApiUserType;
};

export type AuthResponse = {
  user: User;
  token: string;
};

export type AuthUser = User;

export type UserBase = {
  id?: string;
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  dataNascimento: string;
  tipoUsuario: UserType;
  status?: string;
};

export type ResponsibleProfile = {
  userId?: string;
  parentescoPadrao?: string;
  contatoPreferencial?: string;
};

export type CaregiverProfile = {
  userId?: string;
  experiencia: string;
  formacao?: string;
  biografia?: string;
  disponibilidade: string[];
  regiaoAtendimento: string;
  modalidadeAtendimento: string;
  servicosOferecidos: string[];
};

export type Address = {
  cep: string;
  rua: string;
  numero: string;
  complemento?: string;
  bairro: string;
  cidade: string;
  estado: string;
  pontoReferencia?: string;
};

export type EmergencyContact = {
  nome: string;
  telefone: string;
  vinculo: string;
};

export type AssistedPerson = {
  id?: string;
  responsibleUserId?: string;
  nome: string;
  dataNascimento: string;
  cpf?: string;
  grauDependencia: string;
  mobilidade: string;
  enderecoCuidado: Address;
  contatoEmergencia: EmergencyContact;
  necessidadesCuidado: string[];
  alergias?: string;
  medicamentos?: string;
  restricoesAlimentares?: string;
  observacoes?: string;
};

export type RegisterResponsiblePayload = {
  user: UserBase;
  senha: string;
  responsibleProfile: ResponsibleProfile;
  assistedPersons: AssistedPerson[];
  acceptedTerms: boolean;
};

export type RegisterCaregiverPayload = {
  user: UserBase;
  senha: string;
  caregiverProfile: CaregiverProfile;
  acceptedTerms: boolean;
};

export type RegisterRequest = {
  fullName: string;
  cpf: string;
  email: string;
  password: string;
  birthDate: string;
  userType: ApiUserType;
  acceptedTerms: boolean;
};

export type SignupRequest = RegisterRequest;
export type SignupResponse = AuthResponse;

export type LoginRequest = {
  email: string;
  password: string;
};

export type LoginResponse = AuthResponse;

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
