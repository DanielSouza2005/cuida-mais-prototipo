package br.com.cuidaplus.api.caregiver;

import br.com.cuidaplus.api.caregiver.dto.CaregiverAvailabilityResponse;
import br.com.cuidaplus.api.caregiver.dto.CaregiverDetailsResponse;
import br.com.cuidaplus.api.caregiver.dto.CaregiverLocationSuggestionResponse;
import br.com.cuidaplus.api.caregiver.dto.CaregiverSearchItemResponse;
import br.com.cuidaplus.api.caregiver.dto.CaregiverSearchPageResponse;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.profile.AddressFields;
import br.com.cuidaplus.api.profile.CaregiverAvailability;
import br.com.cuidaplus.api.profile.CaregiverProfile;
import br.com.cuidaplus.api.profile.CaregiverProfileRepository;
import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.profile.FormacaoCuidador;
import br.com.cuidaplus.api.profile.ModalidadeAtendimento;
import br.com.cuidaplus.api.profile.PeriodoDisponibilidade;
import br.com.cuidaplus.api.profile.ServicoOferecido;
import br.com.cuidaplus.api.profile.TempoExperiencia;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserType;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CaregiverSearchService {

  private static final int DEFAULT_PAGE_SIZE = 5;
  private static final int MAX_PAGE_SIZE = 50;
  private static final int LOCATION_LIMIT = 12;
  private static final double EARTH_RADIUS_KM = 6371.0088;

  private final CaregiverProfileRepository caregiverProfileRepository;

  public CaregiverSearchService(CaregiverProfileRepository caregiverProfileRepository) {
    this.caregiverProfileRepository = caregiverProfileRepository;
  }

  @Transactional(readOnly = true)
  public CaregiverSearchPageResponse search(CaregiverSearchFilters filters) {
    int page = Math.max(0, filters.page());
    int size = Math.min(Math.max(1, filters.size()), MAX_PAGE_SIZE);

    List<CaregiverSearchItemResponse> filtered = caregiverProfileRepository.findAll().stream()
      .filter(this::isActiveCaregiver)
      .filter(profile -> matchesFilters(profile, filters))
      .sorted(comparator(filters))
      .map(profile -> toSearchItem(profile, filters.originLat(), filters.originLng()))
      .toList();

    int totalElements = filtered.size();
    int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    int fromIndex = Math.min(page * size, totalElements);
    int toIndex = Math.min(fromIndex + size, totalElements);

    return new CaregiverSearchPageResponse(
      filtered.subList(fromIndex, toIndex),
      page,
      size,
      totalElements,
      totalPages,
      page == 0,
      totalPages == 0 || page >= totalPages - 1
    );
  }

  @Transactional(readOnly = true)
  public List<CaregiverLocationSuggestionResponse> locations(String query) {
    String normalizedQuery = normalize(query);
    Map<String, CaregiverLocationSuggestionResponse> suggestions = new LinkedHashMap<>();

    caregiverProfileRepository.findAll().stream()
      .filter(this::isActiveCaregiver)
      .forEach(profile -> {
        AddressFields address = safeAddress(profile);
        String city = trimToNull(address.getCidade());
        String neighborhood = trimToNull(address.getBairro());
        String state = trimToNull(address.getEstado());

        if (city == null || state == null) {
          return;
        }

        addSuggestion(suggestions, normalizedQuery, citySuggestion(city, state));

        if (neighborhood != null) {
          addSuggestion(suggestions, normalizedQuery, neighborhoodSuggestion(neighborhood, city, state));
        }
      });

    return suggestions.values().stream()
      .sorted(Comparator.comparing(CaregiverLocationSuggestionResponse::label, String.CASE_INSENSITIVE_ORDER))
      .limit(LOCATION_LIMIT)
      .toList();
  }

  @Transactional(readOnly = true)
  public CaregiverDetailsResponse details(UUID id, Double originLat, Double originLng) {
    CaregiverProfile profile = caregiverProfileRepository.findById(id)
      .filter(this::isActiveCaregiver)
      .orElseThrow(() -> new BusinessException("Cuidador não encontrado.", HttpStatus.NOT_FOUND));

    AddressFields address = safeAddress(profile);

    return new CaregiverDetailsResponse(
      profile.getId(),
      profile.getUser().getFullName(),
      address.getCidade(),
      address.getBairro(),
      address.getEstado(),
      distanceKm(address, originLat, originLng),
      profile.getTempoExperiencia(),
      new LinkedHashSet<>(profile.getFormacoes()),
      profile.getFormacaoOutro(),
      profile.getBiografia(),
      new LinkedHashSet<>(profile.getModalidades()),
      profile.getModalidadeOutro(),
      new LinkedHashSet<>(profile.getServicosOferecidos()),
      profile.getServicoOutro(),
      toAvailabilityResponse(safeAvailability(profile)),
      profile.getCreatedAt(),
      profile.getUser().getStatus()
    );
  }

  private boolean matchesFilters(CaregiverProfile profile, CaregiverSearchFilters filters) {
    AddressFields address = safeAddress(profile);
    CaregiverAvailability availability = safeAvailability(profile);

    return matchesText(profile.getUser().getFullName(), filters.name())
      && matchesText(address.getCidade(), filters.city())
      && matchesText(address.getBairro(), filters.neighborhood())
      && matchesText(address.getEstado(), filters.state())
      && matchesAny(profile.getServicosOferecidos(), filters.services())
      && matchesAny(profile.getModalidades(), filters.modalities())
      && matchesAny(profile.getFormacoes(), filters.formations())
      && matchesAny(availability.getPeriodos(), filters.availabilityPeriods())
      && matchesAny(availability.getDiasSemana(), filters.availabilityDays())
      && (filters.experienceRange() == null || filters.experienceRange() == profile.getTempoExperiencia());
  }

  private Comparator<CaregiverProfile> comparator(CaregiverSearchFilters filters) {
    return Comparator
      .comparing((CaregiverProfile profile) -> localPriority(profile, filters))
      .thenComparing(profile -> distanceSortValue(profile, filters))
      .thenComparing(profile -> safeString(safeAddress(profile).getCidade()), String.CASE_INSENSITIVE_ORDER)
      .thenComparing(profile -> safeString(safeAddress(profile).getBairro()), String.CASE_INSENSITIVE_ORDER)
      .thenComparing(profile -> safeString(profile.getUser().getFullName()), String.CASE_INSENSITIVE_ORDER);
  }

  private int localPriority(CaregiverProfile profile, CaregiverSearchFilters filters) {
    AddressFields address = safeAddress(profile);
    int priority = 0;

    if (hasText(filters.city()) && matchesText(address.getCidade(), filters.city())) {
      priority -= 2;
    }

    if (hasText(filters.neighborhood()) && matchesText(address.getBairro(), filters.neighborhood())) {
      priority -= 3;
    }

    return priority;
  }

  private <T> boolean matchesAny(Set<T> source, Set<T> filters) {
    return filters.isEmpty() || source.stream().anyMatch(filters::contains);
  }

  private boolean matchesText(String value, String filter) {
    return !hasText(filter) || normalize(value).contains(normalize(filter));
  }

  private CaregiverSearchItemResponse toSearchItem(CaregiverProfile profile, Double originLat, Double originLng) {
    AddressFields address = safeAddress(profile);

    return new CaregiverSearchItemResponse(
      profile.getId(),
      profile.getUser().getFullName(),
      address.getCidade(),
      address.getBairro(),
      address.getEstado(),
      distanceKm(address, originLat, originLng),
      profile.getTempoExperiencia(),
      new LinkedHashSet<>(profile.getFormacoes()),
      profile.getFormacaoOutro(),
      new LinkedHashSet<>(profile.getServicosOferecidos()),
      new LinkedHashSet<>(profile.getModalidades()),
      toAvailabilityResponse(safeAvailability(profile)),
      summarize(profile.getBiografia())
    );
  }

  private CaregiverAvailabilityResponse toAvailabilityResponse(CaregiverAvailability availability) {
    return new CaregiverAvailabilityResponse(
      new LinkedHashSet<>(availability.getDiasSemana()),
      new LinkedHashSet<>(availability.getPeriodos()),
      availability.getHorarioInicio(),
      availability.getHorarioFim(),
      availability.getObservacao()
    );
  }

  private double distanceSortValue(CaregiverProfile profile, CaregiverSearchFilters filters) {
    Double distance = distanceKm(safeAddress(profile), filters.originLat(), filters.originLng());
    return distance == null ? Double.MAX_VALUE : distance;
  }

  private Double distanceKm(AddressFields address, Double originLat, Double originLng) {
    Double targetLat = address.getLatitude() == null ? null : address.getLatitude().doubleValue();
    Double targetLng = address.getLongitude() == null ? null : address.getLongitude().doubleValue();

    if (!hasValidCoordinates(originLat, originLng) || !hasValidCoordinates(targetLat, targetLng)) {
      return null;
    }

    double latDistance = Math.toRadians(targetLat - originLat);
    double lngDistance = Math.toRadians(targetLng - originLng);
    double originLatRad = Math.toRadians(originLat);
    double targetLatRad = Math.toRadians(targetLat);
    double haversine = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
      + Math.cos(originLatRad) * Math.cos(targetLatRad)
      * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

    return Math.round(EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine)) * 10.0) / 10.0;
  }

  private boolean hasValidCoordinates(Double latitude, Double longitude) {
    return latitude != null
      && longitude != null
      && latitude >= -90
      && latitude <= 90
      && longitude >= -180
      && longitude <= 180;
  }

  private void addSuggestion(Map<String, CaregiverLocationSuggestionResponse> suggestions, String normalizedQuery, CaregiverLocationSuggestionResponse suggestion) {
    if (normalizedQuery.isBlank() || normalize(suggestion.label()).contains(normalizedQuery)) {
      suggestions.putIfAbsent(suggestion.id(), suggestion);
    }
  }

  private CaregiverLocationSuggestionResponse citySuggestion(String city, String state) {
    return new CaregiverLocationSuggestionResponse(
      "city-" + slug(city) + "-" + slug(state),
      city + " - " + state,
      "CITY",
      city,
      null,
      state
    );
  }

  private CaregiverLocationSuggestionResponse neighborhoodSuggestion(String neighborhood, String city, String state) {
    return new CaregiverLocationSuggestionResponse(
      "neighborhood-" + slug(neighborhood) + "-" + slug(city) + "-" + slug(state),
      neighborhood + ", " + city + " - " + state,
      "NEIGHBORHOOD",
      city,
      neighborhood,
      state
    );
  }

  private boolean isActiveCaregiver(CaregiverProfile profile) {
    User user = profile.getUser();
    return (user.getUserType() == UserType.CUIDADOR || user.getUserType() == UserType.CAREGIVER)
      && "ACTIVE".equalsIgnoreCase(user.getStatus());
  }

  private AddressFields safeAddress(CaregiverProfile profile) {
    return profile.getEnderecoAtendimento() == null ? new AddressFields() : profile.getEnderecoAtendimento();
  }

  private CaregiverAvailability safeAvailability(CaregiverProfile profile) {
    return profile.getDisponibilidade() == null ? new CaregiverAvailability() : profile.getDisponibilidade();
  }

  private String summarize(String value) {
    if (value == null || value.length() <= 160) {
      return value;
    }

    return value.substring(0, 157).trim() + "...";
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  private String trimToNull(String value) {
    return hasText(value) ? value.trim() : null;
  }

  private String safeString(String value) {
    return value == null ? "" : value;
  }

  private String slug(String value) {
    return normalize(value).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
  }

  private String normalize(String value) {
    if (value == null) {
      return "";
    }

    return Normalizer.normalize(value, Normalizer.Form.NFD)
      .replaceAll("\\p{M}", "")
      .toLowerCase(Locale.ROOT)
      .trim();
  }

  public record CaregiverSearchFilters(
    String name,
    String city,
    String neighborhood,
    String state,
    Set<PeriodoDisponibilidade> availabilityPeriods,
    Set<DiaSemana> availabilityDays,
    Set<ServicoOferecido> services,
    Set<ModalidadeAtendimento> modalities,
    TempoExperiencia experienceRange,
    Set<FormacaoCuidador> formations,
    Double originLat,
    Double originLng,
    int page,
    int size
  ) {
    public CaregiverSearchFilters {
      availabilityPeriods = availabilityPeriods == null ? Set.of() : availabilityPeriods;
      availabilityDays = availabilityDays == null ? Set.of() : availabilityDays;
      services = services == null ? Set.of() : services;
      modalities = modalities == null ? Set.of() : modalities;
      formations = formations == null ? Set.of() : formations;
      page = Math.max(0, page);
      size = size <= 0 ? DEFAULT_PAGE_SIZE : size;
    }
  }
}
