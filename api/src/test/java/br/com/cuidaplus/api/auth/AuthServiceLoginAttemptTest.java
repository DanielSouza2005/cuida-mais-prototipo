package br.com.cuidaplus.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.cuidaplus.api.audit.CriticalActionAuditRepository;
import br.com.cuidaplus.api.auth.dto.LoginRequest;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.email.EmailService;
import br.com.cuidaplus.api.profile.Parentesco;
import br.com.cuidaplus.api.profile.PreferenciaContato;
import br.com.cuidaplus.api.profile.ResponsibleApprovalStatus;
import br.com.cuidaplus.api.profile.ResponsibleProfile;
import br.com.cuidaplus.api.profile.ResponsibleProfileRepository;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserRepository;
import br.com.cuidaplus.api.user.UserType;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(properties = {
  "DATABASE_URL=jdbc:h2:mem:login-attempt-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
  "DATABASE_USERNAME=sa", "DATABASE_PASSWORD=sa",
  "JWT_SECRET=01234567890123456789012345678901",
  "LOGIN_LOCKOUT_MINUTES=15",
  "spring.datasource.driver-class-name=org.h2.Driver",
  "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthServiceLoginAttemptTest {
  @Autowired AuthService authService;
  @Autowired UserRepository users;
  @Autowired ResponsibleProfileRepository responsibles;
  @Autowired CriticalActionAuditRepository audits;
  @Autowired PasswordEncoder passwords;
  @MockBean EmailService emails;

  @BeforeEach void clean() {
    audits.deleteAll();
    responsibles.deleteAll();
    users.deleteAll();
  }

  @Test void fifthInvalidPasswordTemporarilyLocksLogin() {
    User user = approvedUser("locked@example.com");

    for (int attempt = 1; attempt < 5; attempt++) {
      assertInvalidPassword(user.getEmail(), HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }
    assertInvalidPassword(user.getEmail(), HttpStatus.TOO_MANY_REQUESTS, "LOGIN_TEMPORARILY_LOCKED");

    User stored = users.findById(user.getId()).orElseThrow();
    assertThat(stored.getTentativasLoginInvalidas()).isEqualTo(5);
    assertThat(stored.getLoginBloqueadoAte()).isAfter(Instant.now());
    assertThatThrownBy(() -> authService.login(new LoginRequest(user.getEmail(), "secret123")))
      .isInstanceOfSatisfying(BusinessException.class, error -> {
        assertThat(error.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(error.getCode()).isEqualTo("LOGIN_TEMPORARILY_LOCKED");
      });
  }

  @Test void successfulLoginClearsPreviousFailures() {
    User user = approvedUser("success@example.com");
    assertInvalidPassword(user.getEmail(), HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    assertInvalidPassword(user.getEmail(), HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");

    assertThat(authService.login(new LoginRequest(user.getEmail(), "secret123")).token()).isNotBlank();
    User stored = users.findById(user.getId()).orElseThrow();
    assertThat(stored.getTentativasLoginInvalidas()).isZero();
    assertThat(stored.getLoginBloqueadoAte()).isNull();
  }

  @Test void expiredLockAllowsLoginAndClearsFailures() {
    User user = approvedUser("expired@example.com");
    user.setTentativasLoginInvalidas(5);
    user.setLoginBloqueadoAte(Instant.now().minusSeconds(1));
    users.saveAndFlush(user);

    assertThat(authService.login(new LoginRequest(user.getEmail(), "secret123")).token()).isNotBlank();
    User stored = users.findById(user.getId()).orElseThrow();
    assertThat(stored.getTentativasLoginInvalidas()).isZero();
    assertThat(stored.getLoginBloqueadoAte()).isNull();
  }

  private void assertInvalidPassword(String email, HttpStatus status, String code) {
    assertThatThrownBy(() -> authService.login(new LoginRequest(email, "wrong-password")))
      .isInstanceOfSatisfying(BusinessException.class, error -> {
        assertThat(error.getStatus()).isEqualTo(status);
        assertThat(error.getCode()).isEqualTo(code);
      });
  }

  private User approvedUser(String email) {
    User user = new User();
    user.setFullName("Pessoa Teste");
    user.setEmail(email);
    user.setMaiorDeIdadeConfirmado(true);
    user.setUserType(UserType.RESPONSAVEL);
    user.setPasswordHash(passwords.encode("secret123"));
    user = users.save(user);

    ResponsibleProfile profile = new ResponsibleProfile();
    profile.setUser(user);
    profile.setParentesco(Parentesco.FILHO);
    profile.setPreferenciaContato(PreferenciaContato.WHATSAPP);
    profile.setSituacaoAprovacao(ResponsibleApprovalStatus.APROVADO);
    responsibles.save(profile);
    return user;
  }
}
