package br.com.cuidaplus.api.security;

import br.com.cuidaplus.api.common.BusinessException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthenticatedUser {

  private AuthenticatedUser() {}

  public static UUID id() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof UUID userId)) {
      throw new BusinessException("Usuário não autenticado.", HttpStatus.UNAUTHORIZED);
    }

    return userId;
  }
}
