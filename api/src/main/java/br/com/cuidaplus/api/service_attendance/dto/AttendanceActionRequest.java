package br.com.cuidaplus.api.service_attendance.dto;

import jakarta.validation.constraints.*;
import java.time.*;

public record AttendanceActionRequest(
  @NotNull LocalDate attendanceDate,
  @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
  @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
  @NotNull @DecimalMin("0.0") @DecimalMax("1000.0") Double accuracy,
  @NotNull Instant locationCapturedAt,
  boolean mocked,
  @NotBlank @Size(max = 80) String deviceTimezone
) {}
