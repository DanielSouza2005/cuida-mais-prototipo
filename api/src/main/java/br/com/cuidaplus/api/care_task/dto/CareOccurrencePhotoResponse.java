package br.com.cuidaplus.api.care_task.dto;

import java.time.Instant;
import java.util.UUID;

public record CareOccurrencePhotoResponse(UUID id, String url, String contentType, long fileSize, Instant createdAt) {}
