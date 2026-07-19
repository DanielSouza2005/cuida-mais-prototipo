package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.common.BusinessException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CareActivityIntegrationService {
  private final CareActivityRecordRepository repository;
  public CareActivityIntegrationService(CareActivityRecordRepository repository) { this.repository = repository; }

  public CareActivityRecord createForCompletedOccurrence(TaskOccurrence occurrence, Instant executedAt, String note) {
    if (repository.findByOccurrence(occurrence).isPresent()) {
      throw new BusinessException("Esta ocorrência já possui um registro de atividade.", HttpStatus.CONFLICT);
    }
    CareActivityRecord record = new CareActivityRecord();
    record.setOccurrence(occurrence);
    record.setContract(occurrence.getContract());
    record.setAssistedPerson(occurrence.getAssistedPerson());
    record.setResponsible(occurrence.getTask().getResponsibleCreator());
    record.setCaregiver(occurrence.getCaregiver());
    record.setActivityType("TAREFA_CONCLUIDA");
    record.setTitle(occurrence.getTask().getTitle());
    record.setNotes(note);
    record.setOccurredAt(executedAt);
    try {
      return repository.saveAndFlush(record);
    } catch (RuntimeException exception) {
      throw new BusinessException("Não foi possível criar o registro de atividade.", HttpStatus.CONFLICT);
    }
  }
}
