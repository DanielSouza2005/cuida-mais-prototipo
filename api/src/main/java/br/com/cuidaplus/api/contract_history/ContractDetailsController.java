package br.com.cuidaplus.api.contract_history;

import br.com.cuidaplus.api.contract_history.dto.ContractHistoryDetailsResponse;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts")
public class ContractDetailsController {
  private final ResponsibleContractHistoryService service;
  public ContractDetailsController(ResponsibleContractHistoryService service) { this.service = service; }

  @GetMapping("/{contractId}")
  public ContractHistoryDetailsResponse details(@PathVariable UUID contractId) { return service.contractDetails(AuthenticatedUser.id(), contractId); }
}
