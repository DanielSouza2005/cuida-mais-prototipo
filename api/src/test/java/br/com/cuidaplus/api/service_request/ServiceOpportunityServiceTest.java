package br.com.cuidaplus.api.service_request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.care_contract.CareContractRepository;
import br.com.cuidaplus.api.care_contract.CareContract;
import br.com.cuidaplus.api.notification.NotificationService;
import br.com.cuidaplus.api.status_history.StatusHistoryService;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserService;
import br.com.cuidaplus.api.user.UserType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ServiceOpportunityServiceTest {
  @Mock ServiceRequestRepository requests;
  @Mock UserService users;
  @Mock NotificationService notifications;
  @Mock StatusHistoryService history;
  @Mock ServiceRequestDecisionService decisions;
  @Mock CareContractRepository contracts;

  private ServiceOpportunityService service;

  @BeforeEach
  void setUp() {
    service = new ServiceOpportunityService(requests, users, notifications, history, decisions, contracts);
  }

  @Test
  void caregiverCannotApplyToExpiredOpportunity() {
    UUID caregiverId = UUID.randomUUID(), opportunityId = UUID.randomUUID();
    User caregiver = caregiver();
    ServiceRequest opportunity = opportunity(Instant.now().minusSeconds(1));
    when(users.findById(caregiverId)).thenReturn(caregiver);
    when(requests.findOpenOpportunityForUpdate(opportunityId)).thenReturn(Optional.of(opportunity));

    BusinessException error = assertThrows(BusinessException.class, () -> service.apply(caregiverId, opportunityId));

    assertEquals(HttpStatus.CONFLICT, error.getStatus());
    verify(requests, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void caregiverCannotApplyTwiceToTheSameOpportunity() {
    UUID caregiverId = UUID.randomUUID(), opportunityId = UUID.randomUUID();
    User caregiver = caregiver();
    ServiceRequest opportunity = opportunity(Instant.now().plusSeconds(3600));
    User responsible = org.mockito.Mockito.mock(User.class);
    when(responsible.getId()).thenReturn(UUID.randomUUID());
    when(opportunity.getResponsibleUser()).thenReturn(responsible);
    when(users.findById(caregiverId)).thenReturn(caregiver);
    when(requests.findOpenOpportunityForUpdate(opportunityId)).thenReturn(Optional.of(opportunity));
    when(requests.existsBySourceOpportunityAndCaregiverUser(opportunity, caregiver)).thenReturn(true);

    BusinessException error = assertThrows(BusinessException.class, () -> service.apply(caregiverId, opportunityId));

    assertEquals(HttpStatus.CONFLICT, error.getStatus());
    verify(requests, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void acceptingApplicationClosesPublishedOpportunity() {
    UUID responsibleId = UUID.randomUUID(), applicationId = UUID.randomUUID(), opportunityId = UUID.randomUUID();
    User responsible = org.mockito.Mockito.mock(User.class);
    ServiceRequest application = org.mockito.Mockito.mock(ServiceRequest.class);
    ServiceRequest opportunity = org.mockito.Mockito.mock(ServiceRequest.class);
    when(responsible.getUserType()).thenReturn(UserType.RESPONSAVEL);
    when(users.findById(responsibleId)).thenReturn(responsible);
    when(requests.findForUpdateByIdAndResponsibleUser(applicationId, responsible)).thenReturn(Optional.of(application));
    when(application.getInitiatedBy()).thenReturn(ServiceRequestInitiator.CAREGIVER);
    when(application.getSourceOpportunity()).thenReturn(opportunity);
    when(opportunity.getExpiresAt()).thenReturn(Instant.now().plusSeconds(3600));
    when(opportunity.getId()).thenReturn(opportunityId);
    when(requests.findOpenOpportunityForUpdate(opportunityId)).thenReturn(Optional.of(opportunity));

    service.accept(responsibleId, applicationId);

    verify(decisions).accept(application, responsible);
    verify(opportunity).setStatus(ServiceRequestStatus.ACEITA);
    verify(history).record(br.com.cuidaplus.api.status_history.StatusHistoryEntityType.SERVICE_REQUEST, opportunityId, ServiceRequestStatus.ABERTA.name(), ServiceRequestStatus.ACEITA.name(), responsible, null);
  }

  @Test
  void responsibleCannotUseApplicationDecisionEndpointForDirectRequest() {
    UUID responsibleId = UUID.randomUUID(), requestId = UUID.randomUUID();
    User responsible = org.mockito.Mockito.mock(User.class);
    ServiceRequest directRequest = org.mockito.Mockito.mock(ServiceRequest.class);
    when(responsible.getUserType()).thenReturn(UserType.RESPONSAVEL);
    when(users.findById(responsibleId)).thenReturn(responsible);
    when(requests.findForUpdateByIdAndResponsibleUser(requestId, responsible)).thenReturn(Optional.of(directRequest));
    when(directRequest.getInitiatedBy()).thenReturn(ServiceRequestInitiator.RESPONSIBLE);

    BusinessException error = assertThrows(BusinessException.class, () -> service.accept(responsibleId, requestId));

    assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    verify(decisions, never()).accept(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  void caregiverCanResolveContractCreatedFromAcceptedInterest() {
    UUID caregiverId = UUID.randomUUID(), opportunityId = UUID.randomUUID(), contractId = UUID.randomUUID();
    User caregiver = caregiver();
    CareContract contract = org.mockito.Mockito.mock(CareContract.class);
    when(users.findById(caregiverId)).thenReturn(caregiver);
    when(contracts.findFirstByCaregiverUserAndServiceRequestSourceOpportunityId(caregiver, opportunityId)).thenReturn(Optional.of(contract));
    when(contract.getId()).thenReturn(contractId);

    assertEquals(contractId, service.acceptedContract(caregiverId, opportunityId).contractId());
  }

  private User caregiver() {
    User caregiver = org.mockito.Mockito.mock(User.class);
    when(caregiver.getUserType()).thenReturn(UserType.CUIDADOR);
    return caregiver;
  }

  private ServiceRequest opportunity(Instant expiresAt) {
    ServiceRequest opportunity = org.mockito.Mockito.mock(ServiceRequest.class);
    when(opportunity.getExpiresAt()).thenReturn(expiresAt);
    return opportunity;
  }
}
