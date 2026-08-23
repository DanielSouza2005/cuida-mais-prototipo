package br.com.cuidaplus.api.service_request;

import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.profile.AddressFields;
import br.com.cuidaplus.api.service_request.dto.ResponsibleServicePublicationPageResponse;
import br.com.cuidaplus.api.service_request.dto.ResponsibleServicePublicationResponse;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserService;
import br.com.cuidaplus.api.user.UserType;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResponsibleServicePublicationService {
  private final ServiceRequestRepository requests;
  private final UserService users;
  private final ServiceRequestService serviceRequests;

  public ResponsibleServicePublicationService(ServiceRequestRepository requests, UserService users, ServiceRequestService serviceRequests) {
    this.requests = requests;
    this.users = users;
    this.serviceRequests = serviceRequests;
  }

  @Transactional
  public ResponsibleServicePublicationPageResponse list(UUID userId, ServiceRequestStatus status, UUID assistedPersonId, HiringType hiringType, LocalDate startDate, LocalDate endDate, String city, String neighborhood, String needs, int page, int size) {
    User responsible = requireResponsible(userId);
    requests.findByResponsibleUserOrderByCreatedAtDesc(responsible).stream().filter(this::isPublication).forEach(serviceRequests::expireIfNeeded);
    PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<ServiceRequest> result = requests.searchResponsiblePublications(responsible, status, assistedPersonId, hiringType, startDate, endDate, filter(city), filter(neighborhood), filter(needs), pageable);
    List<ResponsibleServicePublicationResponse> content = result.getContent().stream().map(item -> response(item, false)).toList();
    return new ResponsibleServicePublicationPageResponse(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages(), result.isLast());
  }

  @Transactional
  public ResponsibleServicePublicationResponse details(UUID userId, UUID id) { ServiceRequest publication = ownedPublication(userId, id); serviceRequests.expireIfNeeded(publication); return response(publication, true); }

  @Transactional
  public ResponsibleServicePublicationResponse cancel(UUID userId, UUID id) {
    ownedPublication(userId, id);
    serviceRequests.cancel(userId, id);
    return response(ownedPublication(userId, id), true);
  }

  private ResponsibleServicePublicationResponse response(ServiceRequest publication, boolean includeApplications) {
    List<ServiceRequest> applications = requests.findBySourceOpportunityOrderByCreatedAtDesc(publication);
    long pending = applications.stream().filter(item -> item.getStatus() == ServiceRequestStatus.PENDENTE).count();
    long accepted = applications.stream().filter(item -> item.getStatus() == ServiceRequestStatus.ACEITA).count();
    AddressFields address = publication.getAssistedPerson().getEnderecoCuidado();
    List<ResponsibleServicePublicationResponse.Application> applicationResponses = includeApplications
      ? applications.stream().map(item -> new ResponsibleServicePublicationResponse.Application(item.getId(), item.getStatus(), item.getCaregiverUser().getId(), item.getCaregiverUser().getFullName(), item.getCaregiverUser().getProfilePhotoUrl(), item.getCreatedAt())).toList()
      : List.of();
    return new ResponsibleServicePublicationResponse(
      publication.getId(), publication.getStatus(), publication.getHiringType(), publication.getAssistedPerson().getId(), publication.getAssistedPerson().getNome(),
      publication.getStartDate(), publication.getEndDate(), new LinkedHashSet<>(publication.getSpecificDates()),
      publication.getScheduleDays().stream().map(day -> new ResponsibleServicePublicationResponse.Schedule(day.getWeekday(), day.getStartTime(), day.getEndTime())).toList(),
      address.getCidade(), address.getBairro(), address.getEstado(), publication.getNeedsDescription(), applications.size(), pending, accepted, applicationResponses,
      publication.getCreatedAt(), publication.getExpiresAt()
    );
  }

  private ServiceRequest ownedPublication(UUID userId, UUID id) {
    User responsible = requireResponsible(userId);
    return requests.findByIdAndResponsibleUser(id, responsible)
      .filter(this::isPublication)
      .orElseThrow(() -> new BusinessException("Serviço publicado não encontrado.", HttpStatus.NOT_FOUND));
  }

  private boolean isPublication(ServiceRequest item) { return item.getCaregiverUser() == null && item.getSourceOpportunity() == null && item.getInitiatedBy() == ServiceRequestInitiator.RESPONSIBLE; }

  private User requireResponsible(UUID id) {
    User user = users.findById(id);
    if (user.getUserType() != UserType.RESPONSAVEL && user.getUserType() != UserType.FAMILY) throw new BusinessException("Acesso permitido apenas para responsáveis.", HttpStatus.FORBIDDEN);
    return user;
  }

  private String filter(String value) { return value == null || value.isBlank() ? "" : value.trim().toLowerCase(Locale.ROOT); }
}
