package br.com.cuidaplus.api.service_request;

import br.com.cuidaplus.api.common.BusinessException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class ScheduleIntervalPolicy {
  public static final Duration MAX_SERVICE_DURATION = Duration.ofHours(16);

  private ScheduleIntervalPolicy() {}

  public static ResolvedInterval resolve(LocalDate startDate, LocalTime startTime, LocalTime endTime) {
    if (startDate == null || startTime == null || endTime == null) {
      throw new BusinessException("Informe a data e os horários do serviço.");
    }
    if (startTime.equals(endTime)) {
      throw new BusinessException("Os horários inicial e final não podem ser iguais.");
    }
    LocalDateTime start = LocalDateTime.of(startDate, startTime);
    LocalDateTime end = LocalDateTime.of(endTime.isAfter(startTime) ? startDate : startDate.plusDays(1), endTime);
    Duration duration = Duration.between(start, end);
    if (duration.isZero() || duration.isNegative() || duration.compareTo(MAX_SERVICE_DURATION) > 0) {
      throw new BusinessException("A duração do atendimento deve ser maior que zero e de no máximo 16 horas.");
    }
    return new ResolvedInterval(start, end, duration);
  }

  public static boolean contains(LocalDate anchorDate, LocalTime startTime, LocalTime endTime, LocalDateTime moment) {
    ResolvedInterval interval = resolve(anchorDate, startTime, endTime);
    return !moment.isBefore(interval.start()) && !moment.isAfter(interval.end());
  }

  public static boolean overlaps(ResolvedInterval first, ResolvedInterval second) {
    return first.start().isBefore(second.end()) && second.start().isBefore(first.end());
  }

  public record ResolvedInterval(LocalDateTime start, LocalDateTime end, Duration duration) {}
}
