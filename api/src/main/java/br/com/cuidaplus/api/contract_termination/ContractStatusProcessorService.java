package br.com.cuidaplus.api.contract_termination;

import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.notification.*;
import br.com.cuidaplus.api.status_history.*;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContractStatusProcessorService {
  private static final String AUTOMATIC_REASON = "Encerramento automático ao término do período contratado.";
  private final CareContractRepository contracts;
  private final StatusHistoryService history;
  private final NotificationService notifications;

  public ContractStatusProcessorService(CareContractRepository contracts, StatusHistoryService history, NotificationService notifications) {
    this.contracts = contracts; this.history = history; this.notifications = notifications;
  }

  @Transactional
  public void processDueContractStatus() { contracts.findAll().forEach(this::processContractIfDue); }

  public CareContract processContractIfDue(CareContract contract) {
    LocalDate today = LocalDate.now();
    if (contract.getStatus() == CareContractStatus.AGENDADA && !contract.getStartDate().isAfter(today)) {
      transition(contract, CareContractStatus.ATIVA, null, contract.getResponsibleUser());
    }
    if (contract.getStatus() == CareContractStatus.ENCERRAMENTO_AGENDADO && contract.getEffectiveEndDate() != null && !contract.getEffectiveEndDate().isAfter(today)) {
      CareContractStatus previous = contract.getStatus();
      contract.setStatus(CareContractStatus.ENCERRADA);
      contract.setClosureReason(contract.getTerminationReason());
      history.record(StatusHistoryEntityType.CARE_CONTRACT, contract.getId(), previous.name(), CareContractStatus.ENCERRADA.name(), null, contract.getTerminationReason());
      notifyBoth(contract, NotificationType.CONTRACT_TERMINATED, "Serviço encerrado", "A contratação foi encerrada.");
      contracts.saveAndFlush(contract);
    } else if (contract.getStatus() == CareContractStatus.ATIVA && contract.getEndDate() != null && !contract.getEndDate().isAfter(today)) {
      CareContractStatus previous = contract.getStatus();
      contract.setStatus(CareContractStatus.ENCERRADA);
      contract.setTerminationType(ContractTerminationType.AUTOMATICO_TERMINO_PERIODO);
      contract.setTerminationReason(AUTOMATIC_REASON);
      contract.setClosureReason(AUTOMATIC_REASON);
      contract.setEffectiveEndDate(contract.getEndDate());
      history.record(StatusHistoryEntityType.CARE_CONTRACT, contract.getId(), previous.name(), CareContractStatus.ENCERRADA.name(), null, AUTOMATIC_REASON);
      notifyBoth(contract, NotificationType.CONTRACT_AUTOMATICALLY_TERMINATED, "Serviço encerrado", "A contratação foi encerrada ao término do período contratado.");
      contracts.saveAndFlush(contract);
    }
    return contract;
  }

  private void transition(CareContract contract, CareContractStatus status, String reason, br.com.cuidaplus.api.user.User actor) {
    CareContractStatus previous = contract.getStatus(); contract.setStatus(status);
    history.record(StatusHistoryEntityType.CARE_CONTRACT, contract.getId(), previous.name(), status.name(), actor, reason);
    contracts.saveAndFlush(contract);
  }

  private void notifyBoth(CareContract contract, NotificationType type, String title, String message) {
    notifications.create(contract.getResponsibleUser(), type, title, message, RelatedEntityType.CARE_CONTRACT, contract.getId());
    notifications.create(contract.getCaregiverUser(), type, title, message, RelatedEntityType.CARE_CONTRACT, contract.getId());
  }
}
