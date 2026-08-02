package br.com.cuidaplus.api.care_task;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.cuidaplus.api.care_contract.CareContract;
import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.service_request.HiringType;
import br.com.cuidaplus.api.service_request.ServiceRequest;
import br.com.cuidaplus.api.service_request.ServiceRequestScheduleDay;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ContractCareSchedulePolicyTest {
  private static final LocalDate MONDAY = LocalDate.of(2026, 8, 3);

  @Test
  void recurringContractAllowsOnlyCareTimesInsideInclusiveDailyInterval() {
    CareContract contract = contract(HiringType.PERIODO_INDETERMINADO, Set.of());

    assertFalse(ContractCareSchedulePolicy.allows(contract, MONDAY, LocalTime.of(8, 0)));
    assertTrue(ContractCareSchedulePolicy.allows(contract, MONDAY, LocalTime.of(14, 0)));
    assertTrue(ContractCareSchedulePolicy.allows(contract, MONDAY, LocalTime.of(15, 0)));
    assertTrue(ContractCareSchedulePolicy.allows(contract, MONDAY, LocalTime.of(22, 0)));
    assertFalse(ContractCareSchedulePolicy.allows(contract, MONDAY, LocalTime.of(23, 0)));
    assertFalse(ContractCareSchedulePolicy.allows(contract, MONDAY.plusDays(1), LocalTime.of(15, 0)));
  }

  @Test
  void determinedContractUsesTheSameDailyScheduleRule() {
    CareContract contract = contract(HiringType.PERIODO_DETERMINADO, Set.of());

    assertTrue(ContractCareSchedulePolicy.allows(contract, MONDAY, LocalTime.of(15, 0)));
    assertFalse(ContractCareSchedulePolicy.allows(contract, MONDAY, LocalTime.of(23, 0)));
  }

  @Test
  void oneOffContractAlsoRequiresTheExactServiceDate() {
    CareContract contract = contract(HiringType.PONTUAL, Set.of(MONDAY));

    assertTrue(ContractCareSchedulePolicy.allows(contract, MONDAY, LocalTime.of(15, 0)));
    assertFalse(ContractCareSchedulePolicy.allows(contract, MONDAY.plusWeeks(1), LocalTime.of(15, 0)));
    assertFalse(ContractCareSchedulePolicy.allows(contract, MONDAY, LocalTime.of(23, 0)));
  }

  private CareContract contract(HiringType hiringType, Set<LocalDate> dates) {
    ServiceRequestScheduleDay schedule = new ServiceRequestScheduleDay();
    schedule.setWeekday(DiaSemana.SEGUNDA);
    schedule.setStartTime(LocalTime.of(14, 0));
    schedule.setEndTime(LocalTime.of(22, 0));
    ServiceRequest request = new ServiceRequest();
    request.setHiringType(hiringType);
    request.setSpecificDates(new LinkedHashSet<>(dates));
    request.setScheduleDays(new LinkedHashSet<>(Set.of(schedule)));
    CareContract contract = new CareContract();
    contract.setServiceRequest(request);
    return contract;
  }
}
