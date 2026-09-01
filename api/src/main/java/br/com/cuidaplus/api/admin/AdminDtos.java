package br.com.cuidaplus.api.admin;

import br.com.cuidaplus.api.profile.CaregiverApprovalStatus;
import br.com.cuidaplus.api.profile.FormacaoCuidador;
import br.com.cuidaplus.api.profile.ModalidadeAtendimento;
import br.com.cuidaplus.api.profile.Parentesco;
import br.com.cuidaplus.api.profile.PreferenciaContato;
import br.com.cuidaplus.api.profile.ResponsibleApprovalStatus;
import br.com.cuidaplus.api.profile.ServicoOferecido;
import br.com.cuidaplus.api.profile.TempoExperiencia;
import br.com.cuidaplus.api.user.AccountStatus;
import br.com.cuidaplus.api.user.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class AdminDtos {
  private AdminDtos() {}

  public record DashboardSummary(long pendingCaregivers, long pendingResponsibles, long totalUsers,
    long blockedUsers, long recentApprovals) {}
  public record ReasonRequest(@NotBlank(message = "Informe o motivo.") @Size(max = 1000) String reason) {}
  public record UserSummary(UUID id, String name, String email, UserType profileType, String profileLabel,
    AccountStatus accountStatus, String accountStatusLabel, CaregiverApprovalStatus caregiverApprovalStatus,
    String caregiverApprovalStatusLabel, ResponsibleApprovalStatus responsibleApprovalStatus,
    String responsibleApprovalStatusLabel, Instant createdAt) {}
  public record UserPage(List<UserSummary> content, int page, int size, long totalElements, int totalPages) {}
  public record UserDetails(UUID id, String name, String email, String cpf, String phone, LocalDate birthDate,
    UserType profileType, String profileLabel, AccountStatus accountStatus, String accountStatusLabel,
    String accountBlockReason, Instant blockedAt, Instant unblockedAt, Instant lastLoginAt, Instant createdAt,
    CaregiverDetails caregiver, ResponsibleDetails responsible) {}
  public record CaregiverSummary(UUID id, UUID userId, String name, String email, String city,
    TempoExperiencia experience, Set<FormacaoCuidador> formations, CaregiverApprovalStatus status,
    String statusLabel, Instant createdAt) {}
  public record CaregiverPage(List<CaregiverSummary> content, int page, int size, long totalElements, int totalPages) {}
  public record HistoryItem(CaregiverApprovalStatus previousStatus, CaregiverApprovalStatus newStatus,
    String newStatusLabel, String reason, UUID administratorId, String administratorName, Instant createdAt) {}
  public record CaregiverDetails(UUID id, UUID userId, String name, String email, String cpf, String phone,
    String profilePhotoUrl, String biography, TempoExperiencia experience, Set<FormacaoCuidador> formations,
    String otherFormation, Set<ModalidadeAtendimento> modalities, String otherModality,
    Set<ServicoOferecido> services, String otherService, String city, String neighborhood, String state,
    Set<String> availabilityDays, Set<String> availabilityPeriods, LocalTime availabilityStart,
    LocalTime availabilityEnd, String availabilityNotes,
    CaregiverApprovalStatus status, String statusLabel, String rejectionReason, String professionalBlockReason,
    Instant analyzedAt, Instant createdAt, List<HistoryItem> history) {}
  public record ResponsibleSummary(UUID id, UUID userId, String name, String email, Parentesco relationship,
    ResponsibleApprovalStatus status, String statusLabel, Instant createdAt) {}
  public record ResponsiblePage(List<ResponsibleSummary> content, int page, int size, long totalElements, int totalPages) {}
  public record ResponsibleHistoryItem(ResponsibleApprovalStatus previousStatus, ResponsibleApprovalStatus newStatus,
    String newStatusLabel, String reason, UUID administratorId, String administratorName, Instant createdAt) {}
  public record ResponsibleDetails(UUID id, UUID userId, String name, String email, String cpf, String phone,
    Parentesco relationship, String otherRelationship, PreferenciaContato contactPreference,
    ResponsibleApprovalStatus status, String statusLabel, String rejectionReason, String blockReason,
    Instant analyzedAt, Instant createdAt, List<ResponsibleHistoryItem> history) {}
}
