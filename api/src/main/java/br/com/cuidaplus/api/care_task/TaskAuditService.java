package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.audit.*;
import br.com.cuidaplus.api.user.User;
import org.springframework.stereotype.Service;

@Service
public class TaskAuditService {
  private final TaskAuditEntryRepository repository;
  private final AuditService criticalAudit;
  public TaskAuditService(TaskAuditEntryRepository repository, AuditService criticalAudit) {
    this.repository = repository; this.criticalAudit = criticalAudit;
  }

  public void record(CareTask task, TaskOccurrence occurrence, User actor, TaskAuditAction action, String details) {
    TaskAuditEntry entry = new TaskAuditEntry();
    entry.setTask(task); entry.setOccurrence(occurrence); entry.setActor(actor); entry.setAction(action); entry.setDetails(details);
    repository.save(entry);
    AuditAction criticalAction = criticalAction(action);
    if (criticalAction != null) {
      String entityType = occurrence == null ? "TAREFA_CUIDADO" : "OCORRENCIA_CUIDADO";
      criticalAudit.success(criticalAction, AuditCategory.ASSISTENCIAL, actor, entityType,
        occurrence == null ? task.getId() : occurrence.getId(),
        occurrence == null ? null : "TAREFA_CUIDADO", occurrence == null ? null : task.getId());
    }
  }

  private AuditAction criticalAction(TaskAuditAction action) {
    return switch (action) {
      case CRIADA -> AuditAction.TAREFA_CUIDADO_CRIADA;
      case ALTERADA -> AuditAction.TAREFA_CUIDADO_ALTERADA;
      case PAUSADA, CANCELADA -> AuditAction.TAREFA_CUIDADO_INATIVADA;
      case REATIVADA -> AuditAction.TAREFA_CUIDADO_REATIVADA;
      case OCORRENCIA_ALTERADA -> AuditAction.OCORRENCIA_CUIDADO_ALTERADA;
      case OCORRENCIA_CANCELADA -> AuditAction.OCORRENCIA_CUIDADO_CANCELADA;
      case OCORRENCIA_CONCLUIDA -> AuditAction.OCORRENCIA_CUIDADO_CONCLUIDA;
      case OCORRENCIA_NAO_REALIZADA, OCORRENCIA_NAO_REALIZADA_AUTOMATICAMENTE -> AuditAction.OCORRENCIA_CUIDADO_NAO_REALIZADA;
      case FOTO_ANEXADA -> AuditAction.FOTO_ASSISTENCIAL_ENVIADA;
      case NOTIFICACAO_INTERNA_CRIADA, LEMBRETE_CANCELADO -> null;
    };
  }
}
