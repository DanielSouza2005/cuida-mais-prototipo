package br.com.cuidaplus.api.agenda;

import br.com.cuidaplus.api.agenda.dto.*;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agenda/events")
public class AgendaController {
  private final AgendaService service;

  public AgendaController(AgendaService service) {
    this.service = service;
  }

  @GetMapping
  public AgendaEventsResponse events(
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
    @RequestParam(defaultValue = "MONTH") AgendaViewMode viewMode
  ) {
    return service.events(AuthenticatedUser.id(), startDate, endDate, viewMode);
  }

  @GetMapping("/contracts/{contractId}/dates/{eventDate}")
  public AgendaEventDetailsResponse details(
    @PathVariable UUID contractId,
    @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate
  ) {
    return service.details(AuthenticatedUser.id(), contractId, eventDate);
  }

  @Deprecated
  @GetMapping("/detail")
  public AgendaEventDetailsResponse detailsLegacy(
    @RequestParam UUID contractId,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate
  ) {
    return details(contractId, eventDate);
  }
}
