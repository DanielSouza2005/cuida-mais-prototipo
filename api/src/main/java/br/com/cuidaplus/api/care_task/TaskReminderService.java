package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.notification.NotificationService;
import br.com.cuidaplus.api.notification.NotificationType;
import br.com.cuidaplus.api.notification.RelatedEntityType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TaskReminderService {
    private final TaskReminderRepository reminders;
    private final NotificationService notifications;
    private final TaskAuditService audit;

    public TaskReminderService(TaskReminderRepository reminders, NotificationService notifications, TaskAuditService audit) {
        this.reminders = reminders;
        this.notifications = notifications;
        this.audit = audit;
    }

    @Transactional
    public void scheduleFor(List<TaskOccurrence> occurrences) {
        occurrences.forEach(this::scheduleFor);
    }

    @Transactional
    public void scheduleFor(TaskOccurrence occurrence) {
        CareTask task = occurrence.getTask();
        if (!task.isReminderEnabled() || !ContractCareSchedulePolicy.allows(occurrence.getContract(), occurrence.getScheduledDate(), occurrence.getScheduledTime())) return;
        Instant base = occurrence.getScheduledInstantUtc();
        if (task.getReminderMinutesBefore() != null && task.getReminderMinutesBefore() > 0)
            create(occurrence, task.getCaregiverExecutor(), TaskReminderType.BEFORE_SCHEDULED_TIME, base.minusSeconds(task.getReminderMinutesBefore() * 60L));
        if (task.isReminderAtScheduledTime())
            create(occurrence, task.getCaregiverExecutor(), TaskReminderType.AT_SCHEDULED_TIME, base);
        if (task.isOverdueReminderEnabled() && task.getOverdueAfterMinutes() != null) {
            Instant overdue = base.plusSeconds(task.getOverdueAfterMinutes() * 60L);
            create(occurrence, task.getCaregiverExecutor(), TaskReminderType.OVERDUE, overdue);
            if (task.isImportant() && task.isNotifyResponsibleIfImportant())
                create(occurrence, task.getResponsibleCreator(), TaskReminderType.RESPONSIBLE_NOT_DONE_ALERT, overdue);
        }
    }

    @Transactional
    public void cancelFuture(TaskOccurrence occurrence) {
        Instant now = Instant.now();
        List<TaskReminder> scheduled = reminders.findByOccurrenceAndStatus(occurrence, TaskReminderStatus.SCHEDULED);
        scheduled.forEach(reminder -> {
            reminder.setStatus(TaskReminderStatus.CANCELED);
            reminder.setCanceledAt(now);
        });
        if (!scheduled.isEmpty())
            audit.record(occurrence.getTask(), occurrence, null, TaskAuditAction.LEMBRETE_CANCELADO, "Lembretes internos futuros cancelados.");
    }

    @Transactional
    public void reschedule(TaskOccurrence occurrence) {
        cancelFuture(occurrence);
        scheduleFor(occurrence);
    }

    @Transactional
    public void notifyResponsibleNotDone(TaskOccurrence occurrence) {
        CareTask task = occurrence.getTask();
        if (!task.isImportant() || !task.isNotifyResponsibleIfImportant()) return;
        TaskReminder reminder = create(occurrence, task.getResponsibleCreator(), TaskReminderType.RESPONSIBLE_NOT_DONE_ALERT, Instant.now());
        if (reminder != null) send(reminder, Instant.now());
    }

    @Scheduled(fixedDelayString = "${app.task-reminders.poll-ms:60000}")
    @Transactional
    public void dispatchDue() {
        Instant now = Instant.now();
        reminders.findByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(TaskReminderStatus.SCHEDULED, now).stream().limit(200).forEach(reminder -> send(reminder, now));
    }

    private TaskReminder create(TaskOccurrence occurrence, br.com.cuidaplus.api.user.User recipient, TaskReminderType type, Instant at) {
        String key = occurrence.getId() + "|" + type + "|" + at + "|" + recipient.getId();
        if (reminders.existsByDeduplicationKey(key)) return null;
        TaskReminder reminder = new TaskReminder();
        reminder.setOccurrence(occurrence);
        reminder.setRecipient(recipient);
        reminder.setReminderType(type);
        reminder.setScheduledAt(at);
        reminder.setStatus(TaskReminderStatus.SCHEDULED);
        reminder.setDeduplicationKey(key);
        return reminders.save(reminder);
    }

    private void send(TaskReminder reminder, Instant now) {
        TaskOccurrence occurrence = reminder.getOccurrence();
        if (!ContractCareSchedulePolicy.allows(occurrence.getContract(), occurrence.getScheduledDate(), occurrence.getScheduledTime())) {
            reminder.setStatus(TaskReminderStatus.CANCELED);
            reminder.setCanceledAt(now);
            return;
        }
        var contractStatus = occurrence.getContract().getStatus();
        if (contractStatus == br.com.cuidaplus.api.care_contract.CareContractStatus.CANCELADA || contractStatus == br.com.cuidaplus.api.care_contract.CareContractStatus.ENCERRADA || contractStatus == br.com.cuidaplus.api.care_contract.CareContractStatus.FINALIZADA) {
            reminder.setStatus(TaskReminderStatus.CANCELED);
            reminder.setCanceledAt(now);
            return;
        }
        if (reminder.getReminderType() != TaskReminderType.RESPONSIBLE_NOT_DONE_ALERT && occurrence.getStatus() != TaskOccurrenceStatus.PENDENTE && occurrence.getStatus() != TaskOccurrenceStatus.ATRASADA) {
            reminder.setStatus(TaskReminderStatus.SKIPPED);
            return;
        }
        if ((reminder.getReminderType() == TaskReminderType.BEFORE_SCHEDULED_TIME || reminder.getReminderType() == TaskReminderType.AT_SCHEDULED_TIME) && reminder.getScheduledAt().isBefore(now.minusSeconds(300))) {
            reminder.setStatus(TaskReminderStatus.SKIPPED);
            return;
        }
        CareTask task = occurrence.getTask();
        String time = occurrence.getScheduledTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        boolean responsible = reminder.getRecipient().getId().equals(task.getResponsibleCreator().getId());
        NotificationType type;
        String title;
        String message;
        if (responsible) {
            boolean notDone = occurrence.getStatus() == TaskOccurrenceStatus.NAO_REALIZADA;
            type = notDone ? NotificationType.CARE_OCCURRENCE_NOT_DONE : NotificationType.CARE_OCCURRENCE_RESPONSIBLE_ALERT;
            title = notDone ? "Cuidado importante não realizado" : "Cuidado importante pendente";
            message = "Um cuidado importante de " + occurrence.getAssistedPerson().getNome() + (notDone ? " não foi realizado." : " está atrasado.");
        } else if (reminder.getReminderType() == TaskReminderType.OVERDUE || reminder.getReminderType() == TaskReminderType.REPEAT_WHILE_PENDING) {
            type = NotificationType.CARE_OCCURRENCE_OVERDUE;
            title = "Cuidado atrasado";
            message = occurrence.getAssistedPerson().getNome() + " · " + task.getTitle() + " ainda não foi concluído.";
        } else {
            type = NotificationType.CARE_OCCURRENCE_REMINDER;
            title = "Lembrete de cuidado";
            message = occurrence.getAssistedPerson().getNome() + " · " + task.getTitle() + " às " + time;
        }
        notifications.create(reminder.getRecipient(), type, title, message, RelatedEntityType.CARE_OCCURRENCE, occurrence.getId(), reminder.getScheduledAt());
        audit.record(task, occurrence, null, TaskAuditAction.NOTIFICACAO_INTERNA_CRIADA, "Notificação interna do cuidado criada.");
        reminder.setStatus(TaskReminderStatus.SENT);
        reminder.setSentAt(now);
        if (!responsible && task.isRepeatWhilePending() && task.getRepeatIntervalMinutes() != null && (reminder.getReminderType() == TaskReminderType.OVERDUE || reminder.getReminderType() == TaskReminderType.REPEAT_WHILE_PENDING))
            create(occurrence, task.getCaregiverExecutor(), TaskReminderType.REPEAT_WHILE_PENDING, now.plusSeconds(task.getRepeatIntervalMinutes() * 60L));
    }
}
