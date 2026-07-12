package br.com.cuidaplus.api.notification;
import br.com.cuidaplus.api.user.User; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface NotificationRepository extends JpaRepository<Notification,UUID>{List<Notification> findByRecipientAndClearedAtIsNullOrderByCreatedAtDesc(User user);Optional<Notification> findByIdAndRecipient(UUID id,User user);long countByRecipientAndReadAtIsNullAndClearedAtIsNull(User user);}
