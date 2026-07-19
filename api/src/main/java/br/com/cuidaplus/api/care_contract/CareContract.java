package br.com.cuidaplus.api.care_contract;

import br.com.cuidaplus.api.profile.AssistedPerson;
import br.com.cuidaplus.api.service_request.ServiceRequest;
import br.com.cuidaplus.api.user.User;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "care_contracts")
public class CareContract {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "service_request_id") private ServiceRequest serviceRequest;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "responsible_user_id") private User responsibleUser;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "caregiver_user_id") private User caregiverUser;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "assisted_person_id") private AssistedPerson assistedPerson;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private CareContractStatus status;
  @Column(nullable = false) private LocalDate startDate;
  private LocalDate endDate;
  @Column(length = 1000) private String cancellationReason;
  @Column(length = 1000) private String closureReason;
  @Enumerated(EnumType.STRING) @Column(length = 50) private ContractTerminationType terminationType;
  @Column(length = 1000) private String terminationReason;
  @Column(length = 1000) private String terminationNotes;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "termination_requested_by_user_id") private User terminationRequestedByUser;
  private Instant terminationRequestedAt;
  private LocalDate effectiveEndDate;
  private Instant canceledAt;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cancellation_requested_by_user_id") private User cancellationRequestedByUser;
  private Instant cancellationRequestedAt;
  @Column(nullable = false, updatable = false) private Instant createdAt;
  @Column(nullable = false) private Instant updatedAt;

  @PrePersist void create() { createdAt = Instant.now(); updatedAt = createdAt; }
  @PreUpdate void update() { updatedAt = Instant.now(); }

  public UUID getId() { return id; }
  public ServiceRequest getServiceRequest() { return serviceRequest; }
  public void setServiceRequest(ServiceRequest value) { serviceRequest = value; }
  public User getResponsibleUser() { return responsibleUser; }
  public void setResponsibleUser(User value) { responsibleUser = value; }
  public User getCaregiverUser() { return caregiverUser; }
  public void setCaregiverUser(User value) { caregiverUser = value; }
  public AssistedPerson getAssistedPerson() { return assistedPerson; }
  public void setAssistedPerson(AssistedPerson value) { assistedPerson = value; }
  public CareContractStatus getStatus() { return status; }
  public void setStatus(CareContractStatus value) { status = value; }
  public LocalDate getStartDate() { return startDate; }
  public void setStartDate(LocalDate value) { startDate = value; }
  public LocalDate getEndDate() { return endDate; }
  public void setEndDate(LocalDate value) { endDate = value; }
  public String getCancellationReason() { return cancellationReason; }
  public void setCancellationReason(String value) { cancellationReason = value; }
  public String getClosureReason() { return closureReason; }
  public void setClosureReason(String value) { closureReason = value; }
  public ContractTerminationType getTerminationType() { return terminationType; }
  public void setTerminationType(ContractTerminationType value) { terminationType = value; }
  public String getTerminationReason() { return terminationReason; }
  public void setTerminationReason(String value) { terminationReason = value; }
  public String getTerminationNotes() { return terminationNotes; }
  public void setTerminationNotes(String value) { terminationNotes = value; }
  public User getTerminationRequestedByUser() { return terminationRequestedByUser; }
  public void setTerminationRequestedByUser(User value) { terminationRequestedByUser = value; }
  public Instant getTerminationRequestedAt() { return terminationRequestedAt; }
  public void setTerminationRequestedAt(Instant value) { terminationRequestedAt = value; }
  public LocalDate getEffectiveEndDate() { return effectiveEndDate; }
  public void setEffectiveEndDate(LocalDate value) { effectiveEndDate = value; }
  public Instant getCanceledAt() { return canceledAt; }
  public void setCanceledAt(Instant value) { canceledAt = value; }
  public User getCancellationRequestedByUser() { return cancellationRequestedByUser; }
  public void setCancellationRequestedByUser(User value) { cancellationRequestedByUser = value; }
  public Instant getCancellationRequestedAt() { return cancellationRequestedAt; }
  public void setCancellationRequestedAt(Instant value) { cancellationRequestedAt = value; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
