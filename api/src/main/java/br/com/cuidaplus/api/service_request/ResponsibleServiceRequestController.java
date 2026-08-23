package br.com.cuidaplus.api.service_request;

import br.com.cuidaplus.api.security.AuthenticatedUser;
import br.com.cuidaplus.api.service_request.dto.ResponsibleServiceRequestResponse;
import br.com.cuidaplus.api.service_request.dto.RejectServiceRequestRequest;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/responsible/service-requests")
public class ResponsibleServiceRequestController {
  private final ServiceRequestService service;
  private final ServiceOpportunityService opportunities;

  public ResponsibleServiceRequestController(ServiceRequestService service, ServiceOpportunityService opportunities) {
    this.service = service;
    this.opportunities = opportunities;
  }

  @GetMapping("/{id}")
  public ResponsibleServiceRequestResponse details(@PathVariable UUID id) {
    return service.responsibleDetails(AuthenticatedUser.id(), id);
  }

  @PostMapping("/{id}/accept")
  public ResponsibleServiceRequestResponse accept(@PathVariable UUID id) {
    opportunities.accept(AuthenticatedUser.id(), id);
    return service.responsibleDetails(AuthenticatedUser.id(), id);
  }

  @PostMapping("/{id}/reject")
  public ResponsibleServiceRequestResponse reject(@PathVariable UUID id, @Valid @RequestBody RejectServiceRequestRequest body) {
    opportunities.reject(AuthenticatedUser.id(), id, body.reason());
    return service.responsibleDetails(AuthenticatedUser.id(), id);
  }
}
