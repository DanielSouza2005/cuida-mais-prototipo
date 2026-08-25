package br.com.cuidaplus.api.attendance_report;

import br.com.cuidaplus.api.care_contract.CareContract;
import br.com.cuidaplus.api.profile.AssistedPerson;
import br.com.cuidaplus.api.service_attendance.ServiceAttendanceRecord;
import br.com.cuidaplus.api.user.User;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "attendance_reports", uniqueConstraints = @UniqueConstraint(name = "uk_attendance_report_contract_date", columnNames = {"contract_id", "attendance_date"}))
public class AttendanceReport {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "contract_id") private CareContract contract;
  @Column(nullable = false) private LocalDate attendanceDate;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "attendance_start_record_id") private ServiceAttendanceRecord startRecord;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "attendance_end_record_id") private ServiceAttendanceRecord endRecord;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "caregiver_id") private User caregiver;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "responsible_id") private User responsible;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "assisted_person_id") private AssistedPerson assistedPerson;
  @Column(nullable = false, columnDefinition = "TEXT") private String generatedText;
  @Column(columnDefinition = "TEXT") private String editedText;
  @Column(columnDefinition = "TEXT") private String finalText;
  @Column(length = 4000) private String additionalNotes;
  @Column(nullable = false, columnDefinition = "TEXT") private String nursingNotes;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private AttendanceReportStatus status;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private AttendanceReportEmailStatus emailStatus = AttendanceReportEmailStatus.NOT_SENT;
  private Instant emailRequestedAt;
  private Instant emailSentAt;
  @Column(nullable = false) private int emailAttempts;
  private Instant emailNextRetryAt;
  @Column(length = 500) private String emailErrorMessage;
  @Column(nullable = false) private Instant generatedAt;
  private Instant editedAt;
  private Instant finalizedAt;
  @Column(nullable = false, updatable = false) private Instant createdAt;
  @Column(nullable = false) private Instant updatedAt;

  @PrePersist void create() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
  @PreUpdate void update() { updatedAt = Instant.now(); }

  public UUID getId() { return id; }
  public CareContract getContract() { return contract; } public void setContract(CareContract value) { contract = value; }
  public LocalDate getAttendanceDate() { return attendanceDate; } public void setAttendanceDate(LocalDate value) { attendanceDate = value; }
  public ServiceAttendanceRecord getStartRecord() { return startRecord; } public void setStartRecord(ServiceAttendanceRecord value) { startRecord = value; }
  public ServiceAttendanceRecord getEndRecord() { return endRecord; } public void setEndRecord(ServiceAttendanceRecord value) { endRecord = value; }
  public User getCaregiver() { return caregiver; } public void setCaregiver(User value) { caregiver = value; }
  public User getResponsible() { return responsible; } public void setResponsible(User value) { responsible = value; }
  public AssistedPerson getAssistedPerson() { return assistedPerson; } public void setAssistedPerson(AssistedPerson value) { assistedPerson = value; }
  public String getGeneratedText() { return generatedText; } public void setGeneratedText(String value) { generatedText = value; }
  public String getEditedText() { return editedText; } public void setEditedText(String value) { editedText = value; }
  public String getFinalText() { return finalText; } public void setFinalText(String value) { finalText = value; }
  public String getAdditionalNotes() { return additionalNotes; } public void setAdditionalNotes(String value) { additionalNotes = value; }
  public String getNursingNotes() { return nursingNotes; } public void setNursingNotes(String value) { nursingNotes = value; }
  public AttendanceReportStatus getStatus() { return status; } public void setStatus(AttendanceReportStatus value) { status = value; }
  public AttendanceReportEmailStatus getEmailStatus() { return emailStatus; } public void setEmailStatus(AttendanceReportEmailStatus value) { emailStatus = value; }
  public Instant getEmailRequestedAt() { return emailRequestedAt; } public void setEmailRequestedAt(Instant value) { emailRequestedAt = value; }
  public Instant getEmailSentAt() { return emailSentAt; } public void setEmailSentAt(Instant value) { emailSentAt = value; }
  public int getEmailAttempts() { return emailAttempts; } public void setEmailAttempts(int value) { emailAttempts = value; }
  public Instant getEmailNextRetryAt() { return emailNextRetryAt; } public void setEmailNextRetryAt(Instant value) { emailNextRetryAt = value; }
  public String getEmailErrorMessage() { return emailErrorMessage; } public void setEmailErrorMessage(String value) { emailErrorMessage = value; }
  public Instant getGeneratedAt() { return generatedAt; } public void setGeneratedAt(Instant value) { generatedAt = value; }
  public Instant getEditedAt() { return editedAt; } public void setEditedAt(Instant value) { editedAt = value; }
  public Instant getFinalizedAt() { return finalizedAt; } public void setFinalizedAt(Instant value) { finalizedAt = value; }
}
