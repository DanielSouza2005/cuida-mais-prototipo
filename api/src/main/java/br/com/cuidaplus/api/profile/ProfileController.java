package br.com.cuidaplus.api.profile;

import br.com.cuidaplus.api.security.AuthenticatedUser;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserMapper;
import br.com.cuidaplus.api.user.UserService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

  private final UserService userService;
  private final UserMapper userMapper;
  private final ResponsibleProfileRepository responsibleProfileRepository;
  private final AssistedPersonRepository assistedPersonRepository;
  private final EmergencyContactRepository emergencyContactRepository;
  private final CaregiverProfileRepository caregiverProfileRepository;

  public ProfileController(
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

  @GetMapping("/me")
  public Map<String, Object> me() {
    User user = userService.findById(AuthenticatedUser.id());
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
      Map<String, Object> availability = new LinkedHashMap<>();
      availability.put("diasSemana", profile.getDisponibilidade().getDiasSemana());
      availability.put("periodos", profile.getDisponibilidade().getPeriodos());
      availability.put("horarioInicio", profile.getDisponibilidade().getHorarioInicio());
      availability.put("horarioFim", profile.getDisponibilidade().getHorarioFim());
      availability.put("observacao", profile.getDisponibilidade().getObservacao());

      Map<String, Object> caregiverProfile = new LinkedHashMap<>();
      caregiverProfile.put("formacao", profile.getFormacao());
      caregiverProfile.put("formacaoOutro", profile.getFormacaoOutro());
      caregiverProfile.put("experiencia", profile.getExperiencia());
      caregiverProfile.put("biografia", profile.getBiografia());
      caregiverProfile.put("enderecoAtendimento", toAddressResponse(profile.getEnderecoAtendimento()));
      caregiverProfile.put("modalidades", profile.getModalidades());
      caregiverProfile.put("modalidadeOutro", profile.getModalidadeOutro());
      caregiverProfile.put("servicosOferecidos", profile.getServicosOferecidos());
      caregiverProfile.put("servicoOutro", profile.getServicoOutro());
      caregiverProfile.put("disponibilidade", availability);
      response.put("caregiverProfile", caregiverProfile);
    });

    return response;
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
    response.put("alergias", assistedPerson.getAlergias());
    response.put("alergiasOutro", assistedPerson.getAlergiasOutro());
    response.put("alergiasDetalhes", assistedPerson.getAlergiasDetalhes());
    response.put("restricoesAlimentares", assistedPerson.getRestricoesAlimentares());
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
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("cep", address.getCep());
    response.put("rua", address.getRua());
    response.put("numero", address.getNumero());
    response.put("complemento", address.getComplemento());
    response.put("bairro", address.getBairro());
    response.put("cidade", address.getCidade());
    response.put("estado", address.getEstado());
    response.put("pontoReferencia", address.getPontoReferencia());
    return response;
  }
}
