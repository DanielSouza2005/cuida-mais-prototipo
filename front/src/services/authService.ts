import { apiRequest } from '@/services/api';
import type {
  AuthResponse,
  RegisterCaregiverPayload,
  ForgotPasswordRequest,
  LoginRequest,
  MessageResponse,
  CaregiverRegistrationResponse,
  RegistrationReviewResponse,
  RegisterRequest,
  RegisterResponsiblePayload,
  ResetPasswordRequest,
  SignupRequest,
  User,
} from '@/types/auth';
import { appendProfilePhoto } from '@/utils/profilePhoto';

function toIsoDate(value: string) {
  const [day, month, year] = value.split('/');
  if (!day || !month || !year) return value;
  return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;
}

function toApiAddress(address: RegisterCaregiverPayload['caregiverProfile']['enderecoAtendimento']) {
  return {
    cep: address.cep,
    rua: address.rua,
    numero: address.numero,
    complemento: address.complemento,
    bairro: address.bairro,
    cidade: address.cidade,
    estado: address.estado,
    pontoReferencia: address.pontoReferencia,
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
  const assistedPerson = payload.assistedPersons[0];

  return apiRequest<RegistrationReviewResponse>('/api/auth/register/responsible', {
    auth: false,
    method: 'POST',
    body: {
      user: {
        nome: payload.user.nome,
        cpf: payload.user.cpf,
        email: payload.user.email,
        senha: payload.senha,
        telefone: payload.user.telefone,
        dataNascimento: toIsoDate(payload.user.dataNascimento),
      },
      responsibleProfile: {
        parentesco: payload.responsibleProfile.parentescoPadrao,
        parentescoOutro: payload.responsibleProfile.parentescoPersonalizado,
        preferenciaContato: payload.responsibleProfile.contatoPreferencial,
      },
      assistedPerson: {
        nome: assistedPerson.nome,
        cpf: assistedPerson.cpf,
        dataNascimento: toIsoDate(assistedPerson.dataNascimento),
        grauDependencia: assistedPerson.grauDependencia,
        mobilidade: assistedPerson.mobilidade,
        mobilidadeOutro: assistedPerson.mobilidadePersonalizada,
        alergias: assistedPerson.alergias,
        alergiasOutro: assistedPerson.alergias?.includes('OUTRO') ? assistedPerson.detalhesAlergia : undefined,
        alergiasDetalhes: assistedPerson.detalhesAlergia,
        restricoesAlimentares: assistedPerson.restricoesAlimentares,
        restricoesAlimentaresOutro: assistedPerson.restricoesAlimentares?.includes('OUTRO')
          ? assistedPerson.detalhesRestricaoAlimentar
          : undefined,
        restricoesAlimentaresDetalhes: assistedPerson.detalhesRestricaoAlimentar,
        medicamentos: assistedPerson.medicamentos,
        observacoes: assistedPerson.observacoes,
        enderecoCuidado: toApiAddress(assistedPerson.enderecoCuidado),
        contatoEmergencia: {
          nome: assistedPerson.contatoEmergencia.nome,
          telefone: assistedPerson.contatoEmergencia.telefone,
          vinculo: assistedPerson.contatoEmergencia.vinculo,
          isResponsibleContact: Boolean(assistedPerson.contatoEmergencia.isResponsibleEmergencyContact),
        },
      },
    },
  });
}

export function registerCaregiver(payload: RegisterCaregiverPayload) {
  const data = {
    user: {
      nome: payload.user.nome,
      cpf: payload.user.cpf,
      email: payload.user.email,
      senha: payload.senha,
      telefone: payload.user.telefone,
      dataNascimento: toIsoDate(payload.user.dataNascimento),
    },
    address: toApiAddress(payload.caregiverProfile.enderecoAtendimento),
    caregiverProfile: {
      tempoExperiencia: payload.caregiverProfile.tempoExperiencia,
      formacoes: payload.caregiverProfile.formacoes,
      formacaoOutro: payload.caregiverProfile.formacaoPersonalizada,
      biografia: payload.caregiverProfile.biografia,
      modalidades: payload.caregiverProfile.modalidadeAtendimento,
      modalidadeOutro: payload.caregiverProfile.modalidadePersonalizada,
      servicosOferecidos: payload.caregiverProfile.servicosOferecidos,
      servicoOutro: payload.caregiverProfile.servicoPersonalizado,
      disponibilidade: {
        diasSemana: payload.caregiverProfile.disponibilidade.diasSemana,
        periodos: payload.caregiverProfile.disponibilidade.periodos,
        horarioInicio: payload.caregiverProfile.disponibilidade.horariosEspecificos?.inicio,
        horarioFim: payload.caregiverProfile.disponibilidade.horariosEspecificos?.fim,
        observacao: payload.caregiverProfile.disponibilidade.observacao,
      },
    },
  };

  if (payload.profilePhoto) {
    const form = new FormData();
    form.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));
    appendProfilePhoto(form, payload.profilePhoto);
    return apiRequest<CaregiverRegistrationResponse>('/api/auth/register/caregiver', { auth: false, method: 'POST', body: form });
  }

  return apiRequest<CaregiverRegistrationResponse>('/api/auth/register/caregiver', {
    auth: false,
    method: 'POST',
    body: data,
  });
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
      phone: payload.user.telefone,
      birthDate: payload.user.dataNascimento,
      userType: 'family',
  }, token);
}

export function updateCaregiverProfile(payload: RegisterCaregiverPayload, token?: string | null) {
  return updateProfile({
    fullName: payload.user.nome,
      cpf: payload.user.cpf,
      email: payload.user.email,
      phone: payload.user.telefone,
      birthDate: payload.user.dataNascimento,
      userType: 'caregiver',
  }, token);
}

export async function updateAssistedPerson(payload: RegisterResponsiblePayload['assistedPersons'][number]) {
  return payload;
}
