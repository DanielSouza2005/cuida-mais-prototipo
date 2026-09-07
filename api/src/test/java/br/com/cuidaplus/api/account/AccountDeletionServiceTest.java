package br.com.cuidaplus.api.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.cuidaplus.api.auth.PasswordResetTokenRepository;
import br.com.cuidaplus.api.care_contract.CareContractRepository;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.notification.NotificationRepository;
import br.com.cuidaplus.api.notification.UserNotificationPreferenceRepository;
import br.com.cuidaplus.api.profile.AssistedPersonRepository;
import br.com.cuidaplus.api.profile.CaregiverProfileRepository;
import br.com.cuidaplus.api.profile.EmergencyContactRepository;
import br.com.cuidaplus.api.profile.ResponsibleProfile;
import br.com.cuidaplus.api.profile.ResponsibleProfileRepository;
import br.com.cuidaplus.api.storage.ProfilePhotoStorageService;
import br.com.cuidaplus.api.user.AccountStatus;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserRepository;
import br.com.cuidaplus.api.user.UserType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AccountDeletionServiceTest {
  @Mock UserRepository users;
  @Mock AccountDeletionConfirmationRepository confirmations;
  @Mock AccountDeletionAuditRepository audits;
  @Mock CareContractRepository contracts;
  @Mock PasswordResetTokenRepository passwordResetTokens;
  @Mock NotificationRepository notifications;
  @Mock UserNotificationPreferenceRepository notificationPreferences;
  @Mock CaregiverProfileRepository caregivers;
  @Mock ResponsibleProfileRepository responsibles;
  @Mock AssistedPersonRepository assistedPeople;
  @Mock EmergencyContactRepository emergencyContacts;
  @Mock ProfilePhotoStorageService profilePhotos;
  @Mock PasswordEncoder passwordEncoder;
  AccountDeletionService service;

  @BeforeEach
  void setup() {
    service = new AccountDeletionService(users, confirmations, audits, contracts, passwordResetTokens,
      notifications, notificationPreferences, caregivers, responsibles, assistedPeople,
      emergencyContacts, profilePhotos, passwordEncoder);
  }

  @Test
  void incorrectPasswordDoesNotIssueConfirmation() {
    User user = user(UserType.RESPONSAVEL);
    when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("errada", user.getPasswordHash())).thenReturn(false);

    assertThatThrownBy(() -> service.reauthenticate(user.getId(), "errada"))
      .isInstanceOf(BusinessException.class)
      .hasMessage("Senha incorreta. Verifique e tente novamente.");
    verify(confirmations, never()).save(any());
  }

  @Test
  void reauthenticationStoresOnlyHashAndExpiresInFiveMinutes() {
    User user = user(UserType.CUIDADOR);
    when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("correta", user.getPasswordHash())).thenReturn(true);

    Instant before = Instant.now();
    var response = service.reauthenticate(user.getId(), "correta");
    ArgumentCaptor<AccountDeletionConfirmation> captor = ArgumentCaptor.forClass(AccountDeletionConfirmation.class);
    verify(confirmations).save(captor.capture());

    assertThat(response.expiresInSeconds()).isEqualTo(300);
    assertThat(captor.getValue().getTokenHash()).hasSize(64).isNotEqualTo(response.confirmationToken());
    assertThat(captor.getValue().getExpiresAt()).isBetween(before.plusSeconds(299), Instant.now().plusSeconds(301));
  }

  @Test
  void expiredConfirmationIsRejected() {
    User user = user(UserType.RESPONSAVEL);
    AccountDeletionConfirmation confirmation = confirmation(user, Instant.now().minusSeconds(1));
    when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
    when(confirmations.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(confirmation));

    assertThatThrownBy(() -> service.deleteAccount(user.getId(), "expirado"))
      .isInstanceOfSatisfying(BusinessException.class,
        error -> assertThat(error.getCode()).isEqualTo("DELETION_CONFIRMATION_EXPIRED"));
    verify(audits, never()).save(any());
  }

  @Test
  void usedConfirmationCannotBeReused() {
    User user = user(UserType.RESPONSAVEL);
    AccountDeletionConfirmation confirmation = confirmation(user, Instant.now().plusSeconds(60));
    confirmation.setUsedAt(Instant.now());
    when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
    when(confirmations.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(confirmation));

    assertThatThrownBy(() -> service.deleteAccount(user.getId(), "ja-usado"))
      .isInstanceOfSatisfying(BusinessException.class,
        error -> assertThat(error.getCode()).isEqualTo("INVALID_DELETION_CONFIRMATION"));
  }

  @Test
  void logoutInvalidatesUnusedDeletionConfirmation() {
    User user = user(UserType.RESPONSAVEL);
    AccountDeletionConfirmation confirmation = confirmation(user, Instant.now().plusSeconds(60));
    when(users.findById(user.getId())).thenReturn(Optional.of(user));
    when(confirmations.findByUserAndUsedAtIsNull(user)).thenReturn(java.util.List.of(confirmation));

    service.invalidateConfirmations(user.getId());

    assertThat(confirmation.getUsedAt()).isNotNull();
  }

  @Test
  void operationalContractPreventsDeletion() {
    User user = user(UserType.CUIDADOR);
    when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
    when(confirmations.findByTokenHashForUpdate(anyString()))
      .thenReturn(Optional.of(confirmation(user, Instant.now().plusSeconds(60))));
    when(contracts.existsOperationalContract(any(), any())).thenReturn(true);

    assertThatThrownBy(() -> service.deleteAccount(user.getId(), "valido"))
      .isInstanceOfSatisfying(BusinessException.class,
        error -> assertThat(error.getCode()).isEqualTo("ACCOUNT_HAS_OPERATIONAL_SERVICES"));
    assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.ATIVO);
  }

  @Test
  void successfulDeletionAnonymizesPersonalDataRevokesDataAndCreatesMinimalAudit() {
    User user = user(UserType.RESPONSAVEL);
    user.setProfilePhotoUrl("/api/profile-photos/00000000-0000-0000-0000-000000000001.jpg");
    ResponsibleProfile profile = new ResponsibleProfile();
    profile.setUser(user);
    profile.setParentescoOutro("Outro vínculo identificável");
    when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
    when(confirmations.findByTokenHashForUpdate(anyString()))
      .thenReturn(Optional.of(confirmation(user, Instant.now().plusSeconds(60))));
    when(responsibles.findByUser(user)).thenReturn(Optional.of(profile));

    var response = service.deleteAccount(user.getId(), "valido");

    assertThat(response.message()).isEqualTo("Conta excluída com sucesso.");
    assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.EXCLUIDO);
    assertThat(user.getFullName()).isEqualTo("Responsável removido");
    assertThat(user.getEmail()).isEqualTo("excluido_" + user.getId() + "@anon.local");
    assertThat(user.getPhone()).isNull();
    assertThat(user.getProfilePhotoUrl()).isNull();
    assertThat(user.getPasswordHash()).startsWith("!conta-excluida:");
    assertThat(profile.getParentescoOutro()).isNull();
    verify(passwordResetTokens).deleteByUser(user);
    verify(notifications).deleteByRecipient(user);
    verify(notificationPreferences).deleteByUser(user);

    ArgumentCaptor<AccountDeletionAudit> audit = ArgumentCaptor.forClass(AccountDeletionAudit.class);
    verify(audits).save(audit.capture());
    assertThat(audit.getValue().getUserReference()).isEqualTo(user.getId());
    assertThat(audit.getValue().getProfile()).isEqualTo(UserType.RESPONSAVEL);
    assertThat(audit.getValue().getResult()).isEqualTo("SUCESSO");
  }

  private User user(UserType type) {
    User user = new User();
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    user.setUserType(type);
    user.setFullName("Pessoa Teste");
    user.setEmail("pessoa@example.com");
    user.setPhone("11999999999");
    user.setMaiorDeIdadeConfirmado(true);
    user.setPasswordHash("hash");
    user.setAccountStatus(AccountStatus.ATIVO);
    return user;
  }

  private AccountDeletionConfirmation confirmation(User user, Instant expiresAt) {
    AccountDeletionConfirmation confirmation = new AccountDeletionConfirmation();
    confirmation.setUser(user);
    confirmation.setTokenHash("hash");
    confirmation.setExpiresAt(expiresAt);
    return confirmation;
  }
}
