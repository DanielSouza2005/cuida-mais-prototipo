package br.com.cuidaplus.api.auth.dto;

import br.com.cuidaplus.api.user.dto.UserResponse;

public record RegisterProfileResponse(
  UserResponse user,
  String token
) {}
