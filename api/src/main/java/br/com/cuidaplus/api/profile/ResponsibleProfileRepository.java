package br.com.cuidaplus.api.profile;

import br.com.cuidaplus.api.user.User;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResponsibleProfileRepository extends JpaRepository<ResponsibleProfile, UUID> {
  Optional<ResponsibleProfile> findByUser(User user);

  long countBySituacaoAprovacao(ResponsibleApprovalStatus status);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select profile from ResponsibleProfile profile where profile.id = :id")
  Optional<ResponsibleProfile> findByIdForUpdate(@Param("id") UUID id);
}
