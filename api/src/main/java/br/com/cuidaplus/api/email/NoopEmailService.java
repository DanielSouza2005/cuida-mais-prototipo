package br.com.cuidaplus.api.email;

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
}
