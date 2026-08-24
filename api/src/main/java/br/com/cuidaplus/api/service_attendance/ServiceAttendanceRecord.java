package br.com.cuidaplus.api.service_attendance;

import br.com.cuidaplus.api.care_contract.CareContract;
import br.com.cuidaplus.api.profile.AssistedPerson;
import br.com.cuidaplus.api.user.User;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "service_attendance_records", uniqueConstraints = @UniqueConstraint(name = "uk_service_attendance_contract_date_type", columnNames = {"contract_id", "attendance_date", "record_type"}))
public class ServiceAttendanceRecord {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "contract_id") private CareContract contract;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "caregiver_id") private User caregiver;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "responsible_id") private User responsible;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "assisted_person_id") private AssistedPerson assistedPerson;
  @Column(nullable = false) private LocalDate attendanceDate;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private AttendanceRecordType recordType;
  @Column(nullable = false) private Instant recordedAt;
  @Column(nullable = false) private double latitude;
  @Column(nullable = false) private double longitude;
  @Column(nullable = false) private double accuracy;
  @Column(nullable = false) private Instant locationCapturedAt;
  @Column(length = 500) private String addressSnapshot;
  @Column(nullable = false, length = 80) private String deviceTimezone;
  @Column(nullable = false) private LocalTime scheduledStartTime;
  @Column(nullable = false) private LocalTime scheduledEndTime;
  @Column(nullable = false) private Instant allowedWindowStart;
  @Column(nullable = false) private Instant allowedWindowEnd;
  @Column(nullable = false) private boolean withinAllowedWindow;
  @Column(nullable = false, updatable = false) private Instant createdAt;
  @Column(nullable = false) private Instant updatedAt;

  @PrePersist void create() { createdAt = Instant.now(); updatedAt = createdAt; }
  @PreUpdate void update() { updatedAt = Instant.now(); }

  public UUID getId() { return id; }
  public CareContract getContract() { return contract; } public void setContract(CareContract value) { contract = value; }
  public User getCaregiver() { return caregiver; } public void setCaregiver(User value) { caregiver = value; }
  public User getResponsible() { return responsible; } public void setResponsible(User value) { responsible = value; }
  public AssistedPerson getAssistedPerson() { return assistedPerson; } public void setAssistedPerson(AssistedPerson value) { assistedPerson = value; }
  public LocalDate getAttendanceDate() { return attendanceDate; } public void setAttendanceDate(LocalDate value) { attendanceDate = value; }
  public AttendanceRecordType getRecordType() { return recordType; } public void setRecordType(AttendanceRecordType value) { recordType = value; }
  public Instant getRecordedAt() { return recordedAt; } public void setRecordedAt(Instant value) { recordedAt = value; }
  public double getLatitude() { return latitude; } public void setLatitude(double value) { latitude = value; }
  public double getLongitude() { return longitude; } public void setLongitude(double value) { longitude = value; }
  public double getAccuracy() { return accuracy; } public void setAccuracy(double value) { accuracy = value; }
  public Instant getLocationCapturedAt() { return locationCapturedAt; } public void setLocationCapturedAt(Instant value) { locationCapturedAt = value; }
  public String getAddressSnapshot() { return addressSnapshot; } public void setAddressSnapshot(String value) { addressSnapshot = value; }
  public String getDeviceTimezone() { return deviceTimezone; } public void setDeviceTimezone(String value) { deviceTimezone = value; }
  public LocalTime getScheduledStartTime() { return scheduledStartTime; } public void setScheduledStartTime(LocalTime value) { scheduledStartTime = value; }
  public LocalTime getScheduledEndTime() { return scheduledEndTime; } public void setScheduledEndTime(LocalTime value) { scheduledEndTime = value; }
  public Instant getAllowedWindowStart() { return allowedWindowStart; } public void setAllowedWindowStart(Instant value) { allowedWindowStart = value; }
  public Instant getAllowedWindowEnd() { return allowedWindowEnd; } public void setAllowedWindowEnd(Instant value) { allowedWindowEnd = value; }
  public boolean isWithinAllowedWindow() { return withinAllowedWindow; } public void setWithinAllowedWindow(boolean value) { withinAllowedWindow = value; }
}
