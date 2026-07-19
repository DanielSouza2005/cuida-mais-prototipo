package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.user.User;
import org.springframework.stereotype.Service;

@Service
public class TaskAuditService {
  private final TaskAuditEntryRepository repository;
  public TaskAuditService(TaskAuditEntryRepository repository) { this.repository = repository; }

  public void record(CareTask task, TaskOccurrence occurrence, User actor, TaskAuditAction action, String details) {
    TaskAuditEntry entry = new TaskAuditEntry();
    entry.setTask(task); entry.setOccurrence(occurrence); entry.setActor(actor); entry.setAction(action); entry.setDetails(details);
    repository.save(entry);
  }
}
