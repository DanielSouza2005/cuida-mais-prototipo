package br.com.cuidaplus.api.auth;

import br.com.cuidaplus.api.auth.dto.AuthResponse;
import br.com.cuidaplus.api.auth.dto.ForgotPasswordRequest;
import br.com.cuidaplus.api.auth.dto.LoginRequest;
import br.com.cuidaplus.api.auth.dto.RegisterRequest;
import br.com.cuidaplus.api.auth.dto.ResetPasswordRequest;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.common.MessageResponse;
import br.com.cuidaplus.api.email.EmailService;
import br.com.cuidaplus.api.security.TokenService;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserMapper;
import br.com.cuidaplus.api.user.UserRepository;
import br.com.cuidaplus.api.user.UserService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private static final String RESET_MESSAGE = "Se o e-mail estiver cadastrado, enviaremos instrucoes de recuperacao.";

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;
  private final EmailService emailService;
  private final SecureRandom secureRandom = new SecureRandom();
  private final String resetPasswordUrl;
  private final Set<String> allowedOrigins;
  private final long resetExpirationMinutes;

  public AuthService(
    UserRepository userRepository,
    PasswordResetTokenRepository passwordResetTokenRepository,
    UserMapper userMapper,
    PasswordEncoder passwordEncoder,
    TokenService tokenService,
    EmailService emailService,
    @Value("${app.frontend.reset-password-url}") String resetPasswordUrl,
    @Value("${app.cors.allowed-origins}") String allowedOrigins,
    @Value("${app.password-reset.expiration-minutes}") long resetExpirationMinutes
  ) {
    this.userRepository = userRepository;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.tokenService = tokenService;
    this.emailService = emailService;
    this.resetPasswordUrl = resetPasswordUrl;
    this.allowedOrigins = Arrays
      .stream(allowedOrigins.split(","))
      .map(String::trim)
      .filter(origin -> !origin.isBlank())
      .collect(Collectors.toSet());
    this.resetExpirationMinutes = resetExpirationMinutes;
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    String email = UserService.normalizeEmail(request.email());
    String cpf = UserService.onlyDigits(request.cpf());

    if (userRepository.existsByEmail(email)) {
      throw new BusinessException("E-mail ja cadastrado.");
    }

    if (userRepository.existsByCpf(cpf)) {
      throw new BusinessException("CPF ja cadastrado.");
    }

    User user = new User();
    user.setFullName(request.fullName().trim());
    user.setCpf(cpf);
    user.setEmail(email);
    user.setBirthDate(UserService.parseBirthDate(request.birthDate()));
    user.setUserType(request.userType());
    user.setPasswordHash(passwordEncoder.encode(request.password()));

    User savedUser = userRepository.save(user);
    return new AuthResponse(userMapper.toResponse(savedUser), tokenService.generate(savedUser.getId()));
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request) {
    String email = UserService.normalizeEmail(request.email());
    User user = userRepository
      .findByEmail(email)
      .orElseThrow(() -> new BusinessException("E-mail ou senha invalidos.", HttpStatus.UNAUTHORIZED));

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new BusinessException("E-mail ou senha invalidos.", HttpStatus.UNAUTHORIZED);
    }

    return new AuthResponse(userMapper.toResponse(user), tokenService.generate(user.getId()));
  }

  @Transactional
  public MessageResponse requestPasswordReset(ForgotPasswordRequest request, String origin) {
    String email = UserService.normalizeEmail(request.email());
    userRepository.findByEmail(email).ifPresent(user -> {
      invalidateUnusedTokens(user);
      String token = generateResetToken();

      PasswordResetToken resetToken = new PasswordResetToken();
      resetToken.setUser(user);
      resetToken.setTokenHash(hashToken(token));
      resetToken.setExpiresAt(Instant.now().plusSeconds(resetExpirationMinutes * 60));
      passwordResetTokenRepository.save(resetToken);

      emailService.sendPasswordResetEmail(user.getEmail(), buildResetLink(token, origin));
    });

    return new MessageResponse(RESET_MESSAGE);
  }

  @Transactional
  public MessageResponse resetPassword(ResetPasswordRequest request) {
    PasswordResetToken resetToken = passwordResetTokenRepository
      .findByTokenHash(hashToken(request.token()))
      .orElseThrow(() -> new BusinessException("Token de recuperacao invalido ou expirado.", HttpStatus.BAD_REQUEST));

    if (resetToken.getUsedAt() != null) {
      throw new BusinessException("Token de recuperacao ja utilizado.", HttpStatus.BAD_REQUEST);
    }

    if (resetToken.getExpiresAt().isBefore(Instant.now())) {
      throw new BusinessException("Token de recuperacao expirado.", HttpStatus.BAD_REQUEST);
    }

    User user = resetToken.getUser();
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    resetToken.setUsedAt(Instant.now());
    userRepository.save(user);
    passwordResetTokenRepository.save(resetToken);

    return new MessageResponse("Senha redefinida com sucesso.");
  }

  public MessageResponse logout() {
    return new MessageResponse("Sessao encerrada.");
  }

  private void invalidateUnusedTokens(User user) {
    Instant now = Instant.now();
    passwordResetTokenRepository.findByUserAndUsedAtIsNull(user).forEach(token -> token.setUsedAt(now));
  }

  private String generateResetToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String hashToken(String token) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder(digest.length * 2);
      for (byte value : digest) {
        builder.append(String.format("%02x", value));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new BusinessException("Nao foi possivel processar o token.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private String buildResetLink(String token, String origin) {
    String baseUrl = resolveResetPasswordUrl(origin);
    String separator = baseUrl.contains("?") ? "&" : "?";
    return baseUrl + separator + "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
  }

  private String resolveResetPasswordUrl(String origin) {
    if (origin != null) {
      String normalizedOrigin = origin.trim();
      if (allowedOrigins.contains(normalizedOrigin)) {
        return normalizedOrigin;
      }
    }

    return resetPasswordUrl;
  }
}
