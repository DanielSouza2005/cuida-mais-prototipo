package br.com.cuidaplus.api.profile;

import br.com.cuidaplus.api.auth.dto.AddressRequest;
import br.com.cuidaplus.api.common.MessageResponse;
import br.com.cuidaplus.api.profile.dto.CaregiverAvailabilityUpdateRequest;
import br.com.cuidaplus.api.profile.dto.CaregiverExperienceUpdateRequest;
import br.com.cuidaplus.api.profile.dto.CaregiverModalitiesUpdateRequest;
import br.com.cuidaplus.api.profile.dto.CaregiverServicesUpdateRequest;
import br.com.cuidaplus.api.profile.dto.AssistedPersonUpdateRequest;
import br.com.cuidaplus.api.profile.dto.EmergencyContactUpdateRequest;
import br.com.cuidaplus.api.profile.dto.PersonalInfoUpdateRequest;
import br.com.cuidaplus.api.profile.dto.ResponsibleProfileUpdateRequest;
import br.com.cuidaplus.api.profile.dto.ProfilePhotoResponse;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

  private final ProfileService profileService;

  public ProfileController(
    ProfileService profileService
  ) {
    this.profileService = profileService;
  }

  @GetMapping("/me")
  public Map<String, Object> me() {
    return profileService.findMyProfile(AuthenticatedUser.id());
  }

  @PatchMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ProfilePhotoResponse updatePhoto(@RequestPart("photo") MultipartFile photo) {
    return profileService.updateProfilePhoto(AuthenticatedUser.id(), photo);
  }

  @DeleteMapping("/photo")
  public ProfilePhotoResponse deletePhoto() {
    return profileService.deleteProfilePhoto(AuthenticatedUser.id());
  }

  @PatchMapping("/personal-info")
  public MessageResponse updatePersonalInfo(@Valid @RequestBody PersonalInfoUpdateRequest request) {
    return profileService.updatePersonalInfo(AuthenticatedUser.id(), request);
  }

  @PatchMapping("/responsible")
  public MessageResponse updateResponsibleProfile(@Valid @RequestBody ResponsibleProfileUpdateRequest request) {
    return profileService.updateResponsibleProfile(AuthenticatedUser.id(), request);
  }

  @PatchMapping("/assisted-persons/{id}")
  public MessageResponse updateAssistedPerson(
    @PathVariable UUID id,
    @Valid @RequestBody AssistedPersonUpdateRequest request
  ) {
    return profileService.updateAssistedPerson(AuthenticatedUser.id(), id, request);
  }

  @PatchMapping("/assisted-persons/{id}/care-address")
  public MessageResponse updateCareAddress(
    @PathVariable UUID id,
    @Valid @RequestBody AddressRequest request
  ) {
    return profileService.updateCareAddress(AuthenticatedUser.id(), id, request);
  }

  @PatchMapping("/assisted-persons/{id}/emergency-contact")
  public MessageResponse updateEmergencyContact(
    @PathVariable UUID id,
    @Valid @RequestBody EmergencyContactUpdateRequest request
  ) {
    return profileService.updateEmergencyContact(AuthenticatedUser.id(), id, request);
  }

  @PatchMapping("/caregiver/address")
  public MessageResponse updateCaregiverAddress(@Valid @RequestBody AddressRequest request) {
    return profileService.updateCaregiverAddress(AuthenticatedUser.id(), request);
  }

  @PatchMapping("/caregiver/experience")
  public MessageResponse updateCaregiverExperience(@Valid @RequestBody CaregiverExperienceUpdateRequest request) {
    return profileService.updateCaregiverExperience(AuthenticatedUser.id(), request);
  }

  @PatchMapping("/caregiver/availability")
  public MessageResponse updateCaregiverAvailability(@Valid @RequestBody CaregiverAvailabilityUpdateRequest request) {
    return profileService.updateCaregiverAvailability(AuthenticatedUser.id(), request);
  }

  @PatchMapping("/caregiver/modalities")
  public MessageResponse updateCaregiverModalities(@Valid @RequestBody CaregiverModalitiesUpdateRequest request) {
    return profileService.updateCaregiverModalities(AuthenticatedUser.id(), request);
  }

  @PatchMapping("/caregiver/services")
  public MessageResponse updateCaregiverServices(@Valid @RequestBody CaregiverServicesUpdateRequest request) {
    return profileService.updateCaregiverServices(AuthenticatedUser.id(), request);
  }
}
