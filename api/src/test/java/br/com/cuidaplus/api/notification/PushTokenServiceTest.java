package br.com.cuidaplus.api.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PushTokenServiceTest {
  private final UserPushTokenRepository repository = mock(UserPushTokenRepository.class);
  private final UserService users = mock(UserService.class);
  private final PushTokenService service = new PushTokenService(repository, users);

  @Test
  void reassignsAnExistingDeviceTokenToTheAuthenticatedUser() {
    UUID userId = UUID.randomUUID();
    User currentUser = mock(User.class);
    UserPushToken existing = new UserPushToken();
    existing.setActive(false);
    existing.setDisabledAt(java.time.Instant.now());
    when(users.findById(userId)).thenReturn(currentUser);
    when(repository.findByExpoPushToken("ExponentPushToken[device]")).thenReturn(Optional.of(existing));
    when(repository.save(any(UserPushToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

    PushTokenResponse response = service.register(userId, new RegisterPushTokenRequest("ExponentPushToken[device]", "ANDROID", null, "1.0.0"));

    assertSame(currentUser, existing.getUser());
    assertTrue(existing.isActive());
    assertEquals("android", response.platform());
    verify(repository).save(existing);
  }

  @Test
  void onlyDisablesTheTokenWhenItBelongsToTheAuthenticatedUser() {
    UUID userId = UUID.randomUUID();
    User owner = mock(User.class);
    when(owner.getId()).thenReturn(userId);
    UserPushToken token = new UserPushToken();
    token.setUser(owner);
    token.setActive(true);
    when(repository.findByExpoPushToken("ExpoPushToken[device]")).thenReturn(Optional.of(token));

    service.disable(userId, "ExpoPushToken[device]");

    assertFalse(token.isActive());
  }
}
