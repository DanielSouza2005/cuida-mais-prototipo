package br.com.cuidaplus.api.care_routine;

import jakarta.persistence.*;
import br.com.cuidaplus.api.care_task.*;
import br.com.cuidaplus.api.profile.DiaSemana;
import java.time.LocalTime;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "care_routine_items")
public class CareRoutineItem {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "care_routine_id") private CareRoutine careRoutine;
  @Column(nullable = false, length = 140) private String title;
  @Column(length = 1000) private String description;
  @Column(nullable = false) private int sortOrder;
  @Column(nullable = false) private boolean active = true;
  @Enumerated(EnumType.STRING) @Column(length = 40) private TaskCategory category;
  @Column(length = 120) private String customCategory;
  @Enumerated(EnumType.STRING) @Column(length = 20) private TaskPriority priority;
  @Enumerated(EnumType.STRING) @Column(length = 40) private TaskRecurrenceType recurrenceType;
  private LocalTime scheduledTime;
  private Integer intervalDays;
  private Boolean reminderEnabled;
  private Integer reminderMinutesBefore;
  @Column(length = 2000) private String notes;
  @Embedded private MedicationDetails medication;
  @ElementCollection @CollectionTable(name = "care_routine_item_weekdays", joinColumns = @JoinColumn(name = "care_routine_item_id"))
  @Enumerated(EnumType.STRING) @Column(name = "weekday", length = 20) private Set<DiaSemana> weekdays = new LinkedHashSet<>();
  @Column(nullable = false, updatable = false) private Instant createdAt;
  @Column(nullable = false) private Instant updatedAt;
  @PrePersist void create() { createdAt = Instant.now(); updatedAt = createdAt; }
  @PreUpdate void update() { updatedAt = Instant.now(); }
  public UUID getId() { return id; }
  public CareRoutine getCareRoutine() { return careRoutine; }
  public void setCareRoutine(CareRoutine value) { careRoutine = value; }
  public String getTitle() { return title; }
  public void setTitle(String value) { title = value; }
  public String getDescription() { return description; }
  public void setDescription(String value) { description = value; }
  public int getSortOrder() { return sortOrder; }
  public void setSortOrder(int value) { sortOrder = value; }
  public boolean isActive() { return active; }
  public void setActive(boolean value) { active = value; }
  public TaskCategory getCategory() { return category; } public void setCategory(TaskCategory value) { category = value; }
  public String getCustomCategory() { return customCategory; } public void setCustomCategory(String value) { customCategory = value; }
  public TaskPriority getPriority() { return priority; } public void setPriority(TaskPriority value) { priority = value; }
  public TaskRecurrenceType getRecurrenceType() { return recurrenceType; } public void setRecurrenceType(TaskRecurrenceType value) { recurrenceType = value; }
  public LocalTime getScheduledTime() { return scheduledTime; } public void setScheduledTime(LocalTime value) { scheduledTime = value; }
  public Integer getIntervalDays() { return intervalDays; } public void setIntervalDays(Integer value) { intervalDays = value; }
  public Boolean getReminderEnabled() { return reminderEnabled; } public void setReminderEnabled(Boolean value) { reminderEnabled = value; }
  public Integer getReminderMinutesBefore() { return reminderMinutesBefore; } public void setReminderMinutesBefore(Integer value) { reminderMinutesBefore = value; }
  public String getNotes() { return notes; } public void setNotes(String value) { notes = value; }
  public MedicationDetails getMedication() { return medication; } public void setMedication(MedicationDetails value) { medication = value; }
  public Set<DiaSemana> getWeekdays() { return weekdays; } public void setWeekdays(Set<DiaSemana> value) { weekdays = value; }
}
