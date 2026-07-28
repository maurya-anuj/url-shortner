// Translates application exceptions into standard ErrorResponse payloads
package com.urlshortener.exception;

import com.urlshortener.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(FieldError::getDefaultMessage)
            .orElse("Validation failed");
    return buildResponse(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(CodeAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleCodeAlreadyExists(CodeAlreadyExistsException ex) {
    return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(CodeNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleCodeNotFound(CodeNotFoundException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
  }

  private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
    ErrorResponse body =
        ErrorResponse.builder()
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .build();
    return ResponseEntity.status(status).body(body);
  }
}
