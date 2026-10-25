package br.com.cuidaplus.api.auth;

import br.com.cuidaplus.api.common.BusinessException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetRateLimiter {
  private static final int MAX_REQUESTS = 10;
  private static final Duration WINDOW = Duration.ofMinutes(10);
  private final Map<String, Deque<Instant>> requests = new ConcurrentHashMap<>();

  public void requireAllowed(String origin) {
    String key = digest(origin == null || origin.isBlank() ? "unknown" : origin);
    Instant now = Instant.now();
    Deque<Instant> history = requests.computeIfAbsent(key, ignored -> new ArrayDeque<>());
    synchronized (history) {
      Instant cutoff = now.minus(WINDOW);
      while (!history.isEmpty() && history.peekFirst().isBefore(cutoff)) history.removeFirst();
      if (history.size() >= MAX_REQUESTS) {
        throw new BusinessException("Muitas solicitações de recuperação. Aguarde alguns minutos e tente novamente.", HttpStatus.TOO_MANY_REQUESTS, "PASSWORD_RESET_RATE_LIMITED");
      }
      history.addLast(now);
    }
    if (requests.size() > 10_000) requests.entrySet().removeIf(entry -> entry.getValue().isEmpty());
  }

  private String digest(String value) {
    try {
      byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (Exception exception) {
      throw new IllegalStateException("Não foi possível aplicar o limite de recuperação.", exception);
    }
  }
}
