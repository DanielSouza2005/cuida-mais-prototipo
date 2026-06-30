package br.com.cuidaplus.api.profile;

import br.com.cuidaplus.api.user.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaregiverProfileRepository extends JpaRepository<CaregiverProfile, UUID> {
  Optional<CaregiverProfile> findByUser(User user);
}
