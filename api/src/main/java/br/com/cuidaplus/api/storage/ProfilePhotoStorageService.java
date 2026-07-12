package br.com.cuidaplus.api.storage;

import br.com.cuidaplus.api.common.BusinessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfilePhotoStorageService {
  private static final long MAX_SIZE = 5L * 1024 * 1024;
  private static final Map<String, String> EXTENSIONS = Map.of(
    "image/jpeg", ".jpg",
    "image/png", ".png",
    "image/webp", ".webp"
  );

  private final Path uploadDirectory;
  private final String publicBaseUrl;

  public ProfilePhotoStorageService(
    @Value("${app.upload.dir}") String uploadDirectory,
    @Value("${app.upload.public-base-url}") String publicBaseUrl
  ) {
    this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
  }

  public String store(MultipartFile photo) {
    if (photo == null || photo.isEmpty()) return null;
    if (photo.getSize() > MAX_SIZE) {
      throw new BusinessException("A foto deve ter no máximo 5 MB.", HttpStatus.PAYLOAD_TOO_LARGE);
    }

    String contentType = detectContentType(photo);
    String extension = EXTENSIONS.get(contentType);
    if (extension == null) {
      throw new BusinessException("Envie uma imagem JPEG, PNG ou WebP.", HttpStatus.BAD_REQUEST);
    }

    String filename = UUID.randomUUID() + extension;
    Path destination = uploadDirectory.resolve(filename).normalize();
    if (!destination.getParent().equals(uploadDirectory)) {
      throw new BusinessException("Nome de arquivo inválido.", HttpStatus.BAD_REQUEST);
    }

    try {
      Files.createDirectories(uploadDirectory);
      Files.copy(photo.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
      return publicBaseUrl + "/" + filename;
    } catch (IOException exception) {
      throw new BusinessException("Não foi possível salvar a foto. Tente novamente.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public Path resolve(String filename) {
    if (!filename.matches("[0-9a-fA-F-]{36}\\.(jpg|png|webp)")) return null;
    Path file = uploadDirectory.resolve(filename).normalize();
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
      throw new BusinessException("Não foi possível ler a foto enviada.", HttpStatus.BAD_REQUEST);
    }
  }
}
