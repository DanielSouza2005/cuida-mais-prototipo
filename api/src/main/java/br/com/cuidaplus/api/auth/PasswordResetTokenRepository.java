package br.com.cuidaplus.api.auth;

import br.com.cuidaplus.api.user.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

  Optional<PasswordResetToken> findByTokenHash(String tokenHash);

  List<PasswordResetToken> findByUserAndUsedAtIsNull(User user);
}
