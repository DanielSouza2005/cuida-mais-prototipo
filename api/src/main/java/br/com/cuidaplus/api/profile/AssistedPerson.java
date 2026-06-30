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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "assisted_persons")
public class AssistedPerson {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "responsible_user_id", nullable = false)
  private User responsibleUser;

  @Column(nullable = false, length = 140)
  private String nome;

  @Column(length = 11)
  private String cpf;

  @Column(nullable = false)
  private LocalDate dataNascimento;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private GrauDependencia grauDependencia;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private Mobilidade mobilidade;

  @Column(length = 120)
  private String mobilidadeOutro;

  @ElementCollection
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "assisted_person_allergies", joinColumns = @JoinColumn(name = "assisted_person_id"))
  @Column(name = "alergia", length = 40)
  private Set<Alergia> alergias = new LinkedHashSet<>();

  @Column(length = 180)
  private String alergiasOutro;

  @Column(length = 500)
  private String alergiasDetalhes;

  @ElementCollection
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "assisted_person_food_restrictions", joinColumns = @JoinColumn(name = "assisted_person_id"))
  @Column(name = "restricao", length = 40)
  private Set<RestricaoAlimentar> restricoesAlimentares = new LinkedHashSet<>();

  @Column(length = 180)
  private String restricoesAlimentaresOutro;

  @Column(length = 500)
  private String restricoesAlimentaresDetalhes;

  @Column(length = 500)
  private String medicamentos;

  @Column(length = 500)
  private String observacoes;

  @Embedded
  private AddressFields enderecoCuidado = new AddressFields();

  @OneToOne(mappedBy = "assistedPerson", fetch = FetchType.LAZY)
  private EmergencyContact emergencyContact;

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

  public User getResponsibleUser() {
    return responsibleUser;
  }

  public void setResponsibleUser(User responsibleUser) {
    this.responsibleUser = responsibleUser;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getCpf() {
    return cpf;
  }

  public void setCpf(String cpf) {
    this.cpf = cpf;
  }

  public LocalDate getDataNascimento() {
    return dataNascimento;
  }

  public void setDataNascimento(LocalDate dataNascimento) {
    this.dataNascimento = dataNascimento;
  }

  public GrauDependencia getGrauDependencia() {
    return grauDependencia;
  }

  public void setGrauDependencia(GrauDependencia grauDependencia) {
    this.grauDependencia = grauDependencia;
  }

  public Mobilidade getMobilidade() {
    return mobilidade;
  }

  public void setMobilidade(Mobilidade mobilidade) {
    this.mobilidade = mobilidade;
  }

  public String getMobilidadeOutro() {
    return mobilidadeOutro;
  }

  public void setMobilidadeOutro(String mobilidadeOutro) {
    this.mobilidadeOutro = mobilidadeOutro;
  }

  public Set<Alergia> getAlergias() {
    return alergias;
  }

  public void setAlergias(Set<Alergia> alergias) {
    this.alergias = alergias;
  }

  public String getAlergiasOutro() {
    return alergiasOutro;
  }

  public void setAlergiasOutro(String alergiasOutro) {
    this.alergiasOutro = alergiasOutro;
  }

  public String getAlergiasDetalhes() {
    return alergiasDetalhes;
  }

  public void setAlergiasDetalhes(String alergiasDetalhes) {
    this.alergiasDetalhes = alergiasDetalhes;
  }

  public Set<RestricaoAlimentar> getRestricoesAlimentares() {
    return restricoesAlimentares;
  }

  public void setRestricoesAlimentares(Set<RestricaoAlimentar> restricoesAlimentares) {
    this.restricoesAlimentares = restricoesAlimentares;
  }

  public String getRestricoesAlimentaresOutro() {
    return restricoesAlimentaresOutro;
  }

  public void setRestricoesAlimentaresOutro(String restricoesAlimentaresOutro) {
    this.restricoesAlimentaresOutro = restricoesAlimentaresOutro;
  }

  public String getRestricoesAlimentaresDetalhes() {
    return restricoesAlimentaresDetalhes;
  }

  public void setRestricoesAlimentaresDetalhes(String restricoesAlimentaresDetalhes) {
    this.restricoesAlimentaresDetalhes = restricoesAlimentaresDetalhes;
  }

  public String getMedicamentos() {
    return medicamentos;
  }

  public void setMedicamentos(String medicamentos) {
    this.medicamentos = medicamentos;
  }

  public String getObservacoes() {
    return observacoes;
  }

  public void setObservacoes(String observacoes) {
    this.observacoes = observacoes;
  }

  public AddressFields getEnderecoCuidado() {
    return enderecoCuidado;
  }

  public void setEnderecoCuidado(AddressFields enderecoCuidado) {
    this.enderecoCuidado = enderecoCuidado;
  }
}
