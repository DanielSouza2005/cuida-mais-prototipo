package br.com.cuidaplus.api.service_attendance;

import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.service_request.*;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AttendanceScheduleService {
  private static final Duration TOLERANCE = Duration.ofMinutes(30);
  private static final Set<CareContractStatus> ALLOWED_STATUSES = EnumSet.of(
    CareContractStatus.AGENDADA, CareContractStatus.ATIVA, CareContractStatus.ENCERRAMENTO_AGENDADO,
    CareContractStatus.ENCERRADA, CareContractStatus.FINALIZADA
  );

  public AttendanceSchedule requireSchedule(CareContract contract, LocalDate date) {
    if (contract == null || date == null || !ALLOWED_STATUSES.contains(contract.getStatus())) {
      throw new BusinessException("A contratação não possui atendimento disponível nessa data.", HttpStatus.CONFLICT);
    }
    if (date.isBefore(contract.getStartDate()) || upperBound(contract) != null && date.isAfter(upperBound(contract))) {
      throw new BusinessException("A contratação não possui atendimento disponível nessa data.", HttpStatus.CONFLICT);
    }

    ServiceRequest request = contract.getServiceRequest();
    if (request.getHiringType() == HiringType.PONTUAL && !request.getSpecificDates().contains(date)) {
      throw new BusinessException("A contratação não possui atendimento disponível nessa data.", HttpStatus.CONFLICT);
    }

    List<ServiceRequestScheduleDay> all = request.getScheduleDays().stream()
      .filter(item -> item.getStartTime() != null && item.getEndTime() != null)
      .sorted(Comparator.comparing(ServiceRequestScheduleDay::getStartTime))
      .toList();
    List<ServiceRequestScheduleDay> matching = all.stream()
      .filter(item -> item.getWeekday() != null && dayOfWeek(item.getWeekday()) == date.getDayOfWeek())
      .toList();
    if (matching.isEmpty() && request.getHiringType() == HiringType.PONTUAL && !all.isEmpty()) matching = List.of(all.get(0));
    if (matching.isEmpty()) {
      throw new BusinessException("A contratação não possui atendimento previsto para este dia.", HttpStatus.CONFLICT);
    }

    LocalTime start = matching.stream().map(ServiceRequestScheduleDay::getStartTime).min(LocalTime::compareTo).orElseThrow();
    LocalTime end = matching.stream().map(ServiceRequestScheduleDay::getEndTime).max(LocalTime::compareTo).orElseThrow();
    ZonedDateTime scheduledStart = ZonedDateTime.of(date, start, AttendanceTimeConfig.SERVICE_ZONE);
    ZonedDateTime scheduledEnd = ZonedDateTime.of(end.isAfter(start) ? date : date.plusDays(1), end, AttendanceTimeConfig.SERVICE_ZONE);
    return new AttendanceSchedule(
      date, start, end, scheduledStart.toInstant(), scheduledEnd.toInstant(),
      scheduledStart.minus(TOLERANCE).toInstant(), scheduledStart.plus(TOLERANCE).toInstant(),
      scheduledEnd.minus(TOLERANCE).toInstant(), scheduledEnd.plus(TOLERANCE).toInstant()
    );
  }

  public boolean hasSchedule(CareContract contract, LocalDate date) {
    try { requireSchedule(contract, date); return true; }
    catch (BusinessException exception) { return false; }
  }

  private LocalDate upperBound(CareContract contract) {
    if (contract.getEffectiveEndDate() != null) return contract.getEffectiveEndDate();
    return contract.getEndDate();
  }

  private DayOfWeek dayOfWeek(DiaSemana value) {
    return switch (value) {
      case SEGUNDA -> DayOfWeek.MONDAY; case TERCA -> DayOfWeek.TUESDAY; case QUARTA -> DayOfWeek.WEDNESDAY;
      case QUINTA -> DayOfWeek.THURSDAY; case SEXTA -> DayOfWeek.FRIDAY; case SABADO -> DayOfWeek.SATURDAY; case DOMINGO -> DayOfWeek.SUNDAY;
    };
  }
}
