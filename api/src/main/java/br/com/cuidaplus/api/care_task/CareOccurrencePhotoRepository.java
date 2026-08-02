package br.com.cuidaplus.api.care_task;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareOccurrencePhotoRepository extends JpaRepository<CareOccurrencePhoto, UUID> {
  List<CareOccurrencePhoto> findByOccurrenceOrderByCreatedAtAsc(TaskOccurrence occurrence);
  Optional<CareOccurrencePhoto> findByIdAndOccurrence(UUID id, TaskOccurrence occurrence);
  List<CareOccurrencePhoto> findByActivityRecordOrderByCreatedAtAsc(CareActivityRecord activityRecord);
  Optional<CareOccurrencePhoto> findByIdAndActivityRecord(UUID id, CareActivityRecord activityRecord);
}
