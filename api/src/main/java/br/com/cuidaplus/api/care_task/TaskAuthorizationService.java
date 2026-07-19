package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.contract_termination.ContractStatusProcessorService;
import br.com.cuidaplus.api.profile.*;
import br.com.cuidaplus.api.user.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TaskAuthorizationService {
  private final UserService users;
  private final CareContractRepository contracts;
  private final AssistedPersonRepository assistedPeople;
  private final ContractStatusProcessorService statusProcessor;

  public TaskAuthorizationService(UserService users, CareContractRepository contracts, AssistedPersonRepository assistedPeople, ContractStatusProcessorService statusProcessor) {
    this.users = users; this.contracts = contracts; this.assistedPeople = assistedPeople; this.statusProcessor = statusProcessor;
  }

  public User requireResponsible(UUID userId) {
    User user = users.findById(userId);
    if (user.getUserType() != UserType.RESPONSAVEL && user.getUserType() != UserType.FAMILY) {
      throw new BusinessException("Acesso permitido apenas para responsáveis.", HttpStatus.FORBIDDEN);
    }
    return user;
  }

  public User requireCaregiver(UUID userId) {
    User user = users.findById(userId);
    if (user.getUserType() != UserType.CUIDADOR && user.getUserType() != UserType.CAREGIVER) {
      throw new BusinessException("Acesso permitido apenas para cuidadores.", HttpStatus.FORBIDDEN);
    }
    return user;
  }

  public TaskContext requireEligibleContext(User responsible, UUID contractId, UUID assistedPersonId, UUID caregiverId) {
    AssistedPerson assisted = assistedPeople.findByIdAndResponsibleUser(assistedPersonId, responsible)
      .orElseThrow(() -> new BusinessException("Pessoa assistida inválida para este responsável.", HttpStatus.FORBIDDEN));
    CareContract contract = contracts.findByIdAndResponsibleUser(contractId, responsible)
      .map(statusProcessor::processContractIfDue)
      .orElseThrow(() -> new BusinessException("Contratação não encontrada.", HttpStatus.NOT_FOUND));
    if (!contract.getAssistedPerson().getId().equals(assisted.getId())) {
      throw new BusinessException("A pessoa assistida não corresponde à contratação.");
    }
    if (!contract.getCaregiverUser().getId().equals(caregiverId)) {
      throw new BusinessException("O cuidador informado não está vinculado à contratação.", HttpStatus.FORBIDDEN);
    }
    requireContractActive(contract);
    return new TaskContext(responsible, assisted, contract, contract.getCaregiverUser());
  }

  public void requireContractActive(CareContract contract) {
    if (contract.getStatus() != CareContractStatus.ATIVA && contract.getStatus() != CareContractStatus.ENCERRAMENTO_AGENDADO) {
      throw new BusinessException("A contratação precisa estar ativa para gerenciar tarefas.", HttpStatus.CONFLICT);
    }
  }

  public record TaskContext(User responsible, AssistedPerson assistedPerson, CareContract contract, User caregiver) {}
}
