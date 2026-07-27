package br.com.cuidaplus.api.care_routine;

import br.com.cuidaplus.api.care_routine.dto.*;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/responsible/care-routines")
public class CareRoutineController {
  private final CareRoutineService service;
  public CareRoutineController(CareRoutineService service) { this.service = service; }
  @GetMapping public List<CareRoutineResponse> list(@RequestParam(required = false) UUID assistedPersonId, @RequestParam(required = false) Boolean active, @RequestParam(required = false) String search) { return service.list(AuthenticatedUser.id(), assistedPersonId, active, search); }
  @GetMapping("/form-data") public CareRoutineFormDataResponse formData() { return service.formData(AuthenticatedUser.id()); }
  @GetMapping("/{id}") public CareRoutineResponse details(@PathVariable UUID id) { return service.details(AuthenticatedUser.id(), id); }
  @PostMapping public CareRoutineResponse create(@Valid @RequestBody CareRoutineRequest request) { return service.create(AuthenticatedUser.id(), request); }
  @PutMapping("/{id}") public CareRoutineResponse update(@PathVariable UUID id, @Valid @RequestBody CareRoutineRequest request) { return service.update(AuthenticatedUser.id(), id, request); }
  @PatchMapping("/{id}/deactivate") public CareRoutineResponse deactivate(@PathVariable UUID id) { return service.setActive(AuthenticatedUser.id(), id, false); }
  @PatchMapping("/{id}/activate") public CareRoutineResponse activate(@PathVariable UUID id) { return service.setActive(AuthenticatedUser.id(), id, true); }
}
