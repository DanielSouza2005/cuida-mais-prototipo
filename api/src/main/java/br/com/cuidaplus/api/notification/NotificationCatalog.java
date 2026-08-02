package br.com.cuidaplus.api.notification;

import br.com.cuidaplus.api.user.UserType;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NotificationCatalog {
  private static final Map<NotificationType, NotificationType> LEGACY_ALIASES = Map.of(
    NotificationType.TASK_OCCURRENCE_COMPLETED, NotificationType.CARE_OCCURRENCE_COMPLETED,
    NotificationType.TASK_OCCURRENCE_NOT_COMPLETED, NotificationType.CARE_OCCURRENCE_NOT_DONE,
    NotificationType.CARE_TASK_REMINDER, NotificationType.CARE_OCCURRENCE_REMINDER,
    NotificationType.CARE_TASK_OVERDUE, NotificationType.CARE_OCCURRENCE_OVERDUE,
    NotificationType.CARE_TASK_NOT_DONE, NotificationType.CARE_OCCURRENCE_NOT_DONE,
    NotificationType.CARE_TASK_RESPONSIBLE_ALERT, NotificationType.CARE_OCCURRENCE_RESPONSIBLE_ALERT
  );
  private static final Map<NotificationType, NotificationDefinition> DEFINITIONS = definitions();

  private NotificationCatalog() {}

  public static NotificationDefinition get(NotificationType type) {
    return DEFINITIONS.get(canonical(type));
  }

  public static NotificationType canonical(NotificationType type) {
    return LEGACY_ALIASES.getOrDefault(type, type);
  }

  public static List<NotificationDefinition> forUserType(UserType userType) {
    Map<NotificationType, NotificationDefinition> unique = new LinkedHashMap<>();
    for (NotificationType type : NotificationType.values()) {
      NotificationDefinition definition = get(type);
      if (definition == null || (!definition.configurable() && !definition.required())) continue;
      if (!definition.audience().accepts(userType)) continue;
      unique.putIfAbsent(definition.type(), definition);
    }
    return List.copyOf(unique.values());
  }

  private static Map<NotificationType, NotificationDefinition> definitions() {
    EnumMap<NotificationType, NotificationDefinition> values = new EnumMap<>(NotificationType.class);
    add(values, NotificationType.SERVICE_REQUEST_CREATED, "Novas solicitações", "Receba avisos quando uma nova solicitação de serviço chegar.", NotificationAudience.CUIDADOR, NotificationCategory.SOLICITACOES, "clipboard-plus", "blue", RelatedEntityType.SERVICE_REQUEST);
    add(values, NotificationType.SERVICE_REQUEST_ACCEPTED, "Solicitações aceitas", "Receba avisos quando o cuidador aceitar uma solicitação.", NotificationAudience.RESPONSAVEL, NotificationCategory.SOLICITACOES, "circle-check", "green", RelatedEntityType.SERVICE_REQUEST);
    add(values, NotificationType.SERVICE_REQUEST_REJECTED, "Solicitações rejeitadas", "Receba avisos quando o cuidador rejeitar uma solicitação.", NotificationAudience.RESPONSAVEL, NotificationCategory.SOLICITACOES, "circle-x", "red", RelatedEntityType.SERVICE_REQUEST);
    add(values, NotificationType.SERVICE_REQUEST_CANCELED, "Solicitações canceladas", "Receba avisos quando uma solicitação for cancelada.", NotificationAudience.CUIDADOR, NotificationCategory.SOLICITACOES, "ban", "slate", RelatedEntityType.SERVICE_REQUEST);
    add(values, NotificationType.SERVICE_REQUEST_EXPIRED, "Solicitações expiradas", "Avisos históricos sobre solicitações cujo prazo terminou.", NotificationAudience.CUIDADOR, NotificationCategory.SOLICITACOES, "clock-alert", "orange", false, RelatedEntityType.SERVICE_REQUEST);

    add(values, NotificationType.CONTRACT_TERMINATION_SCHEDULED, "Encerramentos agendados", "Receba avisos quando o encerramento de um serviço for agendado.", NotificationAudience.AMBOS, NotificationCategory.CONTRATACOES, "calendar-clock", "amber", RelatedEntityType.CARE_CONTRACT);
    add(values, NotificationType.CONTRACT_TERMINATED, "Serviços encerrados", "Receba avisos quando uma contratação for encerrada.", NotificationAudience.AMBOS, NotificationCategory.CONTRATACOES, "flag", "purple", RelatedEntityType.CARE_CONTRACT);
    add(values, NotificationType.CONTRACT_CANCELED_BEFORE_START, "Contratações canceladas", "Receba avisos quando uma contratação for cancelada antes do início.", NotificationAudience.AMBOS, NotificationCategory.CONTRATACOES, "calendar-x", "rose", RelatedEntityType.CARE_CONTRACT);
    add(values, NotificationType.CONTRACT_AUTOMATICALLY_TERMINATED, "Encerramentos automáticos", "Receba avisos quando uma contratação terminar automaticamente.", NotificationAudience.AMBOS, NotificationCategory.CONTRATACOES, "calendar-check", "indigo", RelatedEntityType.CARE_CONTRACT);

    add(values, NotificationType.CARE_TASK_CREATED, "Novos cuidados", "Receba avisos quando um cuidado for adicionado à rotina.", NotificationAudience.CUIDADOR, NotificationCategory.CUIDADOS, "heart-plus", "cyan", RelatedEntityType.CARE_TASK);
    add(values, NotificationType.CARE_TASK_CANCELED, "Cuidados cancelados", "Receba avisos quando um cuidado da rotina for cancelado.", NotificationAudience.CUIDADOR, NotificationCategory.CUIDADOS, "heart-off", "pink", RelatedEntityType.CARE_TASK);
    add(values, NotificationType.CARE_OCCURRENCE_REMINDER, "Lembretes de cuidados", "Receba lembretes dos cuidados programados.", NotificationAudience.CUIDADOR, NotificationCategory.CUIDADOS, "alarm-clock", "azure", RelatedEntityType.CARE_OCCURRENCE);
    add(values, NotificationType.CARE_OCCURRENCE_OVERDUE, "Cuidados atrasados", "Receba avisos quando um cuidado ficar atrasado.", NotificationAudience.CUIDADOR, NotificationCategory.CUIDADOS, "clock-alert", "orange", RelatedEntityType.CARE_OCCURRENCE);
    add(values, NotificationType.CARE_OCCURRENCE_NOT_DONE, "Cuidados importantes não realizados", "Receba avisos quando um cuidado importante não for realizado.", NotificationAudience.RESPONSAVEL, NotificationCategory.CUIDADOS, "triangle-alert", "red", RelatedEntityType.CARE_OCCURRENCE);
    add(values, NotificationType.CARE_OCCURRENCE_COMPLETED, "Cuidados concluídos", "Receba avisos quando um cuidado for concluído.", NotificationAudience.RESPONSAVEL, NotificationCategory.CUIDADOS, "circle-check", "green", RelatedEntityType.CARE_OCCURRENCE);
    add(values, NotificationType.CARE_OCCURRENCE_RESPONSIBLE_ALERT, "Alertas de cuidados importantes", "Receba alertas quando um cuidado importante estiver pendente.", NotificationAudience.RESPONSAVEL, NotificationCategory.CUIDADOS, "shield-alert", "amber", RelatedEntityType.CARE_OCCURRENCE);
    return Map.copyOf(values);
  }

  private static void add(Map<NotificationType, NotificationDefinition> values, NotificationType type, String label, String description, NotificationAudience audience, NotificationCategory category, String icon, String colorKey, RelatedEntityType relatedEntityType) {
    add(values, type, label, description, audience, category, icon, colorKey, true, relatedEntityType);
  }

  private static void add(Map<NotificationType, NotificationDefinition> values, NotificationType type, String label, String description, NotificationAudience audience, NotificationCategory category, String icon, String colorKey, boolean configurable, RelatedEntityType relatedEntityType) {
    values.put(type, new NotificationDefinition(type, label, description, audience, category, icon, colorKey, configurable, false, true, relatedEntityType));
  }
}
