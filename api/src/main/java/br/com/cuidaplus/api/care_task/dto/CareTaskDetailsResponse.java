package br.com.cuidaplus.api.care_task.dto;

import java.util.List;

public record CareTaskDetailsResponse(CareTaskSummaryResponse task, MedicationResponse medication, List<TaskAuditResponse> audit) {}
