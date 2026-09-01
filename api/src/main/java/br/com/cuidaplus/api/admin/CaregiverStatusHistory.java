package br.com.cuidaplus.api.admin;

import br.com.cuidaplus.api.profile.CaregiverApprovalStatus;
import br.com.cuidaplus.api.profile.CaregiverProfile;
import br.com.cuidaplus.api.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cuidador_historico_situacao")
public class CaregiverStatusHistory {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cuidador_id") private CaregiverProfile caregiver;
  @Enumerated(EnumType.STRING) @Column(name = "situacao_anterior", length = 30) private CaregiverApprovalStatus previousStatus;
  @Enumerated(EnumType.STRING) @Column(name = "situacao_nova", nullable = false, length = 30) private CaregiverApprovalStatus newStatus;
  @Column(length = 1000) private String motivo;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "usuario_administrador_id") private User administrator;
  @Column(nullable = false, updatable = false) private Instant criadoEm;

  @PrePersist void prePersist() { if (criadoEm == null) criadoEm = Instant.now(); }
  public void setCaregiver(CaregiverProfile value) { caregiver = value; }
  public CaregiverApprovalStatus getPreviousStatus() { return previousStatus; }
  public void setPreviousStatus(CaregiverApprovalStatus value) { previousStatus = value; }
  public CaregiverApprovalStatus getNewStatus() { return newStatus; }
  public void setNewStatus(CaregiverApprovalStatus value) { newStatus = value; }
  public String getMotivo() { return motivo; }
  public void setMotivo(String value) { motivo = value; }
  public User getAdministrator() { return administrator; }
  public void setAdministrator(User value) { administrator = value; }
  public Instant getCriadoEm() { return criadoEm; }
}
