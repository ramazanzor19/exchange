package com.exchange.foreign_exchange_api.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.exchange.foreign_exchange_api.exception.ExchangeRateNotFoundException;
import com.exchange.foreign_exchange_api.model.CurrencyCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ExchangeRateFetcherTest {

  private static final String TEST_KEY = "exchange:rates";
  private static final CurrencyCode TEST_CURRENCY = CurrencyCode.EUR;
  private static final Double TEST_RATE = 0.92;
  private ReactiveHashOperations<String, Object, Object> hashOperations;
  private ExchangeRateFetcher exchangeRateFetcher;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    ReactiveRedisTemplate<String, Double> redisTemplate = Mockito.mock(ReactiveRedisTemplate.class);
    hashOperations = Mockito.mock(ReactiveHashOperations.class);
    exchangeRateFetcher = new ExchangeRateFetcher(redisTemplate);

    when(redisTemplate.opsForHash()).thenReturn(hashOperations);
  }

  @Test
  void getExchangeRate_shouldReturnRate_whenExistsInRedis() {
    when(hashOperations.get(anyString(), any())).thenReturn(Mono.just(TEST_RATE));

    StepVerifier.create(exchangeRateFetcher.getExchangeRate(TEST_KEY, TEST_CURRENCY))
        .expectNext(TEST_RATE)
        .verifyComplete();

    verify(hashOperations).get(TEST_KEY, TEST_CURRENCY);
  }

  @Test
  void getExchangeRate_shouldThrowNotFound_whenRateNotFound() {
    when(hashOperations.get(anyString(), any())).thenReturn(Mono.empty());

    StepVerifier.create(exchangeRateFetcher.getExchangeRate(TEST_KEY, TEST_CURRENCY))
        .verifyErrorSatisfies(
            error -> {
              Assertions.assertInstanceOf(ExchangeRateNotFoundException.class, error);
              Assertions.assertTrue(error.getMessage().contains(TEST_CURRENCY.toString()));
            });

    verify(hashOperations).get(TEST_KEY, TEST_CURRENCY);
  }

  @Test
  void getExchangeRate_shouldPropagateError_whenRedisFails() {
    RuntimeException redisError = new RuntimeException("Redis connection failed");
    when(hashOperations.get(anyString(), any())).thenReturn(Mono.error(redisError));

    StepVerifier.create(exchangeRateFetcher.getExchangeRate(TEST_KEY, TEST_CURRENCY))
        .verifyErrorMatches(error -> error.equals(redisError));

    verify(hashOperations).get(TEST_KEY, TEST_CURRENCY);
  }

  @Test
  void getExchangeRate_shouldFilterNonDoubleValues() {
    when(hashOperations.get(anyString(), any())).thenReturn(Mono.just("not-a-double"));

    StepVerifier.create(exchangeRateFetcher.getExchangeRate(TEST_KEY, TEST_CURRENCY))
        .verifyError(ExchangeRateNotFoundException.class);
  }
}
