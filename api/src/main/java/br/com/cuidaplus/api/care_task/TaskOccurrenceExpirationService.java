package br.com.cuidaplus.api.care_task;
import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class TaskOccurrenceExpirationService {
  private final TaskOccurrenceRepository occurrences; private final TaskDateTimeService dateTimes; private final TaskReminderService reminders; private final TaskAuditService audit;
  public TaskOccurrenceExpirationService(TaskOccurrenceRepository occurrences, TaskDateTimeService dateTimes, TaskReminderService reminders, TaskAuditService audit) { this.occurrences=occurrences;this.dateTimes=dateTimes;this.reminders=reminders;this.audit=audit; }
  @Scheduled(fixedDelayString="${app.task-occurrences.expiration-poll-ms:300000}") @Transactional
  public int processExpiredPendingCareOccurrences() {
    Instant now=Instant.now(); int changed=0;
    for (TaskOccurrence occurrence:occurrences.findByStatusIn(List.of(TaskOccurrenceStatus.PENDENTE,TaskOccurrenceStatus.ATRASADA))) {
      if (!ContractCareSchedulePolicy.allows(occurrence.getContract(), occurrence.getScheduledDate(), occurrence.getScheduledTime())) {
        occurrence.setStatus(TaskOccurrenceStatus.CANCELADA); occurrence.setCanceledAt(now); occurrence.setStatusUpdatedAt(now);
        reminders.cancelFuture(occurrence); audit.record(occurrence.getTask(),occurrence,null,TaskAuditAction.OCORRENCIA_CANCELADA,"Ocorrência cancelada por estar fora do horário da contratação."); changed++; continue;
      }
      if (!occurrence.getScheduledDate().isBefore(dateTimes.today(occurrence.getTimezone()))) continue;
      occurrence.setStatus(TaskOccurrenceStatus.NAO_REALIZADA); occurrence.setCompletedAt(null); occurrence.setExecutedBy(null); occurrence.setNonCompletionReason(null); occurrence.setExecutionNote(null); occurrence.setAutoMarkedNotDone(true); occurrence.setStatusUpdatedAt(now);
      reminders.cancelFuture(occurrence); reminders.notifyResponsibleNotDone(occurrence); audit.record(occurrence.getTask(),occurrence,null,TaskAuditAction.OCORRENCIA_NAO_REALIZADA_AUTOMATICAMENTE,"Não realizado automaticamente, pois o dia previsto já passou."); changed++;
    }
    return changed;
  }
}
