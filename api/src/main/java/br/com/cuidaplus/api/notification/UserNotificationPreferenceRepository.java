package br.com.cuidaplus.api.notification;

import br.com.cuidaplus.api.user.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationPreferenceRepository extends JpaRepository<UserNotificationPreference, UUID> {
  Optional<UserNotificationPreference> findByUserAndNotificationType(User user, NotificationType notificationType);
  List<UserNotificationPreference> findByUser(User user);
}
