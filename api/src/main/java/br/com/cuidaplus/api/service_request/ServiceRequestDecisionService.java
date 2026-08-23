package br.com.cuidaplus.api.service_request;

import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.care_task.ContractCareTaskProvisioningService;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.notification.*;
import br.com.cuidaplus.api.status_history.*;
import br.com.cuidaplus.api.user.User;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceRequestDecisionService {
  private final CareContractRepository contracts;
  private final NotificationService notifications;
  private final StatusHistoryService history;
  private final ContractCareTaskProvisioningService taskProvisioning;

  public ServiceRequestDecisionService(CareContractRepository contracts, NotificationService notifications, StatusHistoryService history, ContractCareTaskProvisioningService taskProvisioning) {
    this.contracts = contracts;
    this.notifications = notifications;
    this.history = history;
    this.taskProvisioning = taskProvisioning;
  }

  @Transactional
  public void accept(ServiceRequest request, User actor) {
    requirePending(request);
    if (request.getCaregiverUser() == null || contracts.existsByServiceRequestId(request.getId())) {
      throw new BusinessException("Esta solicitação já foi respondida.", HttpStatus.CONFLICT);
    }
    request.setStatus(ServiceRequestStatus.ACEITA);
    history.record(StatusHistoryEntityType.SERVICE_REQUEST, request.getId(), ServiceRequestStatus.PENDENTE.name(), ServiceRequestStatus.ACEITA.name(), actor, null);
    CareContract contract = new CareContract();
    contract.setServiceRequest(request);
    contract.setResponsibleUser(request.getResponsibleUser());
    contract.setCaregiverUser(request.getCaregiverUser());
    contract.setAssistedPerson(request.getAssistedPerson());
    contract.setStartDate(request.getStartDate());
    contract.setEndDate(request.getEndDate());
    contract.setStatus(request.getStartDate().isAfter(LocalDate.now()) ? CareContractStatus.AGENDADA : CareContractStatus.ATIVA);
    CareContract saved = contracts.save(contract);
    taskProvisioning.provision(saved);
    history.record(StatusHistoryEntityType.CARE_CONTRACT, saved.getId(), null, saved.getStatus().name(), actor, null);
    if (request.getInitiatedBy() == ServiceRequestInitiator.CAREGIVER) {
      notifications.create(request.getCaregiverUser(), NotificationType.SERVICE_OPPORTUNITY_APPLICATION_ACCEPTED, "Interesse aceito", "O responsável aceitou seu interesse no serviço.", RelatedEntityType.CARE_CONTRACT, saved.getId());
    } else {
      notifications.create(request.getResponsibleUser(), NotificationType.SERVICE_REQUEST_ACCEPTED, "Solicitação aceita", "O cuidador aceitou sua solicitação de serviço.", request.getId());
    }
  }

  @Transactional
  public void reject(ServiceRequest request, User actor, String reason) {
    requirePending(request);
    request.setStatus(ServiceRequestStatus.REJEITADA);
    request.setRejectionReason(reason == null || reason.isBlank() ? null : reason.trim());
    history.record(StatusHistoryEntityType.SERVICE_REQUEST, request.getId(), ServiceRequestStatus.PENDENTE.name(), ServiceRequestStatus.REJEITADA.name(), actor, request.getRejectionReason());
    if (request.getInitiatedBy() == ServiceRequestInitiator.CAREGIVER) {
      notifications.create(request.getCaregiverUser(), NotificationType.SERVICE_OPPORTUNITY_APPLICATION_REJECTED, "Interesse rejeitado", "O responsável rejeitou seu interesse no serviço.", request.getSourceOpportunity().getId());
    } else {
      notifications.create(request.getResponsibleUser(), NotificationType.SERVICE_REQUEST_REJECTED, "Solicitação rejeitada", "O cuidador rejeitou sua solicitação de serviço.", request.getId());
    }
  }

  private void requirePending(ServiceRequest request) {
    if (request.getStatus() != ServiceRequestStatus.PENDENTE) {
      throw new BusinessException("Esta solicitação já foi respondida.", HttpStatus.CONFLICT);
    }
  }
}
