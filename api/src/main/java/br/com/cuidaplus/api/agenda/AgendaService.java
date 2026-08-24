package br.com.cuidaplus.api.agenda;

import br.com.cuidaplus.api.agenda.dto.*;
import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.contract_history.ResponsibleContractHistoryService;
import br.com.cuidaplus.api.contract_termination.ContractStatusProcessorService;
import br.com.cuidaplus.api.profile.*;
import br.com.cuidaplus.api.service_request.*;
import br.com.cuidaplus.api.service_attendance.ServiceAttendanceService;
import br.com.cuidaplus.api.user.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendaService {
  private static final long MAX_INTERVAL_DAYS = 90;
  private static final Set<CareContractStatus> VISIBLE_STATUSES = EnumSet.of(
    CareContractStatus.AGENDADA,
    CareContractStatus.ATIVA,
    CareContractStatus.ENCERRAMENTO_AGENDADO
  );

  private final CareContractRepository contracts;
  private final UserService users;
  private final ContractStatusProcessorService statusProcessor;
  private final ResponsibleContractHistoryService contractHistory;
  private final ServiceAttendanceService attendance;

  public AgendaService(
    CareContractRepository contracts,
    UserService users,
    ContractStatusProcessorService statusProcessor,
    ResponsibleContractHistoryService contractHistory,
    ServiceAttendanceService attendance
  ) {
    this.contracts = contracts;
    this.users = users;
    this.statusProcessor = statusProcessor;
    this.contractHistory = contractHistory;
    this.attendance = attendance;
  }

  @Transactional
  public AgendaEventsResponse events(UUID userId, LocalDate startDate, LocalDate endDate, AgendaViewMode viewMode) {
    validateInterval(startDate, endDate);
    Objects.requireNonNull(viewMode, "O modo de visualização é obrigatório.");

    User viewer = users.findById(userId);
    boolean responsibleView = isResponsible(viewer.getUserType());
    if (!responsibleView && !isCaregiver(viewer.getUserType())) {
      throw new BusinessException("A Agenda está disponível apenas para responsáveis e cuidadores.", HttpStatus.FORBIDDEN);
    }

    List<CareContract> participantContracts = responsibleView
      ? contracts.findByResponsibleUserOrderByUpdatedAtDesc(viewer)
      : contracts.findByCaregiverUserOrderByUpdatedAtDesc(viewer);

    Map<String, AgendaEventResponse> uniqueEvents = participantContracts.stream()
      .map(statusProcessor::processContractIfDue)
      .filter(contract -> VISIBLE_STATUSES.contains(contract.getStatus()))
      .flatMap(contract -> eventsForContract(contract, responsibleView, startDate, endDate).stream())
      .collect(Collectors.toMap(AgendaEventResponse::id, Function.identity(), (first, ignored) -> first, LinkedHashMap::new));

    List<AgendaEventResponse> sorted = uniqueEvents.values().stream()
      .sorted(Comparator.comparing(AgendaEventResponse::startDateTime).thenComparing(AgendaEventResponse::id))
      .toList();
    return new AgendaEventsResponse(sorted);
  }

  @Transactional
  public AgendaEventDetailsResponse details(UUID userId, UUID contractId, LocalDate eventDate) {
    AgendaEventResponse event = events(userId, eventDate, eventDate, AgendaViewMode.DAY).content().stream()
      .filter(candidate -> candidate.contractId().equals(contractId))
      .findFirst()
      .orElseThrow(() -> new BusinessException("Este contrato não possui um serviço agendado nessa data.", HttpStatus.NOT_FOUND));
    return new AgendaEventDetailsResponse(event, contractHistory.contractDetails(userId, contractId), attendance.details(userId, contractId, eventDate));
  }

  private List<AgendaEventResponse> eventsForContract(
    CareContract contract,
    boolean responsibleView,
    LocalDate requestedStart,
    LocalDate requestedEnd
  ) {
    LocalDate lowerBound = laterOf(requestedStart, contract.getStartDate());
    LocalDate upperBound = earlierOf(requestedEnd, contract.getEndDate());
    if (contract.getStatus() == CareContractStatus.ENCERRAMENTO_AGENDADO) {
      upperBound = earlierOf(upperBound, contract.getEffectiveEndDate());
    }
    if (lowerBound == null || upperBound == null || upperBound.isBefore(lowerBound)) return List.of();
    final LocalDate eventUpperBound = upperBound;

    ServiceRequest request = contract.getServiceRequest();
    List<ServiceRequestScheduleDay> schedules = request.getScheduleDays().stream()
      .filter(schedule -> schedule.getWeekday() != null && schedule.getStartTime() != null && schedule.getEndTime() != null)
      .sorted(Comparator.comparing((ServiceRequestScheduleDay schedule) -> schedule.getWeekday().ordinal())
        .thenComparing(ServiceRequestScheduleDay::getStartTime))
      .toList();
    if (schedules.isEmpty()) return List.of();

    List<AgendaEventResponse> result = new ArrayList<>();
    if (request.getHiringType() == HiringType.PONTUAL) {
      request.getSpecificDates().stream()
        .filter(date -> !date.isBefore(lowerBound) && !date.isAfter(eventUpperBound))
        .sorted()
        .forEach(date -> schedulesForDate(schedules, date, true)
          .forEach(schedule -> result.add(toEvent(contract, request, responsibleView, date, schedule))));
      return result;
    }

    for (LocalDate date = lowerBound; !date.isAfter(eventUpperBound); date = date.plusDays(1)) {
      LocalDate eventDate = date;
      schedulesForDate(schedules, eventDate, false)
        .forEach(schedule -> result.add(toEvent(contract, request, responsibleView, eventDate, schedule)));
    }
    return result;
  }

  private List<ServiceRequestScheduleDay> schedulesForDate(
    List<ServiceRequestScheduleDay> schedules,
    LocalDate date,
    boolean allowGeneralSchedule
  ) {
    List<ServiceRequestScheduleDay> matches = schedules.stream()
      .filter(schedule -> toDayOfWeek(schedule.getWeekday()) == date.getDayOfWeek())
      .toList();
    if (!matches.isEmpty() || !allowGeneralSchedule) return matches;
    return List.of(schedules.get(0));
  }

  private AgendaEventResponse toEvent(
    CareContract contract,
    ServiceRequest request,
    boolean responsibleView,
    LocalDate date,
    ServiceRequestScheduleDay schedule
  ) {
    User participant = responsibleView ? contract.getCaregiverUser() : contract.getResponsibleUser();
    String assistedName = contract.getAssistedPerson().getNome();
    String startTime = schedule.getStartTime().toString();
    String endTime = schedule.getEndTime().toString();
    String id = contract.getId() + ":" + date + ":" + startTime + ":" + endTime;
    UUID viewerId = responsibleView ? contract.getResponsibleUser().getId() : contract.getCaregiverUser().getId();
    var attendanceSummary = attendance.details(viewerId, contract.getId(), date);
    return new AgendaEventResponse(
      id,
      contract.getId(),
      "Cuidado de " + assistedName,
      participant.getFullName() + " · " + startTime + " às " + endTime,
      LocalDateTime.of(date, schedule.getStartTime()),
      LocalDateTime.of(date, schedule.getEndTime()),
      date,
      contract.getStatus(),
      request.getHiringType(),
      participant.getFullName(),
      participant.getProfilePhotoUrl(),
      assistedName,
      addressSummary(contract.getAssistedPerson().getEnderecoCuidado()),
      AgendaSourceType.CARE_CONTRACT,
      contract.getStatus() == CareContractStatus.ENCERRAMENTO_AGENDADO,
      contract.getEffectiveEndDate(),
      attendanceSummary.status(),
      attendanceSummary.statusLabel()
    );
  }

  private void validateInterval(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null) throw new BusinessException("Informe as datas inicial e final da Agenda.");
    if (endDate.isBefore(startDate)) throw new BusinessException("A data final não pode ser anterior à data inicial.");
    if (ChronoUnit.DAYS.between(startDate, endDate) + 1 > MAX_INTERVAL_DAYS) {
      throw new BusinessException("O período máximo de consulta da Agenda é de 90 dias.");
    }
  }

  private boolean isResponsible(UserType type) {
    return type == UserType.RESPONSAVEL || type == UserType.FAMILY;
  }

  private boolean isCaregiver(UserType type) {
    return type == UserType.CUIDADOR || type == UserType.CAREGIVER;
  }

  private LocalDate laterOf(LocalDate first, LocalDate second) {
    if (first == null) return second;
    if (second == null) return first;
    return first.isAfter(second) ? first : second;
  }

  private LocalDate earlierOf(LocalDate first, LocalDate second) {
    if (first == null) return second;
    if (second == null) return first;
    return first.isBefore(second) ? first : second;
  }

  private DayOfWeek toDayOfWeek(DiaSemana weekday) {
    return switch (weekday) {
      case SEGUNDA -> DayOfWeek.MONDAY;
      case TERCA -> DayOfWeek.TUESDAY;
      case QUARTA -> DayOfWeek.WEDNESDAY;
      case QUINTA -> DayOfWeek.THURSDAY;
      case SEXTA -> DayOfWeek.FRIDAY;
      case SABADO -> DayOfWeek.SATURDAY;
      case DOMINGO -> DayOfWeek.SUNDAY;
    };
  }

  private String addressSummary(AddressFields address) {
    if (address == null) return null;
    String cityState = joinNonBlank(" - ", address.getCidade(), address.getEstado());
    return joinNonBlank(", ", address.getBairro(), cityState);
  }

  private String joinNonBlank(String separator, String... values) {
    return Arrays.stream(values)
      .filter(value -> value != null && !value.isBlank())
      .collect(Collectors.joining(separator));
  }
}
