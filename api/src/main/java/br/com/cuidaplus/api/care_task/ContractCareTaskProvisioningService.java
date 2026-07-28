package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.care_routine.StructuredCareItemMapper;
import br.com.cuidaplus.api.service_request.*;
import br.com.cuidaplus.api.user.User;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContractCareTaskProvisioningService {
  private final CareTaskRepository tasks; private final TaskRecurrenceService recurrence; private final CareContractRepository contracts;
  private final TaskOccurrenceRepository occurrences; private final TaskReminderService reminders;

  public ContractCareTaskProvisioningService(CareTaskRepository tasks,TaskRecurrenceService recurrence,CareContractRepository contracts,
    TaskOccurrenceRepository occurrences,TaskReminderService reminders){this.tasks=tasks;this.recurrence=recurrence;this.contracts=contracts;this.occurrences=occurrences;this.reminders=reminders;}

  @Transactional public void provisionForCaregiver(User caregiver){contracts.findByCaregiverUserOrderByUpdatedAtDesc(caregiver).forEach(this::provision);}
  @Transactional public void provisionForResponsible(User responsible){contracts.findByResponsibleUserOrderByUpdatedAtDesc(responsible).forEach(this::provision);}

  @Transactional
  public void provision(CareContract suppliedContract){
    CareContract contract=contracts.findForUpdateById(suppliedContract.getId()).orElse(suppliedContract);
    if(terminal(contract.getStatus()))return;
    ServiceRequest request=contract.getServiceRequest();if(request==null)return;
    List<CareTask> existing=new ArrayList<>(tasks.findByContractOrderByCreatedAtAsc(contract));
    for(ServiceRequestCareItemSnapshot source:request.getCareItemsSnapshot()){
      CareTask canonical=existing.stream().filter(task->sameSource(task,source)).findFirst().orElse(null);
      if(canonical==null){
        canonical=existing.stream().filter(task->task.getSourceSnapshotItem()==null&&task.getDuplicateOfTask()==null&&equivalent(task,source)).findFirst().orElse(null);
        if(canonical==null){canonical=new CareTask();initializeOwnership(canonical,contract);existing.add(canonical);}
        applySource(canonical,source);canonical=tasks.save(canonical);
      }
      retireLegacyDuplicates(canonical,source,existing);
      generateWindow(canonical,contract);
    }
  }

  private void initializeOwnership(CareTask task,CareContract contract){task.setStartDate(contract.getStartDate());task.setEndDate(contract.getEndDate());task.setTimezone("America/Sao_Paulo");task.setStatus(TaskSeriesStatus.ATIVA);task.setAssistedPerson(contract.getAssistedPerson());task.setContract(contract);task.setResponsibleCreator(contract.getResponsibleUser());task.setCaregiverExecutor(contract.getCaregiverUser());task.setCreatedBy(contract.getResponsibleUser());task.setUpdatedBy(contract.getResponsibleUser());}
  private void applySource(CareTask task,ServiceRequestCareItemSnapshot source){
    task.setSourceSnapshotItem(source);task.setTitle(source.getTitle());task.setDescription(source.getDescription());task.setCategory(source.getCategory());task.setCustomCategory(source.getCustomCategory());task.setPriority(source.getPriority());task.setRecurrenceType(source.getRecurrenceType());task.setScheduledTime(source.getScheduledTime());task.setIntervalDays(source.getIntervalDays());task.setWeekdays(new LinkedHashSet<>(source.getWeekdays()));task.setReminderEnabled(Boolean.TRUE.equals(source.getReminderEnabled()));task.setReminderMinutesBefore(source.getReminderMinutesBefore());task.setReminderAtScheduledTime(source.isReminderAtScheduledTime());task.setOverdueReminderEnabled(source.isOverdueReminderEnabled());task.setOverdueAfterMinutes(source.getOverdueAfterMinutes());task.setRepeatWhilePending(source.isRepeatWhilePending());task.setRepeatIntervalMinutes(source.getRepeatIntervalMinutes());task.setImportant(source.isImportant());task.setNotifyResponsibleIfImportant(source.isNotifyResponsibleIfImportant());task.setRequiresCompletionPhoto(source.isRequiresCompletionPhoto());task.setNotes(source.getNotes());task.setMedication(StructuredCareItemMapper.copyMedication(source.getMedication()));
  }
  private void retireLegacyDuplicates(CareTask canonical,ServiceRequestCareItemSnapshot source,List<CareTask> existing){
    for(CareTask candidate:existing){
      if(candidate==canonical||candidate.getSourceSnapshotItem()!=null||candidate.getDuplicateOfTask()!=null||!equivalent(candidate,source))continue;
      candidate.setDuplicateOfTask(canonical);candidate.setStatus(TaskSeriesStatus.FINALIZADA);candidate.setUpdatedBy(canonical.getResponsibleCreator());
      for(TaskOccurrence occurrence:occurrences.findByTaskOrderByScheduledInstantUtcDesc(candidate)){
        if(occurrence.getStatus()!=TaskOccurrenceStatus.PENDENTE&&occurrence.getStatus()!=TaskOccurrenceStatus.ATRASADA)continue;
        occurrence.setStatus(TaskOccurrenceStatus.CANCELADA);occurrence.setCanceledAt(Instant.now());occurrence.setStatusUpdatedAt(Instant.now());reminders.cancelFuture(occurrence);
      }
    }
  }
  private void generateWindow(CareTask task,CareContract contract){LocalDate windowEnd=contract.getStartDate().plusDays(TaskRecurrenceService.GENERATION_WINDOW_DAYS-1L);LocalDate end=contract.getEndDate()==null||contract.getEndDate().isAfter(windowEnd)?windowEnd:contract.getEndDate();recurrence.generate(task,contract.getStartDate(),end);}
  private boolean sameSource(CareTask task,ServiceRequestCareItemSnapshot source){return task.getSourceSnapshotItem()!=null&&Objects.equals(task.getSourceSnapshotItem().getId(),source.getId());}
  private boolean equivalent(CareTask task,ServiceRequestCareItemSnapshot source){return task.getContract()!=null&&Objects.equals(clean(task.getTitle()),clean(source.getTitle()))&&task.getCategory()==source.getCategory()&&Objects.equals(clean(task.getCustomCategory()),clean(source.getCustomCategory()))&&task.getRecurrenceType()==source.getRecurrenceType()&&Objects.equals(task.getScheduledTime(),source.getScheduledTime());}
  private String clean(String value){return value==null?"":value.trim().toLowerCase(Locale.forLanguageTag("pt-BR"));}
  private boolean terminal(CareContractStatus status){return status==CareContractStatus.CANCELADA||status==CareContractStatus.ENCERRADA||status==CareContractStatus.FINALIZADA;}
}
