import type {
  CareModality,
  CaregiverEducation,
  CaregiverExperienceRange,
  CaregiverService,
  DayPeriod,
  WeekDay,
} from '@/constants/enums';

export type LocationSuggestionType = 'CITY' | 'NEIGHBORHOOD';

export type LocationSuggestion = {
  id: string;
  label: string;
  type: LocationSuggestionType;
  city?: string | null;
  neighborhood?: string | null;
  state?: string | null;
};

export type CaregiverSearchFilters = {
  query: string;
  location: LocationSuggestion | null;
  origin?: {
    latitude: number;
    longitude: number;
  } | null;
  availability: DayPeriod[];
  services: CaregiverService[];
  modalities: CareModality[];
  page: number;
};

export type CaregiverAvailabilitySummary = {
  diasSemana: WeekDay[];
  periodos: DayPeriod[];
  horarioInicio?: string | null;
  horarioFim?: string | null;
  observacao?: string | null;
};

export type CaregiverSearchResult = {
  id: string;
  nome: string;
  cidade?: string | null;
  bairro?: string | null;
  estado?: string | null;
  distanciaKm?: number | null;
  experienciaRange?: CaregiverExperienceRange | null;
  formacoes: CaregiverEducation[];
  formacaoOutro?: string | null;
  servicosOferecidos: CaregiverService[];
  modalidadesAtendimento: CareModality[];
  disponibilidadeResumo: CaregiverAvailabilitySummary;
  biografiaResumo?: string | null;
};

export type CaregiverSearchPageResponse = {
  content: CaregiverSearchResult[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

export type CaregiverProfileDetails = {
  id: string;
  nome: string;
  cidade?: string | null;
  bairro?: string | null;
  estado?: string | null;
  distanciaKm?: number | null;
  experienciaRange?: CaregiverExperienceRange | null;
  formacoes: CaregiverEducation[];
  formacaoOutro?: string | null;
  biografia?: string | null;
  modalidadesAtendimento: CareModality[];
  modalidadeOutro?: string | null;
  servicosOferecidos: CaregiverService[];
  servicoOutro?: string | null;
  disponibilidade: CaregiverAvailabilitySummary;
  dataCadastro?: string | null;
  status?: string | null;
};

export type CaregiverServiceOption = {
  value: CaregiverService;
  label: string;
};
