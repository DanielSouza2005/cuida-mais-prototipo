package br.com.cuidaplus.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class OpenApiConfig {

  private static final String BEARER_AUTH = "bearerAuth";
  private static final String API_ERROR = "#/components/schemas/ApiError";
  private static final Set<String> PUBLIC_POST_PATHS = Set.of(
    "/api/auth/register",
    "/api/auth/register/responsible",
    "/api/auth/register/caregiver",
    "/api/auth/login",
    "/api/auth/forgot-password",
    "/api/auth/reset-password"
  );

  @Bean
  public OpenAPI cuidarPlusOpenApi() {
    return new OpenAPI()
      .info(new Info()
        .title("Cuidar+ API")
        .version("v1")
        .description("Contrato HTTP da plataforma Cuidar+. Datas usam ISO 8601; endpoints protegidos usam Bearer JWT."))
      .components(new Components()
        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
          .name(BEARER_AUTH)
          .type(SecurityScheme.Type.HTTP)
          .scheme("bearer")
          .bearerFormat("JWT")
          .description("Token retornado pelo endpoint de login."))
        .addSchemas("ApiError", apiErrorSchema()));
  }

  @Bean
  public OpenApiCustomizer apiContractCustomizer() {
    return openApi -> {
      openApi.getComponents().addSchemas("ApiError", apiErrorSchema());
      openApi.getPaths().forEach((path, pathItem) ->
        pathItem.readOperationsMap().forEach((method, operation) -> {
        boolean protectedEndpoint = !isPublic(path, method.name());
        if (protectedEndpoint) {
          operation.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
          addError(operation, "401", "Credencial ausente ou inválida");
          addError(operation, "403", "Usuário autenticado sem acesso ao recurso");
        }
        addError(operation, "400", "Requisição inválida");
        addError(operation, "500", "Erro interno");
        describeCompatibilityAlias(path, method.name(), operation);
        })
      );
    };
  }

  private Schema<?> apiErrorSchema() {
    return new ObjectSchema()
      .addProperty("timestamp", new DateTimeSchema().description("Instante UTC do erro"))
      .addProperty("status", new IntegerSchema().format("int32"))
      .addProperty("code", new StringSchema().description("Código estável para tratamento pelo cliente"))
      .addProperty("message", new StringSchema())
      .addProperty("fields", new MapSchema().additionalProperties(new StringSchema()))
      .required(java.util.List.of("timestamp", "status", "code", "message", "fields"));
  }

  private boolean isPublic(String path, String method) {
    if (path.equals("/health") && method.equals(HttpMethod.GET.name())) {
      return true;
    }
    if (path.startsWith("/api/profile-photos/") && method.equals(HttpMethod.GET.name())) {
      return true;
    }
    return method.equals(HttpMethod.POST.name()) && PUBLIC_POST_PATHS.contains(path);
  }

  private void addError(Operation operation, String status, String description) {
    if (operation.getResponses().containsKey(status)) {
      return;
    }
    operation.getResponses().addApiResponse(status, new ApiResponse()
      .description(description)
      .content(new Content().addMediaType("application/json", new MediaType()
        .schema(new Schema<>().$ref(API_ERROR)))));
  }

  private void describeCompatibilityAlias(String path, String method, Operation operation) {
    String canonical = legacyCanonicalPath(path, method);
    if (canonical == null) {
      return;
    }
    operation.setDeprecated(true);
    String notice = "Alias mantido temporariamente para compatibilidade. Use `" + canonical + "`.";
    operation.setDescription(operation.getDescription() == null
      ? notice
      : operation.getDescription() + "\n\n" + notice);
  }

  private String legacyCanonicalPath(String path, String method) {
    if (path.startsWith("/api/caregiver/tasks")) {
      return path.replaceFirst("/api/caregiver/tasks", "/api/caregiver/care-tasks");
    }
    if (path.equals("/api/caregiver/care-tasks/{occurrenceId}")) {
      return "/api/caregiver/care-tasks/occurrences/{occurrenceId}";
    }
    if (path.equals("/api/caregiver/care-tasks/{occurrenceId}/complete")) {
      return "/api/caregiver/care-tasks/occurrences/{occurrenceId}/complete";
    }
    if (path.equals("/api/caregiver/care-tasks/{occurrenceId}/not-done")) {
      return "/api/caregiver/care-tasks/occurrences/{occurrenceId}/not-completed";
    }
    if (path.equals("/api/contracts/{contractId}/attendance") && method.equals(HttpMethod.GET.name())) {
      return "/api/{role}/contracts/{contractId}/attendance";
    }
    if (path.equals("/api/contracts/{contractId}/attendance/{date}/report") && method.equals(HttpMethod.GET.name())) {
      return "/api/{role}/contracts/{contractId}/attendance/{date}/report";
    }
    if (path.equals("/api/agenda/events/detail") && method.equals(HttpMethod.GET.name())) {
      return "/api/agenda/events/contracts/{contractId}/dates/{eventDate}";
    }
    if (path.matches("/api/(responsible|caregiver)/service-requests/\\{id}/(accept|reject)")
      && method.equals(HttpMethod.POST.name())) {
      return "PATCH " + path;
    }
    return null;
  }
}
