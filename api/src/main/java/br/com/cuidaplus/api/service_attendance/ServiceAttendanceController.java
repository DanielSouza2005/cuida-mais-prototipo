package br.com.cuidaplus.api.service_attendance;
import br.com.cuidaplus.api.attendance_report.AttendanceReportService;

import br.com.cuidaplus.api.security.AuthenticatedUser;
import br.com.cuidaplus.api.service_attendance.dto.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class ServiceAttendanceController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAttendanceController.class);
  private final ServiceAttendanceService service;
  private final AttendanceReportService reports;
  public ServiceAttendanceController(ServiceAttendanceService service, AttendanceReportService reports) {
    this.service = service;
    this.reports = reports;
  }

  @PostMapping("/caregiver/contracts/{contractId}/attendance/start")
  public AttendanceSummaryResponse start(@PathVariable UUID contractId, @Valid @RequestBody AttendanceActionRequest request) {
    return service.start(AuthenticatedUser.id(), contractId, request);
  }

  @PostMapping("/caregiver/contracts/{contractId}/attendance/end")
  public AttendanceSummaryResponse end(@PathVariable UUID contractId, @Valid @RequestBody AttendanceActionRequest request) {
    AttendanceSummaryResponse response = service.end(AuthenticatedUser.id(), contractId, request);
    try {
      reports.generate(AuthenticatedUser.id(), contractId, request.attendanceDate());
    } catch (RuntimeException exception) {
      LOGGER.warn("Atendimento encerrado, mas o rascunho do relatório não pôde ser gerado agora. contractId={}", contractId);
    }
    return response;
  }

  @GetMapping("/contracts/{contractId}/attendance")
  public AttendanceSummaryResponse details(@PathVariable UUID contractId,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return service.details(AuthenticatedUser.id(), contractId, date);
  }

  @GetMapping("/caregiver/today-attendance")
  public TodayAttendanceResponse today() { return service.today(AuthenticatedUser.id()); }
}
