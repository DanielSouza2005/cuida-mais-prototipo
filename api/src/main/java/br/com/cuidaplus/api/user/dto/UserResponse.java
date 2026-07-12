package br.com.cuidaplus.api.user.dto;

import br.com.cuidaplus.api.user.UserType;
import java.util.UUID;

public record UserResponse(
  UUID id,
  String fullName,
  String cpf,
  String email,
  String phone,
  String profilePhotoUrl,
  String birthDate,
  UserType userType
) {}
