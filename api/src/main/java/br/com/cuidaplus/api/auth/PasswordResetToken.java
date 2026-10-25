package br.com.cuidaplus.api.auth;

import br.com.cuidaplus.api.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column
  private Instant usedAt;

  @Column(name = "tentativas_codigo", nullable = false)
  private int codeAttempts;

  @Column(name = "verificado_em")
  private Instant verifiedAt;

  @Column(name = "hash_autorizacao", unique = true, length = 64)
  private String authorizationHash;

  @Column(name = "autorizacao_expira_em")
  private Instant authorizationExpiresAt;

  @Column(name = "autorizacao_usada_em")
  private Instant authorizationUsedAt;

  @Column(name = "invalidado_em")
  private Instant invalidatedAt;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    createdAt = Instant.now();
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

  public String getTokenHash() {
    return tokenHash;
  }

  public void setTokenHash(String tokenHash) {
    this.tokenHash = tokenHash;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public Instant getUsedAt() {
    return usedAt;
  }

  public void setUsedAt(Instant usedAt) {
    this.usedAt = usedAt;
  }

  public Instant getCreatedAt() { return createdAt; }
  public int getCodeAttempts() { return codeAttempts; }
  public void setCodeAttempts(int value) { codeAttempts = value; }
  public Instant getVerifiedAt() { return verifiedAt; }
  public void setVerifiedAt(Instant value) { verifiedAt = value; }
  public String getAuthorizationHash() { return authorizationHash; }
  public void setAuthorizationHash(String value) { authorizationHash = value; }
  public Instant getAuthorizationExpiresAt() { return authorizationExpiresAt; }
  public void setAuthorizationExpiresAt(Instant value) { authorizationExpiresAt = value; }
  public Instant getAuthorizationUsedAt() { return authorizationUsedAt; }
  public void setAuthorizationUsedAt(Instant value) { authorizationUsedAt = value; }
  public Instant getInvalidatedAt() { return invalidatedAt; }
  public void setInvalidatedAt(Instant value) { invalidatedAt = value; }
}
