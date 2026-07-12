package br.com.cuidaplus.api.service_request;

import br.com.cuidaplus.api.profile.AssistedPerson;
import br.com.cuidaplus.api.profile.ServicoOferecido;
import br.com.cuidaplus.api.user.User;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "service_requests")
public class ServiceRequest {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "responsible_user_id") private User responsibleUser;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "caregiver_user_id") private User caregiverUser;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "assisted_person_id") private AssistedPerson assistedPerson;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private HiringType hiringType;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private ServiceRequestStatus status;
  private LocalDate startDate;
  private LocalDate endDate;
  @Column(nullable = false, length = 2000) private String needsDescription;
  @Column(length = 500) private String activityOther;
  @Column(length = 2000) private String additionalNotes;
  @Column(length = 1000) private String negotiationNotes;
  @Column(length = 1000) private String rejectionReason;
  @ElementCollection @CollectionTable(name = "service_request_dates", joinColumns = @JoinColumn(name = "service_request_id")) @Column(name = "service_date") private Set<LocalDate> specificDates = new LinkedHashSet<>();
  @ElementCollection @CollectionTable(name = "service_request_schedule_days", joinColumns = @JoinColumn(name = "service_request_id")) private Set<ServiceRequestScheduleDay> scheduleDays = new LinkedHashSet<>();
  @ElementCollection @CollectionTable(name = "service_request_activities", joinColumns = @JoinColumn(name = "service_request_id")) @Enumerated(EnumType.STRING) @Column(name = "activity", length = 50) private Set<ServicoOferecido> activities = new LinkedHashSet<>();
  @Column(nullable = false, updatable = false) private Instant createdAt;
  @Column(nullable = false) private Instant updatedAt;
  @Column(nullable = false) private Instant expiresAt;
  private Instant canceledAt;
  @PrePersist void create() { Instant now = Instant.now(); createdAt = now; updatedAt = now; expiresAt = now.plus(Duration.ofDays(15)); status = ServiceRequestStatus.PENDENTE; }
  @PreUpdate void update() { updatedAt = Instant.now(); }
  public UUID getId() { return id; } public User getResponsibleUser() { return responsibleUser; } public void setResponsibleUser(User v) { responsibleUser=v; }
  public User getCaregiverUser() { return caregiverUser; } public void setCaregiverUser(User v) { caregiverUser=v; }
  public AssistedPerson getAssistedPerson() { return assistedPerson; } public void setAssistedPerson(AssistedPerson v) { assistedPerson=v; }
  public HiringType getHiringType() { return hiringType; } public void setHiringType(HiringType v) { hiringType=v; }
  public ServiceRequestStatus getStatus() { return status; } public void setStatus(ServiceRequestStatus v) { status=v; }
  public LocalDate getStartDate() { return startDate; } public void setStartDate(LocalDate v) { startDate=v; } public LocalDate getEndDate() { return endDate; } public void setEndDate(LocalDate v) { endDate=v; }
  public String getNeedsDescription() { return needsDescription; } public void setNeedsDescription(String v) { needsDescription=v; } public String getActivityOther() { return activityOther; } public void setActivityOther(String v) { activityOther=v; }
  public String getAdditionalNotes() { return additionalNotes; } public void setAdditionalNotes(String v) { additionalNotes=v; } public String getNegotiationNotes() { return negotiationNotes; } public void setNegotiationNotes(String v) { negotiationNotes=v; }
  public String getRejectionReason() { return rejectionReason; } public void setRejectionReason(String v) { rejectionReason=v; }
  public Set<LocalDate> getSpecificDates() { return specificDates; } public void setSpecificDates(Set<LocalDate> v) { specificDates=v; } public Set<ServiceRequestScheduleDay> getScheduleDays() { return scheduleDays; } public void setScheduleDays(Set<ServiceRequestScheduleDay> v) { scheduleDays=v; }
  public Set<ServicoOferecido> getActivities() { return activities; } public void setActivities(Set<ServicoOferecido> v) { activities=v; }
  public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; } public Instant getExpiresAt() { return expiresAt; } public Instant getCanceledAt() { return canceledAt; } public void setCanceledAt(Instant v) { canceledAt=v; }
}
