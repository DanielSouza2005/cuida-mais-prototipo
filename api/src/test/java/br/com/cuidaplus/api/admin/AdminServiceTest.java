package br.com.cuidaplus.api.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.email.EmailService;
import br.com.cuidaplus.api.profile.CaregiverApprovalStatus;
import br.com.cuidaplus.api.profile.CaregiverProfile;
import br.com.cuidaplus.api.profile.CaregiverProfileRepository;
import br.com.cuidaplus.api.profile.Parentesco;
import br.com.cuidaplus.api.profile.PreferenciaContato;
import br.com.cuidaplus.api.profile.ResponsibleApprovalStatus;
import br.com.cuidaplus.api.profile.ResponsibleProfile;
import br.com.cuidaplus.api.profile.ResponsibleProfileRepository;
import br.com.cuidaplus.api.user.AccountStatus;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserRepository;
import br.com.cuidaplus.api.user.UserType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
  @Mock UserRepository users; @Mock CaregiverProfileRepository caregivers;
  @Mock CaregiverStatusHistoryRepository histories; @Mock ResponsibleProfileRepository responsibles;
  @Mock ResponsibleStatusHistoryRepository responsibleHistories; @Mock EmailService emails;
  AdminService service;
  @BeforeEach void setup(){service=new AdminService(users,caregivers,histories,responsibles,responsibleHistories,emails);}

  @Test void administratorCannotBlockOwnAccount(){
    User admin=user(UserType.ADMIN); when(users.findById(admin.getId())).thenReturn(Optional.of(admin));
    when(users.findByIdForUpdate(admin.getId())).thenReturn(Optional.of(admin));
    assertThatThrownBy(()->service.blockUser(admin.getId(),admin.getId(),"Teste"))
      .isInstanceOf(BusinessException.class).hasMessageContaining("própria conta");
  }

  @Test void approvalIsAuditedAndEmailUsesRegisteredAddress(){
    User admin=user(UserType.ADMIN), caregiverUser=user(UserType.CUIDADOR);
    CaregiverProfile caregiver=new CaregiverProfile(); ReflectionTestUtils.setField(caregiver,"id",UUID.randomUUID());
    caregiver.setUser(caregiverUser); caregiver.setSituacaoAprovacao(CaregiverApprovalStatus.PENDENTE);
    when(users.findById(admin.getId())).thenReturn(Optional.of(admin)); when(caregivers.findByIdForUpdate(caregiver.getId())).thenReturn(Optional.of(caregiver));
    when(histories.findByCaregiverOrderByCriadoEmDesc(caregiver)).thenReturn(List.of()); when(histories.save(any())).thenAnswer(call->call.getArgument(0));
    var result=service.review(admin.getId(),caregiver.getId(),CaregiverApprovalStatus.APROVADO,null);
    assertThat(result.status()).isEqualTo(CaregiverApprovalStatus.APROVADO);
    verify(histories).save(any(CaregiverStatusHistory.class));
    verify(emails).sendCaregiverReviewEmail(caregiverUser.getEmail(),caregiverUser.getFullName(),CaregiverApprovalStatus.APROVADO,null);
  }

  @Test void responsibleApprovalIsAuditedAndEmailUsesRegisteredAddress(){
    User admin=user(UserType.ADMIN), responsibleUser=user(UserType.RESPONSAVEL);
    ResponsibleProfile responsible=new ResponsibleProfile(); ReflectionTestUtils.setField(responsible,"id",UUID.randomUUID());
    responsible.setUser(responsibleUser); responsible.setParentesco(Parentesco.FILHO);
    responsible.setPreferenciaContato(PreferenciaContato.WHATSAPP);
    responsible.setSituacaoAprovacao(ResponsibleApprovalStatus.PENDENTE);
    when(users.findById(admin.getId())).thenReturn(Optional.of(admin));
    when(responsibles.findByIdForUpdate(responsible.getId())).thenReturn(Optional.of(responsible));
    when(responsibleHistories.findByResponsibleOrderByCriadoEmDesc(responsible)).thenReturn(List.of());
    when(responsibleHistories.save(any())).thenAnswer(call->call.getArgument(0));
    var result=service.reviewResponsible(admin.getId(),responsible.getId(),ResponsibleApprovalStatus.APROVADO,null);
    assertThat(result.status()).isEqualTo(ResponsibleApprovalStatus.APROVADO);
    verify(responsibleHistories).save(any(ResponsibleStatusHistory.class));
    verify(emails).sendResponsibleReviewEmail(responsibleUser.getEmail(),responsibleUser.getFullName(),ResponsibleApprovalStatus.APROVADO,null);
  }

  @Test void approvedCaregiverCannotBeApprovedAgainAndProducesNoSideEffects(){
    User admin=user(UserType.ADMIN), caregiverUser=user(UserType.CUIDADOR);
    CaregiverProfile caregiver=new CaregiverProfile(); ReflectionTestUtils.setField(caregiver,"id",UUID.randomUUID());
    caregiver.setUser(caregiverUser); caregiver.setSituacaoAprovacao(CaregiverApprovalStatus.APROVADO);
    when(users.findById(admin.getId())).thenReturn(Optional.of(admin));
    when(caregivers.findByIdForUpdate(caregiver.getId())).thenReturn(Optional.of(caregiver));
    assertThatThrownBy(()->service.review(admin.getId(),caregiver.getId(),CaregiverApprovalStatus.APROVADO,null))
      .isInstanceOf(BusinessException.class).hasMessageContaining("pendentes");
    verify(histories,never()).save(any());
    verify(emails,never()).sendCaregiverReviewEmail(any(),any(),any(),any());
  }

  @Test void rejectedCaregiverCannotBeRejectedAgainAndProducesNoSideEffects(){
    User admin=user(UserType.ADMIN), caregiverUser=user(UserType.CUIDADOR);
    CaregiverProfile caregiver=new CaregiverProfile(); ReflectionTestUtils.setField(caregiver,"id",UUID.randomUUID());
    caregiver.setUser(caregiverUser); caregiver.setSituacaoAprovacao(CaregiverApprovalStatus.REPROVADO);
    when(users.findById(admin.getId())).thenReturn(Optional.of(admin));
    when(caregivers.findByIdForUpdate(caregiver.getId())).thenReturn(Optional.of(caregiver));
    assertThatThrownBy(()->service.review(admin.getId(),caregiver.getId(),CaregiverApprovalStatus.REPROVADO,"Novo motivo"))
      .isInstanceOf(BusinessException.class).hasMessageContaining("pendentes");
    verify(histories,never()).save(any());
    verify(emails,never()).sendCaregiverReviewEmail(any(),any(),any(),any());
  }

  @Test void pendingCaregiverCannotBeBlockedAndProducesNoSideEffects(){
    User admin=user(UserType.ADMIN), caregiverUser=user(UserType.CUIDADOR);
    CaregiverProfile caregiver=new CaregiverProfile(); ReflectionTestUtils.setField(caregiver,"id",UUID.randomUUID());
    caregiver.setUser(caregiverUser); caregiver.setSituacaoAprovacao(CaregiverApprovalStatus.PENDENTE);
    when(users.findById(admin.getId())).thenReturn(Optional.of(admin));
    when(caregivers.findByIdForUpdate(caregiver.getId())).thenReturn(Optional.of(caregiver));
    assertThatThrownBy(()->service.review(admin.getId(),caregiver.getId(),CaregiverApprovalStatus.BLOQUEADO,"Teste"))
      .isInstanceOf(BusinessException.class).hasMessageContaining("aprovados");
    verify(histories,never()).save(any());
    verify(emails,never()).sendCaregiverReviewEmail(any(),any(),any(),any());
  }

  @Test void approvedCaregiverCanBeBlockedOnceAndNotifiesAfterTheChange(){
    User admin=user(UserType.ADMIN), caregiverUser=user(UserType.CUIDADOR);
    CaregiverProfile caregiver=new CaregiverProfile(); ReflectionTestUtils.setField(caregiver,"id",UUID.randomUUID());
    caregiver.setUser(caregiverUser); caregiver.setSituacaoAprovacao(CaregiverApprovalStatus.APROVADO);
    when(users.findById(admin.getId())).thenReturn(Optional.of(admin));
    when(caregivers.findByIdForUpdate(caregiver.getId())).thenReturn(Optional.of(caregiver));
    when(histories.findByCaregiverOrderByCriadoEmDesc(caregiver)).thenReturn(List.of());
    when(histories.save(any())).thenAnswer(call->call.getArgument(0));
    var result=service.review(admin.getId(),caregiver.getId(),CaregiverApprovalStatus.BLOQUEADO,"Conduta incompatível");
    assertThat(result.status()).isEqualTo(CaregiverApprovalStatus.BLOQUEADO);
    verify(histories).save(any(CaregiverStatusHistory.class));
    verify(emails).sendCaregiverReviewEmail(caregiverUser.getEmail(),caregiverUser.getFullName(),CaregiverApprovalStatus.BLOQUEADO,"Conduta incompatível");
  }

  @Test void pendingResponsibleCannotBeBlockedAndProducesNoSideEffects(){
    User admin=user(UserType.ADMIN), responsibleUser=user(UserType.RESPONSAVEL);
    ResponsibleProfile responsible=new ResponsibleProfile(); ReflectionTestUtils.setField(responsible,"id",UUID.randomUUID());
    responsible.setUser(responsibleUser); responsible.setSituacaoAprovacao(ResponsibleApprovalStatus.PENDENTE);
    when(users.findById(admin.getId())).thenReturn(Optional.of(admin));
    when(responsibles.findByIdForUpdate(responsible.getId())).thenReturn(Optional.of(responsible));
    assertThatThrownBy(()->service.reviewResponsible(admin.getId(),responsible.getId(),ResponsibleApprovalStatus.BLOQUEADO,"Teste"))
      .isInstanceOf(BusinessException.class).hasMessageContaining("aprovados");
    verify(responsibleHistories,never()).save(any());
    verify(emails,never()).sendResponsibleReviewEmail(any(),any(),any(),any());
  }

  @Test void blockedAccountCannotBeBlockedAgain(){
    User admin=user(UserType.ADMIN), target=user(UserType.RESPONSAVEL); target.setAccountStatus(AccountStatus.BLOQUEADO);
    when(users.findById(admin.getId())).thenReturn(Optional.of(admin));
    when(users.findByIdForUpdate(target.getId())).thenReturn(Optional.of(target));
    assertThatThrownBy(()->service.blockUser(admin.getId(),target.getId(),"Teste"))
      .isInstanceOf(BusinessException.class).hasMessageContaining("ativos");
    verify(emails,never()).sendAccountStatusEmail(any(),any(),any(),any());
  }

  @Test void activeAccountCannotBeUnblocked(){
    User admin=user(UserType.ADMIN), target=user(UserType.RESPONSAVEL);
    when(users.findById(admin.getId())).thenReturn(Optional.of(admin));
    when(users.findByIdForUpdate(target.getId())).thenReturn(Optional.of(target));
    assertThatThrownBy(()->service.unblockUser(admin.getId(),target.getId()))
      .isInstanceOf(BusinessException.class).hasMessageContaining("bloqueados");
  }

  private User user(UserType type){User user=new User();ReflectionTestUtils.setField(user,"id",UUID.randomUUID());user.setUserType(type);user.setFullName("Pessoa Teste");user.setEmail(type.name().toLowerCase()+"@example.com");user.setCpf("12345678901");user.setAccountStatus(AccountStatus.ATIVO);return user;}
}
