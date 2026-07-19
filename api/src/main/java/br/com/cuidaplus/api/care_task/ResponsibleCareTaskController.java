package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_task.dto.*;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/responsible/care-tasks")
public class ResponsibleCareTaskController {
  private final CareTaskService service;
  public ResponsibleCareTaskController(CareTaskService service) { this.service = service; }

  @PostMapping public CareTaskDetailsResponse create(@Valid @RequestBody CreateCareTaskRequest request) { return service.create(AuthenticatedUser.id(), request); }
  @GetMapping("/form-data") public TaskFormDataResponse formData() { return service.formData(AuthenticatedUser.id()); }
  @GetMapping public CareTaskPageResponse list(@RequestParam(required=false) String search, @RequestParam(required=false) TaskCategory category,
    @RequestParam(required=false) TaskSeriesStatus status, @RequestParam(required=false) TaskPriority priority,
    @RequestParam(required=false) UUID assistedPersonId, @RequestParam(required=false) UUID caregiverId,
    @RequestParam(required=false) TaskOccurrenceStatus occurrenceStatus,
    @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate endDate,
    @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
    return service.list(AuthenticatedUser.id(), search, category, status, priority, assistedPersonId, caregiverId, occurrenceStatus, startDate, endDate, page, size);
  }
  @GetMapping("/{taskId}") public CareTaskDetailsResponse details(@PathVariable UUID taskId) { return service.details(AuthenticatedUser.id(), taskId); }
  @PutMapping("/{taskId}") public CareTaskDetailsResponse update(@PathVariable UUID taskId, @Valid @RequestBody UpdateCareTaskRequest request) { return service.update(AuthenticatedUser.id(), taskId, request); }
  @PatchMapping("/{taskId}/pause") public CareTaskDetailsResponse pause(@PathVariable UUID taskId, @Valid @RequestBody TaskActionRequest request) { return service.pause(AuthenticatedUser.id(), taskId, request); }
  @PatchMapping("/{taskId}/reactivate") public CareTaskDetailsResponse reactivate(@PathVariable UUID taskId, @Valid @RequestBody TaskActionRequest request) { return service.reactivate(AuthenticatedUser.id(), taskId, request); }
  @PatchMapping("/{taskId}/cancel") public CareTaskDetailsResponse cancel(@PathVariable UUID taskId, @Valid @RequestBody TaskActionRequest request) { return service.cancel(AuthenticatedUser.id(), taskId, request); }
  @GetMapping("/{taskId}/occurrences") public TaskOccurrencePageResponse occurrences(@PathVariable UUID taskId,
    @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate endDate,
    @RequestParam(required=false) TaskOccurrenceStatus status, @RequestParam(defaultValue="false") boolean history,
    @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
    return service.occurrences(AuthenticatedUser.id(), taskId, startDate, endDate, status, history, page, size);
  }
  @PatchMapping("/occurrences/{occurrenceId}/cancel") public TaskOccurrenceResponse cancelOccurrence(@PathVariable UUID occurrenceId, @Valid @RequestBody TaskActionRequest request) { return service.cancelOccurrence(AuthenticatedUser.id(), occurrenceId, request); }
  @GetMapping("/occurrences/{occurrenceId}") public TaskOccurrenceResponse occurrenceDetails(@PathVariable UUID occurrenceId) { return service.occurrenceDetails(AuthenticatedUser.id(), occurrenceId); }
}
