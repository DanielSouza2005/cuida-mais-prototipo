package br.com.cuidaplus.api.service_request;

import br.com.cuidaplus.api.security.AuthenticatedUser;
import br.com.cuidaplus.api.service_request.dto.CaregiverReceivedRequestResponse;
import br.com.cuidaplus.api.service_request.dto.CaregiverServiceRequestPageResponse;
import br.com.cuidaplus.api.service_request.dto.RejectServiceRequestRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/caregiver/service-requests")
public class CaregiverServiceRequestController {

  private final CaregiverServiceRequestService service;

  public CaregiverServiceRequestController(CaregiverServiceRequestService service) {
    this.service = service;
  }

  @GetMapping
  public CaregiverServiceRequestPageResponse list(
    @RequestParam(required = false) ServiceRequestStatus status,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    return service.list(AuthenticatedUser.id(), status, page, size);
  }

  @GetMapping("/{id}")
  public CaregiverReceivedRequestResponse details(@PathVariable UUID id) {
    return service.details(AuthenticatedUser.id(), id);
  }

  @PatchMapping("/{id}/accept")
  public CaregiverReceivedRequestResponse accept(@PathVariable UUID id) {
    return service.accept(AuthenticatedUser.id(), id);
  }

  @PatchMapping("/{id}/reject")
  public CaregiverReceivedRequestResponse reject(
    @PathVariable UUID id,
    @Valid @RequestBody RejectServiceRequestRequest body
  ) {
    return service.reject(AuthenticatedUser.id(), id, body.reason());
  }

  @Deprecated
  @PostMapping("/{id}/accept")
  public CaregiverReceivedRequestResponse acceptLegacy(@PathVariable UUID id) {
    return accept(id);
  }

  @Deprecated
  @PostMapping("/{id}/reject")
  public CaregiverReceivedRequestResponse rejectLegacy(
    @PathVariable UUID id,
    @Valid @RequestBody RejectServiceRequestRequest body
  ) {
    return reject(id, body);
  }
}
