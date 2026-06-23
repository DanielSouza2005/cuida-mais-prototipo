package br.com.cuidaplus.api.user;

import br.com.cuidaplus.api.user.dto.UserResponse;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  private static final DateTimeFormatter FRONT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  public UserResponse toResponse(User user) {
    return new UserResponse(
      user.getId(),
      user.getFullName(),
      formatCpf(user.getCpf()),
      user.getEmail(),
      FRONT_DATE.format(user.getBirthDate()),
      user.getUserType()
    );
  }

  private String formatCpf(String cpf) {
    if (cpf == null || cpf.length() != 11) {
      return cpf;
    }

    return cpf.substring(0, 3)
      + "."
      + cpf.substring(3, 6)
      + "."
      + cpf.substring(6, 9)
      + "-"
      + cpf.substring(9);
  }
}
