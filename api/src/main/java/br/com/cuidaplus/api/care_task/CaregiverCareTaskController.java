package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_task.dto.*;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caregiver/care-tasks")
public class CaregiverCareTaskController {
  private final TaskOccurrenceService service;
  public CaregiverCareTaskController(TaskOccurrenceService service) { this.service = service; }

  @GetMapping("/day") public TaskOccurrencePageResponse day(
    @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date, @RequestParam String timezone,
    @RequestParam(required=false) TaskCategory category, @RequestParam(required=false) TaskOccurrenceStatus status,
    @RequestParam(required=false) UUID assistedPersonId) {
    return service.day(AuthenticatedUser.id(), date, timezone, category, status, assistedPersonId);
  }
  @GetMapping("/occurrences") public TaskOccurrencePageResponse list(
    @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate endDate,
    @RequestParam(required=false) TaskCategory category, @RequestParam(required=false) TaskOccurrenceStatus status,
    @RequestParam(required=false) UUID assistedPersonId, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
    return service.list(AuthenticatedUser.id(), startDate, endDate, category, status, assistedPersonId, page, size);
  }
  @GetMapping("/occurrences/{occurrenceId}") public TaskOccurrenceResponse details(@PathVariable UUID occurrenceId) { return service.details(AuthenticatedUser.id(), occurrenceId); }
  @PatchMapping("/occurrences/{occurrenceId}/complete") public TaskOccurrenceResponse complete(@PathVariable UUID occurrenceId, @Valid @RequestBody CompleteOccurrenceRequest request) { return service.complete(AuthenticatedUser.id(), occurrenceId, request); }
  @PatchMapping("/occurrences/{occurrenceId}/not-completed") public TaskOccurrenceResponse notCompleted(@PathVariable UUID occurrenceId, @Valid @RequestBody NotCompletedOccurrenceRequest request) { return service.notCompleted(AuthenticatedUser.id(), occurrenceId, request); }
}
