package br.com.cuidaplus.api.profile;

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
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserMapper;
import br.com.cuidaplus.api.user.UserService;
import br.com.cuidaplus.api.user.UserType;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

  private final UserService userService;
  private final UserMapper userMapper;
  private final ResponsibleProfileRepository responsibleProfileRepository;
  private final AssistedPersonRepository assistedPersonRepository;
  private final EmergencyContactRepository emergencyContactRepository;
  private final CaregiverProfileRepository caregiverProfileRepository;

  public ProfileService(
    UserService userService,
    UserMapper userMapper,
    ResponsibleProfileRepository responsibleProfileRepository,
    AssistedPersonRepository assistedPersonRepository,
    EmergencyContactRepository emergencyContactRepository,
    CaregiverProfileRepository caregiverProfileRepository
  ) {
    this.userService = userService;
    this.userMapper = userMapper;
    this.responsibleProfileRepository = responsibleProfileRepository;
    this.assistedPersonRepository = assistedPersonRepository;
    this.emergencyContactRepository = emergencyContactRepository;
    this.caregiverProfileRepository = caregiverProfileRepository;
  }

  @Transactional(readOnly = true)
  public Map<String, Object> findMyProfile(UUID userId) {
    User user = userService.findById(userId);
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("user", userMapper.toResponse(user));

    responsibleProfileRepository.findByUser(user).ifPresent(profile -> {
      Map<String, Object> responsibleProfile = new LinkedHashMap<>();
      responsibleProfile.put("parentesco", profile.getParentesco());
      responsibleProfile.put("parentescoOutro", profile.getParentescoOutro());
      responsibleProfile.put("preferenciaContato", profile.getPreferenciaContato());
      response.put("responsibleProfile", responsibleProfile);
      response.put("assistedPersons", assistedPersonRepository.findByResponsibleUser(user).stream().map(this::toAssistedPersonResponse).toList());
    });

    caregiverProfileRepository.findByUser(user).ifPresent(profile -> {
      CaregiverAvailability availabilityData = safeAvailability(profile);
      Map<String, Object> availability = new LinkedHashMap<>();
      availability.put("diasSemana", new LinkedHashSet<>(availabilityData.getDiasSemana()));
      availability.put("periodos", new LinkedHashSet<>(availabilityData.getPeriodos()));
      availability.put("horarioInicio", availabilityData.getHorarioInicio());
      availability.put("horarioFim", availabilityData.getHorarioFim());
      availability.put("observacao", availabilityData.getObservacao());

      Map<String, Object> caregiverProfile = new LinkedHashMap<>();
      caregiverProfile.put("formacao", profile.getFormacao());
      caregiverProfile.put("formacoes", new LinkedHashSet<>(profile.getFormacoes()));
      caregiverProfile.put("formacaoOutro", profile.getFormacaoOutro());
      caregiverProfile.put("tempoExperiencia", profile.getTempoExperiencia());
      caregiverProfile.put("experiencia", profile.getExperiencia());
      caregiverProfile.put("biografia", profile.getBiografia());
      caregiverProfile.put("enderecoAtendimento", toAddressResponse(safeAddress(profile)));
      caregiverProfile.put("modalidades", new LinkedHashSet<>(profile.getModalidades()));
      caregiverProfile.put("modalidadeOutro", profile.getModalidadeOutro());
      caregiverProfile.put("servicosOferecidos", new LinkedHashSet<>(profile.getServicosOferecidos()));
      caregiverProfile.put("servicoOutro", profile.getServicoOutro());
      caregiverProfile.put("disponibilidade", availability);
      response.put("caregiverProfile", caregiverProfile);
    });

    return response;
  }

  @Transactional
  public MessageResponse updatePersonalInfo(UUID userId, PersonalInfoUpdateRequest request) {
    User user = userService.findById(userId);

    user.setFullName(request.nome().trim());
    user.setPhone(UserService.onlyDigits(request.telefone()));

    return updated();
  }

  @Transactional
  public MessageResponse updateResponsibleProfile(UUID userId, ResponsibleProfileUpdateRequest request) {
    ResponsibleProfile profile = findResponsibleProfile(userId);
    profile.setParentesco(request.parentesco());
    profile.setParentescoOutro(trimToNull(request.parentescoOutro()));
    profile.setPreferenciaContato(request.preferenciaContato());
    return updated();
  }

  @Transactional
  public MessageResponse updateAssistedPerson(UUID userId, UUID assistedPersonId, AssistedPersonUpdateRequest request) {
    AssistedPerson assistedPerson = findResponsibleAssistedPerson(userId, assistedPersonId);
    assistedPerson.setNome(request.nome().trim());
    assistedPerson.setCpf(optionalDigits(request.cpf()));
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
    return updated();
  }

  @Transactional
  public MessageResponse updateCareAddress(UUID userId, UUID assistedPersonId, AddressRequest request) {
    AssistedPerson assistedPerson = findResponsibleAssistedPerson(userId, assistedPersonId);
    assistedPerson.setEnderecoCuidado(toAddress(request));
    return updated();
  }

  @Transactional
  public MessageResponse updateEmergencyContact(UUID userId, UUID assistedPersonId, EmergencyContactUpdateRequest request) {
    AssistedPerson assistedPerson = findResponsibleAssistedPerson(userId, assistedPersonId);
    ResponsibleProfile responsibleProfile = findResponsibleProfile(userId);
    User user = responsibleProfile.getUser();
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
    return updated();
  }

  @Transactional
  public MessageResponse updateCaregiverAddress(UUID userId, AddressRequest request) {
    CaregiverProfile profile = findCaregiverProfile(userId);
    profile.setEnderecoAtendimento(toAddress(request));
    return updated();
  }

  @Transactional
  public MessageResponse updateCaregiverExperience(UUID userId, CaregiverExperienceUpdateRequest request) {
    CaregiverProfile profile = findCaregiverProfile(userId);
    LinkedHashSet<FormacaoCuidador> formacoes = normalizeFormacoes(request.formacoes(), request.formacao());
    profile.setTempoExperiencia(request.tempoExperiencia());
    profile.setFormacao(formacoes.stream().findFirst().orElse(null));
    profile.setFormacoes(formacoes);
    profile.setFormacaoOutro(formacoes.contains(FormacaoCuidador.OUTRO) ? trimToNull(request.formacaoOutro()) : null);
    profile.setBiografia(trimToNull(request.biografia()));
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
    return updated();
  }

  @Transactional
  public MessageResponse updateCaregiverModalities(UUID userId, CaregiverModalitiesUpdateRequest request) {
    CaregiverProfile profile = findCaregiverProfile(userId);
    profile.setModalidades(new LinkedHashSet<>(request.modalidades()));
    profile.setModalidadeOutro(trimToNull(request.modalidadeOutro()));
    return updated();
  }

  @Transactional
  public MessageResponse updateCaregiverServices(UUID userId, CaregiverServicesUpdateRequest request) {
    CaregiverProfile profile = findCaregiverProfile(userId);
    profile.setServicosOferecidos(new LinkedHashSet<>(request.servicosOferecidos()));
    profile.setServicoOutro(trimToNull(request.servicoOutro()));
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

  private Map<String, Object> toAssistedPersonResponse(AssistedPerson assistedPerson) {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("id", assistedPerson.getId());
    response.put("nome", assistedPerson.getNome());
    response.put("cpf", assistedPerson.getCpf());
    response.put("dataNascimento", assistedPerson.getDataNascimento());
    response.put("grauDependencia", assistedPerson.getGrauDependencia());
    response.put("mobilidade", assistedPerson.getMobilidade());
    response.put("mobilidadeOutro", assistedPerson.getMobilidadeOutro());
    response.put("alergias", new LinkedHashSet<>(assistedPerson.getAlergias()));
    response.put("alergiasOutro", assistedPerson.getAlergiasOutro());
    response.put("alergiasDetalhes", assistedPerson.getAlergiasDetalhes());
    response.put("restricoesAlimentares", new LinkedHashSet<>(assistedPerson.getRestricoesAlimentares()));
    response.put("restricoesAlimentaresOutro", assistedPerson.getRestricoesAlimentaresOutro());
    response.put("restricoesAlimentaresDetalhes", assistedPerson.getRestricoesAlimentaresDetalhes());
    response.put("medicamentos", assistedPerson.getMedicamentos());
    response.put("observacoes", assistedPerson.getObservacoes());
    response.put("enderecoCuidado", toAddressResponse(assistedPerson.getEnderecoCuidado()));
    emergencyContactRepository.findByAssistedPerson(assistedPerson).ifPresent(contact -> {
      Map<String, Object> emergencyContact = new LinkedHashMap<>();
      emergencyContact.put("nome", contact.getNome());
      emergencyContact.put("telefone", contact.getTelefone());
      emergencyContact.put("vinculo", contact.getVinculo());
      emergencyContact.put("isResponsibleContact", contact.isResponsibleContact());
      response.put("contatoEmergencia", emergencyContact);
    });
    return response;
  }

  private Map<String, Object> toAddressResponse(AddressFields address) {
    AddressFields safeAddress = address == null ? new AddressFields() : address;
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("cep", safeAddress.getCep());
    response.put("rua", safeAddress.getRua());
    response.put("numero", safeAddress.getNumero());
    response.put("complemento", safeAddress.getComplemento());
    response.put("bairro", safeAddress.getBairro());
    response.put("cidade", safeAddress.getCidade());
    response.put("estado", safeAddress.getEstado());
    response.put("pontoReferencia", safeAddress.getPontoReferencia());
    return response;
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
    return address;
  }

  private String optionalDigits(String value) {
    String digits = UserService.onlyDigits(value);
    return digits.isBlank() ? null : digits;
  }

  private LinkedHashSet<FormacaoCuidador> normalizeFormacoes(java.util.Set<FormacaoCuidador> formacoes, FormacaoCuidador formacao) {
    if (formacoes != null) {
      return new LinkedHashSet<>(formacoes);
    }

    LinkedHashSet<FormacaoCuidador> normalized = new LinkedHashSet<>();
    if (formacao != null) {
      normalized.add(formacao);
    }

    return normalized;
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
