package br.com.cuidaplus.api.notification;

import java.util.UUID;

public record NotificationCreatedEvent(
  UUID notificationId,
  UUID recipientUserId,
  NotificationType notificationType,
  String title,
  RelatedEntityType relatedEntityType,
  UUID relatedEntityId
) {}
