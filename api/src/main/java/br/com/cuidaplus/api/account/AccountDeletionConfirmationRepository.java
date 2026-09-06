package br.com.cuidaplus.api.account;

import br.com.cuidaplus.api.user.User;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountDeletionConfirmationRepository extends JpaRepository<AccountDeletionConfirmation, UUID> {
  List<AccountDeletionConfirmation> findByUserAndUsedAtIsNull(User user);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select confirmation from AccountDeletionConfirmation confirmation join fetch confirmation.user where confirmation.tokenHash = :tokenHash")
  Optional<AccountDeletionConfirmation> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);
}
