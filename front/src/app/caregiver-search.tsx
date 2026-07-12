import { router, type Href } from 'expo-router';
import * as Location from 'expo-location';
import { ArrowRight, MapPin, Navigation, Search, SlidersHorizontal, X } from 'lucide-react-native';
import { useEffect, useState } from 'react';
import { ActivityIndicator, FlatList, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { LoadingState } from '@/components/loading-state';
import { PrimaryButton } from '@/components/primary-button';
import { searchCaregivers, getCaregiverLocations } from '@/services/caregiverSearchService';
import { getMyProfile } from '@/services/profileService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { CareModality, CaregiverService } from '@/constants/enums';
import type {
  CaregiverSearchFilters,
  CaregiverSearchPageResponse,
  CaregiverSearchResult,
  LocationSuggestion,
} from '@/types/caregiverSearch';
import {
  availabilityOptions,
  formatDistance,
  formatLocation,
  getAvailabilityLabel,
  getEducationLabel,
  getExperienceLabel,
  getInitials,
  getServiceLabel,
  searchModalityOptions,
  searchServiceOptions,
} from '@/utils/caregiverSearch';

const initialFilters: CaregiverSearchFilters = {
  query: '',
  location: null,
  origin: null,
  availability: [],
  services: [],
  modalities: [],
  page: 0,
};

export default function CaregiverSearchScreen() {
  const [draftFilters, setDraftFilters] = useState<CaregiverSearchFilters>(initialFilters);
  const [appliedFilters, setAppliedFilters] = useState<CaregiverSearchFilters>(initialFilters);
  const [locationQuery, setLocationQuery] = useState('');
  const [locationSuggestions, setLocationSuggestions] = useState<LocationSuggestion[]>([]);
  const [locationsLoading, setLocationsLoading] = useState(false);
  const [currentLocationLoading, setCurrentLocationLoading] = useState(false);
  const [careAddressLoading, setCareAddressLoading] = useState(false);
  const [locationError, setLocationError] = useState<string | null>(null);
  const [pageData, setPageData] = useState<CaregiverSearchPageResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const hasFilters = Boolean(
    draftFilters.query ||
    draftFilters.location ||
    draftFilters.origin ||
    draftFilters.availability.length ||
    draftFilters.services.length ||
    draftFilters.modalities.length,
  );

  useEffect(() => {
    let active = true;

    setLoading(true);
    setError(null);
    searchCaregivers(appliedFilters)
      .then((response) => {
        if (active) setPageData(response);
      })
      .catch(() => {
        if (active) setError('Não foi possível carregar os cuidadores. Tente novamente.');
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [appliedFilters]);

  useEffect(() => {
    let active = true;

    setLocationsLoading(true);
    const timeoutId = setTimeout(() => {
      getCaregiverLocations(locationQuery)
        .then((response) => {
          if (active) setLocationSuggestions(response);
        })
        .catch(() => {
          if (active) setLocationSuggestions([]);
        })
        .finally(() => {
          if (active) setLocationsLoading(false);
        });
    }, 300);

    return () => {
      active = false;
      clearTimeout(timeoutId);
    };
  }, [locationQuery]);

  function updateDraftFilters(nextFilters: CaregiverSearchFilters) {
    setDraftFilters({ ...nextFilters, page: 0 });
  }

  function updateFilter<Key extends keyof CaregiverSearchFilters>(key: Key, value: CaregiverSearchFilters[Key]) {
    updateDraftFilters({ ...draftFilters, [key]: value });
  }

  function toggleAvailability(value: CaregiverSearchFilters['availability'][number]) {
    updateFilter(
      'availability',
      draftFilters.availability.includes(value)
        ? draftFilters.availability.filter((item) => item !== value)
        : [...draftFilters.availability, value],
    );
  }

  function toggleService(value: CaregiverService) {
    updateFilter(
      'services',
      draftFilters.services.includes(value)
        ? draftFilters.services.filter((item) => item !== value)
        : [...draftFilters.services, value],
    );
  }

  function toggleModality(value: CareModality) {
    updateFilter(
      'modalities',
      draftFilters.modalities.includes(value)
        ? draftFilters.modalities.filter((item) => item !== value)
        : [...draftFilters.modalities, value],
    );
  }

  function selectLocation(location: LocationSuggestion) {
    setLocationQuery(location.label);
    updateFilter('location', location);
  }

  function changeLocationText(value: string) {
    setLocationQuery(value);
    setLocationError(null);

    if (!value.trim() || draftFilters.origin || (draftFilters.location && value !== draftFilters.location.label)) {
      updateDraftFilters({ ...draftFilters, location: null, origin: null });
    }
  }

  function clearLocation() {
    setLocationQuery('');
    setLocationError(null);
    updateDraftFilters({ ...draftFilters, location: null, origin: null });
  }

  function clearFilters() {
    setLocationQuery('');
    setLocationError(null);
    setDraftFilters(initialFilters);
    setAppliedFilters(initialFilters);
  }

  function applySearch() {
    setAppliedFilters({ ...draftFilters, page: 0 });
  }

  async function useCurrentLocation() {
    setCurrentLocationLoading(true);
    setLocationError(null);

    try {
      const permission = await Location.requestForegroundPermissionsAsync();
      if (permission.status !== Location.PermissionStatus.GRANTED) {
        setLocationError('Permita o acesso à localização para calcular distância real.');
        return;
      }

      const currentPosition = await Location.getCurrentPositionAsync({
        accuracy: Location.Accuracy.Balanced,
      });

      setLocationQuery('Minha localização atual');
      updateDraftFilters({
        ...draftFilters,
        location: null,
        origin: {
          latitude: currentPosition.coords.latitude,
          longitude: currentPosition.coords.longitude,
        },
      });
    } catch {
      setLocationError('Não foi possível obter sua localização agora.');
    } finally {
      setCurrentLocationLoading(false);
    }
  }

  async function useCareAddressLocation() {
    setCareAddressLoading(true);
    setLocationError(null);

    try {
      const profile = await getMyProfile();
      const careAddress = profile.assistedPersons?.[0]?.enderecoCuidado;
      const latitude = careAddress?.latitude;
      const longitude = careAddress?.longitude;

      if (!hasCoordinates(latitude, longitude)) {
        setLocationError('Endereço de cuidado sem coordenadas reais para calcular distância.');
        return;
      }

      setLocationQuery('Endereço de cuidado');
      updateDraftFilters({
        ...draftFilters,
        location: null,
        origin: {
          latitude: Number(latitude),
          longitude: Number(longitude),
        },
      });
    } catch {
      setLocationError('Não foi possível carregar o endereço de cuidado agora.');
    } finally {
      setCareAddressLoading(false);
    }
  }

  function hasCoordinates(latitude?: number | null, longitude?: number | null) {
    return typeof latitude === 'number'
      && typeof longitude === 'number'
      && Number.isFinite(latitude)
      && Number.isFinite(longitude);
  }

  function retry() {
    setAppliedFilters((current) => ({ ...current }));
  }

  const caregivers = pageData?.content ?? [];
  const totalPages = pageData?.totalPages ?? 0;
  const currentPage = appliedFilters.page;

  return (
    <SafeAreaView style={styles.screen} edges={['top', 'right', 'bottom', 'left']}>
      <FlatList
        data={loading || error ? [] : caregivers}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.content}
        showsVerticalScrollIndicator={false}
        keyboardShouldPersistTaps="handled"
        ListHeaderComponent={
          <View style={styles.headerContent}>
            <AppHeader
              showBack
              title="Buscar cuidadores"
              subtitle="Encontre profissionais disponíveis para apoiar o cuidado domiciliar."
            />

            <AppTextInput
              label="Nome do cuidador"
              icon={Search}
              value={draftFilters.query}
              onChangeText={(value) => updateFilter('query', value)}
              placeholder="Buscar por nome do cuidador"
              returnKeyType="search"
            />

            <LocationCombobox
              value={locationQuery}
              selectedLocation={draftFilters.location}
              suggestions={locationSuggestions}
              loading={locationsLoading}
              disabled={false}
              onChangeText={changeLocationText}
              onSelectLocation={selectLocation}
              onClear={clearLocation}
            />
            <PrimaryButton
              label="Usar minha localização"
              icon={Navigation}
              variant="secondary"
              loading={currentLocationLoading}
              onPress={useCurrentLocation}
              style={styles.currentLocationButton}
            />
            <PrimaryButton
              label="Usar endereço de cuidado"
              icon={MapPin}
              variant="secondary"
              loading={careAddressLoading}
              onPress={useCareAddressLocation}
              style={styles.currentLocationButton}
            />
            {locationError ? <Text style={styles.locationError}>{locationError}</Text> : null}

            <View style={styles.filterHeader}>
              <View style={styles.filterTitleRow}>
                <SlidersHorizontal color={colors.primary} size={17} strokeWidth={2.5} />
                <Text style={styles.filterTitle}>Filtros</Text>
              </View>
            </View>

            <FilterRow title="Disponibilidade">
              {availabilityOptions.map((option) => (
                <FilterChip
                  key={option.value}
                  label={option.label}
                  selected={draftFilters.availability.includes(option.value)}
                  disabled={loading}
                  onPress={() => toggleAvailability(option.value)}
                />
              ))}
            </FilterRow>

            <FilterRow title="Serviços e especialidades">
              {searchServiceOptions.map((option) => (
                <FilterChip
                  key={option.value}
                  label={option.label}
                  selected={draftFilters.services.includes(option.value)}
                  disabled={loading}
                  onPress={() => toggleService(option.value as CaregiverService)}
                />
              ))}
            </FilterRow>

            <FilterRow title="Modalidade de atendimento">
              {searchModalityOptions.map((option) => (
                <FilterChip
                  key={option.value}
                  label={option.label}
                  selected={draftFilters.modalities.includes(option.value)}
                  disabled={loading}
                  onPress={() => toggleModality(option.value as CareModality)}
                />
              ))}
            </FilterRow>

            <View style={styles.searchActions}>
              <PrimaryButton
                label="Buscar"
                icon={Search}
                loading={loading}
                onPress={applySearch}
                style={styles.searchButton}
              />
              {hasFilters ? (
                <PrimaryButton
                  label="Limpar filtros"
                  variant="secondary"
                  onPress={clearFilters}
                  style={styles.searchButton}
                />
              ) : null}
            </View>

            <View style={styles.resultsHeader}>
              <Text style={styles.resultsCount}>
                {pageData?.totalElements === 0
                  ? 'Nenhum cuidador encontrado'
                  : `${pageData?.totalElements ?? 0} ${(pageData?.totalElements ?? 0) === 1 ? 'cuidador encontrado' : 'cuidadores encontrados'}`}
              </Text>
            </View>
          </View>
        }
        renderItem={({ item }) => <CaregiverCard caregiver={item} />}
        ItemSeparatorComponent={() => <View style={styles.separator} />}
        ListEmptyComponent={
          loading ? (
            <LoadingState message="Carregando cuidadores..." />
          ) : error ? (
            <ErrorState message={error} onRetry={retry} />
          ) : (
            <EmptyState onClear={clearFilters} />
          )
        }
        ListFooterComponent={
          !loading && !error && caregivers.length > 0 ? (
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              loading={loading}
              onPrevious={() => setAppliedFilters((current) => ({ ...current, page: Math.max(0, current.page - 1) }))}
              onNext={() => setAppliedFilters((current) => ({ ...current, page: Math.min(Math.max(0, totalPages - 1), current.page + 1) }))}
            />
          ) : null
        }
      />
    </SafeAreaView>
  );
}

function LocationCombobox({
  value,
  selectedLocation,
  suggestions,
  loading,
  disabled,
  onChangeText,
  onSelectLocation,
  onClear,
}: {
  value: string;
  selectedLocation: LocationSuggestion | null;
  suggestions: LocationSuggestion[];
  loading: boolean;
  disabled: boolean;
  onChangeText: (value: string) => void;
  onSelectLocation: (location: LocationSuggestion) => void;
  onClear: () => void;
}) {
  const [focused, setFocused] = useState(false);
  const showSuggestions = focused && value.trim().length > 0;

  return (
    <View style={styles.locationCombobox}>
      <Text style={styles.inputLabel}>Localização</Text>
      <View style={[
        styles.locationInputShell,
        showSuggestions && styles.locationInputShellOpen,
        focused && styles.locationInputFocused,
        disabled && styles.disabledShell,
      ]}>
        <MapPin color={focused ? colors.primary : colors.mutedForeground} size={19} strokeWidth={2.4} />
        <TextInput
          value={value}
          onChangeText={onChangeText}
          onFocus={() => setFocused(true)}
          editable={!disabled}
          placeholder="Buscar cidade, bairro ou endereço"
          placeholderTextColor={colors.mutedForeground}
          returnKeyType="search"
          style={styles.locationInput}
        />
        {value ? (
          <Pressable accessibilityRole="button" disabled={disabled} onPress={onClear} style={styles.locationClearButton}>
            <X color={colors.mutedForeground} size={16} strokeWidth={2.5} />
          </Pressable>
        ) : null}
      </View>

      {showSuggestions ? (
        <View style={styles.suggestionBox}>
          {loading ? (
            <View style={styles.suggestionLoading}>
              <ActivityIndicator color={colors.primary} size="small" />
              <Text style={styles.noSuggestionText}>Buscando locais...</Text>
            </View>
          ) : suggestions.length > 0 ? (
            suggestions.map((suggestion) => {
              const selected = selectedLocation?.id === suggestion.id;

              return (
                <Pressable
                  key={suggestion.id}
                  accessibilityRole="button"
                  accessibilityState={{ selected }}
                  onPress={() => {
                    onSelectLocation(suggestion);
                    setFocused(false);
                  }}
                  style={({ pressed }) => [styles.suggestionItem, selected && styles.suggestionItemSelected, pressed && styles.pressed]}
                >
                  <View style={styles.suggestionIcon}>
                    <MapPin color={selected ? colors.primary : colors.mutedForeground} size={14} strokeWidth={2.4} />
                  </View>
                  <View style={styles.suggestionCopy}>
                    <Text style={styles.suggestionLabel}>{suggestion.label}</Text>
                    <Text style={styles.suggestionType}>{suggestion.type === 'CITY' ? 'Cidade' : 'Bairro'}</Text>
                  </View>
                </Pressable>
              );
            })
          ) : (
            <Text style={styles.noSuggestionText}>Nenhum local encontrado</Text>
          )}
        </View>
      ) : null}
      <Text style={styles.locationHint}>Busque por cidade, bairro ou endereço.</Text>
    </View>
  );
}

function FilterRow({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <View style={styles.filterRow}>
      <Text style={styles.filterLabel}>{title}</Text>
      <View style={styles.chipList}>{children}</View>
    </View>
  );
}

function FilterChip({
  label,
  selected,
  disabled,
  onPress,
}: {
  label: string;
  selected: boolean;
  disabled: boolean;
  onPress: () => void;
}) {
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={{ selected, disabled }}
      disabled={disabled}
      onPress={onPress}
      style={({ pressed }) => [styles.chip, selected && styles.chipSelected, disabled && styles.disabledShell, pressed && styles.pressed]}
    >
      <Text style={[styles.chipText, selected && styles.chipTextSelected]}>{label}</Text>
    </Pressable>
  );
}

function CaregiverCard({ caregiver }: { caregiver: CaregiverSearchResult }) {
  const servicePreview = caregiver.servicosOferecidos.slice(0, 3);
  const distance = formatDistance(caregiver.distanciaKm);
  const formations = caregiver.formacoes.length > 0
    ? caregiver.formacoes.map(getEducationLabel).join(', ')
    : 'Formação não informada';

  return (
    <Pressable
      accessibilityRole="button"
      onPress={() => router.push({
        pathname: '/caregiver-profile/[id]',
        params: {
          id: caregiver.id,
          ...(caregiver.distanciaKm != null ? { distanciaKm: String(caregiver.distanciaKm) } : {}),
        },
      } as Href)}
      style={({ pressed }) => [styles.card, pressed && styles.cardPressed]}
    >
      <View style={styles.avatar}>
        <Text style={styles.avatarText}>{getInitials(caregiver.nome)}</Text>
      </View>

      <View style={styles.cardBody}>
        <View style={styles.nameRow}>
          <Text numberOfLines={1} style={styles.cardName}>{caregiver.nome}</Text>
        </View>

        <View style={styles.locationLine}>
          <MapPin color={colors.mutedForeground} size={13} strokeWidth={2.3} />
          <Text style={styles.locationText}>{formatLocation(caregiver)}</Text>
        </View>
        {distance ? (
          <View style={styles.distancePill}>
            <Navigation color={colors.primary} size={13} strokeWidth={2.4} />
            <Text style={styles.distanceText}>{distance} de você</Text>
          </View>
        ) : null}
        <Text style={styles.metaText}>{getExperienceLabel(caregiver.experienciaRange)} · {formations}</Text>
        <Text style={styles.availabilityText}>
          {caregiver.disponibilidadeResumo.periodos.length > 0
            ? caregiver.disponibilidadeResumo.periodos.map(getAvailabilityLabel).join(', ')
            : 'Disponibilidade não informada'}
        </Text>

        <View style={styles.serviceChips}>
          {servicePreview.map((service) => (
            <View key={service} style={styles.serviceChip}>
              <Text numberOfLines={1} style={styles.serviceChipText}>{getServiceLabel(service)}</Text>
            </View>
          ))}
        </View>

        <View style={styles.profileLinkRow}>
          <Text style={styles.profileLink}>Ver perfil</Text>
          <ArrowRight color={colors.primary} size={14} strokeWidth={2.6} />
        </View>
      </View>
    </Pressable>
  );
}

function Pagination({
  currentPage,
  totalPages,
  loading,
  onPrevious,
  onNext,
}: {
  currentPage: number;
  totalPages: number;
  loading: boolean;
  onPrevious: () => void;
  onNext: () => void;
}) {
  const previousDisabled = loading || currentPage === 0;
  const nextDisabled = loading || totalPages === 0 || currentPage >= totalPages - 1;

  return (
    <View style={styles.pagination}>
      <Pressable
        accessibilityRole="button"
        accessibilityState={{ disabled: previousDisabled }}
        disabled={previousDisabled}
        onPress={onPrevious}
        style={[styles.pageButton, previousDisabled && styles.pageButtonDisabled]}
      >
        <Text style={[styles.pageButtonText, previousDisabled && styles.pageButtonTextDisabled]}>Anterior</Text>
      </Pressable>
      <Text style={styles.pageText}>Página {currentPage + 1} de {Math.max(1, totalPages)}</Text>
      <Pressable
        accessibilityRole="button"
        accessibilityState={{ disabled: nextDisabled }}
        disabled={nextDisabled}
        onPress={onNext}
        style={[styles.pageButton, nextDisabled && styles.pageButtonDisabled]}
      >
        <Text style={[styles.pageButtonText, nextDisabled && styles.pageButtonTextDisabled]}>Próxima</Text>
      </Pressable>
    </View>
  );
}

function EmptyState({ onClear }: { onClear: () => void }) {
  return (
    <View style={styles.emptyState}>
      <Text style={styles.emptyTitle}>Nenhum cuidador encontrado</Text>
      <Text style={styles.emptyText}>Tente ajustar os filtros para encontrar mais profissionais.</Text>
      <PrimaryButton label="Limpar filtros" variant="secondary" onPress={onClear} style={styles.emptyButton} />
    </View>
  );
}

function ErrorState({ message, onRetry }: { message: string; onRetry: () => void }) {
  return (
    <View style={styles.emptyState}>
      <Text style={styles.emptyTitle}>Não foi possível carregar</Text>
      <Text style={styles.emptyText}>{message}</Text>
      <PrimaryButton label="Tentar novamente" variant="secondary" onPress={onRetry} style={styles.emptyButton} />
    </View>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    paddingTop: spacing.md,
    paddingHorizontal: spacing.lg,
    paddingBottom: spacing.xxl,
  },
  headerContent: {
    gap: spacing.lg,
    marginBottom: spacing.md,
  },
  locationCombobox: {
    gap: spacing.xs,
  },
  inputLabel: {
    fontFamily: fontFamily.semiBold,
    fontSize: 13,
    color: colors.foreground,
  },
  locationInputShell: {
    minHeight: 54,
    borderRadius: radii.lg,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
    paddingHorizontal: spacing.lg,
  },
  locationInputShellOpen: {
    borderBottomLeftRadius: 0,
    borderBottomRightRadius: 0,
    borderBottomColor: colors.card,
  },
  locationInputFocused: {
    borderColor: colors.primary,
    backgroundColor: colors.primaryForeground,
  },
  disabledShell: {
    opacity: 0.62,
  },
  locationInput: {
    flex: 1,
    minHeight: 50,
    paddingVertical: 0,
    fontFamily: fontFamily.medium,
    fontSize: 14,
    color: colors.foreground,
  },
  locationClearButton: {
    width: 30,
    height: 30,
    borderRadius: radii.full,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.muted,
  },
  locationHint: {
    fontFamily: fontFamily.regular,
    fontSize: 11,
    lineHeight: 16,
    color: colors.mutedForeground,
  },
  suggestionBox: {
    marginTop: -spacing.xs,
    borderTopLeftRadius: 0,
    borderTopRightRadius: 0,
    borderBottomLeftRadius: radii.xl,
    borderBottomRightRadius: radii.xl,
    borderWidth: 1,
    borderTopWidth: 0,
    borderColor: colors.border,
    backgroundColor: colors.card,
    overflow: 'hidden',
    ...shadows.card,
  },
  suggestionLoading: {
    minHeight: 54,
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
    paddingHorizontal: spacing.md,
  },
  suggestionItem: {
    minHeight: 58,
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  suggestionItemSelected: {
    backgroundColor: colors.secondary,
  },
  suggestionIcon: {
    width: 32,
    height: 32,
    borderRadius: radii.full,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.secondary,
  },
  suggestionCopy: {
    flex: 1,
    minWidth: 0,
  },
  suggestionLabel: {
    fontFamily: fontFamily.bold,
    fontSize: 13,
    color: colors.foreground,
  },
  suggestionType: {
    marginTop: 2,
    fontFamily: fontFamily.medium,
    fontSize: 11,
    color: colors.mutedForeground,
  },
  noSuggestionText: {
    padding: spacing.md,
    fontFamily: fontFamily.medium,
    fontSize: 13,
    color: colors.mutedForeground,
  },
  filterHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: spacing.md,
  },
  filterTitleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
  },
  filterTitle: {
    fontFamily: fontFamily.extraBold,
    fontSize: 17,
    color: colors.foreground,
  },
  clearButton: {
    minHeight: 34,
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
    borderRadius: radii.full,
    backgroundColor: colors.secondary,
    paddingHorizontal: spacing.md,
  },
  clearButtonText: {
    fontFamily: fontFamily.bold,
    fontSize: 11,
    color: colors.primary,
  },
  filterRow: {
    gap: spacing.sm,
  },
  filterLabel: {
    fontFamily: fontFamily.bold,
    fontSize: 11,
    color: colors.mutedForeground,
    textTransform: 'uppercase',
  },
  chipList: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.sm,
  },
  chip: {
    minHeight: 38,
    maxWidth: '100%',
    borderRadius: radii.full,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    justifyContent: 'center',
    paddingHorizontal: spacing.md,
  },
  chipSelected: {
    borderColor: colors.primary,
    backgroundColor: colors.primary,
    ...shadows.glow,
  },
  chipText: {
    fontFamily: fontFamily.semiBold,
    fontSize: 12,
    color: colors.foreground,
  },
  chipTextSelected: {
    color: colors.primaryForeground,
  },
  pressed: {
    opacity: 0.8,
  },
  resultsHeader: {
    gap: spacing.xs,
    marginTop: spacing.xs,
  },
  searchActions: {
    gap: spacing.sm,
  },
  searchButton: {
    minHeight: 50,
  },
  currentLocationButton: {
    minHeight: 46,
  },
  locationError: {
    marginTop: -spacing.sm,
    fontFamily: fontFamily.medium,
    fontSize: 12,
    lineHeight: 18,
    color: colors.destructive,
  },
  resultsCount: {
    fontFamily: fontFamily.extraBold,
    fontSize: 14,
    color: colors.foreground,
  },
  resultsHint: {
    fontFamily: fontFamily.medium,
    fontSize: 11,
    color: colors.mutedForeground,
  },
  separator: {
    height: spacing.md,
  },
  card: {
    flexDirection: 'row',
    gap: spacing.md,
    borderRadius: radii.xl,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    padding: spacing.md,
    ...shadows.card,
  },
  cardPressed: {
    opacity: 0.86,
    transform: [{ scale: 0.99 }],
  },
  avatar: {
    width: 58,
    height: 58,
    borderRadius: radii.lg,
    backgroundColor: colors.mint,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    fontFamily: fontFamily.extraBold,
    fontSize: 18,
    color: colors.mintForeground,
  },
  cardBody: {
    flex: 1,
    minWidth: 0,
    alignItems: 'stretch',
    gap: spacing.xs,
  },
  nameRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
  },
  cardName: {
    flex: 1,
    fontFamily: fontFamily.extraBold,
    fontSize: 15,
    color: colors.foreground,
  },
  locationLine: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
  },
  locationText: {
    flex: 1,
    fontFamily: fontFamily.medium,
    fontSize: 12,
    lineHeight: 18,
    color: colors.mutedForeground,
  },
  distancePill: {
    alignSelf: 'flex-start',
    minHeight: 26,
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
    borderRadius: radii.full,
    backgroundColor: colors.secondary,
    paddingHorizontal: spacing.sm,
  },
  distanceText: {
    fontFamily: fontFamily.bold,
    fontSize: 11,
    color: colors.primary,
  },
  metaText: {
    fontFamily: fontFamily.medium,
    fontSize: 12,
    lineHeight: 18,
    color: colors.foreground,
  },
  availabilityText: {
    fontFamily: fontFamily.semiBold,
    fontSize: 12,
    lineHeight: 18,
    color: colors.primary,
  },
  serviceChips: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.xs,
  },
  serviceChip: {
    maxWidth: '100%',
    borderRadius: radii.full,
    backgroundColor: colors.secondary,
    paddingHorizontal: spacing.sm,
    paddingVertical: 4,
  },
  serviceChipText: {
    fontFamily: fontFamily.semiBold,
    fontSize: 10,
    color: colors.secondaryForeground,
  },
  profileLink: {
    fontFamily: fontFamily.extraBold,
    fontSize: 12,
    color: colors.primary,
  },
  profileLinkRow: {
    alignSelf: 'flex-end',
    minHeight: 30,
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
    marginTop: spacing.xs,
  },
  pagination: {
    marginTop: spacing.lg,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: spacing.sm,
  },
  pageButton: {
    minHeight: 42,
    minWidth: 92,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: radii.full,
    backgroundColor: colors.secondary,
    paddingHorizontal: spacing.md,
  },
  pageButtonDisabled: {
    opacity: 0.45,
  },
  pageButtonText: {
    fontFamily: fontFamily.bold,
    fontSize: 12,
    color: colors.primary,
  },
  pageButtonTextDisabled: {
    color: colors.mutedForeground,
  },
  pageText: {
    flex: 1,
    textAlign: 'center',
    fontFamily: fontFamily.bold,
    fontSize: 12,
    color: colors.foreground,
  },
  emptyState: {
    alignItems: 'center',
    gap: spacing.sm,
    borderRadius: radii.xl,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    padding: spacing.xl,
    marginTop: spacing.md,
    ...shadows.card,
  },
  emptyTitle: {
    fontFamily: fontFamily.extraBold,
    fontSize: 17,
    color: colors.foreground,
  },
  emptyText: {
    textAlign: 'center',
    fontFamily: fontFamily.regular,
    fontSize: 13,
    lineHeight: 20,
    color: colors.mutedForeground,
  },
  emptyButton: {
    marginTop: spacing.sm,
    minHeight: 46,
  },
});
