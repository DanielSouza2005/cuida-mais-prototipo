package br.com.cuidaplus.api.contract_history;

import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.contract_history.dto.*;
import br.com.cuidaplus.api.profile.*;
import br.com.cuidaplus.api.service_request.*;
import br.com.cuidaplus.api.status_history.*;
import br.com.cuidaplus.api.user.*;
import java.text.Normalizer;
import java.time.*;
import java.util.*;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResponsibleContractHistoryService {
  private final ServiceRequestRepository requests;
  private final CareContractRepository contracts;
  private final CaregiverProfileRepository caregivers;
  private final UserService users;
  private final StatusHistoryService history;

  public ResponsibleContractHistoryService(ServiceRequestRepository requests, CareContractRepository contracts, CaregiverProfileRepository caregivers, UserService users, StatusHistoryService history) {
    this.requests = requests; this.contracts = contracts; this.caregivers = caregivers; this.users = users; this.history = history;
  }

  @Transactional
  public ContractHistoryPageResponse list(UUID userId, ContractHistoryStatusGroup statusGroup, String status, String participantName, LocalDate startDateFrom, LocalDate startDateTo, int page, int size) {
    User responsible = requireResponsible(userId);
    if (startDateFrom != null && startDateTo != null && startDateTo.isBefore(startDateFrom)) throw new BusinessException("A data final não pode ser anterior à data inicial.");
    List<ServiceRequest> serviceRequests = requests.findByResponsibleUserOrderByUpdatedAtDesc(responsible);
    serviceRequests.forEach(this::expireIfNeeded);
    List<CareContract> careContracts = contracts.findByResponsibleUserOrderByUpdatedAtDesc(responsible);
    careContracts.forEach(this::synchronizeContractStatus);

    Stream<ContractHistoryItemResponse> requestItems = serviceRequests.stream()
      .filter(entity -> entity.getStatus() != ServiceRequestStatus.ACEITA)
      .map(this::toListItem);
    Stream<ContractHistoryItemResponse> contractItems = careContracts.stream().map(this::toListItem);
    String participantFilter = normalize(participantName);
    ContractHistoryStatusGroup group = statusGroup == null ? ContractHistoryStatusGroup.TODAS : statusGroup;

    List<ContractHistoryItemResponse> filtered = Stream.concat(requestItems, contractItems)
      .filter(item -> group == ContractHistoryStatusGroup.TODAS || item.statusGroup() == group)
      .filter(item -> status == null || status.isBlank() || item.status().equalsIgnoreCase(status.trim()))
      .filter(item -> participantFilter.isEmpty() || normalize(item.participantName()).contains(participantFilter))
      .filter(item -> startDateFrom == null || item.startDate() != null && !item.startDate().isBefore(startDateFrom))
      .filter(item -> startDateTo == null || item.startDate() != null && !item.startDate().isAfter(startDateTo))
      .sorted(Comparator.comparing(ContractHistoryItemResponse::updatedAt).reversed())
      .toList();

    int safePage = Math.max(page, 0), safeSize = Math.min(Math.max(size, 1), 50);
    int from = Math.min(safePage * safeSize, filtered.size()), to = Math.min(from + safeSize, filtered.size());
    int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / safeSize);
    return new ContractHistoryPageResponse(filtered.subList(from, to), safePage, safeSize, filtered.size(), totalPages, safePage + 1 >= totalPages);
  }

  @Transactional
  public ContractHistoryDetailsResponse details(UUID userId, ContractHistoryItemType itemType, UUID id) {
    User responsible = requireResponsible(userId);
    if (itemType == ContractHistoryItemType.SERVICE_REQUEST) {
      ServiceRequest entity = requests.findByIdAndResponsibleUser(id, responsible).orElseThrow(this::notFound);
      expireIfNeeded(entity);
      return toDetails(entity, null);
    }
    CareContract contract = contracts.findByIdAndResponsibleUser(id, responsible).orElseThrow(this::notFound);
    synchronizeContractStatus(contract);
    return toDetails(contract.getServiceRequest(), contract);
  }

  private ContractHistoryItemResponse toListItem(ServiceRequest entity) {
    return new ContractHistoryItemResponse(entity.getId(), ContractHistoryItemType.SERVICE_REQUEST, entity.getId(), null,
      entity.getCaregiverUser().getFullName(), entity.getCaregiverUser().getProfilePhotoUrl(), entity.getAssistedPerson().getNome(),
      entity.getStatus().name(), requestGroup(entity.getStatus()), entity.getHiringType(), entity.getStartDate(), entity.getEndDate(),
      scheduleSummary(entity), entity.getUpdatedAt(), entity.getRejectionReason(), entity.getCancellationReason(), null);
  }

  private ContractHistoryItemResponse toListItem(CareContract contract) {
    ServiceRequest request = contract.getServiceRequest();
    return new ContractHistoryItemResponse(contract.getId(), ContractHistoryItemType.CARE_CONTRACT, request.getId(), contract.getId(),
      contract.getCaregiverUser().getFullName(), contract.getCaregiverUser().getProfilePhotoUrl(), contract.getAssistedPerson().getNome(),
      contract.getStatus().name(), contractGroup(contract.getStatus()), request.getHiringType(), contract.getStartDate(), contract.getEndDate(),
      scheduleSummary(request), contract.getUpdatedAt(), null, contract.getCancellationReason(), contract.getClosureReason());
  }

  private ContractHistoryDetailsResponse toDetails(ServiceRequest request, CareContract contract) {
    boolean isContract = contract != null;
    ContractHistoryItemType itemType = isContract ? ContractHistoryItemType.CARE_CONTRACT : ContractHistoryItemType.SERVICE_REQUEST;
    UUID id = isContract ? contract.getId() : request.getId();
    String status = isContract ? contract.getStatus().name() : request.getStatus().name();
    AssistedPerson person = request.getAssistedPerson(); AddressFields address = person.getEnderecoCuidado();
    CaregiverProfile caregiver = caregivers.findByUser(request.getCaregiverUser()).orElseThrow();
    AddressFields caregiverAddress = caregiver.getEnderecoAtendimento();
    List<ContractHistoryDetailsResponse.StatusHistoryEntry> entries = historyEntries(request, contract);
    return new ContractHistoryDetailsResponse(id, itemType, request.getId(), isContract ? contract.getId() : null, status, statusLabel(status),
      new ContractHistoryDetailsResponse.Caregiver(caregiver.getId(), request.getCaregiverUser().getFullName(), request.getCaregiverUser().getProfilePhotoUrl(), location(caregiverAddress)),
      new ContractHistoryDetailsResponse.Assisted(person.getId(), person.getNome(), dependencyLabel(person.getGrauDependencia()), mobilityLabel(person.getMobilidade()), allergyLabels(person), foodRestrictionLabels(person), person.getObservacoes()),
      new ContractHistoryDetailsResponse.CareAddress(address.getRua(), address.getNumero(), address.getComplemento(), address.getBairro(), address.getCidade(), address.getEstado(), address.getCep(), address.getPontoReferencia()),
      request.getHiringType(), isContract ? contract.getStartDate() : request.getStartDate(), isContract ? contract.getEndDate() : request.getEndDate(), new LinkedHashSet<>(request.getSpecificDates()),
      request.getScheduleDays().stream().sorted(Comparator.comparing(day -> day.getWeekday().ordinal())).map(day -> new ContractHistoryDetailsResponse.Schedule(day.getWeekday(), day.getStartTime(), day.getEndTime())).toList(),
      request.getActivities().stream().map(this::activityLabel).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)), request.getNeedsDescription(), request.getAdditionalNotes(), request.getNegotiationNotes(), request.getRejectionReason(), request.getCancellationReason(), isContract ? contract.getClosureReason() : null,
      isContract ? contract.getCreatedAt() : request.getCreatedAt(), isContract ? contract.getUpdatedAt() : request.getUpdatedAt(), entries);
  }

  private List<ContractHistoryDetailsResponse.StatusHistoryEntry> historyEntries(ServiceRequest request, CareContract contract) {
    List<StatusHistory> stored = new ArrayList<>(history.find(StatusHistoryEntityType.SERVICE_REQUEST, request.getId()));
    if (contract != null) stored.addAll(history.find(StatusHistoryEntityType.CARE_CONTRACT, contract.getId()));
    stored.sort(Comparator.comparing(StatusHistory::getCreatedAt));
    if (stored.isEmpty()) {
      String status = contract == null ? request.getStatus().name() : contract.getStatus().name();
      StatusHistoryEntityType type = contract == null ? StatusHistoryEntityType.SERVICE_REQUEST : StatusHistoryEntityType.CARE_CONTRACT;
      Instant date = contract == null ? request.getCreatedAt() : contract.getCreatedAt();
      return List.of(new ContractHistoryDetailsResponse.StatusHistoryEntry(null, type, null, status, historyLabel(type, null, status), null, "Sistema", date));
    }
    return stored.stream().map(entry -> new ContractHistoryDetailsResponse.StatusHistoryEntry(entry.getId(), entry.getEntityType(), entry.getPreviousStatus(), entry.getNewStatus(), historyLabel(entry.getEntityType(), entry.getPreviousStatus(), entry.getNewStatus()), entry.getReason(), entry.getChangedByUser().getFullName(), entry.getCreatedAt())).toList();
  }

  private void expireIfNeeded(ServiceRequest entity) {
    if (entity.getStatus() == ServiceRequestStatus.PENDENTE && entity.getExpiresAt().isBefore(Instant.now())) {
      entity.setStatus(ServiceRequestStatus.EXPIRADA);
      history.record(StatusHistoryEntityType.SERVICE_REQUEST, entity.getId(), ServiceRequestStatus.PENDENTE.name(), ServiceRequestStatus.EXPIRADA.name(), entity.getResponsibleUser(), null);
      requests.saveAndFlush(entity);
    }
  }

  private void synchronizeContractStatus(CareContract contract) {
    LocalDate today = LocalDate.now();
    boolean changed = false;
    if (contract.getStatus() == CareContractStatus.AGENDADA && !contract.getStartDate().isAfter(today)) {
      contract.setStatus(CareContractStatus.ATIVA);
      history.record(StatusHistoryEntityType.CARE_CONTRACT, contract.getId(), CareContractStatus.AGENDADA.name(), CareContractStatus.ATIVA.name(), contract.getResponsibleUser(), null);
      changed = true;
    }
    if (contract.getStatus() == CareContractStatus.ATIVA && contract.getEndDate() != null && contract.getEndDate().isBefore(today)) {
      contract.setStatus(CareContractStatus.FINALIZADA); contract.setClosureReason("Período contratado concluído.");
      history.record(StatusHistoryEntityType.CARE_CONTRACT, contract.getId(), CareContractStatus.ATIVA.name(), CareContractStatus.FINALIZADA.name(), contract.getResponsibleUser(), contract.getClosureReason());
      changed = true;
    }
    if (changed) contracts.saveAndFlush(contract);
  }

  private User requireResponsible(UUID id) { User user = users.findById(id); if (user.getUserType() != UserType.RESPONSAVEL && user.getUserType() != UserType.FAMILY) throw new BusinessException("Acesso permitido apenas para responsáveis.", HttpStatus.FORBIDDEN); return user; }
  private BusinessException notFound() { return new BusinessException("Contratação não encontrada.", HttpStatus.NOT_FOUND); }
  private String normalize(String value) { return value == null ? "" : Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim(); }
  private ContractHistoryStatusGroup requestGroup(ServiceRequestStatus status) { return switch (status) { case PENDENTE -> ContractHistoryStatusGroup.PENDENTES; case REJEITADA -> ContractHistoryStatusGroup.REJEITADAS; case CANCELADA -> ContractHistoryStatusGroup.CANCELADAS; case EXPIRADA -> ContractHistoryStatusGroup.EXPIRADAS; case ACEITA -> throw new IllegalArgumentException("Solicitação aceita é exibida como contratação."); }; }
  private ContractHistoryStatusGroup contractGroup(CareContractStatus status) { return switch (status) { case AGENDADA -> ContractHistoryStatusGroup.AGENDADAS; case ATIVA -> ContractHistoryStatusGroup.ATIVAS; case FINALIZADA -> ContractHistoryStatusGroup.ENCERRADAS; case CANCELADA -> ContractHistoryStatusGroup.CANCELADAS; }; }
  private String scheduleSummary(ServiceRequest request) { return request.getScheduleDays().stream().sorted(Comparator.comparing(day -> day.getWeekday().ordinal())).map(day -> weekdayLabel(day.getWeekday()) + " · " + day.getStartTime() + " às " + day.getEndTime()).collect(java.util.stream.Collectors.joining("\n")); }
  private String location(AddressFields address) { return address == null || address.getCidade() == null ? null : address.getCidade() + (address.getEstado() == null ? "" : " - " + address.getEstado()); }
  private String statusLabel(String status) { return switch (status) { case "PENDENTE" -> "Pendente"; case "ACEITA" -> "Aceita"; case "REJEITADA" -> "Rejeitada"; case "CANCELADA" -> "Cancelada"; case "EXPIRADA" -> "Expirada"; case "AGENDADA" -> "Agendada"; case "ATIVA" -> "Ativa"; case "FINALIZADA" -> "Encerrada"; default -> "Atualizada"; }; }
  private String historyLabel(StatusHistoryEntityType type, String previous, String current) { if (type == StatusHistoryEntityType.SERVICE_REQUEST && previous == null && "PENDENTE".equals(current)) return "Solicitação criada"; return (type == StatusHistoryEntityType.SERVICE_REQUEST ? "Solicitação " : "Contratação ") + statusLabel(current).toLowerCase(Locale.forLanguageTag("pt-BR")); }
  private String dependencyLabel(GrauDependencia value) { return switch (value) { case BAIXA -> "Baixa"; case MODERADA -> "Moderada"; case ALTA -> "Alta"; case TOTAL -> "Total"; case NAO_SEI_INFORMAR -> "Não informado"; }; }
  private String mobilityLabel(Mobilidade value) { return switch (value) { case INDEPENDENTE -> "Independente"; case BENGALA -> "Usa bengala"; case ANDADOR -> "Usa andador"; case CADEIRA_RODAS -> "Usa cadeira de rodas"; case ACAMADO -> "Pessoa acamada"; case AUXILIO_PESSOA -> "Precisa de auxílio de outra pessoa"; case OUTRO -> "Outra condição"; }; }
  private String weekdayLabel(DiaSemana value) { return switch (value) { case SEGUNDA -> "Segunda-feira"; case TERCA -> "Terça-feira"; case QUARTA -> "Quarta-feira"; case QUINTA -> "Quinta-feira"; case SEXTA -> "Sexta-feira"; case SABADO -> "Sábado"; case DOMINGO -> "Domingo"; }; }
  private String activityLabel(ServicoOferecido value) { return switch (value) { case HIGIENE_PESSOAL -> "Higiene pessoal"; case BANHO -> "Banho"; case ALIMENTACAO -> "Alimentação"; case LOCOMOCAO -> "Auxílio na locomoção"; case COMPANHIA -> "Companhia"; case MEDICACAO_ORIENTADA -> "Medicação orientada"; case CONSULTAS -> "Acompanhamento em consultas"; case ATIVIDADES_DOMESTICAS_LEVES -> "Atividades domésticas leves"; case MONITORAMENTO_NOTURNO -> "Monitoramento noturno"; case OUTRO -> "Outra atividade"; }; }
  private List<String> allergyLabels(AssistedPerson person) { List<String> labels = person.getAlergias().stream().map(value -> switch (value) { case NAO_POSSUI -> "Não possui"; case MEDICAMENTOS -> "Medicamentos"; case ALIMENTOS -> "Alimentos"; case PRODUTOS_HIGIENE -> "Produtos de higiene"; case LATEX -> "Látex"; case POEIRA -> "Poeira"; case OUTRO -> "Outra"; case NAO_SEI_INFORMAR -> "Não informado"; }).toList(); return appendDetails(labels, person.getAlergiasDetalhes(), person.getAlergiasOutro()); }
  private List<String> foodRestrictionLabels(AssistedPerson person) { List<String> labels = person.getRestricoesAlimentares().stream().map(value -> switch (value) { case NAO_POSSUI -> "Não possui"; case DIABETICA -> "Dieta para diabetes"; case HIPOSSODICA -> "Dieta com pouco sódio"; case PASTOSA -> "Dieta pastosa"; case LIQUIDA -> "Dieta líquida"; case SEM_LACTOSE -> "Sem lactose"; case SEM_GLUTEN -> "Sem glúten"; case VEGETARIANA -> "Vegetariana"; case OUTRO -> "Outra"; case NAO_SEI_INFORMAR -> "Não informado"; }).toList(); return appendDetails(labels, person.getRestricoesAlimentaresDetalhes(), person.getRestricoesAlimentaresOutro()); }
  private List<String> appendDetails(List<String> labels, String details, String other) { List<String> result = new ArrayList<>(labels); if (other != null && !other.isBlank()) result.add(other); if (details != null && !details.isBlank()) result.add(details); return result; }
}
