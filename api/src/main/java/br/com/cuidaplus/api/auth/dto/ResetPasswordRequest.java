package br.com.cuidaplus.api.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

public class ResetPasswordRequest {

  @NotBlank(message = "Informe a autorização de recuperação.")
  private String authorization;

  private String newPassword;

  private String password;

  public ResetPasswordRequest() {}

  public ResetPasswordRequest(String authorization, String newPassword) {
    this.authorization = authorization;
    this.newPassword = newPassword;
  }

  public String getAuthorization() {
    return authorization;
  }

  public void setAuthorization(String authorization) {
    this.authorization = authorization;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String authorization() {
    return authorization;
  }

  public String resolvedPassword() {
    if (newPassword != null && !newPassword.isBlank()) {
      return newPassword;
    }

    return password;
  }

  @AssertTrue(message = "Informe uma nova senha.")
  public boolean isPasswordProvided() {
    String resolved = resolvedPassword();
    return resolved != null && !resolved.isBlank();
  }

  @AssertTrue(message = "A senha deve ter pelo menos 6 caracteres.")
  public boolean isPasswordSizeValid() {
    String resolved = resolvedPassword();
    return resolved == null || resolved.isBlank() || resolved.length() >= 6;
  }

  @Override
  public String toString() {
    return "ResetPasswordRequest[authorization=[PROTEGIDA], password=[PROTEGIDA]]";
  }
}
