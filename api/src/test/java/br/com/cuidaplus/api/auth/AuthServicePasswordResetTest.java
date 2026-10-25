package br.com.cuidaplus.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import br.com.cuidaplus.api.auth.dto.ForgotPasswordRequest;
import br.com.cuidaplus.api.auth.dto.ResetPasswordRequest;
import br.com.cuidaplus.api.auth.dto.VerifyPasswordResetCodeRequest;
import br.com.cuidaplus.api.audit.CriticalActionAuditRepository;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.email.EmailService;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserRepository;
import br.com.cuidaplus.api.user.UserType;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = {
  "DATABASE_URL=jdbc:h2:mem:reset-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
  "DATABASE_USERNAME=sa", "DATABASE_PASSWORD=sa",
  "JWT_SECRET=01234567890123456789012345678901",
  "PASSWORD_RESET_EXPIRATION_MINUTES=10",
  "spring.datasource.driver-class-name=org.h2.Driver",
  "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthServicePasswordResetTest {
  @Autowired AuthService authService;
  @Autowired UserRepository users;
  @Autowired PasswordResetTokenRepository resets;
  @Autowired PasswordEncoder passwordEncoder;
  @Autowired CriticalActionAuditRepository audits;
  @Autowired JdbcTemplate jdbc;
  @MockBean EmailService emails;

  @BeforeEach void setUp() {
    audits.deleteAll(); resets.deleteAll(); users.deleteAll();
  }

  @Test void requestCreatesHashedSixDigitCodeAndUsesGenericResponse() {
    createUser("maria@example.com", "old-pass");
    String message = authService.requestPasswordReset(new ForgotPasswordRequest("maria@example.com"), "origin-a").message();
    String code = capturedCode("maria@example.com");
    assertThat(message).contains("Se o e-mail estiver cadastrado");
    assertThat(code).matches("\\d{6}");
    assertThat(resets.findAll()).singleElement().satisfies(reset -> {
      assertThat(reset.getTokenHash()).doesNotContain(code);
      assertThat(passwordEncoder.matches(code, reset.getTokenHash())).isTrue();
      assertThat(reset.getExpiresAt()).isAfter(Instant.now().plusSeconds(9 * 60));
    });
  }

  @Test void unknownEmailDoesNotEnumerateAccount() {
    String message = authService.requestPasswordReset(new ForgotPasswordRequest("missing@example.com"), "origin-b").message();
    assertThat(message).contains("Se o e-mail estiver cadastrado");
    assertThat(resets.findAll()).isEmpty();
    verify(emails, never()).sendPasswordResetEmail(anyString(), anyString(), anyLong());
  }

  @Test void correctCodeIssuesRestrictedAuthorizationAndChangesPasswordOnce() {
    User user = createUser("maria@example.com", "old-pass");
    String code = requestCode("maria@example.com", "origin-c");
    String authorization = authService.verifyPasswordResetCode(new VerifyPasswordResetCodeRequest("maria@example.com", code)).authorization();
    authService.resetPassword(new ResetPasswordRequest(authorization, "new-pass"));
    assertThat(passwordEncoder.matches("new-pass", users.findById(user.getId()).orElseThrow().getPasswordHash())).isTrue();
    assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest(authorization, "other-pass")))
      .isInstanceOf(BusinessException.class).hasMessageContaining("utilizada");
  }

  @Test void invalidCodeIsBlockedOnFifthAttemptAndSixthRemainsBlocked() {
    createUser("maria@example.com", "old-pass");
    requestCode("maria@example.com", "origin-d");
    for (int attempt = 1; attempt < 5; attempt++) {
      assertThatThrownBy(() -> authService.verifyPasswordResetCode(new VerifyPasswordResetCodeRequest("maria@example.com", "999999")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("inválido");
    }
    assertThatThrownBy(() -> authService.verifyPasswordResetCode(new VerifyPasswordResetCodeRequest("maria@example.com", "999999")))
      .isInstanceOf(BusinessException.class).hasMessageContaining("Limite");
    assertThatThrownBy(() -> authService.verifyPasswordResetCode(new VerifyPasswordResetCodeRequest("maria@example.com", "999999")))
      .isInstanceOf(BusinessException.class);
    assertThat(resets.findAll().getFirst().getCodeAttempts()).isEqualTo(5);
  }

  @Test void expiredCodeAndResetWithoutVerificationAreRejected() {
    createUser("maria@example.com", "old-pass");
    String code = requestCode("maria@example.com", "origin-e");
    PasswordResetToken reset = resets.findAll().getFirst();
    reset.setExpiresAt(Instant.now().minusSeconds(1)); resets.save(reset);
    assertThatThrownBy(() -> authService.verifyPasswordResetCode(new VerifyPasswordResetCodeRequest("maria@example.com", code)))
      .isInstanceOf(BusinessException.class).hasMessageContaining("expirado");
    assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("not-authorized", "new-pass")))
      .isInstanceOf(BusinessException.class).hasMessageContaining("inválida");
  }

  @Test void immediateResendDoesNotCreateOrSendAnotherCode() {
    createUser("maria@example.com", "old-pass");
    requestCode("maria@example.com", "origin-f");
    authService.requestPasswordReset(new ForgotPasswordRequest("maria@example.com"), "origin-f");
    assertThat(resets.findAll()).hasSize(1);
    verify(emails).sendPasswordResetEmail(eq("maria@example.com"), anyString(), eq(10L));
  }

  @Test void resendAfterSixtySecondsInvalidatesPreviousCode() {
    createUser("maria@example.com", "old-pass");
    String previousCode = requestCode("maria@example.com", "origin-g");
    PasswordResetToken previousReset = resets.findAll().getFirst();
    jdbc.update("UPDATE usuario_token_redefinicao_senha SET criado_em = ? WHERE id = ?",
      Timestamp.from(Instant.now().minusSeconds(61)), previousReset.getId());

    String currentCode = requestCode("maria@example.com", "origin-g");

    assertThat(currentCode).isNotEqualTo(previousCode);
    assertThat(resets.findAll()).hasSize(2);
    assertThat(resets.findById(previousReset.getId()).orElseThrow().getInvalidatedAt()).isNotNull();
    assertThatThrownBy(() -> authService.verifyPasswordResetCode(
      new VerifyPasswordResetCodeRequest("maria@example.com", previousCode)))
      .isInstanceOf(BusinessException.class).hasMessageContaining("inválido");
    assertThat(authService.verifyPasswordResetCode(
      new VerifyPasswordResetCodeRequest("maria@example.com", currentCode)).authorization()).isNotBlank();
  }

  @Test void concurrentResetsConsumeTheAuthorizationOnlyOnce() throws Exception {
    createUser("maria@example.com", "old-pass");
    String code = requestCode("maria@example.com", "origin-h");
    String authorization = authService.verifyPasswordResetCode(
      new VerifyPasswordResetCodeRequest("maria@example.com", code)).authorization();
    CountDownLatch start = new CountDownLatch(1);
    Callable<Boolean> reset = () -> {
      start.await();
      try {
        authService.resetPassword(new ResetPasswordRequest(authorization, "new-pass"));
        return true;
      } catch (RuntimeException exception) {
        return false;
      }
    };

    try (var executor = Executors.newFixedThreadPool(2)) {
      var futures = List.of(executor.submit(reset), executor.submit(reset));
      start.countDown();
      assertThat(List.of(futures.get(0).get(), futures.get(1).get()))
        .containsExactlyInAnyOrder(true, false);
    }
  }

  private User createUser(String email, String password) {
    User user = new User(); user.setFullName("Maria Silva"); user.setEmail(email); user.setMaiorDeIdadeConfirmado(true);
    user.setUserType(UserType.RESPONSAVEL); user.setPasswordHash(passwordEncoder.encode(password));
    return users.save(user);
  }

  private String requestCode(String email, String origin) {
    authService.requestPasswordReset(new ForgotPasswordRequest(email), origin);
    return capturedCode(email);
  }

  private String capturedCode(String email) {
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(emails, atLeastOnce()).sendPasswordResetEmail(eq(email), captor.capture(), eq(10L));
    return captor.getAllValues().getLast();
  }
}
