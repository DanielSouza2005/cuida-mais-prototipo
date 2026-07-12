package br.com.cuidaplus.api.service_request.dto;
import br.com.cuidaplus.api.profile.*;
import br.com.cuidaplus.api.service_request.HiringType;
import java.time.LocalDate;
import java.util.*;
public record ServiceRequestFormDataResponse(
  Caregiver caregiver,
  List<Assisted> assistedPersons,
  List<CareAddress> careAddresses,
  Set<ServicoOferecido> activityOptions,
  Set<DiaSemana> weekdayOptions,
  Set<HiringType> hiringTypeOptions
) {
  public record Caregiver(UUID id, String name, String profilePhotoUrl, String city, String neighborhood, String state, TempoExperiencia experienceRange, Set<ServicoOferecido> servicesOffered) {}
  public record Assisted(UUID id, String name, LocalDate birthDate, GrauDependencia dependencyLevel, Mobilidade mobility, String summary) {}
  public record CareAddress(UUID id, UUID assistedPersonId, String cep, String street, String number, String complement, String neighborhood, String city, String state, String referencePoint) {}
}
