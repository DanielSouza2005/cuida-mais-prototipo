package br.com.cuidaplus.api.care_task;

import java.util.*;
import java.time.LocalDate;
import br.com.cuidaplus.api.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareActivityRecordRepository extends JpaRepository<CareActivityRecord, UUID> {
  Optional<CareActivityRecord> findByOccurrence(TaskOccurrence occurrence);
  List<CareActivityRecord> findByCaregiverAndEntryDateAndSourceTypeOrderByOccurredAtAsc(User caregiver, LocalDate date, CareRecordSourceType sourceType);
  List<CareActivityRecord> findByResponsibleAndEntryDateAndSourceTypeOrderByOccurredAtAsc(User responsible, LocalDate date, CareRecordSourceType sourceType);
  Optional<CareActivityRecord> findByIdAndCaregiver(UUID id, User caregiver);
  Optional<CareActivityRecord> findByIdAndResponsible(UUID id, User responsible);
}
