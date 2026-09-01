package br.com.cuidaplus.api.admin;

import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.email.EmailService;
import br.com.cuidaplus.api.profile.AddressFields;
import br.com.cuidaplus.api.profile.CaregiverApprovalStatus;
import br.com.cuidaplus.api.profile.CaregiverProfile;
import br.com.cuidaplus.api.profile.CaregiverProfileRepository;
import br.com.cuidaplus.api.profile.ResponsibleApprovalStatus;
import br.com.cuidaplus.api.profile.ResponsibleProfile;
import br.com.cuidaplus.api.profile.ResponsibleProfileRepository;
import br.com.cuidaplus.api.user.AccountStatus;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserRepository;
import br.com.cuidaplus.api.user.UserType;
import java.text.Normalizer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class AdminService {
  private final UserRepository users;
  private final CaregiverProfileRepository caregivers;
  private final CaregiverStatusHistoryRepository histories;
  private final ResponsibleProfileRepository responsibles;
  private final ResponsibleStatusHistoryRepository responsibleHistories;
  private final EmailService emails;

  public AdminService(UserRepository users, CaregiverProfileRepository caregivers,
    CaregiverStatusHistoryRepository histories, ResponsibleProfileRepository responsibles,
    ResponsibleStatusHistoryRepository responsibleHistories, EmailService emails) {
    this.users = users; this.caregivers = caregivers; this.histories = histories;
    this.responsibles = responsibles; this.responsibleHistories = responsibleHistories; this.emails = emails;
  }

  @Transactional(readOnly = true)
  public AdminDtos.DashboardSummary dashboard() {
    Instant since = Instant.now().minus(7, ChronoUnit.DAYS);
    long recentApprovals = histories.countByNewStatusAndCriadoEmGreaterThanEqual(CaregiverApprovalStatus.APROVADO, since)
      + responsibleHistories.countByNewStatusAndCriadoEmGreaterThanEqual(ResponsibleApprovalStatus.APROVADO, since);
    return new AdminDtos.DashboardSummary(
      caregivers.countBySituacaoAprovacao(CaregiverApprovalStatus.PENDENTE),
      responsibles.countBySituacaoAprovacao(ResponsibleApprovalStatus.PENDENTE),
      users.count(), users.countByAccountStatus(AccountStatus.BLOQUEADO), recentApprovals);
  }

  @Transactional(readOnly = true)
  public AdminDtos.UserPage users(String query, UserType type, AccountStatus status,
    CaregiverApprovalStatus caregiverStatus, ResponsibleApprovalStatus responsibleStatus, int page, int size) {
    int safePage = Math.max(page, 0), safeSize = Math.min(Math.max(size, 1), 50);
    List<AdminDtos.UserSummary> values = users.findAll().stream()
      .filter(user -> type == null || canonicalType(user.getUserType()) == canonicalType(type))
      .filter(user -> status == null || user.getAccountStatus() == status)
      .filter(user -> matches(user, query))
      .filter(user -> caregiverStatus == null || caregivers.findByUser(user)
        .map(profile -> profile.getSituacaoAprovacao() == caregiverStatus).orElse(false))
      .filter(user -> responsibleStatus == null || responsibles.findByUser(user)
        .map(profile -> profile.getSituacaoAprovacao() == responsibleStatus).orElse(false))
      .sorted(Comparator.comparing(User::getCreatedAt).reversed())
      .map(this::userSummary).toList();
    int from = Math.min(safePage * safeSize, values.size()), to = Math.min(from + safeSize, values.size());
    return new AdminDtos.UserPage(values.subList(from, to), safePage, safeSize, values.size(), pages(values.size(), safeSize));
  }

  @Transactional(readOnly = true)
  public AdminDtos.UserDetails user(UUID id) {
    User value = requireUser(id);
    return new AdminDtos.UserDetails(value.getId(), value.getFullName(), value.getEmail(), formatCpf(value.getCpf()),
      value.getPhone(), value.getBirthDate(), value.getUserType(), profileLabel(value.getUserType()),
      value.getAccountStatus(), accountLabel(value.getAccountStatus()), value.getMotivoBloqueio(),
      value.getBloqueadoEm(), value.getDesbloqueadoEm(), value.getUltimoLoginEm(), value.getCreatedAt(),
      caregivers.findByUser(value).map(this::caregiverDetails).orElse(null),
      responsibles.findByUser(value).map(this::responsibleDetails).orElse(null));
  }

  @Transactional
  public AdminDtos.UserDetails blockUser(UUID administratorId, UUID userId, String reason) {
    User administrator = requireAdmin(administratorId), target = requireUserForUpdate(userId);
    if (administrator.getId().equals(target.getId())) throw new BusinessException("Você não pode bloquear a própria conta.", HttpStatus.CONFLICT);
    if (target.getAccountStatus() != AccountStatus.ATIVO)
      throw new BusinessException("Somente usuários ativos podem ser bloqueados.", HttpStatus.CONFLICT);
    if (target.isAdmin() && users.countByUserTypeAndAccountStatus(UserType.ADMIN, AccountStatus.ATIVO) <= 1)
      throw new BusinessException("O último administrador ativo não pode ser bloqueado.", HttpStatus.CONFLICT);
    target.setAccountStatus(AccountStatus.BLOQUEADO); target.setMotivoBloqueio(cleanReason(reason));
    target.setBloqueadoEm(Instant.now()); target.setBloqueadoPorUsuarioId(administrator.getId());
    sendAfterCommit(() -> emails.sendAccountStatusEmail(target.getEmail(), target.getFullName(), AccountStatus.BLOQUEADO, target.getMotivoBloqueio()));
    return user(target.getId());
  }

  @Transactional
  public AdminDtos.UserDetails unblockUser(UUID administratorId, UUID userId) {
    User administrator = requireAdmin(administratorId), target = requireUserForUpdate(userId);
    if (target.getAccountStatus() != AccountStatus.BLOQUEADO)
      throw new BusinessException("Somente usuários bloqueados podem ser desbloqueados.", HttpStatus.CONFLICT);
    target.setAccountStatus(AccountStatus.ATIVO); target.setDesbloqueadoEm(Instant.now());
    target.setDesbloqueadoPorUsuarioId(administrator.getId()); target.setMotivoBloqueio(null);
    sendAfterCommit(() -> emails.sendAccountStatusEmail(target.getEmail(), target.getFullName(), AccountStatus.ATIVO, null));
    return user(target.getId());
  }

  @Transactional(readOnly = true)
  public AdminDtos.CaregiverPage caregivers(String query, CaregiverApprovalStatus status, int page, int size) {
    int safePage = Math.max(page, 0), safeSize = Math.min(Math.max(size, 1), 50);
    List<AdminDtos.CaregiverSummary> values = caregivers.findAll().stream()
      .filter(profile -> status == null || profile.getSituacaoAprovacao() == status)
      .filter(profile -> matches(profile.getUser(), query))
      .sorted(Comparator.comparing(CaregiverProfile::getCreatedAt).reversed())
      .map(this::caregiverSummary).toList();
    int from = Math.min(safePage * safeSize, values.size()), to = Math.min(from + safeSize, values.size());
    return new AdminDtos.CaregiverPage(values.subList(from, to), safePage, safeSize, values.size(), pages(values.size(), safeSize));
  }

  @Transactional(readOnly = true)
  public AdminDtos.CaregiverDetails caregiver(UUID id) { return caregiverDetails(requireCaregiver(id)); }

  @Transactional
  public AdminDtos.CaregiverDetails review(UUID administratorId, UUID caregiverId,
    CaregiverApprovalStatus newStatus, String reason) {
    User administrator = requireAdmin(administratorId);
    CaregiverProfile caregiver = requireCaregiverForUpdate(caregiverId);
    CaregiverApprovalStatus previous = caregiver.getSituacaoAprovacao();
    validateCaregiverTransition(previous, newStatus);
    String normalizedReason = newStatus == CaregiverApprovalStatus.APROVADO ? null : cleanReason(reason);
    caregiver.setSituacaoAprovacao(newStatus); caregiver.setAnalisadoEm(Instant.now());
    caregiver.setAnalisadoPorUsuarioId(administrator.getId());
    caregiver.setMotivoReprovacao(newStatus == CaregiverApprovalStatus.REPROVADO ? normalizedReason : null);
    caregiver.setMotivoBloqueioProfissional(newStatus == CaregiverApprovalStatus.BLOQUEADO ? normalizedReason : null);
    CaregiverStatusHistory history = new CaregiverStatusHistory(); history.setCaregiver(caregiver);
    history.setPreviousStatus(previous); history.setNewStatus(newStatus); history.setMotivo(normalizedReason);
    history.setAdministrator(administrator); histories.save(history);
    String caregiverEmail = caregiver.getUser().getEmail(), caregiverName = caregiver.getUser().getFullName();
    sendAfterCommit(() -> emails.sendCaregiverReviewEmail(caregiverEmail, caregiverName, newStatus, normalizedReason));
    return caregiverDetails(caregiver);
  }

  @Transactional(readOnly = true)
  public AdminDtos.ResponsiblePage responsibles(String query, ResponsibleApprovalStatus status, int page, int size) {
    int safePage = Math.max(page, 0), safeSize = Math.min(Math.max(size, 1), 50);
    List<AdminDtos.ResponsibleSummary> values = responsibles.findAll().stream()
      .filter(profile -> status == null || profile.getSituacaoAprovacao() == status)
      .filter(profile -> matches(profile.getUser(), query))
      .sorted(Comparator.comparing(ResponsibleProfile::getCreatedAt).reversed())
      .map(this::responsibleSummary).toList();
    int from = Math.min(safePage * safeSize, values.size()), to = Math.min(from + safeSize, values.size());
    return new AdminDtos.ResponsiblePage(values.subList(from, to), safePage, safeSize, values.size(), pages(values.size(), safeSize));
  }

  @Transactional(readOnly = true)
  public AdminDtos.ResponsibleDetails responsible(UUID id) { return responsibleDetails(requireResponsible(id)); }

  @Transactional
  public AdminDtos.ResponsibleDetails reviewResponsible(UUID administratorId, UUID responsibleId,
    ResponsibleApprovalStatus newStatus, String reason) {
    User administrator = requireAdmin(administratorId);
    ResponsibleProfile responsible = requireResponsibleForUpdate(responsibleId);
    ResponsibleApprovalStatus previous = responsible.getSituacaoAprovacao();
    validateResponsibleTransition(previous, newStatus);
    String normalizedReason = newStatus == ResponsibleApprovalStatus.APROVADO ? null : cleanReason(reason);
    responsible.setSituacaoAprovacao(newStatus); responsible.setAnalisadoEm(Instant.now());
    responsible.setAnalisadoPorUsuarioId(administrator.getId());
    responsible.setMotivoReprovacao(newStatus == ResponsibleApprovalStatus.REPROVADO ? normalizedReason : null);
    responsible.setMotivoBloqueio(newStatus == ResponsibleApprovalStatus.BLOQUEADO ? normalizedReason : null);
    ResponsibleStatusHistory history = new ResponsibleStatusHistory(); history.setResponsible(responsible);
    history.setPreviousStatus(previous); history.setNewStatus(newStatus); history.setMotivo(normalizedReason);
    history.setAdministrator(administrator); responsibleHistories.save(history);
    String responsibleEmail = responsible.getUser().getEmail(), responsibleName = responsible.getUser().getFullName();
    sendAfterCommit(() -> emails.sendResponsibleReviewEmail(responsibleEmail, responsibleName, newStatus, normalizedReason));
    return responsibleDetails(responsible);
  }

  private User requireAdmin(UUID id) {
    User user = requireUser(id);
    if (!user.isAdmin()) throw new BusinessException("Acesso permitido apenas para administradores.", HttpStatus.FORBIDDEN);
    return user;
  }
  private User requireUser(UUID id) { return users.findById(id).orElseThrow(() -> new BusinessException("Usuário não encontrado.", HttpStatus.NOT_FOUND)); }
  private User requireUserForUpdate(UUID id) { return users.findByIdForUpdate(id).orElseThrow(() -> new BusinessException("Usuário não encontrado.", HttpStatus.NOT_FOUND)); }
  private CaregiverProfile requireCaregiver(UUID id) { return caregivers.findById(id).orElseThrow(() -> new BusinessException("Cuidador não encontrado.", HttpStatus.NOT_FOUND)); }
  private CaregiverProfile requireCaregiverForUpdate(UUID id) { return caregivers.findByIdForUpdate(id).orElseThrow(() -> new BusinessException("Cuidador não encontrado.", HttpStatus.NOT_FOUND)); }
  private ResponsibleProfile requireResponsible(UUID id) { return responsibles.findById(id).orElseThrow(() -> new BusinessException("Responsável não encontrado.", HttpStatus.NOT_FOUND)); }
  private ResponsibleProfile requireResponsibleForUpdate(UUID id) { return responsibles.findByIdForUpdate(id).orElseThrow(() -> new BusinessException("Responsável não encontrado.", HttpStatus.NOT_FOUND)); }
  private void validateCaregiverTransition(CaregiverApprovalStatus previous, CaregiverApprovalStatus next) {
    if (next == CaregiverApprovalStatus.APROVADO && previous != CaregiverApprovalStatus.PENDENTE)
      throw new BusinessException("Apenas cuidadores pendentes podem ser aprovados.", HttpStatus.CONFLICT);
    if (next == CaregiverApprovalStatus.REPROVADO && previous != CaregiverApprovalStatus.PENDENTE)
      throw new BusinessException("Apenas cuidadores pendentes podem ser reprovados.", HttpStatus.CONFLICT);
    if (next == CaregiverApprovalStatus.BLOQUEADO && previous != CaregiverApprovalStatus.APROVADO)
      throw new BusinessException("Somente cuidadores aprovados podem ser bloqueados.", HttpStatus.CONFLICT);
    if (next == CaregiverApprovalStatus.PENDENTE)
      throw new BusinessException("Não é possível retornar o cuidador para pendente.", HttpStatus.CONFLICT);
  }
  private void validateResponsibleTransition(ResponsibleApprovalStatus previous, ResponsibleApprovalStatus next) {
    if (next == ResponsibleApprovalStatus.APROVADO && previous != ResponsibleApprovalStatus.PENDENTE)
      throw new BusinessException("Apenas responsáveis pendentes podem ser aprovados.", HttpStatus.CONFLICT);
    if (next == ResponsibleApprovalStatus.REPROVADO && previous != ResponsibleApprovalStatus.PENDENTE)
      throw new BusinessException("Apenas responsáveis pendentes podem ser reprovados.", HttpStatus.CONFLICT);
    if (next == ResponsibleApprovalStatus.BLOQUEADO && previous != ResponsibleApprovalStatus.APROVADO)
      throw new BusinessException("Somente responsáveis aprovados podem ser bloqueados.", HttpStatus.CONFLICT);
    if (next == ResponsibleApprovalStatus.PENDENTE)
      throw new BusinessException("Não é possível retornar o responsável para pendente.", HttpStatus.CONFLICT);
  }
  private AdminDtos.UserSummary userSummary(User user) {
    var approval = caregivers.findByUser(user).map(CaregiverProfile::getSituacaoAprovacao).orElse(null);
    var responsibleApproval = responsibles.findByUser(user).map(ResponsibleProfile::getSituacaoAprovacao).orElse(null);
    return new AdminDtos.UserSummary(user.getId(), user.getFullName(), user.getEmail(), user.getUserType(),
      profileLabel(user.getUserType()), user.getAccountStatus(), accountLabel(user.getAccountStatus()),
      approval, approval == null ? null : approvalLabel(approval), responsibleApproval,
      responsibleApproval == null ? null : approvalLabel(responsibleApproval), user.getCreatedAt());
  }
  private AdminDtos.CaregiverSummary caregiverSummary(CaregiverProfile profile) {
    AddressFields address = profile.getEnderecoAtendimento();
    return new AdminDtos.CaregiverSummary(profile.getId(), profile.getUser().getId(), profile.getUser().getFullName(),
      profile.getUser().getEmail(), address == null ? null : address.getCidade(), profile.getTempoExperiencia(),
      profile.getFormacoes(), profile.getSituacaoAprovacao(), approvalLabel(profile.getSituacaoAprovacao()), profile.getCreatedAt());
  }
  private AdminDtos.CaregiverDetails caregiverDetails(CaregiverProfile profile) {
    User user = profile.getUser(); AddressFields address = profile.getEnderecoAtendimento();
    List<AdminDtos.HistoryItem> history = histories.findByCaregiverOrderByCriadoEmDesc(profile).stream()
      .map(item -> new AdminDtos.HistoryItem(item.getPreviousStatus(), item.getNewStatus(), approvalLabel(item.getNewStatus()),
        item.getMotivo(), item.getAdministrator().getId(), item.getAdministrator().getFullName(), item.getCriadoEm())).toList();
    return new AdminDtos.CaregiverDetails(profile.getId(), user.getId(), user.getFullName(), user.getEmail(),
      formatCpf(user.getCpf()), user.getPhone(), user.getProfilePhotoUrl(), profile.getBiografia(), profile.getTempoExperiencia(),
      profile.getFormacoes(), profile.getFormacaoOutro(), profile.getModalidades(), profile.getModalidadeOutro(),
      profile.getServicosOferecidos(), profile.getServicoOutro(), address == null ? null : address.getCidade(),
      address == null ? null : address.getBairro(), address == null ? null : address.getEstado(),
      profile.getDisponibilidade().getDiasSemana().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()),
      profile.getDisponibilidade().getPeriodos().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()),
      profile.getDisponibilidade().getHorarioInicio(), profile.getDisponibilidade().getHorarioFim(),
      profile.getDisponibilidade().getObservacao(), profile.getSituacaoAprovacao(),
      approvalLabel(profile.getSituacaoAprovacao()), profile.getMotivoReprovacao(), profile.getMotivoBloqueioProfissional(),
      profile.getAnalisadoEm(), profile.getCreatedAt(), history);
  }
  private AdminDtos.ResponsibleSummary responsibleSummary(ResponsibleProfile profile) {
    return new AdminDtos.ResponsibleSummary(profile.getId(), profile.getUser().getId(), profile.getUser().getFullName(),
      profile.getUser().getEmail(), profile.getParentesco(), profile.getSituacaoAprovacao(),
      approvalLabel(profile.getSituacaoAprovacao()), profile.getCreatedAt());
  }
  private AdminDtos.ResponsibleDetails responsibleDetails(ResponsibleProfile profile) {
    User user = profile.getUser();
    List<AdminDtos.ResponsibleHistoryItem> history = responsibleHistories.findByResponsibleOrderByCriadoEmDesc(profile).stream()
      .map(item -> new AdminDtos.ResponsibleHistoryItem(item.getPreviousStatus(), item.getNewStatus(), approvalLabel(item.getNewStatus()),
        item.getMotivo(), item.getAdministrator().getId(), item.getAdministrator().getFullName(), item.getCriadoEm())).toList();
    return new AdminDtos.ResponsibleDetails(profile.getId(), user.getId(), user.getFullName(), user.getEmail(),
      formatCpf(user.getCpf()), user.getPhone(), profile.getParentesco(), profile.getParentescoOutro(),
      profile.getPreferenciaContato(), profile.getSituacaoAprovacao(), approvalLabel(profile.getSituacaoAprovacao()),
      profile.getMotivoReprovacao(), profile.getMotivoBloqueio(), profile.getAnalisadoEm(), profile.getCreatedAt(), history);
  }
  private boolean matches(User user, String query) {
    String q = normalize(query), digits = query == null ? "" : query.replaceAll("\\D", "");
    return q.isBlank() || normalize(user.getFullName()).contains(q) || normalize(user.getEmail()).contains(q)
      || (!digits.isBlank() && user.getCpf().contains(digits));
  }
  private String normalize(String value) { return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim(); }
  private String cleanReason(String value) { if (value == null || value.isBlank()) throw new BusinessException("Informe o motivo."); return value.trim(); }
  private void sendAfterCommit(Runnable action) {
    if (!TransactionSynchronizationManager.isActualTransactionActive() || !TransactionSynchronizationManager.isSynchronizationActive()) { action.run(); return; }
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override public void afterCommit() { action.run(); }
    });
  }
  private int pages(int total, int size) { return total == 0 ? 0 : (int) Math.ceil((double) total / size); }
  private UserType canonicalType(UserType value) { return value == UserType.FAMILY ? UserType.RESPONSAVEL : value == UserType.CAREGIVER ? UserType.CUIDADOR : value; }
  private String profileLabel(UserType value) { return switch (canonicalType(value)) { case ADMIN -> "Administrador"; case CUIDADOR -> "Cuidador"; default -> "Responsável"; }; }
  private String accountLabel(AccountStatus value) { return switch (value) { case ATIVO -> "Ativo"; case BLOQUEADO -> "Bloqueado"; case INATIVO -> "Inativo"; }; }
  private String approvalLabel(CaregiverApprovalStatus value) { return switch (value) { case PENDENTE -> "Pendente"; case APROVADO -> "Aprovado"; case REPROVADO -> "Reprovado"; case BLOQUEADO -> "Bloqueado"; }; }
  private String approvalLabel(ResponsibleApprovalStatus value) { return switch (value) { case PENDENTE -> "Pendente"; case APROVADO -> "Aprovado"; case REPROVADO -> "Reprovado"; case BLOQUEADO -> "Bloqueado"; }; }
  private String formatCpf(String cpf) { return cpf == null || cpf.length() != 11 ? cpf : cpf.substring(0,3)+"."+cpf.substring(3,6)+"."+cpf.substring(6,9)+"-"+cpf.substring(9); }
}
