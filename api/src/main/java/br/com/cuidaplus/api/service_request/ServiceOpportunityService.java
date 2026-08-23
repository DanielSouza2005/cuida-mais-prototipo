package br.com.cuidaplus.api.service_request;

import br.com.cuidaplus.api.care_routine.StructuredCareItemMapper;
import br.com.cuidaplus.api.care_contract.CareContractRepository;
import br.com.cuidaplus.api.caregiver.dto.CaregiverLocationSuggestionResponse;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.notification.*;
import br.com.cuidaplus.api.profile.*;
import br.com.cuidaplus.api.service_request.dto.*;
import br.com.cuidaplus.api.status_history.*;
import br.com.cuidaplus.api.user.*;
import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceOpportunityService {
  private static final double EARTH_RADIUS_KM = 6371.0088;
  private static final int LOCATION_LIMIT = 12;
  private final ServiceRequestRepository requests;
  private final UserService users;
  private final NotificationService notifications;
  private final StatusHistoryService history;
  private final ServiceRequestDecisionService decisions;
  private final CareContractRepository contracts;

  public ServiceOpportunityService(ServiceRequestRepository requests, UserService users, NotificationService notifications, StatusHistoryService history, ServiceRequestDecisionService decisions, CareContractRepository contracts) {
    this.requests = requests;
    this.users = users;
    this.notifications = notifications;
    this.history = history;
    this.decisions = decisions;
    this.contracts = contracts;
  }

  @Transactional(readOnly = true)
  public ServiceOpportunityPageResponse search(UUID userId, String city, String neighborhood, String state, HiringType hiringType, Double originLat, Double originLng, int page, int size) {
    User caregiver = requireCaregiver(userId);
    PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<ServiceRequest> result = requests.searchOpportunities(Instant.now(), lowerFilter(city), lowerFilter(neighborhood), lowerFilter(state), hiringType, pageable);
    List<ServiceOpportunityResponse> content = result.getContent().stream().map(item -> response(item, application(item, caregiver), originLat, originLng)).toList();
    return new ServiceOpportunityPageResponse(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages(), result.isLast());
  }

  @Transactional
  public ServiceOpportunityResponse details(UUID userId, UUID id, Double originLat, Double originLng) {
    User caregiver = requireCaregiver(userId);
    ServiceRequest opportunity = requests.findById(id).filter(this::isOpportunity)
      .orElseThrow(() -> new BusinessException("Serviço disponível não encontrado.", HttpStatus.NOT_FOUND));
    ServiceRequest application = application(opportunity, caregiver);
    expireApplicationIfNeeded(application);
    if ((opportunity.getStatus() != ServiceRequestStatus.ABERTA || opportunity.getExpiresAt().isBefore(Instant.now())) && application == null) {
      throw new BusinessException("Este serviço não está mais disponível.", HttpStatus.GONE);
    }
    return response(opportunity, application, originLat, originLng);
  }

  @Transactional(readOnly = true)
  public List<CaregiverLocationSuggestionResponse> locations(UUID userId, String query) {
    requireCaregiver(userId);
    String normalizedQuery = normalize(query);
    Map<String, CaregiverLocationSuggestionResponse> suggestions = new LinkedHashMap<>();
    requests.findDiscoverableOpportunities(Instant.now()).forEach(opportunity -> {
      AddressFields address = opportunity.getAssistedPerson().getEnderecoCuidado();
      String city = clean(address.getCidade()), neighborhood = clean(address.getBairro()), state = clean(address.getEstado());
      if (city == null || state == null) return;
      addSuggestion(suggestions, normalizedQuery, citySuggestion(city, state));
      if (neighborhood != null) addSuggestion(suggestions, normalizedQuery, neighborhoodSuggestion(neighborhood, city, state));
    });
    return suggestions.values().stream().sorted(Comparator.comparing(CaregiverLocationSuggestionResponse::label, String.CASE_INSENSITIVE_ORDER)).limit(LOCATION_LIMIT).toList();
  }

  @Transactional(readOnly = true)
  public AcceptedOpportunityContractResponse acceptedContract(UUID userId, UUID opportunityId) {
    User caregiver = requireCaregiver(userId);
    return contracts.findFirstByCaregiverUserAndServiceRequestSourceOpportunityId(caregiver, opportunityId)
      .map(contract -> new AcceptedOpportunityContractResponse(contract.getId()))
      .orElseThrow(() -> new BusinessException("Contratação do interesse aceito não encontrada.", HttpStatus.NOT_FOUND));
  }

  @Transactional
  public ServiceOpportunityPageResponse applications(UUID userId, ServiceRequestStatus status, int page, int size) {
    User caregiver = requireCaregiver(userId);
    requests.findByCaregiverUserAndStatusOrderByCreatedAtDesc(caregiver, ServiceRequestStatus.PENDENTE).stream()
      .filter(item -> item.getInitiatedBy() == ServiceRequestInitiator.CAREGIVER)
      .forEach(this::expireApplicationIfNeeded);
    PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<ServiceRequest> result = requests.searchVisibleApplications(caregiver, status, pageable);
    List<ServiceOpportunityResponse> content = result.getContent().stream()
      .filter(item -> item.getSourceOpportunity() != null)
      .map(item -> response(item.getSourceOpportunity(), item, null, null)).toList();
    return new ServiceOpportunityPageResponse(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages(), result.isLast());
  }

  @Transactional
  public ServiceOpportunityResponse apply(UUID userId, UUID opportunityId) {
    User caregiver = requireCaregiver(userId);
    ServiceRequest opportunity = requests.findOpenOpportunityForUpdate(opportunityId)
      .orElseThrow(() -> new BusinessException("Este serviço não está mais disponível.", HttpStatus.CONFLICT));
    if (opportunity.getExpiresAt().isBefore(Instant.now())) {
      throw new BusinessException("Este serviço não está mais disponível.", HttpStatus.CONFLICT);
    }
    if (opportunity.getResponsibleUser().getId().equals(caregiver.getId())) {
      throw new BusinessException("Você não pode demonstrar interesse no próprio serviço.", HttpStatus.FORBIDDEN);
    }
    if (requests.existsBySourceOpportunityAndCaregiverUser(opportunity, caregiver)) {
      throw new BusinessException("Você já demonstrou interesse neste serviço.", HttpStatus.CONFLICT);
    }
    ServiceRequest application = copyOpportunity(opportunity);
    application.setCaregiverUser(caregiver);
    application.setRequesterUser(caregiver);
    application.setInitiatedBy(ServiceRequestInitiator.CAREGIVER);
    application.setSourceOpportunity(opportunity);
    application.setStatus(ServiceRequestStatus.PENDENTE);
    ServiceRequest saved = requests.save(application);
    history.record(StatusHistoryEntityType.SERVICE_REQUEST, saved.getId(), null, ServiceRequestStatus.PENDENTE.name(), caregiver, null);
    notifications.create(opportunity.getResponsibleUser(), NotificationType.SERVICE_OPPORTUNITY_APPLICATION_CREATED, "Novo cuidador interessado", "Um cuidador demonstrou interesse no seu serviço.", saved.getId());
    return response(opportunity, saved, null, null);
  }

  @Transactional
  public void accept(UUID userId, UUID applicationId) {
    User responsible = requireResponsible(userId);
    ServiceRequest application = requests.findForUpdateByIdAndResponsibleUser(applicationId, responsible)
      .orElseThrow(() -> new BusinessException("Solicitação não encontrada.", HttpStatus.NOT_FOUND));
    requireCaregiverApplication(application);
    ServiceRequest opportunity = requests.findOpenOpportunityForUpdate(application.getSourceOpportunity().getId())
      .orElseThrow(() -> new BusinessException("Este serviço não está mais disponível.", HttpStatus.CONFLICT));
    if (opportunity.getExpiresAt().isBefore(Instant.now())) {
      throw new BusinessException("Este serviço não está mais disponível.", HttpStatus.CONFLICT);
    }
    decisions.accept(application, responsible);
    opportunity.setStatus(ServiceRequestStatus.ACEITA);
    history.record(StatusHistoryEntityType.SERVICE_REQUEST, opportunity.getId(), ServiceRequestStatus.ABERTA.name(), ServiceRequestStatus.ACEITA.name(), responsible, null);
    notifications.create(responsible, NotificationType.SERVICE_PUBLICATION_STATUS_UPDATED, "Serviço atualizado", "Um cuidador foi aceito e a publicação foi encerrada.", RelatedEntityType.SERVICE_REQUEST, opportunity.getId());
  }

  @Transactional
  public void reject(UUID userId, UUID applicationId, String reason) {
    User responsible = requireResponsible(userId);
    ServiceRequest application = requests.findForUpdateByIdAndResponsibleUser(applicationId, responsible)
      .orElseThrow(() -> new BusinessException("Solicitação não encontrada.", HttpStatus.NOT_FOUND));
    requireCaregiverApplication(application);
    decisions.reject(application, responsible, reason);
  }

  private void requireCaregiverApplication(ServiceRequest application) {
    if (application.getInitiatedBy() != ServiceRequestInitiator.CAREGIVER || application.getSourceOpportunity() == null) {
      throw new BusinessException("Somente manifestações iniciadas por cuidadores podem ser respondidas aqui.", HttpStatus.FORBIDDEN);
    }
  }

  private ServiceRequest application(ServiceRequest opportunity, User caregiver) {
    return requests.findBySourceOpportunityAndCaregiverUser(opportunity, caregiver).stream().findFirst().orElse(null);
  }

  private void expireApplicationIfNeeded(ServiceRequest application) {
    if (application != null && application.getStatus() == ServiceRequestStatus.PENDENTE && application.getExpiresAt().isBefore(Instant.now())) {
      application.setStatus(ServiceRequestStatus.EXPIRADA);
      history.record(StatusHistoryEntityType.SERVICE_REQUEST, application.getId(), ServiceRequestStatus.PENDENTE.name(), ServiceRequestStatus.EXPIRADA.name(), application.getResponsibleUser(), null);
    }
  }

  private boolean isOpportunity(ServiceRequest request) {
    return request.getCaregiverUser() == null && request.getInitiatedBy() == ServiceRequestInitiator.RESPONSIBLE;
  }

  private ServiceRequest copyOpportunity(ServiceRequest source) {
    ServiceRequest target = new ServiceRequest();
    target.setResponsibleUser(source.getResponsibleUser());
    target.setAssistedPerson(source.getAssistedPerson());
    target.setCareRoutine(source.getCareRoutine());
    target.setCareRoutineNameSnapshot(source.getCareRoutineNameSnapshot());
    target.setHiringType(source.getHiringType());
    target.setStartDate(source.getStartDate());
    target.setEndDate(source.getEndDate());
    target.setSpecificDates(new LinkedHashSet<>(source.getSpecificDates()));
    Set<ServiceRequestScheduleDay> schedule = new LinkedHashSet<>();
    source.getScheduleDays().forEach(item -> { ServiceRequestScheduleDay copy = new ServiceRequestScheduleDay(); copy.setWeekday(item.getWeekday()); copy.setStartTime(item.getStartTime()); copy.setEndTime(item.getEndTime()); schedule.add(copy); });
    target.setScheduleDays(schedule);
    target.setNeedsDescription(source.getNeedsDescription());
    target.setActivities(new LinkedHashSet<>(source.getActivities()));
    target.setActivityOther(source.getActivityOther());
    target.setAdditionalNotes(source.getAdditionalNotes());
    target.setNegotiationNotes(source.getNegotiationNotes());
    source.getCareItemsSnapshot().forEach(item -> target.addCareItemSnapshot(copySnapshot(source, item)));
    return target;
  }

  private ServiceRequestCareItemSnapshot copySnapshot(ServiceRequest source, ServiceRequestCareItemSnapshot item) {
    ServiceRequestCareItemSnapshot copy = new ServiceRequestCareItemSnapshot();
    copy.setOriginalCareRoutine(source.getCareRoutine()); copy.setTitle(item.getTitle()); copy.setDescription(item.getDescription()); copy.setSortOrder(item.getSortOrder()); copy.setCategory(item.getCategory()); copy.setCustomCategory(item.getCustomCategory()); copy.setPriority(item.getPriority()); copy.setRecurrenceType(item.getRecurrenceType()); copy.setScheduledTime(item.getScheduledTime()); copy.setIntervalDays(item.getIntervalDays()); copy.setWeekdays(new LinkedHashSet<>(item.getWeekdays())); copy.setReminderEnabled(item.getReminderEnabled()); copy.setReminderMinutesBefore(item.getReminderMinutesBefore()); copy.setReminderAtScheduledTime(item.isReminderAtScheduledTime()); copy.setOverdueReminderEnabled(item.isOverdueReminderEnabled()); copy.setOverdueAfterMinutes(item.getOverdueAfterMinutes()); copy.setRepeatWhilePending(item.isRepeatWhilePending()); copy.setRepeatIntervalMinutes(item.getRepeatIntervalMinutes()); copy.setImportant(item.isImportant()); copy.setNotifyResponsibleIfImportant(item.isNotifyResponsibleIfImportant()); copy.setRequiresCompletionPhoto(item.isRequiresCompletionPhoto()); copy.setNotes(item.getNotes()); copy.setMedication(StructuredCareItemMapper.copyMedication(item.getMedication()));
    return copy;
  }

  private ServiceOpportunityResponse response(ServiceRequest opportunity, ServiceRequest application, Double originLat, Double originLng) {
    AddressFields address = opportunity.getAssistedPerson().getEnderecoCuidado();
    var routine = opportunity.getCareRoutine() == null ? null : new ServiceOpportunityResponse.CareRoutine(opportunity.getCareRoutine().getId(), opportunity.getCareRoutineNameSnapshot(), opportunity.getCareItemsSnapshot().stream().map(this::safeCareItem).toList());
    return new ServiceOpportunityResponse(opportunity.getId(), application == null ? null : application.getId(), opportunity.getStatus(), application == null ? null : application.getStatus(), opportunity.getHiringType(), opportunity.getStartDate(), opportunity.getEndDate(), new LinkedHashSet<>(opportunity.getSpecificDates()), opportunity.getScheduleDays().stream().map(day -> new ServiceOpportunityResponse.Schedule(day.getWeekday(), day.getStartTime(), day.getEndTime())).toList(), address.getCidade(), address.getBairro(), address.getEstado(), "Pessoa assistida", opportunity.getAssistedPerson().getGrauDependencia(), opportunity.getAssistedPerson().getMobilidade(), opportunity.getNeedsDescription(), routine, distanceKm(address, originLat, originLng), opportunity.getCreatedAt(), opportunity.getExpiresAt());
  }

  private User requireCaregiver(UUID id) {
    User user = users.findById(id);
    if (user.getUserType() != UserType.CUIDADOR && user.getUserType() != UserType.CAREGIVER) throw new BusinessException("Acesso permitido apenas para cuidadores.", HttpStatus.FORBIDDEN);
    return user;
  }

  private User requireResponsible(UUID id) {
    User user = users.findById(id);
    if (user.getUserType() != UserType.RESPONSAVEL && user.getUserType() != UserType.FAMILY) throw new BusinessException("Acesso permitido apenas para responsáveis.", HttpStatus.FORBIDDEN);
    return user;
  }

  private ServiceOpportunityResponse.CareItem safeCareItem(ServiceRequestCareItemSnapshot item) {
    if (item.getCategory() == null) {
      return new ServiceOpportunityResponse.CareItem("Cuidado", null, null, null, item.getScheduledTime());
    }
    String title = switch (item.getCategory()) {
      case MEDICACAO -> "Apoio com medicação";
      case ALIMENTACAO -> "Apoio com alimentação";
      case HIDRATACAO -> "Apoio com hidratação";
      case HIGIENE_BANHO -> "Higiene e banho";
      case MOBILIDADE -> "Apoio à mobilidade";
      case EXERCICIO -> "Exercício";
      case CURATIVO -> "Curativo";
      case SINAIS_VITAIS -> "Acompanhamento de sinais vitais";
      case CONSULTA_COMPROMISSO -> "Consulta ou compromisso";
      case PERSONALIZADA -> "Outro cuidado";
    };
    return new ServiceOpportunityResponse.CareItem(title, null, item.getCategory(), null, item.getScheduledTime());
  }

  private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
  private String lowerFilter(String value) { String cleaned = clean(value); return cleaned == null ? "" : cleaned.toLowerCase(Locale.ROOT); }
  private Double distanceKm(AddressFields address, Double originLat, Double originLng) {
    Double targetLat = address.getLatitude() == null ? null : address.getLatitude().doubleValue();
    Double targetLng = address.getLongitude() == null ? null : address.getLongitude().doubleValue();
    if (!validCoordinates(originLat, originLng) || !validCoordinates(targetLat, targetLng)) return null;
    double latDistance = Math.toRadians(targetLat - originLat), lngDistance = Math.toRadians(targetLng - originLng);
    double originLatRad = Math.toRadians(originLat), targetLatRad = Math.toRadians(targetLat);
    double haversine = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(originLatRad) * Math.cos(targetLatRad) * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
    return Math.round(EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine)) * 10.0) / 10.0;
  }
  private boolean validCoordinates(Double latitude, Double longitude) { return latitude != null && longitude != null && latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180; }
  private void addSuggestion(Map<String, CaregiverLocationSuggestionResponse> suggestions, String query, CaregiverLocationSuggestionResponse suggestion) { if (query.isBlank() || normalize(suggestion.label()).contains(query)) suggestions.putIfAbsent(suggestion.id(), suggestion); }
  private CaregiverLocationSuggestionResponse citySuggestion(String city, String state) { return new CaregiverLocationSuggestionResponse("city-" + slug(city) + "-" + slug(state), city + " - " + state, "CITY", city, null, state); }
  private CaregiverLocationSuggestionResponse neighborhoodSuggestion(String neighborhood, String city, String state) { return new CaregiverLocationSuggestionResponse("neighborhood-" + slug(neighborhood) + "-" + slug(city) + "-" + slug(state), neighborhood + ", " + city + " - " + state, "NEIGHBORHOOD", city, neighborhood, state); }
  private String slug(String value) { return normalize(value).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", ""); }
  private String normalize(String value) { return value == null ? "" : java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim(); }
}
