package br.com.cuidaplus.api.care_task;

import br.com.cuidaplus.api.care_task.dto.CareDiaryItemResponse;
import br.com.cuidaplus.api.care_task.dto.CareDiaryResponse;
import br.com.cuidaplus.api.care_task.dto.CreateManualCareRequest;
import br.com.cuidaplus.api.care_task.dto.ManualCareFormDataResponse;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class CareDiaryController {
  private final CareDiaryService service;
  public CareDiaryController(CareDiaryService service) { this.service = service; }

  @GetMapping("/caregiver/care-occurrences/manual/form-data")
  public ManualCareFormDataResponse formData(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return service.formData(AuthenticatedUser.id(), date);
  }

  @PostMapping(value = "/caregiver/care-occurrences/manual", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public CareDiaryItemResponse create(@Valid @ModelAttribute CreateManualCareRequest request,
    @RequestPart(name = "photos", required = false) List<MultipartFile> photos) {
    return service.createManual(AuthenticatedUser.id(), request, photos);
  }

  @GetMapping("/caregiver/care-occurrences/manual/{id}")
  public CareDiaryItemResponse caregiverDetails(@PathVariable UUID id) { return service.caregiverDetails(AuthenticatedUser.id(), id); }

  @GetMapping("/responsible/care-occurrences/manual/{id}")
  public CareDiaryItemResponse responsibleDetails(@PathVariable UUID id) { return service.responsibleDetails(AuthenticatedUser.id(), id); }

  @GetMapping("/caregiver/assisted-person-diary")
  public CareDiaryResponse caregiverDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
    @RequestParam(defaultValue = "America/Sao_Paulo") String timezone, @RequestParam(required = false) UUID assistedPersonId,
    @RequestParam(required = false) UUID contractId) {
    return service.caregiverDiary(AuthenticatedUser.id(), date, timezone, assistedPersonId, contractId);
  }

  @GetMapping("/responsible/assisted-person-diary")
  public CareDiaryResponse responsibleDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
    @RequestParam(defaultValue = "America/Sao_Paulo") String timezone, @RequestParam(required = false) UUID assistedPersonId,
    @RequestParam(required = false) UUID contractId) {
    return service.responsibleDiary(AuthenticatedUser.id(), date, timezone, assistedPersonId, contractId);
  }
}
