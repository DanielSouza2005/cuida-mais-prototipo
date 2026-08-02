package br.com.cuidaplus.api.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateNotificationPreferencesRequest(@NotEmpty List<@Valid Preference> preferences) {
  public record Preference(@NotNull NotificationType type, @NotNull Boolean enabled) {}
}
