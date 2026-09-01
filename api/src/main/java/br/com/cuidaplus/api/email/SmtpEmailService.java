package br.com.cuidaplus.api.email;

import br.com.cuidaplus.api.profile.CaregiverApprovalStatus;
import br.com.cuidaplus.api.profile.ResponsibleApprovalStatus;
import br.com.cuidaplus.api.user.AccountStatus;

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
  public void sendPasswordResetEmail(String to, String resetLink, String fallbackWebLink, long expirationMinutes) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(
        message,
        MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
        "UTF-8"
      );
      helper.setFrom(from);
      helper.setReplyTo(from);
      helper.setTo(to);
      helper.setSubject("Redefinição de senha - Cuidar+");
      helper.setText(
        buildPasswordResetText(expirationMinutes),
        buildPasswordResetHtml(resetLink, fallbackWebLink, expirationMinutes)
      );
      mailSender.send(message);
    } catch (MailException exception) {
      LOGGER.error("Não foi possível enviar o e-mail de recuperação. Verifique as configurações SMTP.", exception);
    } catch (Exception exception) {
      LOGGER.error("Não foi possível preparar o e-mail de recuperação.", exception);
    }
  }

  @Override
  public boolean sendAttendanceReportEmail(String to, AttendanceReportEmailMessage report) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
      helper.setFrom(from);
      helper.setReplyTo(from);
      helper.setTo(to);
      helper.setSubject("Relatório de atendimento disponível — Cuidar+");
      helper.setText(buildAttendanceReportText(report));
      mailSender.send(message);
      return true;
    } catch (Exception exception) {
      LOGGER.error("Não foi possível enviar o e-mail do relatório de atendimento. Verifique as configurações SMTP.", exception);
      return false;
    }
  }

  @Override @Async
  public void sendCaregiverReviewEmail(String to, String caregiverName, CaregiverApprovalStatus status, String reason) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
      helper.setFrom(from); helper.setReplyTo(from); helper.setTo(to);
      helper.setSubject(switch (status) {
        case APROVADO -> "Cadastro de cuidador aprovado — Cuidar+";
        case REPROVADO -> "Resultado da análise do cadastro — Cuidar+";
        case BLOQUEADO -> "Perfil de cuidador bloqueado — Cuidar+";
        case PENDENTE -> "Atualização do cadastro de cuidador — Cuidar+";
      });
      helper.setText(switch (status) {
        case APROVADO -> "Olá, " + caregiverName + ".\n\nSeu cadastro de cuidador foi aprovado.\n\nA partir de agora, você já pode acessar o aplicativo Cuidar+ e utilizar as funcionalidades disponíveis para cuidadores.\n\nAtenciosamente,\nEquipe Cuidar+";
        case REPROVADO -> "Olá, " + caregiverName + ".\n\nSeu cadastro de cuidador foi analisado e, no momento, não foi aprovado.\n\nMotivo informado:\n" + reason + "\n\nRevise as informações e entre em contato com o suporte caso tenha dúvidas.\n\nAtenciosamente,\nEquipe Cuidar+";
        case BLOQUEADO -> "Olá, " + caregiverName + ".\n\nSeu perfil profissional de cuidador foi bloqueado.\n\nMotivo informado:\n" + reason + "\n\nEntre em contato com o suporte caso tenha dúvidas.\n\nAtenciosamente,\nEquipe Cuidar+";
        case PENDENTE -> "Olá, " + caregiverName + ".\n\nSeu cadastro de cuidador está pendente de análise.\n\nAtenciosamente,\nEquipe Cuidar+";
      });
      mailSender.send(message);
    } catch (Exception exception) {
      LOGGER.error("Não foi possível enviar o resultado da análise do cuidador.", exception);
    }
  }

  @Override @Async
  public void sendResponsibleReviewEmail(String to, String responsibleName, ResponsibleApprovalStatus status, String reason) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
      helper.setFrom(from); helper.setReplyTo(from); helper.setTo(to);
      helper.setSubject(switch (status) {
        case APROVADO -> "Cadastro de responsável aprovado — Cuidar+";
        case REPROVADO -> "Resultado da análise do cadastro — Cuidar+";
        case BLOQUEADO -> "Cadastro de responsável bloqueado — Cuidar+";
        case PENDENTE -> "Atualização do cadastro de responsável — Cuidar+";
      });
      helper.setText(switch (status) {
        case APROVADO -> "Olá, " + responsibleName + ".\n\nSeu cadastro de responsável foi aprovado.\n\nA partir de agora, você já pode acessar o aplicativo Cuidar+ e utilizar as funcionalidades disponíveis para responsáveis.\n\nAtenciosamente,\nEquipe Cuidar+";
        case REPROVADO -> "Olá, " + responsibleName + ".\n\nSeu cadastro de responsável foi analisado e, no momento, não foi aprovado.\n\nMotivo informado:\n" + reason + "\n\nRevise as informações e entre em contato com o suporte caso tenha dúvidas.\n\nAtenciosamente,\nEquipe Cuidar+";
        case BLOQUEADO -> "Olá, " + responsibleName + ".\n\nSeu cadastro de responsável foi bloqueado.\n\nMotivo informado:\n" + reason + "\n\nEntre em contato com o suporte caso tenha dúvidas.\n\nAtenciosamente,\nEquipe Cuidar+";
        case PENDENTE -> "Olá, " + responsibleName + ".\n\nSeu cadastro de responsável está pendente de análise.\n\nAtenciosamente,\nEquipe Cuidar+";
      });
      mailSender.send(message);
    } catch (Exception exception) {
      LOGGER.error("Não foi possível enviar o resultado da análise do responsável.", exception);
    }
  }

  private String buildAttendanceReportText(AttendanceReportEmailMessage report) {
    return "Olá, " + report.responsibleName() + ".\n\n" +
      "O relatório de atendimento de " + report.assistedPersonName() + ", realizado em " + report.attendanceDate() +
      ", foi finalizado pelo cuidador " + report.caregiverName() + ".\n\n" +
      "Início registrado: " + report.startedAt() + "\nEncerramento registrado: " + report.endedAt() + "\n\n" +
      "Resumo do atendimento:\n" + report.finalText() + "\n\n" +
      "Anotações de enfermagem:\n" + report.nursingNotes() + "\n\n" +
      "Para consultar o relatório completo, o histórico, os anexos e os detalhes do atendimento, acesse o aplicativo Cuidar+.";
  }

  private String buildPasswordResetText(long expirationMinutes) {
    return "Recebemos uma solicitação para redefinir a senha da sua conta no Cuidar+.\n\n" +
      "Use o botão Redefinir senha no e-mail em HTML para criar uma nova senha.\n\n" +
      "Este link é válido por " + expirationMinutes + " minutos. " +
      "Se você não solicitou a redefinição de senha, ignore este e-mail.\n\n" +
      "Cuidar+ - Organizacao e apoio ao cuidado domiciliar.";
  }

  private String buildPasswordResetHtml(String resetLink, String fallbackWebLink, long expirationMinutes) {
    String buttonLink = resolveEmailSafeButtonLink(resetLink, fallbackWebLink);
    String safeButtonLink = escapeHtml(buttonLink);
    String safeFallbackWebLink = escapeHtml(fallbackWebLink);

    return "<!doctype html>" +
      "<html><body style=\"margin:0;padding:0;background:#f6f8fb;font-family:Arial,sans-serif;color:#172033;\">" +
      "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#f6f8fb;padding:24px 0;\">" +
      "<tr><td align=\"center\">" +
      "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:560px;background:#ffffff;border:1px solid #e5eaf0;border-radius:8px;overflow:hidden;\">" +
      "<tr><td style=\"padding:28px 32px 8px 32px;\">" +
      "<div style=\"font-size:14px;font-weight:700;color:#208aef;margin-bottom:16px;\">Cuidar+</div>" +
      "<h1 style=\"font-size:24px;line-height:32px;margin:0 0 12px 0;color:#172033;\">Redefinição de senha</h1>" +
      "<p style=\"font-size:15px;line-height:24px;margin:0;color:#536073;\">Recebemos uma solicitação para redefinir a senha da sua conta no Cuidar+.</p>" +
      "</td></tr>" +
      "<tr><td style=\"padding:24px 32px 12px 32px;\">" +
      "<a href=\"" + safeButtonLink + "\" style=\"display:inline-block;background:#208aef;color:#ffffff;text-decoration:none;font-size:15px;font-weight:700;padding:13px 22px;border-radius:6px;\">Redefinir senha</a>" +
      "</td></tr>" +
      "<tr><td style=\"padding:12px 32px 24px 32px;\">" +
      "<p style=\"font-size:13px;line-height:21px;margin:0 0 10px 0;color:#536073;\">Este link é válido por " + expirationMinutes + " minutos. Se você não solicitou a redefinição de senha, ignore este e-mail.</p>" +
      "<p style=\"font-size:13px;line-height:21px;margin:0;color:#536073;\">Caso o botão não funcione no computador, <a href=\"" + safeFallbackWebLink + "\" style=\"color:#208aef;text-decoration:none;font-weight:700;\">abra a versão web</a> ou solicite um novo link pelo aplicativo.</p>" +
      "</td></tr>" +
      "<tr><td style=\"padding:18px 32px;background:#f9fbfd;border-top:1px solid #e5eaf0;color:#748094;font-size:12px;line-height:18px;\">" +
      "Cuidar+ - Organizacao e apoio ao cuidado domiciliar." +
      "</td></tr>" +
      "</table>" +
      "</td></tr>" +
      "</table>" +
      "</body></html>";
  }

  private String resolveEmailSafeButtonLink(String resetLink, String fallbackWebLink) {
    if (isHttpLink(resetLink)) {
      return resetLink;
    }

    return fallbackWebLink;
  }

  private boolean isHttpLink(String value) {
    String normalizedValue = value == null ? "" : value.trim().toLowerCase();
    return normalizedValue.startsWith("https://") || normalizedValue.startsWith("http://");
  }

  private String escapeHtml(String value) {
    return value
      .replace("&", "&amp;")
      .replace("\"", "&quot;")
      .replace("<", "&lt;")
      .replace(">", "&gt;");
  }

  @Override @Async
  public void sendAccountStatusEmail(String to, String userName, AccountStatus status, String reason) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
      helper.setFrom(from); helper.setReplyTo(from); helper.setTo(to);
      boolean blocked = status == AccountStatus.BLOQUEADO;
      helper.setSubject(blocked ? "Conta bloqueada — Cuidar+" : "Conta desbloqueada — Cuidar+");
      helper.setText(blocked
        ? "Olá, " + userName + ".\n\nSua conta foi bloqueada.\n\nMotivo informado:\n" + reason + "\n\nEntre em contato com o suporte caso tenha dúvidas.\n\nAtenciosamente,\nEquipe Cuidar+"
        : "Olá, " + userName + ".\n\nSua conta foi desbloqueada. A liberação das funcionalidades também depende da situação de aprovação do seu perfil.\n\nAtenciosamente,\nEquipe Cuidar+");
      mailSender.send(message);
    } catch (Exception exception) {
      LOGGER.error("Não foi possível enviar a atualização da situação da conta.", exception);
    }
  }
}
