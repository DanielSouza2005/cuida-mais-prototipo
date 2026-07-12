package br.com.cuidaplus.api.auth;

import br.com.cuidaplus.api.auth.dto.AuthResponse;
import br.com.cuidaplus.api.auth.dto.ForgotPasswordRequest;
import br.com.cuidaplus.api.auth.dto.LoginRequest;
import br.com.cuidaplus.api.auth.dto.RegisterCaregiverRequest;
import br.com.cuidaplus.api.auth.dto.RegisterRequest;
import br.com.cuidaplus.api.auth.dto.RegisterResponsibleRequest;
import br.com.cuidaplus.api.auth.dto.ResetPasswordRequest;
import br.com.cuidaplus.api.common.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

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

  @PostMapping("/register/responsible")
  public AuthResponse registerResponsible(@Valid @RequestBody RegisterResponsibleRequest request) {
    return authService.registerResponsible(request);
  }

  @PostMapping(value = "/register/caregiver", consumes = MediaType.APPLICATION_JSON_VALUE)
  public AuthResponse registerCaregiver(@Valid @RequestBody RegisterCaregiverRequest request) {
    return authService.registerCaregiver(request);
  }

  @PostMapping(value = "/register/caregiver", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public AuthResponse registerCaregiverWithPhoto(
    @Valid @RequestPart("data") RegisterCaregiverRequest request,
    @RequestPart(value = "photo", required = false) MultipartFile photo
  ) {
    return authService.registerCaregiver(request, photo);
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
