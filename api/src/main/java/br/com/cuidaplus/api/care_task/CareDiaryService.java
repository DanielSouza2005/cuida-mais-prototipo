package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_contract.CareContract;
import br.com.cuidaplus.api.care_contract.CareContractRepository;
import br.com.cuidaplus.api.care_contract.CareContractStatus;
import br.com.cuidaplus.api.care_task.dto.CareDiaryContractOptionResponse;
import br.com.cuidaplus.api.care_task.dto.CareDiaryItemResponse;
import br.com.cuidaplus.api.care_task.dto.CareDiaryResponse;
import br.com.cuidaplus.api.care_task.dto.CareOccurrencePhotoResponse;
import br.com.cuidaplus.api.care_task.dto.CreateManualCareRequest;
import br.com.cuidaplus.api.care_task.dto.ManualCareFormDataResponse;
import br.com.cuidaplus.api.care_task.dto.TaskOccurrenceResponse;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.contract_termination.ContractStatusProcessorService;
import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.service_request.HiringType;
import br.com.cuidaplus.api.service_attendance.ServiceAttendanceService;
import br.com.cuidaplus.api.user.User;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CareDiaryService {
  private static final Set<CareContractStatus> ALLOWED_STATUSES = EnumSet.of(
    CareContractStatus.AGENDADA, CareContractStatus.ATIVA, CareContractStatus.ENCERRAMENTO_AGENDADO
  );

  private final CareActivityRecordRepository records;
  private final CareContractRepository contracts;
  private final CareOccurrencePhotoRepository photoRepository;
  private final CareOccurrencePhotoService photoService;
  private final TaskAuthorizationService authorization;
  private final TaskOccurrenceService occurrences;
  private final TaskDateTimeService dateTimes;
  private final ContractStatusProcessorService statusProcessor;
  private final ServiceAttendanceService attendance;

  public CareDiaryService(CareActivityRecordRepository records, CareContractRepository contracts,
    CareOccurrencePhotoRepository photoRepository, CareOccurrencePhotoService photoService,
    TaskAuthorizationService authorization, TaskOccurrenceService occurrences, TaskDateTimeService dateTimes,
    ContractStatusProcessorService statusProcessor, ServiceAttendanceService attendance) {
    this.records = records;
    this.contracts = contracts;
    this.photoRepository = photoRepository;
    this.photoService = photoService;
    this.authorization = authorization;
    this.occurrences = occurrences;
    this.dateTimes = dateTimes;
    this.statusProcessor = statusProcessor;
    this.attendance = attendance;
  }

  @Transactional
  public ManualCareFormDataResponse formData(UUID userId, LocalDate date) {
    User caregiver = authorization.requireCaregiver(userId);
    List<CareDiaryContractOptionResponse> options = contracts.findByCaregiverUserOrderByUpdatedAtDesc(caregiver).stream()
      .map(statusProcessor::processContractIfDue)
      .filter(contract -> isEligibleForDate(contract, date))
      .map(contract -> new CareDiaryContractOptionResponse(
        contract.getId(), "Cuidado de " + contract.getAssistedPerson().getNome(),
        contract.getAssistedPerson().getId(), contract.getAssistedPerson().getNome()
      ))
      .toList();
    return new ManualCareFormDataResponse(date, options);
  }

  @Transactional
  public CareDiaryItemResponse createManual(UUID userId, CreateManualCareRequest request, List<MultipartFile> photos) {
    User caregiver = authorization.requireCaregiver(userId);
    CareContract contract = contracts.findById(request.getContractId())
      .map(statusProcessor::processContractIfDue)
      .orElseThrow(() -> new BusinessException("Contratação não encontrada.", HttpStatus.NOT_FOUND));
    if (!contract.getCaregiverUser().getId().equals(caregiver.getId())) {
      throw new BusinessException("Você não pode registrar cuidados nesta contratação.", HttpStatus.FORBIDDEN);
    }
    if (!contract.getAssistedPerson().getId().equals(request.getAssistedPersonId())) {
      throw new BusinessException("A pessoa assistida não corresponde à contratação.", HttpStatus.FORBIDDEN);
    }
    if (!isEligibleForDate(contract, request.getEntryDate())) {
      throw new BusinessException("A contratação não possui atendimento válido nessa data.", HttpStatus.CONFLICT);
    }
    attendance.requireActiveAttendance(contract, request.getEntryDate());
    dateTimes.requireZone(request.getTimezone());

    CareActivityRecord record = new CareActivityRecord();
    record.setContract(contract);
    record.setAssistedPerson(contract.getAssistedPerson());
    record.setResponsible(contract.getResponsibleUser());
    record.setCaregiver(caregiver);
    record.setCreatedBy(caregiver);
    record.setActivityType("CUIDADO_AVULSO");
    record.setSourceType(CareRecordSourceType.MANUAL);
    record.setEntryDate(request.getEntryDate());
    record.setTimezone(request.getTimezone());
    record.setCareType(request.getCareType().name());
    record.setTitle(request.getTitle().trim());
    record.setDescription(request.getDescription().trim());
    record.setNotes(trimToNull(request.getNotes()));
    record.setImportant(request.isImportant());
    record.setOccurredAt(dateTimes.toInstant(request.getEntryDate(), request.getOccurredTime(), request.getTimezone()));
    records.saveAndFlush(record);
    photoService.attach(record, caregiver, photos);
    return manualItem(record);
  }

  @Transactional
  public CareDiaryResponse caregiverDiary(UUID userId, LocalDate date, String timezone, UUID assistedPersonId, UUID contractId) {
    User caregiver = authorization.requireCaregiver(userId);
    List<CareDiaryItemResponse> items = new ArrayList<>(occurrences.day(userId, date, timezone, null, null, assistedPersonId, contractId, 0, 50).content().stream()
      .map(this::plannedItem).toList());
    records.findByCaregiverAndEntryDateAndSourceTypeOrderByOccurredAtAsc(caregiver, date, CareRecordSourceType.MANUAL).stream()
      .filter(item -> assistedPersonId == null || item.getAssistedPerson().getId().equals(assistedPersonId))
      .filter(item -> contractId == null || item.getContract().getId().equals(contractId))
      .map(this::manualItem).forEach(items::add);
    return new CareDiaryResponse(sort(items));
  }

  @Transactional
  public CareDiaryResponse responsibleDiary(UUID userId, LocalDate date, String timezone, UUID assistedPersonId, UUID contractId) {
    User responsible = authorization.requireResponsible(userId);
    List<CareDiaryItemResponse> items = new ArrayList<>(occurrences.listForResponsible(userId, date, timezone, null, contractId, 0, 50).content().stream()
      .filter(item -> assistedPersonId == null || item.assistedPersonId().equals(assistedPersonId))
      .map(this::plannedItem).toList());
    records.findByResponsibleAndEntryDateAndSourceTypeOrderByOccurredAtAsc(responsible, date, CareRecordSourceType.MANUAL).stream()
      .filter(item -> assistedPersonId == null || item.getAssistedPerson().getId().equals(assistedPersonId))
      .filter(item -> contractId == null || item.getContract().getId().equals(contractId))
      .map(this::manualItem).forEach(items::add);
    return new CareDiaryResponse(sort(items));
  }

  @Transactional(readOnly = true)
  public CareDiaryItemResponse caregiverDetails(UUID userId, UUID id) {
    User caregiver = authorization.requireCaregiver(userId);
    return manualItem(records.findByIdAndCaregiver(id, caregiver)
      .filter(item -> item.getSourceType() == CareRecordSourceType.MANUAL)
      .orElseThrow(() -> new BusinessException("Cuidado avulso não encontrado.", HttpStatus.NOT_FOUND)));
  }

  @Transactional(readOnly = true)
  public CareDiaryItemResponse responsibleDetails(UUID userId, UUID id) {
    User responsible = authorization.requireResponsible(userId);
    return manualItem(records.findByIdAndResponsible(id, responsible)
      .filter(item -> item.getSourceType() == CareRecordSourceType.MANUAL)
      .orElseThrow(() -> new BusinessException("Cuidado avulso não encontrado.", HttpStatus.NOT_FOUND)));
  }

  private CareDiaryItemResponse plannedItem(TaskOccurrenceResponse item) {
    return new CareDiaryItemResponse(
      item.id(), CareRecordSourceType.PLANNED, "Planejado", item.id(), null, item.contractId(), item.scheduledDate(),
      item.scheduledTime(), item.scheduledInstantUtc(), item.completedAt(), item.category().name(), careTypeLabel(item.category().name()),
      item.title(), item.executionNote() == null ? item.description() : item.executionNote(), item.taskNotes(), item.status().name(),
      statusLabel(item.status()), item.assistedPersonId(), item.assistedPersonName(), item.caregiverName(), item.important(), item.photos()
    );
  }

  private CareDiaryItemResponse manualItem(CareActivityRecord item) {
    List<CareOccurrencePhotoResponse> photos = photoRepository.findByActivityRecordOrderByCreatedAtAsc(item).stream()
      .map(photo -> new CareOccurrencePhotoResponse(photo.getId(), "/api/care-diary/" + item.getId() + "/photos/" + photo.getId(), photo.getContentType(), photo.getFileSize(), photo.getCreatedAt()))
      .toList();
    return new CareDiaryItemResponse(
      item.getId(), CareRecordSourceType.MANUAL, "Avulso", null, item.getId(), item.getContract().getId(), item.getEntryDate(),
      item.getOccurredAt().atZone(dateTimes.requireZone(item.getTimezone())).toLocalTime(), item.getOccurredAt(), item.getCreatedAt(),
      item.getCareType(), careTypeLabel(item.getCareType()), item.getTitle(), item.getDescription(), item.getNotes(), "REALIZADO", "Realizado",
      item.getAssistedPerson().getId(), item.getAssistedPerson().getNome(), item.getCaregiver().getFullName(), item.isImportant(), photos
    );
  }

  private List<CareDiaryItemResponse> sort(List<CareDiaryItemResponse> items) {
    return items.stream().sorted(Comparator.comparing(CareDiaryItemResponse::time).thenComparing(CareDiaryItemResponse::id)).toList();
  }

  private boolean isEligibleForDate(CareContract contract, LocalDate date) {
    if (date == null || !ALLOWED_STATUSES.contains(contract.getStatus()) || date.isBefore(contract.getStartDate())) return false;
    LocalDate upperBound = contract.getStatus() == CareContractStatus.ENCERRAMENTO_AGENDADO && contract.getEffectiveEndDate() != null
      ? contract.getEffectiveEndDate() : contract.getEndDate();
    if (upperBound != null && date.isAfter(upperBound)) return false;
    var request = contract.getServiceRequest();
    if (request.getHiringType() == HiringType.PONTUAL) return request.getSpecificDates().contains(date);
    return request.getScheduleDays().stream().anyMatch(schedule -> schedule.getWeekday() != null && toDayOfWeek(schedule.getWeekday()) == date.getDayOfWeek());
  }

  private DayOfWeek toDayOfWeek(DiaSemana value) {
    return switch (value) {
      case SEGUNDA -> DayOfWeek.MONDAY; case TERCA -> DayOfWeek.TUESDAY; case QUARTA -> DayOfWeek.WEDNESDAY;
      case QUINTA -> DayOfWeek.THURSDAY; case SEXTA -> DayOfWeek.FRIDAY; case SABADO -> DayOfWeek.SATURDAY; case DOMINGO -> DayOfWeek.SUNDAY;
    };
  }

  private String statusLabel(TaskOccurrenceStatus value) {
    return switch (value) {
      case PENDENTE -> "Pendente"; case CONCLUIDA -> "Concluído"; case ATRASADA -> "Atrasado";
      case NAO_REALIZADA -> "Não realizado"; case CANCELADA -> "Cancelado";
    };
  }

  private String careTypeLabel(String value) {
    return switch (value) {
      case "MEDICACAO" -> "Medicação"; case "ALIMENTACAO" -> "Alimentação"; case "HIDRATACAO" -> "Hidratação";
      case "HIGIENE", "HIGIENE_BANHO" -> "Higiene"; case "MOBILIDADE" -> "Mobilidade"; case "EXERCICIO" -> "Exercício";
      case "CURATIVO" -> "Curativo"; case "SINAIS_VITAIS" -> "Sinais vitais"; case "CONSULTA_COMPROMISSO" -> "Consulta ou compromisso";
      case "COMPANHIA" -> "Companhia"; case "OBSERVACAO" -> "Observação"; case "OCORRENCIA" -> "Ocorrência";
      default -> "Outro";
    };
  }

  private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
