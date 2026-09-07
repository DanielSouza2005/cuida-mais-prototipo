package br.com.cuidaplus.api.audit;

import java.time.Instant;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit")
public class AuditController {
  private final AuditService service;
  public AuditController(AuditService service) { this.service = service; }

  @GetMapping
  public AuditDtos.Page list(@RequestParam(required = false) AuditAction action,
    @RequestParam(required = false) AuditCategory category, @RequestParam(required = false) AuditResult result,
    @RequestParam(required = false) UUID responsibleUserId, @RequestParam(required = false) String affectedEntityType,
    @RequestParam(required = false) UUID affectedEntityId,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
    @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return service.list(action, category, result, responsibleUserId, affectedEntityType, affectedEntityId, start, end, page, size);
  }

  @GetMapping("/{id}")
  public AuditDtos.Details details(@PathVariable UUID id) { return service.details(id); }
}
