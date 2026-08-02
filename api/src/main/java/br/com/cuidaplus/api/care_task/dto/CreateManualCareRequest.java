package br.com.cuidaplus.api.care_task.dto;

import br.com.cuidaplus.api.care_task.ManualCareType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public class CreateManualCareRequest {
  @NotNull(message = "Informe a contratação.") private UUID contractId;
  @NotNull(message = "Informe a pessoa assistida.") private UUID assistedPersonId;
  @NotNull(message = "Informe a data do cuidado.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate entryDate;
  @NotNull(message = "Informe o horário em que o cuidado ocorreu.") @DateTimeFormat(pattern = "HH:mm") private LocalTime occurredTime;
  @NotNull(message = "Informe o tipo do cuidado.") private ManualCareType careType;
  @NotBlank(message = "Informe o título do cuidado.") @Size(max = 180) private String title;
  @NotBlank(message = "Descreva o cuidado realizado ou observado.") @Size(max = 2000) private String description;
  @Size(max = 1000) private String notes;
  @NotBlank private String timezone;
  private boolean important;

  public UUID getContractId() { return contractId; } public void setContractId(UUID value) { contractId = value; }
  public UUID getAssistedPersonId() { return assistedPersonId; } public void setAssistedPersonId(UUID value) { assistedPersonId = value; }
  public LocalDate getEntryDate() { return entryDate; } public void setEntryDate(LocalDate value) { entryDate = value; }
  public LocalTime getOccurredTime() { return occurredTime; } public void setOccurredTime(LocalTime value) { occurredTime = value; }
  public ManualCareType getCareType() { return careType; } public void setCareType(ManualCareType value) { careType = value; }
  public String getTitle() { return title; } public void setTitle(String value) { title = value; }
  public String getDescription() { return description; } public void setDescription(String value) { description = value; }
  public String getNotes() { return notes; } public void setNotes(String value) { notes = value; }
  public String getTimezone() { return timezone; } public void setTimezone(String value) { timezone = value; }
  public boolean isImportant() { return important; } public void setImportant(boolean value) { important = value; }
}
