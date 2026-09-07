package br.com.cuidaplus.api.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.cuidaplus.api.email.EmailService;
import br.com.cuidaplus.api.profile.CaregiverApprovalStatus;
import br.com.cuidaplus.api.profile.CaregiverProfile;
import br.com.cuidaplus.api.profile.CaregiverProfileRepository;
import br.com.cuidaplus.api.profile.FormacaoCuidador;
import br.com.cuidaplus.api.security.TokenService;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserRepository;
import br.com.cuidaplus.api.user.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
  "DATABASE_URL=jdbc:h2:mem:admin-authorization-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
  "DATABASE_USERNAME=sa", "DATABASE_PASSWORD=sa",
  "JWT_SECRET=01234567890123456789012345678901",
  "spring.datasource.driver-class-name=org.h2.Driver",
  "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class AdminAuthorizationTest {
  @Autowired MockMvc mvc; @Autowired UserRepository users; @Autowired CaregiverProfileRepository caregivers;
  @Autowired TokenService tokens;
  @MockBean EmailService emails;
  User administrator; User responsible;

  @BeforeEach void setup(){
    caregivers.deleteAll(); users.deleteAll(); administrator=save("admin@example.com",UserType.ADMIN);
    responsible=save("responsible@example.com",UserType.RESPONSAVEL);
  }

  @Test void commonUserCannotAccessAdministrativeEndpoint() throws Exception {
    mvc.perform(get("/api/admin/users").header("Authorization","Bearer "+tokens.generate(responsible.getId())))
      .andExpect(status().isForbidden());
    mvc.perform(get("/api/admin/caregivers").param("status","APROVADO")
        .header("Authorization","Bearer "+tokens.generate(responsible.getId())))
      .andExpect(status().isForbidden());
  }

  @Test void administratorCanAccessAdministrativeEndpoint() throws Exception {
    mvc.perform(get("/api/admin/users").header("Authorization","Bearer "+tokens.generate(administrator.getId())))
      .andExpect(status().isOk());
    mvc.perform(get("/api/admin/dashboard").header("Authorization","Bearer "+tokens.generate(administrator.getId())))
      .andExpect(status().isOk());
  }

  @Test void administratorCanListCaregiversWithAllSupportedApprovalFilters() throws Exception {
    saveCaregiver("pending@example.com",CaregiverApprovalStatus.PENDENTE);
    saveCaregiver("approved@example.com",CaregiverApprovalStatus.APROVADO);
    saveCaregiver("rejected@example.com",CaregiverApprovalStatus.REPROVADO);
    saveCaregiver("blocked@example.com",CaregiverApprovalStatus.BLOQUEADO);
    String authorization="Bearer "+tokens.generate(administrator.getId());

    mvc.perform(get("/api/admin/caregivers").header("Authorization",authorization))
      .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(4));
    mvc.perform(get("/api/admin/caregivers").param("status","PENDENTE").header("Authorization",authorization))
      .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
      .andExpect(jsonPath("$.content[0].status").value("PENDENTE"));
    mvc.perform(get("/api/admin/caregivers").param("status","APROVADO").header("Authorization",authorization))
      .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
      .andExpect(jsonPath("$.content[0].status").value("APROVADO"))
      .andExpect(jsonPath("$.content[0].formations[0]").value("CURSO_CUIDADOR_IDOSOS"));
    mvc.perform(get("/api/admin/caregivers").param("status","REPROVADO").header("Authorization",authorization))
      .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
      .andExpect(jsonPath("$.content[0].status").value("REPROVADO"));
    mvc.perform(get("/api/admin/caregivers").param("status","BLOQUEADO").header("Authorization",authorization))
      .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
      .andExpect(jsonPath("$.content[0].status").value("BLOQUEADO"));
  }

  @Test void invalidCaregiverApprovalFilterReturnsControlledBadRequest() throws Exception {
    mvc.perform(get("/api/admin/caregivers").param("status","INVALIDO")
        .header("Authorization","Bearer "+tokens.generate(administrator.getId())))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("INVALID_QUERY_PARAMETER"))
      .andExpect(jsonPath("$.message").value("Situação de aprovação inválida."))
      .andExpect(jsonPath("$.fields.status").value("Situação de aprovação inválida."));
  }

  @Test void onlyAdministratorCanUnblockABlockedAccount() throws Exception {
    responsible.setAccountStatus(br.com.cuidaplus.api.user.AccountStatus.BLOQUEADO); users.save(responsible);
    mvc.perform(patch("/api/admin/users/{id}/unblock",responsible.getId())
        .header("Authorization","Bearer "+tokens.generate(administrator.getId())))
      .andExpect(status().isOk()).andExpect(jsonPath("$.accountStatus").value("ATIVO"));
    mvc.perform(patch("/api/admin/users/{id}/unblock",responsible.getId())
        .header("Authorization","Bearer "+tokens.generate(administrator.getId())))
      .andExpect(status().isConflict());
    mvc.perform(patch("/api/admin/users/{id}/unblock",administrator.getId())
        .header("Authorization","Bearer "+tokens.generate(responsible.getId())))
      .andExpect(status().isForbidden());
  }

  private User save(String email,UserType type){User user=new User();user.setFullName("Pessoa Teste");user.setEmail(email);user.setMaiorDeIdadeConfirmado(true);user.setPasswordHash("not-used");user.setUserType(type);return users.save(user);}
  private CaregiverProfile saveCaregiver(String email,CaregiverApprovalStatus status){User caregiver=save(email,UserType.CUIDADOR);CaregiverProfile profile=new CaregiverProfile();profile.setUser(caregiver);profile.getFormacoes().add(FormacaoCuidador.CURSO_CUIDADOR_IDOSOS);profile.setSituacaoAprovacao(status);return caregivers.save(profile);}
}
