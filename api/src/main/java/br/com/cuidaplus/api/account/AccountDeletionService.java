package br.com.cuidaplus.api.account;

import br.com.cuidaplus.api.account.dto.ReauthenticateResponse;
import br.com.cuidaplus.api.auth.PasswordResetTokenRepository;
import br.com.cuidaplus.api.care_contract.CareContractRepository;
import br.com.cuidaplus.api.care_contract.CareContractStatus;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.common.MessageResponse;
import br.com.cuidaplus.api.notification.NotificationRepository;
import br.com.cuidaplus.api.notification.UserNotificationPreferenceRepository;
import br.com.cuidaplus.api.profile.AddressFields;
import br.com.cuidaplus.api.profile.AssistedPersonRepository;
import br.com.cuidaplus.api.profile.CaregiverApprovalStatus;
import br.com.cuidaplus.api.profile.CaregiverAvailability;
import br.com.cuidaplus.api.profile.CaregiverProfileRepository;
import br.com.cuidaplus.api.profile.EmergencyContactRepository;
import br.com.cuidaplus.api.profile.ResponsibleApprovalStatus;
import br.com.cuidaplus.api.profile.ResponsibleProfileRepository;
import br.com.cuidaplus.api.storage.ProfilePhotoStorageService;
import br.com.cuidaplus.api.user.AccountStatus;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserRepository;
import br.com.cuidaplus.api.user.UserType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class AccountDeletionService {
  static final long CONFIRMATION_EXPIRATION_SECONDS = 300;
  private static final Set<CareContractStatus> OPERATIONAL_STATUSES = Set.of(
    CareContractStatus.AGENDADA, CareContractStatus.ATIVA, CareContractStatus.ENCERRAMENTO_AGENDADO
  );

  private final UserRepository users;
  private final AccountDeletionConfirmationRepository confirmations;
  private final AccountDeletionAuditRepository audits;
  private final CareContractRepository contracts;
  private final PasswordResetTokenRepository passwordResetTokens;
  private final NotificationRepository notifications;
  private final UserNotificationPreferenceRepository notificationPreferences;
  private final CaregiverProfileRepository caregivers;
  private final ResponsibleProfileRepository responsibles;
  private final AssistedPersonRepository assistedPeople;
  private final EmergencyContactRepository emergencyContacts;
  private final ProfilePhotoStorageService profilePhotos;
  private final PasswordEncoder passwordEncoder;
  private final SecureRandom secureRandom = new SecureRandom();

  public AccountDeletionService(
    UserRepository users,
    AccountDeletionConfirmationRepository confirmations,
    AccountDeletionAuditRepository audits,
    CareContractRepository contracts,
    PasswordResetTokenRepository passwordResetTokens,
    NotificationRepository notifications,
    UserNotificationPreferenceRepository notificationPreferences,
    CaregiverProfileRepository caregivers,
    ResponsibleProfileRepository responsibles,
    AssistedPersonRepository assistedPeople,
    EmergencyContactRepository emergencyContacts,
    ProfilePhotoStorageService profilePhotos,
    PasswordEncoder passwordEncoder
  ) {
    this.users = users;
    this.confirmations = confirmations;
    this.audits = audits;
    this.contracts = contracts;
    this.passwordResetTokens = passwordResetTokens;
    this.notifications = notifications;
    this.notificationPreferences = notificationPreferences;
    this.caregivers = caregivers;
    this.responsibles = responsibles;
    this.assistedPeople = assistedPeople;
    this.emergencyContacts = emergencyContacts;
    this.profilePhotos = profilePhotos;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public ReauthenticateResponse reauthenticate(UUID userId, String password) {
    User user = activeUserForUpdate(userId);
    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new BusinessException("Senha incorreta. Verifique e tente novamente.", HttpStatus.UNAUTHORIZED, "INCORRECT_PASSWORD");
    }

    Instant now = Instant.now();
    confirmations.findByUserAndUsedAtIsNull(user).forEach(token -> token.setUsedAt(now));
    String rawToken = generateToken();
    AccountDeletionConfirmation confirmation = new AccountDeletionConfirmation();
    confirmation.setUser(user);
    confirmation.setTokenHash(hashToken(rawToken));
    confirmation.setExpiresAt(now.plusSeconds(CONFIRMATION_EXPIRATION_SECONDS));
    confirmations.save(confirmation);
    return new ReauthenticateResponse(rawToken, CONFIRMATION_EXPIRATION_SECONDS);
  }

  @Transactional
  public void invalidateConfirmations(UUID userId) {
    users.findById(userId).ifPresent(user -> {
      Instant now = Instant.now();
      confirmations.findByUserAndUsedAtIsNull(user).forEach(token -> token.setUsedAt(now));
    });
  }

  @Transactional
  public MessageResponse deleteAccount(UUID userId, String rawConfirmationToken) {
    User user = users.findByIdForUpdate(userId)
      .orElseThrow(() -> new BusinessException("Usuário não encontrado.", HttpStatus.NOT_FOUND));
    if (user.getAccountStatus() == AccountStatus.EXCLUIDO) {
      throw new BusinessException("Esta conta já foi excluída.", HttpStatus.CONFLICT, "ACCOUNT_ALREADY_DELETED");
    }
    if (!user.isActive()) {
      throw new BusinessException("A conta precisa estar ativa para ser excluída.", HttpStatus.CONFLICT, "ACCOUNT_NOT_ACTIVE");
    }

    AccountDeletionConfirmation confirmation = confirmations.findByTokenHashForUpdate(hashToken(rawConfirmationToken))
      .filter(value -> value.getUser().getId().equals(userId))
      .orElseThrow(this::invalidConfirmation);
    if (confirmation.getUsedAt() != null) throw invalidConfirmation();
    if (!confirmation.getExpiresAt().isAfter(Instant.now())) {
      throw new BusinessException(
        "A confirmação expirou. Informe sua senha novamente para continuar.",
        HttpStatus.BAD_REQUEST,
        "DELETION_CONFIRMATION_EXPIRED"
      );
    }
    if (contracts.existsOperationalContract(user, OPERATIONAL_STATUSES)) {
      throw new BusinessException(
        "Você possui serviços ativos ou agendados. Encerre ou cancele esses serviços antes de excluir sua conta.",
        HttpStatus.CONFLICT,
        "ACCOUNT_HAS_OPERATIONAL_SERVICES"
      );
    }
    if (user.isAdmin() && users.countByUserTypeAndAccountStatus(UserType.ADMIN, AccountStatus.ATIVO) <= 1) {
      throw new BusinessException(
        "O último administrador ativo não pode excluir a própria conta.",
        HttpStatus.CONFLICT,
        "LAST_ACTIVE_ADMIN"
      );
    }

    Instant now = Instant.now();
    confirmation.setUsedAt(now);
    user.setExclusaoSolicitadaEm(now);
    String photoUrl = user.getProfilePhotoUrl();
    anonymizeProfiles(user);
    anonymizeResponsibleEmergencyContacts(user);
    passwordResetTokens.deleteByUser(user);
    notifications.deleteByRecipient(user);
    notificationPreferences.deleteByUser(user);
    confirmations.findByUserAndUsedAtIsNull(user).forEach(token -> token.setUsedAt(now));
    anonymizeUser(user, now);

    AccountDeletionAudit audit = new AccountDeletionAudit();
    audit.setUserReference(user.getId());
    audit.setProfile(user.getUserType());
    audit.setRequestedAt(now);
    audit.setCompletedAt(now);
    audit.setResult("SUCESSO");
    audits.save(audit);
    deletePhotoAfterCommit(photoUrl);
    return new MessageResponse("Conta excluída com sucesso.");
  }

  private void anonymizeProfiles(User user) {
    caregivers.findByUser(user).ifPresent(profile -> {
      profile.setFormacoes(new LinkedHashSet<>());
      profile.setFormacaoOutro(null);
      profile.setTempoExperiencia(null);
      profile.setExperiencia(null);
      profile.setBiografia(null);
      profile.setEnderecoAtendimento(new AddressFields());
      profile.setModalidades(new LinkedHashSet<>());
      profile.setModalidadeOutro(null);
      profile.setServicosOferecidos(new LinkedHashSet<>());
      profile.setServicoOutro(null);
      profile.setDisponibilidade(new CaregiverAvailability());
      profile.setSituacaoAprovacao(CaregiverApprovalStatus.BLOQUEADO);
      profile.setMotivoReprovacao(null);
      profile.setMotivoBloqueioProfissional(null);
    });
    responsibles.findByUser(user).ifPresent(profile -> {
      profile.setParentescoOutro(null);
      profile.setSituacaoAprovacao(ResponsibleApprovalStatus.BLOQUEADO);
      profile.setMotivoReprovacao(null);
      profile.setMotivoBloqueio(null);
    });
  }

  private void anonymizeResponsibleEmergencyContacts(User user) {
    assistedPeople.findByResponsibleUser(user).forEach(person -> emergencyContacts.findByAssistedPerson(person)
      .filter(contact -> contact.isResponsibleContact())
      .ifPresent(contact -> {
        contact.setNome(null);
        contact.setTelefone(null);
        contact.setResponsibleContact(false);
      }));
  }

  private void anonymizeUser(User user, Instant now) {
    user.setFullName(user.isCaregiver() ? "Cuidador removido" : user.isResponsible() ? "Responsável removido" : "Usuário removido");
    user.setEmail("excluido_" + user.getId() + "@anon.local");
    user.setCpf(null);
    user.setPhone(null);
    user.setBirthDate(null);
    user.setProfilePhotoUrl(null);
    user.setPasswordHash("!conta-excluida:" + user.getId());
    user.setMotivoBloqueio(null);
    user.setAccountStatus(AccountStatus.EXCLUIDO);
    user.setExcluidoEm(now);
    user.setDadosAnonimizadosEm(now);
  }

  private User activeUserForUpdate(UUID userId) {
    User user = users.findByIdForUpdate(userId)
      .orElseThrow(() -> new BusinessException("Usuário não encontrado.", HttpStatus.NOT_FOUND));
    if (user.getAccountStatus() == AccountStatus.EXCLUIDO) {
      throw new BusinessException("Sua conta foi excluída e não pode mais acessar o sistema.", HttpStatus.FORBIDDEN, "ACCOUNT_DELETED");
    }
    if (!user.isActive()) {
      throw new BusinessException("A conta precisa estar ativa para confirmar a identidade.", HttpStatus.FORBIDDEN, "ACCOUNT_NOT_ACTIVE");
    }
    return user;
  }

  private BusinessException invalidConfirmation() {
    return new BusinessException(
      "Confirmação de identidade inválida. Informe sua senha novamente para continuar.",
      HttpStatus.BAD_REQUEST,
      "INVALID_DELETION_CONFIRMATION"
    );
  }

  private String generateToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String hashToken(String token) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder(digest.length * 2);
      for (byte value : digest) builder.append(String.format("%02x", value));
      return builder.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 indisponível.", exception);
    }
  }

  private void deletePhotoAfterCommit(String photoUrl) {
    if (photoUrl == null) return;
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
      profilePhotos.delete(photoUrl);
      return;
    }
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override public void afterCommit() { profilePhotos.delete(photoUrl); }
    });
  }
}
