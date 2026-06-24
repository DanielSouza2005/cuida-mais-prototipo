package br.com.cuidaplus.api.health;

import java.time.Instant;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  @GetMapping("/health")
  public ResponseEntity<HealthResponse> health() {
    return ResponseEntity
      .ok()
      .cacheControl(CacheControl.noStore())
      .body(new HealthResponse("UP", "cuida-plus-api", Instant.now()));
  }

  public record HealthResponse(
    String status,
    String service,
    Instant timestamp
  ) {}
}
