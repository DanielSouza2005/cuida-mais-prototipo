package br.com.cuidaplus.api.auth;

import br.com.cuidaplus.api.audit.*;
import br.com.cuidaplus.api.auth.dto.AuthResponse;
import br.com.cuidaplus.api.auth.dto.AddressRequest;
import br.com.cuidaplus.api.auth.dto.ForgotPasswordRequest;
import br.com.cuidaplus.api.auth.dto.LoginRequest;
import br.com.cuidaplus.api.auth.dto.RegisterCaregiverRequest;
import br.com.cuidaplus.api.auth.dto.RegisterRequest;
import br.com.cuidaplus.api.auth.dto.RegisterResponsibleRequest;
import br.com.cuidaplus.api.auth.dto.RegisterUserDataRequest;
import br.com.cuidaplus.api.auth.dto.ResetPasswordRequest;
import br.com.cuidaplus.api.auth.dto.VerifyPasswordResetCodeRequest;
import br.com.cuidaplus.api.auth.dto.PasswordResetAuthorizationResponse;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.common.MessageResponse;
import br.com.cuidaplus.api.email.EmailService;
import br.com.cuidaplus.api.profile.AddressFields;
import br.com.cuidaplus.api.profile.AssistedPerson;
import br.com.cuidaplus.api.profile.AssistedPersonRepository;
import br.com.cuidaplus.api.profile.CaregiverAvailability;
import br.com.cuidaplus.api.profile.CaregiverProfile;
import br.com.cuidaplus.api.profile.CaregiverProfileRepository;
import br.com.cuidaplus.api.profile.CaregiverFormationPolicy;
import br.com.cuidaplus.api.profile.EmergencyContact;
import br.com.cuidaplus.api.profile.EmergencyContactRepository;
import br.com.cuidaplus.api.profile.FormacaoCuidador;
import br.com.cuidaplus.api.profile.ResponsibleProfile;
import br.com.cuidaplus.api.profile.ResponsibleProfileRepository;
import br.com.cuidaplus.api.security.TokenService;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.AccountStatus;
import br.com.cuidaplus.api.user.UserMapper;
import br.com.cuidaplus.api.user.UserRepository;
import br.com.cuidaplus.api.user.UserService;
import br.com.cuidaplus.api.user.UserType;
import br.com.cuidaplus.api.storage.ProfilePhotoStorageService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AuthService {

  private static final String RESET_MESSAGE = "Se o e-mail estiver cadastrado, você receberá um código de verificação.";
  private static final int MAX_CODE_ATTEMPTS = 5;
  private static final Duration RESEND_INTERVAL = Duration.ofSeconds(60);
  private static final Duration AUTHORIZATION_EXPIRATION = Duration.ofMinutes(5);

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final ResponsibleProfileRepository responsibleProfileRepository;
  private final AssistedPersonRepository assistedPersonRepository;
  private final EmergencyContactRepository emergencyContactRepository;
  private final CaregiverProfileRepository caregiverProfileRepository;
  private final ProfilePhotoStorageService profilePhotoStorageService;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;
  private final EmailService emailService;
  private final AuditService audit;
  private final PasswordResetRateLimiter passwordResetRateLimiter;
  private final SecureRandom secureRandom = new SecureRandom();
  private final long resetExpirationMinutes;

  public AuthService(
    UserRepository userRepository,
    PasswordResetTokenRepository passwordResetTokenRepository,
    ResponsibleProfileRepository responsibleProfileRepository,
    AssistedPersonRepository assistedPersonRepository,
    EmergencyContactRepository emergencyContactRepository,
    CaregiverProfileRepository caregiverProfileRepository,
    ProfilePhotoStorageService profilePhotoStorageService,
    UserMapper userMapper,
    PasswordEncoder passwordEncoder,
    TokenService tokenService,
    EmailService emailService,
    AuditService audit,
    PasswordResetRateLimiter passwordResetRateLimiter,
    @Value("${app.password-reset.expiration-minutes}") long resetExpirationMinutes
  ) {
    this.userRepository = userRepository;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.responsibleProfileRepository = responsibleProfileRepository;
    this.assistedPersonRepository = assistedPersonRepository;
    this.emergencyContactRepository = emergencyContactRepository;
    this.caregiverProfileRepository = caregiverProfileRepository;
    this.profilePhotoStorageService = profilePhotoStorageService;
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.tokenService = tokenService;
    this.emailService = emailService;
    this.audit = audit;
    this.passwordResetRateLimiter = passwordResetRateLimiter;
    this.resetExpirationMinutes = Math.min(Math.max(resetExpirationMinutes, 5), 15);
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    throw new BusinessException("Use o fluxo de cadastro completo para responsável ou cuidador.", HttpStatus.BAD_REQUEST);
  }

  @Transactional
  public MessageResponse registerResponsible(RegisterResponsibleRequest request) {
    User user = createUser(request.user(), UserType.RESPONSAVEL);

    ResponsibleProfile profile = new ResponsibleProfile();
    profile.setUser(user);
    profile.setParentesco(request.responsibleProfile().parentesco());
    profile.setParentescoOutro(trimToNull(request.responsibleProfile().parentescoOutro()));
    profile.setPreferenciaContato(request.responsibleProfile().preferenciaContato());
    responsibleProfileRepository.save(profile);

    RegisterResponsibleRequest.AssistedPersonRequest assistedRequest = request.assistedPerson();
    AssistedPerson assistedPerson = new AssistedPerson();
    assistedPerson.setResponsibleUser(user);
    assistedPerson.setNome(assistedRequest.nome().trim());
    assistedPerson.setDataNascimento(assistedRequest.dataNascimento());
    assistedPerson.setGrauDependencia(assistedRequest.grauDependencia());
    assistedPerson.setMobilidade(assistedRequest.mobilidade());
    assistedPerson.setMobilidadeOutro(trimToNull(assistedRequest.mobilidadeOutro()));
    assistedPerson.setAlergias(assistedRequest.alergias());
    assistedPerson.setAlergiasOutro(trimToNull(assistedRequest.alergiasOutro()));
    assistedPerson.setAlergiasDetalhes(trimToNull(assistedRequest.alergiasDetalhes()));
    assistedPerson.setRestricoesAlimentares(assistedRequest.restricoesAlimentares());
    assistedPerson.setRestricoesAlimentaresOutro(trimToNull(assistedRequest.restricoesAlimentaresOutro()));
    assistedPerson.setRestricoesAlimentaresDetalhes(trimToNull(assistedRequest.restricoesAlimentaresDetalhes()));
    assistedPerson.setMedicamentos(trimToNull(assistedRequest.medicamentos()));
    assistedPerson.setObservacoes(trimToNull(assistedRequest.observacoes()));
    assistedPerson.setEnderecoCuidado(toAddress(assistedRequest.enderecoCuidado()));
    AssistedPerson savedAssistedPerson = assistedPersonRepository.save(assistedPerson);

    RegisterResponsibleRequest.EmergencyContactRequest contactRequest = assistedRequest.contatoEmergencia();
    if (contactRequest.isResponsibleContact() && user.getPhone() == null) {
      throw new BusinessException("Informe o telefone da conta ou cadastre outro contato de emergência.");
    }
    EmergencyContact contact = new EmergencyContact();
    contact.setAssistedPerson(savedAssistedPerson);
    contact.setResponsibleContact(contactRequest.isResponsibleContact());
    contact.setNome(contactRequest.isResponsibleContact() ? user.getFullName() : contactRequest.nome().trim());
    contact.setTelefone(contactRequest.isResponsibleContact() ? user.getPhone() : UserService.onlyDigits(contactRequest.telefone()));
    contact.setVinculo(contactRequest.isResponsibleContact()
      ? resolveResponsibleRelationship(profile)
      : contactRequest.vinculo().trim());
    emergencyContactRepository.save(contact);

    return new MessageResponse("Cadastro enviado para análise. Você será avisado por e-mail quando a avaliação for concluída.");
  }

  @Transactional
  public MessageResponse registerCaregiver(RegisterCaregiverRequest request) {
    return registerCaregiver(request, null);
  }

  @Transactional
  public MessageResponse registerCaregiver(RegisterCaregiverRequest request, MultipartFile photo) {
    RegisterCaregiverRequest.CaregiverProfileRequest profileRequest = request.caregiverProfile();
    LinkedHashSet<FormacaoCuidador> formacoes = CaregiverFormationPolicy.requireValid(
      profileRequest.formacoes(), profileRequest.formacaoOutro());
    User user = createUser(request.user(), UserType.CUIDADOR);

    CaregiverProfile profile = new CaregiverProfile();
    profile.setUser(user);
    profile.setFormacoes(formacoes);
    profile.setFormacaoOutro(formacoes.contains(FormacaoCuidador.OUTRO)
      ? trimToNull(profileRequest.formacaoOutro()) : null);
    profile.setTempoExperiencia(profileRequest.tempoExperiencia());
    profile.setBiografia(trimToNull(profileRequest.biografia()));
    profile.setEnderecoAtendimento(toAddress(request.address()));
    profile.setModalidades(profileRequest.modalidades());
    profile.setModalidadeOutro(trimToNull(profileRequest.modalidadeOutro()));
    profile.setServicosOferecidos(profileRequest.servicosOferecidos());
    profile.setServicoOutro(trimToNull(profileRequest.servicoOutro()));

    RegisterCaregiverRequest.AvailabilityRequest availabilityRequest = profileRequest.disponibilidade();
    CaregiverAvailability availability = new CaregiverAvailability();
    availability.setDiasSemana(availabilityRequest.diasSemana());
    availability.setPeriodos(availabilityRequest.periodos());
    availability.setHorarioInicio(availabilityRequest.horarioInicio());
    availability.setHorarioFim(availabilityRequest.horarioFim());
    availability.setObservacao(trimToNull(availabilityRequest.observacao()));
    profile.setDisponibilidade(availability);

    caregiverProfileRepository.save(profile);
    if (photo != null && !photo.isEmpty()) {
      user.setProfilePhotoUrl(profilePhotoStorageService.store(photo));
      userRepository.save(user);
    }
    return new MessageResponse("Cadastro enviado para análise. Você será avisado por e-mail quando a avaliação for concluída.");
  }

  @Transactional
  public AuthResponse login(LoginRequest request) {
    String email = UserService.normalizeEmail(request.email());
    User user = userRepository
      .findByEmail(email)
      .orElseThrow(() -> new BusinessException("E-mail ou senha inválidos.", HttpStatus.UNAUTHORIZED));

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new BusinessException("E-mail ou senha inválidos.", HttpStatus.UNAUTHORIZED);
    }

    try {
      validateLoginStatus(user);
    } catch (BusinessException exception) {
      if (exception.getStatus() == HttpStatus.FORBIDDEN) {
        audit.failure(AuditAction.LOGIN_BLOQUEADO, AuditCategory.SEGURANCA, AuditResult.BLOQUEADO_POR_REGRA,
          user, "USUARIO", user.getId(), "Acesso impedido pela situação cadastral.");
      }
      throw exception;
    }
    user.setUltimoLoginEm(Instant.now());
    audit.success(AuditAction.LOGIN_SUCESSO, AuditCategory.SEGURANCA, user, "USUARIO", user.getId(), null, null);

    return new AuthResponse(userMapper.toResponse(user), tokenService.generate(user.getId()));
  }

  @Transactional
  public MessageResponse requestPasswordReset(ForgotPasswordRequest request, String origin) {
    passwordResetRateLimiter.requireAllowed(origin);
    String email = UserService.normalizeEmail(request.email());
    userRepository.findByEmail(email).ifPresent(user -> {
      if (!user.isActive()) return;
      Optional<PasswordResetToken> latest = passwordResetTokenRepository.findFirstByUserOrderByCreatedAtDesc(user);
      if (latest.isPresent() && latest.get().getCreatedAt().isAfter(Instant.now().minus(RESEND_INTERVAL))) return;
      invalidateUnusedTokens(user);
      String code = generateVerificationCode();

      PasswordResetToken resetToken = new PasswordResetToken();
      resetToken.setUser(user);
      resetToken.setTokenHash(passwordEncoder.encode(code));
      resetToken.setExpiresAt(Instant.now().plusSeconds(resetExpirationMinutes * 60));
      passwordResetTokenRepository.save(resetToken);
      audit.success(AuditAction.RECUPERACAO_SENHA_SOLICITADA, AuditCategory.SEGURANCA, user, "USUARIO", user.getId(), null, null);
      sendResetCodeAfterCommit(user.getEmail(), code);
    });

    return new MessageResponse(RESET_MESSAGE);
  }

  @Transactional(noRollbackFor = BusinessException.class)
  public PasswordResetAuthorizationResponse verifyPasswordResetCode(VerifyPasswordResetCodeRequest request) {
    String email = UserService.normalizeEmail(request.email());
    User user = userRepository.findByEmail(email).filter(User::isActive)
      .orElseThrow(() -> invalidCode("PASSWORD_RESET_CODE_INVALID"));
    PasswordResetToken latest = passwordResetTokenRepository.findFirstByUserOrderByCreatedAtDesc(user)
      .flatMap(token -> passwordResetTokenRepository.findForUpdateById(token.getId()))
      .orElseThrow(() -> invalidCode("PASSWORD_RESET_CODE_INVALID"));
    Instant now = Instant.now();
    if (latest.getInvalidatedAt() != null || latest.getUsedAt() != null || latest.getVerifiedAt() != null) {
      throw invalidCode("PASSWORD_RESET_CODE_INVALID");
    }
    if (!latest.getExpiresAt().isAfter(now)) {
      latest.setInvalidatedAt(now);
      throw new BusinessException("Código de verificação expirado. Solicite um novo código.", HttpStatus.BAD_REQUEST, "PASSWORD_RESET_CODE_EXPIRED");
    }
    if (latest.getCodeAttempts() >= MAX_CODE_ATTEMPTS) {
      throw new BusinessException("Limite de tentativas atingido. Solicite um novo código.", HttpStatus.TOO_MANY_REQUESTS, "PASSWORD_RESET_CODE_ATTEMPTS_EXCEEDED");
    }
    if (!passwordEncoder.matches(request.code(), latest.getTokenHash())) {
      latest.setCodeAttempts(latest.getCodeAttempts() + 1);
      if (latest.getCodeAttempts() >= MAX_CODE_ATTEMPTS) {
        latest.setInvalidatedAt(now);
        throw new BusinessException("Limite de tentativas atingido. Solicite um novo código.", HttpStatus.TOO_MANY_REQUESTS, "PASSWORD_RESET_CODE_ATTEMPTS_EXCEEDED");
      }
      throw invalidCode("PASSWORD_RESET_CODE_INVALID");
    }
    String authorization = generateAuthorization();
    latest.setVerifiedAt(now);
    latest.setAuthorizationHash(hashToken(authorization));
    latest.setAuthorizationExpiresAt(now.plus(AUTHORIZATION_EXPIRATION));
    return new PasswordResetAuthorizationResponse(authorization, AUTHORIZATION_EXPIRATION.toSeconds());
  }

  @Transactional
  public MessageResponse resetPassword(ResetPasswordRequest request) {
    PasswordResetToken resetToken = passwordResetTokenRepository
      .findForUpdateByAuthorizationHash(hashToken(request.authorization()))
      .orElseThrow(() -> new BusinessException("Autorização de redefinição inválida ou expirada.", HttpStatus.BAD_REQUEST, "PASSWORD_RESET_AUTHORIZATION_INVALID"));
    Instant now = Instant.now();
    if (resetToken.getAuthorizationUsedAt() != null) {
      throw new BusinessException("Autorização de redefinição já utilizada.", HttpStatus.CONFLICT, "PASSWORD_RESET_AUTHORIZATION_USED");
    }
    if (resetToken.getVerifiedAt() == null || resetToken.getAuthorizationExpiresAt() == null || !resetToken.getAuthorizationExpiresAt().isAfter(now)) {
      throw new BusinessException("Autorização de redefinição inválida ou expirada.", HttpStatus.BAD_REQUEST, "PASSWORD_RESET_AUTHORIZATION_EXPIRED");
    }

    User user = resetToken.getUser();
    user.setPasswordHash(passwordEncoder.encode(request.resolvedPassword()));
    resetToken.setUsedAt(now);
    resetToken.setAuthorizationUsedAt(now);
    userRepository.save(user);
    passwordResetTokenRepository.findByUserAndUsedAtIsNull(user).forEach(token -> {
      token.setInvalidatedAt(now);
      token.setUsedAt(now);
    });
    audit.success(AuditAction.SENHA_REDEFINIDA, AuditCategory.SEGURANCA, user, "USUARIO", user.getId(), null, null);

    return new MessageResponse("Senha redefinida com sucesso. Entre com sua nova senha.");
  }

  public MessageResponse logout() {
    return new MessageResponse("Sessão encerrada.");
  }

  private User createUser(RegisterUserDataRequest request, UserType userType) {
    String email = UserService.normalizeEmail(request.email());

    if (userRepository.existsByEmail(email)) {
      throw new BusinessException("E-mail já cadastrado.");
    }

    User user = new User();
    user.setFullName(request.nome().trim());
    user.setEmail(email);
    user.setMaiorDeIdadeConfirmado(request.maiorDeIdadeConfirmado());
    user.setPhone(UserService.optionalDigits(request.telefone()));
    user.setUserType(userType);
    user.setAccountStatus(AccountStatus.ATIVO);
    user.setPasswordHash(passwordEncoder.encode(request.senha()));

    return userRepository.save(user);
  }

  private void validateLoginStatus(User user) {
    if (user.getAccountStatus() == AccountStatus.EXCLUIDO) {
      throw new BusinessException("Sua conta foi excluída e não pode mais acessar o sistema.", HttpStatus.FORBIDDEN, "ACCOUNT_DELETED");
    }
    if (user.getAccountStatus() == AccountStatus.BLOQUEADO) {
      throw new BusinessException("Sua conta está bloqueada. Entre em contato com o suporte para mais informações.", HttpStatus.FORBIDDEN, "ACCOUNT_BLOCKED");
    }
    if (user.getAccountStatus() == AccountStatus.INATIVO) {
      throw new BusinessException("Sua conta está inativa. Entre em contato com o suporte para mais informações.", HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE");
    }
    if (user.isResponsible()) {
      var profile = responsibleProfileRepository.findByUser(user)
        .orElseThrow(() -> new BusinessException("Perfil de responsável não encontrado.", HttpStatus.FORBIDDEN, "RESPONSIBLE_PROFILE_MISSING"));
      switch (profile.getSituacaoAprovacao()) {
        case PENDENTE -> throw new BusinessException("Seu cadastro de responsável ainda está em análise. Você será avisado por e-mail quando a avaliação for concluída.", HttpStatus.FORBIDDEN, "RESPONSIBLE_PENDING_APPROVAL");
        case REPROVADO -> throw new BusinessException("Seu cadastro de responsável foi reprovado. Verifique seu e-mail para mais informações.", HttpStatus.FORBIDDEN, "RESPONSIBLE_REJECTED");
        case BLOQUEADO -> throw new BusinessException("Seu perfil de responsável está bloqueado. Entre em contato com o suporte para mais informações.", HttpStatus.FORBIDDEN, "RESPONSIBLE_BLOCKED");
        case APROVADO -> { }
      }
      return;
    }
    if (!user.isCaregiver()) return;
    CaregiverProfile profile = caregiverProfileRepository.findByUser(user)
      .orElseThrow(() -> new BusinessException("Perfil de cuidador não encontrado.", HttpStatus.FORBIDDEN));
    switch (profile.getSituacaoAprovacao()) {
      case PENDENTE -> throw new BusinessException("Seu cadastro de cuidador ainda está em análise. Você será avisado por e-mail quando a avaliação for concluída.", HttpStatus.FORBIDDEN, "CAREGIVER_PENDING_APPROVAL");
      case REPROVADO -> throw new BusinessException("Seu cadastro de cuidador foi reprovado. Verifique seu e-mail para mais informações.", HttpStatus.FORBIDDEN, "CAREGIVER_REJECTED");
      case BLOQUEADO -> throw new BusinessException("Seu perfil de cuidador está bloqueado. Entre em contato com o suporte para mais informações.", HttpStatus.FORBIDDEN, "CAREGIVER_BLOCKED");
      case APROVADO -> { }
    }
  }

  private AddressFields toAddress(AddressRequest request) {
    AddressFields address = new AddressFields();
    address.setCep(UserService.onlyDigits(request.cep()));
    address.setRua(request.rua().trim());
    address.setNumero(request.numero().trim());
    address.setComplemento(trimToNull(request.complemento()));
    address.setBairro(request.bairro().trim());
    address.setCidade(request.cidade().trim());
    address.setEstado(request.estado().trim().toUpperCase());
    address.setPontoReferencia(trimToNull(request.pontoReferencia()));
    address.setLatitude(toCoordinate(request.latitude()));
    address.setLongitude(toCoordinate(request.longitude()));
    return address;
  }

  private BigDecimal toCoordinate(Double value) {
    return value == null ? null : BigDecimal.valueOf(value);
  }

  private String trimToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }

  private String resolveResponsibleRelationship(ResponsibleProfile profile) {
    if (profile.getParentescoOutro() != null && !profile.getParentescoOutro().isBlank()) {
      return profile.getParentescoOutro();
    }

    return profile.getParentesco().name();
  }

  private void invalidateUnusedTokens(User user) {
    Instant now = Instant.now();
    passwordResetTokenRepository.findByUserAndUsedAtIsNull(user).forEach(token -> {
      token.setUsedAt(now);
      token.setInvalidatedAt(now);
    });
  }

  private String generateAuthorization() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String generateVerificationCode() {
    return String.format("%06d", secureRandom.nextInt(1_000_000));
  }

  private BusinessException invalidCode(String code) {
    return new BusinessException("Código de verificação inválido.", HttpStatus.BAD_REQUEST, code);
  }

  private void sendResetCodeAfterCommit(String email, String code) {
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
      emailService.sendPasswordResetEmail(email, code, resetExpirationMinutes);
      return;
    }
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override public void afterCommit() {
        emailService.sendPasswordResetEmail(email, code, resetExpirationMinutes);
      }
    });
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
      throw new BusinessException("Não foi possível processar o token.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

}
