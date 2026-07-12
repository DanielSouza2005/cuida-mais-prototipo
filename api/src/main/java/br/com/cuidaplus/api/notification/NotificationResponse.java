package br.com.cuidaplus.api.notification;
import java.time.Instant; import java.util.UUID;
public record NotificationResponse(UUID id,NotificationType type,String title,String message,RelatedEntityType relatedEntityType,UUID relatedEntityId,Instant readAt,Instant createdAt){}
