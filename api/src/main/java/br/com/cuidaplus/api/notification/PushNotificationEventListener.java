package br.com.cuidaplus.api.notification;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PushNotificationEventListener {
  private final ExpoPushNotificationService pushNotifications;
  public PushNotificationEventListener(ExpoPushNotificationService pushNotifications) { this.pushNotifications = pushNotifications; }

  @Async("pushNotificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void afterNotificationCreated(NotificationCreatedEvent event) { pushNotifications.send(event); }
}
