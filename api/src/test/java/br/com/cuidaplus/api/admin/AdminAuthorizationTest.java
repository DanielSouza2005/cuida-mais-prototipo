package br.com.cuidaplus.api.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.cuidaplus.api.email.EmailService;
import br.com.cuidaplus.api.security.TokenService;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserRepository;
import br.com.cuidaplus.api.user.UserType;
import java.time.LocalDate;
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
  @Autowired MockMvc mvc; @Autowired UserRepository users; @Autowired TokenService tokens;
  @MockBean EmailService emails;
  User administrator; User responsible;

  @BeforeEach void setup(){
    users.deleteAll(); administrator=save("admin@example.com","12345678901",UserType.ADMIN);
    responsible=save("responsible@example.com","10987654321",UserType.RESPONSAVEL);
  }

  @Test void commonUserCannotAccessAdministrativeEndpoint() throws Exception {
    mvc.perform(get("/api/admin/users").header("Authorization","Bearer "+tokens.generate(responsible.getId())))
      .andExpect(status().isForbidden());
  }

  @Test void administratorCanAccessAdministrativeEndpoint() throws Exception {
    mvc.perform(get("/api/admin/users").header("Authorization","Bearer "+tokens.generate(administrator.getId())))
      .andExpect(status().isOk());
    mvc.perform(get("/api/admin/dashboard").header("Authorization","Bearer "+tokens.generate(administrator.getId())))
      .andExpect(status().isOk());
  }

  private User save(String email,String cpf,UserType type){User user=new User();user.setFullName("Pessoa Teste");user.setCpf(cpf);user.setEmail(email);user.setBirthDate(LocalDate.of(1990,1,1));user.setPasswordHash("not-used");user.setUserType(type);return users.save(user);}
}
