package br.com.cuidaplus.api.attendance_report;

import br.com.cuidaplus.api.care_contract.CareContract;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceReportRepository extends JpaRepository<AttendanceReport, UUID> {
  Optional<AttendanceReport> findByContractAndAttendanceDate(CareContract contract, LocalDate attendanceDate);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths = {"responsible", "assistedPerson", "caregiver", "startRecord", "endRecord"})
  @Query("select report from AttendanceReport report where report.id = :id")
  Optional<AttendanceReport> findForEmailDeliveryById(@Param("id") UUID id);

  @Query("""
    select report.id from AttendanceReport report
    where report.status = :reportStatus
      and ((report.emailStatus = :pendingStatus and report.emailRequestedAt <= :staleBefore)
        or (report.emailStatus = :failedStatus and report.emailNextRetryAt <= :now))
    order by report.emailRequestedAt asc
    """)
  List<UUID> findEmailDeliveryCandidates(
    @Param("reportStatus") AttendanceReportStatus reportStatus,
    @Param("pendingStatus") AttendanceReportEmailStatus pendingStatus,
    @Param("failedStatus") AttendanceReportEmailStatus failedStatus,
    @Param("staleBefore") Instant staleBefore,
    @Param("now") Instant now,
    Pageable pageable);
}
