package br.com.cuidaplus.api.care_task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class TaskOccurrenceExpirationServiceTest {
  @Test
  void expiredPendingCareIsMarkedNotDoneWithoutReason() {
    TaskOccurrenceRepository occurrences=mock(TaskOccurrenceRepository.class);TaskDateTimeService dates=mock(TaskDateTimeService.class);
    TaskReminderService reminders=mock(TaskReminderService.class);TaskAuditService audit=mock(TaskAuditService.class);
    TaskOccurrence occurrence=mock(TaskOccurrence.class);CareTask task=mock(CareTask.class);
    when(occurrences.findByStatusIn(any())).thenReturn(List.of(occurrence));when(occurrence.getScheduledDate()).thenReturn(LocalDate.of(2026,7,26));when(occurrence.getTimezone()).thenReturn("America/Sao_Paulo");when(dates.today("America/Sao_Paulo")).thenReturn(LocalDate.of(2026,7,27));when(occurrence.getTask()).thenReturn(task);
    int changed=new TaskOccurrenceExpirationService(occurrences,dates,reminders,audit).processExpiredPendingCareOccurrences();
    assertEquals(1,changed);verify(occurrence).setStatus(TaskOccurrenceStatus.NAO_REALIZADA);verify(occurrence).setExecutedBy(null);verify(occurrence).setNonCompletionReason(null);verify(occurrence).setAutoMarkedNotDone(true);verify(reminders).cancelFuture(occurrence);verify(reminders).notifyResponsibleNotDone(occurrence);verify(audit).record(task,occurrence,null,TaskAuditAction.OCORRENCIA_NAO_REALIZADA_AUTOMATICAMENTE,"Não realizado automaticamente, pois o dia previsto já passou.");
  }
}
