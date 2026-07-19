package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.user.User;
import java.time.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskOccurrenceRepository extends JpaRepository<TaskOccurrence, UUID> {
  boolean existsByTaskAndScheduledDateAndScheduledTime(CareTask task, LocalDate date, LocalTime time);
  List<TaskOccurrence> findByTaskAndScheduledDateBetweenOrderByScheduledInstantUtcAsc(CareTask task, LocalDate start, LocalDate end);
  List<TaskOccurrence> findByCaregiverAndScheduledDateBetweenOrderByScheduledInstantUtcAsc(User caregiver, LocalDate start, LocalDate end);
  Optional<TaskOccurrence> findByIdAndCaregiver(UUID id, User caregiver);
  List<TaskOccurrence> findByTaskOrderByScheduledInstantUtcDesc(CareTask task);
}
