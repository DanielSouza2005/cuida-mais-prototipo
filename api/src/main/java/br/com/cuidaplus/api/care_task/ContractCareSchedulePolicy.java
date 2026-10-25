package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_contract.CareContract;
import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.service_request.HiringType;
import br.com.cuidaplus.api.service_request.ServiceRequest;
import br.com.cuidaplus.api.service_request.ServiceRequestScheduleDay;
import br.com.cuidaplus.api.service_request.ScheduleIntervalPolicy;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class ContractCareSchedulePolicy {
  private ContractCareSchedulePolicy() {}

  public static boolean allows(CareContract contract, LocalDate date, LocalTime careTime) {
    if (contract == null || date == null || careTime == null) return false;
    ServiceRequest request = contract.getServiceRequest();
    if (request == null) return true;
    LocalDateTime moment = LocalDateTime.of(date, careTime);
    return request.getScheduleDays().stream().anyMatch(day -> {
      for (LocalDate anchor : new LocalDate[] { date, date.minusDays(1) }) {
        if (day.getWeekday() != toWeekday(anchor.getDayOfWeek())) continue;
        if (request.getHiringType() == HiringType.PONTUAL && !request.getSpecificDates().contains(anchor)) continue;
        if (contract.getStartDate() != null && anchor.isBefore(contract.getStartDate())) continue;
        LocalDate upper = contract.getEffectiveEndDate() != null ? contract.getEffectiveEndDate() : contract.getEndDate();
        if (upper != null && anchor.isAfter(upper)) continue;
        if (ScheduleIntervalPolicy.contains(anchor, day.getStartTime(), day.getEndTime(), moment)) return true;
      }
      return false;
    });
  }

  public static boolean contains(ServiceRequestScheduleDay schedule, LocalTime careTime) {
    LocalTime start = schedule.getStartTime();
    LocalTime end = schedule.getEndTime();
    return contains(start, end, careTime);
  }

  public static boolean hasOvernightSchedule(CareContract contract) {
    return contract != null && contract.getServiceRequest() != null
      && contract.getServiceRequest().getScheduleDays().stream()
        .anyMatch(day -> day.getStartTime() != null && day.getEndTime() != null
          && day.getEndTime().isBefore(day.getStartTime()));
  }

  public static boolean contains(LocalTime start, LocalTime end, LocalTime careTime) {
    if (start == null || end == null || careTime == null) return false;
    LocalDate reference = LocalDate.of(2000, 1, 1);
    LocalDate date = end.isBefore(start) && careTime.isBefore(start) ? reference.plusDays(1) : reference;
    return ScheduleIntervalPolicy.contains(reference, start, end, LocalDateTime.of(date, careTime));
  }

  private static DiaSemana toWeekday(DayOfWeek value) {
    return switch (value) {
      case MONDAY -> DiaSemana.SEGUNDA;
      case TUESDAY -> DiaSemana.TERCA;
      case WEDNESDAY -> DiaSemana.QUARTA;
      case THURSDAY -> DiaSemana.QUINTA;
      case FRIDAY -> DiaSemana.SEXTA;
      case SATURDAY -> DiaSemana.SABADO;
      case SUNDAY -> DiaSemana.DOMINGO;
    };
  }
}
