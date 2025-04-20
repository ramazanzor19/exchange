package com.exchange.foreign_exchange_api.exception;

public class ExchangeRateNotFoundException extends RuntimeException {
  public ExchangeRateNotFoundException(String message) {
    super(message);
  }
}
