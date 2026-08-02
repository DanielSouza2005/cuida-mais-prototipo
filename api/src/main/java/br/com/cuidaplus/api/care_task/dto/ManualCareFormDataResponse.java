package br.com.cuidaplus.api.care_task.dto;

import java.time.LocalDate;
import java.util.List;

public record ManualCareFormDataResponse(LocalDate date, List<CareDiaryContractOptionResponse> contracts) {}
