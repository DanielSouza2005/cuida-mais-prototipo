package br.com.cuidaplus.api.notification;

public record NotificationDefinition(
  NotificationType type,
  String label,
  String description,
  NotificationAudience audience,
  NotificationCategory category,
  String icon,
  String colorKey,
  boolean configurable,
  boolean required,
  boolean defaultEnabled,
  RelatedEntityType relatedEntityType
) {}
