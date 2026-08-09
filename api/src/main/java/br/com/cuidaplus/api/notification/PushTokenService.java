package br.com.cuidaplus.api.notification;

import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserService;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PushTokenService {
  private final UserPushTokenRepository repository;
  private final UserService users;

  public PushTokenService(UserPushTokenRepository repository, UserService users) {
    this.repository = repository;
    this.users = users;
  }

  @Transactional
  public PushTokenResponse register(UUID userId, RegisterPushTokenRequest request) {
    String platform = request.platform().trim().toLowerCase(Locale.ROOT);
    if (!platform.equals("android") && !platform.equals("ios")) {
      throw new BusinessException("Plataforma do dispositivo inválida.", HttpStatus.BAD_REQUEST);
    }
    User user = users.findById(userId);
    Instant now = Instant.now();
    UserPushToken token = repository.findByExpoPushToken(request.token()).orElseGet(UserPushToken::new);
    token.setUser(user);
    token.setExpoPushToken(request.token());
    token.setPlatform(platform);
    token.setDeviceId(blankToNull(request.deviceId()));
    token.setAppVersion(blankToNull(request.appVersion()));
    token.setActive(true);
    token.setDisabledAt(null);
    token.setLastUsedAt(now);
    UserPushToken saved = repository.save(token);
    return new PushTokenResponse(saved.getId(), saved.getPlatform(), saved.isActive(), saved.getLastUsedAt());
  }

  @Transactional
  public void disable(UUID userId, String expoPushToken) {
    repository.findByExpoPushToken(expoPushToken)
      .filter(token -> token.getUser().getId().equals(userId))
      .ifPresent(this::disable);
  }

  @Transactional
  public void disableInvalid(String expoPushToken) {
    repository.findByExpoPushToken(expoPushToken).ifPresent(this::disable);
  }

  @Transactional(readOnly = true)
  public List<String> activeTokens(UUID userId) {
    return repository.findByUserIdAndActiveTrue(userId).stream()
      .map(UserPushToken::getExpoPushToken)
      .distinct()
      .toList();
  }

  private void disable(UserPushToken token) {
    token.setActive(false);
    token.setDisabledAt(Instant.now());
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
