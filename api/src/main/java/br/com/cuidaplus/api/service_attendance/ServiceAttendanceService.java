package br.com.cuidaplus.api.service_attendance;

import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.contract_termination.ContractStatusProcessorService;
import br.com.cuidaplus.api.notification.*;
import br.com.cuidaplus.api.service_attendance.dto.*;
import br.com.cuidaplus.api.user.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceAttendanceService {
    private static final Duration MAX_LOCATION_AGE = Duration.ofMinutes(5);
    private static final Duration MAX_LOCATION_FUTURE = Duration.ofSeconds(60);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final CareContractRepository contracts;
    private final ServiceAttendanceRepository records;
    private final UserService users;
    private final ContractStatusProcessorService statusProcessor;
    private final AttendanceScheduleService schedules;
    private final NotificationService notifications;
    private final Clock clock;

    public ServiceAttendanceService(CareContractRepository contracts, ServiceAttendanceRepository records, UserService users,
                                    ContractStatusProcessorService statusProcessor, AttendanceScheduleService schedules,
                                    NotificationService notifications, Clock attendanceClock) {
        this.contracts = contracts;
        this.records = records;
        this.users = users;
        this.statusProcessor = statusProcessor;
        this.schedules = schedules;
        this.notifications = notifications;
        this.clock = attendanceClock;
    }

    @Transactional
    public AttendanceSummaryResponse start(UUID userId, UUID contractId, AttendanceActionRequest request) {
        User caregiver = requireCaregiver(userId);
        CareContract contract = lockedContract(contractId);
        requireCaregiverContract(caregiver, contract);
        requireActionableContract(contract);
        AttendanceSchedule schedule = schedules.requireSchedule(contract, request.attendanceDate());
        Instant now = clock.instant();
        validateLocation(request, now);
        if (records.existsByContractAndAttendanceDateAndRecordType(contract, request.attendanceDate(), AttendanceRecordType.START)) {
            throw new BusinessException("Este atendimento já foi iniciado.", HttpStatus.CONFLICT);
        }
        requireWindow(now, schedule.startWindowStart(), schedule.startWindowEnd(), true);
        records.save(record(contract, request, schedule, AttendanceRecordType.START, now, schedule.startWindowStart(), schedule.startWindowEnd()));
        notifications.create(contract.getResponsibleUser(), NotificationType.SERVICE_ATTENDANCE_STARTED,
                "Atendimento iniciado", "O cuidador iniciou o atendimento de hoje.", RelatedEntityType.CARE_CONTRACT, contract.getId(), schedule.scheduledStart());
        return summary(contract, request.attendanceDate(), now);
    }

    @Transactional
    public AttendanceSummaryResponse end(UUID userId, UUID contractId, AttendanceActionRequest request) {
        User caregiver = requireCaregiver(userId);
        CareContract contract = lockedContract(contractId);
        requireCaregiverContract(caregiver, contract);
        requireActionableContract(contract);
        AttendanceSchedule schedule = schedules.requireSchedule(contract, request.attendanceDate());
        Instant now = clock.instant();
        validateLocation(request, now);
        if (!records.existsByContractAndAttendanceDateAndRecordType(contract, request.attendanceDate(), AttendanceRecordType.START)) {
            throw new BusinessException("Você precisa iniciar o atendimento antes de encerrá-lo.", HttpStatus.CONFLICT);
        }
        if (records.existsByContractAndAttendanceDateAndRecordType(contract, request.attendanceDate(), AttendanceRecordType.END)) {
            throw new BusinessException("Este atendimento já foi encerrado.", HttpStatus.CONFLICT);
        }
        requireWindow(now, schedule.endWindowStart(), schedule.endWindowEnd(), false);
        records.save(record(contract, request, schedule, AttendanceRecordType.END, now, schedule.endWindowStart(), schedule.endWindowEnd()));
        notifications.create(contract.getResponsibleUser(), NotificationType.SERVICE_ATTENDANCE_ENDED,
                "Atendimento encerrado", "O cuidador encerrou o atendimento de hoje.", RelatedEntityType.CARE_CONTRACT, contract.getId(), schedule.scheduledStart());
        return summary(contract, request.attendanceDate(), now);
    }

    @Transactional
    public AttendanceSummaryResponse details(UUID userId, UUID contractId, LocalDate date) {
        User viewer = users.findById(userId);
        CareContract contract = contracts.findById(contractId).map(statusProcessor::processContractIfDue)
                .orElseThrow(() -> new BusinessException("Contratação não encontrada.", HttpStatus.NOT_FOUND));
        requireParticipant(viewer, contract);
        schedules.requireSchedule(contract, date);
        return summary(contract, date, clock.instant());
    }

    @Transactional
    public TodayAttendanceResponse today(UUID userId) {
        User caregiver = requireCaregiver(userId);
        LocalDate today = LocalDate.now(clock);
        List<AttendanceSummaryResponse> content = contracts.findByCaregiverUserOrderByUpdatedAtDesc(caregiver).stream()
                .map(statusProcessor::processContractIfDue)
      .filter(this::isActionableContract)
      .filter(contract -> schedules.hasSchedule(contract, today))
      .map(contract -> summary(contract, today, clock.instant()))
      .sorted(Comparator.comparing(AttendanceSummaryResponse::scheduledStartTime))
                .toList();
        return new TodayAttendanceResponse(content);
    }

    @Transactional(readOnly = true)
    public void requireActiveAttendance(CareContract contract, LocalDate date) {
        boolean started = records.existsByContractAndAttendanceDateAndRecordType(contract, date, AttendanceRecordType.START);
        boolean ended = records.existsByContractAndAttendanceDateAndRecordType(contract, date, AttendanceRecordType.END);
        if (!started)
            throw new BusinessException("Você precisa iniciar o atendimento antes de registrar cuidados.", HttpStatus.CONFLICT);
        if (ended)
            throw new BusinessException("Este atendimento já foi encerrado. Não é possível registrar novos cuidados.", HttpStatus.CONFLICT);
    }

    private CareContract lockedContract(UUID id) {
        return contracts.findForUpdateById(id).map(statusProcessor::processContractIfDue)
                .orElseThrow(() -> new BusinessException("Contratação não encontrada.", HttpStatus.NOT_FOUND));
    }

    private User requireCaregiver(UUID id) {
        User user = users.findById(id);
        if (user.getUserType() != UserType.CUIDADOR && user.getUserType() != UserType.CAREGIVER) {
            throw new BusinessException("Apenas o cuidador pode registrar o atendimento.", HttpStatus.FORBIDDEN);
        }
        return user;
    }

    private void requireCaregiverContract(User caregiver, CareContract contract) {
        if (!contract.getCaregiverUser().getId().equals(caregiver.getId())) {
            throw new BusinessException("Você não tem permissão para realizar esta ação.", HttpStatus.FORBIDDEN);
        }
    }

    private void requireActionableContract(CareContract contract) {
        if (!isActionableContract(contract))
            throw new BusinessException("Esta contratação não permite registrar atendimento.", HttpStatus.CONFLICT);
    }

    private boolean isActionableContract(CareContract contract) {
        return contract.getStatus() == CareContractStatus.AGENDADA || contract.getStatus() == CareContractStatus.ATIVA
                || contract.getStatus() == CareContractStatus.ENCERRAMENTO_AGENDADO;
    }

    private void requireParticipant(User viewer, CareContract contract) {
        if (!contract.getCaregiverUser().getId().equals(viewer.getId()) && !contract.getResponsibleUser().getId().equals(viewer.getId())) {
            throw new BusinessException("Você não tem permissão para visualizar este atendimento.", HttpStatus.FORBIDDEN);
        }
    }

    private void validateLocation(AttendanceActionRequest request, Instant now) {
        try {
            ZoneId.of(request.deviceTimezone());
        } catch (DateTimeException exception) {
            throw new BusinessException("Informe um fuso horário válido, como America/Sao_Paulo.");
        }
        if (request.mocked())
            throw new BusinessException("Não é possível registrar o atendimento com uma localização simulada.", HttpStatus.CONFLICT);
        if (request.locationCapturedAt().isBefore(now.minus(MAX_LOCATION_AGE)) || request.locationCapturedAt().isAfter(now.plus(MAX_LOCATION_FUTURE))) {
            throw new BusinessException("A localização não é recente. Obtenha sua localização novamente.", HttpStatus.CONFLICT);
        }
    }

    private void requireWindow(Instant now, Instant start, Instant end, boolean starting) {
        if (now.isBefore(start)) {
            String verb = starting ? "iniciar" : "encerrar";
            String time = start.atZone(AttendanceTimeConfig.SERVICE_ZONE).toLocalTime().format(TIME);
            throw new BusinessException("Ainda não é possível " + verb + " este atendimento. Você poderá " + verb + " a partir de " + time + ".", HttpStatus.CONFLICT);
        }
        if (now.isAfter(end)) {
            throw new BusinessException("O prazo para " + (starting ? "iniciar" : "encerrar") + " este atendimento foi encerrado.", HttpStatus.CONFLICT);
        }
    }

    private ServiceAttendanceRecord record(CareContract contract, AttendanceActionRequest request, AttendanceSchedule schedule,
                                           AttendanceRecordType type, Instant now, Instant windowStart, Instant windowEnd) {
        ServiceAttendanceRecord value = new ServiceAttendanceRecord();
        value.setContract(contract);
        value.setCaregiver(contract.getCaregiverUser());
        value.setResponsible(contract.getResponsibleUser());
        value.setAssistedPerson(contract.getAssistedPerson());
        value.setAttendanceDate(request.attendanceDate());
        value.setRecordType(type);
        value.setRecordedAt(now);
        value.setLatitude(request.latitude());
        value.setLongitude(request.longitude());
        value.setAccuracy(request.accuracy());
        value.setLocationCapturedAt(request.locationCapturedAt());
        value.setAddressSnapshot(null);
        value.setDeviceTimezone(request.deviceTimezone());
        value.setScheduledStartTime(schedule.startTime());
        value.setScheduledEndTime(schedule.endTime());
        value.setAllowedWindowStart(windowStart);
        value.setAllowedWindowEnd(windowEnd);
        value.setWithinAllowedWindow(true);
        return value;
    }

    private AttendanceSummaryResponse summary(CareContract contract, LocalDate date, Instant now) {
        AttendanceSchedule schedule = schedules.requireSchedule(contract, date);
        Map<AttendanceRecordType, ServiceAttendanceRecord> byType = records.findByContractAndAttendanceDateOrderByRecordedAtAsc(contract, date).stream()
                .collect(Collectors.toMap(ServiceAttendanceRecord::getRecordType, item -> item, (first, ignored) -> first));
        ServiceAttendanceRecord start = byType.get(AttendanceRecordType.START), end = byType.get(AttendanceRecordType.END);
        AttendanceStatus status;
        if (end != null) status = AttendanceStatus.ENDED;
        else if (start != null && between(now, schedule.endWindowStart(), schedule.endWindowEnd()))
            status = AttendanceStatus.CAN_END;
        else if (start != null && now.isAfter(schedule.endWindowEnd())) status = AttendanceStatus.OUTSIDE_WINDOW;
        else if (start != null) status = AttendanceStatus.IN_PROGRESS;
        else if (between(now, schedule.startWindowStart(), schedule.startWindowEnd()))
            status = AttendanceStatus.CAN_START;
        else if (now.isAfter(schedule.startWindowEnd())) status = AttendanceStatus.MISSED;
        else status = AttendanceStatus.NOT_STARTED;
        return new AttendanceSummaryResponse(
                contract.getId(), date, contract.getAssistedPerson().getNome(), schedule.startTime(), schedule.endTime(),
                schedule.startWindowStart(), schedule.startWindowEnd(), schedule.endWindowStart(), schedule.endWindowEnd(),
                status, status.getLabel(), status == AttendanceStatus.CAN_START, status == AttendanceStatus.CAN_END,
                actionMessage(status, schedule), response(start), response(end)
        );
    }

    private String actionMessage(AttendanceStatus status, AttendanceSchedule schedule) {
        return switch (status) {
            case NOT_STARTED ->
                    "Você poderá iniciar a partir de " + schedule.startWindowStart().atZone(AttendanceTimeConfig.SERVICE_ZONE).toLocalTime().format(TIME) + ".";
            case CAN_START -> "O atendimento está dentro do horário permitido para início.";
            case IN_PROGRESS -> "Atendimento iniciado. O encerramento ficará disponível no horário permitido.";
            case CAN_END -> "O atendimento pode ser encerrado agora.";
            case ENDED -> "Atendimento encerrado com sucesso.";
            case OUTSIDE_WINDOW -> "O prazo para encerrar este atendimento foi encerrado.";
            case MISSED -> "O prazo para iniciar este atendimento foi encerrado.";
        };
    }

    private AttendanceRecordResponse response(ServiceAttendanceRecord value) {
        if (value == null) return null;
        String label = value.getRecordType() == AttendanceRecordType.START ? "Início do atendimento" : "Encerramento do atendimento";
        return new AttendanceRecordResponse(value.getId(), value.getRecordType(), label, value.getRecordedAt(), value.getAttendanceDate(),
                value.getLatitude(), value.getLongitude(), value.getAccuracy(), value.getAddressSnapshot(), value.getDeviceTimezone(),
                value.getScheduledStartTime(), value.getScheduledEndTime(), value.getAllowedWindowStart(), value.getAllowedWindowEnd(), value.isWithinAllowedWindow());
    }

    private boolean between(Instant value, Instant start, Instant end) {
        return !value.isBefore(start) && !value.isAfter(end);
    }

}
