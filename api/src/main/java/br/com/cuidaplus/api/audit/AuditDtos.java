package br.com.cuidaplus.api.audit;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AuditDtos {
  private AuditDtos() {}
  public record Page(List<Summary> content, int page, int size, long totalElements, int totalPages) {}
  public record Summary(UUID id, AuditAction action, String actionLabel, AuditCategory category, String categoryLabel,
    AuditResult result, String resultLabel, UUID responsibleUserId, String responsibleUserName,
    String affectedEntityType, UUID affectedEntityId, Instant createdAt) {}
  public record Actor(UUID id, String name) {}
  public record Details(UUID id, AuditAction action, String actionLabel, AuditCategory category, String categoryLabel,
    AuditResult result, String resultLabel, Actor responsibleUser, String affectedEntityType, UUID affectedEntityId,
    String relatedEntityType, UUID relatedEntityId, Map<String, String> previousSummary, Map<String, String> newSummary,
    String reason, String summaryMessage, Instant createdAt) {}
}

