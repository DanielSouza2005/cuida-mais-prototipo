package br.com.cuidaplus.api.status_history;

import br.com.cuidaplus.api.audit.*;
import br.com.cuidaplus.api.user.User;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class StatusHistoryService {
  private final StatusHistoryRepository repository;
  private final AuditService audit;

  public StatusHistoryService(StatusHistoryRepository repository, AuditService audit) {
    this.repository = repository; this.audit = audit;
  }

  public StatusHistory record(StatusHistoryEntityType type, UUID entityId, String previousStatus, String newStatus, User changedBy, String reason) {
    StatusHistory history = new StatusHistory();
    history.setEntityType(type); history.setEntityId(entityId); history.setPreviousStatus(previousStatus);
    history.setNewStatus(newStatus); history.setChangedByUser(changedBy);
    history.setReason(reason == null || reason.isBlank() ? null : reason.trim());
    StatusHistory saved = repository.save(history);
    AuditAction action = action(type, previousStatus, newStatus);
    if (action != null) {
      audit.success(action, AuditCategory.CONTRATACAO, changedBy, entityType(type), entityId,
        AuditService.state("status", previousStatus), AuditService.state("status", newStatus), reason);
    }
    return saved;
  }

  public List<StatusHistory> find(StatusHistoryEntityType type, UUID entityId) {
    return repository.findByEntityTypeAndEntityIdOrderByCreatedAtAsc(type, entityId);
  }

  private AuditAction action(StatusHistoryEntityType type, String previous, String next) {
    if (type == StatusHistoryEntityType.SERVICE_REQUEST) {
      if (previous == null) return AuditAction.SOLICITACAO_SERVICO_CRIADA;
      return switch (next) {
        case "ACEITA" -> AuditAction.SOLICITACAO_SERVICO_ACEITA;
        case "REJEITADA" -> AuditAction.SOLICITACAO_SERVICO_REJEITADA;
        case "CANCELADA" -> AuditAction.SOLICITACAO_SERVICO_CANCELADA;
        case "EXPIRADA" -> AuditAction.SOLICITACAO_SERVICO_EXPIRADA;
        default -> null;
      };
    }
    if (previous == null) return AuditAction.CONTRATACAO_CRIADA;
    return switch (next) {
      case "ENCERRAMENTO_AGENDADO" -> AuditAction.CONTRATACAO_ENCERRAMENTO_AGENDADO;
      case "ENCERRADA", "FINALIZADA" -> AuditAction.CONTRATACAO_ENCERRADA;
      case "CANCELADA" -> AuditAction.CONTRATACAO_CANCELADA;
      default -> AuditAction.CONTRATACAO_STATUS_ALTERADO;
    };
  }

  private String entityType(StatusHistoryEntityType type) {
    return type == StatusHistoryEntityType.SERVICE_REQUEST ? "SOLICITACAO_SERVICO" : "CONTRATACAO";
  }
}
