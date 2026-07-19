package br.com.cuidaplus.api.care_task.dto;

import java.util.List;

public record CareTaskPageResponse(List<CareTaskSummaryResponse> content, int page, int size, long totalElements, int totalPages, boolean last) {}
