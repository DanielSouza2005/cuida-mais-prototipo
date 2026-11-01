package br.com.cuidaplus.api.auth;

import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginAttemptService {

  static final int MAX_INVALID_ATTEMPTS = 5;

  private final UserRepository users;
  private final PasswordEncoder passwordEncoder;
  private final Duration lockDuration;

  public LoginAttemptService(
    UserRepository users,
    PasswordEncoder passwordEncoder,
    @Value("${app.login.lockout-minutes:15}") long lockoutMinutes
  ) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.lockDuration = Duration.ofMinutes(Math.max(lockoutMinutes, 1));
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = BusinessException.class)
  public UUID authenticate(String email, String rawPassword) {
    User user = users.findByEmailForUpdate(email)
      .orElseThrow(this::invalidCredentials);
    Instant now = Instant.now();

    if (user.getLoginBloqueadoAte() != null && user.getLoginBloqueadoAte().isAfter(now)) {
      throw temporarilyLocked();
    }
    if (user.getLoginBloqueadoAte() != null) {
      clearFailures(user);
    }

    if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
      int attempts = user.getTentativasLoginInvalidas() + 1;
      user.setTentativasLoginInvalidas(attempts);
      if (attempts >= MAX_INVALID_ATTEMPTS) {
        user.setLoginBloqueadoAte(now.plus(lockDuration));
        throw temporarilyLocked();
      }
      throw invalidCredentials();
    }

    clearFailures(user);
    return user.getId();
  }

  private void clearFailures(User user) {
    user.setTentativasLoginInvalidas(0);
    user.setLoginBloqueadoAte(null);
  }

  private BusinessException invalidCredentials() {
    return new BusinessException("E-mail ou senha inválidos.", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
  }

  private BusinessException temporarilyLocked() {
    return new BusinessException(
      "Muitas tentativas de login inválidas. Tente novamente mais tarde.",
      HttpStatus.TOO_MANY_REQUESTS,
      "LOGIN_TEMPORARILY_LOCKED"
    );
  }
}
