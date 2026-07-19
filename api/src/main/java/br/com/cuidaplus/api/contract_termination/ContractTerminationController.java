package br.com.cuidaplus.api.contract_termination;

import br.com.cuidaplus.api.contract_termination.dto.*;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts/{contractId}")
public class ContractTerminationController {
  private final ContractTerminationService service;
  public ContractTerminationController(ContractTerminationService service) { this.service = service; }

  @GetMapping("/termination-form")
  public ContractTerminationFormResponse form(@PathVariable UUID contractId) { return service.form(AuthenticatedUser.id(), contractId); }

  @PostMapping("/terminate")
  public ContractTerminationResponse terminate(@PathVariable UUID contractId, @Valid @RequestBody TerminateContractRequest request) { return service.terminate(AuthenticatedUser.id(), contractId, request); }

  @PostMapping("/cancel-before-start")
  public ContractTerminationResponse cancel(@PathVariable UUID contractId, @Valid @RequestBody CancelContractBeforeStartRequest request) { return service.cancelBeforeStart(AuthenticatedUser.id(), contractId, request); }
}
