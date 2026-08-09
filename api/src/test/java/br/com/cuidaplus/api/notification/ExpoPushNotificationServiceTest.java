package br.com.cuidaplus.api.notification;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ExpoPushNotificationServiceTest {
  @Test
  void sendsOnlyOperationalDataAndDisablesAnInvalidToken() {
    String expoToken = "ExponentPushToken[device]";
    PushTokenService tokens = mock(PushTokenService.class);
    UUID userId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();
    UUID occurrenceId = UUID.randomUUID();
    when(tokens.activeTokens(userId)).thenReturn(List.of(expoToken, expoToken));
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    ExpoPushNotificationService service = new ExpoPushNotificationService(builder.baseUrl("https://exp.host/--/api/v2/push/send").build(), tokens, true);
    server.expect(once(), requestTo("https://exp.host/--/api/v2/push/send"))
      .andExpect(method(HttpMethod.POST))
      .andExpect(content().string(containsString("Abra o Cuidar+ para ver os detalhes.")))
      .andExpect(content().string(containsString(notificationId.toString())))
      .andExpect(content().string(not(containsString("medicamento"))))
      .andRespond(withSuccess("{\"data\":[{\"status\":\"error\",\"details\":{\"error\":\"DeviceNotRegistered\"}}]}", MediaType.APPLICATION_JSON));

    service.send(new NotificationCreatedEvent(notificationId, userId, NotificationType.CARE_OCCURRENCE_REMINDER, "Lembrete de cuidado", RelatedEntityType.CARE_OCCURRENCE, occurrenceId));

    server.verify();
    verify(tokens).disableInvalid(expoToken);
  }
}
