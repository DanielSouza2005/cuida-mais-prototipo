package br.com.cuidaplus.api.notification;

import java.util.List;

public record NotificationPreferenceResponse(List<Group> groups) {
  public record Group(String category, String categoryLabel, List<Item> items) {}
  public record Item(NotificationType type, String label, String description, boolean enabled, boolean configurable, boolean required, String icon, String colorKey) {}
}
