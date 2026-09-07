package br.com.cuidaplus.api.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserType;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class AuditServiceTest {
  private final CriticalActionAuditRepository repository = mock(CriticalActionAuditRepository.class);
  private final AuditService service = new AuditService(repository);

  @AfterEach void cleanup() { RequestContextHolder.resetRequestAttributes(); }

  @Test void savesOnlyMinimalControlledDataAndTechnicalMetadata() {
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.2");
    request.addHeader("User-Agent", "CuidarPlus-Test/1.0\r\nignored");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    User actor = new User(); ReflectionTestUtils.setField(actor, "id", UUID.randomUUID()); actor.setUserType(UserType.ADMIN);

    CriticalActionAudit saved = service.success(AuditAction.USUARIO_BLOQUEADO, AuditCategory.ADMINISTRATIVO, actor,
      "usuario", UUID.randomUUID(), AuditService.state("situacaoConta", "ATIVO"),
      AuditService.state("situacaoConta", "BLOQUEADO"), "CPF informado indevidamente");

    assertThat(saved.getAffectedEntityType()).isEqualTo("USUARIO");
    assertThat(saved.getPreviousSummary()).containsEntry("situacaoConta", "ATIVO");
    assertThat(saved.getReason()).isEqualTo("Conteúdo omitido por minimização de dados.");
    assertThat(saved.getIp()).isEqualTo("203.0.113.10");
    assertThat(saved.getSummarizedUserAgent()).doesNotContain("\r", "\n");
  }

  @Test void rejectsAnyAttemptToCopySensitiveFieldsIntoStateSummary() {
    User actor = new User(); actor.setUserType(UserType.ADMIN);
    assertThatThrownBy(() -> service.success(AuditAction.USUARIO_BLOQUEADO, AuditCategory.ADMINISTRATIVO, actor,
      "USUARIO", UUID.randomUUID(), Map.of("senha", "segredo"), null, null))
      .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("não permitido");
    verifyNoInteractions(repository);
  }

  @Test void omitsSensitiveValuesEvenWithoutAnExplicitFieldName() {
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    User actor = new User(); actor.setUserType(UserType.ADMIN);

    for (String reason : new String[]{"joao@example.com", "123.456.789-00", "(11) 99999-8888", "01310-100", "evidencia.jpg"}) {
      CriticalActionAudit saved = service.success(AuditAction.USUARIO_BLOQUEADO, AuditCategory.ADMINISTRATIVO,
        actor, "USUARIO", UUID.randomUUID(), null, null, reason);
      assertThat(saved.getReason()).isEqualTo("Conteúdo omitido por minimização de dados.");
    }
  }
}
