package br.com.cuidaplus.api.care_task.dto;

import java.util.List;

public record CareDiaryResponse(List<CareDiaryItemResponse> content) {}
