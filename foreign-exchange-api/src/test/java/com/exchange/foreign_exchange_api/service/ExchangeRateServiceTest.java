package com.exchange.foreign_exchange_api.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.exchange.foreign_exchange_api.exception.ExchangeRateNotFoundException;
import com.exchange.foreign_exchange_api.model.CurrencyCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ExchangeRateServiceTest {

  private static final CurrencyCode USD = CurrencyCode.USD;
  private static final CurrencyCode EUR = CurrencyCode.EUR;
  private static final CurrencyCode GBP = CurrencyCode.GBP;
  private static final double EUR_RATE = 0.92;
  private static final double GBP_RATE = 0.79;

  private ExchangeRateFetcher exchangeRateFetcher;
  private ExchangeRateService exchangeRateService;

  @BeforeEach
  void setUp() {
    exchangeRateFetcher = Mockito.mock(ExchangeRateFetcher.class);
    exchangeRateService = new ExchangeRateService(exchangeRateFetcher);
  }

  @Test
  void getExchangeRate_sameCurrency_returnsOne() {
    StepVerifier.create(exchangeRateService.getExchangeRate(USD, USD))
        .expectNext(1.0)
        .verifyComplete();
  }

  @Test
  void getExchangeRate_sourceToTarget_returnsCorrectRate() {
    when(exchangeRateFetcher.getExchangeRate(any(), eq(EUR))).thenReturn(Mono.just(EUR_RATE));
    when(exchangeRateFetcher.getExchangeRate(any(), eq(GBP))).thenReturn(Mono.just(GBP_RATE));

    double expectedRate = GBP_RATE / EUR_RATE; // Cross-rate calculation

    StepVerifier.create(exchangeRateService.getExchangeRate(EUR, GBP))
        .expectNext(expectedRate)
        .verifyComplete();
  }

  @Test
  void getExchangeRate_sourceIsUSD_returnsTargetRate() {
    when(exchangeRateFetcher.getExchangeRate(any(), eq(GBP))).thenReturn(Mono.just(GBP_RATE));

    StepVerifier.create(exchangeRateService.getExchangeRate(USD, GBP))
        .expectNext(GBP_RATE)
        .verifyComplete();
  }

  @Test
  void getExchangeRate_targetIsUSD_returnsInverseRate() {
    when(exchangeRateFetcher.getExchangeRate(any(), eq(EUR))).thenReturn(Mono.just(EUR_RATE));

    double expectedRate = 1 / EUR_RATE;

    StepVerifier.create(exchangeRateService.getExchangeRate(EUR, USD))
        .expectNext(expectedRate)
        .verifyComplete();
  }

  @Test
  void getExchangeRate_sourceNotFound_propagatesFetcherError() {
    when(exchangeRateFetcher.getExchangeRate(any(), eq(CurrencyCode.EUR)))
        .thenReturn(Mono.error(new ExchangeRateNotFoundException("EUR not found")));
    when(exchangeRateFetcher.getExchangeRate(any(), eq(CurrencyCode.GBP)))
        .thenReturn(Mono.just(1.0));
    StepVerifier.create(exchangeRateService.getExchangeRate(CurrencyCode.EUR, CurrencyCode.GBP))
        .verifyErrorMatches(
            ex ->
                ex instanceof ExchangeRateNotFoundException
                    && ex.getMessage().contains("EUR not found"));
  }

  @Test
  void getExchangeRate_targetNotFound_throwsException() {
    when(exchangeRateFetcher.getExchangeRate(any(), eq(EUR))).thenReturn(Mono.just(EUR_RATE));
    when(exchangeRateFetcher.getExchangeRate(any(), eq(GBP)))
        .thenReturn(Mono.error(new ExchangeRateNotFoundException("Not found")));

    StepVerifier.create(exchangeRateService.getExchangeRate(EUR, GBP))
        .verifyErrorSatisfies(
            ex -> {
              Assertions.assertInstanceOf(ExchangeRateNotFoundException.class, ex);
              Assertions.assertTrue(ex.getMessage().contains("Not found"));
            });
  }

  @Test
  void getExchangeRate_redisError_propagatesError() {
    RuntimeException redisError = new RuntimeException("Redis error");
    when(exchangeRateFetcher.getExchangeRate(any(), eq(EUR))).thenReturn(Mono.error(redisError));
    when(exchangeRateFetcher.getExchangeRate(any(), eq(GBP))).thenReturn(Mono.error(redisError));
    StepVerifier.create(exchangeRateService.getExchangeRate(EUR, GBP))
        .verifyErrorMatches(ex -> ex.equals(redisError));
  }
}
