package br.com.cuidaplus.api.user;

import br.com.cuidaplus.api.security.AuthenticatedUser;
import br.com.cuidaplus.api.user.dto.UpdateProfileRequest;
import br.com.cuidaplus.api.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  public UserResponse me() {
    return userService.findProfile(AuthenticatedUser.id());
  }

  @PutMapping("/me")
  public UserResponse updateMe(@Valid @RequestBody UpdateProfileRequest request) {
    return userService.updateProfile(AuthenticatedUser.id(), request);
  }
}
