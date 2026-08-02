package br.com.cuidaplus.api.notification;

import br.com.cuidaplus.api.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_notification_preferences", uniqueConstraints = @UniqueConstraint(name = "uk_user_notification_preferences", columnNames = {"user_id", "notification_type"}))
public class UserNotificationPreference {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
  @Enumerated(EnumType.STRING) @Column(name = "notification_type", nullable = false, length = 64) private NotificationType notificationType;
  @Column(nullable = false) private boolean enabled;
  @Column(nullable = false, updatable = false) private Instant createdAt;
  @Column(nullable = false) private Instant updatedAt;

  @PrePersist void prePersist() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
  @PreUpdate void preUpdate() { updatedAt = Instant.now(); }

  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public NotificationType getNotificationType() { return notificationType; }
  public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }
  public boolean isEnabled() { return enabled; }
  public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
