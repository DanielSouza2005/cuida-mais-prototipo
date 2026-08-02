package br.com.cuidaplus.api.notification;

import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserService;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationPreferenceService {
  private final UserNotificationPreferenceRepository repository;
  private final UserService users;

  public NotificationPreferenceService(UserNotificationPreferenceRepository repository, UserService users) {
    this.repository = repository;
    this.users = users;
  }

  @Transactional(readOnly = true)
  public boolean isEnabled(User user, NotificationType type) {
    type = NotificationCatalog.canonical(type);
    NotificationDefinition definition = NotificationCatalog.get(type);
    if (definition == null || definition.required() || !definition.configurable()) return true;
    return repository.findByUserAndNotificationType(user, type)
      .map(UserNotificationPreference::isEnabled)
      .orElse(definition.defaultEnabled());
  }

  @Transactional(readOnly = true)
  public NotificationPreferenceResponse get(UUID userId) {
    return response(users.findById(userId));
  }

  @Transactional
  public NotificationPreferenceResponse update(UUID userId, UpdateNotificationPreferencesRequest request) {
    User user = users.findById(userId);
    Map<NotificationType, Boolean> normalized = new LinkedHashMap<>();
    request.preferences().forEach(item -> normalized.put(NotificationCatalog.canonical(item.type()), item.enabled()));
    for (Map.Entry<NotificationType, Boolean> item : normalized.entrySet()) {
      NotificationType canonicalType = item.getKey();
      NotificationDefinition definition = NotificationCatalog.get(canonicalType);
      if (definition == null || !definition.audience().accepts(user.getUserType())) {
        throw new BusinessException("Preferência de notificação não disponível para este perfil.", HttpStatus.BAD_REQUEST);
      }
      if (!definition.configurable() || definition.required()) {
        throw new BusinessException("Esta notificação é obrigatória e não pode ser desativada.", HttpStatus.BAD_REQUEST);
      }
      UserNotificationPreference preference = repository.findByUserAndNotificationType(user, canonicalType).orElseGet(() -> {
        UserNotificationPreference created = new UserNotificationPreference();
        created.setUser(user);
        created.setNotificationType(canonicalType);
        return created;
      });
      preference.setEnabled(item.getValue());
      repository.save(preference);
    }
    return response(user);
  }

  private NotificationPreferenceResponse response(User user) {
    Map<NotificationType, Boolean> saved = new EnumMap<>(NotificationType.class);
    repository.findByUser(user).forEach(preference -> saved.put(NotificationCatalog.canonical(preference.getNotificationType()), preference.isEnabled()));
    Map<NotificationCategory, List<NotificationPreferenceResponse.Item>> groups = new LinkedHashMap<>();
    EnumSet<NotificationType> includedTypes = EnumSet.noneOf(NotificationType.class);
    for (NotificationDefinition definition : NotificationCatalog.forUserType(user.getUserType())) {
      if (!includedTypes.add(definition.type())) continue;
      boolean enabled = definition.required() || saved.getOrDefault(definition.type(), definition.defaultEnabled());
      groups.computeIfAbsent(definition.category(), ignored -> new ArrayList<>()).add(new NotificationPreferenceResponse.Item(
        definition.type(), definition.label(), definition.description(), enabled, definition.configurable(), definition.required(), definition.icon(), definition.colorKey()
      ));
    }
    List<NotificationPreferenceResponse.Group> result = groups.entrySet().stream()
      .map(entry -> new NotificationPreferenceResponse.Group(entry.getKey().name(), entry.getKey().getLabel(), entry.getValue()))
      .toList();
    return new NotificationPreferenceResponse(result);
  }
}
