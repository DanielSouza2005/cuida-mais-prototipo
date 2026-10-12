package br.com.cuidaplus.api.profile;

import br.com.cuidaplus.api.audit.*;
import br.com.cuidaplus.api.auth.dto.AddressRequest;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.common.MessageResponse;
import br.com.cuidaplus.api.profile.dto.CaregiverAvailabilityUpdateRequest;
import br.com.cuidaplus.api.profile.dto.CaregiverExperienceUpdateRequest;
import br.com.cuidaplus.api.profile.dto.CaregiverModalitiesUpdateRequest;
import br.com.cuidaplus.api.profile.dto.CaregiverServicesUpdateRequest;
import br.com.cuidaplus.api.profile.dto.AssistedPersonUpdateRequest;
import br.com.cuidaplus.api.profile.dto.EmergencyContactUpdateRequest;
import br.com.cuidaplus.api.profile.dto.PersonalInfoUpdateRequest;
import br.com.cuidaplus.api.profile.dto.ResponsibleProfileUpdateRequest;
import br.com.cuidaplus.api.profile.dto.ProfilePhotoResponse;
import br.com.cuidaplus.api.profile.dto.ProfileResponse;
import br.com.cuidaplus.api.profile.dto.ProfileResponse.AddressResponse;
import br.com.cuidaplus.api.profile.dto.ProfileResponse.AssistedPersonResponse;
import br.com.cuidaplus.api.profile.dto.ProfileResponse.AvailabilityResponse;
import br.com.cuidaplus.api.profile.dto.ProfileResponse.CaregiverProfileResponse;
import br.com.cuidaplus.api.profile.dto.ProfileResponse.EmergencyContactResponse;
import br.com.cuidaplus.api.profile.dto.ProfileResponse.ResponsibleProfileResponse;
import br.com.cuidaplus.api.storage.ProfilePhotoStorageService;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserMapper;
import br.com.cuidaplus.api.user.UserService;
import br.com.cuidaplus.api.user.UserType;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfileService {

  private final UserService userService;
  private final UserMapper userMapper;
  private final ResponsibleProfileRepository responsibleProfileRepository;
  private final AssistedPersonRepository assistedPersonRepository;
  private final EmergencyContactRepository emergencyContactRepository;
  private final CaregiverProfileRepository caregiverProfileRepository;
  private final ProfilePhotoStorageService profilePhotoStorageService;
  private final AuditService audit;

  public ProfileService(
    UserService userService,
    UserMapper userMapper,
    ResponsibleProfileRepository responsibleProfileRepository,
    AssistedPersonRepository assistedPersonRepository,
    EmergencyContactRepository emergencyContactRepository,
    CaregiverProfileRepository caregiverProfileRepository,
    ProfilePhotoStorageService profilePhotoStorageService,
    AuditService audit
  ) {
    this.userService = userService;
    this.userMapper = userMapper;
    this.responsibleProfileRepository = responsibleProfileRepository;
    this.assistedPersonRepository = assistedPersonRepository;
    this.emergencyContactRepository = emergencyContactRepository;
    this.caregiverProfileRepository = caregiverProfileRepository;
    this.profilePhotoStorageService = profilePhotoStorageService;
    this.audit = audit;
  }

  @Transactional
  public ProfilePhotoResponse updateProfilePhoto(UUID userId, MultipartFile photo) {
    User user = requireCaregiver(userId);
    String profilePhotoUrl = profilePhotoStorageService.store(photo);
    if (profilePhotoUrl == null) {
      throw new BusinessException("Selecione uma foto para enviar.", HttpStatus.BAD_REQUEST);
    }
    user.setProfilePhotoUrl(profilePhotoUrl);
    auditProfile(user, "USUARIO", user.getId());
    return new ProfilePhotoResponse(profilePhotoUrl);
  }

  @Transactional
  public ProfilePhotoResponse deleteProfilePhoto(UUID userId) {
    User user = requireCaregiver(userId);
    user.setProfilePhotoUrl(null);
    auditProfile(user, "USUARIO", user.getId());
    return new ProfilePhotoResponse(null);
  }

  private User requireCaregiver(UUID userId) {
    User user = userService.findById(userId);
    if (user.getUserType() != UserType.CUIDADOR && user.getUserType() != UserType.CAREGIVER) {
      throw new BusinessException("A edição de foto está disponível apenas para cuidadores.", HttpStatus.FORBIDDEN);
    }
    return user;
  }

  @Transactional(readOnly = true)
  public ProfileResponse findMyProfile(UUID userId) {
    User user = userService.findById(userId);
    ResponsibleProfileResponse responsibleResponse = null;
    List<AssistedPersonResponse> assistedPersons = null;
    ResponsibleProfile responsibleProfile = responsibleProfileRepository.findByUser(user).orElse(null);
    if (responsibleProfile != null) {
      responsibleResponse = new ResponsibleProfileResponse(
        responsibleProfile.getParentesco(),
        responsibleProfile.getParentescoOutro(),
        responsibleProfile.getPreferenciaContato()
      );
      assistedPersons = assistedPersonRepository.findByResponsibleUser(user).stream()
        .map(this::toAssistedPersonResponse)
        .toList();
    }

    CaregiverProfileResponse caregiverResponse = null;
    CaregiverProfile profile = caregiverProfileRepository.findByUser(user).orElse(null);
    if (profile != null) {
      CaregiverAvailability availabilityData = safeAvailability(profile);
      AvailabilityResponse availability = new AvailabilityResponse(
        new LinkedHashSet<>(availabilityData.getDiasSemana()),
        new LinkedHashSet<>(availabilityData.getPeriodos()),
        availabilityData.getHorarioInicio(),
        availabilityData.getHorarioFim(),
        availabilityData.getObservacao()
      );
      caregiverResponse = new CaregiverProfileResponse(
        new LinkedHashSet<>(profile.getFormacoes()),
        profile.getFormacaoOutro(),
        profile.getTempoExperiencia(),
        profile.getExperiencia(),
        profile.getBiografia(),
        toAddressResponse(safeAddress(profile)),
        new LinkedHashSet<>(profile.getModalidades()),
        profile.getModalidadeOutro(),
        new LinkedHashSet<>(profile.getServicosOferecidos()),
        profile.getServicoOutro(),
        availability
      );
    }

    return new ProfileResponse(userMapper.toResponse(user), responsibleResponse, assistedPersons, caregiverResponse);
  }

  @Transactional
  public MessageResponse updatePersonalInfo(UUID userId, PersonalInfoUpdateRequest request) {
    User user = userService.findById(userId);

    user.setFullName(request.nome().trim());
    user.setPhone(UserService.optionalDigits(request.telefone()));
    auditProfile(user, "USUARIO", user.getId());

    return updated();
  }

  @Transactional
  public MessageResponse updateResponsibleProfile(UUID userId, ResponsibleProfileUpdateRequest request) {
    ResponsibleProfile profile = findResponsibleProfile(userId);
    profile.setParentesco(request.parentesco());
    profile.setParentescoOutro(trimToNull(request.parentescoOutro()));
    profile.setPreferenciaContato(request.preferenciaContato());
    auditProfile(profile.getUser(), "RESPONSAVEL", profile.getId());
    return updated();
  }

  @Transactional
  public MessageResponse updateAssistedPerson(UUID userId, UUID assistedPersonId, AssistedPersonUpdateRequest request) {
    AssistedPerson assistedPerson = findResponsibleAssistedPerson(userId, assistedPersonId);
    assistedPerson.setNome(request.nome().trim());
    assistedPerson.setDataNascimento(request.dataNascimento());
    assistedPerson.setGrauDependencia(request.grauDependencia());
    assistedPerson.setMobilidade(request.mobilidade());
    assistedPerson.setMobilidadeOutro(trimToNull(request.mobilidadeOutro()));
    assistedPerson.setAlergias(new LinkedHashSet<>(request.alergias()));
    assistedPerson.setAlergiasOutro(trimToNull(request.alergiasOutro()));
    assistedPerson.setAlergiasDetalhes(trimToNull(request.alergiasDetalhes()));
    assistedPerson.setRestricoesAlimentares(new LinkedHashSet<>(request.restricoesAlimentares()));
    assistedPerson.setRestricoesAlimentaresOutro(trimToNull(request.restricoesAlimentaresOutro()));
    assistedPerson.setRestricoesAlimentaresDetalhes(trimToNull(request.restricoesAlimentaresDetalhes()));
    assistedPerson.setMedicamentos(trimToNull(request.medicamentos()));
    assistedPerson.setObservacoes(trimToNull(request.observacoes()));
    auditProfile(assistedPerson.getResponsibleUser(), "PESSOA_ASSISTIDA", assistedPerson.getId());
    return updated();
  }

  @Transactional
  public MessageResponse updateCareAddress(UUID userId, UUID assistedPersonId, AddressRequest request) {
    AssistedPerson assistedPerson = findResponsibleAssistedPerson(userId, assistedPersonId);
    assistedPerson.setEnderecoCuidado(toAddress(request));
    auditProfile(assistedPerson.getResponsibleUser(), "PESSOA_ASSISTIDA", assistedPerson.getId());
    return updated();
  }

  @Transactional
  public MessageResponse updateEmergencyContact(UUID userId, UUID assistedPersonId, EmergencyContactUpdateRequest request) {
    AssistedPerson assistedPerson = findResponsibleAssistedPerson(userId, assistedPersonId);
    ResponsibleProfile responsibleProfile = findResponsibleProfile(userId);
    User user = responsibleProfile.getUser();
    if (request.isResponsibleContact() && user.getPhone() == null) {
      throw new BusinessException("Informe o telefone da conta ou cadastre outro contato de emergência.");
    }
    EmergencyContact contact = emergencyContactRepository
      .findByAssistedPerson(assistedPerson)
      .orElseGet(() -> {
        EmergencyContact newContact = new EmergencyContact();
        newContact.setAssistedPerson(assistedPerson);
        return newContact;
      });

    contact.setResponsibleContact(request.isResponsibleContact());
    contact.setNome(request.isResponsibleContact() ? user.getFullName() : request.nome().trim());
    contact.setTelefone(request.isResponsibleContact() ? user.getPhone() : UserService.onlyDigits(request.telefone()));
    contact.setVinculo(request.isResponsibleContact()
      ? resolveResponsibleRelationship(responsibleProfile)
      : request.vinculo().trim());
    emergencyContactRepository.save(contact);
    auditProfile(user, "CONTATO_EMERGENCIA", contact.getId());
    return updated();
  }

  @Transactional
  public MessageResponse updateCaregiverAddress(UUID userId, AddressRequest request) {
    CaregiverProfile profile = findCaregiverProfile(userId);
    profile.setEnderecoAtendimento(toAddress(request));
    auditProfile(profile.getUser(), "CUIDADOR", profile.getId());
    return updated();
  }

  @Transactional
  public MessageResponse updateCaregiverExperience(UUID userId, CaregiverExperienceUpdateRequest request) {
    CaregiverProfile profile = findCaregiverProfile(userId);
    LinkedHashSet<FormacaoCuidador> formacoes = CaregiverFormationPolicy.requireValid(
      request.formacoes(), request.formacaoOutro());
    profile.setTempoExperiencia(request.tempoExperiencia());
    profile.setFormacoes(formacoes);
    profile.setFormacaoOutro(formacoes.contains(FormacaoCuidador.OUTRO) ? trimToNull(request.formacaoOutro()) : null);
    profile.setBiografia(trimToNull(request.biografia()));
    auditProfile(profile.getUser(), "CUIDADOR", profile.getId());
    return updated();
  }

  @Transactional
  public MessageResponse updateCaregiverAvailability(UUID userId, CaregiverAvailabilityUpdateRequest request) {
    CaregiverProfile profile = findCaregiverProfile(userId);
    CaregiverAvailability availability = profile.getDisponibilidade();
    if (availability == null) {
      availability = new CaregiverAvailability();
      profile.setDisponibilidade(availability);
    }

    availability.setDiasSemana(new LinkedHashSet<>(request.diasSemana()));
    availability.setPeriodos(new LinkedHashSet<>(request.periodos()));
    availability.setHorarioInicio(request.horarioInicio());
    availability.setHorarioFim(request.horarioFim());
    availability.setObservacao(trimToNull(request.observacao()));
    auditProfile(profile.getUser(), "CUIDADOR", profile.getId());
    return updated();
  }

  @Transactional
  public MessageResponse updateCaregiverModalities(UUID userId, CaregiverModalitiesUpdateRequest request) {
    CaregiverProfile profile = findCaregiverProfile(userId);
    profile.setModalidades(new LinkedHashSet<>(request.modalidades()));
    profile.setModalidadeOutro(trimToNull(request.modalidadeOutro()));
    auditProfile(profile.getUser(), "CUIDADOR", profile.getId());
    return updated();
  }

  @Transactional
  public MessageResponse updateCaregiverServices(UUID userId, CaregiverServicesUpdateRequest request) {
    CaregiverProfile profile = findCaregiverProfile(userId);
    profile.setServicosOferecidos(new LinkedHashSet<>(request.servicosOferecidos()));
    profile.setServicoOutro(trimToNull(request.servicoOutro()));
    auditProfile(profile.getUser(), "CUIDADOR", profile.getId());
    return updated();
  }

  private CaregiverProfile findCaregiverProfile(UUID userId) {
    User user = userService.findById(userId);
    if (user.getUserType() != UserType.CUIDADOR && user.getUserType() != UserType.CAREGIVER) {
      throw new BusinessException("Perfil de cuidador não encontrado.", HttpStatus.FORBIDDEN);
    }

    return caregiverProfileRepository
      .findByUser(user)
      .orElseGet(() -> {
        CaregiverProfile profile = new CaregiverProfile();
        profile.setUser(user);
        return caregiverProfileRepository.save(profile);
      });
  }

  private void auditProfile(User actor, String entityType, UUID entityId) {
    audit.success(AuditAction.PERFIL_ALTERADO, AuditCategory.ASSISTENCIAL, actor, entityType, entityId, null, null);
  }

  private ResponsibleProfile findResponsibleProfile(UUID userId) {
    User user = userService.findById(userId);
    if (user.getUserType() != UserType.RESPONSAVEL && user.getUserType() != UserType.FAMILY) {
      throw new BusinessException("Perfil de responsável não encontrado.", HttpStatus.FORBIDDEN);
    }

    return responsibleProfileRepository
      .findByUser(user)
      .orElseThrow(() -> new BusinessException("Perfil de responsável não encontrado.", HttpStatus.NOT_FOUND));
  }

  private AssistedPerson findResponsibleAssistedPerson(UUID userId, UUID assistedPersonId) {
    User user = userService.findById(userId);
    if (user.getUserType() != UserType.RESPONSAVEL && user.getUserType() != UserType.FAMILY) {
      throw new BusinessException("Pessoa assistida não encontrada.", HttpStatus.FORBIDDEN);
    }

    return assistedPersonRepository
      .findByIdAndResponsibleUser(assistedPersonId, user)
      .orElseThrow(() -> new BusinessException("Pessoa assistida não encontrada.", HttpStatus.NOT_FOUND));
  }

  private AssistedPersonResponse toAssistedPersonResponse(AssistedPerson assistedPerson) {
    EmergencyContactResponse emergencyContact = emergencyContactRepository.findByAssistedPerson(assistedPerson)
      .map(contact -> new EmergencyContactResponse(
        contact.getNome(),
        contact.getTelefone(),
        contact.getVinculo(),
        contact.isResponsibleContact()
      ))
      .orElse(null);
    return new AssistedPersonResponse(
      assistedPerson.getId(),
      assistedPerson.getNome(),
      assistedPerson.getDataNascimento(),
      assistedPerson.getGrauDependencia(),
      assistedPerson.getMobilidade(),
      assistedPerson.getMobilidadeOutro(),
      new LinkedHashSet<>(assistedPerson.getAlergias()),
      assistedPerson.getAlergiasOutro(),
      assistedPerson.getAlergiasDetalhes(),
      new LinkedHashSet<>(assistedPerson.getRestricoesAlimentares()),
      assistedPerson.getRestricoesAlimentaresOutro(),
      assistedPerson.getRestricoesAlimentaresDetalhes(),
      assistedPerson.getMedicamentos(),
      assistedPerson.getObservacoes(),
      toAddressResponse(assistedPerson.getEnderecoCuidado()),
      emergencyContact
    );
  }

  private AddressResponse toAddressResponse(AddressFields address) {
    AddressFields safeAddress = address == null ? new AddressFields() : address;
    return new AddressResponse(
      safeAddress.getCep(),
      safeAddress.getRua(),
      safeAddress.getNumero(),
      safeAddress.getComplemento(),
      safeAddress.getBairro(),
      safeAddress.getCidade(),
      safeAddress.getEstado(),
      safeAddress.getPontoReferencia(),
      safeAddress.getLatitude(),
      safeAddress.getLongitude()
    );
  }

  private CaregiverAvailability safeAvailability(CaregiverProfile profile) {
    if (profile.getDisponibilidade() != null) {
      return profile.getDisponibilidade();
    }

    return new CaregiverAvailability();
  }

  private AddressFields safeAddress(CaregiverProfile profile) {
    if (profile.getEnderecoAtendimento() != null) {
      return profile.getEnderecoAtendimento();
    }

    return new AddressFields();
  }

  private AddressFields toAddress(AddressRequest request) {
    AddressFields address = new AddressFields();
    address.setCep(UserService.onlyDigits(request.cep()));
    address.setRua(request.rua().trim());
    address.setNumero(request.numero().trim());
    address.setComplemento(trimToNull(request.complemento()));
    address.setBairro(request.bairro().trim());
    address.setCidade(request.cidade().trim());
    address.setEstado(request.estado().trim().toUpperCase());
    address.setPontoReferencia(trimToNull(request.pontoReferencia()));
    address.setLatitude(toCoordinate(request.latitude()));
    address.setLongitude(toCoordinate(request.longitude()));
    return address;
  }

  private BigDecimal toCoordinate(Double value) {
    return value == null ? null : BigDecimal.valueOf(value);
  }

  private String resolveResponsibleRelationship(ResponsibleProfile profile) {
    if (profile.getParentescoOutro() != null && !profile.getParentescoOutro().isBlank()) {
      return profile.getParentescoOutro();
    }

    return profile.getParentesco().name();
  }

  private String trimToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }

  private MessageResponse updated() {
    return new MessageResponse("Perfil atualizado com sucesso.");
  }
}
