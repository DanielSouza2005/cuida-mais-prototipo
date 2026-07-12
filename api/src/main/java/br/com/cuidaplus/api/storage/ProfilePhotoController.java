package br.com.cuidaplus.api.storage;

import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile-photos")
public class ProfilePhotoController {
  private final ProfilePhotoStorageService storage;

  public ProfilePhotoController(ProfilePhotoStorageService storage) {
    this.storage = storage;
  }

  @GetMapping("/{filename:.+}")
  public ResponseEntity<Resource> photo(@PathVariable String filename) throws Exception {
    Path path = storage.resolve(filename);
    if (path == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(new UrlResource(path.toUri()));
  }
}
