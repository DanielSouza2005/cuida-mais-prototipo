package br.com.cuidaplus.api.auth.dto;

public record PasswordResetAuthorizationResponse(String authorization, long expiresInSeconds) {}
