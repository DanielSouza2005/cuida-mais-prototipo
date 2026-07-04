import type {
  CaregiverEducation,
  CaregiverService,
  CareModality,
  DayPeriod,
  WeekDay,
} from '@/constants/enums';
import type { Address, MessageResponse, User } from '@/types/auth';

export type ProfileAvailability = {
  diasSemana: WeekDay[];
  periodos: DayPeriod[];
  horarioInicio?: string | null;
  horarioFim?: string | null;
  observacao?: string | null;
};

export type MyCaregiverProfile = {
  experiencia?: string | null;
  formacao?: CaregiverEducation | null;
  formacaoOutro?: string | null;
  biografia?: string | null;
  enderecoAtendimento?: Address | null;
  modalidades: CareModality[];
  modalidadeOutro?: string | null;
  servicosOferecidos: CaregiverService[];
  servicoOutro?: string | null;
  disponibilidade: ProfileAvailability;
};

export type MyProfile = {
  user: User;
  caregiverProfile?: MyCaregiverProfile;
};

export type PersonalInfoUpdatePayload = {
  nome: string;
  email: string;
  telefone: string;
  dataNascimento: string;
};

export type CaregiverExperienceUpdatePayload = {
  experiencia: string;
  formacao?: CaregiverEducation | null;
  formacaoOutro?: string | null;
  biografia?: string | null;
};

export type CaregiverAvailabilityUpdatePayload = ProfileAvailability;

export type CaregiverModalitiesUpdatePayload = {
  modalidades: CareModality[];
  modalidadeOutro?: string | null;
};

export type CaregiverServicesUpdatePayload = {
  servicosOferecidos: CaregiverService[];
  servicoOutro?: string | null;
};

export type ProfileMessageResponse = MessageResponse;
