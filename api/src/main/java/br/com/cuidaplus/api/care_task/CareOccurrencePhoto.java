package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "care_occurrence_photos")
public class CareOccurrencePhoto {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "occurrence_id") private TaskOccurrence occurrence;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "activity_record_id") private CareActivityRecord activityRecord;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "uploaded_by_user_id") private User uploadedBy;
  @Column(nullable = false, unique = true, length = 80) private String fileName;
  @Column(length = 255) private String originalFileName;
  @Column(nullable = false, length = 30) private String contentType;
  @Column(nullable = false) private long fileSize;
  @Column(nullable = false, updatable = false) private Instant createdAt;
  @PrePersist void create() { createdAt = Instant.now(); }
  public UUID getId() { return id; }
  public TaskOccurrence getOccurrence() { return occurrence; } public void setOccurrence(TaskOccurrence value) { occurrence = value; }
  public CareActivityRecord getActivityRecord() { return activityRecord; } public void setActivityRecord(CareActivityRecord value) { activityRecord = value; }
  public User getUploadedBy() { return uploadedBy; } public void setUploadedBy(User value) { uploadedBy = value; }
  public String getFileName() { return fileName; } public void setFileName(String value) { fileName = value; }
  public String getOriginalFileName() { return originalFileName; } public void setOriginalFileName(String value) { originalFileName = value; }
  public String getContentType() { return contentType; } public void setContentType(String value) { contentType = value; }
  public long getFileSize() { return fileSize; } public void setFileSize(long value) { fileSize = value; }
  public Instant getCreatedAt() { return createdAt; }
}
