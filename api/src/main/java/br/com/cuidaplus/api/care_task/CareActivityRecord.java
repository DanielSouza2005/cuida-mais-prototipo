package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_contract.CareContract;
import br.com.cuidaplus.api.profile.AssistedPerson;
import br.com.cuidaplus.api.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "care_activity_records")
public class CareActivityRecord {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "occurrence_id", unique = true) private TaskOccurrence occurrence;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "contract_id") private CareContract contract;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "assisted_person_id") private AssistedPerson assistedPerson;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "responsible_user_id") private User responsible;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "caregiver_user_id") private User caregiver;
  @Column(nullable = false, length = 40) private String activityType;
  @Column(nullable = false, length = 180) private String title;
  @Column(length = 1000) private String notes;
  @Column(nullable = false) private Instant occurredAt;
  @Column(nullable = false, updatable = false) private Instant createdAt;
  @PrePersist void create() { createdAt = Instant.now(); }
  public UUID getId() { return id; }
  public TaskOccurrence getOccurrence() { return occurrence; } public void setOccurrence(TaskOccurrence value) { occurrence = value; }
  public CareContract getContract() { return contract; } public void setContract(CareContract value) { contract = value; }
  public AssistedPerson getAssistedPerson() { return assistedPerson; } public void setAssistedPerson(AssistedPerson value) { assistedPerson = value; }
  public User getResponsible() { return responsible; } public void setResponsible(User value) { responsible = value; }
  public User getCaregiver() { return caregiver; } public void setCaregiver(User value) { caregiver = value; }
  public String getActivityType() { return activityType; } public void setActivityType(String value) { activityType = value; }
  public String getTitle() { return title; } public void setTitle(String value) { title = value; }
  public String getNotes() { return notes; } public void setNotes(String value) { notes = value; }
  public Instant getOccurredAt() { return occurredAt; } public void setOccurredAt(Instant value) { occurredAt = value; }
  public Instant getCreatedAt() { return createdAt; }
}
