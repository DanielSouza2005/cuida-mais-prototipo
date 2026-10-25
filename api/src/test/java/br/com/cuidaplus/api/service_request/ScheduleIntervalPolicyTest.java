package br.com.cuidaplus.api.service_request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.cuidaplus.api.common.BusinessException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ScheduleIntervalPolicyTest {
  private static final LocalDate DATE = LocalDate.of(2026, 12, 31);

  @Test void resolvesSameDayAndOvernightIntervals() {
    assertEquals(Duration.ofHours(9), ScheduleIntervalPolicy.resolve(DATE, LocalTime.of(8, 0), LocalTime.of(17, 0)).duration());
    var overnight = ScheduleIntervalPolicy.resolve(DATE, LocalTime.of(20, 0), LocalTime.of(8, 0));
    assertEquals(Duration.ofHours(12), overnight.duration());
    assertEquals(LocalDate.of(2027, 1, 1), overnight.end().toLocalDate());
    assertEquals(Duration.ofHours(1), ScheduleIntervalPolicy.resolve(DATE, LocalTime.of(23, 30), LocalTime.of(0, 30)).duration());
  }

  @Test void rejectsEqualTimesAndImplicitTwentyThreeHourService() {
    assertThrows(BusinessException.class, () -> ScheduleIntervalPolicy.resolve(DATE, LocalTime.of(8, 0), LocalTime.of(8, 0)));
    assertThrows(BusinessException.class, () -> ScheduleIntervalPolicy.resolve(DATE, LocalTime.of(20, 0), LocalTime.of(19, 0)));
  }

  @Test void detectsOverlapAcrossMidnightAndAdjacentWeekday() {
    var sundayNight = ScheduleIntervalPolicy.resolve(LocalDate.of(2026, 8, 30), LocalTime.of(20, 0), LocalTime.of(8, 0));
    var mondayMorning = ScheduleIntervalPolicy.resolve(LocalDate.of(2026, 8, 31), LocalTime.of(7, 0), LocalTime.of(9, 0));
    assertEquals(true, ScheduleIntervalPolicy.overlaps(sundayNight, mondayMorning));
  }
}
