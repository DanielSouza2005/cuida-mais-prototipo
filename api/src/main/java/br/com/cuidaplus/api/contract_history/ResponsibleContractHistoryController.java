package br.com.cuidaplus.api.contract_history;

import br.com.cuidaplus.api.contract_history.dto.*;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/responsible/contracts")
public class ResponsibleContractHistoryController {
  private final ResponsibleContractHistoryService service;
  public ResponsibleContractHistoryController(ResponsibleContractHistoryService service) { this.service = service; }

  @GetMapping
  public ContractHistoryPageResponse list(
    @RequestParam(required = false) ContractHistoryStatusGroup statusGroup,
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String participantName,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateTo,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) { return service.list(AuthenticatedUser.id(), statusGroup, status, participantName, startDateFrom, startDateTo, page, size); }

  @GetMapping("/{itemType}/{id}")
  public ContractHistoryDetailsResponse details(@PathVariable ContractHistoryItemType itemType, @PathVariable UUID id) {
    return service.details(AuthenticatedUser.id(), itemType, id);
  }
}
