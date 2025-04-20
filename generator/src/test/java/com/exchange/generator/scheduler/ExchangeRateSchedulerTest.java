package com.exchange.generator.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

import com.exchange.generator.model.ExchangeRateResponse;
import com.exchange.generator.publisher.ExchangeRatePublisher;
import com.exchange.generator.service.CurrencyLayerService;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class ExchangeRateSchedulerTest {

  private CurrencyLayerService currencyLayerService;
  private ExchangeRatePublisher exchangeRatePublisher;
  private ExchangeRateScheduler scheduler;

  @BeforeEach
  void setUp() {
    currencyLayerService = Mockito.mock(CurrencyLayerService.class);
    exchangeRatePublisher = Mockito.mock(ExchangeRatePublisher.class);

    scheduler = new ExchangeRateScheduler(currencyLayerService, exchangeRatePublisher);
  }

  @Test
  void fetchAndPublishRates_shouldPublishTransformedRates() {
    Map<String, Double> inputRates = Map.of("USDEUR", 0.92, "USDGBP", 0.79);
    Map<String, Double> expectedRates = Map.of("EUR", 0.92, "GBP", 0.79);

    Mockito.when(currencyLayerService.getExchangeRate())
        .thenReturn(Mono.just(new ExchangeRateResponse(true, inputRates)));
    Mockito.when(exchangeRatePublisher.publishExchangeRates("exchangeRate", expectedRates))
        .thenReturn(Mono.just(true));

    StepVerifier.create(scheduler.fetchAndPublishRates()).expectNext(true).verifyComplete();
  }

  @Test
  void fetchAndPublishRates_shouldHandleApiFailure() {
    Mockito.when(currencyLayerService.getExchangeRate())
        .thenReturn(Mono.just(new ExchangeRateResponse(false, Map.of())));

    StepVerifier.create(scheduler.fetchAndPublishRates()).verifyComplete();

    Mockito.verify(exchangeRatePublisher, never()).publishExchangeRates(any(), any());
  }
}
