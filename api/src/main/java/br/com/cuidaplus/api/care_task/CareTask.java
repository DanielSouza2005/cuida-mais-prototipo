package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_contract.CareContract;
import br.com.cuidaplus.api.profile.AssistedPerson;
import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.user.User;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "care_tasks")
public class CareTask {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @Column(nullable = false, length = 140) private String title;
  @Column(length = 2000) private String description;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private TaskCategory category;
  @Column(length = 120) private String customCategory;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TaskPriority priority;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private TaskRecurrenceType recurrenceType;
  @Column(nullable = false) private LocalDate startDate;
  private LocalDate endDate;
  @Column(nullable = false) private LocalTime scheduledTime;
  private Integer intervalDays;
  @Column(nullable = false, length = 80) private String timezone;
  @Column(nullable = false) private boolean reminderEnabled;
  private Integer reminderMinutesBefore;
  @Column(length = 2000) private String notes;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TaskSeriesStatus status;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "assisted_person_id") private AssistedPerson assistedPerson;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "contract_id") private CareContract contract;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "responsible_creator_id") private User responsibleCreator;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "caregiver_executor_id") private User caregiverExecutor;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "previous_series_id") private CareTask previousSeries;
  @Embedded private MedicationDetails medication;
  @ElementCollection @CollectionTable(name = "care_task_weekdays", joinColumns = @JoinColumn(name = "task_id"))
  @Enumerated(EnumType.STRING) @Column(name = "weekday", length = 20) private Set<DiaSemana> weekdays = new LinkedHashSet<>();
  @Column(nullable = false, updatable = false) private Instant createdAt;
  @Column(nullable = false) private Instant updatedAt;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by_user_id") private User createdBy;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "updated_by_user_id") private User updatedBy;
  @Version @Column(nullable = false) private long version;

  @PrePersist void create() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
  @PreUpdate void update() { updatedAt = Instant.now(); }

  public UUID getId() { return id; }
  public String getTitle() { return title; } public void setTitle(String value) { title = value; }
  public String getDescription() { return description; } public void setDescription(String value) { description = value; }
  public TaskCategory getCategory() { return category; } public void setCategory(TaskCategory value) { category = value; }
  public String getCustomCategory() { return customCategory; } public void setCustomCategory(String value) { customCategory = value; }
  public TaskPriority getPriority() { return priority; } public void setPriority(TaskPriority value) { priority = value; }
  public TaskRecurrenceType getRecurrenceType() { return recurrenceType; } public void setRecurrenceType(TaskRecurrenceType value) { recurrenceType = value; }
  public LocalDate getStartDate() { return startDate; } public void setStartDate(LocalDate value) { startDate = value; }
  public LocalDate getEndDate() { return endDate; } public void setEndDate(LocalDate value) { endDate = value; }
  public LocalTime getScheduledTime() { return scheduledTime; } public void setScheduledTime(LocalTime value) { scheduledTime = value; }
  public Integer getIntervalDays() { return intervalDays; } public void setIntervalDays(Integer value) { intervalDays = value; }
  public String getTimezone() { return timezone; } public void setTimezone(String value) { timezone = value; }
  public boolean isReminderEnabled() { return reminderEnabled; } public void setReminderEnabled(boolean value) { reminderEnabled = value; }
  public Integer getReminderMinutesBefore() { return reminderMinutesBefore; } public void setReminderMinutesBefore(Integer value) { reminderMinutesBefore = value; }
  public String getNotes() { return notes; } public void setNotes(String value) { notes = value; }
  public TaskSeriesStatus getStatus() { return status; } public void setStatus(TaskSeriesStatus value) { status = value; }
  public AssistedPerson getAssistedPerson() { return assistedPerson; } public void setAssistedPerson(AssistedPerson value) { assistedPerson = value; }
  public CareContract getContract() { return contract; } public void setContract(CareContract value) { contract = value; }
  public User getResponsibleCreator() { return responsibleCreator; } public void setResponsibleCreator(User value) { responsibleCreator = value; }
  public User getCaregiverExecutor() { return caregiverExecutor; } public void setCaregiverExecutor(User value) { caregiverExecutor = value; }
  public CareTask getPreviousSeries() { return previousSeries; } public void setPreviousSeries(CareTask value) { previousSeries = value; }
  public MedicationDetails getMedication() { return medication; } public void setMedication(MedicationDetails value) { medication = value; }
  public Set<DiaSemana> getWeekdays() { return weekdays; } public void setWeekdays(Set<DiaSemana> value) { weekdays = value; }
  public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; }
  public User getCreatedBy() { return createdBy; } public void setCreatedBy(User value) { createdBy = value; }
  public User getUpdatedBy() { return updatedBy; } public void setUpdatedBy(User value) { updatedBy = value; }
  public long getVersion() { return version; }
}
