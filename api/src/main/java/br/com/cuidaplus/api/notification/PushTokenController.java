package br.com.cuidaplus.api.notification;

import br.com.cuidaplus.api.common.MessageResponse;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/push-tokens")
public class PushTokenController {
  private final PushTokenService service;

  public PushTokenController(PushTokenService service) { this.service = service; }

  @PostMapping
  public PushTokenResponse register(@Valid @RequestBody RegisterPushTokenRequest request) {
    return service.register(AuthenticatedUser.id(), request);
  }

  @DeleteMapping("/current")
  public MessageResponse disable(@Valid @RequestBody DisablePushTokenRequest request) {
    service.disable(AuthenticatedUser.id(), request.token());
    return new MessageResponse("Dispositivo removido das notificações.");
  }
}
