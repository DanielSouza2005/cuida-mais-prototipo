package br.com.cuidaplus.api.profile;

import br.com.cuidaplus.api.user.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResponsibleProfileRepository extends JpaRepository<ResponsibleProfile, UUID> {
  Optional<ResponsibleProfile> findByUser(User user);
}
