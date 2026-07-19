package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.common.BusinessException;
import java.time.*;
import org.springframework.stereotype.Service;

@Service
public class TaskDateTimeService {
  public ZoneId requireZone(String timezone) {
    try {
      return ZoneId.of(timezone);
    } catch (DateTimeException | NullPointerException exception) {
      throw new BusinessException("Informe um fuso horário válido, como America/Sao_Paulo.");
    }
  }

  public Instant toInstant(LocalDate date, LocalTime time, String timezone) {
    return ZonedDateTime.of(date, time, requireZone(timezone)).toInstant();
  }

  public LocalDate today(String timezone) {
    return LocalDate.now(requireZone(timezone));
  }
}
