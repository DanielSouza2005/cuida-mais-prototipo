package br.com.cuidaplus.api.care_contract;

import br.com.cuidaplus.api.user.User;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareContractRepository extends JpaRepository<CareContract, UUID> {
  boolean existsByServiceRequestId(UUID id);
  List<CareContract> findByResponsibleUserOrderByUpdatedAtDesc(User responsible);
  Optional<CareContract> findByIdAndResponsibleUser(UUID id, User responsible);
}
