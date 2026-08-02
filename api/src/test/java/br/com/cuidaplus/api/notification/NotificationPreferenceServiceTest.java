package br.com.cuidaplus.api.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserService;
import br.com.cuidaplus.api.user.UserType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NotificationPreferenceServiceTest {
  @Test
  void consolidatesLegacyAndCanonicalItemsIntoOnePreference() {
    UserNotificationPreferenceRepository repository = mock(UserNotificationPreferenceRepository.class);
    UserService users = mock(UserService.class);
    NotificationPreferenceService service = new NotificationPreferenceService(repository, users);
    User user = new User();
    user.setUserType(UserType.RESPONSAVEL);
    UUID userId = UUID.randomUUID();
    when(users.findById(userId)).thenReturn(user);
    when(repository.findByUserAndNotificationType(user, NotificationType.CARE_OCCURRENCE_COMPLETED)).thenReturn(Optional.empty());
    when(repository.findByUser(user)).thenReturn(List.of());
    ArgumentCaptor<UserNotificationPreference> saved = ArgumentCaptor.forClass(UserNotificationPreference.class);

    service.update(userId, new UpdateNotificationPreferencesRequest(List.of(
      new UpdateNotificationPreferencesRequest.Preference(NotificationType.TASK_OCCURRENCE_COMPLETED, false),
      new UpdateNotificationPreferencesRequest.Preference(NotificationType.CARE_OCCURRENCE_COMPLETED, true)
    )));

    verify(repository).save(saved.capture());
    assertEquals(NotificationType.CARE_OCCURRENCE_COMPLETED, saved.getValue().getNotificationType());
    assertEquals(true, saved.getValue().isEnabled());
  }

  @Test
  void responsibleResponseContainsTheReportedLabelsOnlyOnce() {
    NotificationPreferenceResponse response = responseFor(UserType.RESPONSAVEL);

    assertEquals(1, countLabel(response, "Cuidados concluídos"));
    assertEquals(1, countLabel(response, "Alertas de cuidados importantes"));
    assertUniqueTypes(response);
  }

  @Test
  void caregiverResponseContainsTheReportedLabelsOnlyOnce() {
    NotificationPreferenceResponse response = responseFor(UserType.CUIDADOR);

    assertEquals(1, countLabel(response, "Lembretes de cuidados"));
    assertEquals(1, countLabel(response, "Cuidados atrasados"));
    assertUniqueTypes(response);
  }

  private NotificationPreferenceResponse responseFor(UserType userType) {
    UserNotificationPreferenceRepository repository = mock(UserNotificationPreferenceRepository.class);
    UserService users = mock(UserService.class);
    NotificationPreferenceService service = new NotificationPreferenceService(repository, users);
    User user = new User();
    user.setUserType(userType);
    UUID userId = UUID.randomUUID();
    when(users.findById(userId)).thenReturn(user);
    when(repository.findByUser(user)).thenReturn(List.of());
    return service.get(userId);
  }

  private long countLabel(NotificationPreferenceResponse response, String label) {
    return response.groups().stream().flatMap(group -> group.items().stream()).filter(item -> item.label().equals(label)).count();
  }

  private void assertUniqueTypes(NotificationPreferenceResponse response) {
    var types = response.groups().stream().flatMap(group -> group.items().stream()).map(NotificationPreferenceResponse.Item::type).toList();
    assertEquals(types.size(), new java.util.HashSet<>(types).size());
  }
}
