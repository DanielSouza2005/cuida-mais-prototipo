package br.com.cuidaplus.api.status_history;

import br.com.cuidaplus.api.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "status_history")
public class StatusHistory {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private StatusHistoryEntityType entityType;
  @Column(nullable = false) private UUID entityId;
  @Column(length = 30) private String previousStatus;
  @Column(nullable = false, length = 30) private String newStatus;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "changed_by_user_id") private User changedByUser;
  @Column(length = 1000) private String reason;
  @Column(nullable = false, updatable = false) private Instant createdAt;

  @PrePersist void create() { if (createdAt == null) createdAt = Instant.now(); }

  public UUID getId() { return id; }
  public StatusHistoryEntityType getEntityType() { return entityType; }
  public void setEntityType(StatusHistoryEntityType value) { entityType = value; }
  public UUID getEntityId() { return entityId; }
  public void setEntityId(UUID value) { entityId = value; }
  public String getPreviousStatus() { return previousStatus; }
  public void setPreviousStatus(String value) { previousStatus = value; }
  public String getNewStatus() { return newStatus; }
  public void setNewStatus(String value) { newStatus = value; }
  public User getChangedByUser() { return changedByUser; }
  public void setChangedByUser(User value) { changedByUser = value; }
  public String getReason() { return reason; }
  public void setReason(String value) { reason = value; }
  public Instant getCreatedAt() { return createdAt; }
}
