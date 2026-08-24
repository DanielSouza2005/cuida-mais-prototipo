package br.com.cuidaplus.api.service_attendance;

import java.time.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AttendanceTimeConfig {
  public static final ZoneId SERVICE_ZONE = ZoneId.of("America/Sao_Paulo");

  @Bean
  public Clock attendanceClock() { return Clock.system(SERVICE_ZONE); }
}
