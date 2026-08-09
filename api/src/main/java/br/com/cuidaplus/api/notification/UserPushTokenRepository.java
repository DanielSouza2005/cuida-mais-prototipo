package br.com.cuidaplus.api.notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPushTokenRepository extends JpaRepository<UserPushToken, UUID> {
  Optional<UserPushToken> findByExpoPushToken(String expoPushToken);
  List<UserPushToken> findByUserIdAndActiveTrue(UUID userId);
}
