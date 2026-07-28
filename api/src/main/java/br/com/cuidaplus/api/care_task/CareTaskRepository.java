package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_contract.CareContract;
import br.com.cuidaplus.api.service_request.ServiceRequestCareItemSnapshot;
import br.com.cuidaplus.api.user.User;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CareTaskRepository extends JpaRepository<CareTask, UUID> {
  List<CareTask> findByResponsibleCreatorOrderByUpdatedAtDesc(User responsible);
  List<CareTask> findByCaregiverExecutorOrderByUpdatedAtDesc(User caregiver);
  Optional<CareTask> findByIdAndResponsibleCreator(UUID id, User responsible);
  boolean existsBySourceSnapshotItem(ServiceRequestCareItemSnapshot snapshot);
  List<CareTask> findByContractOrderByCreatedAtAsc(CareContract contract);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select task from CareTask task where task.id = :id")
  Optional<CareTask> findForUpdateById(@Param("id") UUID id);
}
