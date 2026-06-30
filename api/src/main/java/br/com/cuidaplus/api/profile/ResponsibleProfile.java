package br.com.cuidaplus.api.profile;

import br.com.cuidaplus.api.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "responsible_profiles")
public class ResponsibleProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private Parentesco parentesco;

  @Column(length = 120)
  private String parentescoOutro;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private PreferenciaContato preferenciaContato;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Parentesco getParentesco() {
    return parentesco;
  }

  public void setParentesco(Parentesco parentesco) {
    this.parentesco = parentesco;
  }

  public String getParentescoOutro() {
    return parentescoOutro;
  }

  public void setParentescoOutro(String parentescoOutro) {
    this.parentescoOutro = parentescoOutro;
  }

  public PreferenciaContato getPreferenciaContato() {
    return preferenciaContato;
  }

  public void setPreferenciaContato(PreferenciaContato preferenciaContato) {
    this.preferenciaContato = preferenciaContato;
  }
}
