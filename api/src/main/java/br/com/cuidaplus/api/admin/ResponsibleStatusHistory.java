package br.com.cuidaplus.api.admin;

import br.com.cuidaplus.api.profile.ResponsibleApprovalStatus;
import br.com.cuidaplus.api.profile.ResponsibleProfile;
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
@Table(name = "responsavel_historico_situacao")
public class ResponsibleStatusHistory {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "responsavel_id") private ResponsibleProfile responsible;
  @Enumerated(EnumType.STRING) @Column(name = "situacao_anterior", length = 30) private ResponsibleApprovalStatus previousStatus;
  @Enumerated(EnumType.STRING) @Column(name = "situacao_nova", nullable = false, length = 30) private ResponsibleApprovalStatus newStatus;
  @Column(length = 1000) private String motivo;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "usuario_administrador_id") private User administrator;
  @Column(nullable = false, updatable = false) private Instant criadoEm;

  @PrePersist void prePersist() { if (criadoEm == null) criadoEm = Instant.now(); }
  public void setResponsible(ResponsibleProfile value) { responsible = value; }
  public ResponsibleApprovalStatus getPreviousStatus() { return previousStatus; }
  public void setPreviousStatus(ResponsibleApprovalStatus value) { previousStatus = value; }
  public ResponsibleApprovalStatus getNewStatus() { return newStatus; }
  public void setNewStatus(ResponsibleApprovalStatus value) { newStatus = value; }
  public String getMotivo() { return motivo; }
  public void setMotivo(String value) { motivo = value; }
  public User getAdministrator() { return administrator; }
  public void setAdministrator(User value) { administrator = value; }
  public Instant getCriadoEm() { return criadoEm; }
}
