package br.com.cuidaplus.api.care_task.dto;

import br.com.cuidaplus.api.care_task.CareRecordSourceType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CareDiaryItemResponse(
  UUID id,
  CareRecordSourceType sourceType,
  String sourceLabel,
  UUID occurrenceId,
  UUID manualEntryId,
  UUID contractId,
  LocalDate date,
  LocalTime time,
  Instant occurredAt,
  Instant registeredAt,
  String careType,
  String careTypeLabel,
  String title,
  String description,
  String notes,
  String status,
  String statusLabel,
  UUID assistedPersonId,
  String assistedPersonName,
  String caregiverName,
  boolean important,
  List<CareOccurrencePhotoResponse> photos
) {}
