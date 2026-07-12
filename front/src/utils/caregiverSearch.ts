import {
  careModalityOptions,
  caregiverEducationOptions,
  caregiverExperienceRangeOptions,
  caregiverServiceOptions,
  dayPeriodOptions,
  weekDayOptions,
} from '@/constants/enums';
import type { CaregiverAvailabilitySummary, CaregiverProfileDetails, CaregiverSearchResult } from '@/types/caregiverSearch';

export const availabilityOptions = [
  ...dayPeriodOptions,
] as const;

export const searchServiceOptions = caregiverServiceOptions;
export const searchModalityOptions = careModalityOptions;

export function getCaregiverLabel(collection: readonly { value: string; label: string }[], value: string) {
  return collection.find((option) => option.value === value)?.label ?? value;
}

export function getAvailabilityLabel(value: CaregiverAvailabilitySummary['periodos'][number]) {
  return getCaregiverLabel(availabilityOptions, value);
}

export function getEducationLabel(value: CaregiverSearchResult['formacoes'][number]) {
  return getCaregiverLabel(caregiverEducationOptions, value);
}

export function getExperienceLabel(value?: CaregiverSearchResult['experienciaRange'] | null) {
  if (!value) return 'Experiência não informada';
  return getCaregiverLabel(caregiverExperienceRangeOptions, value);
}

export function getServiceLabel(value: CaregiverSearchResult['servicosOferecidos'][number]) {
  return getCaregiverLabel(caregiverServiceOptions, value);
}

export function getModalityLabel(value: CaregiverProfileDetails['modalidadesAtendimento'][number]) {
  return getCaregiverLabel(careModalityOptions, value);
}

export function getWeekDayLabel(value: CaregiverAvailabilitySummary['diasSemana'][number]) {
  return getCaregiverLabel(weekDayOptions, value);
}

export function sortWeekDays(values: CaregiverAvailabilitySummary['diasSemana']) {
  const order = new Map(weekDayOptions.map((option, index) => [option.value, index]));
  return [...values].sort((current, next) => (order.get(current) ?? 99) - (order.get(next) ?? 99));
}

export function formatDistance(distanceKm?: number | null) {
  if (typeof distanceKm !== 'number' || !Number.isFinite(distanceKm) || distanceKm < 0) {
    return null;
  }

  if (distanceKm < 1) {
    return `${Math.max(1, Math.round(distanceKm * 1000))} m`;
  }

  return `${distanceKm.toLocaleString('pt-BR', {
    minimumFractionDigits: distanceKm < 10 ? 1 : 0,
    maximumFractionDigits: distanceKm < 10 ? 1 : 0,
  })} km`;
}

export function normalizeSearchText(value: string) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim();
}

export function getInitials(name?: string | null) {
  if (!name) return 'CP';

  const parts = name.trim().split(/\s+/).filter(Boolean);
  const first = parts[0]?.[0] ?? 'C';
  const last = parts.length > 1 ? parts[parts.length - 1]?.[0] : parts[0]?.[1];

  return `${first}${last ?? 'P'}`.toUpperCase();
}

export function formatLocation(value: Pick<CaregiverSearchResult, 'bairro' | 'cidade' | 'estado'>) {
  const cityState = [value.cidade, value.estado].filter(Boolean).join(' - ');

  if (value.bairro && cityState) {
    return `${value.bairro}, ${cityState}`;
  }

  return cityState || value.bairro || 'Localização não informada';
}
