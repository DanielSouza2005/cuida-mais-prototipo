package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "care_task_reminders")
public class TaskReminder {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "occurrence_id") private TaskOccurrence occurrence;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "recipient_user_id") private User recipient;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private TaskReminderType reminderType;
  @Column(nullable = false) private Instant scheduledAt;
  private Instant sentAt; private Instant canceledAt;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TaskReminderStatus status;
  @Column(nullable = false, unique = true, length = 220) private String deduplicationKey;
  @Column(nullable = false, updatable = false) private Instant createdAt;
  @Column(nullable = false) private Instant updatedAt;
  @PrePersist void create() { createdAt = Instant.now(); updatedAt = createdAt; }
  @PreUpdate void update() { updatedAt = Instant.now(); }
  public UUID getId(){return id;} public TaskOccurrence getOccurrence(){return occurrence;} public void setOccurrence(TaskOccurrence v){occurrence=v;}
  public User getRecipient(){return recipient;} public void setRecipient(User v){recipient=v;} public TaskReminderType getReminderType(){return reminderType;} public void setReminderType(TaskReminderType v){reminderType=v;}
  public Instant getScheduledAt(){return scheduledAt;} public void setScheduledAt(Instant v){scheduledAt=v;} public Instant getSentAt(){return sentAt;} public void setSentAt(Instant v){sentAt=v;}
  public Instant getCanceledAt(){return canceledAt;} public void setCanceledAt(Instant v){canceledAt=v;} public TaskReminderStatus getStatus(){return status;} public void setStatus(TaskReminderStatus v){status=v;}
  public String getDeduplicationKey(){return deduplicationKey;} public void setDeduplicationKey(String v){deduplicationKey=v;}
}
