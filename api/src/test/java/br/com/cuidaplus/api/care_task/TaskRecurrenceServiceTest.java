package br.com.cuidaplus.api.care_task;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.profile.DiaSemana;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskRecurrenceServiceTest {
  @Mock TaskOccurrenceRepository occurrences;
  @Mock TaskDateTimeService dateTimes;
  @Captor ArgumentCaptor<List<TaskOccurrence>> createdCaptor;
  TaskRecurrenceService service;
  CareTask task;
  CareContract contract;

  @BeforeEach
  void setUp() {
    service = new TaskRecurrenceService(occurrences, dateTimes);
    task = new CareTask(); contract = new CareContract();
    contract.setStatus(CareContractStatus.ATIVA); contract.setStartDate(LocalDate.of(2026, 7, 1)); contract.setEndDate(LocalDate.of(2026, 12, 31));
    task.setContract(contract); task.setStatus(TaskSeriesStatus.ATIVA); task.setStartDate(LocalDate.of(2026, 7, 1));
    task.setScheduledTime(LocalTime.of(14, 0)); task.setTimezone("America/Sao_Paulo"); task.setWeekdays(new LinkedHashSet<>());
    lenient().when(dateTimes.toInstant(any(), any(), anyString())).thenAnswer(invocation ->
      ZonedDateTime.of(invocation.getArgument(0), invocation.getArgument(1), ZoneId.of(invocation.getArgument(2))).toInstant());
  }

  @Test
  void uniqueTaskCreatesExactlyOneOccurrence() {
    task.setRecurrenceType(TaskRecurrenceType.UNICA);
    service.generate(task, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 30));
    assertEquals(List.of(LocalDate.of(2026, 7, 1)), createdDates());
  }

  @Test
  void dailyAndNoEndDateRemainBoundedByRequestedWindow() {
    task.setRecurrenceType(TaskRecurrenceType.SEM_DATA_FINAL);
    service.generate(task, LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 22));
    assertEquals(List.of(LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 21), LocalDate.of(2026, 7, 22)), createdDates());
  }

  @Test
  void specificWeekdaysGenerateOnlySelectedDays() {
    task.setRecurrenceType(TaskRecurrenceType.DIAS_ESPECIFICOS);
    task.setWeekdays(new LinkedHashSet<>(List.of(DiaSemana.SEGUNDA, DiaSemana.QUARTA, DiaSemana.SEXTA)));
    service.generate(task, LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 26));
    assertEquals(List.of(LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 22), LocalDate.of(2026, 7, 24)), createdDates());
  }

  @Test
  void intervalUsesDaysElapsedFromSeriesStart() {
    task.setRecurrenceType(TaskRecurrenceType.INTERVALO); task.setIntervalDays(2);
    service.generate(task, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 6));
    assertEquals(List.of(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 5)), createdDates());
  }

  @Test
  void determinedPeriodAndScheduledTerminationCapGeneration() {
    task.setRecurrenceType(TaskRecurrenceType.PERIODO_DETERMINADO); task.setEndDate(LocalDate.of(2026, 7, 10));
    contract.setStatus(CareContractStatus.ENCERRAMENTO_AGENDADO); contract.setEffectiveEndDate(LocalDate.of(2026, 7, 8));
    service.generate(task, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
    assertEquals(LocalDate.of(2026, 7, 8), createdDates().getLast());
  }

  @Test
  void duplicateScheduleIsNotInsertedAgain() {
    task.setRecurrenceType(TaskRecurrenceType.UNICA);
    when(occurrences.existsByTaskAndScheduledDateAndScheduledTime(task, task.getStartDate(), task.getScheduledTime())).thenReturn(true);
    service.generate(task, task.getStartDate(), task.getStartDate());
    verify(occurrences, never()).saveAll(any());
  }

  @Test
  void pausedOrClosedContractsDoNotGenerate() {
    task.setRecurrenceType(TaskRecurrenceType.DIARIA); task.setStatus(TaskSeriesStatus.PAUSADA);
    service.generate(task, task.getStartDate(), task.getStartDate().plusDays(2));
    task.setStatus(TaskSeriesStatus.ATIVA); contract.setStatus(CareContractStatus.ENCERRADA);
    service.generate(task, task.getStartDate(), task.getStartDate().plusDays(2));
    verify(occurrences, never()).saveAll(any());
  }

  @Test
  void pendingPastOccurrenceIsEffectivelyLateWithoutDatabaseMutation() {
    TaskOccurrence occurrence = new TaskOccurrence(); occurrence.setStatus(TaskOccurrenceStatus.PENDENTE); occurrence.setScheduledInstantUtc(Instant.now().minusSeconds(60));
    assertEquals(TaskOccurrenceStatus.ATRASADA, service.effectiveStatus(occurrence));
    assertEquals(TaskOccurrenceStatus.PENDENTE, occurrence.getStatus());
  }

  private List<LocalDate> createdDates() {
    verify(occurrences).saveAll(createdCaptor.capture());
    return createdCaptor.getValue().stream().map(TaskOccurrence::getScheduledDate).toList();
  }
}
