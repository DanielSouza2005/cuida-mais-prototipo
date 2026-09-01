package br.com.cuidaplus.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.cuidaplus.api.auth.dto.LoginRequest;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.email.EmailService;
import br.com.cuidaplus.api.profile.CaregiverApprovalStatus;
import br.com.cuidaplus.api.profile.CaregiverProfile;
import br.com.cuidaplus.api.profile.CaregiverProfileRepository;
import br.com.cuidaplus.api.profile.Parentesco;
import br.com.cuidaplus.api.profile.PreferenciaContato;
import br.com.cuidaplus.api.profile.ResponsibleApprovalStatus;
import br.com.cuidaplus.api.profile.ResponsibleProfile;
import br.com.cuidaplus.api.profile.ResponsibleProfileRepository;
import br.com.cuidaplus.api.user.AccountStatus;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserRepository;
import br.com.cuidaplus.api.user.UserType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(properties = {
  "DATABASE_URL=jdbc:h2:mem:access-status-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
  "DATABASE_USERNAME=sa", "DATABASE_PASSWORD=sa",
  "JWT_SECRET=01234567890123456789012345678901",
  "spring.datasource.driver-class-name=org.h2.Driver",
  "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthServiceAccessStatusTest {
  @Autowired AuthService authService;
  @Autowired UserRepository users;
  @Autowired CaregiverProfileRepository caregivers;
  @Autowired ResponsibleProfileRepository responsibles;
  @Autowired PasswordEncoder passwords;
  @MockBean EmailService emails;

  @BeforeEach void clean() { caregivers.deleteAll(); responsibles.deleteAll(); users.deleteAll(); }

  @Test void blockedAccountDoesNotReceiveToken() {
    User user = user("blocked@example.com", UserType.RESPONSAVEL);
    user.setAccountStatus(AccountStatus.BLOQUEADO); users.save(user);
    assertThatThrownBy(() -> authService.login(new LoginRequest(user.getEmail(), "secret123")))
      .isInstanceOfSatisfying(BusinessException.class, error -> assertThat(error.getCode()).isEqualTo("ACCOUNT_BLOCKED"));
  }

  @Test void pendingCaregiverDoesNotReceiveToken() {
    User user = user("pending@example.com", UserType.CUIDADOR);
    CaregiverProfile profile = new CaregiverProfile(); profile.setUser(user);
    profile.setSituacaoAprovacao(CaregiverApprovalStatus.PENDENTE); caregivers.save(profile);
    assertThatThrownBy(() -> authService.login(new LoginRequest(user.getEmail(), "secret123")))
      .isInstanceOfSatisfying(BusinessException.class, error -> assertThat(error.getCode()).isEqualTo("CAREGIVER_PENDING_APPROVAL"));
  }

  @Test void pendingResponsibleDoesNotReceiveToken() {
    User user = user("responsible-pending@example.com", UserType.RESPONSAVEL);
    ResponsibleProfile profile = responsible(user, ResponsibleApprovalStatus.PENDENTE);
    responsibles.save(profile);
    assertThatThrownBy(() -> authService.login(new LoginRequest(user.getEmail(), "secret123")))
      .isInstanceOfSatisfying(BusinessException.class, error -> assertThat(error.getCode()).isEqualTo("RESPONSIBLE_PENDING_APPROVAL"));
  }

  @Test void approvedResponsibleReceivesTokenAndLastLoginIsRecorded() {
    User user = user("responsible-approved@example.com", UserType.RESPONSAVEL);
    responsibles.save(responsible(user, ResponsibleApprovalStatus.APROVADO));
    var response = authService.login(new LoginRequest(user.getEmail(), "secret123"));
    assertThat(response.token()).isNotBlank();
    assertThat(users.findById(user.getId()).orElseThrow().getUltimoLoginEm()).isNotNull();
  }

  @Test void approvedCaregiverReceivesTokenAndLastLoginIsRecorded() {
    User user = user("approved@example.com", UserType.CUIDADOR);
    CaregiverProfile profile = new CaregiverProfile(); profile.setUser(user);
    profile.setSituacaoAprovacao(CaregiverApprovalStatus.APROVADO); caregivers.save(profile);
    var response = authService.login(new LoginRequest(user.getEmail(), "secret123"));
    assertThat(response.token()).isNotBlank();
    assertThat(users.findById(user.getId()).orElseThrow().getUltimoLoginEm()).isNotNull();
  }

  private User user(String email, UserType type) {
    User user = new User(); user.setFullName("Pessoa Teste"); user.setCpf(type == UserType.CUIDADOR ? "12345678901" : "10987654321");
    user.setEmail(email); user.setBirthDate(LocalDate.of(1990,1,1)); user.setUserType(type);
    user.setPasswordHash(passwords.encode("secret123")); return users.save(user);
  }

  private ResponsibleProfile responsible(User user, ResponsibleApprovalStatus status) {
    ResponsibleProfile profile = new ResponsibleProfile(); profile.setUser(user); profile.setParentesco(Parentesco.FILHO);
    profile.setPreferenciaContato(PreferenciaContato.WHATSAPP); profile.setSituacaoAprovacao(status); return profile;
  }
}
