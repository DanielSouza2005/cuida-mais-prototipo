package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.user.User;
import java.nio.file.Path;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CareOccurrencePhotoService {
  private final CareOccurrencePhotoRepository photos;
  private final TaskOccurrenceRepository occurrences;
  private final CareOccurrencePhotoStorageService storage;
  private final TaskAuditService audit;
  public CareOccurrencePhotoService(CareOccurrencePhotoRepository photos, TaskOccurrenceRepository occurrences, CareOccurrencePhotoStorageService storage, TaskAuditService audit) {
    this.photos = photos; this.occurrences = occurrences; this.storage = storage; this.audit = audit;
  }
  @Transactional public void attach(TaskOccurrence occurrence, User caregiver, List<MultipartFile> files) {
    List<MultipartFile> provided = files == null ? List.of() : files.stream().filter(file -> file != null && !file.isEmpty()).toList();
    if (provided.size() > CareOccurrencePhotoStorageService.MAX_PHOTOS) throw new BusinessException("Adicione no máximo 5 fotos.");
    for (MultipartFile file : provided) {
      var stored = storage.store(file); CareOccurrencePhoto photo = new CareOccurrencePhoto();
      photo.setOccurrence(occurrence); photo.setUploadedBy(caregiver); photo.setFileName(stored.fileName()); photo.setOriginalFileName(stored.originalFileName()); photo.setContentType(stored.contentType()); photo.setFileSize(stored.fileSize());
      photos.save(photo); audit.record(occurrence.getTask(), occurrence, caregiver, TaskAuditAction.FOTO_ANEXADA, "Foto de comprovação anexada ao cuidado.");
    }
  }
  @Transactional(readOnly = true) public PhotoFile get(UUID userId, UUID occurrenceId, UUID photoId) {
    TaskOccurrence occurrence = occurrences.findById(occurrenceId).orElseThrow(() -> new BusinessException("Cuidado não encontrado.", HttpStatus.NOT_FOUND));
    boolean caregiver = occurrence.getCaregiver().getId().equals(userId); boolean responsible = occurrence.getTask().getResponsibleCreator().getId().equals(userId);
    if (!caregiver && !responsible) throw new BusinessException("Você não tem permissão para visualizar esta foto.", HttpStatus.FORBIDDEN);
    CareOccurrencePhoto photo = photos.findByIdAndOccurrence(photoId, occurrence).orElseThrow(() -> new BusinessException("Foto não encontrada.", HttpStatus.NOT_FOUND));
    Path path = storage.resolve(photo.getFileName()); if (path == null) throw new BusinessException("Foto não encontrada.", HttpStatus.NOT_FOUND);
    return new PhotoFile(path, photo.getContentType());
  }
  public record PhotoFile(Path path, String contentType) {}
}
