package br.com.cuidaplus.api.account.dto;

public record ReauthenticateResponse(String confirmationToken, long expiresInSeconds) {
  @Override public String toString() {
    return "ReauthenticateResponse[confirmationToken=[PROTEGIDO], expiresInSeconds=" + expiresInSeconds + "]";
  }
}
