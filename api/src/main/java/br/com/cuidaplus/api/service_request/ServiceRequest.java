package br.com.cuidaplus.api.service_request;

import br.com.cuidaplus.api.profile.AssistedPerson;
import br.com.cuidaplus.api.profile.ServicoOferecido;
import br.com.cuidaplus.api.care_routine.CareRoutine;
import br.com.cuidaplus.api.user.User;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "service_requests")
public class ServiceRequest {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "responsible_user_id") private User responsibleUser;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "caregiver_user_id") private User caregiverUser;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ServiceRequestInitiator initiatedBy;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "requester_user_id") private User requesterUser;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "source_opportunity_id") private ServiceRequest sourceOpportunity;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "assisted_person_id") private AssistedPerson assistedPerson;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "care_routine_id") private CareRoutine careRoutine;
  @Column(name = "care_routine_name_snapshot", length = 140) private String careRoutineNameSnapshot;
  @OneToMany(mappedBy = "serviceRequest", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("sortOrder ASC") private List<ServiceRequestCareItemSnapshot> careItemsSnapshot = new ArrayList<>();
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private HiringType hiringType;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private ServiceRequestStatus status;
  private LocalDate startDate;
  private LocalDate endDate;
  @Column(nullable = false, length = 2000) private String needsDescription;
  @Column(length = 500) private String activityOther;
  @Column(length = 2000) private String additionalNotes;
  @Column(length = 1000) private String negotiationNotes;
  @Column(length = 1000) private String rejectionReason;
  @Column(length = 1000) private String cancellationReason;
  @ElementCollection @CollectionTable(name = "service_request_dates", joinColumns = @JoinColumn(name = "service_request_id")) @Column(name = "service_date") private Set<LocalDate> specificDates = new LinkedHashSet<>();
  @ElementCollection @CollectionTable(name = "service_request_schedule_days", joinColumns = @JoinColumn(name = "service_request_id")) private Set<ServiceRequestScheduleDay> scheduleDays = new LinkedHashSet<>();
  @ElementCollection @CollectionTable(name = "service_request_activities", joinColumns = @JoinColumn(name = "service_request_id")) @Enumerated(EnumType.STRING) @Column(name = "activity", length = 50) private Set<ServicoOferecido> activities = new LinkedHashSet<>();
  @Column(nullable = false, updatable = false) private Instant createdAt;
  @Column(nullable = false) private Instant updatedAt;
  @Column(nullable = false) private Instant expiresAt;
  private Instant canceledAt;
  @PrePersist void create() { Instant now = Instant.now(); createdAt = now; updatedAt = now; expiresAt = now.plus(Duration.ofDays(15)); if (status == null) status = ServiceRequestStatus.PENDENTE; if (initiatedBy == null) initiatedBy = ServiceRequestInitiator.RESPONSIBLE; }
  @PreUpdate void update() { updatedAt = Instant.now(); }
  public UUID getId() { return id; } public User getResponsibleUser() { return responsibleUser; } public void setResponsibleUser(User v) { responsibleUser=v; }
  public User getCaregiverUser() { return caregiverUser; } public void setCaregiverUser(User v) { caregiverUser=v; }
  public ServiceRequestInitiator getInitiatedBy() { return initiatedBy; } public void setInitiatedBy(ServiceRequestInitiator v) { initiatedBy=v; }
  public User getRequesterUser() { return requesterUser; } public void setRequesterUser(User v) { requesterUser=v; }
  public ServiceRequest getSourceOpportunity() { return sourceOpportunity; } public void setSourceOpportunity(ServiceRequest v) { sourceOpportunity=v; }
  public AssistedPerson getAssistedPerson() { return assistedPerson; } public void setAssistedPerson(AssistedPerson v) { assistedPerson=v; }
  public CareRoutine getCareRoutine() { return careRoutine; } public void setCareRoutine(CareRoutine v) { careRoutine=v; }
  public String getCareRoutineNameSnapshot() { return careRoutineNameSnapshot; } public void setCareRoutineNameSnapshot(String v) { careRoutineNameSnapshot=v; }
  public List<ServiceRequestCareItemSnapshot> getCareItemsSnapshot() { return careItemsSnapshot; }
  public void addCareItemSnapshot(ServiceRequestCareItemSnapshot item) { item.setServiceRequest(this); careItemsSnapshot.add(item); }
  public HiringType getHiringType() { return hiringType; } public void setHiringType(HiringType v) { hiringType=v; }
  public ServiceRequestStatus getStatus() { return status; } public void setStatus(ServiceRequestStatus v) { status=v; }
  public LocalDate getStartDate() { return startDate; } public void setStartDate(LocalDate v) { startDate=v; } public LocalDate getEndDate() { return endDate; } public void setEndDate(LocalDate v) { endDate=v; }
  public String getNeedsDescription() { return needsDescription; } public void setNeedsDescription(String v) { needsDescription=v; } public String getActivityOther() { return activityOther; } public void setActivityOther(String v) { activityOther=v; }
  public String getAdditionalNotes() { return additionalNotes; } public void setAdditionalNotes(String v) { additionalNotes=v; } public String getNegotiationNotes() { return negotiationNotes; } public void setNegotiationNotes(String v) { negotiationNotes=v; }
  public String getRejectionReason() { return rejectionReason; } public void setRejectionReason(String v) { rejectionReason=v; }
  public String getCancellationReason() { return cancellationReason; } public void setCancellationReason(String v) { cancellationReason=v; }
  public Set<LocalDate> getSpecificDates() { return specificDates; } public void setSpecificDates(Set<LocalDate> v) { specificDates=v; } public Set<ServiceRequestScheduleDay> getScheduleDays() { return scheduleDays; } public void setScheduleDays(Set<ServiceRequestScheduleDay> v) { scheduleDays=v; }
  public Set<ServicoOferecido> getActivities() { return activities; } public void setActivities(Set<ServicoOferecido> v) { activities=v; }
  public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; } public Instant getExpiresAt() { return expiresAt; } public Instant getCanceledAt() { return canceledAt; } public void setCanceledAt(Instant v) { canceledAt=v; }
}
