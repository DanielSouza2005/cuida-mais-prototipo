package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.common.BusinessException;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CareOccurrencePhotoStorageService {
  public static final int MAX_PHOTOS = 5;
  private static final long MAX_SIZE = 5L * 1024 * 1024;
  private static final Map<String, String> EXTENSIONS = Map.of("image/jpeg", ".jpg", "image/png", ".png", "image/webp", ".webp");
  private final Path uploadDirectory;

  public CareOccurrencePhotoStorageService(@Value("${app.care-photo.upload-dir:uploads/care-occurrences}") String directory) {
    uploadDirectory = Path.of(directory).toAbsolutePath().normalize();
  }

  public StoredPhoto store(MultipartFile photo) {
    if (photo == null || photo.isEmpty()) throw new BusinessException("A foto enviada está vazia.");
    if (photo.getSize() > MAX_SIZE) throw new BusinessException("Cada foto deve ter no máximo 5 MB.", HttpStatus.PAYLOAD_TOO_LARGE);
    String contentType = detectContentType(photo);
    String extension = EXTENSIONS.get(contentType);
    if (extension == null) throw new BusinessException("Envie imagens JPEG, PNG ou WebP.");
    String fileName = UUID.randomUUID() + extension;
    Path destination = uploadDirectory.resolve(fileName).normalize();
    if (!destination.getParent().equals(uploadDirectory)) throw new BusinessException("Nome de arquivo inválido.");
    try {
      Files.createDirectories(uploadDirectory);
      Files.copy(photo.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
      return new StoredPhoto(fileName, cleanOriginalName(photo.getOriginalFilename()), contentType, photo.getSize());
    } catch (IOException exception) {
      throw new BusinessException("Não foi possível salvar a foto. Tente novamente.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public Path resolve(String fileName) {
    if (fileName == null || !fileName.matches("[0-9a-fA-F-]{36}\\.(jpg|png|webp)")) return null;
    Path file = uploadDirectory.resolve(fileName).normalize();
    return file.getParent().equals(uploadDirectory) && Files.isRegularFile(file) ? file : null;
  }

  private String detectContentType(MultipartFile photo) {
    try {
      byte[] header = photo.getInputStream().readNBytes(12);
      if (header.length >= 3 && (header[0] & 0xff) == 0xff && (header[1] & 0xff) == 0xd8 && (header[2] & 0xff) == 0xff) return "image/jpeg";
      if (header.length >= 8 && (header[0] & 0xff) == 0x89 && header[1] == 0x50 && header[2] == 0x4e && header[3] == 0x47) return "image/png";
      if (header.length >= 12 && new String(header, 0, 4).equals("RIFF") && new String(header, 8, 4).equals("WEBP")) return "image/webp";
      return null;
    } catch (IOException exception) {
      throw new BusinessException("Não foi possível ler a foto enviada.");
    }
  }

  private String cleanOriginalName(String value) {
    if (value == null || value.isBlank()) return null;
    String normalized = value.replace('\\', '/');
    String name = normalized.substring(normalized.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "");
    if (name.isBlank()) return null;
    return name.length() <= 255 ? name : name.substring(name.length() - 255);
  }
  public record StoredPhoto(String fileName, String originalFileName, String contentType, long fileSize) {}
}
