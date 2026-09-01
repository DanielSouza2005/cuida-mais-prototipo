package br.com.cuidaplus.api.common;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiError> handleBusinessException(BusinessException exception) {
    return ResponseEntity
      .status(exception.getStatus())
      .body(new ApiError(Instant.now(), exception.getStatus().value(), exception.getCode(), exception.getMessage(), Map.of()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException exception) {
    Map<String, String> fields = new LinkedHashMap<>();
    for (FieldError error : exception.getBindingResult().getFieldErrors()) {
      fields.putIfAbsent(error.getField(), error.getDefaultMessage());
    }

    return ResponseEntity
      .badRequest()
      .body(new ApiError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", "Dados inválidos.", fields));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleUnreadableMessage() {
    return ResponseEntity
      .badRequest()
      .body(new ApiError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "INVALID_REQUEST_BODY", "Corpo da requisição inválido.", Map.of()));
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiError> handleUploadTooLarge() {
    return ResponseEntity
      .status(HttpStatus.PAYLOAD_TOO_LARGE)
      .body(new ApiError(Instant.now(), HttpStatus.PAYLOAD_TOO_LARGE.value(), "UPLOAD_TOO_LARGE", "A foto deve ter no máximo 5 MB.", Map.of()));
  }

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  public ResponseEntity<ApiError> handleOptimisticLock() {
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(new ApiError(Instant.now(), HttpStatus.CONFLICT.value(), "OPTIMISTIC_LOCK", "Este registro foi atualizado em outro dispositivo. Recarregue os dados.", Map.of()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnexpectedException() {
    return ResponseEntity
      .internalServerError()
      .body(new ApiError(Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR", "Erro interno.", Map.of()));
  }
}
