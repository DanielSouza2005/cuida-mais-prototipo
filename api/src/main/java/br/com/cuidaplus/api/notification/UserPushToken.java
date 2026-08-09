package br.com.cuidaplus.api.notification;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_push_tokens")
public class UserPushToken {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
  @Column(name = "expo_push_token", nullable = false, unique = true, length = 255) private String expoPushToken;
  @Column(nullable = false, length = 20) private String platform;
  @Column(length = 180) private String deviceId;
  @Column(length = 40) private String appVersion;
  @Column(nullable = false) private boolean active = true;
  @Column(nullable = false, updatable = false) private Instant createdAt;
  @Column(nullable = false) private Instant updatedAt;
  private Instant lastUsedAt;
  private Instant disabledAt;

  @PrePersist void prePersist() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
  @PreUpdate void preUpdate() { updatedAt = Instant.now(); }

  public UUID getId() { return id; }
  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public String getExpoPushToken() { return expoPushToken; }
  public void setExpoPushToken(String expoPushToken) { this.expoPushToken = expoPushToken; }
  public String getPlatform() { return platform; }
  public void setPlatform(String platform) { this.platform = platform; }
  public String getDeviceId() { return deviceId; }
  public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
  public String getAppVersion() { return appVersion; }
  public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
  public Instant getLastUsedAt() { return lastUsedAt; }
  public void setLastUsedAt(Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }
  public Instant getDisabledAt() { return disabledAt; }
  public void setDisabledAt(Instant disabledAt) { this.disabledAt = disabledAt; }
}
