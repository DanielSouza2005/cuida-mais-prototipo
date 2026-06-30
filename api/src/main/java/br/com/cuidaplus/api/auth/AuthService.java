package br.com.cuidaplus.api.auth;

import br.com.cuidaplus.api.auth.dto.AuthResponse;
import br.com.cuidaplus.api.auth.dto.AddressRequest;
import br.com.cuidaplus.api.auth.dto.ForgotPasswordRequest;
import br.com.cuidaplus.api.auth.dto.LoginRequest;
import br.com.cuidaplus.api.auth.dto.RegisterCaregiverRequest;
import br.com.cuidaplus.api.auth.dto.RegisterRequest;
import br.com.cuidaplus.api.auth.dto.RegisterResponsibleRequest;
import br.com.cuidaplus.api.auth.dto.RegisterUserDataRequest;
import br.com.cuidaplus.api.auth.dto.ResetPasswordRequest;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.common.MessageResponse;
import br.com.cuidaplus.api.email.EmailService;
import br.com.cuidaplus.api.profile.AddressFields;
import br.com.cuidaplus.api.profile.AssistedPerson;
import br.com.cuidaplus.api.profile.AssistedPersonRepository;
import br.com.cuidaplus.api.profile.CaregiverAvailability;
import br.com.cuidaplus.api.profile.CaregiverProfile;
import br.com.cuidaplus.api.profile.CaregiverProfileRepository;
import br.com.cuidaplus.api.profile.EmergencyContact;
import br.com.cuidaplus.api.profile.EmergencyContactRepository;
import br.com.cuidaplus.api.profile.ResponsibleProfile;
import br.com.cuidaplus.api.profile.ResponsibleProfileRepository;
import br.com.cuidaplus.api.security.TokenService;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserMapper;
import br.com.cuidaplus.api.user.UserRepository;
import br.com.cuidaplus.api.user.UserService;
import br.com.cuidaplus.api.user.UserType;
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
  private final ResponsibleProfileRepository responsibleProfileRepository;
  private final AssistedPersonRepository assistedPersonRepository;
  private final EmergencyContactRepository emergencyContactRepository;
  private final CaregiverProfileRepository caregiverProfileRepository;
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
    ResponsibleProfileRepository responsibleProfileRepository,
    AssistedPersonRepository assistedPersonRepository,
    EmergencyContactRepository emergencyContactRepository,
    CaregiverProfileRepository caregiverProfileRepository,
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
    this.responsibleProfileRepository = responsibleProfileRepository;
    this.assistedPersonRepository = assistedPersonRepository;
    this.emergencyContactRepository = emergencyContactRepository;
    this.caregiverProfileRepository = caregiverProfileRepository;
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

  @Transactional
  public AuthResponse registerResponsible(RegisterResponsibleRequest request) {
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
    assistedPerson.setCpf(optionalDigits(assistedRequest.cpf()));
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
    EmergencyContact contact = new EmergencyContact();
    contact.setAssistedPerson(savedAssistedPerson);
    contact.setResponsibleContact(contactRequest.isResponsibleContact());
    contact.setNome(contactRequest.isResponsibleContact() ? user.getFullName() : contactRequest.nome().trim());
    contact.setTelefone(contactRequest.isResponsibleContact() ? user.getPhone() : UserService.onlyDigits(contactRequest.telefone()));
    contact.setVinculo(contactRequest.isResponsibleContact()
      ? resolveResponsibleRelationship(profile)
      : contactRequest.vinculo().trim());
    emergencyContactRepository.save(contact);

    return new AuthResponse(userMapper.toResponse(user), tokenService.generate(user.getId()));
  }

  @Transactional
  public AuthResponse registerCaregiver(RegisterCaregiverRequest request) {
    User user = createUser(request.user(), UserType.CUIDADOR);
    RegisterCaregiverRequest.CaregiverProfileRequest profileRequest = request.caregiverProfile();

    CaregiverProfile profile = new CaregiverProfile();
    profile.setUser(user);
    profile.setFormacao(profileRequest.formacao());
    profile.setFormacaoOutro(trimToNull(profileRequest.formacaoOutro()));
    profile.setExperiencia(trimToNull(profileRequest.experiencia()));
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
    return new AuthResponse(userMapper.toResponse(user), tokenService.generate(user.getId()));
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

  private User createUser(RegisterUserDataRequest request, UserType userType) {
    String email = UserService.normalizeEmail(request.email());
    String cpf = UserService.onlyDigits(request.cpf());

    if (userRepository.existsByEmail(email)) {
      throw new BusinessException("E-mail ja cadastrado.");
    }

    if (userRepository.existsByCpf(cpf)) {
      throw new BusinessException("CPF ja cadastrado.");
    }

    User user = new User();
    user.setFullName(request.nome().trim());
    user.setCpf(cpf);
    user.setEmail(email);
    user.setBirthDate(request.dataNascimento());
    user.setPhone(UserService.onlyDigits(request.telefone()));
    user.setUserType(userType);
    user.setStatus("ACTIVE");
    user.setPasswordHash(passwordEncoder.encode(request.senha()));

    return userRepository.save(user);
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
    return address;
  }

  private String optionalDigits(String value) {
    String digits = UserService.onlyDigits(value);
    return digits.isBlank() ? null : digits;
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
