package br.com.cuidaplus.api.security;

import br.com.cuidaplus.api.common.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  private final String secret;
  private final long expirationMinutes;
  private final ObjectMapper objectMapper;

  public TokenService(
    @Value("${app.jwt.secret}") String secret,
    @Value("${app.jwt.expiration-minutes}") long expirationMinutes,
    ObjectMapper objectMapper
  ) {
    this.secret = secret;
    this.expirationMinutes = expirationMinutes;
    this.objectMapper = objectMapper;
  }

  public String generate(UUID userId) {
    try {
      Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("sub", userId.toString());
      payload.put("exp", Instant.now().plusSeconds(expirationMinutes * 60).getEpochSecond());

      String headerPart = encodeJson(header);
      String payloadPart = encodeJson(payload);
      String signature = sign(headerPart + "." + payloadPart);
      return headerPart + "." + payloadPart + "." + signature;
    } catch (Exception exception) {
      throw new BusinessException("Não foi possível gerar o token.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public UUID validateAndGetUserId(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
        throw new IllegalArgumentException("Token inválido.");
      }

      String expectedSignature = sign(parts[0] + "." + parts[1]);
      if (!constantTimeEquals(expectedSignature, parts[2])) {
        throw new IllegalArgumentException("Assinatura inválida.");
      }

      byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
      Map<String, Object> payload = objectMapper.readValue(payloadBytes, new TypeReference<>() {});
      long expiration = ((Number) payload.get("exp")).longValue();
      if (Instant.now().getEpochSecond() > expiration) {
        throw new IllegalArgumentException("Token expirado.");
      }

      return UUID.fromString(String.valueOf(payload.get("sub")));
    } catch (Exception exception) {
      throw new BusinessException("Token inválido ou expirado.", HttpStatus.UNAUTHORIZED);
    }
  }

  private String encodeJson(Map<String, Object> value) throws Exception {
    byte[] json = objectMapper.writeValueAsBytes(value);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
  }

  private String sign(String value) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
  }

  private boolean constantTimeEquals(String left, String right) {
    return MessageDigestSupport.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
  }
}
