package br.com.cuidaplus.api.caregiver;

import br.com.cuidaplus.api.caregiver.CaregiverSearchService.CaregiverSearchFilters;
import br.com.cuidaplus.api.caregiver.dto.CaregiverDetailsResponse;
import br.com.cuidaplus.api.caregiver.dto.CaregiverLocationSuggestionResponse;
import br.com.cuidaplus.api.caregiver.dto.CaregiverSearchPageResponse;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.profile.DiaSemana;
import br.com.cuidaplus.api.profile.FormacaoCuidador;
import br.com.cuidaplus.api.profile.ModalidadeAtendimento;
import br.com.cuidaplus.api.profile.PeriodoDisponibilidade;
import br.com.cuidaplus.api.profile.ServicoOferecido;
import br.com.cuidaplus.api.profile.TempoExperiencia;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/caregivers")
public class CaregiverController {

  private final CaregiverSearchService caregiverSearchService;

  public CaregiverController(CaregiverSearchService caregiverSearchService) {
    this.caregiverSearchService = caregiverSearchService;
  }

  @GetMapping("/search")
  public CaregiverSearchPageResponse search(
    @RequestParam(required = false) String name,
    @RequestParam(required = false) String city,
    @RequestParam(required = false) String neighborhood,
    @RequestParam(required = false) String state,
    @RequestParam(required = false, name = "availabilityPeriods") List<String> availabilityPeriods,
    @RequestParam(required = false, name = "periods") List<String> periods,
    @RequestParam(required = false, name = "availabilityDays") List<String> availabilityDays,
    @RequestParam(required = false) List<String> services,
    @RequestParam(required = false) List<String> modalities,
    @RequestParam(required = false) String experienceRange,
    @RequestParam(required = false) List<String> formations,
    @RequestParam(required = false) Double originLat,
    @RequestParam(required = false) Double originLng,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "5") int size
  ) {
    return caregiverSearchService.search(new CaregiverSearchFilters(
      name,
      city,
      neighborhood,
      state,
      mergeEnums(PeriodoDisponibilidade.class, availabilityPeriods, periods),
      parseEnums(DiaSemana.class, availabilityDays),
      parseEnums(ServicoOferecido.class, services),
      parseEnums(ModalidadeAtendimento.class, modalities),
      parseEnum(TempoExperiencia.class, experienceRange),
      parseEnums(FormacaoCuidador.class, formations),
      originLat,
      originLng,
      page,
      size
    ));
  }

  @GetMapping("/locations")
  public List<CaregiverLocationSuggestionResponse> locations(@RequestParam(required = false) String query) {
    return caregiverSearchService.locations(query);
  }

  @GetMapping("/{id}")
  public CaregiverDetailsResponse details(
    @PathVariable UUID id,
    @RequestParam(required = false) Double originLat,
    @RequestParam(required = false) Double originLng
  ) {
    return caregiverSearchService.details(id, originLat, originLng);
  }

  @SafeVarargs
  private <E extends Enum<E>> Set<E> mergeEnums(Class<E> enumType, List<String>... values) {
    LinkedHashSet<E> result = new LinkedHashSet<>();
    for (List<String> value : values) {
      result.addAll(parseEnums(enumType, value));
    }
    return result;
  }

  private <E extends Enum<E>> Set<E> parseEnums(Class<E> enumType, List<String> values) {
    LinkedHashSet<E> result = new LinkedHashSet<>();
    if (values == null) {
      return result;
    }

    values.stream()
      .flatMap(value -> Arrays.stream(value.split(",")))
      .map(String::trim)
      .filter(value -> !value.isBlank())
      .map(value -> parseEnum(enumType, value))
      .forEach(result::add);

    return result;
  }

  private <E extends Enum<E>> E parseEnum(Class<E> enumType, String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    try {
      return Enum.valueOf(enumType, value.trim().toUpperCase());
    } catch (IllegalArgumentException exception) {
      throw new BusinessException("Filtro inválido: " + value + ".", HttpStatus.BAD_REQUEST);
    }
  }
}
