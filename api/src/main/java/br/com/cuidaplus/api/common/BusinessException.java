package br.com.cuidaplus.api.common;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

  private final HttpStatus status;
  private final String code;

  public BusinessException(String message) {
    this(message, HttpStatus.BAD_REQUEST, null);
  }

  public BusinessException(String message, HttpStatus status) {
    this(message, status, null);
  }

  public BusinessException(String message, HttpStatus status, String code) {
    super(message);
    this.status = status;
    this.code = code;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getCode() { return code; }
}
