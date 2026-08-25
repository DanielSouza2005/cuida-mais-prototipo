package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_contract.CareContract;
import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.service_request.HiringType;
import br.com.cuidaplus.api.service_request.ServiceRequest;
import br.com.cuidaplus.api.service_request.ServiceRequestScheduleDay;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public final class ContractCareSchedulePolicy {
  private ContractCareSchedulePolicy() {}

  public static boolean allows(CareContract contract, LocalDate date, LocalTime careTime) {
    if (contract == null || date == null || careTime == null) return false;
    ServiceRequest request = contract.getServiceRequest();
    if (request == null) return true;
    if (request.getHiringType() == HiringType.PONTUAL && !request.getSpecificDates().contains(date)) return false;

    DiaSemana weekday = toWeekday(date.getDayOfWeek());
    return request.getScheduleDays().stream()
      .filter(day -> day.getWeekday() == weekday)
      .anyMatch(day -> contains(day, careTime));
  }

  public static boolean contains(ServiceRequestScheduleDay schedule, LocalTime careTime) {
    LocalTime start = schedule.getStartTime();
    LocalTime end = schedule.getEndTime();
    return contains(start, end, careTime);
  }

  public static boolean contains(LocalTime start, LocalTime end, LocalTime careTime) {
    if (start == null || end == null || careTime == null) return false;
    if (!end.isBefore(start)) return !careTime.isBefore(start) && !careTime.isAfter(end);
    return !careTime.isBefore(start) || !careTime.isAfter(end);
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
