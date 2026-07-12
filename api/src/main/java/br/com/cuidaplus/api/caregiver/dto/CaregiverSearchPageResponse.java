package br.com.cuidaplus.api.caregiver.dto;

import java.util.List;

public record CaregiverSearchPageResponse(
  List<CaregiverSearchItemResponse> content,
  int page,
  int size,
  long totalElements,
  int totalPages,
  boolean first,
  boolean last
) {}
