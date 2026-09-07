package br.com.cuidaplus.api.user;

import br.com.cuidaplus.api.user.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserResponse toResponse(User user) {
    return new UserResponse(
      user.getId(),
      user.getFullName(),
      user.getEmail(),
      user.getPhone(),
      user.getProfilePhotoUrl(),
      user.getUserType()
    );
  }
}
