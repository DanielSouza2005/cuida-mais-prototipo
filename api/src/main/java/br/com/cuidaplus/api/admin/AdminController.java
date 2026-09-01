package br.com.cuidaplus.api.admin;

import br.com.cuidaplus.api.profile.CaregiverApprovalStatus;
import br.com.cuidaplus.api.profile.ResponsibleApprovalStatus;
import br.com.cuidaplus.api.security.AuthenticatedUser;
import br.com.cuidaplus.api.user.AccountStatus;
import br.com.cuidaplus.api.user.UserType;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping("/api/admin")
public class AdminController {
  private final AdminService service;
  public AdminController(AdminService service) { this.service = service; }
  @GetMapping("/dashboard") public AdminDtos.DashboardSummary dashboard() { return service.dashboard(); }
  @GetMapping("/users") public AdminDtos.UserPage users(@RequestParam(required=false) String query,
    @RequestParam(required=false) UserType type, @RequestParam(required=false) AccountStatus status,
    @RequestParam(required=false) CaregiverApprovalStatus caregiverStatus,
    @RequestParam(required=false) ResponsibleApprovalStatus responsibleStatus, @RequestParam(defaultValue="0") int page,
    @RequestParam(defaultValue="20") int size) { return service.users(query,type,status,caregiverStatus,responsibleStatus,page,size); }
  @GetMapping("/users/{id}") public AdminDtos.UserDetails user(@PathVariable UUID id) { return service.user(id); }
  @PatchMapping("/users/{id}/block") public AdminDtos.UserDetails block(@PathVariable UUID id,
    @Valid @RequestBody AdminDtos.ReasonRequest request) { return service.blockUser(AuthenticatedUser.id(),id,request.reason()); }
  @PatchMapping("/users/{id}/unblock") public AdminDtos.UserDetails unblock(@PathVariable UUID id) { return service.unblockUser(AuthenticatedUser.id(),id); }
  @GetMapping("/caregivers") public AdminDtos.CaregiverPage caregivers(@RequestParam(required=false) String query,
    @RequestParam(required=false) CaregiverApprovalStatus status, @RequestParam(defaultValue="0") int page,
    @RequestParam(defaultValue="20") int size) { return service.caregivers(query,status,page,size); }
  @GetMapping("/caregivers/pending") public AdminDtos.CaregiverPage pending(@RequestParam(required=false) String query,
    @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) { return service.caregivers(query,CaregiverApprovalStatus.PENDENTE,page,size); }
  @GetMapping("/caregivers/{id}") public AdminDtos.CaregiverDetails caregiver(@PathVariable UUID id) { return service.caregiver(id); }
  @PatchMapping("/caregivers/{id}/approve") public AdminDtos.CaregiverDetails approve(@PathVariable UUID id) { return service.review(AuthenticatedUser.id(),id,CaregiverApprovalStatus.APROVADO,null); }
  @PatchMapping("/caregivers/{id}/reject") public AdminDtos.CaregiverDetails reject(@PathVariable UUID id,@Valid @RequestBody AdminDtos.ReasonRequest request) { return service.review(AuthenticatedUser.id(),id,CaregiverApprovalStatus.REPROVADO,request.reason()); }
  @PatchMapping("/caregivers/{id}/block") public AdminDtos.CaregiverDetails blockCaregiver(@PathVariable UUID id,@Valid @RequestBody AdminDtos.ReasonRequest request) { return service.review(AuthenticatedUser.id(),id,CaregiverApprovalStatus.BLOQUEADO,request.reason()); }
  @GetMapping("/responsibles") public AdminDtos.ResponsiblePage responsibles(@RequestParam(required=false) String query,
    @RequestParam(required=false) ResponsibleApprovalStatus status, @RequestParam(defaultValue="0") int page,
    @RequestParam(defaultValue="20") int size) { return service.responsibles(query,status,page,size); }
  @GetMapping("/responsibles/pending") public AdminDtos.ResponsiblePage pendingResponsibles(@RequestParam(required=false) String query,
    @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) { return service.responsibles(query,ResponsibleApprovalStatus.PENDENTE,page,size); }
  @GetMapping("/responsibles/{id}") public AdminDtos.ResponsibleDetails responsible(@PathVariable UUID id) { return service.responsible(id); }
  @PatchMapping("/responsibles/{id}/approve") public AdminDtos.ResponsibleDetails approveResponsible(@PathVariable UUID id) { return service.reviewResponsible(AuthenticatedUser.id(),id,ResponsibleApprovalStatus.APROVADO,null); }
  @PatchMapping("/responsibles/{id}/reject") public AdminDtos.ResponsibleDetails rejectResponsible(@PathVariable UUID id,@Valid @RequestBody AdminDtos.ReasonRequest request) { return service.reviewResponsible(AuthenticatedUser.id(),id,ResponsibleApprovalStatus.REPROVADO,request.reason()); }
  @PatchMapping("/responsibles/{id}/block") public AdminDtos.ResponsibleDetails blockResponsible(@PathVariable UUID id,@Valid @RequestBody AdminDtos.ReasonRequest request) { return service.reviewResponsible(AuthenticatedUser.id(),id,ResponsibleApprovalStatus.BLOQUEADO,request.reason()); }
}
