package br.com.cuidaplus.api.service_attendance;

import br.com.cuidaplus.api.security.AuthenticatedUser;
import br.com.cuidaplus.api.service_attendance.dto.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ServiceAttendanceController {
  private final ServiceAttendanceService service;
  public ServiceAttendanceController(ServiceAttendanceService service) { this.service = service; }

  @PostMapping("/caregiver/contracts/{contractId}/attendance/start")
  public AttendanceSummaryResponse start(@PathVariable UUID contractId, @Valid @RequestBody AttendanceActionRequest request) {
    return service.start(AuthenticatedUser.id(), contractId, request);
  }

  @PostMapping("/caregiver/contracts/{contractId}/attendance/end")
  public AttendanceSummaryResponse end(@PathVariable UUID contractId, @Valid @RequestBody AttendanceActionRequest request) {
    return service.end(AuthenticatedUser.id(), contractId, request);
  }

  @GetMapping("/contracts/{contractId}/attendance")
  public AttendanceSummaryResponse details(@PathVariable UUID contractId,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return service.details(AuthenticatedUser.id(), contractId, date);
  }

  @GetMapping("/caregiver/today-attendance")
  public TodayAttendanceResponse today() { return service.today(AuthenticatedUser.id()); }
}
