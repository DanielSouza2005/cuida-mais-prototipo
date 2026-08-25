package br.com.cuidaplus.api.attendance_report;

import br.com.cuidaplus.api.attendance_report.dto.*;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AttendanceReportController {
  private final AttendanceReportService service;
  public AttendanceReportController(AttendanceReportService service) { this.service = service; }

  @PostMapping("/caregiver/contracts/{contractId}/attendance/{date}/report/generate")
  public AttendanceReportResponse generate(@PathVariable UUID contractId,
    @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return service.generate(AuthenticatedUser.id(), contractId, date);
  }

  @GetMapping("/contracts/{contractId}/attendance/{date}/report")
  public AttendanceReportResponse get(@PathVariable UUID contractId,
    @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return service.get(AuthenticatedUser.id(), contractId, date);
  }

  @GetMapping("/attendance-reports/{reportId}")
  public AttendanceReportResponse getById(@PathVariable UUID reportId) {
    return service.getById(AuthenticatedUser.id(), reportId);
  }

  @PutMapping("/caregiver/contracts/{contractId}/attendance/{date}/report")
  public AttendanceReportResponse update(@PathVariable UUID contractId,
    @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
    @Valid @RequestBody UpdateAttendanceReportRequest request) {
    return service.update(AuthenticatedUser.id(), contractId, date, request);
  }

  @PostMapping("/caregiver/contracts/{contractId}/attendance/{date}/report/finalize")
  public AttendanceReportResponse finalizeReport(@PathVariable UUID contractId,
    @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
    @Valid @RequestBody UpdateAttendanceReportRequest request) {
    return service.finalizeReport(AuthenticatedUser.id(), contractId, date, request);
  }
}
