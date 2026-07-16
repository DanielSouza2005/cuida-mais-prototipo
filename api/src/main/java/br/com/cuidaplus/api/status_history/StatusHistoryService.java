package br.com.cuidaplus.api.status_history;

import br.com.cuidaplus.api.user.User;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class StatusHistoryService {
  private final StatusHistoryRepository repository;

  public StatusHistoryService(StatusHistoryRepository repository) { this.repository = repository; }

  public StatusHistory record(StatusHistoryEntityType type, UUID entityId, String previousStatus, String newStatus, User changedBy, String reason) {
    StatusHistory history = new StatusHistory();
    history.setEntityType(type); history.setEntityId(entityId); history.setPreviousStatus(previousStatus);
    history.setNewStatus(newStatus); history.setChangedByUser(changedBy);
    history.setReason(reason == null || reason.isBlank() ? null : reason.trim());
    return repository.save(history);
  }

  public List<StatusHistory> find(StatusHistoryEntityType type, UUID entityId) {
    return repository.findByEntityTypeAndEntityIdOrderByCreatedAtAsc(type, entityId);
  }
}
