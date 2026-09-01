package br.com.cuidaplus.api.profile;

import br.com.cuidaplus.api.user.User;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CaregiverProfileRepository extends JpaRepository<CaregiverProfile, UUID> {
  Optional<CaregiverProfile> findByUser(User user);

  long countBySituacaoAprovacao(CaregiverApprovalStatus status);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select profile from CaregiverProfile profile where profile.id = :id")
  Optional<CaregiverProfile> findByIdForUpdate(@Param("id") UUID id);
}
