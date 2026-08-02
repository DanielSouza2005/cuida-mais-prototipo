package br.com.cuidaplus.api.care_task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.cuidaplus.api.care_contract.CareContract;
import br.com.cuidaplus.api.care_contract.CareContractStatus;
import br.com.cuidaplus.api.notification.*;
import br.com.cuidaplus.api.profile.AssistedPerson;
import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.service_request.HiringType;
import br.com.cuidaplus.api.service_request.ServiceRequest;
import br.com.cuidaplus.api.service_request.ServiceRequestScheduleDay;
import br.com.cuidaplus.api.user.User;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class TaskReminderServiceTest {
  @Test
  void overdueNotificationKeepsTheExistingOccurrenceAndCalculatedStatus() {
    TaskReminderRepository reminders=mock(TaskReminderRepository.class);
    NotificationService notifications=mock(NotificationService.class);
    TaskAuditService audit=mock(TaskAuditService.class);
    TaskReminderService service=new TaskReminderService(reminders,notifications,audit);
    TaskOccurrence occurrence=mock(TaskOccurrence.class);CareTask task=mock(CareTask.class);
    CareContract contract=mock(CareContract.class);AssistedPerson assisted=mock(AssistedPerson.class);
    User caregiver=mock(User.class);User responsible=mock(User.class);
    UUID occurrenceId=UUID.randomUUID(),caregiverId=UUID.randomUUID(),responsibleId=UUID.randomUUID();
    TaskReminder reminder=new TaskReminder();reminder.setOccurrence(occurrence);reminder.setRecipient(caregiver);
    reminder.setReminderType(TaskReminderType.OVERDUE);reminder.setScheduledAt(Instant.now().minusSeconds(60));reminder.setStatus(TaskReminderStatus.SCHEDULED);
    when(reminders.findByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(eq(TaskReminderStatus.SCHEDULED),any())).thenReturn(List.of(reminder));
    when(occurrence.getId()).thenReturn(occurrenceId);when(occurrence.getTask()).thenReturn(task);when(occurrence.getContract()).thenReturn(contract);
    when(occurrence.getStatus()).thenReturn(TaskOccurrenceStatus.PENDENTE);when(occurrence.getScheduledDate()).thenReturn(LocalDate.of(2026,8,3));when(occurrence.getScheduledTime()).thenReturn(LocalTime.of(8,0));when(occurrence.getAssistedPerson()).thenReturn(assisted);
    when(contract.getStatus()).thenReturn(CareContractStatus.ATIVA);when(assisted.getNome()).thenReturn("Pessoa assistida");
    when(task.getTitle()).thenReturn("Cuidado da manhã");when(task.getCaregiverExecutor()).thenReturn(caregiver);when(task.getResponsibleCreator()).thenReturn(responsible);
    when(caregiver.getId()).thenReturn(caregiverId);when(responsible.getId()).thenReturn(responsibleId);

    service.dispatchDue();

    verify(occurrence,never()).setStatus(any());
    verify(notifications).create(eq(caregiver),eq(NotificationType.CARE_OCCURRENCE_OVERDUE),eq("Cuidado atrasado"),anyString(),eq(RelatedEntityType.CARE_OCCURRENCE),eq(occurrenceId));
  }

  @Test
  void invalidExistingReminderIsCanceledWithoutCreatingNotification() {
    TaskReminderRepository reminders=mock(TaskReminderRepository.class);
    NotificationService notifications=mock(NotificationService.class);
    TaskAuditService audit=mock(TaskAuditService.class);
    TaskReminderService service=new TaskReminderService(reminders,notifications,audit);
    TaskOccurrence occurrence=mock(TaskOccurrence.class);CareTask task=mock(CareTask.class);
    CareContract contract=new CareContract(); ServiceRequest request=new ServiceRequest();
    ServiceRequestScheduleDay schedule=new ServiceRequestScheduleDay();
    schedule.setWeekday(DiaSemana.SEGUNDA);schedule.setStartTime(LocalTime.of(14,0));schedule.setEndTime(LocalTime.of(22,0));
    request.setHiringType(HiringType.PERIODO_DETERMINADO);request.setScheduleDays(Set.of(schedule));contract.setServiceRequest(request);contract.setStatus(CareContractStatus.ATIVA);
    TaskReminder reminder=new TaskReminder();reminder.setOccurrence(occurrence);reminder.setReminderType(TaskReminderType.AT_SCHEDULED_TIME);
    reminder.setScheduledAt(Instant.now().minusSeconds(10));reminder.setStatus(TaskReminderStatus.SCHEDULED);
    when(reminders.findByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(eq(TaskReminderStatus.SCHEDULED),any())).thenReturn(List.of(reminder));
    when(occurrence.getTask()).thenReturn(task);when(occurrence.getContract()).thenReturn(contract);
    when(occurrence.getScheduledDate()).thenReturn(LocalDate.of(2026,8,3));when(occurrence.getScheduledTime()).thenReturn(LocalTime.of(8,0));

    service.dispatchDue();

    assertEquals(TaskReminderStatus.CANCELED, reminder.getStatus());
    verifyNoInteractions(notifications);
  }
}
