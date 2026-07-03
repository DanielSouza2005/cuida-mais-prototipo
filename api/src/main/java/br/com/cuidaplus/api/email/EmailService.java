package br.com.cuidaplus.api.email;

public interface EmailService {

  void sendPasswordResetEmail(String to, String resetLink, String fallbackWebLink, long expirationMinutes);
}
