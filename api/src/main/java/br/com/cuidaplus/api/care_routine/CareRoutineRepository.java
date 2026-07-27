package br.com.cuidaplus.api.care_routine;

import br.com.cuidaplus.api.user.User;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareRoutineRepository extends JpaRepository<CareRoutine, UUID> {
  List<CareRoutine> findByResponsibleUserOrderByUpdatedAtDesc(User responsibleUser);
  Optional<CareRoutine> findByIdAndResponsibleUser(UUID id, User responsibleUser);
}
