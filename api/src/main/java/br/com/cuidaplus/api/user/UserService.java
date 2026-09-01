package br.com.cuidaplus.api.user;

import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.user.dto.UpdateProfileRequest;
import br.com.cuidaplus.api.user.dto.UserResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private static final DateTimeFormatter FRONT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
    String cpf = onlyDigits(request.cpf());

    if (userRepository.existsByEmailAndIdNot(email, userId)) {
      throw new BusinessException("E-mail já cadastrado.");
    }

    if (userRepository.existsByCpfAndIdNot(cpf, userId)) {
      throw new BusinessException("CPF já cadastrado.");
    }

    user.setFullName(request.fullName().trim());
    user.setCpf(cpf);
    user.setEmail(email);
    user.setPhone(UserService.onlyDigits(request.phone()));
    user.setBirthDate(parseBirthDate(request.birthDate()));
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

  public static LocalDate parseBirthDate(String value) {
    try {
      return LocalDate.parse(value, FRONT_DATE);
    } catch (DateTimeParseException exception) {
      throw new BusinessException("Informe a data no formato dd/mm/aaaa.");
    }
  }
}
