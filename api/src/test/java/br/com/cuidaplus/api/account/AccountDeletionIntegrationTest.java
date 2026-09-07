package br.com.cuidaplus.api.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.cuidaplus.api.auth.AuthService;
import br.com.cuidaplus.api.auth.dto.LoginRequest;
import br.com.cuidaplus.api.email.EmailService;
import br.com.cuidaplus.api.profile.Parentesco;
import br.com.cuidaplus.api.profile.PreferenciaContato;
import br.com.cuidaplus.api.profile.ResponsibleApprovalStatus;
import br.com.cuidaplus.api.profile.ResponsibleProfile;
import br.com.cuidaplus.api.profile.ResponsibleProfileRepository;
import br.com.cuidaplus.api.user.AccountStatus;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserRepository;
import br.com.cuidaplus.api.user.UserType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
  "DATABASE_URL=jdbc:h2:mem:account-deletion-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
  "DATABASE_USERNAME=sa", "DATABASE_PASSWORD=sa",
  "JWT_SECRET=01234567890123456789012345678901",
  "spring.datasource.driver-class-name=org.h2.Driver",
  "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class AccountDeletionIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired AuthService authService;
  @Autowired UserRepository users;
  @Autowired ResponsibleProfileRepository responsibles;
  @Autowired AccountDeletionAuditRepository audits;
  @Autowired PasswordEncoder passwords;
  @MockBean EmailService emails;

  @Test
  void fullFlowAnonymizesAndRejectsPreviousJwtAndLogin() throws Exception {
    String originalEmail = "exclusao@example.com";
    User user = new User();
    user.setFullName("Titular dos Dados");
    user.setEmail(originalEmail);
    user.setPhone("11999999999");
    user.setMaiorDeIdadeConfirmado(true);
    user.setUserType(UserType.RESPONSAVEL);
    user.setPasswordHash(passwords.encode("secret123"));
    user.setAccountStatus(AccountStatus.ATIVO);
    users.save(user);
    ResponsibleProfile profile = new ResponsibleProfile();
    profile.setUser(user);
    profile.setParentesco(Parentesco.FILHO);
    profile.setPreferenciaContato(PreferenciaContato.WHATSAPP);
    profile.setSituacaoAprovacao(ResponsibleApprovalStatus.APROVADO);
    responsibles.save(profile);

    String jwt = authService.login(new LoginRequest(originalEmail, "secret123")).token();
    String reauthenticationJson = mvc.perform(post("/api/auth/reauthenticate")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"senha\":\"secret123\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.expiresInSeconds").value(300))
      .andReturn().getResponse().getContentAsString();
    JsonNode reauthentication = objectMapper.readTree(reauthenticationJson);

    mvc.perform(delete("/api/account")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(java.util.Map.of(
          "confirmationToken", reauthentication.get("confirmationToken").asText()
        ))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.message").value("Conta excluída com sucesso."));

    User deleted = users.findById(user.getId()).orElseThrow();
    assertThat(deleted.getAccountStatus()).isEqualTo(AccountStatus.EXCLUIDO);
    assertThat(deleted.getPhone()).isNull();
    assertThat(audits.findAll()).singleElement().satisfies(audit -> {
      assertThat(audit.getUserReference()).isEqualTo(user.getId());
      assertThat(audit.getResult()).isEqualTo("SUCESSO");
    });

    mvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.code").value("ACCOUNT_DELETED"));
    mvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"exclusao@example.com\",\"password\":\"secret123\"}"))
      .andExpect(status().isUnauthorized());
  }
}
