package br.com.cuidaplus.api.service_request.dto;

import java.util.List;

public record ResponsibleServicePublicationPageResponse(
  List<ResponsibleServicePublicationResponse> content,
  int page,
  int size,
  long totalElements,
  int totalPages,
  boolean last
) {}
