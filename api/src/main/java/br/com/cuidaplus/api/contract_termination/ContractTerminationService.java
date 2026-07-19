package br.com.cuidaplus.api.contract_termination;

import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.contract_termination.dto.*;
import br.com.cuidaplus.api.notification.*;
import br.com.cuidaplus.api.service_request.HiringType;
import br.com.cuidaplus.api.status_history.*;
import br.com.cuidaplus.api.user.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContractTerminationService {
  private final CareContractRepository contracts;
  private final UserService users;
  private final StatusHistoryService history;
  private final NotificationService notifications;
  private final ContractStatusProcessorService processor;

  public ContractTerminationService(CareContractRepository contracts, UserService users, StatusHistoryService history, NotificationService notifications, ContractStatusProcessorService processor) {
    this.contracts = contracts; this.users = users; this.history = history; this.notifications = notifications; this.processor = processor;
  }

  @Transactional
  public ContractTerminationFormResponse form(UUID userId, UUID contractId) {
    User actor = users.findById(userId);
    CareContract contract = authorize(contracts.findById(contractId).orElseThrow(this::notFound), actor);
    processor.processContractIfDue(contract);
    ContractParticipantRole role = role(contract, actor);
    ContractTerminationActionType action = contract.getStatus() == CareContractStatus.ATIVA ? ContractTerminationActionType.TERMINATION : contract.getStatus() == CareContractStatus.AGENDADA ? ContractTerminationActionType.CANCELLATION : ContractTerminationActionType.NONE;
    String otherParty = role == ContractParticipantRole.RESPONSAVEL ? contract.getCaregiverUser().getFullName() : contract.getResponsibleUser().getFullName();
    var request = contract.getServiceRequest();
    return new ContractTerminationFormResponse(contract.getId(), contract.getStatus(), action, request.getHiringType(), contract.getStartDate(), contract.getEndDate(), role, contract.getAssistedPerson().getNome(), otherParty,
      request.getScheduleDays().stream().sorted(Comparator.comparing(day -> day.getWeekday().ordinal())).map(day -> new ContractTerminationFormResponse.Schedule(day.getWeekday(), day.getStartTime(), day.getEndTime())).toList(),
      allowedTypes(contract, role));
  }

  @Transactional
  public ContractTerminationResponse terminate(UUID userId, UUID contractId, TerminateContractRequest request) {
    User actor = users.findById(userId);
    CareContract contract = authorize(contracts.findForUpdateById(contractId).orElseThrow(this::notFound), actor);
    processor.processContractIfDue(contract);
    requireActiveForTermination(contract);
    LocalDate today = LocalDate.now();
    if (request.effectiveEndDate().isBefore(today)) throw new BusinessException("A data efetiva não pode ser anterior à data atual.");
    if (request.effectiveEndDate().isBefore(contract.getStartDate())) throw new BusinessException("A data efetiva não pode ser anterior à data de início da contratação.");
    ContractParticipantRole role = role(contract, actor);
    if (!allowedTypes(contract, role).contains(request.terminationType())) throw new BusinessException("Tipo de encerramento inválido para esta contratação.");
    if (request.terminationType() == ContractTerminationType.NA_DATA_PREVISTA && !request.effectiveEndDate().equals(contract.getEndDate())) throw new BusinessException("A data efetiva deve corresponder à data final prevista.");

    String reason = request.reason().trim(); Instant now = Instant.now(); CareContractStatus previous = contract.getStatus();
    CareContractStatus next = request.effectiveEndDate().isAfter(today) ? CareContractStatus.ENCERRAMENTO_AGENDADO : CareContractStatus.ENCERRADA;
    contract.setStatus(next); contract.setTerminationType(request.terminationType()); contract.setTerminationReason(reason); contract.setTerminationNotes(trimToNull(request.notes()));
    contract.setTerminationRequestedByUser(actor); contract.setTerminationRequestedAt(now); contract.setEffectiveEndDate(request.effectiveEndDate());
    if (next == CareContractStatus.ENCERRADA) contract.setClosureReason(reason);
    contracts.saveAndFlush(contract);
    history.record(StatusHistoryEntityType.CARE_CONTRACT, contract.getId(), previous.name(), next.name(), actor, reason);
    User recipient = otherParty(contract, actor);
    if (next == CareContractStatus.ENCERRADA) {
      notifications.create(recipient, NotificationType.CONTRACT_TERMINATED, "Serviço encerrado", "A contratação foi encerrada.", RelatedEntityType.CARE_CONTRACT, contract.getId());
    } else {
      String date = request.effectiveEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
      notifications.create(recipient, NotificationType.CONTRACT_TERMINATION_SCHEDULED, "Encerramento agendado", "O encerramento do serviço foi agendado para " + date + ".", RelatedEntityType.CARE_CONTRACT, contract.getId());
    }
    return response(contract, actor, reason);
  }

  @Transactional
  public ContractTerminationResponse cancelBeforeStart(UUID userId, UUID contractId, CancelContractBeforeStartRequest request) {
    User actor = users.findById(userId);
    CareContract contract = authorize(contracts.findForUpdateById(contractId).orElseThrow(this::notFound), actor);
    processor.processContractIfDue(contract);
    requireScheduledForCancellation(contract);
    String reason = request.reason().trim(); Instant now = Instant.now();
    contract.setStatus(CareContractStatus.CANCELADA); contract.setTerminationType(ContractTerminationType.CANCELAMENTO_ANTES_INICIO);
    contract.setTerminationNotes(trimToNull(request.notes())); contract.setCancellationReason(reason); contract.setCanceledAt(now);
    contract.setCancellationRequestedByUser(actor); contract.setCancellationRequestedAt(now); contract.setEffectiveEndDate(LocalDate.now());
    contracts.saveAndFlush(contract);
    history.record(StatusHistoryEntityType.CARE_CONTRACT, contract.getId(), CareContractStatus.AGENDADA.name(), CareContractStatus.CANCELADA.name(), actor, reason);
    notifications.create(otherParty(contract, actor), NotificationType.CONTRACT_CANCELED_BEFORE_START, "Contratação cancelada", "A contratação foi cancelada antes do início.", RelatedEntityType.CARE_CONTRACT, contract.getId());
    return response(contract, actor, reason);
  }

  private List<ContractTerminationType> allowedTypes(CareContract contract, ContractParticipantRole role) {
    if (contract.getStatus() == CareContractStatus.AGENDADA) return List.of(ContractTerminationType.CANCELAMENTO_ANTES_INICIO);
    if (contract.getStatus() != CareContractStatus.ATIVA) return List.of();
    List<ContractTerminationType> result = new ArrayList<>();
    if (contract.getServiceRequest().getHiringType() == HiringType.PERIODO_DETERMINADO && contract.getEndDate() != null) result.add(ContractTerminationType.NA_DATA_PREVISTA);
    result.add(role == ContractParticipantRole.RESPONSAVEL ? ContractTerminationType.ANTECIPADO_RESPONSAVEL : ContractTerminationType.ANTECIPADO_CUIDADOR);
    result.add(ContractTerminationType.ACORDO_ENTRE_PARTES);
    return List.copyOf(result);
  }

  private void requireActiveForTermination(CareContract contract) {
    switch (contract.getStatus()) {
      case AGENDADA -> throw new BusinessException("Serviços ainda não iniciados devem ser cancelados, não encerrados.");
      case ENCERRAMENTO_AGENDADO -> throw new BusinessException("Já existe um encerramento agendado para esta contratação.", HttpStatus.CONFLICT);
      case ENCERRADA, FINALIZADA -> throw new BusinessException("Esta contratação já foi encerrada.", HttpStatus.CONFLICT);
      case CANCELADA -> throw new BusinessException("Esta contratação já foi cancelada.", HttpStatus.CONFLICT);
      case ATIVA -> { }
    }
  }

  private void requireScheduledForCancellation(CareContract contract) {
    switch (contract.getStatus()) {
      case ATIVA -> throw new BusinessException("Serviços ativos devem ser encerrados, não cancelados.");
      case ENCERRAMENTO_AGENDADO -> throw new BusinessException("Já existe um encerramento agendado para esta contratação.", HttpStatus.CONFLICT);
      case ENCERRADA, FINALIZADA -> throw new BusinessException("Esta contratação já foi encerrada.", HttpStatus.CONFLICT);
      case CANCELADA -> throw new BusinessException("Esta contratação já foi cancelada.", HttpStatus.CONFLICT);
      case AGENDADA -> { if (!contract.getStartDate().isAfter(LocalDate.now())) throw new BusinessException("Serviços ativos devem ser encerrados, não cancelados."); }
    }
  }

  private CareContract authorize(CareContract contract, User actor) {
    if (!contract.getResponsibleUser().getId().equals(actor.getId()) && !contract.getCaregiverUser().getId().equals(actor.getId())) throw new BusinessException("Você não tem permissão para acessar esta contratação.", HttpStatus.FORBIDDEN);
    return contract;
  }
  private ContractParticipantRole role(CareContract contract, User actor) { return contract.getResponsibleUser().getId().equals(actor.getId()) ? ContractParticipantRole.RESPONSAVEL : ContractParticipantRole.CUIDADOR; }
  private User otherParty(CareContract contract, User actor) { return contract.getResponsibleUser().getId().equals(actor.getId()) ? contract.getCaregiverUser() : contract.getResponsibleUser(); }
  private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
  private BusinessException notFound() { return new BusinessException("Contratação não encontrada.", HttpStatus.NOT_FOUND); }
  private ContractTerminationResponse response(CareContract contract, User actor, String reason) { return new ContractTerminationResponse(contract.getId(), contract.getStatus(), contract.getEffectiveEndDate(), contract.getTerminationRequestedAt(), contract.getCanceledAt(), actor.getFullName(), contract.getTerminationType(), reason, contract.getTerminationNotes(), contract.getUpdatedAt()); }
}
