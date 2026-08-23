package br.com.cuidaplus.api.service_request;

import br.com.cuidaplus.api.security.AuthenticatedUser;
import br.com.cuidaplus.api.service_request.dto.ResponsibleServicePublicationPageResponse;
import br.com.cuidaplus.api.service_request.dto.ResponsibleServicePublicationResponse;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/responsible/service-publications")
public class ResponsibleServicePublicationController {
  private final ResponsibleServicePublicationService service;
  public ResponsibleServicePublicationController(ResponsibleServicePublicationService service) { this.service = service; }

  @GetMapping
  public ResponsibleServicePublicationPageResponse list(
    @RequestParam(required = false) ServiceRequestStatus status,
    @RequestParam(required = false) UUID assistedPersonId,
    @RequestParam(required = false) HiringType hiringType,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
    @RequestParam(required = false) String city,
    @RequestParam(required = false) String neighborhood,
    @RequestParam(required = false) String needs,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) { return service.list(AuthenticatedUser.id(), status, assistedPersonId, hiringType, startDate, endDate, city, neighborhood, needs, page, size); }

  @GetMapping("/{id}") public ResponsibleServicePublicationResponse details(@PathVariable UUID id) { return service.details(AuthenticatedUser.id(), id); }
  @PatchMapping("/{id}/cancel") public ResponsibleServicePublicationResponse cancel(@PathVariable UUID id) { return service.cancel(AuthenticatedUser.id(), id); }
}
