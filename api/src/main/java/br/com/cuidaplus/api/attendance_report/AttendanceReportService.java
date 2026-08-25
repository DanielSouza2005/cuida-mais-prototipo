package br.com.cuidaplus.api.attendance_report;

import br.com.cuidaplus.api.attendance_report.dto.*;
import br.com.cuidaplus.api.care_contract.*;
import br.com.cuidaplus.api.care_task.*;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.notification.*;
import br.com.cuidaplus.api.service_attendance.*;
import br.com.cuidaplus.api.user.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceReportService {
  private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

  private final AttendanceReportRepository reports;
  private final CareContractRepository contracts;
  private final ServiceAttendanceRepository attendanceRecords;
  private final TaskOccurrenceRepository occurrences;
  private final CareActivityRecordRepository activityRecords;
  private final CareOccurrencePhotoRepository photos;
  private final UserService users;
  private final NotificationService notifications;
  private final ApplicationEventPublisher events;

  public AttendanceReportService(AttendanceReportRepository reports, CareContractRepository contracts,
    ServiceAttendanceRepository attendanceRecords, TaskOccurrenceRepository occurrences,
    CareActivityRecordRepository activityRecords, CareOccurrencePhotoRepository photos, UserService users,
    NotificationService notifications, ApplicationEventPublisher events) {
    this.reports = reports;
    this.contracts = contracts;
    this.attendanceRecords = attendanceRecords;
    this.occurrences = occurrences;
    this.activityRecords = activityRecords;
    this.photos = photos;
    this.users = users;
    this.notifications = notifications;
    this.events = events;
  }

  @Transactional
  public AttendanceReportResponse generate(UUID userId, UUID contractId, LocalDate date) {
    User caregiver = requireCaregiver(userId);
    CareContract contract = contracts.findForUpdateById(contractId)
      .orElseThrow(() -> new BusinessException("Contratação não encontrada.", HttpStatus.NOT_FOUND));
    requireCaregiverOwner(caregiver, contract);
    Optional<AttendanceReport> existing = reports.findByContractAndAttendanceDate(contract, date);
    if (existing.isPresent()) return response(existing.get());

    ServiceAttendanceRecord start = requireRecord(contract, date, AttendanceRecordType.START,
      "O relatório só pode ser gerado após o início do atendimento.");
    ServiceAttendanceRecord end = requireRecord(contract, date, AttendanceRecordType.END,
      "O relatório só pode ser gerado após o encerramento do atendimento.");
    ZoneId zone = safeZone(start.getDeviceTimezone());
    List<TimelineEvent> events = timeline(contract, date, zone, start, end);
    String nursingNotes = nursingNotes(events, end, zone);

    AttendanceReport report = new AttendanceReport();
    report.setContract(contract);
    report.setAttendanceDate(date);
    report.setStartRecord(start);
    report.setEndRecord(end);
    report.setCaregiver(contract.getCaregiverUser());
    report.setResponsible(contract.getResponsibleUser());
    report.setAssistedPerson(contract.getAssistedPerson());
    report.setNursingNotes(nursingNotes);
    report.setGeneratedText(generatedText(contract, date, start, end, zone, events, nursingNotes));
    report.setStatus(AttendanceReportStatus.DRAFT);
    report.setEmailStatus(AttendanceReportEmailStatus.NOT_SENT);
    report.setGeneratedAt(Instant.now());
    return response(reports.saveAndFlush(report));
  }

  @Transactional(readOnly = true)
  public AttendanceReportResponse get(UUID userId, UUID contractId, LocalDate date) {
    User viewer = users.findById(userId);
    CareContract contract = requireContract(contractId);
    AttendanceReport report = reports.findByContractAndAttendanceDate(contract, date)
      .orElseThrow(() -> new BusinessException("Relatório de atendimento não encontrado.", HttpStatus.NOT_FOUND));
    boolean caregiver = report.getCaregiver().getId().equals(viewer.getId());
    boolean responsible = report.getResponsible().getId().equals(viewer.getId());
    if (!caregiver && !responsible) throw forbidden();
    if (responsible && report.getStatus() != AttendanceReportStatus.FINALIZED) {
      throw new BusinessException("O relatório ainda não foi finalizado.", HttpStatus.NOT_FOUND);
    }
    return response(report);
  }

  @Transactional(readOnly = true)
  public AttendanceReportResponse getById(UUID userId, UUID reportId) {
    User viewer = users.findById(userId);
    AttendanceReport report = reports.findById(reportId)
      .orElseThrow(() -> new BusinessException("Relatório de atendimento não encontrado.", HttpStatus.NOT_FOUND));
    boolean caregiver = report.getCaregiver().getId().equals(viewer.getId());
    boolean responsible = report.getResponsible().getId().equals(viewer.getId());
    if (!caregiver && !responsible) throw forbidden();
    if (responsible && report.getStatus() != AttendanceReportStatus.FINALIZED) {
      throw new BusinessException("O relatório ainda não foi finalizado.", HttpStatus.NOT_FOUND);
    }
    return response(report);
  }

  @Transactional
  public AttendanceReportResponse update(UUID userId, UUID contractId, LocalDate date, UpdateAttendanceReportRequest request) {
    AttendanceReport report = requireEditable(userId, contractId, date);
    report.setEditedText(request.editedText().trim());
    report.setAdditionalNotes(trimToNull(request.additionalNotes()));
    report.setEditedAt(Instant.now());
    return response(reports.save(report));
  }

  @Transactional
  public AttendanceReportResponse finalizeReport(UUID userId, UUID contractId, LocalDate date, UpdateAttendanceReportRequest request) {
    AttendanceReport report = requireEditable(userId, contractId, date);
    requireRecord(report.getContract(), date, AttendanceRecordType.END,
      "O relatório só pode ser finalizado após o encerramento do atendimento.");
    String finalText = request.editedText().trim();
    report.setEditedText(finalText);
    report.setFinalText(finalText);
    report.setAdditionalNotes(trimToNull(request.additionalNotes()));
    report.setEditedAt(Instant.now());
    report.setFinalizedAt(Instant.now());
    report.setStatus(AttendanceReportStatus.FINALIZED);
    report.setEmailStatus(AttendanceReportEmailStatus.PENDING);
    report.setEmailRequestedAt(Instant.now());
    report.setEmailAttempts(0);
    report.setEmailNextRetryAt(null);
    report.setEmailErrorMessage(null);

    AttendanceReport saved = reports.saveAndFlush(report);
    notifications.create(report.getResponsible(), NotificationType.ATTENDANCE_REPORT_AVAILABLE,
      "Relatório de atendimento disponível", "O relatório do atendimento foi finalizado e já pode ser consultado.",
      RelatedEntityType.ATTENDANCE_REPORT, report.getId());
    events.publishEvent(new AttendanceReportEmailRequested(saved.getId()));
    return response(saved);
  }

  private AttendanceReport requireEditable(UUID userId, UUID contractId, LocalDate date) {
    User caregiver = requireCaregiver(userId);
    CareContract contract = requireContract(contractId);
    AttendanceReport report = reports.findByContractAndAttendanceDate(contract, date)
      .orElseThrow(() -> new BusinessException("Relatório de atendimento não encontrado.", HttpStatus.NOT_FOUND));
    if (!report.getCaregiver().getId().equals(caregiver.getId())) throw forbidden();
    if (report.getStatus() == AttendanceReportStatus.FINALIZED) {
      throw new BusinessException("O relatório já foi finalizado.", HttpStatus.CONFLICT);
    }
    return report;
  }

  private List<TimelineEvent> timeline(CareContract contract, LocalDate date, ZoneId zone,
    ServiceAttendanceRecord start, ServiceAttendanceRecord end) {
    List<TimelineEvent> result = new ArrayList<>();
    result.add(new TimelineEvent(start.getRecordedAt(), "Atendimento iniciado. Localização registrada.", "INICIO", 0));
    for (TaskOccurrence occurrence : occurrences.findByContractAndScheduledDateOrderByScheduledInstantUtcAsc(contract, date)) {
      if (!ContractCareSchedulePolicy.contains(start.getScheduledStartTime(), start.getScheduledEndTime(), occurrence.getScheduledTime())) continue;
      Instant instant = occurrence.getCompletedAt() != null ? occurrence.getCompletedAt()
        : occurrence.getStatusUpdatedAt() != null ? occurrence.getStatusUpdatedAt() : occurrence.getScheduledInstantUtc();
      StringBuilder text = new StringBuilder(categoryLabel(occurrence.getTask().getCategory())).append(": ")
        .append(clean(occurrence.getTask().getTitle())).append(". Situação: ").append(statusLabel(occurrence.getStatus())).append(".");
      if (occurrence.getCompletedAt() == null && occurrence.getStatusUpdatedAt() == null) text.append(" Horário previsto.");
      append(text, "Descrição", occurrence.getTask().getDescription());
      append(text, "Observação", occurrence.getExecutionNote());
      append(text, "Justificativa", occurrence.getNonCompletionReason());
      append(text, "Detalhes registrados", occurrence.getTask().getNotes());
      if (!photos.findByOccurrenceOrderByCreatedAtAsc(occurrence).isEmpty()) text.append(" Com foto anexada.");
      result.add(new TimelineEvent(instant, text.toString(), "PLANEJADO", 1));
    }
    for (CareActivityRecord record : activityRecords.findByContractAndEntryDateAndSourceTypeOrderByOccurredAtAsc(contract, date, CareRecordSourceType.MANUAL)) {
      if (record.getOccurredAt().isBefore(start.getRecordedAt()) || record.getOccurredAt().isAfter(end.getRecordedAt())) continue;
      StringBuilder text = new StringBuilder("Anotação — ").append(manualTypeLabel(record.getCareType())).append(": ")
        .append(clean(record.getTitle())).append(". ").append(clean(record.getDescription()));
      append(text, "Observação", record.getNotes());
      if (!photos.findByActivityRecordOrderByCreatedAtAsc(record).isEmpty()) text.append(" Com foto anexada.");
      result.add(new TimelineEvent(record.getOccurredAt(), text.toString().trim(), "MANUAL", 1));
    }
    result.sort(Comparator.comparingInt(TimelineEvent::boundary).thenComparing(TimelineEvent::instant));
    result.add(new TimelineEvent(end.getRecordedAt(), "Atendimento encerrado. Localização registrada.", "ENCERRAMENTO", 2));
    return result;
  }

  private String generatedText(CareContract contract, LocalDate date, ServiceAttendanceRecord start,
    ServiceAttendanceRecord end, ZoneId zone, List<TimelineEvent> events, String nursingNotes) {
    StringBuilder text = new StringBuilder("Relatório de atendimento\n\n")
      .append("Pessoa assistida: ").append(contract.getAssistedPerson().getNome()).append('\n')
      .append("Cuidador responsável pelo atendimento: ").append(contract.getCaregiverUser().getFullName()).append('\n')
      .append("Data do atendimento: ").append(date.format(DATE)).append('\n')
      .append("Horário previsto: ").append(start.getScheduledStartTime().format(TIME)).append(" às ").append(start.getScheduledEndTime().format(TIME)).append('\n')
      .append("Horário de início registrado: ").append(formatInstant(start.getRecordedAt(), zone)).append('\n')
      .append("Horário de encerramento registrado: ").append(formatInstant(end.getRecordedAt(), zone)).append("\n\n")
      .append("Resumo do atendimento:\nO atendimento foi iniciado às ").append(formatInstant(start.getRecordedAt(), zone))
      .append(" e encerrado às ").append(formatInstant(end.getRecordedAt(), zone))
      .append(". Durante o período, foram registrados os cuidados e as anotações abaixo, em ordem cronológica.\n\n")
      .append("Linha do tempo do atendimento:\n");
    for (TimelineEvent event : events) text.append(formatInstant(event.instant(), zone)).append(" — ").append(event.text()).append('\n');
    return text.append("\nAnotações de enfermagem:\n").append(nursingNotes)
      .append("\n\nObservações adicionais:\nCampo reservado para complemento do cuidador.").toString();
  }

  private String nursingNotes(List<TimelineEvent> events, ServiceAttendanceRecord end, ZoneId zone) {
    List<TimelineEvent> careEvents = events.stream().filter(item -> item.boundary() == 1).toList();
    if (careEvents.isEmpty()) {
      return "Não foram registrados cuidados ou observações adicionais durante o atendimento. O atendimento foi encerrado às " + formatInstant(end.getRecordedAt(), zone) + ".";
    }
    long notDone = careEvents.stream().filter(item -> item.text().contains("Situação: não realizado")).count();
    long late = careEvents.stream().filter(item -> item.text().contains("Situação: atrasado")).count();
    long manual = careEvents.stream().filter(item -> item.source().equals("MANUAL")).count();
    StringBuilder text = new StringBuilder("Durante o atendimento, foram registrados ").append(careEvents.size())
      .append(careEvents.size() == 1 ? " cuidado ou anotação" : " cuidados ou anotações").append(".");
    for (TimelineEvent event : careEvents) {
      text.append(" Às ").append(formatInstant(event.instant(), zone)).append(", ")
        .append(Character.toLowerCase(event.text().charAt(0))).append(event.text().substring(1));
    }
    if (notDone > 0) text.append(" ").append(notDone).append(notDone == 1 ? " cuidado foi registrado como não realizado." : " cuidados foram registrados como não realizados.");
    if (late > 0) text.append(" ").append(late).append(late == 1 ? " cuidado apresentou registro de atraso." : " cuidados apresentaram registro de atraso.");
    if (manual > 0) text.append(" Também houve ").append(manual).append(manual == 1 ? " anotação avulsa." : " anotações avulsas.");
    return text.append(" O atendimento foi encerrado às ").append(formatInstant(end.getRecordedAt(), zone)).append(".").toString();
  }

  private ServiceAttendanceRecord requireRecord(CareContract contract, LocalDate date, AttendanceRecordType type, String message) {
    return attendanceRecords.findByContractAndAttendanceDateAndRecordType(contract, date, type)
      .orElseThrow(() -> new BusinessException(message, HttpStatus.CONFLICT));
  }

  private CareContract requireContract(UUID id) {
    return contracts.findById(id).orElseThrow(() -> new BusinessException("Contratação não encontrada.", HttpStatus.NOT_FOUND));
  }

  private User requireCaregiver(UUID id) {
    User user = users.findById(id);
    if (user.getUserType() != UserType.CUIDADOR && user.getUserType() != UserType.CAREGIVER) {
      throw new BusinessException("Apenas o cuidador pode alterar o relatório.", HttpStatus.FORBIDDEN);
    }
    return user;
  }

  private void requireCaregiverOwner(User caregiver, CareContract contract) {
    if (!contract.getCaregiverUser().getId().equals(caregiver.getId())) throw forbidden();
  }

  private BusinessException forbidden() {
    return new BusinessException("Você não tem permissão para acessar este relatório.", HttpStatus.FORBIDDEN);
  }

  private AttendanceReportResponse response(AttendanceReport report) {
    String editable = report.getEditedText() == null ? report.getGeneratedText() : report.getEditedText();
    return new AttendanceReportResponse(report.getId(), report.getContract().getId(), report.getAttendanceDate(),
      report.getAssistedPerson().getNome(), report.getCaregiver().getFullName(), report.getStartRecord().getScheduledStartTime(),
      report.getStartRecord().getScheduledEndTime(), report.getStartRecord().getRecordedAt(), report.getEndRecord().getRecordedAt(),
      report.getGeneratedText(), editable, report.getFinalText(), report.getAdditionalNotes(), report.getNursingNotes(),
      report.getStatus(), report.getStatus().getLabel(), report.getEmailStatus(), report.getGeneratedAt(), report.getEditedAt(), report.getFinalizedAt());
  }

  private String categoryLabel(TaskCategory category) {
    return switch (category) {
      case MEDICACAO -> "Medicação"; case ALIMENTACAO -> "Alimentação"; case HIDRATACAO -> "Hidratação";
      case HIGIENE_BANHO -> "Higiene"; case MOBILIDADE -> "Mobilidade"; case EXERCICIO -> "Exercício";
      case CURATIVO -> "Curativo"; case SINAIS_VITAIS -> "Sinais vitais"; case CONSULTA_COMPROMISSO -> "Consulta ou compromisso";
      case PERSONALIZADA -> "Outro cuidado";
    };
  }

  private String manualTypeLabel(String value) {
    if (value == null) return "Outro cuidado";
    return switch (value) {
      case "MEDICACAO" -> "Medicação"; case "ALIMENTACAO" -> "Alimentação"; case "HIGIENE" -> "Higiene";
      case "MOBILIDADE" -> "Mobilidade"; case "COMPANHIA" -> "Companhia"; case "OBSERVACAO" -> "Observação";
      case "OCORRENCIA" -> "Ocorrência"; default -> "Outro cuidado";
    };
  }

  private String statusLabel(TaskOccurrenceStatus status) {
    return switch (status) {
      case PENDENTE -> "pendente"; case CONCLUIDA -> "concluído"; case ATRASADA -> "atrasado";
      case NAO_REALIZADA -> "não realizado"; case CANCELADA -> "cancelado";
    };
  }

  private void append(StringBuilder target, String label, String value) {
    if (value != null && !value.isBlank()) target.append(' ').append(label).append(": ").append(value.trim()).append('.');
  }

  private String clean(String value) { return value == null || value.isBlank() ? "Informação não registrada" : value.trim(); }
  private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
  private ZoneId safeZone(String value) { try { return ZoneId.of(value); } catch (Exception ignored) { return AttendanceTimeConfig.SERVICE_ZONE; } }
  private String formatInstant(Instant value, ZoneId zone) { return value.atZone(zone).toLocalTime().format(TIME); }

  private record TimelineEvent(Instant instant, String text, String source, int boundary) {}
}
