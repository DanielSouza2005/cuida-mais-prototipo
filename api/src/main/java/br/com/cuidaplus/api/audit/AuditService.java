package br.com.cuidaplus.api.audit;

import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.user.User;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditService {
  private static final Set<String> ALLOWED_SUMMARY_KEYS = Set.of("status", "situacaoConta", "situacaoAprovacao", "ativo");
  private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[A-Z0-9_]{1,80}");
  private static final Pattern FORBIDDEN_CONTENT = Pattern.compile(
    "(?i)(senha|password|token|hash|cpf|e-?mail|telefone|endereco|endere[cç]o|rua|avenida|alameda|travessa|cep|latitude|longitude|coordenada|alergia|medica[cç][aã]o|diagn[oó]stico|doen[cç]a|glicemia|press[aã]o|restri[cç][aã]o|relat[oó]rio|di[aá]rio|observa[cç][aã]o|foto|arquivo)"
  );
  private static final Pattern FORBIDDEN_VALUE = Pattern.compile(
    "(?i)([a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}|\\b\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}\\b|\\b(?:\\+?55\\s*)?(?:\\(?\\d{2}\\)?\\s*)?9?\\d{4}[-\\s]?\\d{4}\\b|\\b\\d{5}-?\\d{3}\\b|\\.(?:jpe?g|png|heic|webp)\\b)"
  );

  private final CriticalActionAuditRepository repository;
  public AuditService(CriticalActionAuditRepository repository) { this.repository = repository; }

  @Transactional
  public CriticalActionAudit success(AuditAction action, AuditCategory category, User actor,
    String entityType, UUID entityId, Map<String, String> previous, Map<String, String> next, String reason) {
    return save(action, category, AuditResult.SUCESSO, actor, entityType, entityId, null, null, previous, next, reason, null);
  }

  @Transactional
  public CriticalActionAudit success(AuditAction action, AuditCategory category, User actor,
    String entityType, UUID entityId, String relatedType, UUID relatedId) {
    return save(action, category, AuditResult.SUCESSO, actor, entityType, entityId, relatedType, relatedId, null, null, null, null);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public CriticalActionAudit failure(AuditAction action, AuditCategory category, AuditResult result, User actor,
    String entityType, UUID entityId, String message) {
    if (result == AuditResult.SUCESSO) throw new IllegalArgumentException("O registro de falha não pode usar resultado de sucesso.");
    return save(action, category, result, actor, entityType, entityId, null, null, null, null, null, message);
  }

  private CriticalActionAudit save(AuditAction action, AuditCategory category, AuditResult result, User actor,
    String entityType, UUID entityId, String relatedType, UUID relatedId, Map<String, String> previous,
    Map<String, String> next, String reason, String message) {
    CriticalActionAudit event = new CriticalActionAudit();
    event.setAction(Objects.requireNonNull(action)); event.setCategory(Objects.requireNonNull(category));
    event.setResult(Objects.requireNonNull(result)); event.setResponsibleUser(actor);
    event.setResponsibleUserType(actor == null ? null : actor.getUserType());
    event.setAffectedEntityType(identifier(entityType)); event.setAffectedEntityId(entityId);
    event.setRelatedEntityType(identifier(relatedType)); event.setRelatedEntityId(relatedId);
    event.setPreviousSummary(summary(previous)); event.setNewSummary(summary(next));
    event.setReason(text(reason, 500)); event.setSummaryMessage(text(message, 500));
    RequestMetadata metadata = requestMetadata(); event.setIp(metadata.ip()); event.setSummarizedUserAgent(metadata.userAgent());
    return repository.save(event);
  }

  @Transactional(readOnly = true)
  public AuditDtos.Page list(AuditAction action, AuditCategory category, AuditResult result, UUID responsibleUserId,
    String affectedEntityType, UUID affectedEntityId, Instant start, Instant end, int page, int size) {
    int safePage = Math.max(page, 0), safeSize = Math.min(Math.max(size, 1), 50);
    Specification<CriticalActionAudit> specification = (root, query, builder) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (action != null) predicates.add(builder.equal(root.get("action"), action));
      if (category != null) predicates.add(builder.equal(root.get("category"), category));
      if (result != null) predicates.add(builder.equal(root.get("result"), result));
      if (responsibleUserId != null) predicates.add(builder.equal(root.get("responsibleUser").get("id"), responsibleUserId));
      if (affectedEntityType != null && !affectedEntityType.isBlank()) predicates.add(builder.equal(root.get("affectedEntityType"), identifier(affectedEntityType)));
      if (affectedEntityId != null) predicates.add(builder.equal(root.get("affectedEntityId"), affectedEntityId));
      if (start != null) predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"), start));
      if (end != null) predicates.add(builder.lessThanOrEqualTo(root.get("createdAt"), end));
      return builder.and(predicates.toArray(Predicate[]::new));
    };
    var resultPage = repository.findAll(specification, PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));
    return new AuditDtos.Page(resultPage.getContent().stream().map(this::summaryDto).toList(), safePage, safeSize,
      resultPage.getTotalElements(), resultPage.getTotalPages());
  }

  @Transactional(readOnly = true)
  public AuditDtos.Details details(UUID id) {
    return detailsDto(repository.findById(id).orElseThrow(() -> new BusinessException("Registro de auditoria não encontrado.", HttpStatus.NOT_FOUND)));
  }

  private AuditDtos.Summary summaryDto(CriticalActionAudit value) {
    User actor = value.getResponsibleUser();
    return new AuditDtos.Summary(value.getId(), value.getAction(), value.getAction().getLabel(), value.getCategory(),
      value.getCategory().getLabel(), value.getResult(), value.getResult().getLabel(), actor == null ? null : actor.getId(),
      actor == null ? "Sistema" : actor.getFullName(), value.getAffectedEntityType(), value.getAffectedEntityId(), value.getCreatedAt());
  }

  private AuditDtos.Details detailsDto(CriticalActionAudit value) {
    User actor = value.getResponsibleUser();
    return new AuditDtos.Details(value.getId(), value.getAction(), value.getAction().getLabel(), value.getCategory(),
      value.getCategory().getLabel(), value.getResult(), value.getResult().getLabel(),
      actor == null ? null : new AuditDtos.Actor(actor.getId(), actor.getFullName()), value.getAffectedEntityType(), value.getAffectedEntityId(),
      value.getRelatedEntityType(), value.getRelatedEntityId(), value.getPreviousSummary(), value.getNewSummary(),
      value.getReason(), value.getSummaryMessage(), value.getCreatedAt());
  }

  public static Map<String, String> state(String key, Object value) {
    return value == null ? null : Map.of(key, value.toString());
  }

  private Map<String, String> summary(Map<String, String> source) {
    if (source == null || source.isEmpty()) return null;
    LinkedHashMap<String, String> result = new LinkedHashMap<>();
    source.forEach((key, value) -> {
      if (!ALLOWED_SUMMARY_KEYS.contains(key)) throw new IllegalArgumentException("Campo não permitido no resumo de auditoria: " + key);
      String cleanValue = text(value, 80);
      if (cleanValue != null) result.put(key, cleanValue);
    });
    return result.isEmpty() ? null : Collections.unmodifiableMap(result);
  }

  private String identifier(String value) {
    if (value == null || value.isBlank()) return null;
    String normalized = value.trim().toUpperCase(Locale.ROOT);
    if (!SAFE_IDENTIFIER.matcher(normalized).matches()) throw new IllegalArgumentException("Identificador de auditoria inválido.");
    return normalized;
  }

  private String text(String value, int maxLength) {
    if (value == null || value.isBlank()) return null;
    String normalized = value.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", " ").replaceAll("[\\r\\n\\t]+", " ").trim();
    if (FORBIDDEN_CONTENT.matcher(normalized).find() || FORBIDDEN_VALUE.matcher(normalized).find())
      return "Conteúdo omitido por minimização de dados.";
    return normalized.substring(0, Math.min(normalized.length(), maxLength));
  }

  private RequestMetadata requestMetadata() {
    if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) return new RequestMetadata(null, null);
    HttpServletRequest request = attributes.getRequest();
    String forwarded = request.getHeader("X-Forwarded-For");
    String ip = forwarded == null || forwarded.isBlank() ? request.getHeader("X-Real-IP") : forwarded.split(",", 2)[0];
    if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
    return new RequestMetadata(boundedHeader(ip, 80), boundedHeader(request.getHeader("User-Agent"), 255));
  }

  private String boundedHeader(String value, int maxLength) {
    if (value == null || value.isBlank()) return null;
    String normalized = value.replaceAll("[\\r\\n\\t]", " ").trim();
    return normalized.substring(0, Math.min(normalized.length(), maxLength));
  }

  private record RequestMetadata(String ip, String userAgent) {}
}
