package br.com.cuidaplus.api.service_request;

import br.com.cuidaplus.api.care_routine.*;
import br.com.cuidaplus.api.care_task.*;
import br.com.cuidaplus.api.profile.DiaSemana;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalTime;
import java.util.*;

@Entity
@Table(name = "service_request_care_items_snapshot")
public class ServiceRequestCareItemSnapshot {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "service_request_id") private ServiceRequest serviceRequest;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "original_care_routine_id") private CareRoutine originalCareRoutine;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "original_care_routine_item_id") private CareRoutineItem originalCareRoutineItem;
  @Column(nullable = false, length = 140) private String title;
  @Column(length = 1000) private String description;
  @Column(nullable = false) private int sortOrder;
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
  @ElementCollection @CollectionTable(name = "service_request_care_snapshot_weekdays", joinColumns = @JoinColumn(name = "snapshot_item_id"))
  @Enumerated(EnumType.STRING) @Column(name = "weekday", length = 20) private Set<DiaSemana> weekdays = new LinkedHashSet<>();
  @Column(nullable = false, updatable = false) private Instant createdAt;
  @PrePersist void create() { createdAt = Instant.now(); }
  public UUID getId() { return id; }
  public void setServiceRequest(ServiceRequest value) { serviceRequest = value; }
  public void setOriginalCareRoutine(CareRoutine value) { originalCareRoutine = value; }
  public void setOriginalCareRoutineItem(CareRoutineItem value) { originalCareRoutineItem = value; }
  public String getTitle() { return title; }
  public void setTitle(String value) { title = value; }
  public String getDescription() { return description; }
  public void setDescription(String value) { description = value; }
  public int getSortOrder() { return sortOrder; }
  public void setSortOrder(int value) { sortOrder = value; }
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
