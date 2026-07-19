package br.com.cuidaplus.api.care_task;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareActivityRecordRepository extends JpaRepository<CareActivityRecord, UUID> {
  Optional<CareActivityRecord> findByOccurrence(TaskOccurrence occurrence);
}
