package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_task.dto.CompleteOccurrenceRequest;
import br.com.cuidaplus.api.care_task.dto.NotCompletedOccurrenceRequest;
import br.com.cuidaplus.api.care_task.dto.TaskOccurrencePageResponse;
import br.com.cuidaplus.api.care_task.dto.TaskOccurrenceResponse;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.contract_termination.ContractStatusProcessorService;
import br.com.cuidaplus.api.notification.NotificationService;
import br.com.cuidaplus.api.notification.NotificationType;
import br.com.cuidaplus.api.notification.RelatedEntityType;
import br.com.cuidaplus.api.service_attendance.ServiceAttendanceService;
import br.com.cuidaplus.api.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class TaskOccurrenceService {
    private final CareTaskRepository tasks;
    private final TaskOccurrenceRepository occurrences;
    private final TaskAuthorizationService authorization;
    private final TaskRecurrenceService recurrence;
    private final TaskResponseMapper mapper;
    private final TaskAuditService audit;
    private final CareActivityIntegrationService activities;
    private final ContractStatusProcessorService contractStatusProcessor;
    private final NotificationService notifications;
    private final TaskDateTimeService dateTimes;
    private final TaskReminderService reminders;
    private final ContractCareTaskProvisioningService provisioning;
    private final TaskOccurrenceExpirationService expiration;
    private final CareOccurrencePhotoService photoService;
    private final ServiceAttendanceService attendance;

    public TaskOccurrenceService(CareTaskRepository tasks, TaskOccurrenceRepository occurrences, TaskAuthorizationService authorization,
                                 TaskRecurrenceService recurrence, TaskResponseMapper mapper, TaskAuditService audit, CareActivityIntegrationService activities,
                                 ContractStatusProcessorService contractStatusProcessor, NotificationService notifications, TaskDateTimeService dateTimes,
                                 TaskReminderService reminders, ContractCareTaskProvisioningService provisioning, TaskOccurrenceExpirationService expiration,
                                 CareOccurrencePhotoService photoService, ServiceAttendanceService attendance) {
        this.tasks = tasks;
        this.occurrences = occurrences;
        this.authorization = authorization;
        this.recurrence = recurrence;
        this.mapper = mapper;
        this.audit = audit;
        this.activities = activities;
        this.contractStatusProcessor = contractStatusProcessor;
        this.notifications = notifications;
        this.dateTimes = dateTimes;
        this.reminders = reminders;
        this.provisioning = provisioning;
        this.expiration = expiration;
        this.photoService = photoService;
        this.attendance = attendance;
    }

    @Transactional
    public TaskOccurrencePageResponse list(UUID userId, LocalDate start, LocalDate end, TaskCategory category, TaskOccurrenceStatus status,
                                           UUID assistedPersonId, int page, int size) {
        User caregiver = authorization.requireCaregiver(userId);
        provisioning.provisionForCaregiver(caregiver);
        recurrence.validateRange(start, end);
        tasks.findByCaregiverExecutorOrderByUpdatedAtDesc(caregiver).forEach(task -> {
            contractStatusProcessor.processContractIfDue(task.getContract());
            recurrence.generate(task, start, end);
        });
        expiration.processExpiredPendingCareOccurrences();
        List<TaskOccurrence> unique = uniqueOccurrences(occurrences.findByCaregiverAndScheduledDateBetweenOrderByScheduledInstantUtcAsc(caregiver, start, end).stream()
                .filter(item -> item.getTask().getStatus() == TaskSeriesStatus.ATIVA || item.getStatus() != TaskOccurrenceStatus.PENDENTE)
                .filter(item -> activeContract(item) || item.getStatus() != TaskOccurrenceStatus.PENDENTE)
                .filter(recurrence::isWithinContractSchedule)
                .filter(item -> category == null || item.getTask().getCategory() == category)
                .filter(item -> assistedPersonId == null || item.getAssistedPerson().getId().equals(assistedPersonId))
                .toList());
        List<TaskOccurrenceResponse> filtered = unique.stream().filter(item -> status == null || recurrence.effectiveStatus(item) == status)
                .sorted(Comparator.comparing(TaskOccurrence::getScheduledInstantUtc).thenComparingInt(this::statusRank)).map(mapper::occurrence).toList();
        return page(filtered, page, size);
    }

    @Transactional
    public TaskOccurrencePageResponse listForResponsible(UUID userId, LocalDate date, String timezone, TaskOccurrenceStatus status, int page, int size) {
        dateTimes.requireZone(timezone);
        User responsible = authorization.requireResponsible(userId);
        provisioning.provisionForResponsible(responsible);
        tasks.findByResponsibleCreatorOrderByUpdatedAtDesc(responsible).stream().filter(task -> task.getDuplicateOfTask() == null).forEach(task -> {
            contractStatusProcessor.processContractIfDue(task.getContract());
            recurrence.generate(task, date, date);
        });
        expiration.processExpiredPendingCareOccurrences();
        List<TaskOccurrenceResponse> content = uniqueOccurrences(occurrences.findByResponsibleAndScheduledDateBetween(responsible, date, date)).stream()
                .filter(recurrence::isWithinContractSchedule)
                .filter(item -> status == null || recurrence.effectiveStatus(item) == status)
                .sorted(Comparator.comparing(TaskOccurrence::getScheduledInstantUtc).thenComparingInt(this::statusRank)).map(mapper::occurrence).toList();
        return page(content, page, size);
    }

    @Transactional
    public TaskOccurrencePageResponse day(UUID userId, LocalDate date, String timezone, TaskCategory category, TaskOccurrenceStatus status, UUID assistedPersonId) {
        return day(userId, date, timezone, category, status, assistedPersonId, 0, 50);
    }

    @Transactional
    public TaskOccurrencePageResponse day(UUID userId, LocalDate date, String timezone, TaskCategory category, TaskOccurrenceStatus status, UUID assistedPersonId, int page, int size) {
        dateTimes.requireZone(timezone);
        User caregiver = authorization.requireCaregiver(userId);
        provisioning.provisionForCaregiver(caregiver);
        tasks.findByCaregiverExecutorOrderByUpdatedAtDesc(caregiver).stream().filter(task -> task.getSourceSnapshotItem() != null && !timezone.equals(task.getTimezone())).forEach(task -> {
            task.setTimezone(timezone);
            occurrences.findByTaskAndScheduledDateBetweenOrderByScheduledInstantUtcAsc(task, date, date).stream()
                    .filter(item -> item.getStatus() == TaskOccurrenceStatus.PENDENTE || item.getStatus() == TaskOccurrenceStatus.ATRASADA)
                    .forEach(item -> {
                        item.setTimezone(timezone);
                        item.setScheduledInstantUtc(dateTimes.toInstant(item.getScheduledDate(), item.getScheduledTime(), timezone));
                        reminders.reschedule(item);
                    });
        });
        return list(userId, date, date, category, status, assistedPersonId, page, size);
    }

    @Transactional
    public TaskOccurrenceResponse details(UUID userId, UUID occurrenceId) {
        User caregiver = authorization.requireCaregiver(userId);
        expiration.processExpiredPendingCareOccurrences();
        return mapper.occurrence(owned(occurrenceId, caregiver));
    }

    @Transactional
    public TaskOccurrenceResponse responsibleDetails(UUID userId, UUID occurrenceId) {
        User responsible = authorization.requireResponsible(userId);
        expiration.processExpiredPendingCareOccurrences();
        TaskOccurrence occurrence = occurrences.findById(occurrenceId).orElseThrow(() -> new BusinessException("Cuidado não encontrado.", HttpStatus.NOT_FOUND));
        if (!occurrence.getTask().getResponsibleCreator().getId().equals(responsible.getId()))
            throw new BusinessException("Você não tem permissão para acessar este cuidado.", HttpStatus.FORBIDDEN);
        if (!recurrence.isWithinContractSchedule(occurrence))
            throw new BusinessException("Cuidado não encontrado.", HttpStatus.NOT_FOUND);
        return mapper.occurrence(occurrence);
    }

    @Transactional
    public TaskOccurrenceResponse complete(UUID userId, UUID occurrenceId, CompleteOccurrenceRequest request) {
        return complete(userId, occurrenceId, request.version(), request.executionNote(), List.of(), request.executedAt());
    }

    @Transactional
    public TaskOccurrenceResponse complete(UUID userId, UUID occurrenceId, long version, String executionNote, List<MultipartFile> photos) {
        return complete(userId, occurrenceId, version, executionNote, photos, null);
    }

    private TaskOccurrenceResponse complete(UUID userId, UUID occurrenceId, long version, String executionNote, List<MultipartFile> photos, Instant requestedExecutionTime) {
        User caregiver = authorization.requireCaregiver(userId);
        TaskOccurrence occurrence = owned(occurrenceId, caregiver);
        validateExecution(occurrence, version);
        List<MultipartFile> provided = photos == null ? List.of() : photos.stream().filter(file -> file != null && !file.isEmpty()).toList();
        if (provided.size() > CareOccurrencePhotoStorageService.MAX_PHOTOS)
            throw new BusinessException("Adicione no máximo 5 fotos.");
        if (occurrence.getTask().isRequiresCompletionPhoto() && provided.isEmpty())
            throw new BusinessException("Adicione pelo menos uma foto para concluir este cuidado.");
        Instant executedAt = requestedExecutionTime == null ? Instant.now() : requestedExecutionTime;
        if (executedAt.isAfter(Instant.now().plusSeconds(300)))
            throw new BusinessException("A data da execução não pode estar no futuro.");
        occurrence.setStatus(TaskOccurrenceStatus.CONCLUIDA);
        occurrence.setCompletedAt(executedAt);
        occurrence.setExecutedBy(caregiver);
        occurrence.setExecutionNote(trim(executionNote));
        occurrence.setStatusUpdatedAt(Instant.now());
        reminders.cancelFuture(occurrence);
        occurrences.saveAndFlush(occurrence);
        photoService.attach(occurrence, caregiver, provided);
        activities.createForCompletedOccurrence(occurrence, executedAt, trim(executionNote));
        audit.record(occurrence.getTask(), occurrence, caregiver, TaskAuditAction.OCORRENCIA_CONCLUIDA, "Cuidado concluído pelo cuidador.");
        notifications.create(occurrence.getTask().getResponsibleCreator(), NotificationType.CARE_OCCURRENCE_COMPLETED, "Cuidado concluído", occurrence.getTask().getTitle() + " foi marcado como concluído.", RelatedEntityType.CARE_OCCURRENCE, occurrence.getId());
        audit.record(occurrence.getTask(), occurrence, caregiver, TaskAuditAction.NOTIFICACAO_INTERNA_CRIADA, "Notificação interna de conclusão criada.");
        return mapper.occurrence(occurrence);
    }

    @Transactional
    public TaskOccurrenceResponse notCompleted(UUID userId, UUID occurrenceId, NotCompletedOccurrenceRequest request) {
        User caregiver = authorization.requireCaregiver(userId);
        TaskOccurrence occurrence = owned(occurrenceId, caregiver);
        validateExecution(occurrence, request.version());
        if (request.reason() == null || request.reason().isBlank())
            throw new BusinessException("Informe a justificativa da não realização.");
        occurrence.setStatus(TaskOccurrenceStatus.NAO_REALIZADA);
        occurrence.setExecutedBy(caregiver);
        occurrence.setNonCompletionReason(request.reason().trim());
        occurrence.setExecutionNote(trim(request.executionNote()));
        occurrence.setStatusUpdatedAt(Instant.now());
        reminders.cancelFuture(occurrence);
        audit.record(occurrence.getTask(), occurrence, caregiver, TaskAuditAction.OCORRENCIA_NAO_REALIZADA, "Cuidado marcado como não realizado.");
        reminders.notifyResponsibleNotDone(occurrence);
        return mapper.occurrence(occurrence);
    }

    private void validateExecution(TaskOccurrence occurrence, long version) {
        if (!occurrence.getScheduledDate().equals(dateTimes.today(occurrence.getTimezone())))
            throw new BusinessException("Este cuidado só pode ser atualizado no dia previsto.", HttpStatus.CONFLICT);
        if (occurrence.getVersion() != version)
            throw new BusinessException("Este cuidado foi atualizado em outro dispositivo. Recarregue os dados.", HttpStatus.CONFLICT);
        if (occurrence.getStatus() == TaskOccurrenceStatus.CONCLUIDA)
            throw new BusinessException("Este cuidado já foi concluído.", HttpStatus.CONFLICT);
        if (occurrence.getStatus() == TaskOccurrenceStatus.CANCELADA)
            throw new BusinessException("Um cuidado cancelado não pode ser executado.", HttpStatus.CONFLICT);
        if (occurrence.getStatus() == TaskOccurrenceStatus.NAO_REALIZADA)
            throw new BusinessException("Este cuidado já foi marcado como não realizado.", HttpStatus.CONFLICT);
        if (occurrence.getTask().getStatus() == TaskSeriesStatus.PAUSADA)
            throw new BusinessException("Este cuidado está pausado.", HttpStatus.CONFLICT);
        if (occurrence.getTask().getStatus() != TaskSeriesStatus.ATIVA)
            throw new BusinessException("Este cuidado não está ativo.", HttpStatus.CONFLICT);
        authorization.requireContractActive(contractStatusProcessor.processContractIfDue(occurrence.getContract()));
        attendance.requireActiveAttendance(occurrence.getContract(), occurrence.getScheduledDate());
    }

    private TaskOccurrence owned(UUID id, User caregiver) {
        return occurrences.findByIdAndCaregiver(id, caregiver)
                .filter(recurrence::isWithinContractSchedule)
                .orElseThrow(() -> new BusinessException("Cuidado não encontrado.", HttpStatus.NOT_FOUND));
    }

    private boolean activeContract(TaskOccurrence occurrence) {
        var status = occurrence.getContract().getStatus();
        return status == br.com.cuidaplus.api.care_contract.CareContractStatus.AGENDADA || status == br.com.cuidaplus.api.care_contract.CareContractStatus.ATIVA || status == br.com.cuidaplus.api.care_contract.CareContractStatus.ENCERRAMENTO_AGENDADO;
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private int statusRank(TaskOccurrence occurrence) {
        return switch (recurrence.effectiveStatus(occurrence)) {
            case ATRASADA -> 0;
            case PENDENTE -> 1;
            case CONCLUIDA -> 2;
            case NAO_REALIZADA -> 3;
            case CANCELADA -> 4;
        };
    }

    List<TaskOccurrence> uniqueOccurrences(List<TaskOccurrence> source) {
        Map<String, TaskOccurrence> unique = new LinkedHashMap<>();
        source.forEach(item -> unique.merge(occurrenceKey(item), item, this::preferredOccurrence));
        return new ArrayList<>(unique.values());
    }

    private String occurrenceKey(TaskOccurrence occurrence) {
        CareTask task = occurrence.getTask();
        CareTask canonical = task.getDuplicateOfTask() == null ? task : task.getDuplicateOfTask();
        UUID careItemId = canonical.getSourceSnapshotItem() == null ? canonical.getId() : canonical.getSourceSnapshotItem().getId();
        if (careItemId == null) careItemId = task.getId();
        return occurrence.getContract().getId() + "|" + careItemId + "|" + occurrence.getScheduledDate() + "|" + occurrence.getScheduledTime();
    }

    private TaskOccurrence preferredOccurrence(TaskOccurrence first, TaskOccurrence second) {
        boolean firstResult = executionResult(first.getStatus()), secondResult = executionResult(second.getStatus());
        if (firstResult != secondResult) return firstResult ? first : second;
        if (firstResult) {
            int firstRank = resultRank(first.getStatus()), secondRank = resultRank(second.getStatus());
            if (firstRank != secondRank) return firstRank < secondRank ? first : second;
        }
        boolean firstCanceled = first.getStatus() == TaskOccurrenceStatus.CANCELADA, secondCanceled = second.getStatus() == TaskOccurrenceStatus.CANCELADA;
        if (firstCanceled != secondCanceled) return firstCanceled ? second : first;
        boolean firstCanonical = first.getTask().getDuplicateOfTask() == null, secondCanonical = second.getTask().getDuplicateOfTask() == null;
        if (firstCanonical != secondCanonical) return firstCanonical ? first : second;
        return first.getCreatedAt().isBefore(second.getCreatedAt()) ? first : second;
    }

    private boolean executionResult(TaskOccurrenceStatus status) {
        return status == TaskOccurrenceStatus.CONCLUIDA || status == TaskOccurrenceStatus.NAO_REALIZADA;
    }

    private int resultRank(TaskOccurrenceStatus status) {
        return switch (status) {
            case CONCLUIDA -> 0;
            case NAO_REALIZADA -> 1;
            case ATRASADA -> 2;
            case PENDENTE -> 3;
            case CANCELADA -> 4;
        };
    }

    private TaskOccurrencePageResponse page(List<TaskOccurrenceResponse> items, int page, int size) {
        int safePage = Math.max(0, page), safeSize = Math.min(Math.max(1, size), 50), from = Math.min(safePage * safeSize, items.size()), to = Math.min(from + safeSize, items.size()), pages = items.isEmpty() ? 0 : (int) Math.ceil((double) items.size() / safeSize);
        return new TaskOccurrencePageResponse(items.subList(from, to), safePage, safeSize, items.size(), pages, safePage + 1 >= pages);
    }
}
