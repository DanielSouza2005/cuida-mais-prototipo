package br.com.cuidaplus.api.care_task.dto;

import br.com.cuidaplus.api.care_task.TaskAuditAction;
import java.time.Instant;
import java.util.UUID;

public record TaskAuditResponse(UUID id, TaskAuditAction action, String actorName, String details, Instant createdAt) {}
