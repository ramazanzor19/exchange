package com.exchange.foreign_exchange_api.api.error;

import com.exchange.foreign_exchange_api.exception.ExchangeRateNotFoundException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ExchangeRateNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(ExchangeRateNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler({WebExchangeBindException.class, ServerWebInputException.class})
  public ResponseEntity<ErrorResponse> handleValidationExceptions(Exception ex) {
    String errorMessage =
        switch (ex) {
          case WebExchangeBindException bindEx ->
              bindEx.getFieldErrors().stream()
                  .map(error -> error.getField() + ": " + error.getDefaultMessage())
                  .collect(Collectors.joining(", "));
          case ServerWebInputException inputEx -> inputEx.getCause().getMessage();
          case null, default -> "Invalid request";
        };

    return ResponseEntity.badRequest()
        .body(new ErrorResponse("Validation failed: " + errorMessage));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("Unexpected error occurred"));
  }
}
