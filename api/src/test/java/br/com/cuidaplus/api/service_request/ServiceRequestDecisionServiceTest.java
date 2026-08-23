package br.com.cuidaplus.api.service_request;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.cuidaplus.api.care_contract.CareContract;
import br.com.cuidaplus.api.care_contract.CareContractRepository;
import br.com.cuidaplus.api.care_contract.CareContractStatus;
import br.com.cuidaplus.api.care_task.ContractCareTaskProvisioningService;
import br.com.cuidaplus.api.notification.NotificationService;
import br.com.cuidaplus.api.notification.NotificationType;
import br.com.cuidaplus.api.notification.RelatedEntityType;
import br.com.cuidaplus.api.profile.AssistedPerson;
import br.com.cuidaplus.api.status_history.StatusHistoryService;
import br.com.cuidaplus.api.user.User;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceRequestDecisionServiceTest {
  @Mock CareContractRepository contracts;
  @Mock NotificationService notifications;
  @Mock StatusHistoryService history;
  @Mock ContractCareTaskProvisioningService taskProvisioning;

  private ServiceRequestDecisionService service;

  @BeforeEach
  void setUp() {
    service = new ServiceRequestDecisionService(contracts, notifications, history, taskProvisioning);
  }

  @Test
  void acceptedCaregiverApplicationNotificationPointsToCreatedContract() {
    UUID requestId = UUID.randomUUID(), contractId = UUID.randomUUID();
    ServiceRequest request = org.mockito.Mockito.mock(ServiceRequest.class);
    CareContract savedContract = org.mockito.Mockito.mock(CareContract.class);
    User caregiver = org.mockito.Mockito.mock(User.class);
    User responsible = org.mockito.Mockito.mock(User.class);
    AssistedPerson assistedPerson = org.mockito.Mockito.mock(AssistedPerson.class);

    when(request.getId()).thenReturn(requestId);
    when(request.getStatus()).thenReturn(ServiceRequestStatus.PENDENTE);
    when(request.getInitiatedBy()).thenReturn(ServiceRequestInitiator.CAREGIVER);
    when(request.getCaregiverUser()).thenReturn(caregiver);
    when(request.getResponsibleUser()).thenReturn(responsible);
    when(request.getAssistedPerson()).thenReturn(assistedPerson);
    when(request.getStartDate()).thenReturn(LocalDate.now());
    when(contracts.save(any(CareContract.class))).thenReturn(savedContract);
    when(savedContract.getId()).thenReturn(contractId);
    when(savedContract.getStatus()).thenReturn(CareContractStatus.ATIVA);

    service.accept(request, responsible);

    verify(notifications).create(
      caregiver,
      NotificationType.SERVICE_OPPORTUNITY_APPLICATION_ACCEPTED,
      "Interesse aceito",
      "O responsável aceitou seu interesse no serviço.",
      RelatedEntityType.CARE_CONTRACT,
      contractId
    );
    verify(taskProvisioning).provision(savedContract);
  }
}
