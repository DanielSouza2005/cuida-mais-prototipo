package br.com.cuidaplus.api.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.cuidaplus.api.user.UserType;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

class NotificationCatalogTest {
  @Test
  void catalogHasMetadataForEveryNotificationType() {
    Arrays.stream(NotificationType.values()).forEach(type -> {
      NotificationDefinition definition = NotificationCatalog.get(type);
      assertNotNull(definition, () -> "Tipo sem catálogo: " + type);
      assertFalse(definition.label().isBlank());
      assertFalse(definition.description().isBlank());
      assertFalse(definition.icon().isBlank());
      assertFalse(definition.colorKey().isBlank());
      assertNotNull(definition.category());
      assertNotNull(definition.relatedEntityType());
    });
  }

  @Test
  void catalogSeparatesCaregiverAndResponsibleTypes() {
    var caregiver = NotificationCatalog.forUserType(UserType.CUIDADOR);
    var responsible = NotificationCatalog.forUserType(UserType.RESPONSAVEL);

    assertTrue(caregiver.stream().anyMatch(item -> item.type() == NotificationType.SERVICE_REQUEST_CREATED));
    assertFalse(caregiver.stream().anyMatch(item -> item.type() == NotificationType.SERVICE_REQUEST_ACCEPTED));
    assertTrue(responsible.stream().anyMatch(item -> item.type() == NotificationType.SERVICE_REQUEST_ACCEPTED));
    assertFalse(responsible.stream().anyMatch(item -> item.type() == NotificationType.SERVICE_REQUEST_CREATED));
    assertFalse(caregiver.stream().anyMatch(item -> item.type() == NotificationType.CARE_TASK_REMINDER));
    assertFalse(responsible.stream().anyMatch(item -> item.type() == NotificationType.TASK_OCCURRENCE_COMPLETED));
    assertEquals(caregiver.size(), new HashSet<>(caregiver.stream().map(NotificationDefinition::type).toList()).size());
    assertEquals(responsible.size(), new HashSet<>(responsible.stream().map(NotificationDefinition::type).toList()).size());
    assertEquals(caregiver.size(), new HashSet<>(caregiver.stream().map(NotificationDefinition::label).toList()).size());
    assertEquals(responsible.size(), new HashSet<>(responsible.stream().map(NotificationDefinition::label).toList()).size());
    assertEquals(
      NotificationCatalog.forUserType(UserType.CUIDADOR).size(),
      NotificationCatalog.forUserType(UserType.CAREGIVER).size()
    );
  }

  @Test
  void legacyTypesResolveToCanonicalDefinitions() {
    assertEquals(NotificationType.CARE_OCCURRENCE_COMPLETED, NotificationCatalog.canonical(NotificationType.TASK_OCCURRENCE_COMPLETED));
    assertEquals(NotificationType.CARE_OCCURRENCE_OVERDUE, NotificationCatalog.get(NotificationType.CARE_TASK_OVERDUE).type());
    assertEquals(NotificationType.CARE_OCCURRENCE_NOT_DONE, NotificationCatalog.get(NotificationType.TASK_OCCURRENCE_NOT_COMPLETED).type());
  }

  @Test
  void acceptedOpportunityInterestPointsToContract() {
    assertEquals(RelatedEntityType.CARE_CONTRACT, NotificationCatalog.get(NotificationType.SERVICE_OPPORTUNITY_APPLICATION_ACCEPTED).relatedEntityType());
  }
}
