package br.com.cuidaplus.api.audit;

import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "auditoria_acao_critica")
public class CriticalActionAudit {
  @Id @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Enumerated(EnumType.STRING) @Column(name = "tipo_acao", nullable = false, length = 80)
  private AuditAction action;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50)
  private AuditCategory category;
  @Enumerated(EnumType.STRING) @Column(name = "resultado", nullable = false, length = 40)
  private AuditResult result;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "usuario_responsavel_id")
  private User responsibleUser;
  @Enumerated(EnumType.STRING) @Column(name = "tipo_usuario_responsavel", length = 40)
  private UserType responsibleUserType;
  @Column(name = "entidade_afetada_tipo", length = 80)
  private String affectedEntityType;
  @Column(name = "entidade_afetada_id")
  private UUID affectedEntityId;
  @Column(name = "entidade_relacionada_tipo", length = 80)
  private String relatedEntityType;
  @Column(name = "entidade_relacionada_id")
  private UUID relatedEntityId;
  @JdbcTypeCode(SqlTypes.JSON) @Column(name = "valor_anterior_resumido", columnDefinition = "jsonb")
  private Map<String, String> previousSummary;
  @JdbcTypeCode(SqlTypes.JSON) @Column(name = "valor_novo_resumido", columnDefinition = "jsonb")
  private Map<String, String> newSummary;
  @Column(length = 500) private String reason;
  @Column(name = "mensagem_resumida", length = 500) private String summaryMessage;
  @Column(length = 80) private String ip;
  @Column(name = "user_agent_resumido", length = 255) private String summarizedUserAgent;
  @Column(name = "criado_em", nullable = false, updatable = false) private Instant createdAt;

  @PrePersist void prePersist() { if (createdAt == null) createdAt = Instant.now(); }

  public UUID getId() { return id; }
  public AuditAction getAction() { return action; } public void setAction(AuditAction value) { action = value; }
  public AuditCategory getCategory() { return category; } public void setCategory(AuditCategory value) { category = value; }
  public AuditResult getResult() { return result; } public void setResult(AuditResult value) { result = value; }
  public User getResponsibleUser() { return responsibleUser; } public void setResponsibleUser(User value) { responsibleUser = value; }
  public UserType getResponsibleUserType() { return responsibleUserType; } public void setResponsibleUserType(UserType value) { responsibleUserType = value; }
  public String getAffectedEntityType() { return affectedEntityType; } public void setAffectedEntityType(String value) { affectedEntityType = value; }
  public UUID getAffectedEntityId() { return affectedEntityId; } public void setAffectedEntityId(UUID value) { affectedEntityId = value; }
  public String getRelatedEntityType() { return relatedEntityType; } public void setRelatedEntityType(String value) { relatedEntityType = value; }
  public UUID getRelatedEntityId() { return relatedEntityId; } public void setRelatedEntityId(UUID value) { relatedEntityId = value; }
  public Map<String, String> getPreviousSummary() { return previousSummary; } public void setPreviousSummary(Map<String, String> value) { previousSummary = value; }
  public Map<String, String> getNewSummary() { return newSummary; } public void setNewSummary(Map<String, String> value) { newSummary = value; }
  public String getReason() { return reason; } public void setReason(String value) { reason = value; }
  public String getSummaryMessage() { return summaryMessage; } public void setSummaryMessage(String value) { summaryMessage = value; }
  public String getIp() { return ip; } public void setIp(String value) { ip = value; }
  public String getSummarizedUserAgent() { return summarizedUserAgent; } public void setSummarizedUserAgent(String value) { summarizedUserAgent = value; }
  public Instant getCreatedAt() { return createdAt; }
}
