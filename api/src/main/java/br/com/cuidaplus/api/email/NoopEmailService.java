package br.com.cuidaplus.api.email;

import br.com.cuidaplus.api.profile.CaregiverApprovalStatus;
import br.com.cuidaplus.api.profile.ResponsibleApprovalStatus;
import br.com.cuidaplus.api.user.AccountStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class NoopEmailService implements EmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(NoopEmailService.class);

  @Override
  @Async
  public void sendPasswordResetEmail(String to, String resetLink, String fallbackWebLink, long expirationMinutes) {
    LOGGER.info("Envio de e-mail desabilitado. Configure app.mail.enabled=true para enviar recuperação de senha.");
  }

  @Override
  public boolean sendAttendanceReportEmail(String to, AttendanceReportEmailMessage report) {
    LOGGER.info("Envio de e-mail desabilitado. O relatório permanece disponível no aplicativo.");
    return false;
  }

  @Override @Async
  public void sendCaregiverReviewEmail(String to, String caregiverName, CaregiverApprovalStatus status, String reason) {
    LOGGER.info("Envio de e-mail desabilitado. Resultado da análise do cuidador: {}.", status);
  }

  @Override @Async
  public void sendResponsibleReviewEmail(String to, String responsibleName, ResponsibleApprovalStatus status, String reason) {
    LOGGER.info("Envio de e-mail desabilitado. Resultado da análise do responsável: {}.", status);
  }

  @Override @Async
  public void sendAccountStatusEmail(String to, String userName, AccountStatus status, String reason) {
    LOGGER.info("Envio de e-mail desabilitado. Nova situação da conta: {}.", status);
  }
}
