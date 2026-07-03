package br.com.cuidaplus.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import br.com.cuidaplus.api.auth.dto.ForgotPasswordRequest;
import br.com.cuidaplus.api.auth.dto.ResetPasswordRequest;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.email.EmailService;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserRepository;
import br.com.cuidaplus.api.user.UserType;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(properties = {
  "DATABASE_URL=jdbc:h2:mem:reset-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
  "DATABASE_USERNAME=sa",
  "DATABASE_PASSWORD=sa",
  "JWT_SECRET=01234567890123456789012345678901",
  "PASSWORD_RESET_MOBILE_URL=cuidarplus://reset-password",
  "PASSWORD_RESET_WEB_URL=http://localhost:8081/reset-password",
  "PASSWORD_RESET_PREFER_MOBILE_LINK=true",
  "spring.datasource.driver-class-name=org.h2.Driver",
  "spring.flyway.enabled=false",
  "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthServicePasswordResetTest {

  private static final String FRONTEND_ORIGIN = "http://localhost:3000";

  @Autowired
  private AuthService authService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @MockBean
  private EmailService emailService;

  @BeforeEach
  void setUp() {
    passwordResetTokenRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void requestPasswordResetWithExistingEmailGeneratesTokenAndSendsEmail() {
    createUser("maria@example.com", "old-pass");

    authService.requestPasswordReset(new ForgotPasswordRequest("maria@example.com"), FRONTEND_ORIGIN);

    assertThat(passwordResetTokenRepository.findAll()).hasSize(1);
    ArgumentCaptor<String> primaryLinkCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> fallbackLinkCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService).sendPasswordResetEmail(eq("maria@example.com"), primaryLinkCaptor.capture(), fallbackLinkCaptor.capture(), eq(30L));
    assertThat(primaryLinkCaptor.getValue()).startsWith("cuidarplus://reset-password?token=");
    assertThat(fallbackLinkCaptor.getValue()).startsWith(FRONTEND_ORIGIN + "/reset-password?token=");
    assertThat(passwordResetTokenRepository.findAll().get(0).getTokenHash()).hasSize(64);
  }

  @Test
  void requestPasswordResetWithUnknownEmailDoesNotRevealUserExistence() {
    String message = authService.requestPasswordReset(new ForgotPasswordRequest("missing@example.com"), FRONTEND_ORIGIN).message();

    assertThat(message).contains("Se o e-mail estiver cadastrado");
    assertThat(passwordResetTokenRepository.findAll()).isEmpty();
    verify(emailService, never()).sendPasswordResetEmail(
      org.mockito.ArgumentMatchers.anyString(),
      org.mockito.ArgumentMatchers.anyString(),
      org.mockito.ArgumentMatchers.anyString(),
      anyLong()
    );
  }

  @Test
  void resetPasswordWithValidTokenChangesPasswordAndMarksTokenAsUsed() {
    User user = createUser("maria@example.com", "old-pass");
    String token = requestToken("maria@example.com");

    authService.resetPassword(new ResetPasswordRequest(token, "new-pass"));

    User updated = userRepository.findById(user.getId()).orElseThrow();
    assertThat(passwordEncoder.matches("new-pass", updated.getPasswordHash())).isTrue();
    assertThat(passwordResetTokenRepository.findAll().get(0).getUsedAt()).isNotNull();
  }

  @Test
  void resetPasswordRejectsInvalidToken() {
    assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("invalid-token", "new-pass")))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("invalido");
  }

  @Test
  void resetPasswordRejectsExpiredToken() {
    createUser("maria@example.com", "old-pass");
    String token = requestToken("maria@example.com");
    PasswordResetToken stored = passwordResetTokenRepository.findAll().get(0);
    stored.setExpiresAt(Instant.now().minusSeconds(1));
    passwordResetTokenRepository.save(stored);

    assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest(token, "new-pass")))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("expirado");
  }

  @Test
  void resetPasswordRejectsAlreadyUsedToken() {
    createUser("maria@example.com", "old-pass");
    String token = requestToken("maria@example.com");
    authService.resetPassword(new ResetPasswordRequest(token, "new-pass"));

    assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest(token, "another-pass")))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("ja utilizado");
  }

  @Test
  void secondRequestInvalidatesPreviousToken() {
    createUser("maria@example.com", "old-pass");
    String firstToken = requestToken("maria@example.com");
    requestToken("maria@example.com");

    assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest(firstToken, "new-pass")))
      .isInstanceOf(BusinessException.class)
      .hasMessageContaining("ja utilizado");
  }

  private User createUser(String email, String password) {
    User user = new User();
    user.setFullName("Maria Silva");
    user.setCpf("12345678901");
    user.setEmail(email);
    user.setBirthDate(LocalDate.of(1990, 1, 1));
    user.setUserType(UserType.RESPONSAVEL);
    user.setPasswordHash(passwordEncoder.encode(password));
    return userRepository.save(user);
  }

  private String requestToken(String email) {
    authService.requestPasswordReset(new ForgotPasswordRequest(email), FRONTEND_ORIGIN);
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(emailService, org.mockito.Mockito.atLeastOnce()).sendPasswordResetEmail(eq(email), captor.capture(), org.mockito.ArgumentMatchers.anyString(), anyLong());
    String link = captor.getAllValues().get(captor.getAllValues().size() - 1);
    return URI.create(link).getQuery().replace("token=", "");
  }
}
