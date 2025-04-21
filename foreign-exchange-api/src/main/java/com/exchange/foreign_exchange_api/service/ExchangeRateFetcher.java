package com.exchange.foreign_exchange_api.service;

import com.exchange.foreign_exchange_api.exception.ExchangeRateNotFoundException;
import com.exchange.foreign_exchange_api.model.CurrencyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ExchangeRateFetcher {

  private final Logger LOGGER = LoggerFactory.getLogger(ExchangeRateFetcher.class);

  private final ReactiveRedisTemplate<String, Double> redisTemplate;

  public ExchangeRateFetcher(ReactiveRedisTemplate<String, Double> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public Mono<Double> getExchangeRate(String key, CurrencyCode currencyCode) {
    return redisTemplate
        .opsForHash()
        .get(key, currencyCode.name())
        .ofType(Double.class)
        .switchIfEmpty(
            Mono.error(
                new ExchangeRateNotFoundException(
                    "Exchange rate not found for currency: " + currencyCode)))
        .doOnError(
            error ->
                LOGGER.error("Error retrieving exchange rate {} from Redis", currencyCode, error));
  }
}
