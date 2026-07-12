package br.com.cuidaplus.api.service_request;

import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.profile.*;
import br.com.cuidaplus.api.service_request.dto.*;
import br.com.cuidaplus.api.user.*;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceRequestService {
  private final ServiceRequestRepository repository;
  private final UserService userService;
  private final CaregiverProfileRepository caregiverRepository;
  private final AssistedPersonRepository assistedRepository;
  public ServiceRequestService(ServiceRequestRepository repository, UserService userService, CaregiverProfileRepository caregiverRepository, AssistedPersonRepository assistedRepository) {
    this.repository=repository; this.userService=userService; this.caregiverRepository=caregiverRepository; this.assistedRepository=assistedRepository;
  }

  @Transactional(readOnly = true)
  public ServiceRequestFormDataResponse formData(UUID responsibleId, UUID caregiverId) {
    User responsible = requireResponsible(responsibleId);
    CaregiverProfile caregiver = requireCaregiver(caregiverId);
    List<AssistedPerson> people = assistedRepository.findByResponsibleUser(responsible);
    if (people.isEmpty()) throw new BusinessException("Cadastre uma pessoa assistida antes de solicitar um serviço.", HttpStatus.BAD_REQUEST);
    List<ServiceRequestFormDataResponse.CareAddress> addresses = people.stream().filter(p -> hasAddress(p.getEnderecoCuidado())).map(this::toAddress).toList();
    if (addresses.isEmpty()) throw new BusinessException("Cadastre um endereço do cuidado antes de solicitar um serviço.", HttpStatus.BAD_REQUEST);
    AddressFields ca = caregiver.getEnderecoAtendimento();
    return new ServiceRequestFormDataResponse(
      new ServiceRequestFormDataResponse.Caregiver(caregiver.getId(), caregiver.getUser().getFullName(), caregiver.getUser().getProfilePhotoUrl(), ca == null ? null : ca.getCidade(), ca == null ? null : ca.getBairro(), ca == null ? null : ca.getEstado(), caregiver.getTempoExperiencia(), new LinkedHashSet<>(caregiver.getServicosOferecidos())),
      people.stream().map(this::toAssisted).toList(), addresses,
      new LinkedHashSet<>(caregiver.getServicosOferecidos()), new LinkedHashSet<>(List.of(DiaSemana.values())), new LinkedHashSet<>(List.of(HiringType.values()))
    );
  }

  @Transactional
  public ServiceRequestResponse create(UUID responsibleId, ServiceRequestCreateRequest request) {
    User responsible = requireResponsible(responsibleId);
    CaregiverProfile caregiver = requireCaregiver(request.caregiverId());
    AssistedPerson assisted = assistedRepository.findByIdAndResponsibleUser(request.assistedPersonId(), responsible).orElseThrow(() -> new BusinessException("Pessoa assistida não pertence ao responsável autenticado.", HttpStatus.FORBIDDEN));
    if (!request.careAddressId().equals(assisted.getId()) || !hasAddress(assisted.getEnderecoCuidado())) throw new BusinessException("Endereço do cuidado inválido.", HttpStatus.BAD_REQUEST);
    validate(request);
    List<ServiceRequest> pending = repository.findByResponsibleUserAndCaregiverUserAndAssistedPersonIdAndStatus(responsible, caregiver.getUser(), assisted.getId(), ServiceRequestStatus.PENDENTE);
    if (pending.stream().peek(this::expireIfNeeded).anyMatch(existing -> existing.getStatus() == ServiceRequestStatus.PENDENTE && overlaps(existing, request))) throw new BusinessException("Já existe uma solicitação pendente para este cuidador e pessoa assistida nesse período.", HttpStatus.CONFLICT);
    ServiceRequest entity = new ServiceRequest(); entity.setResponsibleUser(responsible); entity.setCaregiverUser(caregiver.getUser()); entity.setAssistedPerson(assisted); entity.setHiringType(request.hiringType());
    Set<LocalDate> dates = request.specificDates() == null ? Set.of() : new LinkedHashSet<>(request.specificDates()); entity.setSpecificDates(dates);
    entity.setStartDate(request.hiringType() == HiringType.PONTUAL ? dates.stream().min(LocalDate::compareTo).orElse(null) : request.startDate()); entity.setEndDate(request.hiringType() == HiringType.PERIODO_DETERMINADO ? request.endDate() : null);
    entity.setNeedsDescription(request.needsDescription().trim()); entity.setActivities(new LinkedHashSet<>(request.activities())); entity.setActivityOther(trim(request.activityOther())); entity.setAdditionalNotes(trim(request.additionalNotes())); entity.setNegotiationNotes(trim(request.negotiationNotes()));
    Set<ServiceRequestScheduleDay> schedule = new LinkedHashSet<>(); for (var item : request.scheduleDays() == null ? List.<ServiceRequestCreateRequest.ScheduleDayRequest>of() : request.scheduleDays()) { ServiceRequestScheduleDay day = new ServiceRequestScheduleDay(); day.setWeekday(item.weekday()); day.setStartTime(item.startTime()); day.setEndTime(item.endTime()); schedule.add(day); } entity.setScheduleDays(schedule);
    return toResponse(repository.save(entity));
  }

  @Transactional
  public ServiceRequestResponse find(UUID responsibleId, UUID id) { User owner=requireResponsible(responsibleId); ServiceRequest entity=repository.findByIdAndResponsibleUser(id, owner).orElseThrow(() -> new BusinessException("Solicitação não encontrada.", HttpStatus.NOT_FOUND)); expireIfNeeded(entity); return toResponse(entity); }
  @Transactional
  public List<ServiceRequestResponse> my(UUID responsibleId) { User owner=requireResponsible(responsibleId); return repository.findByResponsibleUserOrderByCreatedAtDesc(owner).stream().peek(this::expireIfNeeded).map(this::toResponse).toList(); }
  @Transactional
  public ServiceRequestResponse cancel(UUID responsibleId, UUID id) { User owner=requireResponsible(responsibleId); ServiceRequest entity=repository.findByIdAndResponsibleUser(id, owner).orElseThrow(() -> new BusinessException("Solicitação não encontrada.", HttpStatus.NOT_FOUND)); expireIfNeeded(entity); if(entity.getStatus()!=ServiceRequestStatus.PENDENTE) throw new BusinessException("Apenas solicitações pendentes podem ser canceladas."); entity.setStatus(ServiceRequestStatus.CANCELADA); entity.setCanceledAt(Instant.now()); return toResponse(entity); }

  private void validate(ServiceRequestCreateRequest r) {
    Set<LocalDate> dates=r.specificDates()==null?Set.of():r.specificDates(); List<ServiceRequestCreateRequest.ScheduleDayRequest> schedule=r.scheduleDays()==null?List.of():r.scheduleDays();
    if(r.hiringType()==HiringType.PONTUAL && dates.isEmpty()) throw new BusinessException("Informe ao menos uma data específica.");
    if(r.hiringType()!=HiringType.PONTUAL && r.startDate()==null) throw new BusinessException("Informe a data prevista de início.");
    if(r.hiringType()==HiringType.PERIODO_DETERMINADO && r.endDate()==null) throw new BusinessException("Informe a data prevista de término.");
    if(r.startDate()!=null && r.endDate()!=null && r.endDate().isBefore(r.startDate())) throw new BusinessException("A data de término não pode ser anterior à data de início.");
    if(r.hiringType()!=HiringType.PONTUAL && schedule.isEmpty()) throw new BusinessException("Selecione pelo menos um dia da semana.");
    if(schedule.isEmpty()) throw new BusinessException("Informe o horário do serviço.");
    if(schedule.stream().anyMatch(d -> !d.startTime().isBefore(d.endTime()))) throw new BusinessException("O horário final deve ser posterior ao horário inicial.");
    if(r.activities().contains(ServicoOferecido.OUTRO) && (r.activityOther()==null || r.activityOther().isBlank())) throw new BusinessException("Descreva a outra atividade esperada.");
  }
  private boolean overlaps(ServiceRequest e, ServiceRequestCreateRequest r) { LocalDate a1=e.getStartDate(), a2=e.getEndDate()==null?LocalDate.MAX:e.getEndDate(); Set<LocalDate> dates=r.specificDates()==null?Set.of():r.specificDates(); LocalDate b1=r.hiringType()==HiringType.PONTUAL?dates.stream().min(LocalDate::compareTo).orElse(LocalDate.MAX):r.startDate(); LocalDate b2=r.hiringType()==HiringType.PONTUAL?dates.stream().max(LocalDate::compareTo).orElse(LocalDate.MIN):(r.endDate()==null?LocalDate.MAX:r.endDate()); return a1!=null && !a1.isAfter(b2) && !b1.isAfter(a2); }
  private void expireIfNeeded(ServiceRequest e) { if(e.getStatus()==ServiceRequestStatus.PENDENTE && e.getExpiresAt().isBefore(Instant.now())) e.setStatus(ServiceRequestStatus.EXPIRADA); }
  private User requireResponsible(UUID id) { User u=userService.findById(id); if(u.getUserType()!=UserType.RESPONSAVEL && u.getUserType()!=UserType.FAMILY) throw new BusinessException("Apenas responsáveis podem enviar solicitações.", HttpStatus.FORBIDDEN); return u; }
  private CaregiverProfile requireCaregiver(UUID id) { return caregiverRepository.findById(id).filter(p -> (p.getUser().getUserType()==UserType.CUIDADOR || p.getUser().getUserType()==UserType.CAREGIVER) && "ACTIVE".equalsIgnoreCase(p.getUser().getStatus())).orElseThrow(() -> new BusinessException("Cuidador não encontrado.", HttpStatus.NOT_FOUND)); }
  private boolean hasAddress(AddressFields a) { return a!=null && a.getRua()!=null && a.getNumero()!=null && a.getBairro()!=null && a.getCidade()!=null && a.getEstado()!=null; }
  private ServiceRequestFormDataResponse.Assisted toAssisted(AssistedPerson p) { return new ServiceRequestFormDataResponse.Assisted(p.getId(),p.getNome(),p.getDataNascimento(),p.getGrauDependencia(),p.getMobilidade(),String.join(" • ", p.getGrauDependencia().name(), p.getMobilidade().name())); }
  private ServiceRequestFormDataResponse.CareAddress toAddress(AssistedPerson p) { AddressFields a=p.getEnderecoCuidado(); return new ServiceRequestFormDataResponse.CareAddress(p.getId(),p.getId(),a.getCep(),a.getRua(),a.getNumero(),a.getComplemento(),a.getBairro(),a.getCidade(),a.getEstado(),a.getPontoReferencia()); }
  private String formatAddress(AddressFields a) { return String.join(", ", a.getRua()+" "+a.getNumero(), a.getBairro(), a.getCidade()+" - "+a.getEstado()); }
  private ServiceRequestResponse toResponse(ServiceRequest e) { CaregiverProfile cp=caregiverRepository.findByUser(e.getCaregiverUser()).orElseThrow(); return new ServiceRequestResponse(e.getId(),e.getStatus(),e.getHiringType(),cp.getId(),e.getCaregiverUser().getFullName(),e.getCaregiverUser().getProfilePhotoUrl(),e.getAssistedPerson().getId(),e.getAssistedPerson().getNome(),formatAddress(e.getAssistedPerson().getEnderecoCuidado()),e.getStartDate(),e.getEndDate(),new LinkedHashSet<>(e.getSpecificDates()),e.getScheduleDays().stream().map(d -> new ServiceRequestResponse.Schedule(d.getWeekday(),d.getStartTime(),d.getEndTime())).toList(),e.getNeedsDescription(),new LinkedHashSet<>(e.getActivities()),e.getActivityOther(),e.getAdditionalNotes(),e.getNegotiationNotes(),e.getCreatedAt(),e.getExpiresAt(),e.getCanceledAt()); }
  private String trim(String v) { return v==null||v.isBlank()?null:v.trim(); }
}
