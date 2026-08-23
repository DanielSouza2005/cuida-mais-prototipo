package br.com.cuidaplus.api.service_request;

import br.com.cuidaplus.api.security.AuthenticatedUser;
import br.com.cuidaplus.api.service_request.dto.*;
import br.com.cuidaplus.api.caregiver.dto.CaregiverLocationSuggestionResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caregiver/service-opportunities")
public class ServiceOpportunityController {
  private final ServiceOpportunityService service;
  public ServiceOpportunityController(ServiceOpportunityService service) { this.service = service; }

  @GetMapping public ServiceOpportunityPageResponse search(@RequestParam(required = false) String city, @RequestParam(required = false) String neighborhood, @RequestParam(required = false) String state, @RequestParam(required = false) HiringType hiringType, @RequestParam(required = false) Double originLat, @RequestParam(required = false) Double originLng, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) { return service.search(AuthenticatedUser.id(), city, neighborhood, state, hiringType, originLat, originLng, page, size); }
  @GetMapping("/locations") public List<CaregiverLocationSuggestionResponse> locations(@RequestParam(required = false) String query) { return service.locations(AuthenticatedUser.id(), query); }
  @GetMapping("/applications") public ServiceOpportunityPageResponse applications(@RequestParam(required = false) ServiceRequestStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) { return service.applications(AuthenticatedUser.id(), status, page, size); }
  @GetMapping("/{id}") public ServiceOpportunityResponse details(@PathVariable UUID id, @RequestParam(required = false) Double originLat, @RequestParam(required = false) Double originLng) { return service.details(AuthenticatedUser.id(), id, originLat, originLng); }
  @GetMapping("/{id}/contract") public AcceptedOpportunityContractResponse acceptedContract(@PathVariable UUID id) { return service.acceptedContract(AuthenticatedUser.id(), id); }
  @PostMapping("/{id}/apply") public ServiceOpportunityResponse apply(@PathVariable UUID id) { return service.apply(AuthenticatedUser.id(), id); }
}
