package br.com.cuidaplus.api.notification;

import java.time.Instant;
import java.util.UUID;

public record PushTokenResponse(UUID id, String platform, boolean active, Instant lastUsedAt) {}
