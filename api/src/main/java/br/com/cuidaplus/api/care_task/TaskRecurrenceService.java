package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.profile.DiaSemana;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class TaskRecurrenceService {
  public static final int GENERATION_WINDOW_DAYS = 60;
  public static final int MAX_QUERY_DAYS = 90;
  private final TaskOccurrenceRepository occurrences;
  private final TaskDateTimeService dateTimes;

  public TaskRecurrenceService(TaskOccurrenceRepository occurrences, TaskDateTimeService dateTimes) {
    this.occurrences = occurrences; this.dateTimes = dateTimes;
  }

  public void generateInitialWindow(CareTask task) {
    LocalDate end = task.getStartDate().plusDays(GENERATION_WINDOW_DAYS - 1L);
    generate(task, task.getStartDate(), end);
  }

  public void generate(CareTask task, LocalDate requestedStart, LocalDate requestedEnd) {
    validateRange(requestedStart, requestedEnd);
    if (task.getStatus() != TaskSeriesStatus.ATIVA || !contractCanGenerate(task.getContract())) return;

    LocalDate start = later(requestedStart, task.getStartDate());
    LocalDate end = earlier(requestedEnd, task.getEndDate());
    end = earlier(end, task.getContract().getEndDate());
    if (task.getContract().getStatus() == CareContractStatus.ENCERRAMENTO_AGENDADO) {
      end = earlier(end, task.getContract().getEffectiveEndDate());
    }
    if (start == null || end == null || end.isBefore(start)) return;

    List<TaskOccurrence> created = new ArrayList<>();
    for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
      if (!matches(task, date) || occurrences.existsByTaskAndScheduledDateAndScheduledTime(task, date, task.getScheduledTime())) continue;
      TaskOccurrence occurrence = new TaskOccurrence();
      occurrence.setTask(task); occurrence.setContract(task.getContract()); occurrence.setAssistedPerson(task.getAssistedPerson());
      occurrence.setCaregiver(task.getCaregiverExecutor()); occurrence.setScheduledDate(date); occurrence.setScheduledTime(task.getScheduledTime());
      occurrence.setTimezone(task.getTimezone()); occurrence.setScheduledInstantUtc(dateTimes.toInstant(date, task.getScheduledTime(), task.getTimezone()));
      occurrence.setStatus(TaskOccurrenceStatus.PENDENTE);
      created.add(occurrence);
    }
    if (!created.isEmpty()) occurrences.saveAll(created);
  }

  public TaskOccurrenceStatus effectiveStatus(TaskOccurrence occurrence) {
    if (occurrence.getStatus() == TaskOccurrenceStatus.PENDENTE && occurrence.getScheduledInstantUtc().isBefore(Instant.now())) {
      return TaskOccurrenceStatus.ATRASADA;
    }
    return occurrence.getStatus();
  }

  public void validateRange(LocalDate start, LocalDate end) {
    if (start == null || end == null || end.isBefore(start)) throw new BusinessException("Período de ocorrências inválido.");
    if (ChronoUnit.DAYS.between(start, end) + 1 > MAX_QUERY_DAYS) throw new BusinessException("Consulte no máximo 90 dias por vez.");
  }

  private boolean matches(CareTask task, LocalDate date) {
    return switch (task.getRecurrenceType()) {
      case UNICA -> date.equals(task.getStartDate());
      case DIARIA, PERIODO_DETERMINADO, SEM_DATA_FINAL -> true;
      case DIAS_ESPECIFICOS -> task.getWeekdays().contains(toWeekday(date.getDayOfWeek()));
      case INTERVALO -> ChronoUnit.DAYS.between(task.getStartDate(), date) % task.getIntervalDays() == 0;
    };
  }

  private boolean contractCanGenerate(CareContract contract) {
    return contract.getStatus() == CareContractStatus.ATIVA || contract.getStatus() == CareContractStatus.ENCERRAMENTO_AGENDADO;
  }

  private DiaSemana toWeekday(DayOfWeek value) {
    return switch (value) {
      case MONDAY -> DiaSemana.SEGUNDA; case TUESDAY -> DiaSemana.TERCA; case WEDNESDAY -> DiaSemana.QUARTA;
      case THURSDAY -> DiaSemana.QUINTA; case FRIDAY -> DiaSemana.SEXTA; case SATURDAY -> DiaSemana.SABADO; case SUNDAY -> DiaSemana.DOMINGO;
    };
  }

  private LocalDate later(LocalDate first, LocalDate second) {
    if (first == null) return second; if (second == null) return first; return first.isAfter(second) ? first : second;
  }
  private LocalDate earlier(LocalDate first, LocalDate second) {
    if (first == null) return second; if (second == null) return first; return first.isBefore(second) ? first : second;
  }
}
