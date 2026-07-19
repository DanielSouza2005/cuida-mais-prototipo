package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "care_task_audit")
public class TaskAuditEntry {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "task_id") private CareTask task;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "occurrence_id") private TaskOccurrence occurrence;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "actor_user_id") private User actor;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private TaskAuditAction action;
  @Column(length = 500) private String details;
  @Column(nullable = false, updatable = false) private Instant createdAt;
  @PrePersist void create() { createdAt = Instant.now(); }
  public UUID getId() { return id; }
  public CareTask getTask() { return task; } public void setTask(CareTask value) { task = value; }
  public TaskOccurrence getOccurrence() { return occurrence; } public void setOccurrence(TaskOccurrence value) { occurrence = value; }
  public User getActor() { return actor; } public void setActor(User value) { actor = value; }
  public TaskAuditAction getAction() { return action; } public void setAction(TaskAuditAction value) { action = value; }
  public String getDetails() { return details; } public void setDetails(String value) { details = value; }
  public Instant getCreatedAt() { return createdAt; }
}
