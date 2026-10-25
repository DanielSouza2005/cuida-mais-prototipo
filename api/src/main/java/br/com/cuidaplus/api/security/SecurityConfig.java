package br.com.cuidaplus.api.security;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final SecurityErrorWriter securityErrorWriter;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, SecurityErrorWriter securityErrorWriter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.securityErrorWriter = securityErrorWriter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
      .csrf(csrf -> csrf.disable())
      .cors(cors -> {})
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .exceptionHandling(exception -> exception
        .authenticationEntryPoint((request, response, authException) -> securityErrorWriter.write(
          response, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "Autenticação necessária."
        ))
        .accessDeniedHandler((request, response, accessDeniedException) -> securityErrorWriter.write(
          response, HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Acesso negado."
        ))
      )
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.GET, "/health").permitAll()
        .requestMatchers(HttpMethod.HEAD, "/health").permitAll()
        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/profile-photos/**").permitAll()
        .requestMatchers(
          HttpMethod.POST,
          "/api/auth/register",
          "/api/auth/register/responsible",
          "/api/auth/register/caregiver",
          "/api/auth/login",
          "/api/auth/forgot-password",
          "/api/auth/password-reset/verify-code",
          "/api/auth/reset-password"
        ).permitAll()
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated()
      )
      .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
      .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(
    @Value("${app.cors.allowed-origins}") String allowedOrigins
  ) {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList());
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
