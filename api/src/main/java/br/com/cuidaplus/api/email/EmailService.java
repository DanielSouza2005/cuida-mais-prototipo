package br.com.cuidaplus.api.email;

import br.com.cuidaplus.api.profile.CaregiverApprovalStatus;
import br.com.cuidaplus.api.profile.ResponsibleApprovalStatus;
import br.com.cuidaplus.api.user.AccountStatus;

public interface EmailService {

  void sendPasswordResetEmail(String to, String resetLink, String fallbackWebLink, long expirationMinutes);

  boolean sendAttendanceReportEmail(String to, AttendanceReportEmailMessage report);

  void sendCaregiverReviewEmail(String to, String caregiverName, CaregiverApprovalStatus status, String reason);

  void sendResponsibleReviewEmail(String to, String responsibleName, ResponsibleApprovalStatus status, String reason);

  void sendAccountStatusEmail(String to, String userName, AccountStatus status, String reason);
}
