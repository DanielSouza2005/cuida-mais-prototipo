package br.com.cuidaplus.api.account;

import br.com.cuidaplus.api.account.dto.DeleteAccountRequest;
import br.com.cuidaplus.api.common.MessageResponse;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {
  private final AccountDeletionService service;

  public AccountController(AccountDeletionService service) { this.service = service; }

  @DeleteMapping
  public MessageResponse delete(@Valid @RequestBody DeleteAccountRequest request) {
    return service.deleteAccount(AuthenticatedUser.id(), request.confirmationToken());
  }
}
