package br.com.cuidaplus.api.auth;

import br.com.cuidaplus.api.auth.dto.AuthResponse;
import br.com.cuidaplus.api.auth.dto.ForgotPasswordRequest;
import br.com.cuidaplus.api.auth.dto.LoginRequest;
import br.com.cuidaplus.api.auth.dto.RegisterRequest;
import br.com.cuidaplus.api.auth.dto.ResetPasswordRequest;
import br.com.cuidaplus.api.common.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @PostMapping("/forgot-password")
  public MessageResponse forgotPassword(
    @Valid @RequestBody ForgotPasswordRequest request,
    @RequestHeader(value = "Origin", required = false) String origin
  ) {
    return authService.requestPasswordReset(request, origin);
  }

  @PostMapping("/reset-password")
  public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    return authService.resetPassword(request);
  }

  @PostMapping("/logout")
  public MessageResponse logout() {
    return authService.logout();
  }
}
