package br.com.cuidaplus.api.caregiver.dto;

public record CaregiverLocationSuggestionResponse(
  String id,
  String label,
  String type,
  String city,
  String neighborhood,
  String state
) {}
