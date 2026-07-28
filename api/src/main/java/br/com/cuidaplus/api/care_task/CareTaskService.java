package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.care_task.TaskAuthorizationService.TaskContext;
import br.com.cuidaplus.api.care_task.dto.*;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.contract_termination.ContractStatusProcessorService;
import br.com.cuidaplus.api.notification.*;
import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.user.User;
import java.time.*;
import java.util.*;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CareTaskService {
  private final CareTaskRepository tasks;
  private final TaskOccurrenceRepository occurrences;
  private final TaskAuthorizationService authorization;
  private final TaskDateTimeService dateTimes;
  private final TaskRecurrenceService recurrence;
  private final TaskResponseMapper mapper;
  private final TaskAuditService audit;
  private final CareContractRepository contracts;
  private final ContractStatusProcessorService contractStatusProcessor;
  private final NotificationService notifications;
  private final TaskOccurrenceExpirationService expiration;

  public CareTaskService(CareTaskRepository tasks, TaskOccurrenceRepository occurrences, TaskAuthorizationService authorization,
    TaskDateTimeService dateTimes, TaskRecurrenceService recurrence, TaskResponseMapper mapper, TaskAuditService audit,
    CareContractRepository contracts, ContractStatusProcessorService contractStatusProcessor,
    NotificationService notifications, TaskOccurrenceExpirationService expiration) {
    this.tasks = tasks; this.occurrences = occurrences; this.authorization = authorization; this.dateTimes = dateTimes;
    this.recurrence = recurrence; this.mapper = mapper; this.audit = audit;
    this.contracts = contracts; this.contractStatusProcessor = contractStatusProcessor; this.notifications = notifications; this.expiration = expiration;
  }

  @Transactional
  public CareTaskDetailsResponse create(UUID userId, CreateCareTaskRequest request) {
    User responsible = authorization.requireResponsible(userId);
    TaskContext context = authorization.requireEligibleContext(responsible, request.contractId(), request.assistedPersonId(), request.caregiverId());
    validate(request, context.contract());
    CareTask task = new CareTask();
    apply(task, request, context, responsible);
    task.setStatus(TaskSeriesStatus.ATIVA); task.setCreatedBy(responsible); task.setUpdatedBy(responsible);
    task = tasks.saveAndFlush(task);
    audit.record(task, null, responsible, TaskAuditAction.CRIADA, "Rotina de cuidados criada.");
    recurrence.generateInitialWindow(task);
    notifications.create(context.caregiver(), NotificationType.CARE_TASK_CREATED, "Novo cuidado", "Um novo cuidado foi adicionado à sua rotina.", RelatedEntityType.CARE_TASK, task.getId());
    return mapper.details(task);
  }

  @Transactional
  public TaskFormDataResponse formData(UUID userId) {
    User responsible = authorization.requireResponsible(userId);
    List<TaskFormDataResponse.ContractOption> options = contracts.findByResponsibleUserOrderByUpdatedAtDesc(responsible).stream()
      .map(contractStatusProcessor::processContractIfDue)
      .filter(this::activeContract)
      .map(contract -> new TaskFormDataResponse.ContractOption(contract.getId(), contract.getStatus(), contract.getStartDate(), contract.getEndDate(), contract.getEffectiveEndDate(),
        contract.getAssistedPerson().getId(), contract.getAssistedPerson().getNome(), contract.getCaregiverUser().getId(), contract.getCaregiverUser().getFullName()))
      .toList();
    return new TaskFormDataResponse(options);
  }

  @Transactional
  public CareTaskPageResponse list(UUID userId, String search, TaskCategory category, TaskSeriesStatus status, TaskPriority priority,
    UUID assistedPersonId, UUID caregiverId, TaskOccurrenceStatus occurrenceStatus, LocalDate startDate, LocalDate endDate, int page, int size) {
    User responsible = authorization.requireResponsible(userId);
    if (startDate != null || endDate != null) {
      if (startDate == null || endDate == null) throw new BusinessException("Informe as duas datas do período.");
      recurrence.validateRange(startDate, endDate);
    }
    Stream<CareTask> stream = tasks.findByResponsibleCreatorOrderByUpdatedAtDesc(responsible).stream().map(this::processSeries);
    String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.forLanguageTag("pt-BR"));
    List<CareTaskSummaryResponse> filtered = stream
      .filter(task -> normalizedSearch.isEmpty() || task.getTitle().toLowerCase(Locale.forLanguageTag("pt-BR")).contains(normalizedSearch))
      .filter(task -> category == null || task.getCategory() == category)
      .filter(task -> status == null || task.getStatus() == status)
      .filter(task -> priority == null || task.getPriority() == priority)
      .filter(task -> assistedPersonId == null || task.getAssistedPerson().getId().equals(assistedPersonId))
      .filter(task -> caregiverId == null || task.getCaregiverExecutor().getId().equals(caregiverId))
      .filter(task -> matchesOccurrenceFilters(task, occurrenceStatus, startDate, endDate))
      .peek(this::extendCurrentWindow)
      .map(mapper::summary).toList();
    return pageTasks(filtered, page, size);
  }

  @Transactional
  public CareTaskDetailsResponse details(UUID userId, UUID taskId) {
    User responsible = authorization.requireResponsible(userId);
    CareTask task = ownedTask(taskId, responsible);
    processSeries(task); extendCurrentWindow(task);
    return mapper.details(task);
  }

  @Transactional
  public CareTaskDetailsResponse update(UUID userId, UUID taskId, UpdateCareTaskRequest request) {
    User responsible = authorization.requireResponsible(userId);
    CareTask task = ownedTask(taskId, responsible);
    checkVersion(task.getVersion(), request.version());
    authorization.requireContractActive(contractStatusProcessor.processContractIfDue(task.getContract()));

    if (request.scope() == TaskEditScope.SOMENTE_ESTA_OCORRENCIA) {
      TaskOccurrence occurrence = ownedOccurrence(task, request.occurrenceId());
      checkVersion(occurrence.getVersion(), requiredOccurrenceVersion(request));
      editSingleOccurrence(occurrence, request.startDate(), request.scheduledTime(), request.timezone(), responsible);
      return mapper.details(task);
    }

    TaskContext context = new TaskContext(responsible, task.getAssistedPerson(), task.getContract(), task.getCaregiverExecutor());
    validate(request, task.getContract());
    if (request.scope() == TaskEditScope.ESTA_E_FUTURAS) {
      TaskOccurrence pivot = ownedOccurrence(task, request.occurrenceId());
      checkVersion(pivot.getVersion(), requiredOccurrenceVersion(request));
      cancelPendingFrom(task, pivot.getScheduledDate());
      LocalDate previousEnd = pivot.getScheduledDate().minusDays(1);
      task.setEndDate(previousEnd.isBefore(task.getStartDate()) ? task.getStartDate() : previousEnd);
      task.setStatus(TaskSeriesStatus.FINALIZADA); task.setUpdatedBy(responsible);
      CareTask next = new CareTask();
      apply(next, request, context, responsible); next.setStartDate(pivot.getScheduledDate()); next.setPreviousSeries(task);
      next.setStatus(TaskSeriesStatus.ATIVA); next.setCreatedBy(responsible); next.setUpdatedBy(responsible);
      next = tasks.saveAndFlush(next);
      audit.record(task, pivot, responsible, TaskAuditAction.ALTERADA, "Série encerrada e substituída a partir desta ocorrência.");
      audit.record(next, null, responsible, TaskAuditAction.CRIADA, "Continuação de uma rotina anterior.");
      recurrence.generateInitialWindow(next);
      return mapper.details(next);
    }

    deletePendingFrom(task, dateTimes.today(request.timezone()));
    apply(task, request, context, responsible); task.setUpdatedBy(responsible);
    audit.record(task, null, responsible, TaskAuditAction.ALTERADA, "Toda a série foi atualizada.");
    tasks.flush(); recurrence.generateInitialWindow(task);
    return mapper.details(task);
  }

  @Transactional
  public CareTaskDetailsResponse pause(UUID userId, UUID taskId, TaskActionRequest request) {
    User actor = authorization.requireResponsible(userId); CareTask task = ownedTask(taskId, actor); checkVersion(task.getVersion(), request.version());
    if (task.getStatus() != TaskSeriesStatus.ATIVA) throw new BusinessException("Apenas cuidados ativos podem ser pausados.", HttpStatus.CONFLICT);
    task.setStatus(TaskSeriesStatus.PAUSADA); task.setUpdatedBy(actor); audit.record(task, null, actor, TaskAuditAction.PAUSADA, safeReason(request.reason(), "Rotina pausada."));
    return mapper.details(task);
  }

  @Transactional
  public CareTaskDetailsResponse reactivate(UUID userId, UUID taskId, TaskActionRequest request) {
    User actor = authorization.requireResponsible(userId); CareTask task = ownedTask(taskId, actor); checkVersion(task.getVersion(), request.version());
    if (task.getStatus() != TaskSeriesStatus.PAUSADA) throw new BusinessException("Apenas cuidados pausados podem ser reativados.", HttpStatus.CONFLICT);
    authorization.requireContractActive(contractStatusProcessor.processContractIfDue(task.getContract()));
    task.setStatus(TaskSeriesStatus.ATIVA); task.setUpdatedBy(actor); audit.record(task, null, actor, TaskAuditAction.REATIVADA, safeReason(request.reason(), "Rotina reativada."));
    tasks.flush(); extendCurrentWindow(task); return mapper.details(task);
  }

  @Transactional
  public CareTaskDetailsResponse cancel(UUID userId, UUID taskId, TaskActionRequest request) {
    User actor = authorization.requireResponsible(userId); CareTask task = ownedTask(taskId, actor); checkVersion(task.getVersion(), request.version());
    if (task.getStatus() == TaskSeriesStatus.CANCELADA || task.getStatus() == TaskSeriesStatus.FINALIZADA) throw new BusinessException("Este cuidado não pode mais ser cancelado.", HttpStatus.CONFLICT);
    task.setStatus(TaskSeriesStatus.CANCELADA); task.setUpdatedBy(actor); cancelPendingFrom(task, LocalDate.MIN);
    audit.record(task, null, actor, TaskAuditAction.CANCELADA, safeReason(request.reason(), "Rotina cancelada."));
    notifications.create(task.getCaregiverExecutor(), NotificationType.CARE_TASK_CANCELED, "Cuidado cancelado", "Um cuidado da rotina foi cancelado.", RelatedEntityType.CARE_TASK, task.getId());
    return mapper.details(task);
  }

  @Transactional
  public TaskOccurrencePageResponse occurrences(UUID userId, UUID taskId, LocalDate start, LocalDate end, TaskOccurrenceStatus status, boolean history, int page, int size) {
    User responsible = authorization.requireResponsible(userId); CareTask task = ownedTask(taskId, responsible);
    recurrence.validateRange(start, end); contractStatusProcessor.processContractIfDue(task.getContract()); recurrence.generate(task, start, end);
    expiration.processExpiredPendingCareOccurrences();
    Comparator<TaskOccurrence> order = Comparator.comparing(TaskOccurrence::getScheduledInstantUtc);
    if (history) order = order.reversed();
    List<TaskOccurrenceResponse> items = occurrences.findByTaskAndScheduledDateBetweenOrderByScheduledInstantUtcAsc(task, start, end).stream()
      .filter(item -> status == null || recurrence.effectiveStatus(item) == status).sorted(order).map(mapper::occurrence).toList();
    return pageOccurrences(items, page, size);
  }

  @Transactional
  public TaskOccurrenceResponse cancelOccurrence(UUID userId, UUID occurrenceId, TaskActionRequest request) {
    User actor = authorization.requireResponsible(userId);
    TaskOccurrence occurrence = occurrences.findById(occurrenceId).orElseThrow(() -> new BusinessException("Ocorrência não encontrada.", HttpStatus.NOT_FOUND));
    if (!occurrence.getTask().getResponsibleCreator().getId().equals(actor.getId())) throw new BusinessException("Você não tem permissão para acessar esta ocorrência.", HttpStatus.FORBIDDEN);
    checkVersion(occurrence.getVersion(), request.version()); requireMutable(occurrence);
    occurrence.setStatus(TaskOccurrenceStatus.CANCELADA); occurrence.setCanceledAt(Instant.now());
    audit.record(occurrence.getTask(), occurrence, actor, TaskAuditAction.OCORRENCIA_CANCELADA, safeReason(request.reason(), "Ocorrência cancelada."));
    return mapper.occurrence(occurrence);
  }

  @Transactional
  public TaskOccurrenceResponse occurrenceDetails(UUID userId, UUID occurrenceId) {
    User actor = authorization.requireResponsible(userId);
    expiration.processExpiredPendingCareOccurrences();
    TaskOccurrence occurrence = occurrences.findById(occurrenceId).orElseThrow(() -> new BusinessException("Ocorrência não encontrada.", HttpStatus.NOT_FOUND));
    if (!occurrence.getTask().getResponsibleCreator().getId().equals(actor.getId())) throw new BusinessException("Você não tem permissão para acessar esta ocorrência.", HttpStatus.FORBIDDEN);
    return mapper.occurrence(occurrence);
  }

  private void validate(CreateCareTaskRequest request, CareContract contract) {
    validateValues(request.category(), request.customCategory(), request.recurrenceType(), request.startDate(), request.endDate(), request.scheduledTime(),
      request.intervalDays(), request.weekdays(), request.timezone(), request.reminderEnabled(), request.reminderMinutesBefore(), request.medication(), contract);
  }
  private void validate(UpdateCareTaskRequest request, CareContract contract) {
    validateValues(request.category(), request.customCategory(), request.recurrenceType(), request.startDate(), request.endDate(), request.scheduledTime(),
      request.intervalDays(), request.weekdays(), request.timezone(), request.reminderEnabled(), request.reminderMinutesBefore(), request.medication(), contract);
    if ((request.scope() == TaskEditScope.SOMENTE_ESTA_OCORRENCIA || request.scope() == TaskEditScope.ESTA_E_FUTURAS) && request.occurrenceId() == null) throw new BusinessException("Selecione a ocorrência que será alterada.");
  }

  private void validateValues(TaskCategory category, String customCategory, TaskRecurrenceType recurrenceType, LocalDate startDate, LocalDate endDate,
    LocalTime scheduledTime, Integer intervalDays, Set<DiaSemana> weekdays, String timezone, boolean reminderEnabled, Integer reminderMinutesBefore,
    MedicationRequest medication, CareContract contract) {
    dateTimes.requireZone(timezone);
    if (endDate != null && endDate.isBefore(startDate)) throw new BusinessException("A data final não pode ser anterior à data inicial.");
    if (recurrenceType == TaskRecurrenceType.PERIODO_DETERMINADO && endDate == null) throw new BusinessException("Informe a data final do período determinado.");
    if (recurrenceType == TaskRecurrenceType.DIAS_ESPECIFICOS && (weekdays == null || weekdays.isEmpty())) throw new BusinessException("Selecione ao menos um dia da semana.");
    if (recurrenceType == TaskRecurrenceType.INTERVALO && (intervalDays == null || intervalDays <= 0)) throw new BusinessException("O intervalo deve ser maior que zero.");
    if (category == TaskCategory.PERSONALIZADA && blank(customCategory)) throw new BusinessException("Informe o nome da categoria personalizada.");
    if (reminderEnabled && reminderMinutesBefore == null) throw new BusinessException("Informe a antecedência do lembrete.");
    if (startDate.isBefore(contract.getStartDate())) throw new BusinessException("O cuidado não pode começar antes da contratação.");
    LocalDate contractEnd = contract.getStatus() == CareContractStatus.ENCERRAMENTO_AGENDADO ? contract.getEffectiveEndDate() : contract.getEndDate();
    if (contractEnd != null && (startDate.isAfter(contractEnd) || endDate != null && endDate.isAfter(contractEnd))) throw new BusinessException("O período do cuidado ultrapassa a vigência da contratação.");
    validateMedication(category, medication);
  }

  private void validateMedication(TaskCategory category, MedicationRequest medication) {
    if (category != TaskCategory.MEDICACAO) {
      if (medication != null && (medication.unit() != null || medication.administrationRoute() != null || Stream.of(medication.name(), medication.dosage(), medication.customUnit(), medication.customAdministrationRoute(), medication.additionalInstructions()).anyMatch(value -> !blank(value))))
        throw new BusinessException("Dados de medicamento só podem ser informados na categoria Medicação.");
      return;
    }
    if (medication == null || blank(medication.name()) || blank(medication.dosage()) || medication.unit() == null || medication.administrationRoute() == null)
      throw new BusinessException("Preencha nome, dosagem, unidade e forma de administração do medicamento.");
    if (medication.unit() == MedicationUnit.PERSONALIZADA && blank(medication.customUnit())) throw new BusinessException("Informe a unidade personalizada.");
    if (medication.administrationRoute() == MedicationAdministrationRoute.OUTRA && blank(medication.customAdministrationRoute())) throw new BusinessException("Informe a forma de administração personalizada.");
  }

  private void apply(CareTask task, CreateCareTaskRequest request, TaskContext context, User actor) {
    applyValues(task, request.title(), request.description(), request.category(), request.customCategory(), request.priority(), request.recurrenceType(), request.startDate(), request.endDate(), request.scheduledTime(), request.intervalDays(), request.weekdays(), request.timezone(), request.reminderEnabled(), request.reminderMinutesBefore(), request.notes(), request.medication());
    task.setAssistedPerson(context.assistedPerson()); task.setContract(context.contract()); task.setResponsibleCreator(context.responsible()); task.setCaregiverExecutor(context.caregiver());
  }
  private void apply(CareTask task, UpdateCareTaskRequest request, TaskContext context, User actor) {
    applyValues(task, request.title(), request.description(), request.category(), request.customCategory(), request.priority(), request.recurrenceType(), request.startDate(), request.endDate(), request.scheduledTime(), request.intervalDays(), request.weekdays(), request.timezone(), request.reminderEnabled(), request.reminderMinutesBefore(), request.notes(), request.medication());
    task.setAssistedPerson(context.assistedPerson()); task.setContract(context.contract()); task.setResponsibleCreator(context.responsible()); task.setCaregiverExecutor(context.caregiver());
  }
  private void applyValues(CareTask task, String title, String description, TaskCategory category, String customCategory, TaskPriority priority,
    TaskRecurrenceType recurrenceType, LocalDate startDate, LocalDate endDate, LocalTime time, Integer interval, Set<DiaSemana> weekdays,
    String timezone, boolean reminder, Integer reminderMinutes, String notes, MedicationRequest medication) {
    task.setTitle(title.trim()); task.setDescription(trim(description)); task.setCategory(category); task.setCustomCategory(category == TaskCategory.PERSONALIZADA ? trim(customCategory) : null);
    task.setPriority(priority); task.setRecurrenceType(recurrenceType); task.setStartDate(startDate);
    task.setEndDate(recurrenceType == TaskRecurrenceType.UNICA ? startDate : recurrenceType == TaskRecurrenceType.SEM_DATA_FINAL ? null : endDate);
    task.setScheduledTime(time); task.setIntervalDays(recurrenceType == TaskRecurrenceType.INTERVALO ? interval : null);
    task.setWeekdays(recurrenceType == TaskRecurrenceType.DIAS_ESPECIFICOS ? new LinkedHashSet<>(weekdays) : new LinkedHashSet<>());
    task.setTimezone(timezone); task.setReminderEnabled(reminder); task.setReminderMinutesBefore(reminder ? reminderMinutes : null); task.setNotes(trim(notes));
    task.setMedication(category == TaskCategory.MEDICACAO ? medication(medication) : null);
  }

  private MedicationDetails medication(MedicationRequest request) {
    MedicationDetails value = new MedicationDetails(); value.setName(request.name().trim()); value.setDosage(request.dosage().trim()); value.setUnit(request.unit());
    value.setCustomUnit(trim(request.customUnit())); value.setAdministrationRoute(request.administrationRoute()); value.setCustomAdministrationRoute(trim(request.customAdministrationRoute())); value.setAdditionalInstructions(trim(request.additionalInstructions())); return value;
  }

  private CareTask processSeries(CareTask task) {
    contractStatusProcessor.processContractIfDue(task.getContract());
    if (task.getStatus() == TaskSeriesStatus.ATIVA && !activeContract(task.getContract())) task.setStatus(TaskSeriesStatus.FINALIZADA);
    if (task.getStatus() == TaskSeriesStatus.ATIVA && task.getEndDate() != null && task.getEndDate().isBefore(dateTimes.today(task.getTimezone()))) task.setStatus(TaskSeriesStatus.FINALIZADA);
    return task;
  }
  private void extendCurrentWindow(CareTask task) {
    if (task.getStatus() != TaskSeriesStatus.ATIVA) return;
    LocalDate today = dateTimes.today(task.getTimezone()); recurrence.generate(task, today, today.plusDays(TaskRecurrenceService.GENERATION_WINDOW_DAYS - 1L));
  }
  private void cancelPendingFrom(CareTask task, LocalDate from) {
    occurrences.findByTaskOrderByScheduledInstantUtcDesc(task).stream().filter(item -> !item.getScheduledDate().isBefore(from) && item.getStatus() == TaskOccurrenceStatus.PENDENTE)
      .forEach(item -> { item.setStatus(TaskOccurrenceStatus.CANCELADA); item.setCanceledAt(Instant.now()); });
  }
  private void deletePendingFrom(CareTask task, LocalDate from) {
    List<TaskOccurrence> pending = occurrences.findByTaskOrderByScheduledInstantUtcDesc(task).stream()
      .filter(item -> !item.getScheduledDate().isBefore(from) && item.getStatus() == TaskOccurrenceStatus.PENDENTE).toList();
    occurrences.deleteAll(pending);
    occurrences.flush();
  }
  private void editSingleOccurrence(TaskOccurrence occurrence, LocalDate date, LocalTime time, String timezone, User actor) {
    requireMutable(occurrence); dateTimes.requireZone(timezone);
    if ((!occurrence.getScheduledDate().equals(date) || !occurrence.getScheduledTime().equals(time)) && occurrences.existsByTaskAndScheduledDateAndScheduledTime(occurrence.getTask(), date, time)) throw new BusinessException("Já existe uma ocorrência nesse dia e horário.", HttpStatus.CONFLICT);
    occurrence.setScheduledDate(date); occurrence.setScheduledTime(time); occurrence.setTimezone(timezone); occurrence.setScheduledInstantUtc(dateTimes.toInstant(date, time, timezone)); occurrence.setException(true);
    audit.record(occurrence.getTask(), occurrence, actor, TaskAuditAction.OCORRENCIA_ALTERADA, "Somente esta ocorrência foi atualizada.");
  }
  private CareTask ownedTask(UUID id, User owner) { return tasks.findByIdAndResponsibleCreator(id, owner).orElseThrow(() -> new BusinessException("Cuidado não encontrado.", HttpStatus.NOT_FOUND)); }
  private TaskOccurrence ownedOccurrence(CareTask task, UUID id) { if (id == null) throw new BusinessException("Ocorrência não encontrada."); return occurrences.findById(id).filter(item -> item.getTask().getId().equals(task.getId())).orElseThrow(() -> new BusinessException("Ocorrência não encontrada.", HttpStatus.NOT_FOUND)); }
  private void requireMutable(TaskOccurrence occurrence) { if (occurrence.getStatus() == TaskOccurrenceStatus.CONCLUIDA) throw new BusinessException("Uma ocorrência concluída não pode ser alterada.", HttpStatus.CONFLICT); if (occurrence.getStatus() == TaskOccurrenceStatus.CANCELADA) throw new BusinessException("Uma ocorrência cancelada não pode ser alterada.", HttpStatus.CONFLICT); if (occurrence.getStatus() == TaskOccurrenceStatus.NAO_REALIZADA) throw new BusinessException("Uma ocorrência não realizada não pode ser alterada.", HttpStatus.CONFLICT); }
  private void checkVersion(long current, long requested) { if (current != requested) throw new BusinessException("Este registro foi atualizado em outro dispositivo. Recarregue os dados.", HttpStatus.CONFLICT); }
  private long requiredOccurrenceVersion(UpdateCareTaskRequest request) { if (request.occurrenceVersion() == null) throw new BusinessException("Informe a versão da ocorrência."); return request.occurrenceVersion(); }
  private boolean activeContract(CareContract contract) { return contract.getStatus() == CareContractStatus.ATIVA || contract.getStatus() == CareContractStatus.ENCERRAMENTO_AGENDADO; }
  private boolean matchesOccurrenceFilters(CareTask task, TaskOccurrenceStatus status, LocalDate start, LocalDate end) {
    if (status == null && start == null) return true;
    LocalDate from = start == null ? dateTimes.today(task.getTimezone()).minusDays(89) : start;
    LocalDate to = end == null ? dateTimes.today(task.getTimezone()) : end;
    recurrence.generate(task, from, to);
    return occurrences.findByTaskAndScheduledDateBetweenOrderByScheduledInstantUtcAsc(task, from, to).stream()
      .anyMatch(item -> status == null || recurrence.effectiveStatus(item) == status);
  }
  private boolean blank(String value) { return value == null || value.isBlank(); }
  private String trim(String value) { return blank(value) ? null : value.trim(); }
  private String safeReason(String reason, String fallback) { return blank(reason) ? fallback : reason.trim(); }
  private CareTaskPageResponse pageTasks(List<CareTaskSummaryResponse> items, int page, int size) { int safePage=Math.max(0,page),safeSize=Math.min(Math.max(1,size),50),from=Math.min(safePage*safeSize,items.size()),to=Math.min(from+safeSize,items.size()),pages=items.isEmpty()?0:(int)Math.ceil((double)items.size()/safeSize); return new CareTaskPageResponse(items.subList(from,to),safePage,safeSize,items.size(),pages,safePage+1>=pages); }
  private TaskOccurrencePageResponse pageOccurrences(List<TaskOccurrenceResponse> items, int page, int size) { int safePage=Math.max(0,page),safeSize=Math.min(Math.max(1,size),50),from=Math.min(safePage*safeSize,items.size()),to=Math.min(from+safeSize,items.size()),pages=items.isEmpty()?0:(int)Math.ceil((double)items.size()/safeSize); return new TaskOccurrencePageResponse(items.subList(from,to),safePage,safeSize,items.size(),pages,safePage+1>=pages); }
}
