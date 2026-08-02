package br.com.cuidaplus.api.notification;

import br.com.cuidaplus.api.user.UserType;

public enum NotificationAudience {
  CUIDADOR,
  RESPONSAVEL,
  AMBOS;

  public boolean accepts(UserType userType) {
    boolean caregiver = userType == UserType.CUIDADOR || userType == UserType.CAREGIVER;
    boolean responsible = userType == UserType.RESPONSAVEL || userType == UserType.FAMILY;
    return this == AMBOS || (this == CUIDADOR && caregiver) || (this == RESPONSAVEL && responsible);
  }
}
