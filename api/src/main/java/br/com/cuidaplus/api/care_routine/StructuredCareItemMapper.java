package br.com.cuidaplus.api.care_routine;

import br.com.cuidaplus.api.care_routine.dto.StructuredCareItemResponse;
import br.com.cuidaplus.api.care_task.*;
import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.service_request.ServiceRequestCareItemSnapshot;
import java.time.LocalTime;
import java.util.*;

public final class StructuredCareItemMapper {
  private StructuredCareItemMapper() {}
  public static StructuredCareItemResponse response(CareRoutineItem item) {
    return response(item.getId(), item.getTitle(), item.getDescription(), item.getSortOrder(), item.getCategory(), item.getCustomCategory(), item.getPriority(), item.getRecurrenceType(), item.getScheduledTime(), item.getIntervalDays(), item.getWeekdays(), item.getReminderEnabled(), item.getReminderMinutesBefore(), item.isReminderAtScheduledTime(), item.isOverdueReminderEnabled(), item.getOverdueAfterMinutes(), item.isRepeatWhilePending(), item.getRepeatIntervalMinutes(), item.isImportant(), item.isNotifyResponsibleIfImportant(), item.isRequiresCompletionPhoto(), item.getNotes(), item.getMedication());
  }
  public static StructuredCareItemResponse response(ServiceRequestCareItemSnapshot item) {
    return response(item.getId(), item.getTitle(), item.getDescription(), item.getSortOrder(), item.getCategory(), item.getCustomCategory(), item.getPriority(), item.getRecurrenceType(), item.getScheduledTime(), item.getIntervalDays(), item.getWeekdays(), item.getReminderEnabled(), item.getReminderMinutesBefore(), item.isReminderAtScheduledTime(), item.isOverdueReminderEnabled(), item.getOverdueAfterMinutes(), item.isRepeatWhilePending(), item.getRepeatIntervalMinutes(), item.isImportant(), item.isNotifyResponsibleIfImportant(), item.isRequiresCompletionPhoto(), item.getNotes(), item.getMedication());
  }
  public static MedicationDetails copyMedication(MedicationDetails source) {
    if (source == null || source.getName() == null) return null;
    MedicationDetails target = new MedicationDetails(); target.setName(source.getName()); target.setDosage(source.getDosage()); target.setUnit(source.getUnit()); target.setCustomUnit(source.getCustomUnit()); target.setAdministrationRoute(source.getAdministrationRoute()); target.setCustomAdministrationRoute(source.getCustomAdministrationRoute()); target.setAdditionalInstructions(source.getAdditionalInstructions()); return target;
  }
  private static StructuredCareItemResponse response(UUID id, String title, String description, int sortOrder, TaskCategory category, String customCategory, TaskPriority priority, TaskRecurrenceType recurrenceType, LocalTime scheduledTime, Integer intervalDays, Set<DiaSemana> weekdays, Boolean reminderEnabled, Integer reminderMinutesBefore, boolean reminderAtScheduledTime, boolean overdueReminderEnabled, Integer overdueAfterMinutes, boolean repeatWhilePending, Integer repeatIntervalMinutes, boolean important, boolean notifyResponsibleIfImportant, boolean requiresCompletionPhoto, String notes, MedicationDetails medication) {
    return new StructuredCareItemResponse(id, title, description, sortOrder, category, categoryLabel(category, customCategory), customCategory, priority, recurrenceType, scheduledTime, intervalDays, weekdays == null ? Set.of() : new LinkedHashSet<>(weekdays), reminderEnabled, reminderMinutesBefore, reminderAtScheduledTime, overdueReminderEnabled, overdueAfterMinutes, repeatWhilePending, repeatIntervalMinutes, important, notifyResponsibleIfImportant, requiresCompletionPhoto, notes, medication == null || medication.getName() == null ? null : new StructuredCareItemResponse.Medication(medication.getName(), medication.getDosage(), medication.getUnit(), medication.getCustomUnit(), medication.getAdministrationRoute(), medication.getCustomAdministrationRoute(), medication.getAdditionalInstructions()));
  }
  private static String categoryLabel(TaskCategory category, String customCategory) { if (category == null) return "Cuidado"; if (category == TaskCategory.PERSONALIZADA && customCategory != null && !customCategory.isBlank()) return customCategory; return switch (category) { case MEDICACAO -> "Medicação"; case ALIMENTACAO -> "Alimentação"; case HIDRATACAO -> "Hidratação"; case HIGIENE_BANHO -> "Higiene e banho"; case MOBILIDADE -> "Mobilidade"; case EXERCICIO -> "Exercício"; case CURATIVO -> "Curativo"; case SINAIS_VITAIS -> "Sinais vitais"; case CONSULTA_COMPROMISSO -> "Consulta ou compromisso"; case PERSONALIZADA -> "Personalizada"; }; }
}
