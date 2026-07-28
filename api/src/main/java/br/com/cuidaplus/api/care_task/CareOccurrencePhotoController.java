package br.com.cuidaplus.api.care_task;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import java.util.UUID;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/care-occurrences/{occurrenceId}/photos")
public class CareOccurrencePhotoController {
  private final CareOccurrencePhotoService service; public CareOccurrencePhotoController(CareOccurrencePhotoService service) { this.service = service; }
  @GetMapping("/{photoId}") public ResponseEntity<Resource> photo(@PathVariable UUID occurrenceId, @PathVariable UUID photoId) throws Exception {
    var photo = service.get(AuthenticatedUser.id(), occurrenceId, photoId);
    return ResponseEntity.ok().contentType(MediaType.parseMediaType(photo.contentType())).cacheControl(CacheControl.noStore()).body(new UrlResource(photo.path().toUri()));
  }
}
