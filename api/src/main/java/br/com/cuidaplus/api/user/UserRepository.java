package br.com.cuidaplus.api.user;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByCpf(String cpf);

  boolean existsByEmailAndIdNot(String email, UUID id);

  boolean existsByCpfAndIdNot(String cpf, UUID id);

  long countByUserTypeAndAccountStatus(UserType userType, AccountStatus accountStatus);

  long countByAccountStatus(AccountStatus accountStatus);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select user from User user where user.id = :id")
  Optional<User> findByIdForUpdate(@Param("id") UUID id);
}
