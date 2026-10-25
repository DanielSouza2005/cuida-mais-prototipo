package br.com.cuidaplus.api.auth;

import br.com.cuidaplus.api.user.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

  Optional<PasswordResetToken> findByTokenHash(String tokenHash);

  List<PasswordResetToken> findByUserAndUsedAtIsNull(User user);

  Optional<PasswordResetToken> findFirstByUserOrderByCreatedAtDesc(User user);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select token from PasswordResetToken token where token.id = :id")
  Optional<PasswordResetToken> findForUpdateById(@Param("id") UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select token from PasswordResetToken token where token.authorizationHash = :hash")
  Optional<PasswordResetToken> findForUpdateByAuthorizationHash(@Param("hash") String hash);

  void deleteByUser(User user);
}
