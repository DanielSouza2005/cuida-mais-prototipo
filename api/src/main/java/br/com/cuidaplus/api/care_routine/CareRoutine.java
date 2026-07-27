package br.com.cuidaplus.api.care_routine;

import br.com.cuidaplus.api.profile.AssistedPerson;
import br.com.cuidaplus.api.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "care_routines")
public class CareRoutine {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "responsible_user_id") private User responsibleUser;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assisted_person_id") private AssistedPerson assistedPerson;
  @Column(nullable = false, length = 140) private String name;
  @Column(length = 1000) private String description;
  @Column(nullable = false) private boolean active = true;
  @OneToMany(mappedBy = "careRoutine", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("sortOrder ASC, createdAt ASC")
  private List<CareRoutineItem> items = new ArrayList<>();
  @Column(nullable = false, updatable = false) private Instant createdAt;
  @Column(nullable = false) private Instant updatedAt;

  @PrePersist void create() { createdAt = Instant.now(); updatedAt = createdAt; }
  @PreUpdate void update() { updatedAt = Instant.now(); }
  public UUID getId() { return id; }
  public User getResponsibleUser() { return responsibleUser; }
  public void setResponsibleUser(User value) { responsibleUser = value; }
  public AssistedPerson getAssistedPerson() { return assistedPerson; }
  public void setAssistedPerson(AssistedPerson value) { assistedPerson = value; }
  public String getName() { return name; }
  public void setName(String value) { name = value; }
  public String getDescription() { return description; }
  public void setDescription(String value) { description = value; }
  public boolean isActive() { return active; }
  public void setActive(boolean value) { active = value; }
  public List<CareRoutineItem> getItems() { return items; }
  public void replaceItems(List<CareRoutineItem> value) { items.clear(); value.forEach(this::addItem); }
  public void addItem(CareRoutineItem item) { item.setCareRoutine(this); items.add(item); }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
