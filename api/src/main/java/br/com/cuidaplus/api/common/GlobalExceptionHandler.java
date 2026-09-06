package br.com.cuidaplus.api.common;

import br.com.cuidaplus.api.profile.CaregiverApprovalStatus;
import br.com.cuidaplus.api.profile.ResponsibleApprovalStatus;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger LOGGER = LoggerFactory
    .getLogger(GlobalExceptionHandler.class);

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

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiError> handleInvalidQueryParameter(MethodArgumentTypeMismatchException exception) {
    String parameter = exception.getName();
    boolean approvalStatus = exception.getRequiredType() == CaregiverApprovalStatus.class
      || exception.getRequiredType() == ResponsibleApprovalStatus.class;
    String message = approvalStatus
      ? "Situação de aprovação inválida."
      : "Parâmetro de consulta inválido.";
    return ResponseEntity
      .badRequest()
      .body(new ApiError(
        Instant.now(),
        HttpStatus.BAD_REQUEST.value(),
        "INVALID_QUERY_PARAMETER",
        message,
        Map.of(parameter, message)
      ));
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
  public ResponseEntity<ApiError> handleUnexpectedException(Exception exception) {
    LOGGER.error("Erro inesperado ao processar requisição.", exception);
    return ResponseEntity
      .internalServerError()
      .body(new ApiError(Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR", "Erro interno.", Map.of()));
  }
}
