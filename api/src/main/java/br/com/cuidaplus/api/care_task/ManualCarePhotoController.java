package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.security.AuthenticatedUser;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/care-diary/{recordId}/photos")
public class ManualCarePhotoController {
  private final CareOccurrencePhotoService service;

  public ManualCarePhotoController(CareOccurrencePhotoService service) { this.service = service; }

  @GetMapping("/{photoId}")
  public ResponseEntity<Resource> photo(@PathVariable UUID recordId, @PathVariable UUID photoId) throws Exception {
    var photo = service.getManual(AuthenticatedUser.id(), recordId, photoId);
    return ResponseEntity.ok().contentType(MediaType.parseMediaType(photo.contentType())).cacheControl(CacheControl.noStore()).body(new UrlResource(photo.path().toUri()));
  }
}
