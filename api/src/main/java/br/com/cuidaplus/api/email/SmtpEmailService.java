package br.com.cuidaplus.api.email;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class SmtpEmailService implements EmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SmtpEmailService.class);

  private final JavaMailSender mailSender;
  private final String from;

  public SmtpEmailService(
    JavaMailSender mailSender,
    @Value("${app.mail.from}") String from
  ) {
    this.mailSender = mailSender;
    this.from = from.trim();
  }

  @Override
  @Async
  public void sendPasswordResetEmail(String to, String resetLink) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
      helper.setFrom(from);
      helper.setReplyTo(from);
      helper.setTo(to);
      helper.setSubject("Redefinicao de senha - Cuidar+");
      helper.setText(
        "Recebemos uma solicitacao para redefinir sua senha no Cuidar+.\n\n" +
        "Use o link abaixo para criar uma nova senha:\n" +
        resetLink +
        "\n\nSe voce nao solicitou essa alteracao, ignore este e-mail.",
        false
      );
      mailSender.send(message);
    } catch (MailException exception) {
      LOGGER.error("Nao foi possivel enviar o e-mail de recuperacao. Verifique as configuracoes SMTP.");
    } catch (Exception exception) {
      LOGGER.error("Nao foi possivel preparar o e-mail de recuperacao.");
    }
  }
}
