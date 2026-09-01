package br.com.cuidaplus.api.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
  name = "users",
  uniqueConstraints = {
    @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
    @UniqueConstraint(name = "uk_users_cpf", columnNames = "cpf")
  }
)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 140)
  private String fullName;

  @Column(nullable = false, length = 11)
  private String cpf;

  @Column(nullable = false, length = 180)
  private String email;

  @Column(nullable = false)
  private String passwordHash;

  @Column(nullable = false)
  private LocalDate birthDate;

  @Column(length = 20)
  private String phone;

  @Column(length = 500)
  private String profilePhotoUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserType userType;

  @Enumerated(EnumType.STRING)
  @Column(name = "situacao_conta", nullable = false, length = 30)
  private AccountStatus accountStatus = AccountStatus.ATIVO;

  @Column(length = 1000)
  private String motivoBloqueio;

  private Instant bloqueadoEm;

  private UUID bloqueadoPorUsuarioId;

  private Instant desbloqueadoEm;

  private UUID desbloqueadoPorUsuarioId;

  private Instant ultimoLoginEm;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getCpf() {
    return cpf;
  }

  public void setCpf(String cpf) {
    this.cpf = cpf;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getProfilePhotoUrl() {
    return profilePhotoUrl;
  }

  public void setProfilePhotoUrl(String profilePhotoUrl) {
    this.profilePhotoUrl = profilePhotoUrl;
  }

  public UserType getUserType() {
    return userType;
  }

  public void setUserType(UserType userType) {
    this.userType = userType;
  }

  public AccountStatus getAccountStatus() {
    return accountStatus;
  }

  public void setAccountStatus(AccountStatus accountStatus) {
    this.accountStatus = accountStatus;
  }

  public String getMotivoBloqueio() { return motivoBloqueio; }
  public void setMotivoBloqueio(String value) { motivoBloqueio = value; }
  public Instant getBloqueadoEm() { return bloqueadoEm; }
  public void setBloqueadoEm(Instant value) { bloqueadoEm = value; }
  public UUID getBloqueadoPorUsuarioId() { return bloqueadoPorUsuarioId; }
  public void setBloqueadoPorUsuarioId(UUID value) { bloqueadoPorUsuarioId = value; }
  public Instant getDesbloqueadoEm() { return desbloqueadoEm; }
  public void setDesbloqueadoEm(Instant value) { desbloqueadoEm = value; }
  public UUID getDesbloqueadoPorUsuarioId() { return desbloqueadoPorUsuarioId; }
  public void setDesbloqueadoPorUsuarioId(UUID value) { desbloqueadoPorUsuarioId = value; }
  public Instant getUltimoLoginEm() { return ultimoLoginEm; }
  public void setUltimoLoginEm(Instant value) { ultimoLoginEm = value; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public boolean isActive() { return accountStatus == AccountStatus.ATIVO; }
  public boolean isAdmin() { return userType == UserType.ADMIN; }
  public boolean isCaregiver() {
    return userType == UserType.CUIDADOR || userType == UserType.CAREGIVER;
  }
  public boolean isResponsible() {
    return userType == UserType.RESPONSAVEL || userType == UserType.FAMILY;
  }
}
