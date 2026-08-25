package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.care_contract.CareContract;
import java.time.*;
import java.util.*;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskOccurrenceRepository extends JpaRepository<TaskOccurrence, UUID> {
  boolean existsByTaskAndScheduledDateAndScheduledTime(CareTask task, LocalDate date, LocalTime time);
  Optional<TaskOccurrence> findByContractAndTaskAndScheduledDateAndScheduledTime(
    br.com.cuidaplus.api.care_contract.CareContract contract, CareTask task, LocalDate date, LocalTime time);
  @Modifying(flushAutomatically = true)
  @Query(value = """
    INSERT INTO task_occurrences (
      id, task_id, contract_id, assisted_person_id, caregiver_user_id,
      scheduled_date, scheduled_time, scheduled_instant_utc, timezone, status,
      exception, auto_marked_not_done, created_at, updated_at, version
    ) VALUES (
      :id, :taskId, :contractId, :assistedPersonId, :caregiverId,
      :scheduledDate, :scheduledTime, :scheduledInstantUtc, :timezone, 'PENDENTE',
      FALSE, FALSE, :createdAt, :createdAt, 0
    )
    ON CONFLICT (task_id, scheduled_date, scheduled_time) DO NOTHING
    """, nativeQuery = true)
  int insertIfAbsent(@Param("id") UUID id, @Param("taskId") UUID taskId,
    @Param("contractId") UUID contractId, @Param("assistedPersonId") UUID assistedPersonId,
    @Param("caregiverId") UUID caregiverId, @Param("scheduledDate") LocalDate scheduledDate,
    @Param("scheduledTime") LocalTime scheduledTime, @Param("scheduledInstantUtc") Instant scheduledInstantUtc,
    @Param("timezone") String timezone, @Param("createdAt") Instant createdAt);
  List<TaskOccurrence> findByTaskAndScheduledDateBetweenOrderByScheduledInstantUtcAsc(CareTask task, LocalDate start, LocalDate end);
  List<TaskOccurrence> findByCaregiverAndScheduledDateBetweenOrderByScheduledInstantUtcAsc(User caregiver, LocalDate start, LocalDate end);
  List<TaskOccurrence> findByContractAndScheduledDateOrderByScheduledInstantUtcAsc(CareContract contract, LocalDate date);
  Optional<TaskOccurrence> findByIdAndCaregiver(UUID id, User caregiver);
  List<TaskOccurrence> findByTaskOrderByScheduledInstantUtcDesc(CareTask task);
  List<TaskOccurrence> findByStatusIn(Collection<TaskOccurrenceStatus> statuses);
  @Query("select occurrence from TaskOccurrence occurrence where occurrence.task.responsibleCreator = :responsible and occurrence.scheduledDate between :start and :end order by occurrence.scheduledInstantUtc")
  List<TaskOccurrence> findByResponsibleAndScheduledDateBetween(@Param("responsible") User responsible, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
