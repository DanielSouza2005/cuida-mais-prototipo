package br.com.cuidaplus.api.notification;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ExpoPushNotificationService {
  private static final Logger log = LoggerFactory.getLogger(ExpoPushNotificationService.class);
  private static final int EXPO_BATCH_SIZE = 100;
  private static final String SAFE_BODY = "Abra o Cuidar+ para ver os detalhes.";
  private final RestClient client;
  private final PushTokenService tokens;
  private final boolean enabled;

  public ExpoPushNotificationService(RestClient expoPushRestClient, PushTokenService tokens, @Value("${app.push.enabled:true}") boolean enabled) {
    this.client = expoPushRestClient;
    this.tokens = tokens;
    this.enabled = enabled;
  }

  public void send(NotificationCreatedEvent event) {
    if (!enabled) return;
    try {
      List<String> activeTokens = tokens.activeTokens(event.recipientUserId());
      for (int start = 0; start < activeTokens.size(); start += EXPO_BATCH_SIZE) {
        List<String> batch = activeTokens.subList(start, Math.min(start + EXPO_BATCH_SIZE, activeTokens.size()));
        sendBatch(event, batch);
      }
    } catch (Exception exception) {
      log.warn("Falha ao preparar o push da notificação {}.", event.notificationId());
      log.debug("Tipo da falha no push: {}.", exception.getClass().getSimpleName());
    }
  }

  private void sendBatch(NotificationCreatedEvent event, List<String> batch) {
    List<ExpoPushMessage> messages = batch.stream().map(token -> message(event, token)).toList();
    try {
      ExpoPushResponse response = client.post().body(messages).retrieve().body(ExpoPushResponse.class);
      processTickets(batch, response == null ? null : response.data());
    } catch (Exception exception) {
      log.warn("Falha ao enviar lote de push da notificação {} para {} dispositivo(s).", event.notificationId(), batch.size());
      log.debug("Tipo da falha no envio de push: {}.", exception.getClass().getSimpleName());
    }
  }

  private ExpoPushMessage message(NotificationCreatedEvent event, String token) {
    Map<String, String> data = new LinkedHashMap<>();
    data.put("notificationId", event.notificationId().toString());
    data.put("notificationType", event.notificationType().name());
    data.put("relatedEntityType", event.relatedEntityType().name());
    data.put("relatedEntityId", event.relatedEntityId().toString());
    return new ExpoPushMessage(token, event.title(), SAFE_BODY, "default", "default", data);
  }

  private void processTickets(List<String> batch, List<ExpoPushTicket> tickets) {
    if (tickets == null) return;
    int count = Math.min(batch.size(), tickets.size());
    List<String> invalidTokens = new ArrayList<>();
    for (int index = 0; index < count; index++) {
      ExpoPushTicket ticket = tickets.get(index);
      if (ticket != null && ticket.details() != null && "DeviceNotRegistered".equals(ticket.details().error())) invalidTokens.add(batch.get(index));
    }
    invalidTokens.forEach(tokens::disableInvalid);
    if (!invalidTokens.isEmpty()) log.info("{} token(s) de push inválido(s) foram desativados.", invalidTokens.size());
  }

  record ExpoPushMessage(String to, String title, String body, String sound, String channelId, Map<String, String> data) {}
  record ExpoPushResponse(List<ExpoPushTicket> data) {}
  record ExpoPushTicket(String status, String id, String message, ExpoPushErrorDetails details) {}
  record ExpoPushErrorDetails(String error) {}
}
