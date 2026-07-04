package br.com.cuidaplus.api.profile;

import br.com.cuidaplus.api.auth.dto.AddressRequest;
import br.com.cuidaplus.api.common.MessageResponse;
import br.com.cuidaplus.api.profile.dto.CaregiverAvailabilityUpdateRequest;
import br.com.cuidaplus.api.profile.dto.CaregiverExperienceUpdateRequest;
import br.com.cuidaplus.api.profile.dto.CaregiverModalitiesUpdateRequest;
import br.com.cuidaplus.api.profile.dto.CaregiverServicesUpdateRequest;
import br.com.cuidaplus.api.profile.dto.PersonalInfoUpdateRequest;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

  @PatchMapping("/personal-info")
  public MessageResponse updatePersonalInfo(@Valid @RequestBody PersonalInfoUpdateRequest request) {
    return profileService.updatePersonalInfo(AuthenticatedUser.id(), request);
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
