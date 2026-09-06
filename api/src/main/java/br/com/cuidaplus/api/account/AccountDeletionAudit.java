package br.com.cuidaplus.api.account;

import br.com.cuidaplus.api.user.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuario_exclusao_auditoria")
public class AccountDeletionAudit {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "usuario_referencia", nullable = false)
  private UUID userReference;

  @Enumerated(EnumType.STRING)
  @Column(name = "perfil", nullable = false, length = 30)
  private UserType profile;

  @Column(name = "solicitado_em", nullable = false)
  private Instant requestedAt;

  @Column(name = "concluido_em", nullable = false)
  private Instant completedAt;

  @Column(name = "resultado", nullable = false, length = 30)
  private String result;

  @Column(name = "criado_em", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() { createdAt = Instant.now(); }

  public void setUserReference(UUID value) { userReference = value; }
  public void setProfile(UserType value) { profile = value; }
  public void setRequestedAt(Instant value) { requestedAt = value; }
  public void setCompletedAt(Instant value) { completedAt = value; }
  public void setResult(String value) { result = value; }
  public UUID getUserReference() { return userReference; }
  public UserType getProfile() { return profile; }
  public String getResult() { return result; }
}
