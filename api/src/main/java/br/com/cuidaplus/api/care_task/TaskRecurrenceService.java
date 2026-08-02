package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.profile.DiaSemana;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskRecurrenceService {
  public static final int GENERATION_WINDOW_DAYS = 60;
  public static final int MAX_QUERY_DAYS = 90;
  private final TaskOccurrenceRepository occurrences;
  private final TaskDateTimeService dateTimes;
  private final TaskReminderService reminders;
  private final CareTaskRepository tasks;

  public TaskRecurrenceService(TaskOccurrenceRepository occurrences, TaskDateTimeService dateTimes, TaskReminderService reminders, CareTaskRepository tasks) {
    this.occurrences = occurrences; this.dateTimes = dateTimes; this.reminders = reminders; this.tasks = tasks;
  }

  public void generateInitialWindow(CareTask task) {
    LocalDate end = task.getStartDate().plusDays(GENERATION_WINDOW_DAYS - 1L);
    generate(task, task.getStartDate(), end);
  }

  @Transactional
  public void generate(CareTask task, LocalDate requestedStart, LocalDate requestedEnd) {
    validateRange(requestedStart, requestedEnd);
    if (task.getId() != null) task = tasks.findForUpdateById(task.getId()).orElse(task);
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
      if (!matchesContract(task, date) || !matches(task, date) || !ContractCareSchedulePolicy.allows(task.getContract(), date, task.getScheduledTime())) continue;
      OccurrenceCreationResult result = getOrCreateCareOccurrence(task, date);
      if (result.created()) created.add(result.occurrence());
    }
    if (!created.isEmpty()) reminders.scheduleFor(created);
  }

  OccurrenceCreationResult getOrCreateCareOccurrence(CareTask task, LocalDate date) {
    LocalTime time = task.getScheduledTime();
    Optional<TaskOccurrence> existing = occurrences.findByContractAndTaskAndScheduledDateAndScheduledTime(task.getContract(), task, date, time);
    if (existing.isPresent()) return new OccurrenceCreationResult(existing.get(), false);

    UUID id = UUID.randomUUID();
    Instant scheduledInstant = dateTimes.toInstant(date, time, task.getTimezone());
    int inserted = occurrences.insertIfAbsent(id, task.getId(), task.getContract().getId(), task.getAssistedPerson().getId(),
      task.getCaregiverExecutor().getId(), date, time, scheduledInstant, task.getTimezone(), Instant.now());
    TaskOccurrence occurrence = inserted == 1
      ? occurrences.findById(id).orElseThrow(() -> new BusinessException("Não foi possível criar a ocorrência do cuidado."))
      : occurrences.findByContractAndTaskAndScheduledDateAndScheduledTime(task.getContract(), task, date, time)
        .orElseThrow(() -> new BusinessException("Não foi possível localizar a ocorrência do cuidado."));
    return new OccurrenceCreationResult(occurrence, inserted == 1);
  }

  record OccurrenceCreationResult(TaskOccurrence occurrence, boolean created) {}

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
    return contract.getStatus() == CareContractStatus.AGENDADA || contract.getStatus() == CareContractStatus.ATIVA || contract.getStatus() == CareContractStatus.ENCERRAMENTO_AGENDADO;
  }

  private boolean matchesContract(CareTask task, LocalDate date) {
    var request = task.getContract().getServiceRequest();
    if (request == null) return true;
    if (request.getHiringType() == br.com.cuidaplus.api.service_request.HiringType.PONTUAL) return request.getSpecificDates().contains(date);
    return request.getScheduleDays().stream().anyMatch(day -> day.getWeekday() == toWeekday(date.getDayOfWeek()));
  }

  boolean isWithinContractSchedule(TaskOccurrence occurrence) {
    return ContractCareSchedulePolicy.allows(
      occurrence.getContract(), occurrence.getScheduledDate(), occurrence.getScheduledTime()
    );
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
