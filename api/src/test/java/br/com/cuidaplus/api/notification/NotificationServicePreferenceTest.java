package br.com.cuidaplus.api.notification;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NotificationServicePreferenceTest {
  private final NotificationRepository repository = mock(NotificationRepository.class);
  private final NotificationPreferenceService preferences = mock(NotificationPreferenceService.class);
  private final NotificationService service = new NotificationService(repository, mock(UserService.class), preferences);
  private final User recipient = new User();

  @Test
  void doesNotPersistNewNotificationWhenPreferenceIsDisabled() {
    when(preferences.isEnabled(recipient, NotificationType.SERVICE_REQUEST_CREATED)).thenReturn(false);

    service.create(recipient, NotificationType.SERVICE_REQUEST_CREATED, "Nova solicitação", "Mensagem", RelatedEntityType.SERVICE_REQUEST, UUID.randomUUID());

    verify(repository, never()).save(org.mockito.ArgumentMatchers.any(Notification.class));
  }

  @Test
  void persistsNotificationWhenPreferenceIsEnabled() {
    when(preferences.isEnabled(recipient, NotificationType.SERVICE_REQUEST_CREATED)).thenReturn(true);

    service.create(recipient, NotificationType.SERVICE_REQUEST_CREATED, "Nova solicitação", "Mensagem", RelatedEntityType.SERVICE_REQUEST, UUID.randomUUID());

    verify(repository).save(org.mockito.ArgumentMatchers.any(Notification.class));
  }

  @Test
  void ignoresRepeatedNotificationWithTheSameCanonicalEventKey() {
    UUID relatedId = UUID.randomUUID();
    when(preferences.isEnabled(recipient, NotificationType.CARE_OCCURRENCE_OVERDUE)).thenReturn(true);
    when(repository.existsByDeduplicationKey(org.mockito.ArgumentMatchers.anyString())).thenReturn(true);

    service.create(recipient, NotificationType.CARE_TASK_OVERDUE, "Cuidado atrasado", "Mensagem", RelatedEntityType.CARE_OCCURRENCE, relatedId);

    verify(repository, never()).save(org.mockito.ArgumentMatchers.any(Notification.class));
  }

  @Test
  void storesLegacyTypeAsCanonicalType() {
    when(preferences.isEnabled(recipient, NotificationType.CARE_OCCURRENCE_COMPLETED)).thenReturn(true);
    ArgumentCaptor<Notification> notification = ArgumentCaptor.forClass(Notification.class);

    service.create(recipient, NotificationType.TASK_OCCURRENCE_COMPLETED, "Cuidado concluído", "Mensagem", RelatedEntityType.CARE_OCCURRENCE, UUID.randomUUID());

    verify(repository).save(notification.capture());
    org.junit.jupiter.api.Assertions.assertEquals(NotificationType.CARE_OCCURRENCE_COMPLETED, notification.getValue().getType());
  }
}
