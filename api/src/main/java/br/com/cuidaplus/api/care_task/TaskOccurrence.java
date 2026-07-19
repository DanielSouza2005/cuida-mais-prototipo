package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_contract.CareContract;
import br.com.cuidaplus.api.profile.AssistedPerson;
import br.com.cuidaplus.api.user.User;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "task_occurrences", uniqueConstraints = @UniqueConstraint(name = "uk_task_occurrence_schedule", columnNames = {"task_id", "scheduled_date", "scheduled_time"}))
public class TaskOccurrence {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "task_id") private CareTask task;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "contract_id") private CareContract contract;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "assisted_person_id") private AssistedPerson assistedPerson;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "caregiver_user_id") private User caregiver;
  @Column(nullable = false) private LocalDate scheduledDate;
  @Column(nullable = false) private LocalTime scheduledTime;
  @Column(nullable = false) private Instant scheduledInstantUtc;
  @Column(nullable = false, length = 80) private String timezone;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 25) private TaskOccurrenceStatus status;
  private Instant completedAt;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "executed_by_user_id") private User executedBy;
  @Column(length = 1000) private String nonCompletionReason;
  @Column(length = 1000) private String executionNote;
  private Instant canceledAt;
  @Column(nullable = false) private boolean exception;
  @Column(nullable = false, updatable = false) private Instant createdAt;
  @Column(nullable = false) private Instant updatedAt;
  @Version @Column(nullable = false) private long version;

  @PrePersist void create() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
  @PreUpdate void update() { updatedAt = Instant.now(); }

  public UUID getId() { return id; }
  public CareTask getTask() { return task; } public void setTask(CareTask value) { task = value; }
  public CareContract getContract() { return contract; } public void setContract(CareContract value) { contract = value; }
  public AssistedPerson getAssistedPerson() { return assistedPerson; } public void setAssistedPerson(AssistedPerson value) { assistedPerson = value; }
  public User getCaregiver() { return caregiver; } public void setCaregiver(User value) { caregiver = value; }
  public LocalDate getScheduledDate() { return scheduledDate; } public void setScheduledDate(LocalDate value) { scheduledDate = value; }
  public LocalTime getScheduledTime() { return scheduledTime; } public void setScheduledTime(LocalTime value) { scheduledTime = value; }
  public Instant getScheduledInstantUtc() { return scheduledInstantUtc; } public void setScheduledInstantUtc(Instant value) { scheduledInstantUtc = value; }
  public String getTimezone() { return timezone; } public void setTimezone(String value) { timezone = value; }
  public TaskOccurrenceStatus getStatus() { return status; } public void setStatus(TaskOccurrenceStatus value) { status = value; }
  public Instant getCompletedAt() { return completedAt; } public void setCompletedAt(Instant value) { completedAt = value; }
  public User getExecutedBy() { return executedBy; } public void setExecutedBy(User value) { executedBy = value; }
  public String getNonCompletionReason() { return nonCompletionReason; } public void setNonCompletionReason(String value) { nonCompletionReason = value; }
  public String getExecutionNote() { return executionNote; } public void setExecutionNote(String value) { executionNote = value; }
  public Instant getCanceledAt() { return canceledAt; } public void setCanceledAt(Instant value) { canceledAt = value; }
  public boolean isException() { return exception; } public void setException(boolean value) { exception = value; }
  public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; }
  public long getVersion() { return version; }
}
