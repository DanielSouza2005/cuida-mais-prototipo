package br.com.cuidaplus.api.service_request;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import br.com.cuidaplus.api.service_request.dto.*;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/service-requests")
public class ServiceRequestController {
  private final ServiceRequestService service;
  public ServiceRequestController(ServiceRequestService service) { this.service=service; }
  @GetMapping("/form-data") public ServiceRequestFormDataResponse formData(@RequestParam UUID caregiverId) { return service.formData(AuthenticatedUser.id(), caregiverId); }
  @PostMapping public ServiceRequestResponse create(@Valid @RequestBody ServiceRequestCreateRequest request) { return service.create(AuthenticatedUser.id(), request); }
  @GetMapping("/{id}") public ServiceRequestResponse find(@PathVariable UUID id) { return service.find(AuthenticatedUser.id(), id); }
  @GetMapping("/my") public List<ServiceRequestResponse> my() { return service.my(AuthenticatedUser.id()); }
  @PatchMapping("/{id}/cancel") public ServiceRequestResponse cancel(@PathVariable UUID id) { return service.cancel(AuthenticatedUser.id(), id); }
}
