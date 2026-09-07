package br.com.cuidaplus.api.profile;

import br.com.cuidaplus.api.common.BusinessException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.http.HttpStatus;

public final class CaregiverFormationPolicy {
  private CaregiverFormationPolicy() {}

  public static LinkedHashSet<FormacaoCuidador> requireValid(Set<FormacaoCuidador> formations, String customFormation) {
    if (formations == null || formations.isEmpty()) {
      throw new BusinessException("Informe ao menos uma formação.", HttpStatus.BAD_REQUEST);
    }
    LinkedHashSet<FormacaoCuidador> normalized = new LinkedHashSet<>(formations);
    if (normalized.contains(FormacaoCuidador.OUTRO) && (customFormation == null || customFormation.isBlank())) {
      throw new BusinessException("Informe a formação personalizada.", HttpStatus.BAD_REQUEST);
    }
    return normalized;
  }
}
