package br.com.cuidaplus.api.care_task;

import jakarta.persistence.*;

@Embeddable
public class MedicationDetails {
  @Column(name = "medication_name", length = 180) private String name;
  @Column(name = "medication_dosage", length = 80) private String dosage;
  @Enumerated(EnumType.STRING) @Column(name = "medication_unit", length = 30) private MedicationUnit unit;
  @Column(name = "medication_custom_unit", length = 80) private String customUnit;
  @Enumerated(EnumType.STRING) @Column(name = "medication_administration_route", length = 30) private MedicationAdministrationRoute administrationRoute;
  @Column(name = "medication_custom_route", length = 120) private String customAdministrationRoute;
  @Column(name = "medication_instructions", length = 1000) private String additionalInstructions;

  public String getName() { return name; }
  public void setName(String value) { name = value; }
  public String getDosage() { return dosage; }
  public void setDosage(String value) { dosage = value; }
  public MedicationUnit getUnit() { return unit; }
  public void setUnit(MedicationUnit value) { unit = value; }
  public String getCustomUnit() { return customUnit; }
  public void setCustomUnit(String value) { customUnit = value; }
  public MedicationAdministrationRoute getAdministrationRoute() { return administrationRoute; }
  public void setAdministrationRoute(MedicationAdministrationRoute value) { administrationRoute = value; }
  public String getCustomAdministrationRoute() { return customAdministrationRoute; }
  public void setCustomAdministrationRoute(String value) { customAdministrationRoute = value; }
  public String getAdditionalInstructions() { return additionalInstructions; }
  public void setAdditionalInstructions(String value) { additionalInstructions = value; }
}
