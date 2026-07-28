package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_task.dto.*;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class TaskResponseMapper {
  private final TaskOccurrenceRepository occurrences;
  private final CareActivityRecordRepository activities;
  private final TaskAuditEntryRepository audit;
  private final TaskRecurrenceService recurrence;
  private final CareOccurrencePhotoRepository photos;

  public TaskResponseMapper(TaskOccurrenceRepository occurrences, CareActivityRecordRepository activities, TaskAuditEntryRepository audit, TaskRecurrenceService recurrence, CareOccurrencePhotoRepository photos) {
    this.occurrences = occurrences; this.activities = activities; this.audit = audit; this.recurrence = recurrence; this.photos = photos;
  }

  public CareTaskSummaryResponse summary(CareTask task) {
    TaskOccurrence next = occurrences.findByTaskOrderByScheduledInstantUtcDesc(task).stream()
      .filter(item -> item.getStatus() == TaskOccurrenceStatus.PENDENTE && !item.getScheduledInstantUtc().isBefore(Instant.now()))
      .min(Comparator.comparing(TaskOccurrence::getScheduledInstantUtc)).orElse(null);
    return new CareTaskSummaryResponse(
      task.getId(), task.getTitle(), task.getDescription(), task.getCategory(), task.getCustomCategory(), task.getPriority(), task.getRecurrenceType(),
      task.getStartDate(), task.getEndDate(), task.getScheduledTime(), task.getIntervalDays(), new LinkedHashSet<>(task.getWeekdays()), task.getTimezone(),
      task.isReminderEnabled(), task.getReminderMinutesBefore(), task.getNotes(), task.getStatus(), task.getAssistedPerson().getId(), task.getAssistedPerson().getNome(),
      task.getContract().getId(), task.getCaregiverExecutor().getId(), task.getCaregiverExecutor().getFullName(),
      next == null ? null : next.getScheduledDate(), next == null ? null : next.getScheduledTime(), task.getVersion(), task.getCreatedAt(), task.getUpdatedAt()
    );
  }

  public CareTaskDetailsResponse details(CareTask task) {
    List<TaskAuditResponse> auditResponses = audit.findByTaskOrderByCreatedAtDesc(task).stream().limit(30)
      .map(entry -> new TaskAuditResponse(entry.getId(), entry.getAction(), entry.getActor() == null ? "Sistema" : entry.getActor().getFullName(), entry.getDetails(), entry.getCreatedAt())).toList();
    return new CareTaskDetailsResponse(summary(task), medication(task), auditResponses);
  }

  public TaskOccurrenceResponse occurrence(TaskOccurrence occurrence) {
    CareTask task = occurrence.getTask();
    UUID activityId = activities.findByOccurrence(occurrence).map(CareActivityRecord::getId).orElse(null);
    return new TaskOccurrenceResponse(
      occurrence.getId(), task.getId(), task.getTitle(), task.getDescription(), task.getCategory(), task.getCustomCategory(), task.getPriority(),
      occurrence.getScheduledDate(), occurrence.getScheduledTime(), occurrence.getScheduledInstantUtc(), occurrence.getTimezone(), recurrence.effectiveStatus(occurrence),
      occurrence.getAssistedPerson().getId(), occurrence.getAssistedPerson().getNome(), occurrence.getContract().getId(), occurrence.getCaregiver().getId(),
      occurrence.getCaregiver().getFullName(), task.getResponsibleCreator().getId(), task.getResponsibleCreator().getFullName(), medication(task), task.getNotes(),
      task.isImportant(), task.isReminderEnabled(), task.getReminderMinutesBefore(), task.isReminderAtScheduledTime(), task.isOverdueReminderEnabled(), task.getOverdueAfterMinutes(), task.isRepeatWhilePending(), task.getRepeatIntervalMinutes(),
      hiringLabel(occurrence.getContract().getServiceRequest().getHiringType()), occurrence.getContract().getStartDate(), occurrence.getContract().getEndDate(), address(occurrence), dependencyLabel(occurrence), mobilityLabel(occurrence),
      occurrence.getCompletedAt(), occurrence.getExecutedBy() == null ? null : occurrence.getExecutedBy().getFullName(), occurrence.getNonCompletionReason(),
      occurrence.getExecutionNote(), activityId, occurrence.isException(), task.isRequiresCompletionPhoto(), occurrence.isAutoMarkedNotDone(),
      photos.findByOccurrenceOrderByCreatedAtAsc(occurrence).stream().map(photo -> new CareOccurrencePhotoResponse(photo.getId(), "/api/care-occurrences/" + occurrence.getId() + "/photos/" + photo.getId(), photo.getContentType(), photo.getFileSize(), photo.getCreatedAt())).toList(),
      task.getVersion(), occurrence.getVersion()
    );
  }

  private String hiringLabel(br.com.cuidaplus.api.service_request.HiringType value){return switch(value){case PONTUAL->"Serviço pontual";case PERIODO_DETERMINADO->"Período determinado";case PERIODO_INDETERMINADO->"Período indeterminado";};}
  private String address(TaskOccurrence occurrence){var a=occurrence.getAssistedPerson().getEnderecoCuidado();return a==null?null:String.join(", ",a.getRua()+", "+a.getNumero(),a.getBairro(),a.getCidade()+" - "+a.getEstado());}
  private String dependencyLabel(TaskOccurrence occurrence){return switch(occurrence.getAssistedPerson().getGrauDependencia()){case BAIXA->"Baixa";case MODERADA->"Moderada";case ALTA->"Alta";case TOTAL->"Total";case NAO_SEI_INFORMAR->"Não informada";};}
  private String mobilityLabel(TaskOccurrence occurrence){return switch(occurrence.getAssistedPerson().getMobilidade()){case INDEPENDENTE->"Independente";case BENGALA->"Usa bengala";case ANDADOR->"Usa andador";case CADEIRA_RODAS->"Usa cadeira de rodas";case ACAMADO->"Pessoa acamada";case AUXILIO_PESSOA->"Precisa de auxílio";case OUTRO->"Outra condição";};}

  public MedicationResponse medication(CareTask task) {
    MedicationDetails value = task.getMedication();
    if (value == null || value.getName() == null) return null;
    return new MedicationResponse(value.getName(), value.getDosage(), value.getUnit(), value.getCustomUnit(), value.getAdministrationRoute(), value.getCustomAdministrationRoute(), value.getAdditionalInstructions());
  }
}
