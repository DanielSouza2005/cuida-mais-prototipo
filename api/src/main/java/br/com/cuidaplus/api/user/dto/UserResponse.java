package br.com.cuidaplus.api.user.dto;

import br.com.cuidaplus.api.user.UserType;
import java.util.UUID;

public record UserResponse(
  UUID id,
  String fullName,
  String email,
  String phone,
  String profilePhotoUrl,
  UserType userType
) {}
