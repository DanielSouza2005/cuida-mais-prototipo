package br.com.cuidaplus.api.profile;

import br.com.cuidaplus.api.user.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "caregiver_profiles")
public class CaregiverProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(length = 40)
  private FormacaoCuidador formacao;

  @Column(length = 180)
  private String formacaoOutro;

  @Column(length = 500)
  private String experiencia;

  @Column(length = 500)
  private String biografia;

  @Embedded
  private AddressFields enderecoAtendimento = new AddressFields();

  @ElementCollection
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "caregiver_modalities", joinColumns = @JoinColumn(name = "caregiver_profile_id"))
  @Column(name = "modalidade", length = 40)
  private Set<ModalidadeAtendimento> modalidades = new LinkedHashSet<>();

  @Column(length = 180)
  private String modalidadeOutro;

  @ElementCollection
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "caregiver_services", joinColumns = @JoinColumn(name = "caregiver_profile_id"))
  @Column(name = "servico", length = 50)
  private Set<ServicoOferecido> servicosOferecidos = new LinkedHashSet<>();

  @Column(length = 180)
  private String servicoOutro;

  @Embedded
  private CaregiverAvailability disponibilidade = new CaregiverAvailability();

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

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public FormacaoCuidador getFormacao() {
    return formacao;
  }

  public void setFormacao(FormacaoCuidador formacao) {
    this.formacao = formacao;
  }

  public String getFormacaoOutro() {
    return formacaoOutro;
  }

  public void setFormacaoOutro(String formacaoOutro) {
    this.formacaoOutro = formacaoOutro;
  }

  public String getExperiencia() {
    return experiencia;
  }

  public void setExperiencia(String experiencia) {
    this.experiencia = experiencia;
  }

  public String getBiografia() {
    return biografia;
  }

  public void setBiografia(String biografia) {
    this.biografia = biografia;
  }

  public AddressFields getEnderecoAtendimento() {
    return enderecoAtendimento;
  }

  public void setEnderecoAtendimento(AddressFields enderecoAtendimento) {
    this.enderecoAtendimento = enderecoAtendimento;
  }

  public Set<ModalidadeAtendimento> getModalidades() {
    return modalidades;
  }

  public void setModalidades(Set<ModalidadeAtendimento> modalidades) {
    this.modalidades = modalidades;
  }

  public String getModalidadeOutro() {
    return modalidadeOutro;
  }

  public void setModalidadeOutro(String modalidadeOutro) {
    this.modalidadeOutro = modalidadeOutro;
  }

  public Set<ServicoOferecido> getServicosOferecidos() {
    return servicosOferecidos;
  }

  public void setServicosOferecidos(Set<ServicoOferecido> servicosOferecidos) {
    this.servicosOferecidos = servicosOferecidos;
  }

  public String getServicoOutro() {
    return servicoOutro;
  }

  public void setServicoOutro(String servicoOutro) {
    this.servicoOutro = servicoOutro;
  }

  public CaregiverAvailability getDisponibilidade() {
    return disponibilidade;
  }

  public void setDisponibilidade(CaregiverAvailability disponibilidade) {
    this.disponibilidade = disponibilidade;
  }
}
