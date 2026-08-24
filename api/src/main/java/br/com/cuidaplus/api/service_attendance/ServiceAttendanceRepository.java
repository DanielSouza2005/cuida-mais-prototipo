package br.com.cuidaplus.api.service_attendance;

import br.com.cuidaplus.api.care_contract.CareContract;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceAttendanceRepository extends JpaRepository<ServiceAttendanceRecord, UUID> {
  List<ServiceAttendanceRecord> findByContractAndAttendanceDateOrderByRecordedAtAsc(CareContract contract, LocalDate attendanceDate);
  Optional<ServiceAttendanceRecord> findByContractAndAttendanceDateAndRecordType(CareContract contract, LocalDate attendanceDate, AttendanceRecordType recordType);
  boolean existsByContractAndAttendanceDateAndRecordType(CareContract contract, LocalDate attendanceDate, AttendanceRecordType recordType);
}
