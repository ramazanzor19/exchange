package com.exchange.foreign_exchange_api.api;

import com.exchange.foreign_exchange_api.api.dto.ExchangeRateRequest;
import com.exchange.foreign_exchange_api.api.dto.ExchangeRateResponse;
import com.exchange.foreign_exchange_api.service.ExchangeRateService;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {

  private final ExchangeRateService exchangeRateService;

  public ExchangeRateController(ExchangeRateService exchangeRateService) {
    this.exchangeRateService = exchangeRateService;
  }

  @GetMapping
  public Mono<ExchangeRateResponse> getExchangeRate(@Valid ExchangeRateRequest request) {
    return exchangeRateService
        .getExchangeRate(request.source(), request.target())
        .map(
            rate ->
                new ExchangeRateResponse(request.source(), request.target(), rate, Instant.now()));
  }
}
