package br.com.cuidaplus.api.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import java.util.UUID;

@Entity
@Table(name = "emergency_contacts")
public class EmergencyContact {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "assisted_person_id", nullable = false, unique = true)
  private AssistedPerson assistedPerson;

  @Column(nullable = false, length = 140)
  private String nome;

  @Column(nullable = false, length = 20)
  private String telefone;

  @Column(nullable = false, length = 120)
  private String vinculo;

  @Column(nullable = false)
  private boolean responsibleContact;

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

  public AssistedPerson getAssistedPerson() {
    return assistedPerson;
  }

  public void setAssistedPerson(AssistedPerson assistedPerson) {
    this.assistedPerson = assistedPerson;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getTelefone() {
    return telefone;
  }

  public void setTelefone(String telefone) {
    this.telefone = telefone;
  }

  public String getVinculo() {
    return vinculo;
  }

  public void setVinculo(String vinculo) {
    this.vinculo = vinculo;
  }

  public boolean isResponsibleContact() {
    return responsibleContact;
  }

  public void setResponsibleContact(boolean responsibleContact) {
    this.responsibleContact = responsibleContact;
  }
}
