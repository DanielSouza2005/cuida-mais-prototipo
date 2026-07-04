package br.com.cuidaplus.api.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

public class ResetPasswordRequest {

  @NotBlank(message = "Informe o token de recuperação.")
  private String token;

  private String newPassword;

  private String password;

  public ResetPasswordRequest() {}

  public ResetPasswordRequest(String token, String newPassword) {
    this.token = token;
    this.newPassword = newPassword;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
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

  public String token() {
    return token;
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
}
