package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_task.dto.*;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping({"/api/caregiver/care-tasks", "/api/caregiver/tasks"})
public class CaregiverCareTaskController {
  private final TaskOccurrenceService service;
  public CaregiverCareTaskController(TaskOccurrenceService service) { this.service = service; }

  @GetMapping public TaskOccurrencePageResponse tasks(
    @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date,
    @RequestParam(defaultValue="America/Sao_Paulo") String timezone,
    @RequestParam(required=false) TaskCategory category,
    @RequestParam(required=false) TaskOccurrenceStatus status,
    @RequestParam(required=false) UUID assistedPersonId,
    @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
    return service.day(AuthenticatedUser.id(), date, timezone, category, status, assistedPersonId, page, size);
  }

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
  @GetMapping("/{occurrenceId}") public TaskOccurrenceResponse taskDetails(@PathVariable UUID occurrenceId) { return service.details(AuthenticatedUser.id(), occurrenceId); }
  @PatchMapping(value="/occurrences/{occurrenceId}/complete", consumes=MediaType.APPLICATION_JSON_VALUE) public TaskOccurrenceResponse complete(@PathVariable UUID occurrenceId, @Valid @RequestBody CompleteOccurrenceRequest request) { return service.complete(AuthenticatedUser.id(), occurrenceId, request); }
  @PatchMapping(value="/{occurrenceId}/complete", consumes=MediaType.APPLICATION_JSON_VALUE) public TaskOccurrenceResponse completeTask(@PathVariable UUID occurrenceId, @Valid @RequestBody CompleteOccurrenceRequest request) { return service.complete(AuthenticatedUser.id(), occurrenceId, request); }
  @PatchMapping(value={"/occurrences/{occurrenceId}/complete", "/{occurrenceId}/complete"}, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
  public TaskOccurrenceResponse completeWithPhotos(@PathVariable UUID occurrenceId, @RequestParam long version,
    @RequestParam(required=false) String notes, @RequestPart(required=false) List<MultipartFile> photos) {
    return service.complete(AuthenticatedUser.id(), occurrenceId, version, notes, photos);
  }
  @PatchMapping("/occurrences/{occurrenceId}/not-completed") public TaskOccurrenceResponse notCompleted(@PathVariable UUID occurrenceId, @Valid @RequestBody NotCompletedOccurrenceRequest request) { return service.notCompleted(AuthenticatedUser.id(), occurrenceId, request); }
  @PatchMapping("/{occurrenceId}/not-done") public TaskOccurrenceResponse notDone(@PathVariable UUID occurrenceId, @Valid @RequestBody NotCompletedOccurrenceRequest request) { return service.notCompleted(AuthenticatedUser.id(), occurrenceId, request); }
}
