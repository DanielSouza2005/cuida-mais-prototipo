package br.com.cuidaplus.api.user;

import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.user.dto.UpdateProfileRequest;
import br.com.cuidaplus.api.user.dto.UserResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public UserService(UserRepository userRepository, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
  }

  @Transactional(readOnly = true)
  public UserResponse findProfile(UUID userId) {
    return userMapper.toResponse(findById(userId));
  }

  @Transactional
  public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
    User user = findById(userId);
    String email = normalizeEmail(request.email());

    if (userRepository.existsByEmailAndIdNot(email, userId)) {
      throw new BusinessException("E-mail já cadastrado.");
    }

    user.setFullName(request.fullName().trim());
    user.setEmail(email);
    user.setPhone(optionalDigits(request.phone()));
    // O papel é definido no cadastro e nunca pode ser promovido por uma edição de perfil.

    return userMapper.toResponse(user);
  }

  public User findById(UUID userId) {
    return userRepository
      .findById(userId)
      .orElseThrow(() -> new BusinessException("Usuário não encontrado.", HttpStatus.NOT_FOUND));
  }

  public static String normalizeEmail(String email) {
    return email.trim().toLowerCase();
  }

  public static String onlyDigits(String value) {
    return value == null ? "" : value.replaceAll("\\D", "");
  }

  public static String optionalDigits(String value) {
    String digits = onlyDigits(value);
    return digits.isBlank() ? null : digits;
  }
}
