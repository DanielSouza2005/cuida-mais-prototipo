package br.com.cuidaplus.api.care_routine;

import br.com.cuidaplus.api.care_routine.dto.*;
import br.com.cuidaplus.api.care_task.*;
import br.com.cuidaplus.api.care_task.dto.MedicationRequest;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.profile.*;
import br.com.cuidaplus.api.user.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CareRoutineService {
  private final CareRoutineRepository routines;
  private final AssistedPersonRepository assistedPeople;
  private final UserService users;

  public CareRoutineService(CareRoutineRepository routines, AssistedPersonRepository assistedPeople, UserService users) {
    this.routines = routines; this.assistedPeople = assistedPeople; this.users = users;
  }

  @Transactional(readOnly = true)
  public List<CareRoutineResponse> list(UUID userId, UUID assistedPersonId, Boolean active, String search) {
    User owner = requireResponsible(userId);
    String term = search == null ? "" : search.trim().toLowerCase(Locale.forLanguageTag("pt-BR"));
    return routines.findByResponsibleUserOrderByUpdatedAtDesc(owner).stream()
      .filter(r -> active == null || r.isActive() == active)
      .filter(r -> assistedPersonId == null || r.getAssistedPerson() == null || r.getAssistedPerson().getId().equals(assistedPersonId))
      .filter(r -> term.isEmpty() || r.getName().toLowerCase(Locale.forLanguageTag("pt-BR")).contains(term))
      .map(this::response).toList();
  }

  @Transactional(readOnly = true)
  public CareRoutineResponse details(UUID userId, UUID id) { return response(owned(userId, id)); }

  @Transactional(readOnly = true)
  public CareRoutineFormDataResponse formData(UUID userId) {
    User owner = requireResponsible(userId);
    return new CareRoutineFormDataResponse(assistedPeople.findByResponsibleUser(owner).stream()
      .map(p -> new CareRoutineFormDataResponse.AssistedPerson(p.getId(), p.getNome())).toList());
  }

  @Transactional
  public CareRoutineResponse create(UUID userId, CareRoutineRequest request) {
    User owner = requireResponsible(userId);
    CareRoutine routine = new CareRoutine(); routine.setResponsibleUser(owner); routine.setActive(true);
    apply(routine, owner, request);
    return response(routines.save(routine));
  }

  @Transactional
  public CareRoutineResponse update(UUID userId, UUID id, CareRoutineRequest request) {
    CareRoutine routine = owned(userId, id);
    apply(routine, routine.getResponsibleUser(), request);
    return response(routine);
  }

  @Transactional
  public CareRoutineResponse setActive(UUID userId, UUID id, boolean active) {
    CareRoutine routine = owned(userId, id); routine.setActive(active); return response(routine);
  }

  @Transactional(readOnly = true)
  public CareRoutine requireSelectable(UUID userId, UUID id, UUID assistedPersonId) {
    CareRoutine routine = owned(userId, id);
    if (!routine.isActive()) throw new BusinessException("Esta rotina de cuidados está inativa.");
    if (routine.getAssistedPerson() != null && !routine.getAssistedPerson().getId().equals(assistedPersonId)) {
      throw new BusinessException("Esta rotina não pertence à pessoa assistida selecionada.");
    }
    return routine;
  }

  private void apply(CareRoutine routine, User owner, CareRoutineRequest request) {
    String name = clean(request.name());
    if (name == null) throw new BusinessException("Informe o nome da rotina.");
    if (request.items() == null || request.items().isEmpty()) throw new BusinessException("Adicione pelo menos um cuidado.");
    AssistedPerson assisted = request.assistedPersonId() == null ? null : assistedPeople.findByIdAndResponsibleUser(request.assistedPersonId(), owner)
      .orElseThrow(() -> new BusinessException("Pessoa assistida não pertence ao responsável autenticado.", HttpStatus.FORBIDDEN));
    List<CareRoutineItem> items = new ArrayList<>();
    for (int index = 0; index < request.items().size(); index++) {
      var source = request.items().get(index); String title = clean(source.title());
      if (title == null) throw new BusinessException("Informe o título do cuidado.");
      CareRoutineItem item = new CareRoutineItem(); item.setTitle(title); item.setDescription(clean(source.description()));
      validateStructuredItem(source);
      item.setSortOrder(source.sortOrder() == null ? index + 1 : source.sortOrder()); item.setActive(true); item.setCategory(source.category()); item.setCustomCategory(source.category() == TaskCategory.PERSONALIZADA ? clean(source.customCategory()) : null); item.setPriority(source.priority()); item.setRecurrenceType(source.recurrenceType()); item.setScheduledTime(source.scheduledTime()); item.setIntervalDays(source.recurrenceType() == TaskRecurrenceType.INTERVALO ? source.intervalDays() : null); item.setWeekdays(source.recurrenceType() == TaskRecurrenceType.DIAS_ESPECIFICOS && source.weekdays() != null ? new LinkedHashSet<>(source.weekdays()) : new LinkedHashSet<>()); item.setReminderEnabled(source.reminderEnabled()); item.setReminderMinutesBefore(source.reminderEnabled() ? source.reminderMinutesBefore() : null); item.setReminderAtScheduledTime(source.reminderEnabled() && source.reminderAtScheduledTime()); item.setOverdueReminderEnabled(source.reminderEnabled() && source.overdueReminderEnabled()); item.setOverdueAfterMinutes(source.overdueReminderEnabled() ? source.overdueAfterMinutes() : null); item.setRepeatWhilePending(source.reminderEnabled() && source.repeatWhilePending()); item.setRepeatIntervalMinutes(source.repeatWhilePending() ? source.repeatIntervalMinutes() : null); item.setImportant(source.important()); item.setNotifyResponsibleIfImportant(source.important() && source.notifyResponsibleIfImportant()); item.setRequiresCompletionPhoto(source.requiresCompletionPhoto()); item.setNotes(clean(source.notes())); item.setMedication(source.category() == TaskCategory.MEDICACAO ? medication(source.medication()) : null); items.add(item);
    }
    routine.setName(name); routine.setDescription(clean(request.description())); routine.setAssistedPerson(assisted); routine.replaceItems(items);
  }

  private CareRoutine owned(UUID userId, UUID id) {
    User owner = requireResponsible(userId);
    return routines.findByIdAndResponsibleUser(id, owner).orElseThrow(() -> new BusinessException("Rotina de cuidados não encontrada.", HttpStatus.NOT_FOUND));
  }
  private User requireResponsible(UUID id) {
    User user = users.findById(id);
    if (user.getUserType() != UserType.RESPONSAVEL && user.getUserType() != UserType.FAMILY) throw new BusinessException("Acesso permitido apenas para responsáveis.", HttpStatus.FORBIDDEN);
    return user;
  }
  private CareRoutineResponse response(CareRoutine routine) {
    var assisted = routine.getAssistedPerson() == null ? null : new CareRoutineResponse.AssistedPerson(routine.getAssistedPerson().getId(), routine.getAssistedPerson().getNome());
    return new CareRoutineResponse(routine.getId(), routine.getName(), routine.getDescription(), routine.isActive(), assisted,
      routine.getItems().stream().filter(CareRoutineItem::isActive).map(StructuredCareItemMapper::response).toList(), routine.getCreatedAt(), routine.getUpdatedAt());
  }
  private void validateStructuredItem(CareRoutineRequest.Item item) {
    if (item.category() == TaskCategory.PERSONALIZADA && clean(item.customCategory()) == null) throw new BusinessException("Informe o nome do tipo de cuidado personalizado.");
    if (item.recurrenceType() == TaskRecurrenceType.DIAS_ESPECIFICOS && (item.weekdays() == null || item.weekdays().isEmpty())) throw new BusinessException("Selecione ao menos um dia da semana para o cuidado.");
    if (item.recurrenceType() == TaskRecurrenceType.INTERVALO && (item.intervalDays() == null || item.intervalDays() <= 0)) throw new BusinessException("O intervalo do cuidado deve ser maior que zero.");
    if (item.reminderEnabled() && item.reminderMinutesBefore() == null) throw new BusinessException("Informe a antecedência do lembrete.");
    if (item.overdueReminderEnabled() && item.overdueAfterMinutes() == null) throw new BusinessException("Informe após quantos minutos o cuidado será considerado atrasado.");
    if (item.repeatWhilePending() && item.repeatIntervalMinutes() == null) throw new BusinessException("Informe o intervalo de repetição do lembrete.");
    if (item.notifyResponsibleIfImportant() && !item.important()) throw new BusinessException("Marque o cuidado como importante para notificar o responsável.");
    if (item.category() == TaskCategory.MEDICACAO) {
      MedicationRequest medication = item.medication();
      if (medication == null || clean(medication.name()) == null || clean(medication.dosage()) == null || medication.unit() == null || medication.administrationRoute() == null) throw new BusinessException("Preencha nome, dosagem, unidade e forma de administração do medicamento.");
      if (medication.unit() == MedicationUnit.PERSONALIZADA && clean(medication.customUnit()) == null) throw new BusinessException("Informe a unidade personalizada.");
      if (medication.administrationRoute() == MedicationAdministrationRoute.OUTRA && clean(medication.customAdministrationRoute()) == null) throw new BusinessException("Informe a forma de administração personalizada.");
    } else if (item.medication() != null) throw new BusinessException("Dados de medicamento só podem ser informados no tipo Medicação.");
  }
  private MedicationDetails medication(MedicationRequest source) { MedicationDetails value = new MedicationDetails(); value.setName(clean(source.name())); value.setDosage(clean(source.dosage())); value.setUnit(source.unit()); value.setCustomUnit(clean(source.customUnit())); value.setAdministrationRoute(source.administrationRoute()); value.setCustomAdministrationRoute(clean(source.customAdministrationRoute())); value.setAdditionalInstructions(clean(source.additionalInstructions())); return value; }
  private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
