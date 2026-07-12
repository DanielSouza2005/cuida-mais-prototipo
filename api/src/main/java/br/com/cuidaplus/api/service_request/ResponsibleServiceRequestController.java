package br.com.cuidaplus.api.service_request;

import br.com.cuidaplus.api.security.AuthenticatedUser;
import br.com.cuidaplus.api.service_request.dto.ResponsibleServiceRequestResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/responsible/service-requests")
public class ResponsibleServiceRequestController {
  private final ServiceRequestService service;

  public ResponsibleServiceRequestController(ServiceRequestService service) {
    this.service = service;
  }

  @GetMapping("/{id}")
  public ResponsibleServiceRequestResponse details(@PathVariable UUID id) {
    return service.responsibleDetails(AuthenticatedUser.id(), id);
  }
}
