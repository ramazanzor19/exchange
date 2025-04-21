package com.exchange.foreign_exchange_api.service;

import com.exchange.foreign_exchange_api.model.CurrencyCode;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ExchangeRateService {

  private static final String EXCHANGE_RATE_KEY = "exchange:rates";
  private final ExchangeRateFetcher exchangeRateFetcher;

  public ExchangeRateService(ExchangeRateFetcher exchangeRateFetcher) {
    this.exchangeRateFetcher = exchangeRateFetcher;
  }

  public Mono<Double> getExchangeRate(CurrencyCode source, CurrencyCode target) {
    if (source == target) {
      return Mono.just(1.0);
    }

    return Mono.zip(getUsdRate(source), getUsdRate(target))
        .map(
            rates -> {
              double sourceToUsd = rates.getT1();
              double targetToUsd = rates.getT2();
              return targetToUsd / sourceToUsd; // Cross-rate calculation
            });
  }

  private Mono<Double> getUsdRate(CurrencyCode currency) {
    if (currency == CurrencyCode.USD) {
      return Mono.just(1.0);
    }
    return exchangeRateFetcher.getExchangeRate(EXCHANGE_RATE_KEY, currency);
  }
}
