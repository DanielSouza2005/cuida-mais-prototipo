package br.com.cuidaplus.api.service_request;

import br.com.cuidaplus.api.care_contract.*;
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
public class CaregiverServiceRequestService {
  private final ServiceRequestRepository requests;
  private final UserService users;
  private final CareContractRepository contracts;
  private final NotificationService notifications;
  private final ResponsibleProfileRepository responsibleProfiles;
  private final StatusHistoryService history;

  public CaregiverServiceRequestService(ServiceRequestRepository requests, UserService users, CareContractRepository contracts, NotificationService notifications, ResponsibleProfileRepository responsibleProfiles, StatusHistoryService history) {
    this.requests = requests; this.users = users; this.contracts = contracts; this.notifications = notifications;
    this.responsibleProfiles = responsibleProfiles; this.history = history;
  }

  @Transactional
  public CaregiverServiceRequestPageResponse list(UUID userId, ServiceRequestStatus status, int page, int size) {
    User caregiver = requireCaregiver(userId);
    requests.findByCaregiverUserAndStatusOrderByCreatedAtDesc(caregiver, ServiceRequestStatus.PENDENTE).forEach(this::expireIfNeeded);
    PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<ServiceRequest> result = status == null ? requests.findByCaregiverUser(caregiver, pageable) : requests.findByCaregiverUserAndStatus(caregiver, status, pageable);
    List<CaregiverReceivedRequestResponse> content = result.getContent().stream().map(entity -> response(entity, hasConflict(entity))).toList();
    return new CaregiverServiceRequestPageResponse(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages(), result.isLast());
  }

  @Transactional public CaregiverReceivedRequestResponse details(UUID userId, UUID id) { ServiceRequest entity = owned(userId, id); expireIfNeeded(entity); return response(entity, hasConflict(entity)); }

  @Transactional
  public CaregiverReceivedRequestResponse accept(UUID userId, UUID id) {
    ServiceRequest entity = ownedForUpdate(userId, id); requirePending(entity);
    entity.setStatus(ServiceRequestStatus.ACEITA);
    if (contracts.existsByServiceRequestId(entity.getId())) throw new BusinessException("Esta solicitação já foi respondida.", HttpStatus.CONFLICT);
    history.record(StatusHistoryEntityType.SERVICE_REQUEST, entity.getId(), ServiceRequestStatus.PENDENTE.name(), ServiceRequestStatus.ACEITA.name(), entity.getCaregiverUser(), null);
    CareContract contract = new CareContract(); contract.setServiceRequest(entity); contract.setResponsibleUser(entity.getResponsibleUser()); contract.setCaregiverUser(entity.getCaregiverUser()); contract.setAssistedPerson(entity.getAssistedPerson()); contract.setStartDate(entity.getStartDate()); contract.setEndDate(entity.getEndDate());
    contract.setStatus(entity.getStartDate().isAfter(LocalDate.now()) ? CareContractStatus.AGENDADA : CareContractStatus.ATIVA);
    CareContract saved = contracts.save(contract);
    history.record(StatusHistoryEntityType.CARE_CONTRACT, saved.getId(), null, saved.getStatus().name(), entity.getCaregiverUser(), null);
    notifications.create(entity.getResponsibleUser(), NotificationType.SERVICE_REQUEST_ACCEPTED, "Solicitação aceita", "O cuidador aceitou sua solicitação de serviço.", entity.getId());
    return response(entity, hasConflict(entity));
  }

  @Transactional
  public CaregiverReceivedRequestResponse reject(UUID userId, UUID id, String reason) {
    ServiceRequest entity = ownedForUpdate(userId, id); requirePending(entity);
    entity.setStatus(ServiceRequestStatus.REJEITADA); entity.setRejectionReason(reason == null || reason.isBlank() ? null : reason.trim());
    history.record(StatusHistoryEntityType.SERVICE_REQUEST, entity.getId(), ServiceRequestStatus.PENDENTE.name(), ServiceRequestStatus.REJEITADA.name(), entity.getCaregiverUser(), entity.getRejectionReason());
    notifications.create(entity.getResponsibleUser(), NotificationType.SERVICE_REQUEST_REJECTED, "Solicitação rejeitada", "O cuidador rejeitou sua solicitação de serviço.", entity.getId());
    return response(entity, hasConflict(entity));
  }

  private ServiceRequest owned(UUID userId, UUID id) { User caregiver = requireCaregiver(userId); return requests.findByIdAndCaregiverUser(id, caregiver).orElseThrow(() -> new BusinessException("Solicitação não encontrada.", HttpStatus.NOT_FOUND)); }
  private ServiceRequest ownedForUpdate(UUID userId, UUID id) { User caregiver = requireCaregiver(userId); return requests.findForUpdateByIdAndCaregiverUser(id, caregiver).orElseThrow(() -> new BusinessException("Solicitação não encontrada.", HttpStatus.NOT_FOUND)); }
  private User requireCaregiver(UUID id) { User user = users.findById(id); if (user.getUserType() != UserType.CUIDADOR && user.getUserType() != UserType.CAREGIVER) throw new BusinessException("Acesso permitido apenas para cuidadores.", HttpStatus.FORBIDDEN); return user; }
  private void requirePending(ServiceRequest entity) { expireIfNeeded(entity); if (entity.getStatus() != ServiceRequestStatus.PENDENTE) throw new BusinessException("Esta solicitação já foi respondida.", HttpStatus.CONFLICT); }
  private void expireIfNeeded(ServiceRequest entity) { if (entity.getStatus() == ServiceRequestStatus.PENDENTE && entity.getExpiresAt().isBefore(Instant.now())) { entity.setStatus(ServiceRequestStatus.EXPIRADA); history.record(StatusHistoryEntityType.SERVICE_REQUEST, entity.getId(), ServiceRequestStatus.PENDENTE.name(), ServiceRequestStatus.EXPIRADA.name(), entity.getResponsibleUser(), null); } }
  private boolean hasConflict(ServiceRequest target) { return requests.findByCaregiverUserAndStatusOrderByCreatedAtDesc(target.getCaregiverUser(), ServiceRequestStatus.ACEITA).stream().filter(other -> !other.getId().equals(target.getId())).anyMatch(other -> dateOverlap(target, other) && scheduleOverlap(target, other)); }
  private boolean dateOverlap(ServiceRequest first, ServiceRequest second) { LocalDate firstEnd = first.getEndDate() == null ? LocalDate.MAX : first.getEndDate(), secondEnd = second.getEndDate() == null ? LocalDate.MAX : second.getEndDate(); return !first.getStartDate().isAfter(secondEnd) && !second.getStartDate().isAfter(firstEnd); }
  private boolean scheduleOverlap(ServiceRequest first, ServiceRequest second) { return first.getScheduleDays().stream().anyMatch(left -> second.getScheduleDays().stream().anyMatch(right -> left.getWeekday() == right.getWeekday() && left.getStartTime().isBefore(right.getEndTime()) && left.getEndTime().isAfter(right.getStartTime()))); }

  private CaregiverReceivedRequestResponse response(ServiceRequest entity, boolean conflict) {
    AssistedPerson person = entity.getAssistedPerson(); AddressFields address = person.getEnderecoCuidado(); ResponsibleProfile profile = responsibleProfiles.findByUser(entity.getResponsibleUser()).orElse(null);
    return new CaregiverReceivedRequestResponse(entity.getId(), entity.getStatus(), entity.getCreatedAt(), entity.getExpiresAt(), conflict,
      new CaregiverReceivedRequestResponse.Responsible(entity.getResponsibleUser().getId(), entity.getResponsibleUser().getFullName(), profile == null ? null : String.valueOf(profile.getParentescoOutro() != null ? profile.getParentescoOutro() : profile.getParentesco()), profile == null ? null : String.valueOf(profile.getPreferenciaContato())),
      new CaregiverReceivedRequestResponse.Assisted(person.getId(), person.getNome(), Period.between(person.getDataNascimento(), LocalDate.now()).getYears(), person.getGrauDependencia(), person.getMobilidade(), new LinkedHashSet<>(person.getAlergias()), new LinkedHashSet<>(person.getRestricoesAlimentares()), person.getMedicamentos(), person.getObservacoes()),
      new CaregiverReceivedRequestResponse.CareAddress(address.getCep(), address.getRua(), address.getNumero(), address.getComplemento(), address.getBairro(), address.getCidade(), address.getEstado(), address.getPontoReferencia()), null,
      entity.getHiringType(), entity.getStartDate(), entity.getEndDate(), new LinkedHashSet<>(entity.getSpecificDates()), entity.getScheduleDays().stream().map(day -> new CaregiverReceivedRequestResponse.Schedule(day.getWeekday(), day.getStartTime(), day.getEndTime())).toList(), entity.getNeedsDescription(), new LinkedHashSet<>(entity.getActivities()), entity.getAdditionalNotes(), entity.getNegotiationNotes(), entity.getRejectionReason());
  }
}
