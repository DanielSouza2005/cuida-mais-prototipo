package br.com.cuidaplus.api.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserType {
  FAMILY("family"),
  CAREGIVER("caregiver");

  private final String apiValue;

  UserType(String apiValue) {
    this.apiValue = apiValue;
  }

  @JsonValue
  public String getApiValue() {
    return apiValue;
  }

  @JsonCreator
  public static UserType fromApiValue(String value) {
    for (UserType userType : values()) {
      if (userType.apiValue.equalsIgnoreCase(value) || userType.name().equalsIgnoreCase(value)) {
        return userType;
      }
    }

    throw new IllegalArgumentException("Tipo de usuário inválido.");
  }
}
