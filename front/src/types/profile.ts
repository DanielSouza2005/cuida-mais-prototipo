import type {
  Allergy,
  CaregiverEducation,
  CaregiverExperienceRange,
  CaregiverService,
  CareModality,
  ContactPreference,
  DayPeriod,
  DependencyLevel,
  FoodRestriction,
  Mobility,
  Relationship,
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
  tempoExperiencia?: CaregiverExperienceRange | null;
  experiencia?: string | null;
  formacoes: CaregiverEducation[];
  formacaoOutro?: string | null;
  biografia?: string | null;
  enderecoAtendimento?: Address | null;
  modalidades: CareModality[];
  modalidadeOutro?: string | null;
  servicosOferecidos: CaregiverService[];
  servicoOutro?: string | null;
  disponibilidade: ProfileAvailability;
};

export type MyResponsibleProfile = {
  parentesco?: Relationship | null;
  parentescoOutro?: string | null;
  preferenciaContato?: ContactPreference | null;
};

export type ProfileEmergencyContact = {
  nome?: string | null;
  telefone?: string | null;
  vinculo?: string | null;
  isResponsibleContact?: boolean;
};

export type ProfileAssistedPerson = {
  id: string;
  nome: string;
  cpf?: string | null;
  dataNascimento: string;
  grauDependencia?: DependencyLevel | null;
  mobilidade?: Mobility | null;
  mobilidadeOutro?: string | null;
  alergias: Allergy[];
  alergiasOutro?: string | null;
  alergiasDetalhes?: string | null;
  restricoesAlimentares: FoodRestriction[];
  restricoesAlimentaresOutro?: string | null;
  restricoesAlimentaresDetalhes?: string | null;
  medicamentos?: string | null;
  observacoes?: string | null;
  enderecoCuidado?: Address | null;
  contatoEmergencia?: ProfileEmergencyContact | null;
};

export type MyProfile = {
  user: User;
  responsibleProfile?: MyResponsibleProfile;
  assistedPersons?: ProfileAssistedPerson[];
  caregiverProfile?: MyCaregiverProfile;
};

export type PersonalInfoUpdatePayload = {
  nome: string;
  telefone: string;
};

export type ResponsibleProfileUpdatePayload = {
  parentesco: Relationship;
  parentescoOutro?: string | null;
  preferenciaContato: ContactPreference;
};

export type AssistedPersonUpdatePayload = {
  nome: string;
  cpf?: string | null;
  dataNascimento: string;
  grauDependencia: DependencyLevel;
  mobilidade: Mobility;
  mobilidadeOutro?: string | null;
  alergias: Allergy[];
  alergiasOutro?: string | null;
  alergiasDetalhes?: string | null;
  restricoesAlimentares: FoodRestriction[];
  restricoesAlimentaresOutro?: string | null;
  restricoesAlimentaresDetalhes?: string | null;
  medicamentos?: string | null;
  observacoes?: string | null;
};

export type EmergencyContactUpdatePayload = {
  nome?: string | null;
  telefone?: string | null;
  vinculo?: string | null;
  isResponsibleContact: boolean;
};

export type CaregiverExperienceUpdatePayload = {
  tempoExperiencia: CaregiverExperienceRange;
  formacoes: CaregiverEducation[];
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
