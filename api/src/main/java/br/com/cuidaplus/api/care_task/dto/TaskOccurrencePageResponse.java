package br.com.cuidaplus.api.care_task.dto;

import java.util.List;

public record TaskOccurrencePageResponse(List<TaskOccurrenceResponse> content, int page, int size, long totalElements, int totalPages, boolean last) {}
