package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_task.dto.*;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/responsible/care-occurrences")
public class ResponsibleCareOccurrenceController {
  private final TaskOccurrenceService service;
  public ResponsibleCareOccurrenceController(TaskOccurrenceService service){this.service=service;}
  @GetMapping public TaskOccurrencePageResponse list(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date,
    @RequestParam(defaultValue="America/Sao_Paulo") String timezone,@RequestParam(required=false) TaskOccurrenceStatus status,
    @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.listForResponsible(AuthenticatedUser.id(),date,timezone,status,page,size);}
  @GetMapping("/{occurrenceId}") public TaskOccurrenceResponse details(@PathVariable UUID occurrenceId){return service.responsibleDetails(AuthenticatedUser.id(),occurrenceId);}
}
