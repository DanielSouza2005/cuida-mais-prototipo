package br.com.cuidaplus.api.care_task;
import java.time.Instant; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface TaskReminderRepository extends JpaRepository<TaskReminder,UUID>{
  boolean existsByDeduplicationKey(String key);
  List<TaskReminder> findByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(TaskReminderStatus status,Instant due);
  List<TaskReminder> findByOccurrenceAndStatus(TaskOccurrence occurrence,TaskReminderStatus status);
}
