package br.com.cuidaplus.api.care_contract;

import br.com.cuidaplus.api.user.User;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CareContractRepository extends JpaRepository<CareContract, UUID> {
  boolean existsByServiceRequestId(UUID id);
  List<CareContract> findByResponsibleUserOrderByUpdatedAtDesc(User responsible);
  List<CareContract> findByCaregiverUserOrderByUpdatedAtDesc(User caregiver);
  Optional<CareContract> findByIdAndResponsibleUser(UUID id, User responsible);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select contract from CareContract contract where contract.id = :id")
  Optional<CareContract> findForUpdateById(@Param("id") UUID id);
}
