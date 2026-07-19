package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.user.User;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareTaskRepository extends JpaRepository<CareTask, UUID> {
  List<CareTask> findByResponsibleCreatorOrderByUpdatedAtDesc(User responsible);
  List<CareTask> findByCaregiverExecutorOrderByUpdatedAtDesc(User caregiver);
  Optional<CareTask> findByIdAndResponsibleCreator(UUID id, User responsible);
}
