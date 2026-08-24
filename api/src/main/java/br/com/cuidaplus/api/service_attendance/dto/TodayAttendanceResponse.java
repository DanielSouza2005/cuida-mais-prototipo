package br.com.cuidaplus.api.service_attendance.dto;

import java.util.List;

public record TodayAttendanceResponse(List<AttendanceSummaryResponse> content) {}
